package com.sismics.books.rest.util;

import com.sismics.rest.exception.ClientException;
import com.sismics.rest.util.HttpUrlValidator;
import com.sismics.rest.util.IValidator;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Test the validations.
 *
 * @author jtremeaux
 */
public class TestValidationUtil {
    @Test
    public void testValidateHttpUrlFail() throws Exception {
        IValidator httpUrlValidator = new HttpUrlValidator();
        httpUrlValidator.validate("http://www.google.com", "url");
        httpUrlValidator.validate("https://www.google.com", "url");
        httpUrlValidator.validate(" https://www.google.com ", "url");
        try {
            httpUrlValidator.validate("ftp://www.google.com", "url");
            Assert.fail();
        } catch (ClientException e) {
            // NOP
        }
        try {
            httpUrlValidator.validate("http://", "url");
            Assert.fail();
        } catch (ClientException e) {
            // NOP
        }
    }
 }