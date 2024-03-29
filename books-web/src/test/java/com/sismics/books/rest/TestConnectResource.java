package com.sismics.books.rest;

import java.util.ResourceBundle;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultWebRequestor;
import com.restfb.FacebookClient;
import com.restfb.WebRequestor;
import com.restfb.WebRequestor.Response;
import com.sismics.books.core.util.ConfigUtil;
import com.sismics.books.rest.filter.CookieAuthenticationFilter;
import com.sismics.books.rest.model.FacebookUser;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Test the connect resource.
 * 
 * @author jtremeaux
 */
public class TestConnectResource {


        BaseJerseyTest baseJerseyTest = new BaseJerseyTest() {};
    /**
     * Test of connected application list.
     * 
     * @throws JSONException
     */
    @Test
    public void testListApp() throws JSONException {
        // Create and connect user connect_list
        baseJerseyTest.clientUtil.createUser("connect_list");
        String connectListAuthToken = baseJerseyTest.clientUtil.login("connect_list");

        // List applications
        WebResource listResource = baseJerseyTest.resource().path("/connect/list");
        listResource.addFilter(new CookieAuthenticationFilter(connectListAuthToken));
        ClientResponse response = listResource.get(ClientResponse.class);
        response = listResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray apps = json.getJSONArray("apps");
        Assert.assertTrue(apps.length() > 0);
    }

