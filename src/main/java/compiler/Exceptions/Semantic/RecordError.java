package compiler.Exceptions.Semantic;

public class RecordError extends RuntimeException {
    public RecordError(String message) {
        super(formatMessage(message, null));
    }

    public RecordError(String message, int lineNumber) {
        super(formatMessage(message, lineNumber));
    }

    private static String formatMessage(String message, Integer lineNumber) {
        String errorName = "RecordError: ";
        String processedMessage = message.contains(errorName) ? message : errorName + message;

        return lineNumber == null ? processedMessage : processedMessage + " at line " + lineNumber;
    }
}

