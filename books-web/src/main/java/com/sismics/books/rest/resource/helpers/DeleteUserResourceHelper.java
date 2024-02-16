package com.sismics.books.rest.resource.helpers;

import java.util.Set;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.RoleBaseFunctionDao;
import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.model.jpa.User;
import com.sismics.books.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.security.IPrincipal;

public class DeleteUserResourceHelper {
    /**
     * Delete a user.
     * 
     * @return Response
     */
    public static Response delete(Boolean hasbasefunction,IPrincipal principal) throws JSONException {

        // Ensure that the admin user is not deleted
        if (hasbasefunction) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }

        // Delete the user
        UserDao userDao = new UserDao();
        userDao.delete(principal.getName());

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a user.
     * 
     * @param username Username
     * @return Response
     * @throws JSONException
     */

    public static Response delete(String username) throws JSONException {

        // Check if the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }

        // Ensure that the admin user is not deleted
        RoleBaseFunctionDao userBaseFuction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFuction.findByRoleId(user.getRoleId());
        if (baseFunctionSet.contains(BaseFunction.ADMIN.name())) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }

        // Delete the user
        userDao.delete(user.getUsername());

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
