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

import com.sismics.rest.exception.ForbiddenClientException;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * Book REST resources.
 * 
 * @author bgamard
 */
@Path("/book")
public class BookResource extends BaseResource {

    /**
     * Creates a new book.
     * 
     * @param isbn ISBN Number
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("isbn") String isbn) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        return AddBookResource.add(isbn, principal);

    }

    /**
     * Deletes a book.
     * 
     * @param userBookId User book ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String userBookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        return DeleteBookResource.delete(userBookId, principal);

    }

    /**
     * Add a book book manually.
     * 
     * @param title       Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Path("manual")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("title") String title,
            @FormParam("subtitle") String subtitle,
            @FormParam("author") String author,
            @FormParam("description") String description,
            @FormParam("isbn10") String isbn10,
            @FormParam("isbn13") String isbn13,
            @FormParam("page_count") Long pageCount,
            @FormParam("language") String language,
            @FormParam("publish_date") String publishDateStr,
            @FormParam("tags") List<String> tagList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        return AddBookResource.add_manual(title, subtitle, author, description, isbn10, isbn13, pageCount, language,
                publishDateStr, tagList, principal);

    }

    /**
     * Updates the book.
     * 
     * @param title       Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String userBookId,
            @FormParam("title") String title,
            @FormParam("subtitle") String subtitle,
            @FormParam("author") String author,
            @FormParam("description") String description,
            @FormParam("isbn10") String isbn10,
            @FormParam("isbn13") String isbn13,
            @FormParam("page_count") Long pageCount,
            @FormParam("language") String language,
            @FormParam("publish_date") String publishDateStr,
            @FormParam("tags") List<String> tagList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        return UpdateBookResource.update(userBookId, title, subtitle, author, description, isbn10, isbn13, pageCount,
                language, publishDateStr, tagList, principal);
    }

    /**
     * Get a book.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String userBookId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        return GetBookResource.get(userBookId, principal);
    }

    /**
     * Returns a book cover.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response cover(
            @PathParam("id") final String userBookId) throws JSONException {

        return GetBookResource.cover(userBookId);

    }

    /**
     * Updates a book cover.
     * 
     * @param id User book ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/cover")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCover(
            @PathParam("id") String userBookId,
            @FormParam("url") String imageUrl) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        return UpdateBookResource.updateCover(userBookId, imageUrl, principal);

    }

    /**
     * Returns all books.
     * 
     * @param limit  Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("read") Boolean read,
            @QueryParam("tag") String tagName) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        return GetBookResource.list(limit, offset, sortColumn, asc, search, read, tagName, principal);
    }

    /**
     * Imports books.
     * 
     * @param fileBodyPart File to import
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Consumes("multipart/form-data")
    @Path("import")
    public Response importFile(
            @FormDataParam("file") FormDataBodyPart fileBodyPart) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        return AddBookResource.importFile(fileBodyPart, principal);

    }

    /**
     * Set a book as read/unread.
     * 
     * @param id   User book ID
     * @param read Read state
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @PathParam("id") final String userBookId,
            @FormParam("read") boolean read) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        return UpdateBookResource.read(userBookId, read, principal);

    }
}
