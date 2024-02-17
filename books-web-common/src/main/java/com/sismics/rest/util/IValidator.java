package com.sismics.rest.util;

import org.codehaus.jettison.json.JSONException;

public interface IValidator {
    void validate(String s, String name) throws JSONException;
}
