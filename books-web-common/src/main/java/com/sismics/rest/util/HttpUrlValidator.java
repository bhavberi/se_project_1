package com.sismics.rest.util;
import com.sismics.rest.exception.ClientException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

public class HttpUrlValidator implements IValidator {
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("https?://.+");

    @Override
    public void validate(String s, String name) throws JSONException {
        s = StringUtils.strip(s);
        if (!HTTP_URL_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be an HTTP(s) URL", name));
        }
    }
}