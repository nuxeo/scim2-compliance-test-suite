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
package org.wso2.scim2.testsuite.core.utils;

/**
 * This class contains the constants used in.
 */
public class ComplianceConstants {

    /**
     * This method contains the constants used in a  request.
     */
    public static class RequestCodeConstants {

        public static final String URL = "url";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String AUTHORIZATION_SERVER = "authorizationServer";
        public static final String HTTP = "http";
        public static final String HTTPS = "https";
        public static final String ACCEPT = "Accept";
        public static final String APPLICATION_JSON = "application/json";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    /**
     * This method contains the constants used in  tests.
     */
    public static class TestConstants {

        public static final String LABEL_IMPORTANT = "label-important";
        public static final String LABEL_SUCCESS = "label-success";
        public static final String LABEL_INFO = "label-info";

        public static final String FAILED = "Failed";
        public static final String SUCCESS = "Success";
        public static final String SKIPPED = "Skipped";

        // Endpoint constants.
        public static final String SERVICE_PROVIDER_ENDPOINT = "/ServiceProviderConfig";
        public static final String RESOURCE_TYPE_ENDPOINT = "/ResourceTypes";
        public static final String USERS_ENDPOINT = "/Users";
        public static final String GROUPS_ENDPOINT = "/Groups";
        public static final String ROLES_ENDPOINT = "/Roles";
        public static final String ME_ENDPOINT = "/Me";
        public static final String BULK_ENDPOINT = "/Bulk";
        public static final String SCHEMAS_ENDPOINT = "/Schemas";

        // Test name constants.
        public static final String SCHEMA_TEST = "Schema Test";
        public static final String REQUIRED_ATTRIBUTE_TEST = "Required Attribute Test";
        public static final String ATTRIBUTE_MUTABILITY_TEST = "Attribute Mutability Test";
        public static final String STATUS_CODE = "Verify Http response code";
        public static final String LOCATION_HEADER = "Verify Location Header";
        public static final String ALL_GROUPS_IN_TEST = "All Groups In Test";
        public static final String ALL_USERS_IN_TEST = "Test listing all users";
        public static final String PAGINATION_USER_TEST = "Pagination User Test";
        public static final String PAGINATION_GROUP_TEST = "Pagination Group Test";
        public static final String FILTER_CONTENT_TEST = "Validate the filter response with the filter search " +
                "attribute";
        public static final String SORT_USERS_TEST = "Sort Users Test";
        public static final String FILTER_USER_WITH_PAGINATION = "Test user filtering with pagination params";
        public static final String SORT_GROUPS_TEST = "Sort Groups Test";
        public static final String GET_CONFIG = "Get ServiceProviderConfig";
        public static final String GET_RESOURCETYPE = "Get ResourceType";
        public static final String GET_SCHEMAS = "Get Schemas";

        // Enterprise user schema uri.
        public static final String EXTENSION_SCHEMA_URI =
                "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

        // Sub tests constants.
        public static final String ACTUAL = "Actual : ";
        public static final String EXPECTED = "Expected : ";
        public static final String MESSAGE = "Message : ";
        public static final String STATUS_SUCCESS = "Status : Success";
        public static final String STATUS_FAILED = "Status : FAILED";
        public static final String STATUS_SKIPPED = "Status : Skipped";
        public static final String STATUS_REDIRECT = "Status : Permanent Redirect";

        // Status codes.
        public static final int SC_REDIRECT = 308;
    }

    /**
     * This method contains the defined constant used in scim resource endpoints.
     */
    public static class EndPointConstants {

        public static final String SERVICEPROVIDERCONFIG = "ServiceProviderConfig";
        public static final String RESOURCETYPE = "ResourceType";
        public static final String SCHEMAS = "Schemas";
        public static final String USER = "User";
        public static final String GROUP = "Group";
        public static final String ME = "Me";
        public static final String BULK = "Bulk";
        public static final String ROLE = "Role";
    }

    /**
     * This method contains the defined constants used in tests.
     */
    public static class DefinedInstances {

        public static String defineUserPassword = "7019asd84";
        public static String defineUserName = "loginUser";

