package compiler.Exceptions.Semantic;

public class OperatorError extends RuntimeException {
    public OperatorError(String message) {
        super(formatMessage(message, null));
    }

    public OperatorError(String message, int lineNumber) {
        super(formatMessage(message, lineNumber));
    }

    private static String formatMessage(String message, Integer lineNumber) {
        String errorName = "Operator Error: ";
        String processedMessage = message.contains(errorName) ? message : errorName + message;

        return lineNumber == null ? processedMessage : processedMessage + " at line " + lineNumber;
    }
}
