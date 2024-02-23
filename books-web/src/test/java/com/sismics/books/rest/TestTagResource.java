package com.sismics.books.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.books.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Test the tag resource.
 * 
 * @author bgamard
 */
public class TestTagResource {

    BaseJerseyTest baseJerseyTest = new BaseJerseyTest() {
    };

    private void createTag(String tagName, String tagColor, String authToken) {
        WebResource tagResource = baseJerseyTest.resource().path("/tag");
        tagResource.addFilter(new CookieAuthenticationFilter(authToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("name", tagName);
        postParams.add("color", tagColor);
        ClientResponse response = tagResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
    }

    private String getTagId(String tagName, String authToken) throws JSONException {
        WebResource tagResource = baseJerseyTest.resource().path("/tag/list");
        tagResource.addFilter(new CookieAuthenticationFilter(authToken));
        ClientResponse response = tagResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray tags = json.getJSONArray("tags");
        for (int i = 0; i < tags.length(); i++) {
            JSONObject tag = tags.getJSONObject(i);
            if (tag.getString("name").equals(tagName)) {
                return tag.getString("id");
            }
        }
        return null;
    }

    private void updateTag(String tagId, String updatedName, String updatedColor, String authToken) {
        WebResource tagResource = baseJerseyTest.resource().path("/tag/" + tagId);
        tagResource.addFilter(new CookieAuthenticationFilter(authToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("name", updatedName);
        postParams.add("color", updatedColor);
        ClientResponse response = tagResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
    }

    private void deleteTag(String tagId, String authToken) {
        WebResource tagResource = baseJerseyTest.resource().path("/tag/" + tagId);
        tagResource.addFilter(new CookieAuthenticationFilter(authToken));
        ClientResponse response = tagResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
    }

    /**
     * Test the tag resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testTagResource() throws JSONException {
        // Login tag1
        baseJerseyTest.clientUtil.createUser("tag1");
        String tag1Token = baseJerseyTest.clientUtil.login("tag1");

        // Create tags
        createTag("Tag3", "#ff0000", tag1Token);
        createTag("Tag4", "#00ff00", tag1Token);

        // Create a tag with space (not allowed)
        WebResource tagResource = baseJerseyTest.resource().path("/tag");
        tagResource.addFilter(new CookieAuthenticationFilter(tag1Token));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("name", "Tag 4");
        ClientResponse response = tagResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));

        // Create books
        String tag3Id = getTagId("Tag3", tag1Token);
        String tag4Id = getTagId("Tag4", tag1Token);
        WebResource bookResource = baseJerseyTest.resource().path("/book");
        bookResource.addFilter(new CookieAuthenticationFilter(tag1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("isbn", "9781468304930");
        postParams.add("tags", tag3Id);
        response = bookResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        postParams = new MultivaluedMapImpl();
        postParams.add("isbn", "0553293400");
        postParams.add("tags", tag4Id);
        response = bookResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Update a tag
        updateTag(tag4Id, "UpdatedName", "#0000ff", tag1Token);

        // Get all tags
        String updatedTagId = getTagId("UpdatedName", tag1Token);
        Assert.assertNotNull(updatedTagId);

        // Deletes a tag
        deleteTag(updatedTagId, tag1Token);

        // Get all tags
        WebResource tagListResource = baseJerseyTest.resource().path("/tag/list");
        tagListResource.addFilter(new CookieAuthenticationFilter(tag1Token));
        response = tagListResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray tags = json.getJSONArray("tags");
        Assert.assertEquals(1, tags.length());
    }
}