    /**
     * Test of Facebook connection.
     * 
     * @throws JSONException
     */
    @Test
    public void testFacebook() throws Exception {
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String facebookAppId = configBundle.getString("app_key.facebook.id");
        String facebookAppSecret = configBundle.getString("app_key.facebook.secret");
        FacebookClient facebookClient = new DefaultFacebookClient();
        String appAccessToken = facebookClient.obtainAppAccessToken(facebookAppId, facebookAppSecret)
                                .getAccessToken();
        WebRequestor webRequestor = new DefaultWebRequestor();

        // Create Carol
        Response fbResponse = webRequestor.executePost(
                "https://graph.facebook.com/" + facebookAppId + "/accounts/test-users",
                "installed=true&permissions=email,publish_stream,read_stream&name=Carol Oneill&method=post&access_token="
                        + appAccessToken);
        JSONObject testUser = new JSONObject(fbResponse.getBody());
        FacebookUser carol = new FacebookUser(testUser.getString("id"), testUser.getString("email"),
                                "Carol Oneill",
                testUser.getString("access_token"));

        // Create Charlie
        fbResponse = webRequestor.executePost(
                                "https://graph.facebook.com/" + facebookAppId + "/accounts/test-users",
                "installed=true&permissions=email,publish_stream,read_stream&name=Charlie Dylan&method=post&access_token="
                        + appAccessToken);
        testUser = new JSONObject(fbResponse.getBody());
        FacebookUser charlie = new FacebookUser(testUser.getString("id"), testUser.getString("email"),
                                "Charlie Dylan",
                testUser.getString("access_token"));

        // Carol is friend with Charlie
        fbResponse = webRequestor.executePost(
                                "https://graph.facebook.com/" + carol.getId() + "/friends/" + charlie.getId(),
                "method=post&access_token=" + carol.getAccessToken());
        fbResponse = webRequestor.executePost(
                                "https://graph.facebook.com/" + charlie.getId() + "/friends/" + carol.getId(),
                "method=post&access_token=" + charlie.getAccessToken());

        // Create and connect user carol_fb
        baseJerseyTest.clientUtil.createUser("carol_fb");
        String carolFbAuthToken = baseJerseyTest.clientUtil.login("carol_fb");

        // Add Myspace application : KO (application unknown)
        WebResource connectResource = baseJerseyTest.resource().path("/connect/myspace/add");
        connectResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.putSingle("access_token", "123456789");
        ClientResponse response = connectResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals("AppNotFound", json.getString("type"));

        // Carol lists its applications : Facebook application not connected
        WebResource listResource = baseJerseyTest.resource().path("/connect/list");
        listResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        response = listResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray apps = json.getJSONArray("apps");
        Assert.assertEquals(1, apps.length());
        JSONObject app = (JSONObject) apps.get(0);
        Assert.assertEquals("FACEBOOK", app.optString("id"));
        Assert.assertFalse(app.optBoolean("connected"));
        Assert.assertFalse(app.optBoolean("sharing"));

        // Carol add Facebook application
        connectResource = baseJerseyTest.resource().path("/connect/facebook/add");
        connectResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("access_token", carol.getAccessToken());
        response = connectResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Carol lists its application : Facebook application connected
        listResource = baseJerseyTest.resource().path("/connect/list");
        listResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        response = listResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        apps = json.getJSONArray("apps");
        Assert.assertEquals(1, apps.length());
        app = (JSONObject) apps.get(0);
        Assert.assertEquals("FACEBOOK", app.optString("id"));
        Assert.assertTrue(app.optBoolean("connected"));
        Assert.assertTrue(app.optBoolean("sharing"));

        // Carol lists its Facebook contacts
        WebResource contactListResource = baseJerseyTest.resource().path("/connect/facebook/contact/list");
        contactListResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        response = contactListResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray contacts = json.getJSONArray("contacts");
        Assert.assertTrue(contacts.length() >= 1);
        JSONObject contact = (JSONObject) contacts.get(0);
        Assert.assertEquals(charlie.getId(), contact.optString("external_id"));
        Assert.assertEquals(charlie.getFullName(), contact.optString("full_name"));

        // Carol searches FB contacts : nothing
        contactListResource = baseJerseyTest.resource().path("/connect/facebook/contact/list");
        contactListResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("query", "not_my_friend");
        response = contactListResource.queryParams(getParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        contacts = json.getJSONArray("contacts");
        Assert.assertEquals(0, contacts.length());

        // Carol searches FB contacts
        contactListResource = baseJerseyTest.resource().path("/connect/facebook/contact/list");
        contactListResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("query", charlie.getFullName());
        response = contactListResource.queryParams(getParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        contacts = json.getJSONArray("contacts");
        Assert.assertEquals(1, contacts.length());

        // Carol disable Facebook sharing
        connectResource = baseJerseyTest.resource().path("/connect/facebook/update");
        connectResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        postParams = new MultivaluedMapImpl();
        response = connectResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Carol lists its application : sharing is disabled
        listResource = baseJerseyTest.resource().path("/connect/list");
        listResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        response = listResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        apps = json.getJSONArray("apps");
        Assert.assertEquals(1, apps.length());
        app = (JSONObject) apps.get(0);
        Assert.assertEquals("FACEBOOK", app.optString("id"));
        Assert.assertTrue(app.optBoolean("connected"));
        Assert.assertFalse(app.optBoolean("sharing"));

        // Carol deletes Facebook connection
        connectResource = baseJerseyTest.resource().path("/connect/facebook/remove");
        connectResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        postParams = new MultivaluedMapImpl();
        response = connectResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Carol lists its application : Facebook application disconnected
        listResource = baseJerseyTest.resource().path("/connect/list");
        listResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        response = listResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        apps = json.getJSONArray("apps");
        Assert.assertEquals(1, apps.length());
        app = (JSONObject) apps.get(0);
        Assert.assertEquals("FACEBOOK", app.optString("id"));
        Assert.assertFalse(app.optBoolean("connected"));
        Assert.assertFalse(app.optBoolean("sharing"));

        // Carol searches in its Facebook contacts : KO (not connected)
        contactListResource = baseJerseyTest.resource().path("/connect/facebook/contact/list");
        contactListResource.addFilter(new CookieAuthenticationFilter(carolFbAuthToken));
        response = contactListResource.get(ClientResponse.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("AppNotConnected", json.getString("type"));

        // Delete Facebook test users
        fbResponse = webRequestor.executePost("https://graph.facebook.com/" + carol.getId(),
                "method=delete&access_token=" + appAccessToken);
        fbResponse = webRequestor.executePost("https://graph.facebook.com/" + charlie.getId(),
                "method=delete&access_token=" + appAccessToken);
    }
}