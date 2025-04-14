package compiler.Parser;

import compiler.Exceptions.ParserException;
import compiler.Lexer.Lexer;
import compiler.Components.Symbol;
import compiler.Components.Token;
import compiler.Components.Blocks.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class  Parser {
    private final Lexer lexer;
    private Symbol lookahead;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.lookahead = lexer.getNextSymbol();
    }

    private Symbol match(Token expectedToken) throws ParserException {
        if(lookahead.getToken() != expectedToken) {
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
                match(Token.KEYWORD);
                baseType = new Type(typeName, TypeCategory.PRIMITIVE);
            } else {
                throw new ParserException("Invalid type: " + typeName, lookahead.getLineNumber());
            }
        } else if (lookahead.getToken() == Token.IDENTIFIER) {
            // Handle user-defined types (record)
            Symbol identifier = match(Token.IDENTIFIER);
            baseType = new Type(identifier.getValue(), TypeCategory.RECORD);
        } else {
            throw new ParserException("Invalid type: " + lookahead.getValue(), lookahead.getLineNumber());
        }

        // Check if it's an array type
        if (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET) {
            match(Token.OPEN_SQUARE_BRACKET);
            match(Token.CLOSE_SQUARE_BRACKET);
            return new Type(baseType.getIdentifier() + "[]", TypeCategory.ARRAY);
        }

        return baseType;
    }

    private Param parseParam() throws ParserException {
        Symbol identifier = match(Token.IDENTIFIER);
        Type type = parseType();
        return new Param(type, identifier.getValue());
    }

    private ArrayList<Param> parseParams() throws ParserException {
        ArrayList<Param> parameters = new ArrayList<>();

        if(lookahead.getToken() != Token.CLOSE_PARENTHESIS) {
            parameters.add(parseParam());
            while(lookahead.getToken() == Token.COMMA) {
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
        } else {
            throw new ParserException("Expected a type for variable '" + name.getValue() + "' but found: " + lookahead.getToken(), lookahead.getLineNumber());
        }

        Expression value = null;

        if (lookahead.getToken() == Token.ASSIGN) {
            match(Token.ASSIGN);
            value = parseExpression();
        }
        match(Token.SEMI_COLON);

        return new VariableDeclaration(name.getValue(), type, value, isFinal);
    }

    private Statement parseStatement() throws ParserException {
        if (lookahead.getToken() == Token.IDENTIFIER) {
            Symbol identifier = match(Token.IDENTIFIER);

            // Handle Assignment
            if (lookahead.getToken() == Token.ASSIGN) {
                match(Token.ASSIGN);
                Expression value = parseExpression();
                match(Token.SEMI_COLON);
                return new Assignment(new VarReference(identifier.getValue()), value);
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

        while (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET || lookahead.getToken() == Token.DOT) {
            if (lookahead.getToken() == Token.OPEN_SQUARE_BRACKET) {
                match(Token.OPEN_SQUARE_BRACKET);
                Expression index = parseExpression();
                match(Token.CLOSE_SQUARE_BRACKET);
                leftHandSide = new ArrayAccess(identifier.getValue(), index);
            } else if (lookahead.getToken() == Token.DOT) {
                match(Token.DOT);
                Symbol field = match(Token.IDENTIFIER);
                leftHandSide = new RecordFieldAccess(leftHandSide, field.getValue());
            }
        }

        if (lookahead.getToken() == Token.ASSIGN) {
            match(Token.ASSIGN);
            Expression value = parseExpression();
            match(Token.SEMI_COLON);
            return new Assignment(leftHandSide, value);
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
            return new FunctionCall(identifier.getValue(), arguments);
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
        matchKeyword("while");
        match(Token.OPEN_PARENTHESIS);

        Expression condition = parseLogicalOr();

        match(Token.CLOSE_PARENTHESIS);
        Block body = parseBlock();
        return new WhileLoop(condition, body);
    }

    private Statement parseDoWhileLoop() throws ParserException {
        matchKeyword("do");
        Block body = parseBlock();

        matchKeyword("while");
        match(Token.OPEN_PARENTHESIS);
        Expression condition = parseLogicalOr();
        match(Token.CLOSE_PARENTHESIS);
        match(Token.SEMI_COLON);

        return new DoWhileLoop(condition, body);
    }


    private Statement parseForLoop() throws ParserException {
        matchKeyword("for");
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

        return new ForLoop(variable.getValue(), start, maxValue, step, body);
    }

    private Statement parseIfStatement() throws ParserException {
        matchKeyword("if");
        match(Token.OPEN_PARENTHESIS);

        Expression condition = parseLogicalOr();

        match(Token.CLOSE_PARENTHESIS);

        Block thenBlock = parseBlock();
        Block elseBlock = null;

        if (lookahead.getToken() == Token.KEYWORD && "else".equals(lookahead.getValue())) {
            matchKeyword("else");
            elseBlock = parseBlock();
        }

        return new IfStatement(condition, thenBlock, elseBlock);
    }

    private Statement parseReturnStatement() throws ParserException {
        matchKeyword("return");

        Expression returnValue = null;
        if (lookahead.getToken() != Token.SEMI_COLON) {
            returnValue = parseExpression();
        }

        match(Token.SEMI_COLON);

        return new ReturnStatement(returnValue);
    }

    private Statement parseFreeStatement() throws ParserException {
        matchKeyword("free");
        Symbol identifier = match(Token.IDENTIFIER);  // Match the variable to deallocate
        match(Token.SEMI_COLON);

        return new FreeStatement(identifier.getValue());
    }

    private Expression parseExpression() throws ParserException {
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() throws ParserException {
        Expression left = parseLogicalAnd();

        while (lookahead.getToken() == Token.LOGICAL_OR) {
            Token operator = lookahead.getToken();
            match(operator);
            Expression right = parseLogicalAnd();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression parseLogicalAnd() throws ParserException {
        Expression left = parseEquality();

        while (lookahead.getToken() == Token.LOGICAL_AND) {
            Token operator = lookahead.getToken();
            match(operator);
            Expression right = parseEquality();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression parseEquality() throws ParserException {
        Expression left = parseComparison();

        while (lookahead.getToken() == Token.EQUAL || lookahead.getToken() == Token.NOT_EQUAL) {
            Token operator = lookahead.getToken();
            match(operator);
            Expression right = parseComparison();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression parseComparison() throws ParserException {
        Expression left = parseTerm();

        while (lookahead.getToken() == Token.LESS || lookahead.getToken() == Token.GREATER ||
                lookahead.getToken() == Token.LESS_OR_EQUAL || lookahead.getToken() == Token.GREATER_OR_EQUAL) {
            Token operator = lookahead.getToken();
            match(operator);
            Expression right = parseTerm();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression parseTerm() throws ParserException {
        Expression left = parseFactor();

        while (lookahead.getToken() == Token.ADD || lookahead.getToken() == Token.SUBTRACT) {
            Token operator = lookahead.getToken();
            match(operator);
            Expression right = parseFactor();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression parseFactor() throws ParserException {
        Expression left = parseUnary();

        while (lookahead.getToken() == Token.MULTIPLY || lookahead.getToken() == Token.DIVIDE || lookahead.getToken() == Token.MODULO) {
            Token operator = lookahead.getToken();
            match(operator);
            Expression right = parseUnary();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression parseUnary() throws ParserException {
        if (lookahead.getToken() == Token.SUBTRACT) {
            Token operator = lookahead.getToken();
            match(operator);
            Expression operand = parseUnary();
            return new UnaryExpression(operator, operand);
        }

        return parsePrimary();
    }

    private Expression parsePrimary() throws ParserException {
        switch (lookahead.getToken()) {
            case Token.INTEGER_NUMBER:
            case Token.FLOAT_NUMBER:
            case Token.BOOLEAN:
            case Token.STRING:
                return new Literal(match(lookahead.getToken()));

            case Token.IDENTIFIER:
                Symbol identifier = match(Token.IDENTIFIER);

                Expression expr = new VarReference(identifier.getValue());

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
                    } else if (lookahead.getToken() == Token.DOT) {
                        match(Token.DOT);
                        Symbol field = match(Token.IDENTIFIER);
                        expr = new RecordFieldAccess(expr, field.getValue());  // Allow access to record fields
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
        matchKeyword("array");
        match(Token.OPEN_SQUARE_BRACKET);
        Expression size = parseExpression();
        match(Token.CLOSE_SQUARE_BRACKET);
        matchKeyword("of");
        Type elementType = parseType();

        return new ArrayCreation(size, elementType);
    }

    private Expression parseAssignedCallExpression(Symbol recordType) throws ParserException {
        match(Token.OPEN_PARENTHESIS);
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
        return new CallExpression(recordType.getValue(), arguments);
    }

    private Statement parseRecordDefinition(String name) throws ParserException {
        matchKeyword("rec");
        match(Token.OPEN_CURLY_BRACE);

        List<RecordField> fields = new ArrayList<>();

        while (lookahead.getToken() != Token.CLOSE_CURLY_BRACE) {
            Symbol fieldName = match(Token.IDENTIFIER);
            Type fieldType = parseType();
            match(Token.SEMI_COLON);

            fields.add(new RecordField(fieldName.getValue(), fieldType));
        }

        match(Token.CLOSE_CURLY_BRACE);

        return new RecordDefinition(name, fields);
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
        return new Method(functionName.getValue(), returnType, params, body);
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
