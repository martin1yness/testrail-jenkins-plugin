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

/**
 * Created by Drew on 3/25/2014.
 */
public class Result {
	public enum STATUS {_UNDEFINED_, PASSED, BLOCKED, UNTESTED, RETESTED, FAILED}

    private transient Case caseObj;
    private int caseId;
    private int statusId;
    private String comment;

    public Result(Case caseObj, STATUS status, String comment) {
        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.statusId = status.ordinal();
        this.comment = comment;
    }

    public Result(int caseId, int statusId, String comment) {
        this.caseId = caseId;
        this.statusId = statusId;
        this.comment = comment;
    }

    public void setCaseId(int caseId) { this.caseId = caseId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
    public void setComment(String comment) { this.comment = comment; }

    public Case getCaseObj() {
        return caseObj;
    }
    public STATUS getStatus() {
        return STATUS.values()[statusId];
    }
    public int getCaseId() { return this.caseId; }
    public int getStatusId() { return this.statusId; }
    public String getComment() { return this.comment; }
}
