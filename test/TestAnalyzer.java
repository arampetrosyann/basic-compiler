import compiler.Analyzer.Analyzer;
import compiler.Components.Blocks.ASTNodeImpl;
import compiler.Exceptions.Semantic.*;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertThrows;

public class TestAnalyzer {
    private void analyze(String input) {
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNodeImpl ast = parser.getAST();
        Analyzer analyzer = Analyzer.getInstance();
        analyzer.analyze(ast);
    }

    private void analyzeExpecting(Class<? extends RuntimeException> expectedError, String input) {
        assertThrows(expectedError, () -> analyze(input));
    }

    @Before
    public void resetAnalyzer() {
        Analyzer.getInstance().reset();
    }

    // Assignment

    @Test
    public void testValidAssignment() {
        analyze("a int = 10; b int = a + 5;");
    }

    @Test
    public void testMismatchedAssignment() {
        analyzeExpecting(TypeError.class, "a int = \"hello\";");
    }

    @Test
    public void testUndeclaredVariableReference() {
        analyzeExpecting(ScopeError.class, "a = 5;");
    }

    // If statement

    @Test
    public void testIfWithNonBooleanCondition() {
        analyzeExpecting(MissingConditionError.class, "a int = 5; if (a) { b int = 10; }");
    }

    @Test
    public void testIfWithBooleanCondition() {
        analyze("a bool = true; if (a) { b int = 10; }");
    }

    // Loops

    @Test
    public void testForLoopWithMismatchedTypes() {
        analyzeExpecting(TypeError.class, """
            i int = 0;
            for (i, 0, 10.0, 1) { x int = 2; }
        """);
    }

    @Test
    public void testForLoopWithCorrectTypes() {
        analyze("""
            i int = 0;
            for (i, 0, 10, 1) { x int = 2; }
        """);
    }

    @Test
    public void testWhileWithNonBooleanCondition() {
        analyzeExpecting(MissingConditionError.class, "x int = 0; while (x) { x = x + 1; }");
    }

    @Test
    public void testWhileWithBooleanCondition() {
        analyze("x bool = true; while (x) { x = false; }");
    }

    @Test
    public void testValidDoWhileLoop() {
        analyze("""
        x int = 0;
        do {
            x = x + 1;
        } while (x < 10);
    """);
    }

    @Test
    public void testDoWhileNonBooleanCondition() {
        analyzeExpecting(MissingConditionError.class, """
        x int = 0;
        do {
            x = x + 1;
        } while (x);
    """);
    }

    @Test
    public void testDoWhileUsesUndeclaredVariable() {
        analyzeExpecting(ScopeError.class, """
        do {
            x = 5;
        } while (true);
    """);
    }

    // Binary Expressions

    @Test
    public void testBinaryExpressionMismatch() {
        analyzeExpecting(OperatorError.class, """
            a int = 10;
            b string = "hi";
            c int = a + b;
        """);
    }

    @Test
    public void testValidBinaryExpression() {
        analyze("a int = 1; b int = 2; c int = a + b;");
    }

    // Blocks and scopes

    @Test
    public void testValidNestedBlocks() {
        analyze("""
            a int = 5;
            {
                b int = a + 2;
                {
                    c int = b * 3;
                }
            }
        """);
    }

    @Test
    public void testScopeErrorInNestedBlock() {
        analyzeExpecting(ScopeError.class, """
            {
                x int = 1;
            }
            y int = x + 2;
        """);
    }

    @Test
    public void testScopeInNestedBlockSameVarName() {
        analyze("""
            {
                            a int = 10;
                            {
                                a int = 20;
                            }
                        }
        """);
    }

    @Test
    public void testScopeInNestedBlockSameVarNameDifType() {
        analyze("""
            {
                            a int = 10;
                            {
                                a string = "Hello";
                            }
                            b int = a + 1;
                        }
        """);
    }

    @Test
    public void testScopeErrorInNestedBlockSameVarNameDifType() {
        analyzeExpecting(OperatorError.class, """
            {
                            a string = "Hello";
                            {
                                a int = 10;
                            }
                            b int = a + 1;
                        }
        """);
    }

    // Records

    @Test
    public void testValidRecordDefinition() {
        analyze("Person rec { name string; age int; }");
    }

    @Test
    public void testRecordUsage() {
        analyze("""
            Point rec {
                x int;
                y int;
            }
        
            Person rec{
                name string;
                location Point;
                history int[];
            }
            
            i int = 2;
            d Person= Person("me", Point(3,7), array [i*2] of int);
        """);
    }

    @Test
    public void testRecordUsageInsideFunction() {
        analyze("""
            Point rec {
                x int;
                y int;
                }
        
                fun copyPoints(p Point[]) Point {
                  return Point(p[0].x+p[1].x, p[0].y+p[1].y);
                }
        """);
    }

    // Functions

    @Test
    public void testValidFunctionDeclarationAndCallExpression() {
        analyze("""
            fun add(a int, b int) int {
                return a + b;
            }
            result int = add(1, 2);
        """);
    }

    @Test
    public void testValidFunctionDeclarationAndFunctionCall() {
        analyze("""
            fun add(a int, b int) {
                c int = a + b;
            }
            add(1, 2);
        """);
    }

    @Test
    public void testFunctionArgumentCountMismatch() {
        analyzeExpecting(ArgumentError.class, """
            fun printOne(x int) {
                return;
            }
            printOne();
        """);
    }

    @Test
    public void testFunctionArgumentTypeMismatch() {
        analyzeExpecting(ArgumentError.class, """
            fun square(x int) int {
                return x * x;
            }
            result int = square("hello");
        """);
    }

    // Arrays

    @Test
    public void testValidArrayAccess() {
        analyze("""
            a int[] = array [5] of int;
            x int = a[2];
        """);
    }

    @Test
    public void testArrayAccessWithNonIntegerIndex() {
        analyzeExpecting(TypeError.class, """
            a int[] = array [5] of int;
            x int = a["two"];
        """);
    }

    @Test
    public void testArrayElementAssignmentTypeMismatch() {
        analyzeExpecting(TypeError.class, """
            nums int[] = array [3] of int;
            nums[0] = "hi";
        """);
    }

    // Usage of built-in functions

    @Test
    public void testReadInt() {
        analyze("i int = readInt();");
    }

    @Test
    public void testReadFloat() {
        analyze("i float = readFloat();");
    }

    @Test
    public void testReadString() {
        analyze("name string = readString();");
    }

    @Test
    public void testWriteln() {
        analyze("writeln(1);");
    }

    @Test
    public void testWrite() {
        analyze("""
            write("Hello, World!!");
        """);
    }

    @Test
    public void testWriteInt() {
        analyze("writeInt(1);");
    }

    @Test
    public void testWriteFloat() {
        analyze("writeFloat(2.6);");
    }
}
