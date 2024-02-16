package com.sismics.books.rest.resource.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.constant.Constants;
import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.dao.jpa.dto.UserDto;
import com.sismics.books.core.model.jpa.User;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.security.IPrincipal;
import com.sismics.security.UserPrincipal;

public class GetResourceHelper {
    /**
     * Checks if a username is available. Search only on active accounts.
     * 
     * @param username Username to check
     * @return Response
     */
    public static Response checkUsername(
            String username) throws JSONException {

        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);

        JSONObject response = new JSONObject();
        if (user != null) {
            response.put("status", "ko");
            response.put("message", "Username already registered");
        } else {
            response.put("status", "ok");
        }

        return Response.ok().entity(response).build();
    }

    /**
     * Returns the information about the connected user.
     * 
     * @return Response
     * @throws JSONException
     */

    public static Response info(Boolean authenticateheck, Boolean hasbasefunction, IPrincipal principal)
            throws JSONException {
        JSONObject response = new JSONObject();
        if (!authenticateheck) {
            response.put("anonymous", true);

            // Check if admin has the default password
            UserDao userDao = new UserDao();
            User adminUser = userDao.getById("admin");
            if (adminUser != null && adminUser.getDeleteDate() == null) {
                response.put("is_default_password", Constants.DEFAULT_ADMIN_PASSWORD.equals(adminUser.getPassword()));
            }
        } else {
            response.put("anonymous", false);
            UserDao userDao = new UserDao();
            User user = userDao.getById(principal.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("theme", user.getTheme());
            response.put("locale", user.getLocaleId());
            response.put("first_connection", user.isFirstConnection());
            JSONArray baseFunctions = new JSONArray(((UserPrincipal) principal).getBaseFunctionSet());
            response.put("base_functions", baseFunctions);
            response.put("is_default_password",
                    hasbasefunction && Constants.DEFAULT_ADMIN_PASSWORD.equals(user.getPassword()));
        }

        return Response.ok().entity(response).build();
    }

    /**
     * Returns the information about a user.
     * 
     * @param username Username
     * @return Response
     * @throws JSONException
     */
    public static Response view(String username) throws JSONException {

        JSONObject response = new JSONObject();

        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }

        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("theme", user.getTheme());
        response.put("locale", user.getLocaleId());

        return Response.ok().entity(response).build();
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
    public static Response list(
            Integer limit,
            Integer offset,
            Integer sortColumn,
            Boolean asc) throws JSONException {


        JSONObject response = new JSONObject();
        List<JSONObject> users = new ArrayList<>();

        PaginatedList<UserDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        UserDao userDao = new UserDao();
        userDao.findAll(paginatedList, sortCriteria);
        for (UserDto userDto : paginatedList.getResultList()) {
            JSONObject user = new JSONObject();
            user.put("id", userDto.getId());
            user.put("username", userDto.getUsername());
            user.put("email", userDto.getEmail());
            user.put("create_date", userDto.getCreateTimestamp());
            users.add(user);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("users", users);

        return Response.ok().entity(response).build();
    }

}
