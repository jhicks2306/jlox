package jlox;

class RuntimeError extends RuntimeException {
    // Defines a Lox RuntimeError to handle Java RuntimeExceptions.
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}