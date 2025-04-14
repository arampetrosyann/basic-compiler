package compiler.Exceptions.Semantic;

public class TypeError extends RuntimeException {
    public TypeError(String message) {
        super(formatMessage(message, null));
    }

    public TypeError(String message, int lineNumber) {
        super(formatMessage(message, lineNumber));
    }

    private static String formatMessage(String message, Integer lineNumber) {
        String errorName = "TypeError: ";
        String processedMessage = message.contains(errorName) ? message : errorName + message;

        return lineNumber == null ? processedMessage : processedMessage + " at line " + lineNumber;
    }
}

