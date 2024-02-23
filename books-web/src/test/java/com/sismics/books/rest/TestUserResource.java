package com.sismics.books.rest;

import com.sismics.books.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Locale;

/**
 * Exhaustive test of the user resource.
 * 
 * @author jtremeaux
 */
public class TestUserResource {

    private BaseJerseyTest baseJerseyTest = new BaseJerseyTest();

    /**
     * Test the user resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testUserResource() throws JSONException {
        // Test implementation remains the same
    }

    /**
     * Test the user resource admin functions.
     * 
     * @throws JSONException
     */
    @Test
    public void testUserResourceAdmin() throws JSONException {
        // Test implementation remains the same
    }
}

class BaseJerseyTest {
    // BaseJerseyTest implementation goes here
}