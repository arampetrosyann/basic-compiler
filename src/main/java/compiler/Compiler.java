package compiler;

import compiler.Analyzer.Analyzer;
import compiler.Components.Blocks.ASTPrinter;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;
import compiler.Components.Blocks.ASTNodeImpl;

import java.io.*;
import java.util.Objects;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String mode = args.length > 1 ? args[0] : "-analysis";
        String filepath = args.length > 1 ? args[1] : args[0];

        // read the file
        FileInputStream inputStream = new FileInputStream(filepath);
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        StringBuilder content = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line).append("\n");
        }

        try {
            Lexer lexer = new Lexer(new StringReader(content.toString()));

            if (Objects.equals(mode, "-lexer")) {
                while (!lexer.isComplete()) {
                    System.out.println(lexer.getNextSymbol());
                }
            }
            else if (Objects.equals(mode, "-parser")) {
                Parser parser = new Parser(lexer);

                ASTNodeImpl ast = parser.getAST();
                ASTPrinter printer = new ASTPrinter();
                printer.printAST(ast);

            } else if (Objects.equals(mode, "-analysis")) {
                Parser parser = new Parser(lexer);

                ASTNodeImpl ast = parser.getAST();

                Analyzer analyzer = Analyzer.getInstance();
                analyzer.analyze(ast);
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }
}
