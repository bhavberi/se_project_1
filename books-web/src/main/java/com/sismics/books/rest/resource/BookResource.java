package com.sismics.books.rest.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;

import com.sismics.books.rest.service.BookService;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * Book REST resources.
 * 
 * @author bgamard
 */
@Path("/book")
public class BookResource {
    private final BookService bookService;

    public BookResource() {
        this.bookService = new BookService();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@FormParam("isbn") String isbn) throws JSONException {
        authenticate();
        return bookService.addBook(isbn, principal);
    }

    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String userBookId) throws JSONException {
        authenticate();
        return bookService.deleteBook(userBookId, principal);
    }

    @PUT
    @Path("manual")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@FormParam("title") String title, @FormParam("subtitle") String subtitle,
            @FormParam("author") String author, @FormParam("description") String description,
            @FormParam("isbn10") String isbn10, @FormParam("isbn13") String isbn13,
            @FormParam("page_count") Long pageCount, @FormParam("language") String language,
            @FormParam("publish_date") String publishDateStr, @FormParam("tags") List<String> tagList)
            throws JSONException {
        authenticate();
        return bookService.addManualBook(title, subtitle, author, description, isbn10, isbn13, pageCount, language,
                publishDateStr, tagList, principal);
    }

    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String userBookId, @FormParam("title") String title,
            @FormParam("subtitle") String subtitle, @FormParam("author") String author,
            @FormParam("description") String description, @FormParam("isbn10") String isbn10,
            @FormParam("isbn13") String isbn13, @FormParam("page_count") Long pageCount,
            @FormParam("language") String language, @FormParam("publish_date") String publishDateStr,
            @FormParam("tags") List<String> tagList) throws JSONException {
        authenticate();
        return bookService.updateBook(userBookId, title, subtitle, author, description, isbn10, isbn13, pageCount,
                language, publishDateStr, tagList, principal);
    }

    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") String userBookId) throws JSONException {
        authenticate();
        return bookService.getBook(userBookId, principal);
    }

    @GET
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response cover(@PathParam("id") final String userBookId) throws JSONException {
        return bookService.getCover(userBookId);
    }

    @POST
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCover(@PathParam("id") String userBookId, @FormParam("url") String imageUrl)
            throws JSONException {
        authenticate();
        return bookService.updateCover(userBookId, imageUrl, principal);
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn, @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search, @QueryParam("read") Boolean read, @QueryParam("tag") String tagName)
            throws JSONException {
        authenticate();
        return bookService.listBooks(limit, offset, sortColumn, asc, search, read, tagName, principal);
    }

    @PUT
    @Consumes("multipart/form-data")
    @Path("import")
    public Response importFile(@FormDataParam("file") FormDataBodyPart fileBodyPart) throws JSONException {
        authenticate();
        return bookService.importFile(fileBodyPart, principal);
    }

    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@PathParam("id") final String userBookId, @FormParam("read") boolean read)
            throws JSONException {
        authenticate();
        return bookService.markAsRead(userBookId, read, principal);
    }

    private void authenticate() {
        // Authentication logic
    }

    private String principal = "examplePrincipal";
}