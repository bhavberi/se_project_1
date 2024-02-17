package com.sismics.books.rest.resource.helpers;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.service.ASearcher;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.IPrincipal;

public class UpdateBookResourceHelper extends BaseBookResourceHelper {

    // Magic Strings
    private static final String BOOK_NOT_FOUND = "BookNotFound";

    /**
     * Updates the book.
     * 
     * @param title       Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    public static Response update(
            String userBookId,
            String title,
            String subtitle,
            String author,
            String description,
            String isbn10,
            String isbn13,
            Long pageCount,
            String language,
            String publishDateStr,
            List<String> tagList, IPrincipal principal) throws JSONException {

        Date publishDate = ValidationUtil.validateInputData(title, subtitle, author, description, isbn10, isbn13,
                language,
                publishDateStr, true);

        UserBook userBook = getUserBook(userBookId, principal);
        Book book = getBook(userBook);

        checkISBNNumbers(book, isbn10, isbn13);

        updateBook(book, title, subtitle, author, description, isbn10, isbn13, pageCount, language, publishDate);

        updateTags(userBookId, tagList, principal);

        return buildResponse(userBookId);
    }

    /**
     * Updates a book cover.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    public static Response updateCover(String userBookId, String imageUrl, IPrincipal principal) throws JSONException {

        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }

        // Get the book
        BookDao bookDao = new BookDao();
        Book book = bookDao.getById(userBook.getBookId());

        // Download the new cover
        try {
            ASearcher.downloadThumbnail(book, imageUrl);
        } catch (Exception e) {
            throw new ClientException("DownloadCoverError", "Error downloading the cover image");
        }

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok(response).build();
    }

    /**
     * Set a book as read/unread.
     * 
     * @param id   User book ID
     * @param read Read state
     * @return Response
     * @throws JSONException
     */
    public static Response read(final String userBookId, boolean read, IPrincipal principal) throws JSONException {

        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());

        // Update the read date
        userBook.setReadDate(read ? new Date() : null);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

}
