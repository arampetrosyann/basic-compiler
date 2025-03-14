import compiler.Parser.ParserException;
import org.junit.Test;

import java.io.StringReader;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;
import compiler.utils.parser.ASTNode;

import static org.junit.Assert.assertThrows;

public class TestParser {
        private ASTNode parse(String input) throws ParserException {
            StringReader reader = new StringReader(input);
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            return parser.getAST();
        }

        private void parseWithError(String input) {
            ParserException exception = assertThrows(ParserException.class, () -> {
                parse(input);
            });

            System.out.println("Caught exception: " + exception.getMessage() + " for input: " + input);
        }
        
        @Test
        public void testSimpleAssignment() {
            parse("x = 5;");
        }

        @Test
        public void testArrayAccess() {
            parse("x[5] = 10;");
        }

        @Test
        public void testBinaryExpression() {
            parse("y = a + b * 2;");
        }

        @Test
        public void testBlock() {
            parse("{ x int = 5; y int = x + 2; }");
        }

        @Test
        public void testNestedBlocks() {
            parse("{ x int = 5; { y int = x + 2; { z int = y * 3; } } }");
        }

        @Test
        public void testEmptyBlock() {
            parse("{}");
        }

        @Test
        public void testForLoop() {
            parse("for (i, 1, 10, 1) { writeln(i); }");
        }

        @Test
        public void testFunctionDeclaration() {
            parse("fun add(int a, int b) int { return a + b; }");
        }

        @Test
        public void testFunctionWithoutReturnType() {
            parse("fun writeInt(int a) { writeln(a); }");
        }

        @Test
        public void testFunctionCallInStatement() {
            parse("print(x);");
        }

        @Test
        public void testFunctionCallWithoutArgument() {
            parse("readInt();");
        }

        @Test
        public void testFunctionCallWithMixedArguments() {
            parse("print(42, \"Hello\", true, array [3] of int);");
        }

        @Test
        public void testFunctionCallInExpression() {
            parse("y = sum(a, b) * 2;");
        }

        @Test
        public void testIfStatementWithElse() {
            parse("if (x > 5) { y = 10; } else { y = 0; }");
        }

        @Test
        public void testIfStatementWithoutElse() {
            parse("if (x > 5) { y = 10; }");
        }

        @Test
        public void testParenthesizedExpression() {
            parse("z = (x + y) * 2;");
        }

        @Test
        public void testRecordFieldAccess() {
            parse("person.name = \"John\";");
        }

        @Test
        public void testReturnStatement() {
            parse("return x + y;");
        }

        @Test
        public void testUnaryExpression() {
            parse("z = -x;");
            parse("z bool = !(x);");
        }

        @Test
        public void testUnaryExpressionWithParentheses() {
            parse("z = -(x + y) * 2;");
        }

        @Test
        public void testVariableDeclaration() {
            parse("x int;");
        }

        @Test
        public void testVariableDeclarationWithAssignment() {
            parse("a int = 10;");
        }

        @Test
        public void testFinalVariableDeclaration() {
            parse("final b float = 10.0;");
        }

        @Test
        public void testWhileLoop() {
            parse("while (x < 10) { x = x + 1; }");
        }

        @Test
        public void testArrayCreation() {
            parse("a bool[] = array [2] of bool;");
        }

        @Test
        public void testArrayCreationWithExpression() {
            parse("b int[] = array [x + 3] of int;");
        }

        @Test
        public void testRecordUse() {
            parse("d Person= Person(\"me\", Point(3,7), array [i*2] of int);");
        }

        @Test
        public void testArrayAccessInFunction() {
            parse("return Point(p[0].x+p[1].x, p[0].y+p[1].y);");
        }

        @Test
        public void testLeftSizeAssignment() {
            parse("a[3] = 1234;");
            parse("a.x = 123;");
            parse("a[3].x = 12;");
        }

        @Test
        public void testFree() {
            parse("free x;");
        }

        @Test
        public void testSemanticError() {
            parse("a int = \"Hello\";");
        }

        @Test
        public void testVariableAssignmentError(){
            parseWithError("a int 10;");
        }

        @Test
        public void testVariableDeclarationError(){
            parseWithError("int a;");
        }

        @Test
        public void testMissingSemiColonError(){
            parseWithError("a int = 6");
        }

        @Test
        public void testMissingClosingParenthesesError(){
            parseWithError("a = (5;");
        }

        @Test
        public void testMissingClosingBraceError(){
            parseWithError("{ a = 5;");
        }

        @Test
        public void testMissingClosingBracketError(){
            parseWithError("a[5 = 10;");
        }

        @Test
        public void testMissingRecordFieldError(){
            parseWithError("person. = \"John\";");
        }

        @Test
        public void testMissingRecordKeywordError(){
            parseWithError("Person {");
        }

        @Test
        public void testWhileLoopError(){
            parseWithError("while { x = x + 1; ");
        }

        @Test
        public void testForLoopError(){
            parseWithError("for (i, 1, 10) { writeln(i); }");
        }

        @Test
        public void testArrayCreationError(){
            parseWithError("a bool[] = array [2] of;");
            parseWithError("a bool[] = array [] of bool");
            parseWithError("a bool[] = [2] of bool");
        }

        @Test
        public void testDoWhileLoop() {
            parse("do { x = x + 1; } while (x < 10);");
        }

        @Test
        public void testDoWhileLoopWithBlock() {
            parse("do { x = x + 1; y = y * 2; } while (x < y);");
        }

        @Test
        public void testDoWhileLoopError() {
            parseWithError("do { x = x + 1; } while x < 10;");
        }


}
