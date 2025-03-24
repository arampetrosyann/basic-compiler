package compiler;

import compiler.Lexer.Lexer;
import compiler.Parser.Parser;
import compiler.Components.Blocks.ASTNodeImpl;

import java.io.*;
import java.util.Objects;

public class Compiler {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java Compiler -lexer filepath OR java Compiler -parser filepath");
            return;
        }

        String mode = args[0];
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

        if (Objects.equals(mode, "-lexer")) {
            while (!lexer.isComplete()) {
                System.out.println(lexer.getNextSymbol());
            }
        }
        else if (Objects.equals(mode, "-parser")) {
            Parser parser = new Parser(lexer);

            ASTNodeImpl ast = parser.getAST();
            ast.printAST(0);
        }
    }
}
