package com.sismics.rest.util;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONException;

import com.sismics.rest.exception.ClientException;

public class EmailValidator implements IValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");

    @Override
    public void validate(String s, String name) throws JSONException {
        if (!EMAIL_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be an email", name));
        }
    }
}