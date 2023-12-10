package jlox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name) {
        
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name,
        "Undefined variable'" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        // Defines variables (and allows redefinition).
        values.put(name, value);
    }

}