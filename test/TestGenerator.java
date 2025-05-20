import compiler.Analyzer;
import compiler.Components.Blocks.Block;
import compiler.Generator;
import compiler.Lexer;
import compiler.Parser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestGenerator {
    private static final String FILES_DIR = "./test/examples";

    private String runCommand(String... command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private String compileAndRunLangProgram(String filename) throws Exception {
        File file = new File(FILES_DIR, filename);
        if (!file.exists()) throw new FileNotFoundException("Missing file: " + file);

        String compileOutput = runCommand("./gradlew", "run", "--args=" + file.getPath());
        System.out.println("Compile output:\n" + compileOutput);

        return runCommand("java", "test");
    }

    private void compileLangFile(String filename) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("./gradlew", "run", "--args=test/examples/" + filename);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {} // consume output
        } catch (IOException ignored) {}

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void cleanUpClassFiles() {
        File dir = new File(".");
        File[] classFiles = dir.listFiles((d, name) -> name.endsWith(".class"));

        if (classFiles != null) {
            for (File file : classFiles) {
                file.delete();
            }
        }
    }

    @Test
    public void testAssignment() throws Exception {
        String output = compileAndRunLangProgram("assignment.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'b = 15'", output.contains("b = 15"));
    }

    @Test
    public void testUnaryExpression() throws Exception {
        String output = compileAndRunLangProgram("unaryExpr.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'c = -1'", output.contains("c = -1"));
    }

    @Test
    public void testBinaryExpression() throws Exception {
        String output = compileAndRunLangProgram("binaryExpr.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'c = 3.8'", output.contains("c = 3.8"));
    }

    @Test
    public void testNestedBlocks() throws Exception {
        String output = compileAndRunLangProgram("nestedBlocks.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'c = 21'", output.contains("c = 21"));
    }

    @Test
    public void testIfStatement() throws Exception {
        String output = compileAndRunLangProgram("ifStatement.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'b = 5'", output.contains("b = 5"));
    }

    @Test
    public void testForLoop() throws Exception {
        String output = compileAndRunLangProgram("forLoop.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'j = 1'", output.contains("j = 1"));
        assertTrue("Expected 'j = 2'", output.contains("j = 2"));
        assertTrue("Expected 'j = 3'", output.contains("j = 3"));
    }

    @Test
    public void testWhileLoop() throws Exception {
        String output = compileAndRunLangProgram("whileLoop.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'i = 3'", output.contains("i = 3"));
    }

    @Test
    public void testWhileLoopFloat() throws Exception {
        String output = compileAndRunLangProgram("whileLoopFloat.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'i = 4.5'", output.contains("i = 4.5"));
    }

    @Test
    public void testDoWhileLoop() throws Exception {
        String output = compileAndRunLangProgram("doWhileLoop.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'x = 10'", output.contains("x = 10"));
    }

    @Test
    public void testRecordUsage() throws Exception {
        String output = compileAndRunLangProgram("record.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'name: me'", output.contains("name: me"));
        assertTrue("Expected 'history[0] = 1'", output.contains("history[0] = 1"));
        assertTrue("Expected 'location (x,y): (3,7)'", output.contains("location (x,y): (3,7)"));
    }

    @Test
    public void testFunctionCall() throws Exception {
        String output = compileAndRunLangProgram("functionCall.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'Result: 3'", output.contains("Result: 3"));
    }

    @Test
    public void testArrayAccess() throws Exception {
        String output = compileAndRunLangProgram("arrayAccess.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'a[0] = 0'", output.contains("a[0] = 0"));
        assertTrue("Expected 'a[1] = 2'", output.contains("a[1] = 2"));
        assertTrue("Expected 'a[2] = 4'", output.contains("a[2] = 4"));
        assertTrue("Expected 'a[3] = 6'", output.contains("a[3] = 6"));
        assertTrue("Expected 'a[4] = 8'", output.contains("a[4] = 8"));
    }

    @Test
    public void testArrayOfRecord() throws Exception {
        String output = compileAndRunLangProgram("arrayOfRecord.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'p0 = (1,2)'", output.contains("p0 = (1,2)"));
        assertTrue("Expected 'p1 = (3,4)'", output.contains("p1 = (3,4)"));
    }

    @Test
    public void testFunctionOfRecord() throws Exception {
        String output = compileAndRunLangProgram("funcOfRecord.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'p = (10.0,20.0)'", output.contains("p = (10.0,20.0)"));
    }

    @Test
    public void testFunctionOfRecordReturn() throws Exception {
        String output = compileAndRunLangProgram("funcOfRecordReturn.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'p = (1.0,2.0)'", output.contains("p = (1.0,2.0)"));
    }

    @Test
    public void testFunctionOfArray() throws Exception {
        String output = compileAndRunLangProgram("funcOfArray.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected '2,4,6'", output.contains("2,4,6"));
    }

    @Test
    public void testFunctionOfArrayOfString() throws Exception {
        String output = compileAndRunLangProgram("funcOfArrayOfString.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'John'", output.contains("John"));
        assertTrue("Expected 'Jake'", output.contains("Jake"));
        assertTrue("Expected 'James'", output.contains("James"));
        assertTrue("Expected 'Jimmy'", output.contains("Jimmy"));
        assertTrue("Expected 'Jack'", output.contains("Jack"));
    }

    @Test
    public void testOperationsFloat() throws Exception {
        String output = compileAndRunLangProgram("operationsFloat.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'b = 15.43'", output.contains("b = 15.43"));
        assertTrue("Expected 'c = 160.9349'", output.contains("c = 160.9349"));
        assertTrue("Expected 'd = 0.6759'", output.contains("d = 0.6759"));
        assertTrue("Expected 'e = -5.0'", output.contains("e = -5.0"));
        assertTrue("Expected 'g = 8.5'", output.contains("g = 8.5"));
    }

    @Test
    public void testWriteAndWriteln() throws Exception {
        String output = compileAndRunLangProgram("writeAndWriteln.lang");

        System.out.println("Program Output:\n" + output);

        assertTrue("Expected 'a = 10.43'", output.contains("a = 10.43"));
        assertTrue("Expected 'b = 43'", output.contains("b = 43"));
        assertTrue("Expected 'c = true'", output.contains("c = true"));
        assertTrue("Expected 'Hello'", output.contains("Hello"));
    }

    @Test
    public void testReadInt() throws Exception {
        String simulatedInput = "42\n";
        compileLangFile("readInt.lang");

        ProcessBuilder pb = new ProcessBuilder("java", "test");
        pb.directory(new File("."));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        ) {
            writer.write(simulatedInput);
            writer.flush();

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
            String result = output.toString();

            System.out.println("Program Output:\n" + result);
            assertTrue(result.contains("You entered: 42"));
        }
    }

    @Test
    public void testReadFloat() throws Exception {
        String simulatedInput = "3.14\n";
        compileLangFile("readFloat.lang");

        ProcessBuilder pb = new ProcessBuilder("java", "test");
        pb.directory(new File("."));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        ) {
            writer.write(simulatedInput);
            writer.flush();

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
            String result = output.toString();

            System.out.println("Program Output:\n" + result);
            assertTrue(result.contains("You entered: 3.14"));
        }
    }

    @Test
    public void testWriteFloat() throws Exception {
        String simulatedInput = "3.14\n";
        compileLangFile("writeFloat.lang");

        ProcessBuilder pb = new ProcessBuilder("java", "test");
        pb.directory(new File("."));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        ) {
            writer.write(simulatedInput);
            writer.flush();

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
            String result = output.toString();

            System.out.println("Program Output:\n" + result);
            assertTrue(result.contains("-x = -3.14"));
        }
    }

    @Test
    public void testReadString() throws Exception {
        String simulatedInput = "LINFO2132 student\n";
        compileLangFile("readString.lang");

        ProcessBuilder pb = new ProcessBuilder("java", "test");
        pb.directory(new File("."));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        ) {
            writer.write(simulatedInput);
            writer.flush();

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            assertEquals(0, exitCode);
            String result = output.toString();

            System.out.println("Program Output:\n" + result);
            assertTrue(result.contains("Hello, LINFO2132 student"));
        }
    }



    @Test
    public void testFactorialWithInput() throws Exception {
        String program = "factorial.lang";
        compileLangFile(program);

        String simulatedInput = "5\n";

        ProcessBuilder pb = new ProcessBuilder("java", "test");
        pb.directory(new File(".")); // working dir must contain `test.class`
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        ) {
            writer.write(simulatedInput);
            writer.flush();

            // Read output
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            assertEquals(0, exitCode);

            String result = output.toString();
            System.out.println("Program Output:\n" + result);

            assertTrue("Should prompt for input", result.contains("Enter a number"));
            assertTrue("Should compute factorial", result.contains("Factorial is: 120"));
        }
    }
}
