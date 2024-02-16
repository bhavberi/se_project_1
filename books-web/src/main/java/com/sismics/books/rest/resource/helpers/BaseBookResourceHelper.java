package com.sismics.books.rest.resource.helpers;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;
import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.rest.exception.ClientException;
import com.sismics.security.IPrincipal;

public class BaseBookResourceHelper {
    protected static final String BOOK_NOT_FOUND = "BookNotFound";
    protected static final String BOOK_ALREADY_ADDED = "BookAlreadyAdded";
    protected static final String BOOK_ALREADY_ADDED_ERROR = "Book already added";

    protected static Book createBook(String title, String subtitle, String author, String description, String isbn10,
            String isbn13, Long pageCount, String language, Date publishDate) {
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());
        updateBook(book, title, subtitle, author, description, isbn10, isbn13, pageCount, language, publishDate);
        return book;
    }

    protected static void saveBook(Book book) throws JSONException {
        BookDao bookDao = new BookDao();
        Book existingBookIsbn10 = bookDao.getByIsbn(book.getIsbn10());
        Book existingBookIsbn13 = bookDao.getByIsbn(book.getIsbn13());
        if (existingBookIsbn10 != null || existingBookIsbn13 != null) {
            throw new ClientException(BOOK_ALREADY_ADDED, BOOK_ALREADY_ADDED_ERROR);
        }
        bookDao.create(book);
    }

    protected static UserBook createUserBook(Book book, IPrincipal principal) {
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = new UserBook();
        userBook.setUserId(principal.getId());
        userBook.setBookId(book.getId());
        userBook.setCreateDate(new Date());
        userBookDao.create(userBook);
        return userBook;
    }

    protected static void updateTags(String userBookId, List<String> tagList, IPrincipal principal)
            throws JSONException {
        if (tagList != null) {
            TagDao tagDao = new TagDao();
            Set<String> tagSet = new HashSet<>();
            Set<String> tagIdSet = new HashSet<>();
            List<Tag> tagDbList = tagDao.getByUserId(principal.getId());

            for (Tag tagDb : tagDbList) {
                tagIdSet.add(tagDb.getId());
            }
            for (String tagId : tagList) {
                if (!tagIdSet.contains(tagId)) {
                    throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
                }
                tagSet.add(tagId);
            }
            tagDao.updateTagList(userBookId, tagSet);
        }
    }

    protected static Response buildResponse(String userBookId) throws JSONException {
        JSONObject response = new JSONObject();
        response.put("id", userBookId);
        return Response.ok().entity(response).build();
    }

    protected static UserBook getUserBook(String userBookId, IPrincipal principal) throws JSONException {
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }
        return userBook;
    }

    protected static Book getBook(UserBook userBook) {
        BookDao bookDao = new BookDao();
        return bookDao.getById(userBook.getBookId());
    }

    protected static void checkISBNNumbers(Book book, String isbn10, String isbn13) throws JSONException {
        BookDao bookDao = new BookDao();
        if (!Strings.isNullOrEmpty(isbn10) && book.getIsbn10() != null && !book.getIsbn10().equals(isbn10)) {
            Book bookIsbn10 = bookDao.getByIsbn(isbn10);
            if (bookIsbn10 != null) {
                throw new ClientException(BOOK_ALREADY_ADDED, BOOK_ALREADY_ADDED_ERROR);
            }
        }

        if (!Strings.isNullOrEmpty(isbn13) && book.getIsbn13() != null && !book.getIsbn13().equals(isbn13)) {
            Book bookIsbn13 = bookDao.getByIsbn(isbn13);
            if (bookIsbn13 != null) {
                throw new ClientException(BOOK_ALREADY_ADDED, BOOK_ALREADY_ADDED_ERROR);
            }
        }
    }

    protected static void updateBook(
            Book book, String title,
            String subtitle, String author,
            String description, String isbn10,
            String isbn13, Long pageCount,
            String language, Date publishDate) {
        if (title != null) {
            book.setTitle(title);
        }
        if (subtitle != null) {
            book.setSubtitle(subtitle);
        }
        if (author != null) {
            book.setAuthor(author);
        }
        if (description != null) {
            book.setDescription(description);
        }
        if (isbn10 != null) {
            book.setIsbn10(isbn10);
        }
        if (isbn13 != null) {
            book.setIsbn13(isbn13);
        }
        if (pageCount != null) {
            book.setPageCount(pageCount);
        }
        if (language != null) {
            book.setLanguage(language);
        }
        if (publishDate != null) {
            book.setPublishDate(publishDate);
        }
    }

}
