package compiler.Exceptions;

public class GeneratorException extends RuntimeException {
  public GeneratorException(String message) {
    super(formatMessage(message, null));
  }

  public GeneratorException(String message, int lineNumber) {
    super(formatMessage(message, lineNumber));
  }

  private static String formatMessage(String message, Integer lineNumber) {
    String processedMessage = message.contains("GeneratorError: ") ? message : "GeneratorError: " + message;

    return lineNumber == null ? processedMessage : processedMessage + " | line number: " + lineNumber;
  }
}
