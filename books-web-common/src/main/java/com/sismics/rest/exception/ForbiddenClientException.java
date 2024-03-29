package com.sismics.rest.exception;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Unauthorized access to the resource exception.
 *
 * @author jtremeaux
 */
public class ForbiddenClientException extends WebApplicationException {
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of ForbiddenClientException.
     * 
     * @throws JSONException
     */
    public ForbiddenClientException() throws JSONException {
        super(Response.status(Status.FORBIDDEN).entity(new JSONObject()
                .put("type", "ForbiddenError")
                .put("message", "You don't have access to this resource")).build());
    }
}
