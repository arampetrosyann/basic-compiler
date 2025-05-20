package compiler;

import compiler.Components.Blocks.Block;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String sourceFilepath = args[0];
        String targetFilepath = args.length > 2 ? args[2] : "test.class";

        // read the file
        FileInputStream inputStream = new FileInputStream(sourceFilepath);
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        StringBuilder content = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line).append("\n");
        }

        try {
            Lexer lexer = new Lexer(new StringReader(content.toString()));
            Parser parser = new Parser(lexer);

            Block ast = parser.getAST();

            Analyzer analyzer = Analyzer.getInstance();

            analyzer.analyze(ast);

            File targetFile = new File(targetFilepath);

            if (targetFile.getParentFile() != null) {
                targetFile.getParentFile().mkdirs();
            }

            Generator generator = new Generator(targetFile);
            generator.generate(ast);

            System.out.println("Done!!!");
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }
}
