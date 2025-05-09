package compiler;

import compiler.Exceptions.ParserException;
import compiler.Components.Symbol;
import compiler.Components.Token;
import compiler.Components.Blocks.*;
import compiler.Exceptions.Semantic.TypeError;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Symbol lookahead;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.lookahead = lexer.getNextSymbol();
    }

    private Symbol match(Token expectedToken) throws ParserException {
        if (lookahead.getToken() != expectedToken) {
            throw new ParserException("Expected " + expectedToken + " but found " + lookahead.getToken(), lookahead.getLineNumber());
        } else {
            Symbol matchingSymbol = lookahead;
            lookahead = lexer.getNextSymbol();
            return matchingSymbol;
        }
    }

    private Symbol matchKeyword(String expectedKeyword) throws ParserException {
        if (lookahead.getToken() != Token.KEYWORD || !lookahead.getValue().equals(expectedKeyword)) {
            throw new ParserException("Expected keyword '" + expectedKeyword + "' but found '" + lookahead.getValue() + "'", lookahead.getLineNumber());
        }

        Symbol matchedSymbol = lookahead;
        lookahead = lexer.getNextSymbol();
        return matchedSymbol;
    }

    private Type parseType() throws ParserException {
        Type baseType;

        if (lookahead.getToken() == Token.KEYWORD) {
            // Handle primitive types
            String typeName = lookahead.getValue();
            if (typeName.equals("int") || typeName.equals("float") ||
                    typeName.equals("bool") || typeName.equals("string")) {
                Symbol key = match(Token.KEYWORD);
                baseType = new Type(typeName, TypeCategory.PRIMITIVE);
                baseType.setLineNumber(key.getLineNumber());
            } else {
                throw new TypeError("Invalid type: " + typeName, lookahead.getLineNumber());
            }
        } else if (lookahead.getToken() == Token.IDENTIFIER) {
            // Handle user-defined types (record)
            Symbol identifier = match(Token.IDENTIFIER);
            baseType = new Type(identifier.getValue(), TypeCategory.RECORD);
            baseType.setLineNumber(identifier.getLineNumber());
        } else {
            throw new TypeError("Invalid type: " + lookahead.getValue(), lookahead.getLineNumber());
        }

        // Check if it's an array type
        if (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET) {
            match(Token.OPEN_SQUARE_BRACKET);
            match(Token.CLOSE_SQUARE_BRACKET);

            Type type = new Type(baseType.getIdentifier() + "[]", TypeCategory.ARRAY);
            type.setLineNumber(baseType.getLineNumber());

            return type;
        }

        return baseType;
    }

    private Param parseParam() throws ParserException {
        Symbol identifier = match(Token.IDENTIFIER);
        Type type = parseType();

        Param param = new Param(type, identifier.getValue());
        param.setLineNumber(identifier.getLineNumber());

        return param;
    }

    private ArrayList<Param> parseParams() throws ParserException {
        ArrayList<Param> parameters = new ArrayList<>();

        if (lookahead.getToken() != Token.CLOSE_PARENTHESIS) {
            parameters.add(parseParam());
            while (lookahead.getToken() == Token.COMMA) {
                match(Token.COMMA);
                parameters.add(parseParam());
            }
        }

        return parameters;
    }

    private Block parseBlock() throws ParserException {
        match(Token.OPEN_CURLY_BRACE);
        List<Statement> statements = new ArrayList<>();

        while (lookahead.getToken() != Token.CLOSE_CURLY_BRACE && lookahead.getToken() != Token.EOF) {
            statements.add(parseStatement());
        }

        match(Token.CLOSE_CURLY_BRACE);
        return new Block(statements);
    }

    private Statement parseVariableDeclaration(Symbol name, boolean isFinal) throws ParserException {
        Type type;

        if (lookahead.getToken() == Token.KEYWORD) {
            // Primitive type
            type = parseType();
        } else if (lookahead.getToken() == Token.IDENTIFIER) {
            // User-defined record type
            Symbol recordName = match(Token.IDENTIFIER);
            type = new Type(recordName.getValue(), TypeCategory.RECORD);
            type.setLineNumber(recordName.getLineNumber());
        } else {
            throw new TypeError("Expected a type for variable '" + name.getValue() + "' but found: " + lookahead.getToken(), lookahead.getLineNumber());
        }

        Expression value = null;

        if (lookahead.getToken() == Token.ASSIGN) {
            match(Token.ASSIGN);
            value = parseExpression();
        }
        match(Token.SEMI_COLON);

        VariableDeclaration variableDeclaration = new VariableDeclaration(name.getValue(), type, value, isFinal);
        variableDeclaration.setLineNumber(name.getLineNumber());

        return variableDeclaration;
    }

    private Statement parseStatement() throws ParserException {
        if (lookahead.getToken() == Token.IDENTIFIER) {
            Symbol identifier = match(Token.IDENTIFIER);

            // Handle Assignment
            if (lookahead.getToken() == Token.ASSIGN) {
                Symbol assign = match(Token.ASSIGN);
                Expression value = parseExpression();
                match(Token.SEMI_COLON);

                Assignment assignment = new Assignment(new VarReference(identifier.getValue()), value);
                assignment.setLineNumber(assign.getLineNumber());

                return assignment;
            }

            // Handle Function Calls
            if (lookahead.getToken() == Token.OPEN_PARENTHESIS) {
                return parseAssignmentOrFunctionCall(identifier);
            }

            // Handle Record Field Access
            if (lookahead.getToken() == Token.DOT) {
                return parseAssignmentOrFunctionCall(identifier);
            }

            // Handle Array Access
            if (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET) {
                return parseAssignmentOrFunctionCall(identifier);
            }

            // check if "rec" comes next
            if (lookahead.getToken() == Token.KEYWORD && lookahead.getValue().equals("rec")) {
                return parseRecordDefinition(identifier.getValue());
            }

            // check if a type comes next
            if (lookahead.getToken() == Token.KEYWORD &&
                    (lookahead.getValue().equals("int") || lookahead.getValue().equals("float") ||
                            lookahead.getValue().equals("bool") || lookahead.getValue().equals("string"))) {
                return parseVariableDeclaration(identifier, false);
            }

            // check if a user-defined record type comes next
            if (lookahead.getToken() == Token.IDENTIFIER) {
                return parseVariableDeclaration(identifier, false);
            }

            throw new ParserException("Unexpected token after identifier: " + lookahead.getToken(), lookahead.getLineNumber());
        }

        switch (lookahead.getToken()) {
            case Token.KEYWORD:
                String keyword = lookahead.getValue();
                if (keyword.equals("final")) {
                    matchKeyword("final");
                    Symbol identifier = match(Token.IDENTIFIER);
                    return parseVariableDeclaration(identifier, true);
                }
                return parseKeywordStatement();

            case Token.OPEN_CURLY_BRACE:
                return parseBlock();

            default:
                throw new ParserException("Unexpected token: " + lookahead.getToken(), lookahead.getLineNumber());
        }
    }

    private Statement parseAssignmentOrFunctionCall(Symbol identifier) throws ParserException {
        Expression leftHandSide = new VarReference(identifier.getValue());
        leftHandSide.setLineNumber(identifier.getLineNumber());

        while (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET || lookahead.getToken() == Token.DOT) {
            if (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET) {
                match(Token.OPEN_SQUARE_BRACKET);
                Expression index = parseExpression();
                match(Token.CLOSE_SQUARE_BRACKET);
                leftHandSide = new ArrayAccess(identifier.getValue(), index);
                leftHandSide.setLineNumber(identifier.getLineNumber());
            } else if (lookahead.getToken() == Token.DOT) {
                match(Token.DOT);
                Symbol field = match(Token.IDENTIFIER);
                leftHandSide = new RecordFieldAccess(leftHandSide, field.getValue());
                leftHandSide.setLineNumber(field.getLineNumber());
            }
        }

        if (lookahead.getToken() == Token.ASSIGN) {
            Symbol assign = match(Token.ASSIGN);
            Expression value = parseExpression();
            match(Token.SEMI_COLON);

            Assignment assignment = new Assignment(leftHandSide, value);
            assignment.setLineNumber(assign.getLineNumber());

            return assignment;
        } else if (lookahead.getToken() == Token.OPEN_PARENTHESIS) {
            match(Token.OPEN_PARENTHESIS);
            ArrayList<Expression> arguments = new ArrayList<>();

            if (lookahead.getToken() != Token.CLOSE_PARENTHESIS) {
                arguments.add(parseExpression());
                while (lookahead.getToken() == Token.COMMA) {
                    match(Token.COMMA);
                    arguments.add(parseExpression());
                }
            }

            match(Token.CLOSE_PARENTHESIS);
            match(Token.SEMI_COLON);

            FunctionCall functionCall = new FunctionCall(identifier.getValue(), arguments);
            functionCall.setLineNumber(identifier.getLineNumber());

            return functionCall;
        } else {
            throw new ParserException("Invalid statement: " + identifier.getValue(), identifier.getLineNumber());
        }
    }

    private Statement parseKeywordStatement() throws ParserException {
        String keyword = lookahead.getValue();

        return switch (keyword) {
            case "if" -> parseIfStatement();
            case "while" -> parseWhileLoop();
            case "for" -> parseForLoop();
            case "return" -> parseReturnStatement();
            case "final" -> {
                Symbol identifier = match(Token.KEYWORD);
                yield parseVariableDeclaration(identifier, true);
            }
            case "free" -> parseFreeStatement();
            case "do" -> parseDoWhileLoop();
            default -> throw new ParserException("Unexpected keyword: " + keyword, lookahead.getLineNumber());
        };
    }

    private Statement parseWhileLoop() throws ParserException {
        Symbol whileKeyword = matchKeyword("while");
        match(Token.OPEN_PARENTHESIS);

        Expression condition = parseLogicalOr();

        match(Token.CLOSE_PARENTHESIS);
        Block body = parseBlock();

        WhileLoop whileLoop = new WhileLoop(condition, body);
        whileLoop.setLineNumber(whileKeyword.getLineNumber());

        return whileLoop;
    }

    private Statement parseDoWhileLoop() throws ParserException {
        matchKeyword("do");
        Block body = parseBlock();

        Symbol whileKeyword = matchKeyword("while");
        match(Token.OPEN_PARENTHESIS);
        Expression condition = parseLogicalOr();
        match(Token.CLOSE_PARENTHESIS);
        match(Token.SEMI_COLON);

        DoWhileLoop doWhileLoop = new DoWhileLoop(condition, body);
        doWhileLoop.setLineNumber(whileKeyword.getLineNumber());

        return doWhileLoop;
    }


    private Statement parseForLoop() throws ParserException {
        Symbol forKeyword = matchKeyword("for");
        match(Token.OPEN_PARENTHESIS);

        Symbol variable = match(Token.IDENTIFIER);
        match(Token.COMMA);

        Expression start = parseExpression();
        match(Token.COMMA);

        Expression maxValue = parseExpression();
        match(Token.COMMA);

        Expression step = parseExpression();
        match(Token.CLOSE_PARENTHESIS);

        Block body = parseBlock();

        ForLoop forLoop = new ForLoop(variable.getValue(), start, maxValue, step, body);
        forLoop.setLineNumber(forKeyword.getLineNumber());

        return forLoop;
    }

    private Statement parseIfStatement() throws ParserException {
        Symbol ifKeyword = matchKeyword("if");
        match(Token.OPEN_PARENTHESIS);

        Expression condition = parseLogicalOr();

        match(Token.CLOSE_PARENTHESIS);

        Block thenBlock = parseBlock();
        Block elseBlock = null;

        if (lookahead.getToken() == Token.KEYWORD && "else".equals(lookahead.getValue())) {
            matchKeyword("else");
            elseBlock = parseBlock();
        }

        IfStatement ifStatement = new IfStatement(condition, thenBlock, elseBlock);
        ifStatement.setLineNumber(ifKeyword.getLineNumber());

        return ifStatement;
    }

    private Statement parseReturnStatement() throws ParserException {
        Symbol returnKeyword = matchKeyword("return");

        Expression returnValue = null;
        if (lookahead.getToken() != Token.SEMI_COLON) {
            returnValue = parseExpression();
        }

        match(Token.SEMI_COLON);

        ReturnStatement returnStatement = new ReturnStatement(returnValue);
        returnStatement.setLineNumber(returnKeyword.getLineNumber());

        return returnStatement;
    }

    private Statement parseFreeStatement() throws ParserException {
        Symbol free = matchKeyword("free");
        Symbol identifier = match(Token.IDENTIFIER);  // Match the variable to deallocate
        match(Token.SEMI_COLON);

        FreeStatement freeStatement = new FreeStatement(identifier.getValue());
        freeStatement.setLineNumber(free.getLineNumber());

        return freeStatement;
    }

    private Expression parseExpression() throws ParserException {
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() throws ParserException {
        Expression left = parseLogicalAnd();

        while (lookahead.getToken() == Token.LOGICAL_OR) {
            Token operator = lookahead.getToken();
            Symbol op = match(operator);
            Expression right = parseLogicalAnd();
            left = new BinaryExpression(left, operator, right);
            left.setLineNumber(op.getLineNumber());
        }

        return left;
    }

    private Expression parseLogicalAnd() throws ParserException {
        Expression left = parseEquality();

        while (lookahead.getToken() == Token.LOGICAL_AND) {
            Token operator = lookahead.getToken();
            Symbol op = match(operator);
            Expression right = parseEquality();
            left = new BinaryExpression(left, operator, right);
            left.setLineNumber(op.getLineNumber());
        }

        return left;
    }

    private Expression parseEquality() throws ParserException {
        Expression left = parseComparison();

        while (lookahead.getToken() == Token.EQUAL || lookahead.getToken() == Token.NOT_EQUAL) {
            Token operator = lookahead.getToken();
            Symbol op = match(operator);
            Expression right = parseComparison();
            left = new BinaryExpression(left, operator, right);
            left.setLineNumber(op.getLineNumber());
        }

        return left;
    }

    private Expression parseComparison() throws ParserException {
        Expression left = parseTerm();

        while (lookahead.getToken() == Token.LESS || lookahead.getToken() == Token.GREATER ||
                lookahead.getToken() == Token.LESS_OR_EQUAL || lookahead.getToken() == Token.GREATER_OR_EQUAL) {
            Token operator = lookahead.getToken();
            Symbol op = match(operator);
            Expression right = parseTerm();
            left = new BinaryExpression(left, operator, right);
            left.setLineNumber(op.getLineNumber());
        }

        return left;
    }

    private Expression parseTerm() throws ParserException {
        Expression left = parseFactor();

        while (lookahead.getToken() == Token.ADD || lookahead.getToken() == Token.SUBTRACT) {
            Token operator = lookahead.getToken();
            Symbol op = match(operator);
            Expression right = parseFactor();
            left = new BinaryExpression(left, operator, right);
            left.setLineNumber(op.getLineNumber());
        }

        return left;
    }

    private Expression parseFactor() throws ParserException {
        Expression left = parseUnary();

        while (lookahead.getToken() == Token.MULTIPLY || lookahead.getToken() == Token.DIVIDE || lookahead.getToken() == Token.MODULO) {
            Token operator = lookahead.getToken();
            Symbol op = match(operator);
            Expression right = parseUnary();
            left = new BinaryExpression(left, operator, right);
            left.setLineNumber(op.getLineNumber());
        }

        return left;
    }

    private Expression parseUnary() throws ParserException {
        if (lookahead.getToken() == Token.SUBTRACT) {
            Token operator = lookahead.getToken();
            Symbol op = match(operator);
            Expression operand = parseUnary();

            UnaryExpression unaryExpression = new UnaryExpression(operator, operand);
            unaryExpression.setLineNumber(op.getLineNumber());

            return unaryExpression;
        }

        return parsePrimary();
    }

    private Expression parsePrimary() throws ParserException {
        switch (lookahead.getToken()) {
            case Token.INTEGER_NUMBER:
            case Token.FLOAT_NUMBER:
            case Token.BOOLEAN:
            case Token.STRING:
                Symbol val = match(lookahead.getToken());
                Literal literal = new Literal(val);
                literal.setLineNumber(val.getLineNumber());
                return literal;

            case Token.IDENTIFIER:
                Symbol identifier = match(Token.IDENTIFIER);

                Expression expr = new VarReference(identifier.getValue());
                expr.setLineNumber(identifier.getLineNumber());

                if (lookahead.getToken() == Token.OPEN_PARENTHESIS) {
                    return parseAssignedCallExpression(identifier);
                }

                // Handle chained array access and field access
                while (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET || lookahead.getToken() == Token.DOT) {
                    if (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET) {
                        match(Token.OPEN_SQUARE_BRACKET);
                        Expression index = parseExpression();
                        match(Token.CLOSE_SQUARE_BRACKET);
                        expr = new ArrayAccess(identifier.getValue(), index);  // Now expr is an array element reference
                        expr.setLineNumber(identifier.getLineNumber());
                    } else if (lookahead.getToken() == Token.DOT) {
                        match(Token.DOT);
                        Symbol field = match(Token.IDENTIFIER);
                        expr = new RecordFieldAccess(expr, field.getValue());  // Allow access to record fields
                        expr.setLineNumber(field.getLineNumber());
                    }
                }

                return expr;

            case Token.KEYWORD:
                if (lookahead.getValue().equals("array")) {
                    return parseArrayCreation();
                }
                throw new ParserException("Unexpected keyword: " + lookahead.getValue(), lookahead.getLineNumber());

            case Token.OPEN_PARENTHESIS:
                match(Token.OPEN_PARENTHESIS);
                Expression expression = parseExpression();
                match(Token.CLOSE_PARENTHESIS);
                return expression;

            default:
                throw new ParserException("Unexpected token: " + lookahead.getToken(), lookahead.getLineNumber());
        }
    }

    private Expression parseArrayCreation() throws ParserException {
        Symbol array = matchKeyword("array");
        match(Token.OPEN_SQUARE_BRACKET);
        Expression size = parseExpression();
        match(Token.CLOSE_SQUARE_BRACKET);
        matchKeyword("of");
        Type elementType = parseType();

        ArrayCreation arrayCreation = new ArrayCreation(size, elementType);
        arrayCreation.setLineNumber(array.getLineNumber());

        return arrayCreation;
    }

    private Expression parseAssignedCallExpression(Symbol recordType) throws ParserException {
        Symbol parenthesis = match(Token.OPEN_PARENTHESIS);
        List<Expression> arguments = new ArrayList<>();

        // Checks if it is a function call or a record creation
        if (lookahead.getToken() != Token.CLOSE_PARENTHESIS) {
            arguments.add(parseExpression());
            while (lookahead.getToken() == Token.COMMA) {
                match(Token.COMMA);
                arguments.add(parseExpression());
            }
        }

        match(Token.CLOSE_PARENTHESIS);

        CallExpression callExpression = new CallExpression(recordType.getValue(), arguments);
        callExpression.setLineNumber(parenthesis.getLineNumber());

        return callExpression;
    }

    private Statement parseRecordDefinition(String name) throws ParserException {
        Symbol keyword = matchKeyword("rec");
        match(Token.OPEN_CURLY_BRACE);

        List<RecordField> fields = new ArrayList<>();

        while (lookahead.getToken() != Token.CLOSE_CURLY_BRACE) {
            Symbol fieldName = match(Token.IDENTIFIER);
            Type fieldType = parseType();
            match(Token.SEMI_COLON);

            fields.add(new RecordField(fieldName.getValue(), fieldType));
        }

        match(Token.CLOSE_CURLY_BRACE);

        RecordDefinition recordDefinition = new RecordDefinition(name, fields);
        recordDefinition.setLineNumber(keyword.getLineNumber());

        return recordDefinition;
    }

    private Method parseMethod() throws ParserException {
        matchKeyword("fun");

        Symbol functionName = match(Token.IDENTIFIER);

        match(Token.OPEN_PARENTHESIS);
        ArrayList<Param> params = parseParams();
        match(Token.CLOSE_PARENTHESIS);

        Type returnType = null;
        if (lookahead.getToken() != Token.OPEN_CURLY_BRACE) {
            returnType = parseType();
        }

        Block body = parseBlock();

        Method method = new Method(functionName.getValue(), returnType, params, body);
        method.setLineNumber(functionName.getLineNumber());

        return method;
    }

    public Block getAST() throws ParserException {
        List<Statement> statements = new ArrayList<>();

        while (lookahead.getToken() != Token.EOF) {
            if (lookahead.getToken() == Token.KEYWORD && lookahead.getValue().equals("fun")) {
                statements.add(parseMethod());
            } else {
                statements.add(parseStatement());
            }
        }

        return new Block(statements);
    }
}
