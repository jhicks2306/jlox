package jlox;

import java.util.ArrayList;
import java.util.List;

import static jlox.TokenType.*;

class Parser {
    /* Consumes flat input sequence of tokens. */
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    /*  Methods for each rule of Lox's grammar. Each method for 
    parsing a grammar rule produces a syntax tree for that rule and 
    returns it to the caller.
    
    The grammar comments use the following syntax:
    '|' separates productions
    '*' means production can appear zero or more times.
    '+' means production can appear one or more times.
    '?' means production can appear zero or one time.
    '()'group productions */

    private Expr expression() {
        // expression     → equality ;
        return equality();
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement() {
        // Parses a print statement.
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        // Parses an expression statement.
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr equality() {
        // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        // term           → factor ( ( "-" | "+" ) factor )* ;
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        // factor         → unary ( ( "/" | "*" ) unary )* ;
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        // unary          → ( "!" | "-" ) unary | primary ;
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        // primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null); 
        
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            /* If a ')' is matched then ')' must be matched after parsing
             the grouped expression. */
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        /* Checks if current token matches those in list provided.
         If there is a match then consumes the token and returns true.
         Otherwise false and token not consumed. */
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
    }

    private Token consume(TokenType type, String message) {
        // Checks current token and consumes if matches. Otherwise, throws error.
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        // Returns true if current token is of given type.
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        // Consumes current token and returns it.
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        // Check if have reached end of file.
        return peek().type == EOF;
    }

    private Token peek() {
        // Returns current token yet to be consumed.
        return tokens.get(current);
    }

    private Token previous() {
        // Returns most recently consumed token.
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        // Defines a parse error and passes to general Lox error.
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        // Discards tokens until finds a statement boundary.
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:                    
                    return;
            }

            advance();
        }
    }
}