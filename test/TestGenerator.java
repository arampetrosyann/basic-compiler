import compiler.Analyzer;
import compiler.Components.Blocks.Block;
import compiler.Generator;
import compiler.Lexer;
import compiler.Parser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.stream.Collectors;

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

    public String disassemble() throws Exception {
        Process process = new ProcessBuilder("javap", "-c", "test.class")
                .redirectErrorStream(true)
                .start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    public String getConstantPool() throws Exception {
        Process process = new ProcessBuilder("javap", "-v", "test.class")
                .redirectErrorStream(true)
                .start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private void expectBytecode(String output, String expectedInstruction) {
        assertTrue("Expected bytecode to contain " + expectedInstruction, output.contains(expectedInstruction));
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

    @Test
    public void testConstantPoolDump() throws Exception {
        String code = """
                fun main() {
                    s string = "hello";
                    x int = 123;
                }
                """;

        generate(code);

        String output = getConstantPool();

        assertTrue("Expected 'hello' in constant pool", output.contains("hello"));
        assertTrue("Expected 123 in constant pool", output.contains("123"));
    }

    @Test
    public void testGenerate_ValidAssignment() throws Exception {
        String code = """
                fun main() {
                    a int = 10;
                    b int = a + 5;
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "istore_1");
        expectBytecode(output, "iload_1");
        expectBytecode(output, "iadd");
    }

    @Test
    public void testGenerate_ValidUnaryxpression() throws Exception {
        String code = """
                fun main() {
                    a int = 1;
                    c int = -a;
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "iload_1");
        expectBytecode(output, "ineg");
    }

    @Test
    public void testGenerate_ValidBinaryExpression() throws Exception {
        String code = """
                fun main() {
                    a int = 1;
                    b int = 2;
                    c int = a + b;
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "iadd");
    }

    @Test
    public void testGenerate_ValidNestedBlocks() throws Exception {
        String code = """
                fun main() {
                    a int = 5;
                    {
                        b int = a + 2;
                        {
                            c int = b * 3;
                        }
                    }
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "imul");
    }

    @Test
    public void testGenerate_ValidIf() throws Exception {
        String code = """
                fun main() {
                    a bool = true;
                    if (a) {
                        b int = 10;
                    } else {
                       b int = 5;
                    }
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "ifeq");
        expectBytecode(output, "goto");
    }

    @Test
    public void testGenerate_ValidForLoop() throws Exception {
        String code = """
                fun main() {
                    i int = 0;
                    for (i, 0, 10, 1) {
                        x int = 2;
                    }
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "goto");
    }

    @Test
    public void testGenerate_ValidForLoop_Float() throws Exception {
        String code = """
                fun main() {
                    i float = 0.0;
                    for (i, 0.0, 5.0, 0.5) {
                        x int = 2;
                    }
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "goto");
    }

    @Test
    public void testGenerate_ValidWhileLoop() throws Exception {
        String code = """
                fun main() {
                    i int = 0;
                    while (i < 3) {
                        i = i + 1;
                    }
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "goto");
    }

    @Test
    public void testGenerate_ValidDoWhileLoop() throws Exception {
        String code = """
                fun main(){
                    x int = 0;
                    do {
                        x = x + 1;
                    } while (x < 10);
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "goto");
    }

    @Test
    public void testGenerate_ValidRecordUsage() throws Exception {
        String code = """
                fun main() {
                    Point rec {
                        x int;
                        y int;
                    }
                    Person rec {
                        name string;
                        location Point;
                        history int[];
                    }
                    i int = 2;
                    d Person = Person("me", Point(3, 7), array [i*2] of int);
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "class Person");
        expectBytecode(output, "new");
        expectBytecode(output, "putfield");
    }

    @Test
    public void testGenerate_ValidFunctionCall() throws Exception {
        String code = """
                fun add(a int, b int) int {
                        return a + b;
                    }
                
                fun main() {
                    result int = add(1, 2);
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "invokestatic");
    }

    @Test
    public void testGenerate_ValidFunctionCallVoid() throws Exception {
        String code = """
                fun printIt(a int) {
                        writeInt(a);
                    }
                
                fun main() {
                    printIt(5);
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "invokestatic");
    }

    @Test
    public void testGenerate_ValidArrayAccess() throws Exception {
        String code = """
                fun main() {
                    a int[] = array [5] of int;
                    a[2] = 2;
                    x int = a[2];
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "iaload");
    }

    @Test
    public void testGenerate_WriteInt() throws Exception {
        String code = """
                fun main() {
                    writeInt(1);
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "i");
        expectBytecode(output, "ldc");
        expectBytecode(output, "writeInt");
    }

    @Test
    public void testGenerate_WriteFloat() throws Exception {
        String code = """
                fun main() {
                    writeFloat(2.5);
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "2.5");
        expectBytecode(output, "ldc");
        expectBytecode(output, "writeFloat");
    }

    @Test
    public void testGenerate_WriteString() throws Exception {
        String code = """
                fun main() {
                    write("Hello!");
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "Hello");
        expectBytecode(output, "ldc");
        expectBytecode(output, "write");
    }

    @Test
    public void testGenerate_ReadInt() throws Exception {
        String code = """
                fun main() {
                    i int = readInt();
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "invokestatic");
        expectBytecode(output, "istore");
        expectBytecode(output, "readInt");
    }

    @Test
    public void testGenerate_ReadFloat() throws Exception {
        String code = """
                fun main() {
                    i float = readFloat();
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "invokestatic");
        expectBytecode(output, "fstore");
        expectBytecode(output, "readFloat");
    }

    @Test
    public void testGenerate_ReadString() throws Exception {
        String code = """
                fun main() {
                    name string = readString();
                }
                """;

        generate(code);
        String output = disassemble();

        expectBytecode(output, "invokestatic");
        expectBytecode(output, "astore");
        expectBytecode(output, "readString");
    }
}
