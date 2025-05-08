package compiler;

import compiler.Components.Semantic.*;
import compiler.Components.SymbolTableManager;
import compiler.Exceptions.GeneratorException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;

import compiler.Exceptions.Semantic.TypeError;
import compiler.Components.Blocks.*;
import compiler.Components.Token;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.io.*;

public class Generator {
    private static SymbolTableManager symbolTableManager;
    private final String className;
    private final String outputDirectory;
    private ClassWriter classWriter;
    private final Stack<MethodVisitor> methodVisitorStack = new Stack<>();

    public Generator(File file) {
        symbolTableManager = SymbolTableManager.getInstance();

        className = file.getName().split("\\.")[0];
        outputDirectory = file.getParent() == null ? "./" : file.getParent();
    }

    private void createClassFile(String className, byte[] bytecode) {
        Path filePath = Paths.get(outputDirectory, className + ".class");

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(bytecode);
        } catch (Exception e) {
            throw new GeneratorException("Failed to save class file - " + filePath);
        }
    }

    // root
    public void generate(Block ast) {
        VarType mainType = symbolTableManager.getGlobalTable().lookup("main");

        if (!(mainType instanceof FunctionType mainFunctionType)) {
            throw new GeneratorException("No 'main' function found");
        }

        if (mainFunctionType.getParametersCount() > 0) {
            throw new GeneratorException("The function 'main' should not have any parameters");
        }

        classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

        generateBuiltInFunctions();

        // main function
        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        LocalVariablesSorter localVariablesSorter = new LocalVariablesSorter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "([Ljava/lang/String;)V", methodVisitor);

        methodVisitorStack.push(localVariablesSorter);

        methodVisitorStack.peek().visitCode();

        ast.accept(this);

        // call main function
        new FunctionCall("main", List.of()).accept(this);

        methodVisitorStack.peek().visitInsn(Opcodes.RETURN);
        methodVisitorStack.peek().visitMaxs(0, 0);
        methodVisitorStack.peek().visitEnd();

        methodVisitorStack.pop();

        classWriter.visitEnd();

        byte[] bytecode = classWriter.toByteArray();

        createClassFile(className, bytecode);
    }

    // built-ins
    private void generateNegateBoolean() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "!", "(Z)Z", null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ILOAD, 0);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IXOR);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateChr() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "chr", "(I)Ljava/lang/String;", null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ILOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateFloor() {
        MethodVisitor mv = classWriter.visitMethod(
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "floor",
                "(F)I",
                null,
                null
        );

        mv.visitCode();
        mv.visitVarInsn(Opcodes.FLOAD, 0);
        mv.visitInsn(Opcodes.F2D);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Math",
                "floor",
                "(D)D",
                false
        );
        mv.visitInsn(Opcodes.D2I);
        mv.visitInsn(Opcodes.IRETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateLen() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "len", "(Ljava/lang/String;)I", null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateReadInt() {
        // MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "readInt", "()I", null, null);

        // mv.visitCode();

        // @Todo implement this function

        //  mv.visitInsn(Opcodes.IRETURN);
        // mv.visitMaxs(0, 0);
        // mv.visitEnd();
    }

    private void generateReadFloat() {
        //MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "readFloat", "()F", null, null);

        // mv.visitCode();
        // @Todo implement this function

        //  mv.visitInsn(Opcodes.FRETURN);
        // mv.visitMaxs(0, 0);
        // mv.visitEnd();
    }

    private void generateReadString() {
        // MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "readString", "()Ljava/lang/String;", null, null);

        // mv.visitCode();

        // @Todo implement this function

        //  mv.visitInsn(Opcodes.ARETURN);
        // mv.visitMaxs(0, 0);
        // mv.visitEnd();
    }

    private void generateWriteInt() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "writeInt", "(I)V", null, null);

        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(Opcodes.ILOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(I)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateWriteFloat() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "writeFloat", "(F)V", null, null);

        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(Opcodes.FLOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(F)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateWrite() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "write", "(Ljava/lang/String;)V", null, null);

        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateWriteln() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "writeln", "(Ljava/lang/String;)V", null, null);

        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateBuiltInFunctions() {
        generateNegateBoolean();
        generateChr();
        generateFloor();
        generateLen();
        generateReadInt();
        generateReadFloat();
        generateReadString();
        generateWriteInt();
        generateWriteFloat();
        generateWrite();
        generateWriteln();
    }
    //

    private String mapToPrimitive(String id) {
        return switch (id.toLowerCase()) {
            case "int", "integer" -> "I";
            case "float" -> "F";
            case "bool", "boolean" -> "Z";
            case "string" -> "Ljava/lang/String;";
            default -> throw new TypeError("Illegal type " + id);
        };
    }

    private int getStoreOpcode(String typeDescriptor) {
        if (Objects.equals(typeDescriptor, "Ljava/lang/String;")) return Opcodes.ASTORE;

        String firstElem = typeDescriptor.substring(0, 1);

        return switch (firstElem) {
            case "I", "Z" -> Opcodes.ISTORE; // int or boolean
            case "F" -> Opcodes.FSTORE; // float
            case "[", "L" -> Opcodes.ASTORE; // string, array, or record
            default -> throw new GeneratorException("Unsupported type descriptor: " + typeDescriptor);
        };
    }

    private String getTypeDescriptor(Type type) {
        if (type == null) return "V";

        String id = type.getIdentifier();

        return switch (type.getCategory()) {
            case PRIMITIVE -> mapToPrimitive(id);
            case ARRAY -> "[" + getTypeDescriptor(type.getArrayElementType());
            case RECORD -> "L" + id + ";";
        };
    }

    private String getTypeDescriptor(VarType varType) {
        return switch (varType.getName()) {
            case INTEGER, FLOAT, BOOLEAN, STRING -> mapToPrimitive(varType.getName().name());
            case VOID -> "V";
            case ARRAY -> {
                ArrayType arrayType = (ArrayType) varType;

                yield "[" + getTypeDescriptor(arrayType.getElementType());
            }
            case RECORD -> {
                RecordType recordType = (RecordType) varType;

                yield "L" + recordType.getRecordName() + ";";
            }
            case FUNCTION -> ""; // @Todo not handled;
        };
    }

    private String getMethodDescriptor(FunctionCall elem) {
        FunctionType functionType = (FunctionType) symbolTableManager.lookup(elem.getFunctionName());

        StringBuilder descriptor = new StringBuilder("(");

        for (VarType paramType : functionType.getParameters()) {
            descriptor.append(getTypeDescriptor(paramType));
        }

        descriptor.append(")");
        descriptor.append(getTypeDescriptor(functionType.getReturnType()));

        return descriptor.toString();
    }

    private String getMethodDescriptor(Method elem) {
        StringBuilder descriptor = new StringBuilder("(");

        for (Param param : elem.getParameters()) {
            descriptor.append(getTypeDescriptor(param.getType()));
        }

        descriptor.append(")");
        descriptor.append(getTypeDescriptor(elem.getReturnType()));

        return descriptor.toString();
    }

    public void generateBlock(ASTNodeImpl node) {
    }

    private void generateBlock(Expression expr) {
        if (expr instanceof Literal lit) {
            generateBlock(lit);
        } else if (expr instanceof VarReference ref) {
            generateBlock(ref);
        } else if (expr instanceof BinaryExpression bin) {
            generateBlock(bin);
        } else if (expr instanceof ArrayCreation arrCreation) {
            generateBlock(arrCreation);
        } else if (expr instanceof ArrayAccess access) {
            generateBlock(access);
        } else if (expr instanceof FunctionCall call) {
            generateBlock(call);
        } else if (expr instanceof CallExpression callExpr) {
            generateBlock(callExpr);
        } else if (expr instanceof RecordFieldAccess fieldAccess) {
            generateBlock(fieldAccess);
        } else if (expr instanceof UnaryExpression unary) {
            generateBlock(unary);
        } else if (expr instanceof ReturnStatement ret) {
            generateBlock(ret);
        }
    }

    public void generateBlock(Assignment elem) {
        // @Todo
        // use symbolTableManager
    }

    public void generateBlock(BinaryExpression elem) {
        // @Todo
    }

    public void generateBlock(Literal elem) {
        switch (elem.getType().toLowerCase()) {
            case "int":
            case "integer":
                methodVisitorStack.peek().visitLdcInsn(Integer.parseInt(elem.getValue()));
                break;
            case "float":
                methodVisitorStack.peek().visitLdcInsn(Float.parseFloat(elem.getValue()));
                break;
            case "bool":
            case "boolean":
                if (Boolean.parseBoolean(elem.getValue())) {
                    methodVisitorStack.peek().visitInsn(Opcodes.ICONST_1);
                } else {
                    methodVisitorStack.peek().visitInsn(Opcodes.ICONST_0);
                }
                break;
            case "string":
                methodVisitorStack.peek().visitLdcInsn(elem.getValue());
                break;
        }
    }

    public void generateBlock(ArrayCreation elem) {
        generateBlock(elem.getSize());

        if (elem.getElementType().getCategory() == TypeCategory.ARRAY) {
            throw new TypeError("Illegal type when creating an array");
        }

        String desc = getTypeDescriptor(elem.getElementType());
        MethodVisitor mv = methodVisitorStack.peek();

        switch (desc) {
            case "I" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
            case "F" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
            case "Z" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
            default -> {
                String internalName = desc;

                if (internalName.startsWith("L") && internalName.endsWith(";")) {
                    internalName = internalName.substring(1, internalName.length() - 1);
                }

                mv.visitTypeInsn(Opcodes.ANEWARRAY, internalName);
            }
        }
    }

    public void generateBlock(ArrayAccess elem) {
        generateBlock(elem.getIndex());
        methodVisitorStack.peek().visitInsn(Opcodes.IALOAD);
    }

    public void generateBlock(FunctionCall elem) {
        for (Expression arg : elem.getArguments()) {
            generateBlock(arg);
        }

        methodVisitorStack.peek().visitMethodInsn(Opcodes.INVOKESTATIC, className, elem.getFunctionName(), getMethodDescriptor(elem), false);
    }

    public void generateBlock(IfStatement elem) {
        generateBlock(elem.getCondition());

        Label elseLabel = new Label();
        Label endLabel = new Label();

        methodVisitorStack.peek().visitJumpInsn(Opcodes.IFEQ, elseLabel);

        generateBlock(elem.getThenBlock());

        methodVisitorStack.peek().visitJumpInsn(Opcodes.GOTO, endLabel);

        methodVisitorStack.peek().visitLabel(elseLabel);

        if (elem.getElseBlock() != null) {
            generateBlock(elem.getElseBlock());
        }

        methodVisitorStack.peek().visitLabel(endLabel);
    }

    public void generateBlock(ReturnStatement elem) {
        if (elem.getReturnValue() != null) {
            generateBlock(elem.getReturnValue());
            methodVisitorStack.peek().visitInsn(Opcodes.IRETURN);
        } else {
            methodVisitorStack.peek().visitInsn(Opcodes.RETURN);
        }
    }

    public void generateBlock(CallExpression elem) {
        String name = elem.getType();
        VarType lookedUp = symbolTableManager.lookup(name);

        // @Todo
    }

    public void generateBlock(RecordFieldAccess elem) {
        // @Todo
    }

    public void generateBlock(VarReference elem) {
        VarType varType = symbolTableManager.lookup(elem.getName());

        // @Todo
    }

    public void generateBlock(VariableDeclaration elem) {
        String typeDescriptor = getTypeDescriptor(elem.getType());

        MethodVisitor mv = methodVisitorStack.peek();
        LocalVariablesSorter localVariablesSorter = (LocalVariablesSorter) mv;

        if (elem.getValue() != null) {
            generateBlock(elem.getValue());
        } else {
            // set a default value
            switch (elem.getType().getCategory()) {
                case PRIMITIVE:
                    switch (elem.getType().getIdentifier()) {
                        case "int", "integer":
                            methodVisitorStack.peek().visitInsn(Opcodes.ICONST_0);
                            break;
                        case "bool", "boolean":
                            methodVisitorStack.peek().visitInsn(Opcodes.ICONST_0);
                            break;
                        case "float":
                            methodVisitorStack.peek().visitInsn(Opcodes.FCONST_0);
                            break;
                        case "string":
                            methodVisitorStack.peek().visitLdcInsn("");
                            break;
                    }
                    break;
                case ARRAY, RECORD:
                    methodVisitorStack.peek().visitInsn(Opcodes.ACONST_NULL);
                    break;
            }
        }

        org.objectweb.asm.Type asmType = org.objectweb.asm.Type.getType(typeDescriptor);
        int slot = localVariablesSorter.newLocal(asmType);

        int storeOp = getStoreOpcode(typeDescriptor);
        localVariablesSorter.visitVarInsn(storeOp, slot);

        Label start = new Label();
        Label end = new Label();

        localVariablesSorter.visitLabel(start);
        localVariablesSorter.visitLabel(end);

        localVariablesSorter.visitLocalVariable(
                elem.getIdentifier(),
                asmType.getDescriptor(),
                null,
                start,
                end,
                slot
        );
    }

    public void generateBlock(Method elem) {
        String desc = getMethodDescriptor(elem);

        MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, elem.getName(), getMethodDescriptor(elem), null, null);

        LocalVariablesSorter localVariablesSorter = new LocalVariablesSorter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, desc, methodVisitor);

        methodVisitorStack.push(localVariablesSorter);

        methodVisitorStack.peek().visitCode();

        symbolTableManager.enterSymbolTable(elem);

        generateBlock(elem.getBody());

        methodVisitorStack.peek().visitInsn(Opcodes.RETURN);
        methodVisitorStack.peek().visitMaxs(0, 0);
        methodVisitorStack.peek().visitEnd();

        symbolTableManager.leaveSymbolTable();

        methodVisitorStack.pop();
    }

    public void generateBlock(FreeStatement elem) {
        // nothing needed for free statement
    }

    public void generateBlock(UnaryExpression elem) {
        generateBlock(elem.getOperand());

        if (Objects.requireNonNull(elem.getOperator()) == Token.SUBTRACT) {
            methodVisitorStack.peek().visitInsn(Opcodes.INEG);
        }
    }

    public void generateBlock(Type elem) {
    }

    public void generateBlock(RecordDefinition elem) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, elem.getName(), null, "java/lang/Object", null);

        for (RecordField field : elem.getFields()) {
            cw.visitField(Opcodes.ACC_PUBLIC, field.getName(), getTypeDescriptor(field.getType()), null, null).visitEnd();
        }

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();

        byte[] bytecode = cw.toByteArray();

        createClassFile(elem.getName(), bytecode);
    }

    public void generateBlock(Param elem) {
    }

    public void generateBlock(RecordField elem) {
    }

    public void generateBlock(WhileLoop elem) {
        // @Todo
    }

    public void generateBlock(DoWhileLoop elem) {
        // @Todo
    }

    public void generateBlock(ForLoop elem) {
        VarType loopVarType = symbolTableManager.lookup(elem.getVariable());

        // @Todo
    }

    public void generateBlock(Block block) {
        symbolTableManager.enterSymbolTable(block);

        for (Statement stmt : block.getStatements()) {
            stmt.accept(this);
        }

        symbolTableManager.leaveSymbolTable();
    }
}
