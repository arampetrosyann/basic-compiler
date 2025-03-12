import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.StringReader;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;
import compiler.utils.parser.ASTNode;

public class TestParser {
    @Test
    public void testSimpleAssignment() throws Exception {
        String input = "x = 5;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode ast = parser.getAST();
        assertNotNull(ast);
        System.out.println(ast);  // Debug output
    }

    @Test
    public void testFunctionDeclaration() throws Exception {
        String input = "fun add(int a, int b) int { return a + b; }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode ast = parser.getAST();
        assertNotNull(ast);
        System.out.println(ast);  // Debug output
    }

        private ASTNode parse(String input) throws Exception {
            StringReader reader = new StringReader(input);
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            ASTNode ast = parser.getAST();
            assertNotNull(ast);
            System.out.println(ast);  // Debug output
            return ast;
        }

        @Test
        public void testArrayAccess() throws Exception {
            parse("x[5] = 10;");
        }

        @Test
        public void testBinaryExpression() throws Exception {
            parse("y = a + b * 2;");
        }

        @Test
        public void testBlock() throws Exception {
            parse("{ x int = 5; y int = x + 2; }");
        }

        @Test
        public void testNestedBlocks() throws Exception {
            parse("{ x int = 5; { y int = x + 2; { z int = y * 3; } } }");
        }

        @Test
        public void testEmptyBlock() throws Exception {
            parse("{}");
        }

        @Test
        public void testForLoop() throws Exception {
            parse("for (i, 1, 10, 1) { writeln(i); }");
        }

        @Test
        public void testFunctionWithoutReturnType() throws Exception {
            parse("fun writeInt(int a) { writeln(a); }");
        }

        @Test
        public void testFunctionCallInStatement() throws Exception {
            parse("print(x);");
        }

        @Test
        public void testFunctionCallWithoutArgument() throws Exception {
            parse("readInt();");
        }

        @Test
        public void testFunctionCallWithMixedArguments() throws Exception {
            parse("print(42, \"Hello\", true, array [3] of int);");
        }

        @Test
        public void testFunctionCallInExpression() throws Exception {
            parse("y = sum(a, b) * 2;");
        }

        @Test
        public void testIfStatementWithElse() throws Exception {
            parse("if (x > 5) { y = 10; } else { y = 0; }");
        }

        @Test
        public void testIfStatementWithoutElse() throws Exception {
            parse("if (x > 5) { y = 10; }");
        }

        @Test
        public void testParenthesizedExpression() throws Exception {
            parse("z = (x + y) * 2;");
        }

        @Test
        public void testRecordFieldAccess() throws Exception {
            parse("person.name = \"John\";");
        }

        @Test
        public void testReturnStatement() throws Exception {
            parse("return x + y;");
        }

        @Test
        public void testUnaryExpression() throws Exception {
            parse("z = -x;");
        }

        @Test
        public void testUnaryExpression2() throws Exception {
            parse("z bool = !(x);");
        }

        @Test
        public void testUnaryExpressionWithParentheses() throws Exception {
            parse("z = -(x + y) * 2;");
        }

        @Test
        public void testVariableDeclaration() throws Exception {
            parse("a int = 10;");
        }

        @Test
        public void testFinalVariableDeclaration() throws Exception {
            parse("final b float = 10.0;");
        }

        @Test
        public void testVariableDeclaration2() throws Exception {
            parse("x int;");
        }

        @Test
        public void testWhileLoop() throws Exception {
            parse("while (x < 10) { x = x + 1; }");
        }

        @Test
        public void testArrayCreation() throws Exception {
            parse("a bool[] = array [2] of bool;");
        }

        @Test
        public void testArrayCreationWithExpression() throws Exception {
            parse("b int[] = array [x + 3] of int;");
        }

        @Test
        public void testRecordUse() throws Exception {
            parse("d Person= Person(\"me\", Point(3,7), array [i*2] of int);");
        }

        @Test
        public void testArrayAccessInFunction() throws Exception {
            parse("return Point(p[0].x+p[1].x, p[0].y+p[1].y);");
        }

        @Test
        public void testLeftSizeAssignment() throws Exception {
            parse("a[3] = 1234;");
            parse("a.x = 123;");
            parse("a[3].x = 12;");
        }

        @Test
        public void testFree() throws Exception{
            parse("free x;");
        }

        @Test
        public void testSemanticError() throws Exception {
            parse("a int = \"Hello\";");
        }
}
