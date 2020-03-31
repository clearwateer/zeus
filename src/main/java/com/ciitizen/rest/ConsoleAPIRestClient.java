package com.ciitizen.rest;

import com.ciitizen.zeus.common.ConfigHelper;
import com.ciitizen.zeus.enums.ConfigProperty;
import com.ciitizen.zeus.enums.RuntimeProperty;
import com.ciitizen.zeus.enums.ServiceAPIs;
import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ConsoleAPIRestClient {
    private static final Logger logger = LogManager.getLogger(ConsoleAPIRestClient.class);

    public static Map<String, String> login() {
        String adminUsr = ConfigHelper.getConfigPropValue(ConfigProperty.USER_NAME.getName());
        String adminPwd = ConfigHelper.getConfigPropValue(ConfigProperty.PASSWORD.getName());
        return login(adminUsr, adminPwd, true);
    }

    public static Map<String, String> login(String username, String password, boolean setCookies) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());
        String resource = hostUrl + ServiceAPIs.CONSOLE_LOGIN;
        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()

                .accept(ContentType.JSON)

                .formParam("login", username)
                .formParam("password", password)
//                    .log().all()
                .when()
                .post(resource);

        Assert.assertEquals(resp.getStatusCode(), 200);
        Map<String, String> cookies = resp.getCookies();
        if (setCookies) ConfigHelper.setRuntimeValue(RuntimeProperty.HOST_COOKIES.getName(), cookies);
        return cookies;
    }

    public static Response sendGetRequest(String resource, Map<String, String> cookies) {
        return sendGetRequest(resource, cookies, ContentType.JSON);

    }

    public static Response sendGetRequest(String resource, Map<String, String> cookies, ContentType accept) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .config(RestAssured.config().redirect(RedirectConfig.redirectConfig().redirectConfig().followRedirects(false)))

                .accept(accept)

                .cookies(cookies)
                .when()
                .get(resource);
        Assert.assertEquals(resp.getStatusCode(), 200);
        return resp;
    }

    public static Response sendGetRequest(String resource, Map<String, String> cookies, Map<String, String> params) {
        return sendGetRequest(resource, cookies, null, params);
    }

    public static Response sendGetRequest(String resource, Map<String, String> cookies, Map<String, String> headers, Map<String, String> params) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());
        String referer = hostUrl + "/index_uc.html";

        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Referer", referer);

        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType("application/x-www-form-urlencoded")
                .queryParams(params)
                .accept(ContentType.JSON)
                .headers(headers)
                .cookies(cookies)
                .when()
                .get(resource);
        Assert.assertEquals(resp.getStatusCode(), 200);
        return resp;
    }

    public static Response sendGetRequest(String resource, Map<String, String> cookies, ContentType accept, int statusCode) {

        return sendGetRequest(resource, cookies, null, accept, statusCode);
    }

    public static Response sendGetRequest(String resource, Map<String, String> cookies, Map<String, String> headers, ContentType accept, int statusCode) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());
        String referer = hostUrl + "/index_uc.html";

        if (headers == null) {
            headers = new HashMap<>();

        }

        headers.put("Referer", referer);

        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType("application/x-www-form-urlencoded")
                .accept(accept)
                .headers(headers)
                .cookies(cookies)
                .when()
                .get(resource);
        Assert.assertEquals(resp.getStatusCode(), statusCode);
        return resp;
    }

    public static Response sendPostRequest(String resource, Map<String, String> cookies, String payload) {
        return sendPostRequest(resource, cookies, null, "application/x-www-form-urlencoded", payload);
    }

    public static Response sendPostRequest(String resource, String payload) {

        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(payload).log().all()
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendPostRequest(String resource, Map<String, String> cookies, Map<String, String> headers, String contentType, String payload) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        if (headers == null) {
            headers = new HashMap<>();
        }


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType(contentType)
                .accept(ContentType.JSON)
                .headers(headers)
                .cookies(cookies)
                .body(payload).log().all()
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendPutRequest(String resource, Map<String, String> cookies, String payload) {
        return sendPutRequest(resource, cookies, null, ContentType.URLENC, payload);
    }

    public static Response sendPutRequest(String resource, Map<String, String> cookies, Map<String, String> headers, ContentType contentType, String payload) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        if (headers == null) {
            headers = new HashMap<>();
        }


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType(contentType)
                .accept(ContentType.JSON)
                .headers(headers)
                .cookies(cookies)
                .body(payload).log().all()
                .when()
                .put(resource);
        return resp;
    }

    public static Response sendPostRequestApp(String resource, Map<String, String> cookies, File filePath, String filename) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .multiPart("uploadFile", filePath)
                .cookies(cookies)

                .formParam("filename", filename)
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendPostRequestApp(String resource, Map<String, String> cookies, String fileName, String platform) {

        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());
        String referer = hostUrl + "/index_uc.html";
        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .cookies(cookies)
                .header("Referer", referer).accept(ContentType.JSON).contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .queryParam("fileName", fileName).queryParam("platform", platform).queryParam("vppAccountId", "-1")
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendFileUploadPostRequest(String resource, Map<String, String> cookies, File file, Map<String, String> formParams) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType("multipart/form-data")
                .accept(ContentType.ANY)
                .cookies(cookies)
                .multiPart("uploadFile", file)
                .formParams(formParams)
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendPostRequestWithFormParams(String resource, Map<String, String> cookies, Map<String, String> formParams) {
        return sendPostRequestWithFormParams(resource, cookies, formParams, ContentType.ANY);
    }

    public static Response sendPostRequestWithFormParams(String resource, Map<String, String> cookies, Map<String, ?> formParams, ContentType contentType) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType("application/x-www-form-urlencoded")
                .accept(contentType)

                .cookies(cookies)
                .formParams(formParams)
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendPostRequest(String resource, Map<String, String> cookies, String contentTypeHeader, String payload) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType(contentTypeHeader)
                .accept(ContentType.JSON)
                .cookies(cookies)
                .body(payload).log().all()
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendPostRequest(String resource, Map<String, String> cookies, String contentTypeHeader, String payload, int expectedStatus) {
        Response response = sendPostRequest(resource, cookies, contentTypeHeader, payload);
        checkResponseCode(response, expectedStatus);
        return response;
    }

    public static Response sendDeleteRequest(String resource, Map<String, String> cookies) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());

        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType("application/json")
                .accept(ContentType.JSON)

                .cookies(cookies)
                .when()
                .delete(resource);
        return resp;
    }

    public static Response sendDeleteRequest(String resource, Map<String, String> cookies, Map<String, String> headers, String payload) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());

        RestAssured.useRelaxedHTTPSValidation();

        if (headers == null) {
            headers = new HashMap<>();
        }


        Response resp = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .headers(headers)
                .cookies(cookies)
                .body(payload)
                .when()
                .delete(resource);
        return resp;
    }

    public static Response sendDeleteRequest(String resource, Map<String, String> cookies, String payload) {
        return sendDeleteRequest(resource, cookies, null, payload);
    }

    public static Response sendDeleteRequest(String resource, Map<String, String> cookies, String payload, int statusCode) {
        return sendDeleteRequest(resource, cookies, null, payload);
    }

    public static Response sendDeleteRequest(String resource, Map<String, String> cookies, Map<String, String> headers, String payload, int statusCode) {
        Response response = sendDeleteRequest(resource, cookies, headers, payload);
        checkResponseCode(response, statusCode);
        return response;
    }

    public static Response sendPutRequest(String resource, Map<String, String> cookies, String payload, int statusCode) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType("application/json")
                .accept(ContentType.JSON)

                .cookies(cookies)
                .body(payload).log().all()
                .when()
                .put(resource);
        checkResponseCode(resp, statusCode);
        return resp;
    }

    public static Response sendPutRequest(String resource, Map<String, String> cookies, String contentTypeHeader, String payload) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType(contentTypeHeader)
                .accept(ContentType.JSON)

                .cookies(cookies)
                .body(payload).log().all()
                .when()
                .put(resource);
        return resp;
    }

    public static Response fileUpload(String endpoint, Map<String, String> cookies, String filePath) {
        String referer = getReferer();
        Response resp = given()
                .multiPart("uploadFile", new File(filePath))
                .header("Referer", referer)
                .cookies(cookies)
                .when()
                .post(endpoint);

        return resp;
    }


    public static void checkResponseCode(Response resp, int statusCode) {
        Assert.assertNotNull(resp);
        Assert.assertEquals(resp.getStatusCode(), statusCode, resp.getBody() != null ? resp.getBody().asString() : "");
    }

    private static String getReferer() {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());
        return hostUrl;
    }

    public static void main(String args[]) {
        try {
            Map<String, String> cookies = login();
            logger.info("token : " + cookies.keySet().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Response sendGetRequestWithNoAssert(String resource, Map<String, String> cookies) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType("application/x-www-form-urlencoded")
                .accept(ContentType.JSON)

                .cookies(cookies)
                .when()
                .get(resource);
        return resp;
    }

    public static Response sendPostRequestWithNoAssert(String resource, Map<String, String> cookies, ContentType contentType, String payload) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());

        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType(contentType)
                .accept(ContentType.JSON)

                .cookies(cookies)
                .body(payload).log().all()
                .when()
                .post(resource);
        return resp;
    }

    public static Response sendPutRequestWithNoAssert(String resource, Map<String, String> cookies, ContentType contentType, String payload) {
        String hostUrl = ConfigHelper.getConfigPropValue(ConfigProperty.HOSTURL.getName());


        RestAssured.useRelaxedHTTPSValidation();
        Response resp = given()
                .contentType(contentType)
                .accept(ContentType.JSON)

                .cookies(cookies)
                .body(payload).log().all()
                .when()
                .put(resource);
        return resp;
    }


}
