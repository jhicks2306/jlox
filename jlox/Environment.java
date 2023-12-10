package jlox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    // Set refer to enclosing environments for local scope.
    final Environment enclosing;
    
    // Constructor for global scope (no chaining).
    Environment() {
        enclosing = null;
    }

    // Constructor for local scopes.
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name) {
        
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // If variable not found in local scope, try enclosing one.
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
        "Undefined variable'" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        // Assigns variable (but doesn't allow creation of new variables).
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        // Defines variables (and allows redefinition).
        values.put(name, value);
    }

}