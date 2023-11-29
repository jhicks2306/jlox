package jlox;

import java.util.List;

import static jlox.TokenType.*;

class Parser {
    /* Consumes flat input sequence of tokens. */
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /*  Methods for each rule of Lox's grammar. Each method for 
    parsing a grammar rule produces a syntax tree for that rule and 
    returns it to the caller. */

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
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


}