        public static String defineUser =
                "{\"name\":{\"givenName\":\"Kim\",\"familyName\":\"Berry\"},\"password\": \"7019asd84\",\"userName\":" +
                        " \"loginUser\",\"emails\":[{" +
                        "\"value\": \"kim@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"kim@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"1234A\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedUser1 =
                "{\"name\":{\"givenName\":\"Samindra\",\"familyName\":\"Perera\"},\"password\": \"7019asd84\"," +
                        "\"userName\": \"loginUser1\",\"emails\":[{" +
                        "\"value\": \"Samindra@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"Samindra@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"12345\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedUser2 =
                "{\"name\":{\"givenName\":\"Danny\",\"familyName\":\"Gomez\"},\"password\": \"7019asd84\"," +
                        "\"userName\":" +
                        " \"loginUser2\",\"emails\":[{" +
                        "\"value\": \"danny@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"danny@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"243\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedUser3 =
                "{\"name\":{\"givenName\":\"Jason\",\"familyName\":\"Diesel\"},\"password\": \"7019asd84\"," +
                        "\"userName\":" +
                        " \"loginUser3\",\"emails\":[{" +
                        "\"value\": \"json@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"json@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"534\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedUser4 =
                "{\"name\":{\"givenName\":\"Tom\",\"familyName\":\"Hardy\"},\"password\": \"7019asd84\",\"userName\":" +
                        " \"loginUser4\",\"emails\":[{" +
                        "\"value\": \"tom@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"tom@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"343\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedUser5 =
                "{\"name\":{\"givenName\":\"Taylor\",\"familyName\":\"Swift\"},\"password\": \"7019asd84\"," +
                        "\"userName\":" +
                        " \"loginUser5\",\"emails\":[{" +
                        "\"value\": \"taylorn@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"taylor@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"536\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedWithoutUserNameUser =
                "{\"name\":{\"givenName\":\"Samindra\",\"familyName\":\"Perera\"},\"password\": \"7019asd84\"," +
                        "\"emails\":[{" +
                        "\"value\": \"Samindra@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"Samindra@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"12345A\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedUpdatedUser1 =
                "{\"name\":{\"givenName\":\"Kims\",\"familyName\":\"Berry\"},\"password\": \"7019asd85\"," +
                        "\"userName\":" +
                        " \"loginUser\",\"emails\":[{" +
                        "\"value\": \"kim@example.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"kim@jensen.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"1234A\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedUpdatedUser2 =
                "{\"name\":{\"givenName\":\"Kimi\",\"familyName\":\"Berry\"},\"password\": \"7019asd85\"," +
                        "\"userName\":" +
                        " \"loginUserUpdated\",\"emails\":[{" +
                        "\"value\": \"kimi@wso2.com\",\"type\": \"work\",\"primary\": true },{" +
                        "\"value\": \"kim@example.org\",\"type\": \"home\"}], " +
                        "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": {\"employeeNumber\": " +
                        "\"1234A\",\"manager\": {\"value\": \"Taylor\"}}}";

        public static String definedPatchUserPayload1 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"]," +
                        "\"Operations\":[{\"op\":\"add\",\"value\":{\"nickName\":\"shaggy\"}}]}";

        public static String definedPatchUserPayload2 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],\"Operations\":[{\"op\":\"remove\"," +
                        "\"path\":\"emails[type eq \\\"work\\\" and value ew \\\"example.com\\\"]\"}]}";

        public static String definedPatchUserPayload3 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"]," +
                        "\"Operations\":[{\"op\":\"replace\",\"path\":\"emails[type eq \\\"home\\\"]\"," +
                        "\"value\":{\"type\":\"home\",\"value\":\"home@example.com\"}}]}";

        public static String definedPatchUserPayload4 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],\"Operations\":[{\"op\":\"add\"," +
                        "\"value\":{\"nickName\":\"shaggy\"}},{\"op\":\"remove\",\"path\":\"emails[type eq " +
                        "\\\"work\\\" and value ew \\\"example.com\\\"\"},{\"op\":\"replace\",\"path\":\"emails[type " +
                        "eq \\\"home\\\"]\",\"value\":{\"type\":\"home\",\"value\":\"anjana@anjana.com\"}}]}";

