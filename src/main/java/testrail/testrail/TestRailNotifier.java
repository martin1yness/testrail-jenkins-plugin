/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testrail.testrail;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.DirScanner;
import hudson.util.FileVisitor;
import hudson.util.ListBoxModel;
import hudson.tasks.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import testrail.testrail.JunitResults.JUnitResultFileProcessor;
import testrail.testrail.JunitResults.JUnitResults;
import testrail.testrail.JunitResults.Testcase;
import testrail.testrail.JunitResults.Testsuite;
import testrail.testrail.TestRailObjects.*;
import testrail.testrail.testng.TestNGCase;
import testrail.testrail.testng.TestNGResultFileProcessor;
import testrail.testrail.testng.TestNGSaxParser;
import testrail.testrail.testng.TestNGSuite;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestRailNotifier extends Notifier {

    private int testrailProject;
    private String junitResultsGlob;
    private String testNGResultGlob;
    private String testrailMilestone;
    private boolean enableMilestone;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TestRailNotifier(int testrailProject, String junitResultsGlob, String testNGResultGlob, String testrailMilestone, boolean enableMilestone) {
        this.testrailProject = testrailProject;
        this.junitResultsGlob = junitResultsGlob;
		this.testNGResultGlob = testNGResultGlob;
        this.testrailMilestone = testrailMilestone;
        this.enableMilestone = enableMilestone;
    }

    public void setTestrailProject(int project) { this.testrailProject = project;}
    public int getTestrailProject() { return this.testrailProject; }
    public void setJunitResultsGlob(String glob) { this.junitResultsGlob = glob; }
	public String getJunitResultsGlob() { return this.junitResultsGlob; }
	public String getTestNGResultGlob() { return testNGResultGlob; }
	public void setTestNGResultGlob(String testNGResultGlob) { this.testNGResultGlob = testNGResultGlob; }
    public String getTestrailMilestone() { return this.testrailMilestone; }
    public void setTestrailMilestone(String milestone) { this.testrailMilestone = milestone; }
    public void setEnableMilestone(boolean mstone) {this.enableMilestone = mstone; }
    public boolean getEnableMilestone() { return  this.enableMilestone; }

    @Override
    public boolean perform(final AbstractBuild build, Launcher launcher, final BuildListener listener)
            throws IOException, InterruptedException {
		listener.getLogger().println("---------------------------- [TestRail Plugin] ----------------------------");

        TestRailClient  testrail = getDescriptor().getTestrailInstance();
        testrail.setHost(getDescriptor().getTestrailHost());
        testrail.setUser(getDescriptor().getTestrailUser());
        testrail.setPassword(getDescriptor().getTestrailPassword());
        listener.getLogger().println("[TestRail Plugin] Connecting to TestRail: http://"+testrail.getHost()+", with user: " + testrail.getUser());

        ExistingTestCases testCases = null;
        try {
            testCases = new ExistingTestCases(testrail, this.testrailProject);
            listener.getLogger().println("[TestRail Plugin] Found ("+testCases.getCases().size()+") test cases for project "+this.testrailProject);
            for(Map.Entry<Suite, List<Case>> e: testCases.getCases().entrySet()) {
                StringBuilder sb = new StringBuilder();
                for(Case c: e.getValue()) {
                    sb.append(c.getTitle()).append(", ");
                }
                listener.getLogger().println("[TestRail Plugin] Suite `"+e.getKey().getName()+"`, cases: " + sb.toString());
            }
        } catch (ElementNotFoundException e) {
            listener.getLogger().println("Cannot find project on TestRail server. Please check your Jenkins job and system configurations.");
            return false;
        }

        String[] caseNames = null;
        try {
            caseNames = testCases.listTestCases();
        } catch (ElementNotFoundException e) {
            listener.getLogger().println("Failed to list test cases");
            listener.getLogger().println("Element not found:" + e.getMessage());
        }

        final Results results = new Results();

		Results junitResults = Utils.processResultsFiles(junitResultsGlob,
				new JUnitResultFileProcessor(listener.getLogger(), testCases),
				testCases, build.getWorkspace(), listener.getLogger());

		Results testNgResults = Utils.processResultsFiles(testNGResultGlob,
				new TestNGResultFileProcessor(listener.getLogger(), testCases),
				testCases, build.getWorkspace(), listener.getLogger());

		results.merge(junitResults);
		results.merge(testNgResults);

        listener.getLogger().println("Uploading results to TestRail.");
        String runComment = "Automated results from Jenkins: " + BuildWrapper.all().jenkins.getRootUrl() + build.getUrl().toString();
        String milestoneId = testrailMilestone;

        Map<Integer, Integer> suiteIdToRunIdMap = new HashMap<Integer, Integer>();
        Map<Integer, Results> suiteIdToResultsMap = new HashMap<Integer, Results>();
        for(Result result: results.getResults()) {
            Suite testRailSuite = testCases.getCasesSuite(result.getCaseId());
            if(!suiteIdToRunIdMap.containsKey(testRailSuite.getId())) {
                int runId = testrail.addRun(testCases.getProjectId(), testRailSuite.getId(), milestoneId, runComment);
                suiteIdToRunIdMap.put(testRailSuite.getId(), runId);
                suiteIdToResultsMap.put(testRailSuite.getId(), new Results());
            }
			listener.getLogger().println("Result["+result.getStatus().name()+"]: "
					+ testRailSuite.getName() + " -> " + result.getCaseObj().getTitle());
            suiteIdToResultsMap.get(testRailSuite.getId()).addResult(result);
        }

        boolean buildResult = false;
		if(suiteIdToResultsMap.isEmpty())
			listener.getLogger().println("No test results were found, or none could be mapped to TestRail!");
        for(Integer suiteId: suiteIdToResultsMap.keySet()) {
            int runId = suiteIdToRunIdMap.get(suiteId);
            TestRailResponse response = testrail.addResultsForCases(runId, suiteIdToResultsMap.get(suiteId));
            buildResult = (200 == response.getStatus());
            if (buildResult) {
                listener.getLogger().println("Successfully uploaded test results.");
            } else {
                listener.getLogger().println("Failed to add results to TestRail.");
                listener.getLogger().println("status: " + response.getStatus());
                listener.getLogger().println("body :\n" + response.getBody());
            }
            testrail.closeRun(runId);
        }

        return buildResult;
    }


    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE; //null;
    }

    /**
     * Descriptor for {@link TestRailNotifier}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/TestRailRecorder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String testrailHost = "";
        private String testrailUser = "";
        private String testrailPassword = "";
        private TestRailClient testrail = new TestRailClient("", "", "");

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckTestrailProject(@QueryParameter int value)
                throws IOException, ServletException {
            testrail.setHost(getTestrailHost());
            testrail.setUser(getTestrailUser());
            testrail.setPassword(getTestrailPassword());
            if (getTestrailHost().isEmpty() || getTestrailUser().isEmpty() || getTestrailPassword().isEmpty() || !testrail.serverReachable() || !testrail.authenticationWorks()) {
                return FormValidation.warning("Please fix your TestRail configuration in Manage Jenkins -> Configure System.");
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillTestrailProjectItems() {
            testrail.setHost(getTestrailHost());
            testrail.setUser(getTestrailUser());
            testrail.setPassword(getTestrailPassword());
            ListBoxModel items = new ListBoxModel();
            try {
                for (Project prj : testrail.getProjects()) {
                    if(prj != null)
                        items.add(prj.getName(), prj.getStringId());
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to load testrail projects: " + e.getMessage(), e);
            }
            return items;
        }

        public FormValidation doCheckJunitResultsGlob(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.warning("Please select test result path.");
            // TODO: Should we check to see if the files exist? Probably not.
            return FormValidation.ok();
        }

		public FormValidation doCheckTestNGResultGlob(@QueryParameter String value)
				throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.warning("Please enter testng result XML file location.");
			final DirScanner scanner = new DirScanner.Glob(value, null);
			final AtomicBoolean fileFound = new AtomicBoolean(false);
			scanner.scan(new File("."), new FileVisitor() {
				@Override public void visit(File file, String s) throws IOException {
					fileFound.set(true);
				}
			});
			if(!fileFound.get())
				return FormValidation.warning("Could not find file: " + value);

			return FormValidation.ok();
		}

        public FormValidation doCheckTestrailHost(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.warning("Please add your TestRail host URI.");
            }
            // TODO: There is probably a better way to do URL validation.
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                return FormValidation.error("Host must be a valid URL.");
            }
            testrail.setHost(value);
            testrail.setUser("");
            testrail.setPassword("");
            if (!testrail.serverReachable()) {
                return FormValidation.error("Host is not reachable.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTestrailUser(@QueryParameter String value,
                                                  @QueryParameter String testrailHost,
                                                  @QueryParameter String testrailPassword)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.warning("Please add your user's email address.");
            }
            if (testrailPassword.length() > 0) {
                testrail.setHost(testrailHost);
                testrail.setUser(value);
                testrail.setPassword(testrailPassword);
                if (testrail.serverReachable() && !testrail.authenticationWorks()){
                    return FormValidation.error("Invalid user/password combination.");
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTestrailPassword(@QueryParameter String value,
                                                      @QueryParameter String testrailHost,
                                                      @QueryParameter String testrailUser)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.warning("Please add your password.");
            }
            if (testrailUser.length() > 0) {
                testrail.setHost(testrailHost);
                testrail.setUser(testrailUser);
                testrail.setPassword(value);
                if (testrail.serverReachable() && !testrail.authenticationWorks()){
                    return FormValidation.error("Invalid user/password combination.");
                }
            }
            return FormValidation.ok();
        }



        public ListBoxModel doFillTestrailMilestoneItems(@QueryParameter int testrailProject) {
            ListBoxModel items = new ListBoxModel();
            items.add("None", "");
            try {
                for (Milestone mstone : testrail.getMilestones(testrailProject)) {
                    items.add(mstone.getName(), mstone.getId());
                }
            } catch (ElementNotFoundException e) {
            } catch (IOException e) {
            }
            return items;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Publish test results to TestRail";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            testrailHost = formData.getString("testrailHost");
            testrailUser = formData.getString("testrailUser");
            testrailPassword = formData.getString("testrailPassword");


            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setTestrailHost)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public void setTestrailHost(String host) { this.testrailHost = host; }
        public String getTestrailHost() { return testrailHost; }
        public void setTestrailUser(String user) { this.testrailUser = user; }
        public String getTestrailUser() { return testrailUser; }
        public void setTestrailPassword(String password) { this.testrailPassword = password; }
        public String getTestrailPassword() { return testrailPassword; }
        public void setTestrailInstance(TestRailClient trc) { testrail = trc; }
        public TestRailClient getTestrailInstance() { return testrail; }

    }
}
