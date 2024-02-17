package com.sismics.books.rest.resource.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sismics.books.core.dao.jpa.BookDao;
import com.sismics.books.core.dao.jpa.TagDao;
import com.sismics.books.core.dao.jpa.UserBookDao;
import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
import com.sismics.books.core.dao.jpa.dto.TagDto;
import com.sismics.books.core.dao.jpa.dto.UserBookDto;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.model.jpa.Tag;
import com.sismics.books.core.model.jpa.UserBook;
import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.jpa.PaginatedList;
import com.sismics.books.core.util.jpa.PaginatedLists;
import com.sismics.books.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.security.IPrincipal;

public class GetBookResourceHelpers extends BaseBookResourceHelper {

    // Magic Strings
    private static final String BOOK_NOT_FOUND = "BookNotFound";

    /**
     * Get a book.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    public static Response get(String userBookId, IPrincipal principal) throws JSONException {

        // Fetch the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
        if (userBook == null) {
            throw new ClientException(BOOK_NOT_FOUND, "Book not found with id " + userBookId);
        }

        // Fetch the book
        BookDao bookDao = new BookDao();
        Book bookDb = bookDao.getById(userBook.getBookId());

        // Return book data
        JSONObject book = new JSONObject();
        book.put("id", userBook.getId());
        book.put("title", bookDb.getTitle());
        book.put("subtitle", bookDb.getSubtitle());
        book.put("author", bookDb.getAuthor());
        book.put("page_count", bookDb.getPageCount());
        book.put("description", bookDb.getDescription());
        book.put("isbn10", bookDb.getIsbn10());
        book.put("isbn13", bookDb.getIsbn13());
        book.put("language", bookDb.getLanguage());
        if (bookDb.getPublishDate() != null) {
            book.put("publish_date", bookDb.getPublishDate().getTime());
        }
        book.put("create_date", userBook.getCreateDate().getTime());
        if (userBook.getReadDate() != null) {
            book.put("read_date", userBook.getReadDate().getTime());
        }

        // Add tags
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookId);
        List<JSONObject> tags = new ArrayList<>();
        for (TagDto tagDto : tagDtoList) {
            JSONObject tag = new JSONObject();
            tag.put("id", tagDto.getId());
            tag.put("name", tagDto.getName());
            tag.put("color", tagDto.getColor());
            tags.add(tag);
        }
        book.put("tags", tags);

        return Response.ok().entity(book).build();
    }

    /**
     * Returns a book cover.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    public static Response cover(final String userBookId) throws JSONException {
        // Get the user book
        UserBookDao userBookDao = new UserBookDao();
        UserBook userBook = userBookDao.getUserBook(userBookId);

        // Get the cover image
        File file = Paths.get(DirectoryUtil.getBookDirectory().getPath(), userBook.getBookId()).toFile();
        InputStream inputStream = null;
        try {
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = new FileInputStream(new File(
                        GetBookResourceHelpers.class.getResource("/dummy.png").getFile()));
            }
        } catch (FileNotFoundException e) {
            throw new ServerException("FileNotFound", "Cover file not found", e);
        }

        return Response.ok(inputStream)
                .header("Content-Type", "image/jpeg")
                .header("Expires",
                        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000))
                .build();
    }

    /**
     * Returns all books.
     * 
     * @param limit  Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    public static Response list(
            Integer limit,
            Integer offset,
            Integer sortColumn,
            Boolean asc,
            String search,
            Boolean read,
            String tagName, IPrincipal principal) throws JSONException {

        JSONObject response = new JSONObject();
        List<JSONObject> books = new ArrayList<>();

        UserBookDao userBookDao = new UserBookDao();
        TagDao tagDao = new TagDao();
        PaginatedList<UserBookDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        UserBookCriteria criteria = new UserBookCriteria();
        criteria.setSearch(search);
        criteria.setRead(read);
        criteria.setUserId(principal.getId());
        if (!Strings.isNullOrEmpty(tagName)) {
            Tag tag = tagDao.getByName(principal.getId(), tagName);
            if (tag != null) {
                criteria.setTagIdList(Lists.newArrayList(tag.getId()));
            }
        }
        try {
            userBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in books", e);
        }

        for (UserBookDto userBookDto : paginatedList.getResultList()) {
            JSONObject book = new JSONObject();
            book.put("id", userBookDto.getId());
            book.put("title", userBookDto.getTitle());
            book.put("subtitle", userBookDto.getSubtitle());
            book.put("author", userBookDto.getAuthor());
            book.put("language", userBookDto.getLanguage());
            book.put("publish_date", userBookDto.getPublishTimestamp());
            book.put("create_date", userBookDto.getCreateTimestamp());
            book.put("read_date", userBookDto.getReadTimestamp());

            // Get tags
            List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookDto.getId());
            List<JSONObject> tags = new ArrayList<>();
            for (TagDto tagDto : tagDtoList) {
                JSONObject tag = new JSONObject();
                tag.put("id", tagDto.getId());
                tag.put("name", tagDto.getName());
                tag.put("color", tagDto.getColor());
                tags.add(tag);
            }
            book.put("tags", tags);

            books.add(book);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("books", books);

        return Response.ok().entity(response).build();
    }

}
