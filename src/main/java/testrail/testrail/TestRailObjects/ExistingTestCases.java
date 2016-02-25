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
package testrail.testrail.TestRailObjects;

import testrail.testrail.TestRailClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Drew on 3/24/2014.
 */
public class ExistingTestCases {
    private TestRailClient testRailClient;
    private int projectId;
    private Map<Suite, List<Case>> cases;
    private Map<Integer, Suite> caseIdToSuite;

    public ExistingTestCases(TestRailClient testRailClient, int projectId)
            throws IOException, ElementNotFoundException {
        this.projectId = projectId;
        this.testRailClient = testRailClient;
        this.cases = testRailClient.getCases(this.projectId);
        caseIdToSuite = new HashMap<Integer, Suite>(this.cases.size() * 2);
        for(Suite suite: this.cases.keySet()) {
            for(Case testRailCase: this.cases.get(suite)) {
                caseIdToSuite.put(testRailCase.getId(), suite);
            }
        }
    }

    public int getProjectId() {
        return this.projectId;
    }

    public Map<Suite, List<Case>> getCases() {
        return this.cases;
    }

    public String[] listTestCases() throws ElementNotFoundException {
        ArrayList<String> result = new ArrayList<String>();
        for(List<Case> caseList: cases.values()) {
            Iterator<Case> caseIterator = caseList.iterator();
            while (caseIterator.hasNext()) {
                Case testcase = caseIterator.next();
                String sectionName = testRailClient.getSection(testcase.getSectionId()).getName();
                result.add(sectionName + ": " + testcase.getTitle());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public Suite getCasesSuite(Integer caseId) {
        return caseIdToSuite.get(caseId);
    }

    public List<Case> getCasesInSuite(String suiteName) {
        for(Suite suite: cases.keySet()) {
            if(suite.getName().equalsIgnoreCase(suiteName))
                return cases.get(suite);
        }
        return Collections.emptyList();
    }
}

