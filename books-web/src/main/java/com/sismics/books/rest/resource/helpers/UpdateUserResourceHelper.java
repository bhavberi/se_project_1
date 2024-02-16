package com.sismics.books.rest.resource.helpers;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.model.jpa.User;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.IPrincipal;

public class UpdateUserResourceHelper {
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

    public static Response update(String password, String email, String themeId, String localeId,
            Boolean firstConnection, Boolean hasbasefunction, IPrincipal principal) throws JSONException {

        // Validate the input data
        password = ValidationUtil.validatePassword(password, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        localeId = ValidationUtil.validateLocale(localeId, "locale", true);
        themeId = ValidationUtil.validateTheme(themeId, "theme", true);

        // Update the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());
        if (email != null) {
            user.setEmail(email);
        }
        if (themeId != null) {
            user.setTheme(themeId);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }
        if (firstConnection != null && hasbasefunction) {
            user.setFirstConnection(firstConnection);
        }

        user = userDao.update(user);

        if (StringUtils.isNotBlank(password)) {
            user.setPassword(password);
            userDao.updatePassword(user);
        }

        // Always return "ok"
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
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
    public static Response update(
            String username,
            String password,
            String email, String themeId,
            String localeId) throws JSONException {

        // Validate the input data
        password = ValidationUtil.validatePassword(password, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        localeId = ValidationUtil.validateLocale(localeId, "locale", true);
        themeId = ValidationUtil.validateTheme(themeId, "theme", true);

        // Check if the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }

        // Update the user
        if (email != null) {
            user.setEmail(email);
        }
        if (themeId != null) {
            user.setTheme(themeId);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }

        user = userDao.update(user);

        if (StringUtils.isNotBlank(password)) {
            // Change the password
            user.setPassword(password);
            userDao.updatePassword(user);
        }

        // Always return "ok"
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    
}
