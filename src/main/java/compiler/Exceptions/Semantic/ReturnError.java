package compiler.Exceptions.Semantic;

public class ReturnError extends RuntimeException {
    public ReturnError(String message) {
        super(formatMessage(message, null));
    }

    public ReturnError(String message, int lineNumber) {
        super(formatMessage(message, lineNumber));
    }

    private static String formatMessage(String message, Integer lineNumber) {
        String errorName = "ReturnError: ";
        String processedMessage = message.contains(errorName) ? message : errorName + message;

        return lineNumber == null ? processedMessage : processedMessage + " | line number: " + lineNumber;
    }
}
