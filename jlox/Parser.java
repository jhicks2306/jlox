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

    
}