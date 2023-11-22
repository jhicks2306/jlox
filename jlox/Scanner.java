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

            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;

            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        // Detects identifier lexeme.
        while (isAlphaNumeric(peek())) advance();

        addToken(IDENTIFIER);
    }

    private void number() {
        // Detects number lexeme whilst checking for decimal point.
        while (isDigit(peek())) advance();

        // Look for fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
    }
 
    private void string() {
        // Detects string lexeme to add token. Throws error if no closing ".
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current -1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        // Checks a character and consumes it if there is a match.
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        // Looks one ahead but doesn't consume character.
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        // Looks two ahead but doesn't consume character.
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        // Checks if character is alpha.
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
      }

    private boolean isAlphaNumeric(char c) {
        // Checks if character is alphanumeric.
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        // Checks if character is a digit.
        return c >= '0' && c <= '9';
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