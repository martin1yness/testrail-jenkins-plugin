testrail-jenkins-plugin
=======================

Forked from  https://github.com/simplymeasured/testrail-jenkins-plugin
Integrate test results from Jenkins into TestRail.
Upload your junit test results to TestRail after every run.
Each Jenkins build becomes test run.
Each testsuite becomes test group.


This fork changelog
---------------
- fixed validation issues
- added milestone support
- fixed junit files parsing
- added nested <testsuite> tags support in junit
- added dropdown lists to select projects, suites and milestones
- defined a notation for mapping JUnit/TestNG XML test results to TestRail Test Suite/Case names

Build
-----
This is a Maven project. You'll need the following in your ~/.m2/settings.xml.

    <settings>
      <pluginGroups>
        <pluginGroup>org.jenkins-ci.tools</pluginGroup>
      </pluginGroups>
      <profiles>
        <profile>
          <id>jenkins</id>
          <activation>
            <activeByDefault>true</activeByDefault>
          </activation>
          <repositories>
            <repository>
              <id>repo.jenkins-ci.org</id>
              <url>http://repo.jenkins-ci.org/public/</url>
            </repository>
          </repositories>
          <pluginRepositories>
            <pluginRepository>
              <id>repo.jenkins-ci.org</id>
              <url>http://repo.jenkins-ci.org/public/</url>
            </pluginRepository>
          </pluginRepositories>
        </profile>
      </profiles>
      <mirrors>
        <mirror>
          <id>repo.jenkins-ci.org</id>
          <url>http://repo.jenkins-ci.org/public/</url>
          <mirrorOf>m.g.o-public</mirrorOf>
        </mirror>
      </mirrors>
    </settings>
    
To run on your development box you can just do

    mvn hpi:run
    
That will build and start a Jenkins instance running at http://localhost:8080/jenkins. It will have the plugin installed but not configured.


And to build a package to install on your production Jenkins box

    mvn clean package
        
That creates a .hpi file in the target directory. For more information about installing plugins, please see https://wiki.jenkins-ci.org/display/JENKINS/Plugins.


TestNG Support
---------------------------
To provide robust control over how suites and cases are implemented, support for TestNG's XML configuration has been
added. This enables test engineers to define suites and cases as they are in TestRail and map classes/methods to
these arbitrarily.

Example `testng-results.xml` XML output (see: testrail.testrail.testng/testng-results.xml):
    <suite name="Widget" duration-ms="91" started-at="2016-02-18T15:41:01Z" finished-at="2016-02-18T15:41:01Z">
    <groups>
    </groups>
    <test name="Widget Works" duration-ms="41" started-at="2016-02-18T15:41:01Z" finished-at="2016-02-18T15:41:01Z">
        <class name="testrail.testrail.TestWidgetWorks">
            <test-method status="PASS" signature="testVerifySomething_givenSomething_thenSomething()[pri:0, instance:testrail.testrail.TestWidgetWorks@6a2f6f80]" name="testVerifySomething_givenSomething_thenSomething" duration-ms="18" started-at="2016-02-18T09:41:01Z" finished-at="2016-02-18T09:41:01Z">
                <reporter-output>
                </reporter-output>
            </test-method> <!-- testVerifySomething_givenSomething_thenSomething -->
        </class> <!-- testrail.testrail.TestWidgetWorks -->
    </test> <!-- Widget Works -->

This example would map to a Suite named `Widget` and a test case named `Widget Works`. This output comes from the
example TestNG configuration file:
    <!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd" >

    <!-- the suite `name` attribute must match the test suite name in TestRail -->
    <suite name="Widget" verbose="1" >

        <!-- The `name` attribute must match the name in TestRail -->
        <test name="Widget Works" >
            <classes>
                <class name="testrail.testrail.TestWidgetWorks">
                    <methods>
                        <include name="testVerifySomething_givenSomething_thenSomething"/>
                    </methods>
                </class>
            </classes>
        </test>

        <!-- The `name` attribute must match the name in TestRail -->
        <test name="Widget Does X">
            <classes>
                <class name="testrail.testrail.TestWidgetWorks">
                    <methods>
                        <include name="testVerifySomething_givenSomethingElse_thenSomethingElse"/>
                    </methods>
                </class>
            </classes>
        </test>
    </suite>

License
-------
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