        public static String definedPatchUserPayload5 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"]," +
                        "\"Operations\":[{\"op\":\"remove\",\"value\":{\"nickName\":\"shaggy\"}}]}";

        public static String definedPatchUserPayload6 = "{\n" +
                "  \"schemas\": [\n" +
                "    \"urn:ietf:params:scim:api:messages:2.0:PatchOp\"\n" +
                "  ],\n" +
                "  \"Operations\": [\n" +
                "    {\n" +
                "      \"op\": \"add\",\n" +
                "        \"path\":\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager\",\n" +
                "      \"value\": \n" +
                "        {\n" +
                "          \"value\": \"Civil\"\n" +
                "        }\n" +
                "      \n" +
                "    },\n" +
                "      {\"op\":\"replace\",\"path\":\"urn:ietf:params:scim:schemas:extension:enterprise:2" +
                ".0:User:manager\",\"value\": \n" +
                "        {\n" +
                "          \"value\": \"Civil-sub\"\n" +
                "        }},\n" +
                "    {\"op\":\"remove\",\"path\":\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:" +
                "User:manager\"}\n" +
                "  \n" +
                "  ]\n" +
                "}";

        public static String definedSearchUsersPayload1 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"],\"attributes\":[\"name" +
                        ".familyName\",\"userName\"],\"filter\":\"userName sw \\\"login\\\"\"," +
                        "\"startIndex\":1,\"count\":10}";

        public static String definedSearchUsersPayload2 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"],\"attributes\":[\"name" +
                        ".familyName\",\"userName\"],\"filter\":\"userName ssw \\\"login\\\"\"," +
                        "\"startIndex\":1,\"count\":10}";

        public static String definedSearchUsersPayload3 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"],\"attributes\":[\"name" +
                        ".familyName\",\"userName\"],\"filter\":\"userName sw \\\"login\\\"\"}";

        public static String definedSearchUsersPayload4 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"],\"attributes\":[\"name" +
                        ".familyName\",\"userName\"],\"filter\":\"userName sw \\\"login\\\"\"," +
                        "\"startIndex\":1}";

        public static String getDefinedSearchGroupsPayload1 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"],\"startIndex\":1," +
                        "\"count\":10,\"filter\":\"displayName eq \\\"XwLtOP23\\\"\"}";

        public static String getDefinedSearchGroupsPayload2 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"],\"startIndex\":1," +
                        "\"count\":10,\"filter\":\"displayName esq \\\"XwLtOP23\\\"\"}";

        public static String getDefinedSearchGroupsPayload3 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"]," +
                        "\"filter\":\"displayName eq \\\"XwLtOP23\\\"\"}";

        public static String getDefinedSearchGroupsPayload4 =
                "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:SearchRequest\"],\"startIndex\":1," +
                        "\"filter\":\"displayName eq \\\"XwLtOP23\\\"\"}";

        public static String defineBulkRequest1 = "{\"failOnErrors\":1,\"schemas\":" +
                "[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\"Operations\":" +
                "[{\"method\": \"POST\",\"path\": \"/Users\",\"bulkId\": \"qwerty\",\"data\":" +
                "{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"],\"userName\": " +
                "\"loginUser6\",\"password\":\"pw12435\"}},{\"method\": \"POST\",\"path\": \"/Users\"," +
                "\"bulkId\":\"ytrewq\",\"data\":{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"," +
                "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\"],\"userName\":\"loginUser7\"," +
                "\"password\":\"pw13424\"}}]}";

