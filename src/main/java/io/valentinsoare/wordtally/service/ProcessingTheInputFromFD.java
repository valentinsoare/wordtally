package io.valentinsoare.wordtally.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/***
 *  Here, as you can see by the name, we have the design on how to process the input coming from inputStream (System.in)
 * Here we tackle the input data in a simple way.
 * This is the first version, but in the next one I will use async and parallel processing here and define more methods, one per type of task. In this case, each time we call a method
 * we need to mark the BufferReader object, and then if another method is called, we do a reader. Reset to reset the inputStream to
 * beginning and then mark it again and start the app logic.
 * Also, we are going to need more BufferReader objects, one BufferReader per type of task,
 * and when we create these bufferReaders, we need to use reset in other to go to the beginning of the initial inputStream
 * */

@Service
public class ProcessingTheInputFromFD implements ProcessingAsAService {

    private final OutputFormat outputFormat;

    @Autowired
    public ProcessingTheInputFromFD(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    @Override
    public List<Long> countingAndPrinting(InputStream inputStream, boolean toCountLines, boolean toCountWords,
                                          boolean toCountChars, boolean toCountBytes) throws IOException {
        long numberOfChars = 0, numberOfLines = 0, numberOfWords = 0, numberOfBytes = 0;
        inputStream.mark(Integer.MAX_VALUE);

        if (toCountBytes) {
            int readBytes;
            byte[] buffer = new byte[2048];

            while ((readBytes = inputStream.read(buffer)) != -1) {
                numberOfBytes += readBytes;
            }
        }

        int byteRead;
        boolean isWord = false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        inputStream.reset();

        while ((byteRead = reader.read()) != -1) {

            if ((toCountLines) && (byteRead == '\n')) {
                numberOfLines += 1;
            }

            if (toCountWords) {
                if (Character.isWhitespace(byteRead)) {
                    if (isWord) {
                        isWord = false;
                    }
                } else {
                    if (!isWord) {
                        isWord = true;
                        numberOfWords += 1;
                    }
                }
            }

            if (toCountChars) {
                numberOfChars += 1;
            }
        }

        return Stream.of(numberOfLines, numberOfWords, numberOfChars, numberOfBytes)
                .filter(e -> e != 0)
                .toList();
    }

    @Override
    public List<Long> execTheTasksWithCountingInParallelWithParallelStreams(List<String> options, InputStream inputStream) {
        Map<String, Boolean> taskOptions = initializeTaskOptions(options);

        try {
            return performCountingTasks(inputStream, taskOptions);
        } catch (IOException e) {
            handleIOException(e, "executeTasksWithParallelStreams");
            return Collections.emptyList();
        }
    }

    private Map<String, Boolean> initializeTaskOptions(List<String> options) {
        Map<String, Boolean> taskOptions = new HashMap<>();

        taskOptions.put("lines", options.contains("lines"));
        taskOptions.put("words", options.contains("words"));
        taskOptions.put("chars", options.contains("chars"));
        taskOptions.put("bytes", options.contains("bytes"));

        return taskOptions;
    }

    private List<Long> performCountingTasks(InputStream inputStream, Map<String, Boolean> taskOptions) throws IOException {
        return countingAndPrinting(inputStream, taskOptions.get("lines"), taskOptions.get("words"),
                taskOptions.get("chars"), taskOptions.get("bytes")
        );
    }

    private void handleIOException(IOException e, String methodName) {
        ErrorMessage msg = ErrorMessage.builder()
                .threadName(Thread.currentThread().getName())
                .methodName(methodName)
                .clazzName(this.getClass().getName())
                .message(e.getMessage())
                .dateTime(Instant.now().toString())
                .severity(Severity.ERROR)
                .build();

        try {
            System.out.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
