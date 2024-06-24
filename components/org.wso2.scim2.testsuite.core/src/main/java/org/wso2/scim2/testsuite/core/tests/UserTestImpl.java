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
import java.util.List;
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
import org.mortbay.log.Log;
import org.wso2.charon3.core.encoder.JSONDecoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.InternalErrorException;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.objects.plainobjects.MultiValuedComplexType;
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
 * Implementation of User test cases.
 */
public class UserTestImpl implements ResourceType {

    private final ComplianceTestMetaDataHolder complianceTestMetaDataHolder;

    private final String url;

    /**
     * Initialize.
     *
     * @param complianceTestMetaDataHolder Stores data required to run tests.
     */
    public UserTestImpl(ComplianceTestMetaDataHolder complianceTestMetaDataHolder) {

        this.complianceTestMetaDataHolder = complianceTestMetaDataHolder;
        url = complianceTestMetaDataHolder.getUrl() + ComplianceConstants.TestConstants.USERS_ENDPOINT;
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
     * Method check whether return users are sorted or not.
     *
     * @param userList Array of users to get checked whether they are sorted or not.
     * @return true or false.
     * @throws CharonException Exception by charon library.
     */
    private boolean isUserListSorted(ArrayList<User> userList) throws CharonException {

        boolean sorted = true;
        for (int i = 1; i < userList.size(); i++) {
            String previous = userList.get(i - 1).getId();
            String next  = userList.get(i).getId();
            if (previous.compareTo(next) > 0 && previous.compareToIgnoreCase(next) > 0) {
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
     * Initiate test data needed for user get test case.
     *
     * @return requestPaths Array of initialize data.
     */
    private RequestPath[] initiateData() {

        RequestPath[] requestPaths;
        // Creating objects to store sub test information.
        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("List Users");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("?attributes=userName,name.givenName");
        requestPath2.setTestCaseName("List users with specified resource attributes to return");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl("?excludedAttributes=name.givenName,emails");
        requestPath3.setTestCaseName("List users excluding attributes givenName and emails");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setUrl("?sortBy=id&sortOrder=ascending");
        requestPath4.setTestCaseName("Sort users by user id without pagination and filtering params");
        try {
            requestPath4.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getSortSupported());
        } catch (Exception e) {
            requestPath4.setTestSupported(true);
        }

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setUrl("?startIndex=1&count=2");
        requestPath5.setTestCaseName("List users with pagination");

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setUrl("?startIndex=-1&count=2");
        requestPath6.setTestCaseName("Paginate users with a negative startIndex");

        RequestPath requestPath7 = new RequestPath();
        requestPath7.setUrl("?count=2");
        requestPath7.setTestCaseName("Paginate users without startIndex and with positive count param");

        RequestPath requestPath8 = new RequestPath();
        requestPath8.setUrl("?startIndex=1");
        requestPath8.setTestCaseName("Paginate users with positive startIndex and without count param");

        RequestPath requestPath9 = new RequestPath();
        requestPath9.setUrl("?filter=userName+eq+%22loginUser1%22");
        requestPath9.setTestCaseName("List users by filtering - userName eq");
        try {
            requestPath9.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath9.setTestSupported(true);
        }

        RequestPath requestPath10 = new RequestPath();
        requestPath10.setUrl("?filter=userName+eq+%22loginUser1%22&startIndex=1&count=1");
        requestPath10.setTestCaseName("Filter users by username with pagination params");
        try {
            requestPath10.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath10.setTestSupported(true);
        }

        RequestPath requestPath11 = new RequestPath();
        requestPath11.setUrl("?filter=userName+eq+%22loginUser1%22&startIndex=1");
        requestPath11.setTestCaseName("List users by filtering - userName eq with only using startIndex");
        try {
            requestPath11.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath11.setTestSupported(true);
        }

        /*
         * RequestPath requestPath12 = new RequestPath();
         * requestPath12.setUrl("?filter=userName+eq+%22loginUser1%22+and+emails.type+eq+%22work%22");
         * requestPath12.setTestCaseName("List users by filtering - userName eq and emails.type"); try {
         * requestPath12.setTestSupported(complianceTestMetaDataHolder.getScimServiceProviderConfig().
         * getFilterSupported()); } catch (Exception e) { requestPath12.setTestSupported(true); }
         */

        RequestPath requestPath13 = new RequestPath();
        requestPath13.setUrl("?filter=USERNAME+eq+%22loginUser1%22");
        requestPath13.setTestCaseName("List users by filtering - userName eq to check case insensitivity of attribute");
        try {
            requestPath13.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath13.setTestSupported(true);
        }

        RequestPath requestPath14 = new RequestPath();
        requestPath14.setUrl("?filter=userName+EQ+%22loginUser1%22");
        requestPath14.setTestCaseName("List users by filtering - userName eq to check case insensitivity of operator");
        try {
            requestPath14.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath14.setTestSupported(true);
        }

        RequestPath requestPath15 = new RequestPath();
        requestPath15.setUrl("?filter=userName+ne+%22loginUser1%22");
        requestPath15.setTestCaseName("List users by filtering - userName ne");
        try {
            requestPath15.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath15.setTestSupported(true);
        }

        RequestPath requestPath16 = new RequestPath();
        requestPath16.setUrl("?filter=userName+co+%22loginUser1%22");
        requestPath16.setTestCaseName("List users by filtering - userName co");
        try {
            requestPath16.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath16.setTestSupported(true);
        }

        RequestPath requestPath17 = new RequestPath();
        requestPath17.setUrl("?filter=userName+sw+%22loginUser1%22");
        requestPath17.setTestCaseName("List users by filtering - userName sw");
        try {
            requestPath17.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath17.setTestSupported(true);
        }

        RequestPath requestPath18 = new RequestPath();
        requestPath18.setUrl("?filter=userName+ew+%22ginUser1%22");
        requestPath18.setTestCaseName("List users by filtering - userName ew");
        try {
            requestPath18.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath18.setTestSupported(true);
        }

        RequestPath requestPath19 = new RequestPath();
        requestPath19.setUrl("?filter=userName+pr");
        requestPath19.setTestCaseName("List users by filtering - userName pr");
        try {
            requestPath19.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported());
        } catch (Exception e) {
            requestPath19.setTestSupported(true);
        }

        /*
         * RequestPath requestPath20 = new RequestPath(); requestPath20.setUrl(
         * "?filter=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" + ":employeeNumber+ne+12345");
         * requestPath20.setTestCaseName("List users by filtering - employeeNumber ne"); try {
         * requestPath20.setTestSupported(
         * complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported()); } catch (Exception e) {
         * requestPath20.setTestSupported(true); }
         */

        /*
         * RequestPath requestPath21 = new RequestPath(); requestPath21.setUrl(
         * "?filter=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" + ":employeeNumber+co+12345");
         * requestPath21.setTestCaseName("List users by filtering - employeeNumber co"); try {
         * requestPath21.setTestSupported(
         * complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported()); } catch (Exception e) {
         * requestPath21.setTestSupported(true); }
         */

        /*
         * RequestPath requestPath22 = new RequestPath(); requestPath22.setUrl(
         * "?filter=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" + ":employeeNumber+sw+12345");
         * requestPath22.setTestCaseName("List users by filtering - employeeNumber sw"); try {
         * requestPath22.setTestSupported(
         * complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported()); } catch (Exception e) {
         * requestPath22.setTestSupported(true); }
         */

        /*
         * RequestPath requestPath23 = new RequestPath(); requestPath23.setUrl(
         * "?filter=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" + ":employeeNumber+ew+12345");
         * requestPath23.setTestCaseName("List users by filtering - employeeNumber ew"); try {
         * requestPath23.setTestSupported(
         * complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported()); } catch (Exception e) {
         * requestPath23.setTestSupported(true); }
         */

        /*
         * RequestPath requestPath24 = new RequestPath(); requestPath24.setUrl(
         * "?filter=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" + ":employeeNumber+eq+12345");
         * requestPath24.setTestCaseName("List users by filtering - employeeNumber eq"); try {
         * requestPath24.setTestSupported(
         * complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported()); } catch (Exception e) {
         * requestPath24.setTestSupported(true); }
         */

        /*
         * RequestPath requestPath25 = new RequestPath(); requestPath25.setUrl(
         * "?filter=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" + ":employeeNumber+pr");
         * requestPath25.setTestCaseName("List users by filtering - employeeNumber pr"); try {
         * requestPath25.setTestSupported(
         * complianceTestMetaDataHolder.getScimServiceProviderConfig().getFilterSupported()); } catch (Exception e) {
         * requestPath25.setTestSupported(true); }
         */

        // This array hold the sub tests details.
        requestPaths = new RequestPath[] {
                requestPath1, requestPath2, requestPath3, requestPath4, requestPath5, requestPath6, requestPath7,
                requestPath8, requestPath9, requestPath10, requestPath11, /* requestPath12, */
                requestPath13, requestPath14, requestPath15, requestPath16, requestPath17, requestPath18, requestPath19,
                /* requestPath20, */ /* requestPath21, */ /* requestPath22, */ /* requestPath23, */ /* requestPath24, */
                /* requestPath25 */ };
        return requestPaths;
    }

    /**
     * Get user tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> getMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults = new ArrayList<>();
        ArrayList<String> userIDs;
        RequestPath[] requestPaths;

        // Initialize 5 users.
        userIDs = createTestsUsers("Many");
        // Initiate data necessary for getMethod test.
        requestPaths = initiateData();

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
                                    "Could not list the users at url " + url, ComplianceUtils.getWire(method,
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
                // Obtain the schema corresponding to user.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                ArrayList<User> userList = new ArrayList<>();
                JSONObject jsonObjResponse;
                try {
                    // Called only for user get by id.
                    JSONObject jsonObj = new JSONObject(responseString);
                    jsonObjResponse = jsonObj;
                    JSONArray usersArray = jsonObj.getJSONArray("Resources");
                    startIndex = (int) jsonObjResponse.get("startIndex");
                    count = (int) jsonObjResponse.get("totalResults");
                    JSONObject tmp;
                    for (int j = 0; j < usersArray.length(); j++) {
                        tmp = usersArray.getJSONObject(j);
                        userList.add(jsonDecoder.decodeResource(tmp.toString(), schema, new User()));
                        try {
                            ResponseValidateTests.runValidateTests(userList.get(j), schema, null, null, method,
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
                // Check for all created groups.
                switch (requestPath.getTestCaseName()) {
                    case "List Users":
                    case "Paginate users with positive startIndex and without count param":
                    case "List users by filtering - userName pr":
                        // Check for list of users returned.
                        ArrayList<String> returnedUserIDs = new ArrayList<>();
                        for (User u : userList) {
                            returnedUserIDs.add(u.getId());
                        }
                        for (String id : userIDs) {
                            if (!returnedUserIDs.contains(id)) {
                                addAssertion(ComplianceConstants.TestConstants.ALL_USERS_IN_TEST,
                                        "Check the created 5 users are listed.",
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain all the created users",
                                        ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                                responseStatus, subTests),
                                        stopTime - startTime));
                                errorOccur = true;
                                break;
                            }
                        }
                        if (!errorOccur) {
                            addAssertion(ComplianceConstants.TestConstants.ALL_USERS_IN_TEST,
                                    "Check the created 5 users are listed.",
                                    ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        }
                        break;
                    case "List users by filtering - userName eq":
                    case "List users by filtering - userName eq with only using startIndex":
                    case "List users by filtering - userName eq to check case insensitivity of attribute":
                    case "List users by filtering - userName eq to check case insensitivity of operator":
                    case "List users by filtering - userName co":
                    case "List users by filtering - userName sw":
                    case "List users by filtering - userName ew": {
                        String value = "loginUser1";
                        for (User user1 : userList) {
                            try {
                                if (!value.equals(user1.getUserName())) {
                                    addAssertion(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST,
                                            "userName:" + user1.getUserName(), "userName:" + value,
                                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                    long stopTime = System.currentTimeMillis();
                                    testResults.add(
                                            new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                                    "Response does not contain the expected users",
                                                    ComplianceUtils.getWire(method, responseString,
                                                            headerString.toString(), responseStatus, subTests),
                                                    stopTime - startTime));
                                    errorOccur = true;
                                    break;
                                }
                            } catch (CharonException e) {
                                addAssertion(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST,
                                        "userName:" + StringUtils.EMPTY, "userName:" + value,
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain the expected users", ComplianceUtils.getWire(method,
                                                responseString, headerString.toString(), responseStatus, subTests),
                                        stopTime - startTime));
                                errorOccur = true;
                                break;
                            }
                        }
                        if (!errorOccur) {
                            addAssertion(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST, "userName:" + value,
                                    "userName:" + value, ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        }
                        break;
                    }
                    case "List users by filtering - userName ne": {
                        String value = "loginUser1";
                        for (User user1 : userList) {
                            try {
                                if (value.equals(user1.getUserName())) {
                                    addAssertion(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST,
                                            "userName:" + user1.getUserName(), "userName:" + value,
                                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                    long stopTime = System.currentTimeMillis();
                                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                            "Response contains the unexpected users", ComplianceUtils.getWire(method,
                                                    responseString, headerString.toString(), responseStatus, subTests),
                                            stopTime - startTime));
                                    errorOccur = true;
                                    break;
                                }
                            } catch (CharonException e) {
                                addAssertion(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST,
                                        "userName:" + StringUtils.EMPTY, "userName:" + value,
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain the expected users", ComplianceUtils.getWire(method,
                                                responseString, headerString.toString(), responseStatus, subTests),
                                        stopTime - startTime));
                                errorOccur = true;
                                break;
                            }
                        }
                        if (!errorOccur) {
                            addAssertion(ComplianceConstants.TestConstants.FILTER_CONTENT_TEST, "userName:" + value,
                                    "userName:" + value, ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        }
                        break;
                    }
                    case "List users with pagination":
                    case "Paginate users with a negative startIndex":
                        String assertionName = null;
                        if (requestPath.getTestCaseName().equals("List users with pagination")) {
                            assertionName = "Validate paginated users response";
                        } else if (requestPath.getTestCaseName().equals("Paginate users with a negative startIndex")) {
                            assertionName = "Test user pagination when startIndex is not specified";
                        }
                        if (userList.size() != 2) {
                            addAssertion(assertionName,
                                    "startIndex:" + startIndex + "," + "totalResults:" + userList.size(),
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
                        addAssertion(assertionName,
                                "startIndex:" + startIndex + "," + "totalResults:" + userList.size(),
                                "startIndex:1,totalResults:2", ComplianceConstants.TestConstants.STATUS_SUCCESS,
                                subTests);
                        break;
                    case "Sort users by user id without pagination and " + "filtering params":
                        try {
                            if (!isUserListSorted(userList)) {
                                addAssertion(ComplianceConstants.TestConstants.SORT_USERS_TEST,
                                        "Check the created 5 users are sorted or not.",
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain the sorted list of users",
                                        ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                                responseStatus, subTests),
                                        stopTime - startTime));
                                continue;
                            }
                        } catch (CharonException e) {
                            addAssertion(ComplianceConstants.TestConstants.SORT_USERS_TEST,
                                    "Check the created 5 users are sorted or not.",
                                    ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(ComplianceConstants.TestConstants.SORT_USERS_TEST,
                                "Check the created 5 users are sorted or not.",
                                ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        break;
                    case "Filter users by username with pagination " + "params": {
                        if (userList.size() != 1) {
                            addAssertion(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION,
                                    "startIndex:" + startIndex + "," + "totalResults:" + userList.size(),
                                    "startIndex:1,totalResults:1", ComplianceConstants.TestConstants.STATUS_FAILED,
                                    subTests);
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Response does not contain right number of users.", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        String value = "loginUser1";
                        for (User user1 : userList) {
                            try {
                                if (!value.equals(user1.getUserName())) {
                                    addAssertion(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION,
                                            "startIndex:" + startIndex + ",totalResults:" + userList.size()
                                                    + ",userName:" + user1.getUserName(),
                                            "startIndex:1,totalResults:1,userName:loginUser1",
                                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                    long stopTime = System.currentTimeMillis();
                                    testResults.add(
                                            new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                                    "Response does not contain the expected users",
                                                    ComplianceUtils.getWire(method, responseString,
                                                            headerString.toString(), responseStatus, subTests),
                                                    stopTime - startTime));
                                    errorOccur = true;
                                    break;
                                }
                            } catch (CharonException e) {
                                addAssertion(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION,
                                        "startIndex:null,totalResults:null,userName:null",
                                        "startIndex:1,totalResults:1,userName:loginUser1",
                                        ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                                long stopTime = System.currentTimeMillis();
                                testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                        "Response does not contain the expected users", ComplianceUtils.getWire(method,
                                                responseString, headerString.toString(), responseStatus, subTests),
                                        stopTime - startTime));
                                errorOccur = true;
                                break;
                            }
                        }
                        if (!errorOccur) {
                            addAssertion(ComplianceConstants.TestConstants.FILTER_USER_WITH_PAGINATION,
                                    "startIndex:1,totalResults:1,userName:1",
                                    "startIndex:1,totalResults:1,userName:loginUser1",
                                    ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        }
                        break;
                    }
                    case "Paginate users without startIndex and with " + "positive count param":
                        if (startIndex != 1 && count != 2) {
                            addAssertion("Test user pagination when startIndex is not specified",
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
                        addAssertion("Test user pagination when startIndex is not specified",
                                "startIndex:" + startIndex + "," + "totalResults:" + count,
                                "startIndex:1,totalResults:2", ComplianceConstants.TestConstants.STATUS_SUCCESS,
                                subTests);
                        break;
                    default: {
                        Log.warn("{} not asserted ", requestPath.getTestCaseName());
                    }
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
            cleanUpUser(id, "get users test");
        }
        // This should be a array containing results.
        return testResults;
    }

    /**
     * Get user by id tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> getByIdMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("Get user by ID");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl("?attributes=userName,name.givenName");
        requestPath2.setTestCaseName("Get a user with specific attributes userName and givenName");

        // RequestPath requestPath3 = new RequestPath();
        // requestPath3.setUrl("?excludedAttributes=emails");
        // requestPath3.setTestCaseName("Get a user with excluding attribute emails");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setUrl(generateUniqueID());
        requestPath4.setTestCaseName("Get a non existing user and validate user not found error response");

        // RequestPath requestPath5 = new RequestPath();
        // requestPath5.setUrl("?attributes=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:employeeNumber" +
        // ",userName");
        // requestPath5.setTestCaseName("Get a enterprise user with specific attribute employeeNumber");

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setUrl(
                "?excludedAttributes=urn:ietf:params:scim:schemas:extension:enterprise:2.0:User" + ":employeeNumber");
        requestPath6.setTestCaseName("Get a enterprise user with excluding attribute employeeNumber");

        // requestPaths = new RequestPath[]{requestPath1, requestPath2, requestPath3, requestPath4, requestPath5,
        // requestPath6};
        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath4, requestPath6 };

        for (RequestPath requestPath : requestPaths) {
            long startTime = System.currentTimeMillis();
            // Create default user.
            ArrayList<String> userIDs;
            userIDs = createTestsUsers("One");
            String id = userIDs.get(0);
            String getUserURL = url + "/" + id + requestPath.getUrl();
            HttpGet method = new HttpGet(getUserURL);
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            method.setHeader(ComplianceConstants.RequestCodeConstants.ACCEPT,
                    ComplianceConstants.RequestCodeConstants.APPLICATION_JSON);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            String locationHeader = null;
            JSONObject jsonObj;
            String givenName;
            String userName;
            try {
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
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
                // Clean the created user.
                cleanUpUser(id, "Get User");
                if (!requestPath.getTestCaseName()
                                .equals("Get a non existing user and validate user not found " + "error response")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                            "Could not get the default user from url " + url, ComplianceUtils.getWire(method,
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
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    jsonObj = new JSONObject(responseString);
                } catch (JSONException e) {
                    // Clean the created user.
                    cleanUpUser(id, "Get User");
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                            "Could not decode response from server response", ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                User user;
                try {
                    user = jsonDecoder.decodeResource(responseString, schema, new User());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    // Clean the created user.
                    cleanUpUser(id, "Get User");
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, locationHeader, url + "/" + id,
                            ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                } else {
                    addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, null, url + "/" + id,
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);
                } catch (Exception e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    // Clean the created user.
                    cleanUpUser(id, "Get User");
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                switch (requestPath.getTestCaseName()) {
                    case "Get a user with specific attributes userName and " + "givenName":
                        try {
                            JSONObject innerJsonObject = jsonObj.getJSONObject("name");
                            givenName = innerJsonObject.getString("givenName");
                        } catch (JSONException e) {
                            addAssertion(requestPath.getTestCaseName() + " test", "givenName:" + null, "givenName:Kim",
                                    ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode givenName attribute from server response",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        try {
                            userName = user.getUserName();
                        } catch (CharonException e) {
                            addAssertion(requestPath.getTestCaseName() + " test",
                                    "userName:" + null + "givenName:" + givenName, "userName:loginUser,givenName:Kim",
                                    ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode userName attribute from server response",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        if (!userName.equals("loginUser") || !givenName.equals("Kim")) {
                            addAssertion(requestPath.getTestCaseName() + " test",
                                    "userName:" + userName + "givenName" + ":" + givenName,
                                    "userName:loginUser," + "givenName:Kim",
                                    ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not retrieve the expected attributes.", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(requestPath.getTestCaseName() + " test", "userName:loginUser," + "givenName:Kim",
                                "userName:loginUser,givenName:Kim", ComplianceConstants.TestConstants.STATUS_SUCCESS,
                                subTests);
                        break;
                    case "Get a user with excluding attribute emails":
                        List<MultiValuedComplexType> emails;
                        try {
                            emails = user.getEmails();
                        } catch (Exception e) {
                            addAssertion(requestPath.getTestCaseName() + " test", "emails:" + null, "emails:null",
                                    ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode emails complex multivalued attribute from server response",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        if (emails.size() != 0) {
                            addAssertion(requestPath.getTestCaseName() + " test", "emails:" + emails, "emails:null",
                                    ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Excluded attribute is present in response.", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(requestPath.getTestCaseName() + " test", "emails:null", "emails:null",
                                ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        break;
                    case "Get a enterprise user with specific attribute " + "employeeNumber": {
                        String employeeNumber;
                        try {
                            JSONObject innerJsonObject = jsonObj.getJSONObject(
                                    "urn:ietf:params:scim:schemas:" + "extension:enterprise:2.0:User");
                            employeeNumber = innerJsonObject.getString("employeeNumber");
                        } catch (JSONException e) {
                            addAssertion(requestPath.getTestCaseName() + " test", "employeeNumber:" + null,
                                    "employeeNumber:1234A", ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not decode employeeNumber attribute from server response",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        if (!employeeNumber.equals("1234A")) {
                            addAssertion(requestPath.getTestCaseName() + " test", "employeeNumber:" + employeeNumber,
                                    "employeeNumber:1234A", ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "Could not retrieve the expected attribute employeeNumber",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(requestPath.getTestCaseName() + " test", "employeeNumber:1234A",
                                "employeeNumber:1234A", ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        break;
                    }
                    case "Get a enterprise user with excluding attribute " + "employeeNumber": {
                        String employeeNumber = null;
                        try {
                            JSONObject innerJsonObject = jsonObj.getJSONObject(
                                    "urn:ietf:params:scim:schemas:" + "extension:enterprise:2.0:User");
                            employeeNumber = innerJsonObject.getString("employeeNumber");
                        } catch (JSONException ignored) {

                        }
                        if (employeeNumber != null) {
                            addAssertion(requestPath.getTestCaseName() + " test", "employeeNumber:" + employeeNumber,
                                    "employeeNumber:null", ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                            // Clean the created user.
                            cleanUpUser(id, "Get User");
                            long stopTime = System.currentTimeMillis();
                            testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                                    "EmployeeNumber attribute from server response is not excluded as expected",
                                    ComplianceUtils.getWire(method, responseString, headerString.toString(),
                                            responseStatus, subTests),
                                    stopTime - startTime));
                            continue;
                        }
                        addAssertion(requestPath.getTestCaseName() + " test", "employeeNumber:null",
                                "employeeNumber:null", ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                        break;
                    }
                }
                // Clean the created user.
                cleanUpUser(id, "Get User");
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPath.getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPath.getTestCaseName()
                                  .equals("Get a non existing user and validate user not " + "found error response")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_NOT_FOUND), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPath.getTestCaseName(),
                        "Server successfully given the expected error 404(User not found in the user store) "
                                + "message",
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else {
                // Clean the created user.
                cleanUpUser(id, "Get User");
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
     * Post user tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> postMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> definedUsers = new ArrayList<>();

        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUser1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedWithoutUserNameUser);

        ArrayList<String> userIDs = new ArrayList<>();
        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Create User");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Create User with existing userName");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Create User without userName");

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            User user;
            HttpPost method = new HttpPost(url);
            // Create user test.
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
            String locationHeader = null;
            String location;
            JSONObject jsonObj;
            try {
                // Create the user.
                HttpEntity entity = new ByteArrayEntity(definedUsers.get(i).getBytes(StandardCharsets.UTF_8));
                method.setEntity(entity);
                response = client.execute(method);
                // Read the response body.
                responseString = new BasicResponseHandler().handleResponse(response);
                // Get all headers.
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
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
                if (requestPaths[i].getTestCaseName().equals("Create User")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()),
                            String.valueOf(HttpStatus.SC_CREATED), ComplianceConstants.TestConstants.STATUS_FAILED,
                            subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
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
                /*
                 * Obtain the schema corresponding to user. Unless configured returns core-user schema or else returns
                 * extended user schema.
                 */
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    jsonObj = new JSONObject(responseString);
                    JSONObject innerJsonObject = jsonObj.getJSONObject("meta");
                    location = innerJsonObject.getString("location");
                } catch (JSONException e) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not decode location attribute from server response", ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                try {
                    user = jsonDecoder.decodeResource(responseString, schema, new User());
                    userIDs.add(user.getId());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    if (locationHeader.equals(location)) {
                        addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, locationHeader, location,
                                ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                    } else {
                        addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, locationHeader, location,
                                ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    }
                } else {
                    addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, null, location,
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Create User with existing userName")
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
            } else if (requestPaths[i].getTestCaseName().equals("Create User without userName")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                // Check for status returned.
                addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                        String.valueOf(response.getStatusLine().getStatusCode()),
                        String.valueOf(HttpStatus.SC_BAD_REQUEST), ComplianceConstants.TestConstants.STATUS_SUCCESS,
                        subTests);
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                        "Server successfully given the expected error 400(Required attribute userName is "
                                + "missing in the SCIM Object) message",
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
            cleanUpUser(id, "Post users test");
        }
        return testResults;
    }

    /**
     * Patch user tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> patchMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> definedUsers = new ArrayList<>();

        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload2);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload3);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload4);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload5);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload6);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedPatchUserPayload6);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Patch User with add operation");
        try {
            requestPath1.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath1.setTestSupported(true);
        }

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Patch User with remove operation");
        try {
            requestPath2.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath2.setTestSupported(true);
        }

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Patch User with replace operation");
        try {
            requestPath3.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath3.setTestSupported(true);
        }

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setTestCaseName("Patch User with array of operations");
        try {
            requestPath4.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath4.setTestSupported(true);
        }

        RequestPath requestPath5 = new RequestPath();
        requestPath5.setTestCaseName("Patch User - remove attribute without defining a path");
        try {
            requestPath5.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath5.setTestSupported(true);
        }

        RequestPath requestPath6 = new RequestPath();
        requestPath6.setTestCaseName("Patch Enterprise User with array of operations");
        try {
            requestPath6.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath6.setTestSupported(true);
        }

        RequestPath requestPath7 = new RequestPath();
        requestPath7.setTestCaseName("Patch non existing user with array of operations");
        requestPath7.setUrl(generateUniqueID());
        try {
            requestPath7.setTestSupported(
                    complianceTestMetaDataHolder.getScimServiceProviderConfig().getPatchSupported());
        } catch (Exception e) {
            requestPath7.setTestSupported(true);
        }

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3, requestPath4, requestPath5,
                requestPath6, requestPath7 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            // Create default user.
            ArrayList<String> userIDs;
            userIDs = createTestsUsers("One");
            String id = userIDs.get(0);
            User user;
            String patchUserURL = url + "/" + id + requestPaths[i].getUrl();
            HttpPatch method = new HttpPatch(patchUserURL);
            // Create user test.
            HttpClient client = HTTPClient.getHttpClient();
            HTTPClient.setAuthorizationHeader(complianceTestMetaDataHolder, method);
            HttpResponse response = null;
            String responseString = StringUtils.EMPTY;
            StringBuilder headerString = new StringBuilder(StringUtils.EMPTY);
            String responseStatus;
            ArrayList<String> subTests = new ArrayList<>();
            String locationHeader = null;
            try {
                // Patch the user.
                HttpEntity entity = new ByteArrayEntity(definedUsers.get(i).getBytes(StandardCharsets.UTF_8));
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
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
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
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                if (!requestPaths[i].getTestCaseName().equals("Patch User - remove attribute without defining a path")
                        && !requestPaths[i].getTestCaseName().equals("Patch non existing user with array of operations")
                        && requestPaths[i].getTestSupported()
                        && response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_IMPLEMENTED) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not patch the default user at url " + url, ComplianceUtils.getWire(method,
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
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = jsonDecoder.decodeResource(responseString, schema, new User());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    // Clean the created user.
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, locationHeader, url + "/" + id,
                            ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                } else {
                    addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, null, url + "/" + id,
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    long stopTime = System.currentTimeMillis();
                    // Clean the created user.
                    cleanUpUser(id, requestPaths[i].getTestCaseName());
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                    continue;
                }
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName()
                                      .equals("Patch User - remove attribute without defining a " + "path")
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
            } else if (requestPaths[i].getTestCaseName().equals("Patch non existing user with array of operations")
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
                // Clean the created user.
                cleanUpUser(id, requestPaths[i].getTestCaseName());
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
     * Put user tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> putMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        ArrayList<String> definedUsers = new ArrayList<>();

        definedUsers.add(ComplianceConstants.DefinedInstances.definedUpdatedUser1);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUpdatedUser2);
        definedUsers.add(ComplianceConstants.DefinedInstances.definedUpdatedUser1);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Update User");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Update user with schema violation");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Update non existing user and and verify Http status code");
        requestPath3.setUrl(generateUniqueID());

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            // Create default user.
            ArrayList<String> userIDs;
            userIDs = createTestsUsers("One");
            String id = userIDs.get(0);
            User user;
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
            String locationHeader = null;
            try {
                // Update the user.
                HttpEntity entity = new ByteArrayEntity(definedUsers.get(i).getBytes(StandardCharsets.UTF_8));
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
                    if (header.getName().equals("Location")) {
                        locationHeader = header.getValue();
                    }
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
                // Clean the created user.
                cleanUpUser(id, "Update User");
                if (requestPaths[i].getTestCaseName().equals("Update User")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not update the default user at url " + url, ComplianceUtils.getWire(method,
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
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                try {
                    user = jsonDecoder.decodeResource(responseString, schema, new User());
                } catch (BadRequestException | CharonException | InternalErrorException e) {
                    // Clean the created user.
                    cleanUpUser(id, "Update User");
                    long stopTime = System.currentTimeMillis();
                    testResults.add(
                            new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                                    "Could not decode the server response", ComplianceUtils.getWire(method,
                                            responseString, headerString.toString(), responseStatus, subTests),
                                    stopTime - startTime));
                    continue;
                }
                // Assertion to check location header.
                if (locationHeader != null) {
                    addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, locationHeader, url + "/" + id,
                            ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                } else {
                    addAssertion(ComplianceConstants.TestConstants.LOCATION_HEADER, null, url + "/" + id,
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                }
                try {
                    ResponseValidateTests.runValidateTests(user, schema, null, null, method, responseString,
                            headerString.toString(), responseStatus, subTests);
                } catch (BadRequestException | CharonException e) {
                    subTests.add(ComplianceConstants.TestConstants.STATUS_FAILED);
                    subTests.add(StringUtils.EMPTY);
                    // Clean the created user.
                    cleanUpUser(id, "Update User");
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Response Validation Error", ComplianceUtils.getWire(method, responseString,
                                    headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
                }
                // Clean the created user.
                cleanUpUser(id, "Update User");
                long stopTime = System.currentTimeMillis();
                testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(), StringUtils.EMPTY,
                        ComplianceUtils.getWire(method, responseString, headerString.toString(), responseStatus,
                                subTests),
                        stopTime - startTime));
            } else if (requestPaths[i].getTestCaseName().equals("Update user with schema violation")
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
                                       .equals("Update non existing user and and verify Http " + "status code"))
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
                // Clean the created user.
                cleanUpUser(id, "Update User");
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
     * Delete user tests.
     *
     * @return testResults Array containing test results.
     * @throws GeneralComplianceException General exceptions.
     * @throws ComplianceException Constructed new exception with the specified detail message.
     */
    @Override
    public ArrayList<TestResult> deleteMethodTest() throws GeneralComplianceException, ComplianceException {

        ArrayList<TestResult> testResults;
        testResults = new ArrayList<>();

        // Create default user.
        ArrayList<String> userIDs;
        userIDs = createTestsUsers("One");
        String id = userIDs.get(0);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setUrl(StringUtils.EMPTY);
        requestPath1.setTestCaseName("Delete user by ID");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setUrl(StringUtils.EMPTY);
        requestPath2.setTestCaseName("Delete user twice and verify Http status code");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setUrl(generateUniqueID());
        requestPath3.setTestCaseName("Delete non existing user and validate user not found error response");

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3 };

        for (RequestPath requestPath : requestPaths) {
            long startTime = System.currentTimeMillis();
            String deleteUserURL;
            deleteUserURL = url + "/" + id + requestPath.getUrl();
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
                if (requestPath.getTestCaseName().equals("Delete user by ID")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()),
                            String.valueOf(HttpStatus.SC_NO_CONTENT), ComplianceConstants.TestConstants.STATUS_FAILED,
                            subTests);
                    // Clean the created user.
                    cleanUpUser(id, "Delete User");
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPath.getTestCaseName(),
                            "Could not delete the default user at url " + url, ComplianceUtils.getWire(method,
                                    responseString, headerString.toString(), responseStatus, subTests),
                            stopTime - startTime));
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
            } else if ((requestPath.getTestCaseName()
                                   .equals("Delete non existing user and validate user not " + "found error response")
                    || requestPath.getTestCaseName().equals("Delete user twice and " + "verify Http status code"))
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
        return testResults;
    }

    /**
     * Search user tests.
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

        // Post bodies of search methods.
        ArrayList<String> definedSearchMethods = new ArrayList<>();

        definedSearchMethods.add(ComplianceConstants.DefinedInstances.definedSearchUsersPayload1);
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.definedSearchUsersPayload2);
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.definedSearchUsersPayload3);
        definedSearchMethods.add(ComplianceConstants.DefinedInstances.definedSearchUsersPayload4);

        RequestPath[] requestPaths;

        RequestPath requestPath1 = new RequestPath();
        requestPath1.setTestCaseName("Search user with filter and pagination query parameters");

        RequestPath requestPath2 = new RequestPath();
        requestPath2.setTestCaseName("Search user with invalid filter");

        RequestPath requestPath3 = new RequestPath();
        requestPath3.setTestCaseName("Search user without pagination parameters");

        RequestPath requestPath4 = new RequestPath();
        requestPath4.setTestCaseName("Search user with index paging and without count parameter");

        requestPaths = new RequestPath[] { requestPath1, requestPath2, requestPath3, requestPath4 };

        for (int i = 0; i < requestPaths.length; i++) {
            long startTime = System.currentTimeMillis();
            String searchUsersUrl;
            searchUsersUrl = url + "/.search";
            HttpPost method = new HttpPost(searchUsersUrl);
            // Create user test.
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
                if (!requestPaths[i].getTestCaseName().equals("Search user with invalid filter")) {
                    // Check for status returned.
                    addAssertion(ComplianceConstants.TestConstants.STATUS_CODE,
                            String.valueOf(response.getStatusLine().getStatusCode()), String.valueOf(HttpStatus.SC_OK),
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                    long stopTime = System.currentTimeMillis();
                    testResults.add(new TestResult(TestResult.ERROR, requestPaths[i].getTestCaseName(),
                            "Could not create default user at url " + url, ComplianceUtils.getWire(method,
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
                // Obtain the schema corresponding to user.
                SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
                JSONDecoder jsonDecoder = new JSONDecoder();
                ArrayList<User> userList = new ArrayList<>();
                JSONObject jsonObjResponse;
                try {
                    JSONObject jsonObj = new JSONObject(responseString);
                    jsonObjResponse = jsonObj;
                    JSONArray usersArray = jsonObj.getJSONArray("Resources");
                    totalResults = (int) jsonObjResponse.get("totalResults");
                    JSONObject tmp;
                    for (int j = 0; j < usersArray.length(); j++) {
                        tmp = usersArray.getJSONObject(j);
                        userList.add(jsonDecoder.decodeResource(tmp.toString(), schema, new User()));
                        try {
                            ResponseValidateTests.runValidateTests(userList.get(j), schema, null, null, method,
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
                if (totalResults == 5) {
                    // Assert expected result.
                    addAssertion("Check expected result",
                            "Expected 5 users whose userNames starts " + "with login contain in response.",
                            ComplianceConstants.TestConstants.STATUS_SUCCESS, subTests);
                    long stopTime = System.currentTimeMillis();
                    if (!errorOccur) {
                        testResults.add(new TestResult(TestResult.SUCCESS, requestPaths[i].getTestCaseName(),
                                StringUtils.EMPTY, ComplianceUtils.getWire(method, responseString,
                                        headerString.toString(), responseStatus, subTests),
                                stopTime - startTime));
                    }
                } else {
                    // Assert expected result.
                    addAssertion("Check expected result",
                            "Expected 5 users whose userNames starts" + " with login contain in response.",
                            ComplianceConstants.TestConstants.STATUS_FAILED, subTests);
                }
            } else if (requestPaths[i].getTestCaseName().equals("Search user with invalid filter")
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
            cleanUpUser(id, "Search users");
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