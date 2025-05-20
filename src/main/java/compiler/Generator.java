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
    private final Stack<Map<String, Integer>> slotStack = new Stack<>();

    private static Analyzer analyzer;

    public Generator(File file) {
        analyzer = Analyzer.getInstance();
        symbolTableManager = SymbolTableManager.getInstance();

        className = file.getName().split("\\.")[0];
        outputDirectory = file.getParent() == null ? "./" : file.getParent();
    }

    private void createClassFile(String className, byte[] bytecode) {
        Path filePath = Paths.get(outputDirectory, className + ".class");

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(bytecode);
        } catch (Exception e) {
            throw new InternalError("Failed to save class file - " + filePath);
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
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,"readInt","()I",null,null);

        mv.visitCode();

        mv.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateReadFloat() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,"readFloat","()F",null,null);

        mv.visitCode();

        mv.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextFloat", "()F", false);
        mv.visitInsn(Opcodes.FRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateReadString() {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "readString", "()Ljava/lang/String;", null, null);

        mv.visitCode();

        mv.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
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

    private void generateBuiltInFunctions() {
        generateNegateBoolean();
        generateChr();
        generateFloor();
        generateReadInt();
        generateReadFloat();
        generateReadString();
        generateWriteInt();
        generateWriteFloat();
    }

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

    private int getLoadOpcode(String typeDescriptor) {
        if (Objects.equals(typeDescriptor, "Ljava/lang/String;")) return Opcodes.ALOAD;

        String firstElem = typeDescriptor.substring(0, 1);

        return switch (firstElem) {
            case "I", "Z" -> Opcodes.ILOAD;     // int or boolean
            case "F" -> Opcodes.FLOAD;          // float
            case "[", "L" -> Opcodes.ALOAD;     // array or record
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
            case FUNCTION -> {
                FunctionType func = (FunctionType) varType;
                StringBuilder descriptor = new StringBuilder("(");

                for (VarType paramType : func.getParameters()) {
                    descriptor.append(getTypeDescriptor(paramType));
                }

                descriptor.append(")");
                descriptor.append(getTypeDescriptor(func.getReturnType()));

                yield descriptor.toString();
            }
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

    private String getMethodDescriptor(CallExpression elem) {
        String functionName = elem.getType();
        FunctionType functionType = (FunctionType) symbolTableManager.lookup(functionName);

        StringBuilder descriptor = new StringBuilder("(");

        for (VarType paramType : functionType.getParameters()) {
            descriptor.append(getTypeDescriptor(paramType));
        }

        descriptor.append(")");
        descriptor.append(getTypeDescriptor(functionType.getReturnType()));

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
        Expression target = elem.getTarget();
        Expression value = elem.getValue();

        if (target instanceof VarReference ref) {
            String varName = ref.getName();

            generateBlock(value);

            VarType varType = symbolTableManager.lookup(varName);
            String descriptor = getTypeDescriptor(varType);
            int slot = resolveSlot(varName);

            methodVisitorStack.peek().visitVarInsn(getStoreOpcode(descriptor), slot);
            return;
        }

        if (target instanceof ArrayAccess access) {
            MethodVisitor mv = methodVisitorStack.peek();

            generateBlock(access.getArrayExpr());

            // Load index
            generateBlock(access.getIndex());

            generateBlock(value);

            VarType arrayType = analyzer.getType(access.getArrayExpr());
            if (!(arrayType instanceof ArrayType array)) {
                throw new GeneratorException("Expected array type but found: " + arrayType);
            }

            VarType elemType = array.getElementType();
            String typeDesc = getTypeDescriptor(elemType);

            // Store into array
            switch (typeDesc) {
                case "I", "Z" -> mv.visitInsn(Opcodes.IASTORE);
                case "F" -> mv.visitInsn(Opcodes.FASTORE);
                default -> mv.visitInsn(Opcodes.AASTORE);
            }

            return;
        }

        throw new GeneratorException("Unsupported assignment target: " + target.getClass().getSimpleName());
    }

    public void generateBlock(BinaryExpression elem) {
        Expression left = elem.getLeft();
        Expression right = elem.getRight();
        Token op = elem.getOperator();

        // Evaluate left and right operands
        generateBlock(left);
        generateBlock(right);

        MethodVisitor mv = methodVisitorStack.peek();

        VarType leftType = analyzer.getType(left);
        VarType resultType = analyzer.getType(elem);

        TypeName operandType = leftType.getName();
        TypeName resultTypeName = resultType.getName();

        if (resultTypeName == TypeName.BOOLEAN && operandType == TypeName.INTEGER) {
            Label trueLabel = new Label();
            Label endLabel = new Label();

            switch (op) {
                case LESS -> mv.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                case GREATER -> mv.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                case LESS_OR_EQUAL -> mv.visitJumpInsn(Opcodes.IF_ICMPLE, trueLabel);
                case GREATER_OR_EQUAL -> mv.visitJumpInsn(Opcodes.IF_ICMPGE, trueLabel);
                case EQUAL -> mv.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                case NOT_EQUAL -> mv.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                default -> throw new GeneratorException("Unsupported integer comparison: " + op);
            }

            mv.visitInsn(Opcodes.ICONST_0); // false
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_1); // true
            mv.visitLabel(endLabel);
            return;
        }

        if (resultTypeName == TypeName.BOOLEAN && operandType == TypeName.FLOAT) {
            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitInsn(Opcodes.FCMPL);

            switch (op) {
                case LESS -> mv.visitJumpInsn(Opcodes.IFLT, trueLabel);
                case GREATER -> mv.visitJumpInsn(Opcodes.IFGT, trueLabel);
                case EQUAL -> mv.visitJumpInsn(Opcodes.IFEQ, trueLabel);
                case NOT_EQUAL -> mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
                case LESS_OR_EQUAL -> mv.visitJumpInsn(Opcodes.IFLE, trueLabel);
                case GREATER_OR_EQUAL -> mv.visitJumpInsn(Opcodes.IFGE, trueLabel);
                default -> throw new GeneratorException("Unsupported float comparison: " + op);
            }

            mv.visitInsn(Opcodes.ICONST_0); // false
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_1); // true
            mv.visitLabel(endLabel);
            return;
        }


        switch (operandType) {
            case INTEGER -> {
                switch (op) {
                    case ADD -> mv.visitInsn(Opcodes.IADD);
                    case SUBTRACT -> mv.visitInsn(Opcodes.ISUB);
                    case MULTIPLY -> mv.visitInsn(Opcodes.IMUL);
                    case DIVIDE -> mv.visitInsn(Opcodes.IDIV);
                    case MODULO -> mv.visitInsn(Opcodes.IREM);
                    default -> throw new GeneratorException("Unsupported int operator: " + op);
                }
            }

            case FLOAT -> {
                switch (op) {
                    case ADD -> mv.visitInsn(Opcodes.FADD);
                    case SUBTRACT -> mv.visitInsn(Opcodes.FSUB);
                    case MULTIPLY -> mv.visitInsn(Opcodes.FMUL);
                    case DIVIDE -> mv.visitInsn(Opcodes.FDIV);
                    case MODULO -> mv.visitInsn(Opcodes.FREM);
                    default -> throw new GeneratorException("Unsupported float operator: " + op);
                }
            }

            case BOOLEAN -> {
                Label trueLabel = new Label();
                Label endLabel = new Label();

                switch (op) {
                    case LOGICAL_AND -> {
                        mv.visitJumpInsn(Opcodes.IFEQ, endLabel);
                        generateBlock(right); // re-evaluate right side
                        mv.visitJumpInsn(Opcodes.IFEQ, endLabel);
                        mv.visitInsn(Opcodes.ICONST_1);
                        mv.visitJumpInsn(Opcodes.GOTO, trueLabel);
                        mv.visitLabel(endLabel);
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitLabel(trueLabel);
                    }

                    case LOGICAL_OR -> {
                        mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
                        generateBlock(right);
                        mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                        mv.visitLabel(trueLabel);
                        mv.visitInsn(Opcodes.ICONST_1);
                        mv.visitLabel(endLabel);
                    }

                    default -> throw new GeneratorException("Unsupported bool operator: " + op);
                }
            }

            default -> throw new GeneratorException("Unsupported operand type in binary expression: " + operandType);
        }
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
            throw new TypeError("Illegal type when creating an array. Nested arrays are not supported", elem.getLineNumber());
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
        MethodVisitor mv = methodVisitorStack.peek();

        generateBlock(elem.getArrayExpr());

        generateBlock(elem.getIndex());

        VarType arrayType = analyzer.getType(elem.getArrayExpr());
        if (!(arrayType instanceof ArrayType array)) {
            throw new GeneratorException("Expected array type but found: " + arrayType);
        }

        VarType elemType = array.getElementType();
        String typeDesc = getTypeDescriptor(elemType);

        switch (typeDesc) {
            case "I", "Z" -> mv.visitInsn(Opcodes.IALOAD);
            case "F" -> mv.visitInsn(Opcodes.FALOAD);
            default -> mv.visitInsn(Opcodes.AALOAD); // String, records
        }
    }

    public void generateBlock(FunctionCall elem) {
        String name = elem.getFunctionName();
        MethodVisitor mv = methodVisitorStack.peek();

        switch (name) {
            case "readInt" -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, className, "readInt", "()I", false);
            case "readFloat" -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, className, "readFloat", "()F", false);
            case "readString" -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, className, "readString", "()Ljava/lang/String;", false);
            case "write" -> {
                Expression arg = elem.getArguments().getFirst();
                VarType argType = analyzer.getType(arg);
                String descriptor = getTypeDescriptor(argType);

                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                generateBlock(arg);

                switch (descriptor) {
                    case "I" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(I)V", false);
                    case "Z" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Z)V", false);
                    case "F" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(F)V", false);
                    case "Ljava/lang/String;" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
                    default -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/Object;)V", false);
                }
            }
            case "writeln" -> {
                Expression arg = elem.getArguments().getFirst();
                VarType argType = analyzer.getType(arg);
                String descriptor = getTypeDescriptor(argType);

                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                generateBlock(arg);

                switch (descriptor) {
                    case "I" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
                    case "F" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
                    case "Z" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
                    case "Ljava/lang/String;" -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    default -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
                }
            }
            default -> {
                // Normal function calls
                for (Expression arg : elem.getArguments()) {
                    generateBlock(arg);
                }

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        className,
                        name,
                        getMethodDescriptor(elem),
                        false
                );
            }
        }
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

            VarType returnType = analyzer.getType(elem.getReturnValue());
            String descriptor = getTypeDescriptor(returnType);

            switch (descriptor.charAt(0)) {
                case 'I', 'Z' -> methodVisitorStack.peek().visitInsn(Opcodes.IRETURN); // int, bool
                case 'F' -> methodVisitorStack.peek().visitInsn(Opcodes.FRETURN);     // float
                case 'L', '[' -> methodVisitorStack.peek().visitInsn(Opcodes.ARETURN); // record, string, array
                default -> throw new GeneratorException("Unsupported return type: " + descriptor);
            }
        } else {
            methodVisitorStack.peek().visitInsn(Opcodes.RETURN);
        }
    }

    public void generateBlock(CallExpression elem) {
        String name = elem.getType();

        VarType type = symbolTableManager.lookup(name);

        if (type instanceof RecordType recordType) {
            String internalName = recordType.getRecordName();
            MethodVisitor mv = methodVisitorStack.peek();

            // Allocate new record object
            mv.visitTypeInsn(Opcodes.NEW, internalName);
            mv.visitInsn(Opcodes.DUP);

            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", "()V", false);

            List<String> fieldNames = recordType.getFieldNames();
            List<Expression> args = elem.getArguments();

            for (int i = 0; i < fieldNames.size(); i++) {
                mv.visitInsn(Opcodes.DUP); // keep reference on stack
                generateBlock(args.get(i)); // push field value
                String fieldName = fieldNames.get(i);
                VarType fieldType = recordType.getFieldValue(fieldName);
                mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, fieldName, getTypeDescriptor(fieldType));
            }

            return;
        }

        // Generate bytecode for all arguments
        for (Expression arg : elem.getArguments()) {
            generateBlock(arg);
        }

        // Handle built-in read functions
        switch (name) {
            case "readInt" -> methodVisitorStack.peek().visitMethodInsn(
                    Opcodes.INVOKESTATIC, className, "readInt", "()I", false);
            case "readFloat" -> methodVisitorStack.peek().visitMethodInsn(
                    Opcodes.INVOKESTATIC, className, "readFloat", "()F", false);
            case "readString" -> methodVisitorStack.peek().visitMethodInsn(
                    Opcodes.INVOKESTATIC, className, "readString", "()Ljava/lang/String;", false);
            case "len" -> {
                Expression argExpr = elem.getArguments().get(0);
                VarType argType = analyzer.getType(argExpr);

                generateBlock(argExpr);

                MethodVisitor mv = methodVisitorStack.peek();

                if (argType.equals(PrimitiveType.STRING)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
                } else if (argType instanceof ArrayType) {
                    mv.visitInsn(Opcodes.ARRAYLENGTH);
                } else {
                    throw new GeneratorException("Unsupported type for len(): " + argType);
                }
            }
            default -> methodVisitorStack.peek().visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    className,
                    name,
                    getMethodDescriptor(elem),
                    false
            );
        }
    }

    public void generateBlock(RecordFieldAccess elem) {
        MethodVisitor mv = methodVisitorStack.peek();

        generateBlock(elem.getRecord());

        RecordType recordType = elem.getRecordType();
        String internalName = recordType.getRecordName();
        String fieldName = elem.getFieldName();
        VarType fieldType = recordType.getFieldValue(fieldName);
        String descriptor = getTypeDescriptor(fieldType);

        mv.visitFieldInsn(Opcodes.GETFIELD, internalName, fieldName, descriptor);
    }



    public void generateBlock(VarReference elem) {
        VarType varType = symbolTableManager.lookup(elem.getName());

        if (varType == null) {
            throw new GeneratorException("Variable '" + elem.getName() + "' not found in scope");
        }

        String typeDescriptor = getTypeDescriptor(varType);
        int loadOpcode = getLoadOpcode(typeDescriptor);

        int slot = resolveSlot(elem.getName());
        methodVisitorStack.peek().visitVarInsn(loadOpcode, slot);
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
        slotStack.peek().put(elem.getIdentifier(), slot);

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
        slotStack.push(new HashMap<>());

        // Assign slots for the parameters
        int incomingSlot = 0;
        for (Param param : elem.getParameters()) {
            String typeDesc = getTypeDescriptor(param.getType());
            org.objectweb.asm.Type asmType = org.objectweb.asm.Type.getType(typeDesc);

            int localSlot = localVariablesSorter.newLocal(asmType);
            slotStack.peek().put(param.getName(), localSlot);

            int loadOp = getLoadOpcode(typeDesc);
            int storeOp = getStoreOpcode(typeDesc);

            methodVisitorStack.peek().visitVarInsn(loadOp, incomingSlot);
            methodVisitorStack.peek().visitVarInsn(storeOp, localSlot);

            Label start = new Label();
            Label end = new Label();

            localVariablesSorter.visitLabel(start);
            localVariablesSorter.visitLabel(end);

            localVariablesSorter.visitLocalVariable(param.getName(), asmType.getDescriptor(), null, start, end, localSlot);

            incomingSlot += asmType.getSize();
        }


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
            VarType type = analyzer.getType(elem.getOperand());

            switch (type.getName()) {
                case INTEGER -> methodVisitorStack.peek().visitInsn(Opcodes.INEG);
                case FLOAT -> methodVisitorStack.peek().visitInsn(Opcodes.FNEG);
                default -> throw new GeneratorException("Unsupported unary negation for type: " + type.getName());
            }
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
        MethodVisitor mv = methodVisitorStack.peek();

        Label loopStart = new Label();
        Label loopEnd = new Label();

        mv.visitLabel(loopStart);
        generateBlock(elem.getCondition());
        mv.visitJumpInsn(Opcodes.IFEQ, loopEnd);
        generateBlock(elem.getBody());

        mv.visitJumpInsn(Opcodes.GOTO, loopStart);

        mv.visitLabel(loopEnd);
    }

    public void generateBlock(DoWhileLoop elem) {
        MethodVisitor mv = methodVisitorStack.peek();

        Label loopStart = new Label();
        Label loopCondition = new Label();

        mv.visitLabel(loopStart);

        generateBlock(elem.getBody());

        mv.visitLabel(loopCondition);

        generateBlock(elem.getCondition());

        // if the condition is true, jump back to the start of the body
        mv.visitJumpInsn(Opcodes.IFNE, loopStart);
    }

    public void generateBlock(ForLoop elem) {
        MethodVisitor mv = methodVisitorStack.peek();

        String varName = elem.getVariable();

        generateBlock(elem.getStart());
        VarType varType = analyzer.getType(elem.getStart());
        String typeDescriptor = getTypeDescriptor(varType);
        int slot = ((LocalVariablesSorter) mv).newLocal(org.objectweb.asm.Type.getType(typeDescriptor));
        slotStack.peek().put(varName, slot);

        int storeOpcode = getStoreOpcode(typeDescriptor);
        mv.visitVarInsn(storeOpcode, slot);

        Label loopStart = new Label();
        Label loopEnd = new Label();

        mv.visitLabel(loopStart);

        // Load loop variable
        int loadOpcode = getLoadOpcode(typeDescriptor);
        mv.visitVarInsn(loadOpcode, slot);

        // Push max value
        generateBlock(elem.getMaxValue());

        // check if iterator reached its max value to exit loop
        switch (varType.getName()) {
            case INTEGER -> mv.visitJumpInsn(Opcodes.IF_ICMPGE, loopEnd);
            case FLOAT -> {
                mv.visitInsn(Opcodes.FCMPG);
                mv.visitJumpInsn(Opcodes.IFGE, loopEnd);
            }
            default -> throw new GeneratorException("Unsupported for-loop type: " + varType.getName());
        }

        generateBlock(elem.getBody());

        // add step to iterator
        mv.visitVarInsn(loadOpcode, slot);
        generateBlock(elem.getStep());

        switch (varType.getName()) {
            case INTEGER -> mv.visitInsn(Opcodes.IADD);
            case FLOAT -> mv.visitInsn(Opcodes.FADD);
            default -> throw new GeneratorException("Unsupported for-loop type: " + varType.getName());
        }

        mv.visitVarInsn(storeOpcode, slot); // store updated iterator variable
        mv.visitJumpInsn(Opcodes.GOTO, loopStart);

        mv.visitLabel(loopEnd);
    }

    public void generateBlock(Block block) {
        symbolTableManager.enterSymbolTable(block);
        slotStack.push(new HashMap<>());

        for (Statement stmt : block.getStatements()) {
            stmt.accept(this);
        }

        slotStack.pop();
        symbolTableManager.leaveSymbolTable();
    }

    private int resolveSlot(String name) {
        for (int i = slotStack.size() - 1; i >= 0; i--) {
            if (slotStack.get(i).containsKey(name)) {
                return slotStack.get(i).get(name);
            }
        }
        throw new GeneratorException("Slot not found for variable: " + name);
    }
}
