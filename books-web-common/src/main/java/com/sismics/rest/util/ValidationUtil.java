package com.sismics.rest.util;

import com.google.common.base.Strings;
import com.sismics.books.core.dao.file.theme.ThemeDao;
import com.sismics.books.core.dao.jpa.LocaleDao;
import com.sismics.books.core.model.jpa.Locale;
import com.sismics.rest.exception.ClientException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * Utility class to validate parameters.
 *
 * @author jtremeaux
 */
public class ValidationUtil {
    // Constants
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 50;

    /**
     * Checks that the argument is not null.
     * 
     * @param s    Object tu validate
     * @param name Name of the parameter
     * @throws JSONException
     */
    public static void validateRequired(Object s, String name) throws JSONException {
        if (s == null) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
        }
    }

    /**
     * Validate a string length.
     * 
     * @param s         String to validate
     * @param name      Name of the parameter
     * @param lengthMin Minimum length (or null)
     * @param lengthMax Maximum length (or null)
     * @param nullable  True if the string can be empty or null
     * @return String without white spaces
     * @throws ClientException
     */
    public static String validateLength(String s, String name, Integer lengthMin, Integer lengthMax, boolean nullable)
            throws JSONException {
        s = StringUtils.strip(s);
        if (nullable && StringUtils.isEmpty(s)) {
            return s;
        }
        if (s == null) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
        }
        if (lengthMin != null && s.length() < lengthMin) {
            throw new ClientException("ValidationError",
                    MessageFormat.format("{0} must be more than {1} characters", name, lengthMin));
        }
        if (lengthMax != null && s.length() > lengthMax) {
            throw new ClientException("ValidationError",
                    MessageFormat.format("{0} must be less than {1} characters", name, lengthMax));
        }
        return s;
    }

    /**
     * Validate a string length. The string mustn't be empty.
     * 
     * @param s         String to validate
     * @param name      Name of the parameter
     * @param lengthMin Minimum length (or null)
     * @param lengthMax Maximum length (or null)
     * @return String without white spaces
     * @throws ClientException
     */
    public static String validateLength(String s, String name, Integer lengthMin, Integer lengthMax)
            throws JSONException {
        return validateLength(s, name, lengthMin, lengthMax, false);
    }

    /**
     * Validate a password.
     * 
     * @param s        String to validate
     * @param nullable True if the string can be empty or null
     * @return String without white spaces
     * @throws ClientException
     */
    public static String validatePassword(String s, boolean nullable) throws JSONException {
        return validateLength(s, "password", PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH, nullable);
    }

    /**
     * Checks if the string is not null and is not only whitespaces.
     * 
     * @param s    String to validate
     * @param name Name of the parameter
     * @return String without white spaces
     * @throws JSONException
     */
    public static String validateStringNotBlank(String s, String name) throws JSONException {
        return validateLength(s, name, 1, null, false);
    }

    /**
     * Checks if the string is a hexadecimal color.
     * 
     * @param s        String to validate
     * @param name     Name of the parameter
     * @param nullable True if the string can be empty or null
     * @throws JSONException
     */
    public static void validateHexColor(String s, String name, boolean nullable) throws JSONException {
        ValidationUtil.validateLength(s, name, 7, 7, nullable);
    }

    /**
     * Validates and parses a date.
     * 
     * @param s        String to validate
     * @param name     Name of the parameter
     * @param nullable True if the string can be empty or null
     * @return Parsed date
     * @throws JSONException
     */
    public static Date validateDate(String s, String name, boolean nullable) throws JSONException {
        if (Strings.isNullOrEmpty(s)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
            } else {
                return null;
            }
        }
        try {
            return new DateTime(Long.parseLong(s)).toDate();
        } catch (NumberFormatException e) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be a date", name));
        }
    }

    /**
     * Validates a locale.
     * 
     * @param localeId String to validate
     * @param name     Name of the parameter
     * @return String without white spaces
     * @param nullable True if the string can be empty or null
     * @throws ClientException
     */
    public static String validateLocale(String localeId, String name, boolean nullable) throws JSONException {
        localeId = StringUtils.strip(localeId);
        if (StringUtils.isEmpty(localeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} is required", name));
            } else {
                return null;
            }
        }
        LocaleDao localeDao = new LocaleDao();
        Locale locale = localeDao.getById(localeId);
        if (locale == null) {
            throw new ClientException("ValidationError", "Locale not found: " + localeId);
        }
        return localeId;
    }

    /**
     * Validates a theme.
     * 
     * @param themeId ID of the theme to validate
     * @param name    Name of the parameter
     * @return String without white spaces
     * @param nullable True if the string can be empty or null
     * @throws ClientException
     */
    public static String validateTheme(String themeId, String name, boolean nullable) throws JSONException {
        themeId = StringUtils.strip(themeId);
        if (StringUtils.isEmpty(themeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} is required", name));
            } else {
                return null;
            }
        }
        ThemeDao themeDao = new ThemeDao();
        List<String> themeList = themeDao.findAll();
        if (!themeList.contains(themeId)) {
            throw new ClientException("ValidationError", "Theme not found: " + themeId);
        }
        return themeId;
    }

    /**
     * Validates the input data for a book.
     * 
     * @param title          The title of the book
     * @param subtitle       The subtitle of the book
     * @param author         The author of the book
     * @param description    The description of the book
     * @param isbn10         The ISBN-10 of the book
     * @param isbn13         The ISBN-13 of the book
     * @param language       The language of the book
     * @param publishDateStr The publish date of the book as a string
     * @param update         True if the data is being updated, false if it's a new
     *                       book
     * @return The parsed publish date as a Date object
     * @throws JSONException If there is an error in the JSON data
     */
    public static Date validateInputData(String title, String subtitle, String author, String description,
            String isbn10, String isbn13, String language, String publishDateStr, Boolean update) throws JSONException {
        ValidationUtil.validateLength(title, "title", 1, 255, update);
        ValidationUtil.validateLength(subtitle, "subtitle", 1, 255, true);
        ValidationUtil.validateLength(author, "author", 1, 255, update);
        ValidationUtil.validateLength(description, "description", 1, 4000, true);
        ValidationUtil.validateLength(isbn10, "isbn10", 10, 10, true);
        ValidationUtil.validateLength(isbn13, "isbn13", 13, 13, true);
        ValidationUtil.validateLength(language, "language", 2, 2, true);
        return ValidationUtil.validateDate(publishDateStr, "publish_date", update);
    }

    public static void validateISBN(String isbn10, String isbn13) throws JSONException {
        if (Strings.isNullOrEmpty(isbn10) && Strings.isNullOrEmpty(isbn13)) {
            throw new ClientException("ValidationError", "At least one ISBN number is mandatory");
        }
    }
}