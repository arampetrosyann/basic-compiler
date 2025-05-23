package compiler.Exceptions.Semantic;

public class MissingConditionError extends RuntimeException {
    public MissingConditionError(String message) {
        super(formatMessage(message, null));
    }

    public MissingConditionError(String message, int lineNumber) {
        super(formatMessage(message, lineNumber));
    }

    private static String formatMessage(String message, Integer lineNumber) {
        String errorName = "MissingConditionError: ";
        String processedMessage = message.contains(errorName) ? message : errorName + message;

        return lineNumber == null ? processedMessage : processedMessage + " | line number: " + lineNumber;
    }
}
