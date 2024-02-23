package com.sismics.books.rest.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;

import com.sismics.books.rest.constant.BaseFunction;
import com.sismics.books.rest.resource.helpers.AddUserResourceHelper;
import com.sismics.books.rest.resource.helpers.DeleteUserResourceHelper;
import com.sismics.books.rest.resource.helpers.GetResourceHelper;
import com.sismics.books.rest.resource.helpers.UpdateUserResourceHelper;

/**
 * User REST resources.
 * 
 * @author jtremeaux
 */
@Path("/user")
public class UserResource {
    /**
     * Creates a new user.
     * 
     * @param username User's username
     * @param password Password
     * @param email    E-Mail
     * @param localeId Locale ID
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("locale") String localeId,
            @FormParam("email") String email) throws JSONException {

        return AddUserResourceHelper.register(username, password, email);
    }

    /**
     * Updates user information.
     * 
     * @param password        Password
     * @param email           E-Mail
     * @param themeId         Theme
     * @param localeId        Locale ID
     * @param firstConnection True if the user hasn't acknowledged the first
     *                        connection wizard yet.
     * @return Response
     * @throws JSONException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @FormParam("password") String password,
            @FormParam("email") String email,
            @FormParam("theme") String themeId,
            @FormParam("locale") String localeId,
            @FormParam("first_connection") Boolean firstConnection) throws JSONException {

        return UpdateUserResourceHelper.update(password, email, themeId, localeId, firstConnection);
    }

    // Other methods remain unchanged
}