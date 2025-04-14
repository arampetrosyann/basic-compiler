package compiler.Exceptions;

public class ParserException extends RuntimeException {
  public ParserException(String message) {
    super(formatMessage(message, null));
  }

  public ParserException(String message, int lineNumber) {
    super(formatMessage(message, lineNumber));
  }

  private static String formatMessage(String message, Integer lineNumber) {
    String processedMessage = message.contains("ParserError: ") ? message : "Parser Error: " + message;

    return lineNumber == null ? processedMessage : processedMessage + " at line " + lineNumber;
  }
}
