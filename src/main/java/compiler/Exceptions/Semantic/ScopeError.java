package compiler.Exceptions.Semantic;

public class ScopeError extends RuntimeException {
    public ScopeError(String message) {
        super(formatMessage(message, null));
    }

    public ScopeError(String message, int lineNumber) {
        super(formatMessage(message, lineNumber));
    }

    private static String formatMessage(String message, Integer lineNumber) {
        String errorName = "ScopeError: ";
        String processedMessage = message.contains(errorName) ? message : errorName + message;

        return lineNumber == null ? processedMessage : processedMessage + " at line " + lineNumber;
    }
}
