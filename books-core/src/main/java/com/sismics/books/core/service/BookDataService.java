package com.sismics.books.core.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractIdleService;
import com.sismics.books.core.constant.ConfigType;
import com.sismics.books.core.constant.Constants;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.util.ConfigUtil;
import com.sismics.books.core.util.TransactionUtil;

/**
 * Service to fetch book informations.
 *
 * @author bgamard
 */
public class BookDataService extends AbstractIdleService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(BookDataService.class);

    /**
     * Executor for book API requests.
     */
    private ExecutorService executor;

    /**
     * API key Google.
     */
    private String apiKeyGoogle = null;

    /**
     * Parser for multiple date formats;
     */
    private static DateTimeFormatter formatter;

    /**
     * Google Books API Search Object.
     */
    private ASearcher googlesearcher = new BookSearchGoogle();

    /**
     * Open Library API Object.
     */
    private ASearcher openLibrarysearcher = new BookSearchOpenLib();

    static {
        // Initialize date parser
        DateTimeParser[] parsers = {
                DateTimeFormat.forPattern("yyyy").getParser(),
                DateTimeFormat.forPattern("yyyy-MM").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
                DateTimeFormat.forPattern("MMM d, yyyy").getParser() };
        formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
    }

    @Override
    protected void startUp() throws Exception {
        initConfig();
        executor = Executors.newSingleThreadExecutor();
        if (log.isInfoEnabled()) {
            log.info("Book data service started");
        }
    }

    /**
     * Initialize service configuration.
     */
    public void initConfig() {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                apiKeyGoogle = ConfigUtil.getConfigStringValue(ConfigType.API_KEY_GOOGLE);
            }
        });
    }

    /**
     * Search a book by its ISBN.
     * 
     * @return Book found
     * @throws Exception
     */
    public Book searchBook(String rawIsbn) throws Exception {
        // Sanitize ISBN (keep only digits)
        final String isbn = rawIsbn.replaceAll("[^\\d]", "");

        // Validate ISBN
        if (Strings.isNullOrEmpty(isbn)) {
            throw new Exception("ISBN is empty");
        }
        if (isbn.length() != 10 && isbn.length() != 13) {
            throw new Exception("ISBN must be 10 or 13 characters long");
        }

        Callable<Book> callable = new Callable<Book>() {

            @Override
            public Book call() throws Exception {
                try {
                    return googlesearcher.search(isbn, apiKeyGoogle, formatter);
                } catch (Exception e) {
                    log.warn("Book not found with Google: " + isbn + " with error: " + e.getMessage());
                    try {
                        return openLibrarysearcher.search(isbn, null, formatter);
                    } catch (Exception e0) {
                        log.warn("Book not found with Open Library: " + isbn + " with error: " + e0.getMessage());
                        log.error("Book not found with any API: " + isbn);
                        throw e0;
                    }
                }
            }
        };
        FutureTask<Book> futureTask = new FutureTask<Book>(callable);
        executor.submit(futureTask);

        return futureTask.get();
    }

    @Override
    protected void shutDown() throws Exception {
        executor.shutdown();
        executor.awaitTermination(Constants.DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        if (log.isInfoEnabled()) {
            log.info("Book data service stopped");
        }
    }
}
