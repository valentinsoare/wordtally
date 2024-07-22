package io.valentinsoare.wordtally.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/***
 * Here we defined several methods that are in fact used when files are given as arguments.
 * With these methods, we count the lines, words, chars and bytes in these files.
 * Almost all of them are used in async way and processing the files is done in parallel.
 * Since it is async, we can process more files at a time in case it is needed and increase the throughput.
 */
@Service
public class ParseTheInput implements ParsingAsAService {

    private final OutputFormat outputFormat;

    /**
     * Constructs a new ParseTheInput instance with a dependency on OutputFormat for formatting output messages.
     *
     * @param outputFormat The service for formatting output.
     */
    @Autowired
    private ParseTheInput(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Asynchronously counts the number of lines in a given file.
     * Utilizes parallel streams to enhance performance.
     *
     * @param inputFile The path to the file whose lines are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of lines in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfLines(Path inputFile) {
        try (Stream<String> execCounting = Files.lines(inputFile).parallel()) {
           return CompletableFuture.completedFuture(execCounting.count());
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .severity(Severity.ERROR)
                    .threadName(Thread.currentThread().getName())
                    .methodName("countTheNumberOfLines")
                    .clazzName(this.getClass().getName())
                    .dateTime(Instant.now().toString())
                    .message(e.getMessage())
                    .build();

            try {
                System.out.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        } catch (CompletionException | UncheckedIOException f) {
            System.out.printf("wordtally: %s: Cannot count number of lines for this format.%n", inputFile);
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Asynchronously counts the number of characters in a given file.
     * Reads the file character by character to ensure accurate counting.
     *
     * @param inputFile The path to the file whose characters are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of characters in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfChars(Path inputFile) {
        int bR = 0;
        long numberOfChars = 0;

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(Files.newInputStream(inputFile), StandardCharsets.UTF_8))) {
            while ((bR = reader.read()) != -1) {
                numberOfChars += 1;
            }

            return CompletableFuture.completedFuture(numberOfChars);
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .severity(Severity.ERROR)
                    .methodName("countTheNumberOfChars")
                    .message(e.getMessage())
                    .threadName(Thread.currentThread().getName())
                    .dateTime(Instant.now().toString())
                    .clazzName(this.getClass().getName())
                    .build();

            try {
                System.out.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Asynchronously counts the number of words in a given file.
     * Splits the file content by whitespace to identify words, using parallel streams for efficiency.
     *
     * @param inputFile The path to the file whose words are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of words in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfWords(Path inputFile) {
        try (Stream<String> s = Files.lines(inputFile).parallel()) {
            AtomicLong wordCount = new AtomicLong();

            s.flatMap(line -> Stream.of(line.split("\\s")))
                    .forEach(e -> {
                        if (!"".equals(e)) {
                            wordCount.addAndGet(1);
                        }
                    });

            return CompletableFuture.completedFuture(wordCount.get());
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .clazzName(this.getClass().getName())
                    .methodName("countTheNumberOfWords")
                    .severity(Severity.ERROR)
                    .message(e.getMessage())
                    .threadName(Thread.currentThread().getName())
                    .dateTime(Instant.now().toString())
                    .build();
            try {
                System.out.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        } catch (CompletionException | UncheckedIOException f) {
            System.out.printf("wordtally: %s: Cannot count number of words for this format.%n", inputFile);
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Asynchronously counts the number of bytes in a given file.
     * Reads the file in chunks to efficiently count the bytes.
     *
     * @param inputFile The path to the file whose bytes are to be counted.
     * @return A CompletableFuture that, upon completion, returns the count of bytes in the file.
     */
    @Async
    @Override
    public CompletableFuture<Long> countTheNumberOfBytes(Path inputFile) {

        try (InputStream inputStream = Files.newInputStream(inputFile)) {
            long bytesCount = 0;

            int byteRead;
            byte[] bytes = new byte[2048];

            while ((byteRead = inputStream.read(bytes)) != -1) {
                bytesCount += byteRead;
            }

            return CompletableFuture.completedFuture(bytesCount);
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .threadName(Thread.currentThread().getName())
                    .clazzName(this.getClass().getName())
                    .methodName("countTheNumberOfBytes")
                    .dateTime(Instant.now().toString())
                    .severity(Severity.ERROR)
                    .message(e.getMessage())
                    .build();

            try {
                System.out.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        return CompletableFuture.completedFuture(-1L);
    }

    /**
     * Checks if the input stream is ready to be read.
     * This method is used to ensure that there is input available from the stream before attempting to read.
     *
     * @param inputStream The input stream to check.
     * @return true if the stream is ready to be read, false otherwise.
     * @throws JsonProcessingException If there is an error in processing the JSON output for error messages.
     */
    @Override
    public boolean checkTheReaderIsReady(InputStream inputStream) throws JsonProcessingException {
        try {
            return inputStream.available() > 0;
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .methodName("parseTheFileDescriptor")
                    .dateTime(Instant.now().toString())
                    .severity(Severity.ERROR)
                    .clazzName(this.getClass().getName())
                    .threadName(Thread.currentThread().getName())
                    .message(e.getMessage())
                    .build();

            System.out.printf("%s%n", outputFormat.withJSONStyle().writeValueAsString(msg));
            return false;
        }
    }
}
