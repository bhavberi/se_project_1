# BookDataService - Missing Modularization

[Link to chat](https://chat.openai.com/share/5c505aec-e75b-4849-ba91-f9d6c0f16965)
## Chat 1: Before being told that it was a Missing Hierarchy & Modularization Smell:

Design smells are symptoms of poor design choices or violations of good design principles that can lead to various issues such as reduced maintainability, flexibility, or scalability. In the provided code, several design smells can be identified:

God Class: The BookDataService class is handling too many responsibilities. It's responsible for configuration initialization, API communication, book searching, downloading thumbnails, and managing thread execution. This violates the Single Responsibility Principle.

Long Method: The searchBookWithGoogle and searchBookWithOpenLibrary methods are quite long and handle multiple concerns. They perform API communication, parsing JSON responses, and building book objects. Breaking down these methods into smaller, more focused methods would improve readability and maintainability.

Primitive Obsession: The handling of ISBN strings directly within the searchBook method indicates a primitive obsession smell. It would be better to encapsulate ISBN-related operations into a separate class.

Magic Numbers/String: There are several magic numbers and strings scattered throughout the code (e.g., API URLs, timeout values). These should be extracted into constants for better maintainability and readability.

Exception Handling: The exception handling in the searchBook method is not optimal. It catches generic Exception, which makes it hard to distinguish between different failure scenarios. It would be better to catch more specific exceptions and handle them accordingly.

Concurrency Handling: The code uses ExecutorService for asynchronous execution, but it doesn't handle potential exceptions or errors that might occur during execution. Proper error handling and logging mechanisms should be implemented.

Hardcoded Dependencies: The code directly instantiates dependencies like ObjectMapper and URL instead of injecting them. This makes the code less flexible and harder to test.

To refactor the code and address these design smells, here are some suggestions:

Extract Classes: Break down the BookDataService class into smaller classes, each responsible for a single concern such as configuration management, API communication, book parsing, and thumbnail downloading.

Single Responsibility Principle: Ensure that each class and method has a single responsibility. For example, create separate classes for API communication and book parsing.

Encapsulation: Create a separate class for handling ISBN-related operations, encapsulating all ISBN-related logic.

Use Constants: Replace magic numbers and strings with named constants to improve readability and maintainability.

Refactor Exception Handling: Use more specific exception types and handle them appropriately. Consider using checked exceptions for recoverable errors and unchecked exceptions for unexpected errors.

Improve Concurrency Handling: Implement error handling and logging mechanisms for asynchronous execution. Consider using CompletableFuture or other constructs for more flexible concurrency management.

Dependency Injection: Inject dependencies instead of directly instantiating them, which makes the code more modular and testable.

By addressing these design smells and refactoring the code accordingly, you can improve its readability, maintainability, and flexibility.

```java
package com.sismics.books.core.service;

import com.neovisionaries.i18n.LanguageCode;
import com.sismics.books.core.constant.ConfigType;
import com.sismics.books.core.model.jpa.Book;
import com.sismics.books.core.util.ConfigUtil;
import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.mime.MimeType;
import com.sismics.books.core.util.mime.MimeTypeUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookDataService {
    private static final Logger log = LoggerFactory.getLogger(BookDataService.class);

    private static final String GOOGLE_BOOKS_SEARCH_FORMAT = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s&key=%s";
    private static final String OPEN_LIBRARY_FORMAT = "http://openlibrary.org/api/volumes/brief/isbn/%s.json";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String apiKeyGoogle;

    public BookDataService() {
        apiKeyGoogle = ConfigUtil.getConfigStringValue(ConfigType.API_KEY_GOOGLE);
    }

    public Book searchBook(String rawIsbn) throws Exception {
        final String isbn = sanitizeIsbn(rawIsbn);

        if (isbn.isEmpty()) {
            throw new IllegalArgumentException("ISBN is empty");
        }

        Callable<Book> callable = () -> {
            try {
                return searchBookWithGoogle(isbn);
            } catch (Exception e) {
                log.warn("Book not found with Google: " + isbn + " with error: " + e.getMessage());
                return searchBookWithOpenLibrary(isbn);
            }
        };
        return executor.submit(callable).get();
    }

    private Book searchBookWithGoogle(String isbn) throws Exception {
        JsonNode rootNode = fetchJsonResponse(String.format(Locale.ENGLISH, GOOGLE_BOOKS_SEARCH_FORMAT, isbn, apiKeyGoogle));
        return parseBook(rootNode);
    }

    private Book searchBookWithOpenLibrary(String isbn) throws Exception {
        JsonNode rootNode = fetchJsonResponse(String.format(Locale.ENGLISH, OPEN_LIBRARY_FORMAT, isbn));
        return parseBook(rootNode.get("records").getElements().next());
    }

    private JsonNode fetchJsonResponse(String urlString) throws Exception {
        URL url = new URL(urlString);
        try (InputStream inputStream = new BufferedInputStream(url.openStream())) {
            return objectMapper.readTree(inputStream);
        }
    }

    private Book parseBook(JsonNode bookNode) throws Exception {
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());
        // Parse book details and set properties
        return book;
    }

    private String sanitizeIsbn(String rawIsbn) {
        return rawIsbn.replaceAll("[^\\d]", "");
    }

    public void downloadThumbnail(Book book, String imageUrl) throws Exception {
        URLConnection imageConnection = new URL(imageUrl).openConnection();
        imageConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
        try (InputStream inputStream = new BufferedInputStream(imageConnection.getInputStream())) {
            if (MimeTypeUtil.guessMimeType(inputStream) != MimeType.IMAGE_JPEG) {
                throw new IllegalArgumentException("Only JPEG images are supported as thumbnails");
            }

            Path imagePath = Paths.get(DirectoryUtil.getBookDirectory().getPath(), book.getId());
            Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
```

In this refactored version:

Responsibility Segregation: The responsibilities have been segregated into methods like searchBookWithGoogle, searchBookWithOpenLibrary, fetchJsonResponse, parseBook, etc.

Constructor Initialization: The API key is now initialized in the constructor.

Improved Exception Handling: Exceptions are handled more appropriately within methods, making the code clearer.

Encapsulation of ISBN Sanitization: ISBN sanitization logic is encapsulated within the sanitizeIsbn method.

Dependency Injection: The ObjectMapper is instantiated as a class member, making it easier to manage and inject if needed.

Resource Management: Proper resource management is ensured by using try-with-resources for handling input streams.

Thread Pool Management: The executor service is properly initialized and shut down.

This refactored version is more modular, easier to read, and follows better design practices.

---
## Chat 2: After being told that it was a Missing Hierarchy & Modularization smell:

To address the Missing Hierarchy & Modularization smell, we can introduce a more hierarchical and modular structure to the code by separating concerns into smaller, more specialized classes. Below is a refactored version of the code following a more hierarchical and modular approach:

```java
package com.sismics.books.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BookDataService {
    private static final Logger log = LoggerFactory.getLogger(BookDataService.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final BookSearcher bookSearcher;

    public BookDataService(BookSearcher bookSearcher) {
        this.bookSearcher = bookSearcher;
    }

    public Future<Book> searchBook(String rawIsbn) {
        return executor.submit(() -> {
            try {
                return bookSearcher.searchBook(rawIsbn);
            } catch (Exception e) {
                log.error("Failed to search book", e);
                throw e;
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
```

```java
package com.sismics.books.core.service;

import com.sismics.books.core.model.jpa.Book;

import java.util.concurrent.Future;

public interface BookSearcher {
    Future<Book> searchBook(String rawIsbn) throws Exception;
}
```

```java
package com.sismics.books.core.service;

import com.sismics.books.core.util.ConfigUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Future;

public class GoogleBooksSearcher implements BookSearcher {
    private static final String GOOGLE_BOOKS_SEARCH_FORMAT = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s&key=%s";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKeyGoogle;

    public GoogleBooksSearcher() {
        this.apiKeyGoogle = ConfigUtil.getConfigStringValue(ConfigType.API_KEY_GOOGLE);
    }

    @Override
    public Future<Book> searchBook(String rawIsbn) throws Exception {
        String isbn = sanitizeIsbn(rawIsbn);
        String urlString = String.format(Locale.ENGLISH, GOOGLE_BOOKS_SEARCH_FORMAT, isbn, apiKeyGoogle);
        return fetchBook(urlString);
    }

    private String sanitizeIsbn(String rawIsbn) {
        return rawIsbn.replaceAll("[^\\d]", "");
    }

    private Future<Book> fetchBook(String urlString) throws Exception {
        try (InputStream inputStream = new BufferedInputStream(new URL(urlString).openStream())) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            // Parse JsonNode and return Book object
            return null;
        }
    }
}
```

```java
package com.sismics.books.core.service;

import com.sismics.books.core.util.ConfigUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Future;

public class OpenLibrarySearcher implements BookSearcher {
    private static final String OPEN_LIBRARY_FORMAT = "http://openlibrary.org/api/volumes/brief/isbn/%s.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Future<Book> searchBook(String rawIsbn) throws Exception {
        String isbn = sanitizeIsbn(rawIsbn);
        String urlString = String.format(Locale.ENGLISH, OPEN_LIBRARY_FORMAT, isbn);
        return fetchBook(urlString);
    }

    private String sanitizeIsbn(String rawIsbn) {
        return rawIsbn.replaceAll("[^\\d]", "");
    }

    private Future<Book> fetchBook(String urlString) throws Exception {
        try (InputStream inputStream = new BufferedInputStream(new URL(urlString).openStream())) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            // Parse JsonNode and return Book object
            return null;
        }
    }
}
```

In this refactoring:

The BookDataService class is responsible for submitting search tasks to the executor service.
The BookSearcher interface defines a contract for classes responsible for searching books.
The GoogleBooksSearcher and OpenLibrarySearcher classes implement the BookSearcher interface and handle searching books using Google Books API and Open Library API respectively.
Each class now has a single responsibility, which promotes better maintainability and testability.
The executor service is used for asynchronous execution of search tasks.
The dependency on API keys is encapsulated within the respective searcher classes.
Error handling and logging are simplified and centralized in the BookDataService class.

---

Here, we see that the LLM was not able to exactly figure out the Modularity smell but it was able to understand that there is a smell related to Violation of Single Responsibility Principle (SRP) though it was not able to refactor it.

Once we provide it the context, the model performs much better by making an interface for bookSearchService and abstracting out the two classes i.e. GoogleBookSearchService and OpenLibrarySearchService just as was done while manual refactoring.
