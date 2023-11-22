package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF,"",null, line));
        return tokens;
    }

    private void scanToken() {
        // Detects different tokens.
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            default:
                Lox.error(line, "Unexpected character.");
                break;
        }
    }
 
    private boolean match(char expected) {
        // Checks a character and consumes it if there is a match.
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private boolean isAtEnd() {
        // Checks if end of source code reached.
        return current >= source.length();
    }

    private char advance() {
        // Moves forward one character and consumes it.
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        // Overload method for tokens without literals.
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        // Adds token and metadata to list of tokens.
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}