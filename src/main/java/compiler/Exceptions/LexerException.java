package compiler.Exceptions;

public class LexerException extends RuntimeException {
  public LexerException(String message) {
    super(formatMessage(message, null));
  }

  public LexerException(String message, int lineNumber) {
    super(formatMessage(message, lineNumber));
  }

  private static String formatMessage(String message, Integer lineNumber) {
    String processedMessage = message.contains("LexerError: ") ? message : "Lexer Error: " + message;

    return lineNumber == null ? processedMessage : processedMessage + " | line number: " + lineNumber;
  }
}
