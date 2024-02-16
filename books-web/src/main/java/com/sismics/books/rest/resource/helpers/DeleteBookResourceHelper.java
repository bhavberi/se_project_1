package com.sismics.books.rest.resource.helpers;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.rest.exception.ClientException;
import com.sismics.security.IPrincipal;

public class DeleteBookResourceHelper extends BaseBookResourceHelper {
    private static final String BOOK_NOT_FOUND = "BookNotFound";

    /**
     * Deletes a book.
     * 
     * @param userBookId User book ID
     * @return Response
     * @throws JSONException
     */
    public static Response delete(String userBookId, IPrincipal principal) throws JSONException {

        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }

        // Delete the user book
        userBookDao.delete(userBook.getId());

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
