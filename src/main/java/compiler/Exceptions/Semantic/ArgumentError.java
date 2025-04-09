package compiler.Exceptions.Semantic;

public class ArgumentError extends RuntimeException {
    public ArgumentError(String message) {
        super(formatMessage(message, null));
    }

    public ArgumentError(String message, int lineNumber) {
        super(formatMessage(message, lineNumber));
    }

    private static String formatMessage(String message, Integer lineNumber) {
        String errorName = "Argument Error: ";
        String processedMessage = message.contains(errorName) ? message : errorName + message;

        return lineNumber == null ? processedMessage : processedMessage + " at line " + lineNumber;
    }
}