        public static String defineBulkRequest2 = " {\n" +
                "    \"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "    \"Operations\": [\n" +
                "       {\n" +
                "            \"method\": \"POST\",\n" +
                "            \"path\": \"/Groups\",\n" +
                "            \"bulkId\": \"ytrewq\",\n" +
                "            \"data\": {\n" +
                "                \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"],\n" +
                "                \"displayName\": \"Abx2312\"\n" +
                "            }\n" +
                "        },\n" +
                "               {\n" +
                "            \"method\": \"POST\",\n" +
                "            \"path\": \"/Groups\",\n" +
                "            \"bulkId\": \"ytrewq\",\n" +
                "            \"data\": {\n" +
                "                \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"],\n" +
                "                \"displayName\": \"Cdx2142\"\n" +
                "            }\n" +
                "        }\n" +
                "     ]\n" +
                "}";

        public static String defineBulkRequest3 = "{\"failOnErrors\":1,\"schemas\":" +
                "[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\"Operations\":" +
                "[{\"method\": \"POST\",\"path\": \"/Users\",\"data\":" +
                "{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"],\"userName\": " +
                "\"loginUser8\",\"password\":\"pw12435\"}},{\"method\": \"POST\",\"path\": \"/Users\",\"data\":" +
                "{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"," +
                "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\"],\"userName\":\"loginUser9\"," +
                "\"password\":\"pw13424\"}}]}";

        public static String defineBulkRequest4 = "{\"failOnErrors\":1,\"schemas\":" +
                "[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\"Operations\":" +
                "[{\"method\": \"POST\",\"bulkId\": \"qwerty\",\"data\":" +
                "{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"],\"userName\": " +
                "\"loginUser10\",\"password\":\"pw12435\"}},{\"method\": \"POST\"," +
                "\"bulkId\":\"ytrewq\",\"data\":{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"," +
                "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\"],\"userName\":\"loginUser11\"," +
                "\"password\":\"pw13424\"}}]}";

        public static String defineBulkRequest5 = "{\"failOnErrors\":1,\"schemas\":" +
                "[\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\"Operations\":" +
                "[{\"method\": \"POST\",\"path\": \"/Users\",\"bulkId\": \"qwerty\"},{\"method\": \"POST\",\"path\":" +
                " \"/Users\"," +
                "\"bulkId\":\"ytrewq\"}]}";

        public static String defineBulkRequest6 = "   {\n" +
                "     \"schemas\": [\"urn:ietf:params:scim:api:messages:2.0:BulkRequest\"],\n" +
                "     \"Operations\": [\n" +
                "       {\n" +
                "         \"method\": \"POST\",\n" +
                "         \"path\": \"/Users\",\n" +
                "         \"bulkId\": \"qwerty\",\n" +
                "         \"data\": {\n" +
                "           \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:User\"],\n" +
                "           \"userName\": \"loginUser14\"\n" +
                "         }\n" +
                "       },\n" +
                "       {\n" +
                "         \"method\": \"POST\",\n" +
                "         \"path\": \"/Groups\",\n" +
                "         \"bulkId\": \"ytrewq\",\n" +
                "         \"data\": {\n" +
                "           \"schemas\": [\"urn:ietf:params:scim:schemas:core:2.0:Group\"],\n" +
                "           \"displayName\": \"Xsdw21324\",\n" +
                "           \"members\": [\n" +
                "             {\n" +
                "               \"type\": \"User\",\n" +
                "               \"value\": \"bulkId:qwerty\"\n" +
                "             }\n" +
                "           ]\n" +
                "         }\n" +
                "       }\n" +
                "     ]\n" +
                "   }";

        public static String defineBulkRequest7 = "{\"failOnErrors\":1,\"schemas\":[\"urn:ietf:params:scim:api" +
                ":messages:2.0:BulkRequest\"],\"Operations\":[{\"method\": \"POST\",\"path\": \"/Users\",\"bulkId\": " +
                "\"qwerty\",\"data\":{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"],\"userName\": " +
                "\"loginUser21\",\"password\":\"pw12435\"}}, {\"method\": \"POST\",\"path\": \"/Users\"," +
                "\"bulkId\":\"ytrewssq\",\"data\":{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"]," +
                "\"userName\": \"loginUser1\",\"password\":\"pw12435\"}},{\"method\": \"POST\",\"path\": \"/Users\"," +
                "\"bulkId\":\"ytrewSq\",\"data\":{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"," +
                "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\"],\"userName\":\"loginUser21\"," +
                "\"password\":\"pw1323424\"}}]}";

    }
}
