package io.valentinsoare.wordtally.auxiliary;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.valentinsoare.wordtally.exception.ErrorMessage;
import io.valentinsoare.wordtally.exception.Severity;
import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

@Component
public class Utils {
    private static OutputFormat outputFormat;

    static {
        outputFormat = new OutputFormat();
    }

    public static boolean isBinaryFile(Path path) {
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            int numberOfTries = 0;

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (fileChannel.read(buffer) > 0 && numberOfTries < 3) {
                buffer.flip();

                while (buffer.hasRemaining()) {
                    byte b = buffer.get();

                    if (b < 0x09 || (b > 0x0D && b < 0x20) || b == 0x7F) {
                        return true;
                    }
                }

                numberOfTries++;

                buffer.clear();
            }

            return false;
        } catch (IOException e) {
            ErrorMessage msg = ErrorMessage.builder()
                    .severity(Severity.ERROR)
                    .methodName("isBinaryFile")
                    .message(e.getMessage())
                    .threadName(Thread.currentThread().getName())
                    .dateTime(Instant.now().toString())
                    .clazzName(Utils.class.getName())
                    .build();

            try {
                System.err.printf("%s %n", outputFormat.withJSONStyle().writeValueAsString(msg));
            } catch (JsonProcessingException ex) {
                System.err.printf("%n\033[1;31m%s - class: %s, method: %s, %s\0330m%n",
                        Severity.FATAL, Utils.class.getClass().getName(), "isBinaryFile", ex.getMessage());
            }
        }

        return false;
    }
}
