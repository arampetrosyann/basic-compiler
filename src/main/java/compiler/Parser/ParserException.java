package compiler.Parser;

public class ParserException extends RuntimeException {
  public ParserException(String message) {
    super(formatMessage(message));
  }

  private static String formatMessage(String message) {
    if (message.contains("Parser Error: ")) {
      return message;
    }
    return "Parser Error: " + message;
  }
}
