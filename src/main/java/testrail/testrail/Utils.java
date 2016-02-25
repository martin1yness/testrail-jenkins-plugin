package testrail.testrail;

import hudson.FilePath;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import hudson.util.DirScanner;
import hudson.util.FileVisitor;
import testrail.testrail.TestRailObjects.ExistingTestCases;
import testrail.testrail.TestRailObjects.Results;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by achikin on 7/31/14.
 */
public class Utils {
    private static final Logger LOGGER = Logger.getLogger("TestrailPluginDebug");
    public static void log(Object... objects) {
        LOGGER.log(Level.WARNING, Arrays.toString(objects));
    }

    public static Results processResultsFiles(final String pathGlob, final ResultFileProcessor processor,
                                           ExistingTestCases exist, FilePath workspace, PrintStream logger)
                                            throws IOException, InterruptedException {
        logger.println("Munging test result files for: " + processor.getClass().getName());
        final Results results = new Results();
        // FilePath doesn't have a read method. We want to actually process the files on the master
        // because during processing we talk to TestRail and slaves might not be able to.
        // So we'll copy the result files to the master and munge them there:
        //
        // Create a temp directory.
        // Do a base.copyRecursiveTo() with file masks into the temp dir.
        // process the temp files.
        // it looks like the destructor deletes the temp dir when we're finished
        FilePath tempdir = new FilePath(Util.createTempDir());
        // This picks up *all* result files so if you have old results in the same directory we'll see those, too.
        workspace.copyRecursiveTo(pathGlob, "", tempdir);

        tempdir.act(new FilePath.FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                final DirScanner scanner = new DirScanner.Glob(pathGlob, null);
                scanner.scan(f, new FileVisitor() {
                    @Override
                    public void visit(File file, String s) throws IOException {
                        processor.handleResult(file, results);
                    }
                });
                return null;
            }
        });

        return results;
    }
}
