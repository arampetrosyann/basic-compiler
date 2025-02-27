package compiler;

import compiler.Lexer.Lexer;

import java.io.*;
import java.util.Objects;

public class Compiler {
    public static void main(String[] args) throws IOException {
        if (Objects.equals(args[0], "-lexer")) {
            String filepath = args[1];

            // read the file
            FileInputStream inputStream = new FileInputStream(filepath);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            StringBuilder content = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }

            Lexer lexer = new Lexer(new StringReader(content.toString()));

            while (!lexer.isComplete()) {
                System.out.println(lexer.getNextSymbol());
            }
        }
    }
}
