/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.scim2.testsuite.core.tests;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.exception.ComplianceException;
import org.wso2.scim2.testsuite.core.exception.GeneralComplianceException;
import org.wso2.scim2.testsuite.core.httpclient.HTTPClient;
import org.wso2.scim2.testsuite.core.protocol.ComplianceTestMetaDataHolder;
import org.wso2.scim2.testsuite.core.protocol.ComplianceUtils;
import org.wso2.scim2.testsuite.core.tests.common.ResponseValidateTests;
import org.wso2.scim2.testsuite.core.tests.model.RequestPath;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

/**
 * Implementation of Group test cases.
 */
public class GroupTestImpl implements ResourceType {

    private final ComplianceTestMetaDataHolder complianceTestMetaDataHolder;

    private final String url;

    /**
     * Initialize.
     *
     * @param complianceTestMetaDataHolder Stores data required to run tests.
     */
    public GroupTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;

        url = complianceTestMetaDataHolder.getUrl() + ComplianceConstants.TestConstants.GROUPS_ENDPOINT;
    }

    /**
     * Create test users for test cases.
     *
     * @param noOfUsers Specify the number of users needs to create.
     * @return userIDS of created users.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     * @throws GeneralComplianceException General exceptions.
     */
    private ArrayList<String> createTestsUsers(String noOfUsers)
            throws ComplianceException, GeneralComplianceException {

        String url = complianceTestMetaDataHolder.getUrl() + ComplianceConstants.TestConstants.USERS_ENDPOINT;

        ArrayList<String> definedUsers = new ArrayList<>();
        ArrayList<String> userIDs = new ArrayList<>();

        if (noOfUsers.equals("One")) {
            definedUsers.add(ComplianceConstants.DefinedInstances.defineUser);
        } else if (noOfUsers.equals("Many")) {
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser1);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser2);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser3);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser4);
            definedUsers.add(ComplianceConstants.DefinedInstances.definedUser5);
        }

        HttpPost method = new HttpPost(url);
        // Create users.
        HttpClient client = HTTPClient.getHttpClient();
        HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        for (String definedUser : definedUsers) {
            long startTime = System.currentTimeMillis();
            try {
                // Create Users.
                HttpEntity entity = new ByteArrayEntity(definedUser.getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    // Obtain the schema corresponding to the user.
                    SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                    JSONDecoder jsonDecoder = new JSONDecoder();
                    User user;
                    try {
                        user = jsonDecoder.decodeResource(responseString, schema, new User());
                    } catch (BadRequestException | CharonException | InternalErrorException e) {
                        long stopTime = System.currentTimeMillis();
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Users",
                                "Could not decode the server response of users create.", ComplianceUtils.getWire(method,
                                        responseString, headerString.toString(), responseStatus, subTests),
                                stopTime - startTime));
                    }
                    userIDs.add(user.getId());
                }
            } catch (Exception e) {
                // Read the response body.
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                long stopTime = System.currentTimeMillis();
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Users",
                        "Could not create default users at url " + url, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));
            }
        }
        return userIDs;
    }

    /**
     * Delete a user after test execution.
     *
     * @param id User id to delete a user.
     * @param testName Respective test case.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    private void cleanUpUser(String id, String testName) throws GeneralComplianceException, ComplianceException {

        long startTime = System.currentTimeMillis();
        String url = complianceTestMetaDataHolder.getUrl() + ComplianceConstants.TestConstants.USERS_ENDPOINT;
        String deleteUserURL = url + "/" + id;
        HttpDelete method = new HttpDelete(deleteUserURL);
        HttpClient client = HTTPClient.getHttpClient();
        HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        try {
            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
        } catch (Exception e) {
            /*
             * Read the response body. Get all headers.
             */
            assert response != null;
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            long stopTime = System.currentTimeMillis();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus, subTests),
                    stopTime - startTime));
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
            long stopTime = System.currentTimeMillis();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default user at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus, subTests),
                    stopTime - startTime));
        }
    }

    /**
     * Create test groups for test cases.
     *
     * @param userIDs Array of user ids to use as members in groups.
     * @param noOfGroups Specify the number of groups needs to create.
     * @return groupIDs of created groups.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     * @throws GeneralComplianceException General exceptions.
     */
    private ArrayList<String> createTestsGroups(ArrayList<String> userIDs, String noOfGroups)
            throws ComplianceException, GeneralComplianceException {

        ArrayList<String> groupIDs = new ArrayList<>();
        ArrayList<String> definedGroups = new ArrayList<>();

        if (noOfGroups.equals("One")) {
            definedGroups.add("{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:Group\"],"
                    + "\"displayName\":\"XwLtOP23\",\"members\":[{\"value\":\"" + userIDs.get(0) + "\",\"display"
                    + "\":\"loginUser1\",\"$ref\":\"" + complianceTestMetaDataHolder.getUrl()
                    + ComplianceConstants.TestConstants.USERS_ENDPOINT + "/" + userIDs.get(0) + "\"}," + "{\"value\":\""
                    + userIDs.get(1) + "\",\"display\":\"loginUser2\"},{\"value\":\"" + userIDs.get(2)
                    + "\",\"display\":\"loginUser3\"},{\"value\":\"" + userIDs.get(3) + "\",\"display"
                    + "\":\"loginUser4" + "\"}," + "{\"value\":\"" + userIDs.get(4)
                    + "\",\"display\":\"loginUser5\"}]}");
        } else if (noOfGroups.equals("Many")) {
            definedGroups.add("{\"displayName\": \"EYtXcD21\"}");
            definedGroups.add("{\"displayName\": \"BktqER22\"}");
            definedGroups.add("{\"displayName\": \"ZwLtOP23\"}");
            definedGroups.add("{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:Group\"],"
                    + "\"displayName\":\"XwLtOP23\",\"members\":[{\"value\":\"" + userIDs.get(0) + "\",\"display"
                    + "\":\"loginUser1\"}," + "{\"value\":\"" + userIDs.get(1) + "\",\"display\":\"loginUser2\"},"
                    + "{\"value\":\"" + userIDs.get(2) + "\",\"display\":\"loginUser3\"},{\"value\":\"" + userIDs.get(3)
                    + "\",\"display" + "\":\"loginUser4" + "\"}," + "{\"value\":\"" + userIDs.get(4)
                    + "\",\"display\":\"loginUser5\"}]}");
        }

        HttpPost method = new HttpPost(url);
        // create groups
        HttpClient client = HTTPClient.getHttpClient();
        HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder();
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        for (String definedGroup : definedGroups) {
            long startTime = System.currentTimeMillis();
            try {
                // Create the group.
                HttpEntity entity = new ByteArrayEntity(definedGroup.getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                responseStatus = String.valueOf(response.getStatusLine().getStatusCode());
                if (responseStatus.equals("201")) {
                    // Obtain the schema corresponding to group.
                    SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                    JSONDecoder jsonDecoder = new JSONDecoder();
                    Group group;
                    try {
                        group = jsonDecoder.decodeResource(responseString, schema, new Group());
                    } catch (BadRequestException | CharonException | InternalErrorException e) {
                        long stopTime = System.currentTimeMillis();
                        throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Groups",
                                "Could not decode the server response of groups create.",
                                ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                        subTests),
                                stopTime - startTime));
                    }
                    groupIDs.add(group.getId());
                }
            } catch (Exception e) {
                // Read the response body.
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                long stopTime = System.currentTimeMillis();
                throw new GeneralComplianceException(new TestResult(TestResult.ERROR, "List Groups",
                        "Could not create default groups at url " + url, ComplianceUtils.getWire(method, responseString,
                                headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));
            }
        }
        return groupIDs;
    }

    /**
     * This method cleans the group with the given groupId and the user with the given id.
     *
     * @param groupId Contains group id.
     * @param testName Contains name of the test.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    private void cleanUpGroup(String groupId, String testName) throws GeneralComplianceException, ComplianceException {

        long startTime = System.currentTimeMillis();
        String deleteGroupURL;
        deleteGroupURL = url + "/" + groupId;
        HttpDelete method = new HttpDelete(deleteGroupURL);
        HttpClient client = HTTPClient.getHttpClient();
        HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
        method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
        HttpResponse response = null;
        String responseString = StringUtils.EMPTY;
        StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
        String responseStatus;
        ArrayList<String> subTests = new ArrayList<>();
        try {
            response = client.execute(method);
            // Read the response body.
            responseString = new BasicResponseHandler().handleResponse(response);
            // Get all headers.
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
        } catch (Exception e) {
            /*
             * Read the response body. Get all headers.
             */
            assert response != null;
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
            }
            responseStatus = response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase();
            long stopTime = System.currentTimeMillis();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus, subTests),
                    stopTime - startTime));
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
            long stopTime = System.currentTimeMillis();
            throw new GeneralComplianceException(new TestResult(TestResult.ERROR, testName,
                    "Could not delete the default group at url " + url,
                    ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus, subTests),
                    stopTime - startTime));
        }
    }

    /**
     * This checks whether the given array list of groups are in sorted order with respect to group id.
     *
     * @param returnedGroups Array of groups to get checked whether they are sorted or not.
     * @return true or false.
     * @throws CharonException Exception by charon library.
     */
    private boolean isGroupListSorted(ArrayList<Group> returnedGroups) throws CharonException {

        boolean sorted = true;
        for (int i = 1; i < returnedGroups.size(); i++) {
            if (returnedGroups.get(i - 1).getId().compareTo(returnedGroups.get(i).getId()) > 0) {
                sorted = false;
            }
        }
        return sorted;
    }

    /**
     * Generating unique numbers.
     *
     * @return unique number.
     */
    private static String generateUniqueID() {

        return UUID.randomUUID().toString();
    }

    /**
     * Add assertion details.
     *
     * @param assertionName Assertion name.
     * @param actual Actual result.
     * @param expected Expected result.
     * @param status Status of the assertion.
     * @param subTests Array containing assertions details.
     */
    private void addAssertion(String assertionName, String actual, String expected, String status,
            ArrayList<String> subTests) {

        subTests.add(assertionName);
        subTests.add(ComplianceConstants.TestConstants.ACTUAL + actual);
        subTests.add(ComplianceConstants.TestConstants.EXPECTED + expected);
        subTests.add(status);
        subTests.add(StringUtils.EMPTY);
    }

    /**
     * Add assertion details.
     *
     * @param assertionName Assertion name.
     * @param message Descriptive message about the assertion.
     * @param status Success or failure.
     * @param subTests Array containing assertions details.
     */
    private void addAssertion(String assertionName, String message, String status, ArrayList<String> subTests) {

        subTests.add(assertionName);
        subTests.add(ComplianceConstants.TestConstants.MESSAGE + message);
        subTests.add(status);
        subTests.add(StringUtils.EMPTY);
    }

    /**
     * Get group tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults = new ArrayList<>();
        ArrayList<String> userIDs;
        ArrayList<String> groupIDs;
        // Create 5 test users to assign for groups.
        userIDs = createTestsUsers("Many");
        // Create test groups with users.
        groupIDs = createTestsGroups(userIDs, "Many");

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("List groups");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("?attributes=displayName,members.value");
        requestPath2.setTestCaseName("Get groups with displayName and member.value attributes");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl("?excludedAttributes=members");
        requestPath3.setTestCaseName("Get groups with excluding members attribute");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setUrl("?sortBy=id&sortOrder=ascending");
        requestPath4.setTestCaseName("Get groups with group id sorting and ascending order");
        try {
            requestPath4.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getSortSupported());
        } catch (Exception e) {
            requestPath4.setTestSupported(true);
        }

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setUrl("?startIndex=1&count=2");
        requestPath5.setTestCaseName("Get groups with index pagination and count");

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setUrl("?startIndex=-1&count=2");
        requestPath6.setTestCaseName("Get groups having negative number as index");

        RequestPath requestPath7 = new RequestPath();
        requestPath7.setUrl("?count=2");
        requestPath7.setTestCaseName("Get groups without index and only using count");

        RequestPath requestPath8 = new RequestPath();
        requestPath8.setUrl("?startIndex=1");
        requestPath8.setTestCaseName("List groups with only using startIndex");

        RequestPath requestPath9 = new RequestPath();
        requestPath9.setUrl("?filter=displayName+eq+%22EYtXcD21%22&startIndex=1&count=1");
        requestPath9.setTestCaseName("Get groups with displayName as filter and with pagination");
        try {
            requestPath9.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath9.setTestSupported(true);
        }

        RequestPath requestPath10 = new RequestPath();
        requestPath10.setUrl("?filter=displayName+eq+%22EYtXcD21%22");
        requestPath10.setTestCaseName("Get groups with displayName as filter");
        try {
            requestPath10.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath10.setTestSupported(true);
        }

        RequestPath requestPath11 = new RequestPath();
        requestPath11.setUrl("?filter=displayName+eq+%22EYtXcD21%22&startIndex=1");
        requestPath11.setTestCaseName("List groups by filtering - displayName eq with only using startIndex");
        try {
            requestPath11.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath11.setTestSupported(true);
        }

        RequestPath requestPath12 = new RequestPath();
        requestPath12.setUrl("?filter=DISPLAYNAME+eq+%22EYtXcD21%22");
        requestPath12.setTestCaseName(
                "List groups by filtering - displayName eq to check case insensitivity of " + "attribute");
        try {
            requestPath12.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath12.setTestSupported(true);
        }

        RequestPath requestPath13 = new RequestPath();
        requestPath13.setUrl("?filter=displayName+EQ+%22EYtXcD21%22");
        requestPath13.setTestCaseName(
                "List groups by filtering - displayName eq to check case insensitivity of " + "operator");
        try {
            requestPath13.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath13.setTestSupported(true);
        }

        RequestPath requestPath14 = new RequestPath();
        requestPath14.setUrl("?filter=displayName+ne+%22EYtXcD21%22");
        requestPath14.setTestCaseName("List groups by filtering - displayName ne");
        try {
            requestPath14.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath14.setTestSupported(true);
        }

        RequestPath requestPath15 = new RequestPath();
        requestPath15.setUrl("?filter=displayName+co+%22EYtXcD21%22");
        requestPath15.setTestCaseName("List groups by filtering - displayName co");
        try {
            requestPath15.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath15.setTestSupported(true);
        }

        RequestPath requestPath16 = new RequestPath();
        requestPath16.setUrl("?filter=displayName+sw+%22EYtXcD21%22");
        requestPath16.setTestCaseName("List groups by filtering - displayName sw");
        try {
            requestPath16.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath16.setTestSupported(true);
        }

        RequestPath requestPath17 = new RequestPath();
        requestPath17.setUrl("?filter=displayName+ew+%22EYtXcD21%22");
        requestPath17.setTestCaseName("List groups by filtering - displayName ew");
        try {
            requestPath17.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath17.setTestSupported(true);
        }

        RequestPath requestPath18 = new RequestPath();
        requestPath18.setUrl("?filter=displayName+pr");
        requestPath18.setTestCaseName("List groups by filtering - displayName pr");
        try {
            requestPath18.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath18.setTestSupported(true);
        }

        // This array hold the sub tests details.
        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3, requestPath4, requestPath5,
                requestPath6, requestPath7, requestPath8, requestPath9, requestPath10, requestPath11, requestPath12,
                requestPath13, requestPath14, requestPath15, requestPath16, requestPath17, requestPath18 };
        for (RequestPath requestPath : requestPaths) {
            long startTime = System.currentTimeMillis();
            String requestUrl = url + requestPath.getUrl();
            HttpGet method = new HttpGet(requestUrl);
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            int startIndex;
            int count;
            ArrayList<String> subTests = new ArrayList<>();
            boolean errorOccur = false;
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                /*
                 * Read the response body. Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPath.getTestSupported()
                        && response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_IMPLEMENTED) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not list the groups at url " + url, ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                // Obtain the schema corresponding to group.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                JSONObject jsonObjResponse;
                ArrayList<Group> groupList = new ArrayList<>();
                try {
                    JSONObject jsonObj = new JSONObject(responseString);
                    jsonObjResponse = jsonObj;
                    JSONArray groupsArray = jsonObj.getJSONArray("Resources");
                    startIndex = (int) jsonObjResponse.get("startIndex");
                    count = (int) jsonObjResponse.get("totalResults");
                    JSONObject tmp;
                    for (int j = 0; j < groupsArray.length(); j++) {
                        tmp = groupsArray.getJSONObject(j);
                        groupList.add(jsonDecoder.decodeResource(tmp.toString(), schema, new Group()));
                        try {
                            ResponseValidateTests.runValidateTests(groupList.get(j), schema, null, null, method,
                                    responseString, headerString.toString(), responseStatus, subTests);
                        } catch (BadRequestException | CharonException e) {
                            subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                            subTests.add(StringUtils.EMPTY);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(
                                    new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                                    headerString.toString(), responseStatus, subTests),
                                            stopTime - startTime));
                            errorOccur = true;
                            break;
                        }
                    }
                } catch (JSONException | BadRequestException | CharonException | InternalErrorException e) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                switch (requestPath.getTestCaseName()) {
                    case "List groups":
                    case "List groups with only using startIndex":
                        // check for all created groups
                        ArrayList<String> returnedGroupIDs = new ArrayList<>();
                        for (Group group : groupList) {
                            returnedGroupIDs.add(group.getId());
                        }
                        for (String id : groupIDs) {
                            if (!returnedGroupIDs.contains(id)) {
                                addAssertion(ComplianceConstants.TestConstants.ALL_GROUPS_IN_TEST,
                                        "Check the created 5 groups are listed.",
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain all the created groups",
                                        ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                                responseStatus, subTests),
                                        stopTime - startTime));
                                errorOccur = true;
                                break;
                            }
                        }
                        if (!errorOccur) {
                            addAssertion(ComplianceConstants.TestConstants.ALL_GROUPS_IN_TEST,
                                    "Check the created 5 groups are listed.",
                                    ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        }
                        break;
                    case "Get groups with displayName as filter":
                    case "List groups by filtering - displayName eq with only" + " using startIndex":
                    case "List groups by f" + "iltering - displayName eq to check case insensitivity of attribute":
                    case "List groups by filtering - displayName eq to check " + "case insensitivity of operator":
                    case "List groups by filtering - displayName co":
                    case "List groups by filtering - displayName sw":
                    case "List groups by filtering - displayName ew": {
                        String value = "EYtXcD21";
                        for (Group group : groupList) {
                            if (!Objects.equals(value, group.getDisplayName())) {
                                addAssertion(requestPath.getTestCaseName() + " test",
                                        "displayName:" + group.getDisplayName(), "displayName:" + value,
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain the expected groups", ComplianceUtils.getWire(method,
                                                responseString, headerString.toString(), responseStatus, subTests),
                                        stopTime - startTime));
                                errorOccur = true;
                                break;
                            }
                        }
                        if (!errorOccur) {
                            addAssertion(requestPath.getTestCaseName() + " test", "displayName:" + value,
                                    "displayName:" + value, ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        }
                        break;
                    }
                    case "Get groups with index pagination and count":
                    case "Get users having negative number as index":
                        if (groupList.size() != 2) {
                            addAssertion(requestPath.getTestCaseName() + "Test",
                                    "startIndex:" + startIndex + ",totalResults:" + groupList.size(),
                                    "startIndex:1,totalResults:2", ComplianceConstants.TestConstants.STATUS_FAILED,
                                    subTests);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Response does not contain right number of paginated groups",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(requestPath.getTestCaseName() + "Test",
                                "startIndex:" + startIndex + ",totalResults:" + groupList.size(),
                                "startIndex:1,totalResults:2", ComplianceConstants.TestConstants.STATUS_SUCCESS,
                                subTests);
                        break;
                    case "Get groups with group id sorting and " + "ascending order":
                        try {
                            if (!isGroupListSorted(groupList)) {
                                addAssertion(ComplianceConstants.TestConstants.SORT_GROUPS_TEST,
                                        "Check created groups are sorted or not.",
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain the sorted list of groups",
                                        ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                                responseStatus, subTests),
                                        stopTime - startTime));
                                continue;
                            }
                        } catch (CharonException e) {
                            addAssertion(ComplianceConstants.TestConstants.SORT_GROUPS_TEST,
                                    "Check created groups are sorted or not.",
                                    ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Response does not contain the sorted list of groups",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(ComplianceConstants.TestConstants.SORT_GROUPS_TEST,
                                "Check created groups are sorted or not.",
                                ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        break;
                    case "Get groups with displayName as filter and " + "with pagination": {
                        if (groupList.size() != 1) {
                            addAssertion(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION,
                                    "startIndex:" + startIndex + ",totalResults:" + groupList.size(),
                                    "startIndex:1,totalResults:1", ComplianceConstants.TestConstants.STATUS_FAILED,
                                    subTests);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Response does not contain right number of users.", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        String value = "EYtXcD21";
                        for (Group group : groupList) {
                            if (!Objects.equals(value, group.getDisplayName())) {
                                addAssertion(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION,
                                        "startIndex:" + startIndex + ",totalResults:" + groupList.size()
                                                + ",displayName:" + group.getDisplayName(),
                                        "startIndex:1,totalResults:1,displayName:loginUser1",
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain the expected groups", ComplianceUtils.getWire(method,
                                                responseString, headerString.toString(), responseStatus, subTests),
                                        stopTime - startTime));
                                errorOccur = true;
                                break;
                            }
                        }
                        if (!errorOccur) {
                            addAssertion(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION,
                                    "startIndex:1,totalResults:1,displayName:loginUser1",
                                    "startIndex:1,totalResults:1,displayName:loginUser1",
                                    ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        }
                        break;
                    }
                    case "Get groups without index and only using count":
                        if (startIndex != 1 && count != 2) {
                            addAssertion(ComplianceConstants.TestConstants.PAGINATION_USER_TEST,
                                    "startIndex:" + startIndex + "," + "totalResults:" + count,
                                    "startIndex:1,totalResults:2", ComplianceConstants.TestConstants.STATUS_FAILED,
                                    subTests);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Response does not contain right number of pagination.",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(ComplianceConstants.TestConstants.PAGINATION_USER_TEST,
                                "startIndex:1,totalResults:2", "startIndex:1,totalResults:2",
                                ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        break;
                }
                long stopTime = System.currentTimeMillis();
                if (!errorOccur) {
                    testResults.add(new TestResult(TestResult.SUCCESS, requestPath.getTestCaseName(), StringUtils.EMPTY,
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests),
                            stopTime - startTime));
                }
            } else if (!requestPath.getTestSupported()
                    || response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_IMPLEMENTED) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_SKIPPED, subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SKIPPED, requestPath.getTestCaseName(),
                        "This functionality is not implemented. Hence given status code 501",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            }
        }
        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "get groups test");
        }
        // Clean up groups.
        for (String id : groupIDs) {
            cleanUpGroup(id, "Get groups");
        }
        return testResults;
    }

    /**
     * Get group by id tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> userIDs;

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("Get group by ID");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("?attributes=displayName,members.value");
        requestPath2.setTestCaseName("Get a group with specific attributes");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl("?excludedAttributes=members");
        requestPath3.setTestCaseName("Get a group with excluding members attribute");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setUrl(generateUniqueID());
        requestPath4.setTestCaseName("Get group with non existing ID and validate group not found error response");

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3, requestPath4 };

        for (RequestPath requestPath : requestPaths) {
            long startTime = System.currentTimeMillis();
            userIDs = createTestsUsers("Many");
            ArrayList<String> groupId = createTestsGroups(userIDs, "One");
            String id = groupId.get(0);
            Group group;
            String getGroupURL;
            getGroupURL = url + "/" + id + requestPath.getUrl();
            HttpGet method = new HttpGet(getGroupURL);
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                // Get all headers.
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                // Clean up users.
                for (String uId : userIDs) {
                    cleanUpUser(uId, requestPath.getTestCaseName());
                }
                cleanUpGroup(id, requestPath.getTestCaseName());
                if (!requestPath.getTestCaseName()
                                .equals("Get group with non existing ID and validate group "
                                        + "not found error response")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                            "Could not get the default group from url " + url, ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                // Obtain the schema corresponding to group.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    group = jsonDecoder.decodeResource(responseString, schema, new Group());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    // Clean up users.
                    for (String uId : userIDs) {
                        cleanUpUser(uId, requestPath.getTestCaseName());
                    }
                    cleanUpGroup(id, requestPath.getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(group, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    // Clean up users.
                    for (String uId : userIDs) {
                        cleanUpUser(uId, requestPath.getTestCaseName());
                    }
                    cleanUpGroup(id, requestPath.getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                // Clean up users.
                for (String uId : userIDs) {
                    cleanUpUser(uId, requestPath.getTestCaseName());
                }
                cleanUpGroup(id, requestPath.getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPath.getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPath.getTestCaseName()
                                  .equals("Get group with non existing ID and validate "
                                          + "group not found error response")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_NOT_FOUND), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPath.getTestCaseName(),
                        "Server successfully given the expected error 404(Group not found in the user store) "
                                + "message",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else {
                // Clean up users.
                for (String uId : userIDs) {
                    cleanUpUser(uId, requestPath.getTestCaseName());
                }
                cleanUpGroup(id, requestPath.getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            }
        }
        return testResults;
    }

    /**
     * Post group tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults = new ArrayList<>();
        ArrayList<String> userIDs;
        ArrayList<String> definedGroups = new ArrayList<>();
        String groupId = null;
        userIDs = createTestsUsers("Many");
        definedGroups.add("{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:Group\"],"
                + "\"displayName\":\"XwLtOP23\",\"members\":[{\"value\":\"" + userIDs.get(0) + "\",\"display"
                + "\":\"loginUser1\",\"$ref\":\"" + complianceTestMetaDataHolder.getUrl()
                + ComplianceConstants.TestConstants.USERS_ENDPOINT + "/" + userIDs.get(0) + "\"}," + "{\"value\":\""
                + userIDs.get(1) + "\",\"display\":\"loginUser2\"},{\"value\":\"" + userIDs.get(2)
                + "\",\"display\":\"loginUser3\"},{\"value\":\"" + userIDs.get(3) + "\",\"display" + "\":\"loginUser4"
                + "\"}," + "{\"value\":\"" + userIDs.get(4) + "\",\"display\":\"loginUser5\"}]}");
        definedGroups.add("{\"displayName\": \"XwLtOP23\"}");
        definedGroups.add("");

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Create group");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Create group with existing displayName");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Create group without displayName");

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            Group group;
            String definedGroup;
            definedGroup = definedGroups.get(i);
            HttpPost method = new HttpPost(url);
            // Create group test.
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                // Create the group.
                HttpEntity entity = new ByteArrayEntity(definedGroup.getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                /*
                 * Read the response body. Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPaths[i].getTestCaseName().equals("Create group")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()),
                            String.valueOf(HttpStatus.SC_CREATED), ComplianceConstants.TestConstants.STATUS_FAILED,
                            subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, "Create Group",
                            "Could not create default user at url " + url, ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_CREATED),
                        ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                // Obtain the schema corresponding to group.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    group = jsonDecoder.decodeResource(responseString, schema, new Group());
                    groupId = group.getId();
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, "Create Group",
                            "Could not decode the server response", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(group, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, "Create Group", "Response Validation Error",
                            ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                    subTests),
                            stopTime - startTime));
                    continue;
                }
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(
                        TestResult.SUCCESS, "Create Group", StringUtils.EMPTY, ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Create group with existing displayName")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_CONFLICT), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Server successfully given the expected error 409(conflict) message",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Create group without displayName")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_BAD_REQUEST), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Server successfully given the expected error 400(Required attribute displayName is "
                                + "missing in the SCIM Object) message",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(
                        TestResult.ERROR, "Create Group", StringUtils.EMPTY, ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));
            }
        }
        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "get users test");
        }
        // Clean up group.
        cleanUpGroup(groupId, "Group Create");

        return testResults;
    }

    /**
     * Patch group tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> patchMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();
        ArrayList<String> userIDs = createTestsUsers("Many");
        ArrayList<String> definedPatchedGroup = new ArrayList<>();

        definedPatchedGroup.add("{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],"
                + "\"Operations\":[{\"op\":\"add\",\"value\":{\"displayName\": \"XwLtOP23-patch\"}}]}");
        definedPatchedGroup.add("{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],"
                + "\"Operations\":[{\"op\":\"remove\",\"path\":\"members\"}]}");
        definedPatchedGroup.add("{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],"
                + "\"Operations\":[{\"op\":\"replace\",\"value\":{\"members\":[{\"display\":\"loginUser4\",\"value"
                + "\":\"" + userIDs.get(4) + "\",\"$ref\":\"" + complianceTestMetaDataHolder.getUrl()
                + ComplianceConstants.TestConstants.USERS_ENDPOINT + "/" + userIDs.get(0) + "\"}]}}]}");
        definedPatchedGroup.add("{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],"
                + "\"Operations\":[{\"op\":\"remove\",\"path\":\"members\"},{\"op\":\"add\",\"path\":\"members\","
                + "\"value\":[{\"display\":\"loginUser1\",\"value\":\"" + userIDs.get(0) + "\"}]},"
                + "{\"op\":\"replace\",\"path\":\"members\",\"value\":[{\"display\":\"loginUser1\",\"value\":\""
                + userIDs.get(0) + "\",\"$ref\":\"" + complianceTestMetaDataHolder.getUrl()
                + ComplianceConstants.TestConstants.USERS_ENDPOINT + "/" + userIDs.get(0) + "\"}]}]}");
        definedPatchedGroup.add("{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],"
                + "\"Operations\":[{\"op\":\"remove\",\"path\":\"members\"}]}");
        definedPatchedGroup.add("{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],"
                + "\"Operations\":[{\"op\":\"add\",\"value\":{\"displayName\": \"XwLtOP23-patchNonExistingGroup\"}}]}");

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Patch group with add operation");
        try {
            requestPath1.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath1.setTestSupported(true);
        }

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Patch group with remove operation");
        try {
            requestPath2.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath2.setTestSupported(true);
        }

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Patch group with replace operation");
        try {
            requestPath3.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath3.setTestSupported(true);
        }

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setTestCaseName("Patch group with array of operations");
        try {
            requestPath4.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath4.setTestSupported(true);
        }

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setTestCaseName("Patch group and validate error response");
        try {
            requestPath5.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath5.setTestSupported(true);
        }

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setTestCaseName("Patch non existing group");
        requestPath6.setUrl(generateUniqueID());
        try {
            requestPath6.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath6.setTestSupported(true);
        }

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3, requestPath4, requestPath5,
                requestPath6 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            Group group;
            ArrayList<String> groupId = createTestsGroups(userIDs, "One");
            String id = groupId.get(0);
            String patchGroupURL;
            patchGroupURL = url + "/" + id + requestPaths[i].getUrl();
            HttpPatch method = new HttpPatch(patchGroupURL);
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                // Patch group.
                HttpEntity entity = new ByteArrayEntity(definedPatchedGroup.get(i).getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                        ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
                method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                        ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                // Get all headers.
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                cleanUpGroup(id, "Patch Group");
                if (!requestPaths[i].getTestCaseName().equals("Patch group and validate error response")
                        && !requestPaths[i].getTestCaseName().equals("Patch non existing group")
                        && requestPaths[i].getTestSupported()
                        && response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_IMPLEMENTED) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not patch the default group at url " + url, ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                /*
                 * Obtain the schema corresponding to user. Unless configured returns core-user schema or else returns
                 * extended user schema.
                 */
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    group = jsonDecoder.decodeResource(responseString, schema, new Group());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    cleanUpGroup(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(group, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    cleanUpGroup(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                }
                cleanUpGroup(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Patch group and validate error response")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_BAD_REQUEST), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Service Provider successfully given the expected error 400", ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));

            } else if (requestPaths[i].getTestCaseName().equals("Patch non existing group")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_NOT_FOUND), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Server successfully given the expected error 404 message", ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));
            } else if (!requestPaths[i].getTestSupported()
                    || response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_IMPLEMENTED) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_SKIPPED, subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SKIPPED, requestPaths[i].getTestCaseName(),
                        "This functionality is not implemented. Hence given status code 501",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            }
        }
        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "Patch groups test");
        }
        return testResults;
    }

    /**
     * Put group tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> putMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> definedGroups = new ArrayList<>();

        definedGroups.add("{\n" + "  \"displayName\": \"XwLtOP23-Updated\",\n" + "  \"members\": [\n" + "    {\n"
                + "      \"display\": \"loginUser1\"\n" + "    },{\n" + "          \"display\": \"loginUser2\"\n"
                + "    },{\n" + "          \"display\": \"loginUser3\"\n" + "    },{\n"
                + "          \"display\": \"loginUser4\"\n" + "    },{\n" + "          \"display\": \"loginUser5\"\n"
                + "    }\n" + "  ]\n" + "}");
        definedGroups.add("{\"displaayName\": \"XwLtOP23-Updated\"}");
        definedGroups.add("{\"displayName\": \"XwLtOP23-UpdatedWithNonExistingId\"}");
        ArrayList<String> userIDs;
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Update Group");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Update group with schema violation to validate error response");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Update non existing group and and verify Http status code");
        requestPath3.setUrl(generateUniqueID());

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            userIDs = createTestsUsers("Many");
            ArrayList<String> groupId = createTestsGroups(userIDs, "One");
            String id = groupId.get(0);
            Group group;
            String updateUserURL;
            updateUserURL = url + "/" + id + requestPaths[i].getUrl();
            HttpPut method = new HttpPut(updateUserURL);
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                // Update the group.
                HttpEntity entity = new ByteArrayEntity(definedGroups.get(i).getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                        ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
                method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                        ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                // get all headers
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                // Clean up users.
                for (String uId : userIDs) {
                    cleanUpUser(uId, requestPaths[i].getTestCaseName());
                }
                cleanUpGroup(id, requestPaths[i].getTestCaseName());
                if (requestPaths[i].getTestCaseName().equals("Update Group")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not update the default group at url " + url, ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                /*
                 * Obtain the schema corresponding to user. Unless configured returns core-user schema or else returns
                 * extended user schema.
                 */
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    group = jsonDecoder.decodeResource(responseString, schema, new Group());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    // Clean up users.
                    for (String uId : userIDs) {
                        cleanUpUser(uId, requestPaths[i].getTestCaseName());
                    }
                    cleanUpGroup(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                try {
                    ResponseValidateTests.runValidateTests(group, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);

                } catch (BadRequestException | CharonException e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    // Clean up users.
                    for (String uId : userIDs) {
                        cleanUpUser(uId, requestPaths[i].getTestCaseName());
                    }
                    cleanUpGroup(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                // Clean up users.
                for (String uId : userIDs) {
                    cleanUpUser(uId, requestPaths[i].getTestCaseName());
                }
                cleanUpGroup(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName()
                                      .equals("Update group with schema violation to validate " + "error response")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_BAD_REQUEST), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Service Provider successfully given the expected error 400", ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));

            } else if ((requestPaths[i].getTestCaseName()
                                       .equals("Update non existing group and and verify Http " + "status code"))
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_NOT_FOUND), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Server successfully given the expected error 404 message", ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));
            } else {
                // Clean up users.
                for (String uId : userIDs) {
                    cleanUpUser(uId, requestPaths[i].getTestCaseName());
                }
                cleanUpGroup(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            }
        }
        return testResults;
    }

    /**
     * Delete group tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> deleteMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> userIDs;
        userIDs = createTestsUsers("Many");
        ArrayList<String> groupId = createTestsGroups(userIDs, "One");
        String id = groupId.get(0);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("Delete group by ID");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl(StringUtils.EMPTY);
        requestPath2.setTestCaseName("Delete group twice and verify Http status code");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl(generateUniqueID());
        requestPath3.setTestCaseName("Delete group with non existing ID and validate group not found error response");

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3 };

        for (RequestPath requestPath : requestPaths) {
            long startTime = System.currentTimeMillis();
            String deleteGroupURL;
            deleteGroupURL = url + "/" + id + requestPath.getUrl();
            HttpDelete method = new HttpDelete(deleteGroupURL);
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
            } catch (Exception e) {
                /*
                 * Read the response body. Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();
                if (requestPath.getTestCaseName().equals("Delete group by ID")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()),
                            String.valueOf(HttpStatus.SC_NO_CONTENT), ComplianceConstants.TestConstants.STATUS_FAILED,
                            subTests);
                    // Clean up group.
                    cleanUpGroup(id, requestPath.getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                            "Could not delete the default group at url " + url, ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_NO_CONTENT), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPath.getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if ((requestPath.getTestCaseName().equals("Delete group twice and verify Http status code")
                    || requestPath.getTestCaseName()
                                  .equals("Delete group with non existing ID and validate "
                                          + "group not found error response"))
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_NOT_FOUND), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPath.getTestCaseName(),
                        "Server successfully given the expected error 404 message", ComplianceUtils.getWire(method,
                                responseString, headerString.toString(), responseStatus, subTests),
                        stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            }
        }
        // Clean up users.
        for (String uId : userIDs) {
            cleanUpUser(uId, "Delete groups");
        }
        return testResults;
    }

    /**
     * Search group tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> searchMethodTest() throws GeneralComplianceException, ComplianceException {

        // Store test results.
        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        // Store userIDS of 5 users.
        ArrayList<String> userIDs = createTestsUsers("Many");
        ArrayList<String> groupIDs = createTestsGroups(userIDs, "One");

        // Post bodies of search methods.
        ArrayList<String> definedSearchMethods = new ArrayList<>();
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.getDefinedSearchGroupsPayload1);
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.getDefinedSearchGroupsPayload2);
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.getDefinedSearchGroupsPayload3);
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.getDefinedSearchGroupsPayload4);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Search groups with displayName as filter and with pagination query parameters");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Search group with invalid filter");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Search group without pagination parameters");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setTestCaseName("Search group with index paging and without count parameter");

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3, requestPath4 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            String searchUsersUrl;
            searchUsersUrl = url + "/.search";
            HttpPost method = new HttpPost(searchUsersUrl);
            // Create group test.
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            method.setHeader(ComplianceConstants.RequestCodeConstants.CONTENT_TYPE,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            int totalResults;
            // JSONObject jsonObj = null;
            ArrayList<String> subTests = new ArrayList<>();
            boolean errorOccur = false;
            try {
                // Create the request.
                HttpEntity entity = new ByteArrayEntity(definedSearchMethods.get(i).getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

            } catch (Exception e) {
                /*
                 * Read the response body. Get all headers.
                 */
                assert response != null;
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    headerString.append(String.format("%s : %s \n", header.getName(), header.getValue()));
                }
                responseStatus = response.getStatusLine().getStatusCode() + " "
                        + response.getStatusLine().getReasonPhrase();

                if (!requestPaths[i].getTestCaseName().equals("Search group with invalid filter")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not create default group at url " + url, ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                        ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                // Obtain the schema corresponding to group.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                JSONObject jsonObjResponse;
                ArrayList<Group> groupList = new ArrayList<>();
                try {
                    JSONObject jsonObj = new JSONObject(responseString);
                    jsonObjResponse = jsonObj;
                    JSONArray groupsArray = jsonObj.getJSONArray("Resources");
                    totalResults = (int) jsonObjResponse.get("totalResults");
                    JSONObject tmp;
                    for (int j = 0; j < groupsArray.length(); j++) {
                        tmp = groupsArray.getJSONObject(j);
                        groupList.add(jsonDecoder.decodeResource(tmp.toString(), schema, new Group()));
                        try {
                            ResponseValidateTests.runValidateTests(groupList.get(j), schema, null, null, method,
                                    responseString, headerString.toString(), responseStatus, subTests);
                        } catch (BadRequestException | CharonException e) {
                            subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                            subTests.add(StringUtils.EMPTY);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(
                                    new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                                    headerString.toString(), responseStatus, subTests),
                                            stopTime - startTime));
                            errorOccur = true;
                            break;
                        }
                    }
                } catch (JSONException | BadRequestException | CharonException | InternalErrorException e) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                if (totalResults == 1) {
                    // Assert expected result.
                    addAssertion("Check expected result",
                            "Check expected group with displayName " + "XwLtOP23 contained in response.",
                            ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                    if (!errorOccur) {
                        long stopTime = System.currentTimeMillis();
                        testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                        headerString.toString(), responseStatus, subTests),
                                stopTime - startTime));
                    }
                } else {
                    // Assert expected result.
                    addAssertion("Check expected result",
                            "Check expected group with displayName " + "XwLtOP23 contained in response.",
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                }
            } else if (requestPaths[i].getTestCaseName().equals("Search group with invalid filter")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_BAD_REQUEST), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Service Provider successfully given the expected error 400 message",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else {
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            }
        }
        // Clean up users after all tasks.
        for (String id : userIDs) {
            cleanUpUser(id, "Search groups");
        }
        // Clean up groups.
        for (String id : groupIDs) {
            cleanUpGroup(id, "Search groups");
        }
        return testResults;
    }

    /**
     * Execute all tests.
     *
     * @return null.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> executeAllTests() throws GeneralComplianceException, ComplianceException {

        // This method is not needed for the current implementation.
        return null;
    }
}