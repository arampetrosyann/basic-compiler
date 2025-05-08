import compiler.Analyzer;
import compiler.Components.Blocks.Block;
import compiler.Generator;
import compiler.Lexer;
import compiler.Parser;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.StringReader;

import static org.junit.Assert.*;

public class TestGenerator {
    private final File testFile = new File("./test.class");

    public void generate(String input) {
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        Block ast = parser.getAST();

        Analyzer analyzer = Analyzer.getInstance();
        analyzer.analyze(ast);

        Generator generator = new Generator(testFile);

        generator.generate(ast);
    }

    @Before
    public void resetAnalyzer() {
        Analyzer.getInstance().reset();
    }

    @After
    public void removeFile() {
        try {
            testFile.delete();
        } catch (Error e) {
            System.out.println(e.getMessage());
        }
    }

    private void generateExpecting(Class<? extends RuntimeException> expectedError, String input) {
        assertThrows(expectedError, () -> generate(input));
    }
}
