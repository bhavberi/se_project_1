package com.sismics.books.rest.resource.helpers;

import java.util.Date;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.constant.Constants;
import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.model.jpa.User;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.AlphanumericValidator;
import com.sismics.rest.util.EmailValidator;
import com.sismics.rest.util.IValidator;
import com.sismics.rest.util.ValidationUtil;

public class AddUserResourceHelper {
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
    public static Response register(String username, String password, String email)
            throws JSONException {
        // Validate the input data
        IValidator alphanumericValidator = new AlphanumericValidator();
        IValidator emailValidator = new EmailValidator();
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        alphanumericValidator.validate(username, "username");
        password = ValidationUtil.validatePassword(password, false);
        email = ValidationUtil.validateLength(email, "email", 3, 50);
        emailValidator.validate(email, "email");

        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setCreateDate(new Date());
        user.setLocaleId(Constants.DEFAULT_LOCALE_ID);

        // Create the user
        UserDao userDao = new UserDao();
        try {
            userDao.create(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ServerException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown Server Error", e);
            }
        }

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

}
