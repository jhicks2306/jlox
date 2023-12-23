package jlox;

import java.util.HashMap;
import java.util.Map;


public class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    Object get(Token name) {
        // Get field on LoxInstance.
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        // Set field on LoxInstance.
        fields.put(name.lexeme, value);
    }

    
    @Override
    public String toString() {
        return klass.name + " instance.";
    }
}