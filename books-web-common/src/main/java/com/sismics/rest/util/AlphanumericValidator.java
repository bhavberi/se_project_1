package com.sismics.rest.util;

import com.sismics.rest.exception.ClientException;
import org.codehaus.jettison.json.JSONException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

public class AlphanumericValidator implements IValidator {
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

    @Override
    public void validate(String s, String name) throws JSONException {
        if (!ALPHANUMERIC_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError",
                    MessageFormat.format("{0} must have only alphanumeric or underscore characters", name));
        }
    }
}