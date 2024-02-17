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
public class UserResource extends BaseResource {
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

        authenticate();
        checkBaseFunction(BaseFunction.ADMIN);

        return AddUserResourceHelper.register(username, password, email);
    }

    /**
     * Updates user informations.
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

        authenticate();

        return UpdateUserResourceHelper.update(password, email, themeId, localeId, firstConnection,
                hasBaseFunction(BaseFunction.ADMIN), principal);
    }

    /**
     * Updates user informations.
     * 
     * @param username Username
     * @param password Password
     * @param email    E-Mail
     * @param themeId  Theme
     * @param localeId Locale ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{username: [a-zA-Z0-9_]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email,
            @FormParam("theme") String themeId,
            @FormParam("locale") String localeId) throws JSONException {

        authenticate();
        checkBaseFunction(BaseFunction.ADMIN);

        return UpdateUserResourceHelper.update(username, password, email, themeId, localeId);
    }

    /**
     * Checks if a username is available. Search only on active accounts.
     * 
     * @param username Username to check
     * @return Response
     */
    @GET
    @Path("check_username")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkUsername(
            @QueryParam("username") String username) throws JSONException {

        return GetResourceHelper.checkUsername(username);
    }

    /**
     * This resource is used to authenticate the user and create a user session.
     * The "session" is only used to identify the user, no other data is stored in
     * the session.
     * 
     * @param username   Username
     * @param password   Password
     * @param longLasted Remember the user next time, create a long lasted session.
     * @return Response
     */
    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("remember") boolean longLasted) throws JSONException {

        return SessionManager.login(username, password, longLasted);
    }

    /**
     * Logs out the user and deletes the active session.
     * 
     * @return Response
     */
    @POST
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() throws JSONException {
        authenticate();
        return SessionManager.logout(request);
    }

    /**
     * Delete a user.
     * 
     * @return Response
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete() throws JSONException {
        authenticate();
        return DeleteUserResourceHelper.delete(hasBaseFunction(BaseFunction.ADMIN), principal);
    }

    /**
     * Deletes a user.
     * 
     * @param username Username
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{username: [a-zA-Z0-9_]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("username") String username) throws JSONException {
        authenticate();
        checkBaseFunction(BaseFunction.ADMIN);

        return DeleteUserResourceHelper.delete(username);
    }

    /**
     * Returns the information about the connected user.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response info() throws JSONException {
        return GetResourceHelper.info(authenticateCheck(), hasBaseFunction(BaseFunction.ADMIN), principal);
    }

    /**
     * Returns the information about a user.
     * 
     * @param username Username
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{username: [a-zA-Z0-9_]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("username") String username) throws JSONException {
        authenticate();
        checkBaseFunction(BaseFunction.ADMIN);

        return GetResourceHelper.view(username);
    }

    /**
     * Returns all active users.
     * 
     * @param limit      Page limit
     * @param offset     Page offset
     * @param sortColumn Sort index
     * @param asc        If true, ascending sorting, else descending
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) throws JSONException {
        authenticate();
        checkBaseFunction(BaseFunction.ADMIN);

        return GetResourceHelper.list(limit, offset, sortColumn, asc);
    }

    /**
     * Returns all active sessions.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response session() throws JSONException {
        authenticate();
        return SessionManager.session(request, principal);
    }

    /**
     * Deletes all active sessions except the one used for this request.
     * 
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSession() throws JSONException {
        authenticate();

        return SessionManager.deleteSession(request, principal);
    }
}
