package com.sismics.books.rest.resource.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.dao.jpa.UserDao;
import com.sismics.books.core.event.BookImportedEvent;
import com.sismics.books.core.model.context.AppContext;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.User;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.IPrincipal;
import com.sun.jersey.multipart.FormDataBodyPart;

public class AddBookResourceHelper extends BaseBookResourceHelper {

    private static final String BOOK_NOT_FOUND = "BookNotFound";
    private static final String BOOK_ALREADY_ADDED = "BookAlreadyAdded";
    private static final String BOOK_ALREADY_ADDED_ERROR = "Book already added";

    /**
     * Creates a new book.
     * 
     * @param isbn ISBN Number
     * @return Response
     * @throws JSONException
     */
    public static Response add(String isbn, IPrincipal principal) throws JSONException {

        // Validate input data
        ValidationUtil.validateRequired(isbn, "isbn");

        // Fetch the book
        BookDao bookDao = new BookDao();
        Book book = bookDao.getByIsbn(isbn);
        if (book == null) {
            // Try to get the book from a public API
            try {
                book = AppContext.getInstance().getBookDataService().searchBook(isbn);
            } catch (Exception e) {
                throw new ClientException(BOOK_NOT_FOUND, e.getCause().getMessage(), e);
            }

            // Save the new book in database
            bookDao.create(book);
        }

        // Create the user book if needed
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getByBook(book.getId(), principal.getId());
        if (userBook == null) {
            userBook = new UserBook();
            userBook.setUserId(principal.getId());
            userBook.setBookId(book.getId());
            userBook.setCreateDate(new Date());
            userBookDao.create(userBook);
        } else {
            throw new ClientException(BOOK_ALREADY_ADDED, BOOK_ALREADY_ADDED_ERROR);
        }

        JSONObject response = new JSONObject();
        response.put("id", userBook.getId());
        return Response.ok().entity(response).build();
    }

    /**
     * Add a book book manually.
     * 
     * @param title       Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    public static Response add_manual(
            String title,
            String subtitle,
            String author,
            String description,
            String isbn10,
            String isbn13,
            Long pageCount,
            String language,
            String publishDateStr,
            List<String> tagList,
            IPrincipal principal) throws JSONException {

        Date publishDate = ValidationUtil.validateInputData(title, subtitle, author, description,
                isbn10, isbn13, language, publishDateStr, false);
        ValidationUtil.validateISBN(isbn10, isbn13);

        Book book = createBook(title, subtitle, author, description, isbn10, isbn13, pageCount, language, publishDate);
        saveBook(book);
        UserBook userBook = createUserBook(book, principal);
        updateTags(userBook.getId(), tagList, principal);

        return buildResponse(userBook.getId());
    }

    /**
     * Imports books.
     *
     * @param fileBodyPart File to import
     * @return Response
     * @throws JSONException
     */

    public static Response importFile(FormDataBodyPart fileBodyPart, IPrincipal principal) throws JSONException {

        // Validate input data
        ValidationUtil.validateRequired(fileBodyPart, "file");

        UserDao userDao = new UserDao();
        User user = userDao.getById(principal.getId());

        InputStream in = fileBodyPart.getValueAs(InputStream.class);
        File importFile = null;
        try {
            // Copy the incoming stream content into a temporary file
            importFile = File.createTempFile("books_import", null);
            FileOutputStream out = new FileOutputStream(importFile);
            IOUtils.copy(in, out);
            out.close();

            BookImportedEvent event = new BookImportedEvent();
            event.setUser(user);
            event.setImportFile(importFile);
            AppContext.getInstance().getImportEventBus().post(event);

            // Always return ok
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            return Response.ok().entity(response).build();
        } catch (Exception e) {
            if (importFile != null) {
                try {
                    importFile.delete();
                } catch (SecurityException e2) {
                    // NOP
                }
            }
            throw new ServerException("ImportError", "Error importing books", e);
        }
    }

}
