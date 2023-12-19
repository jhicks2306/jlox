package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    void resolve(List<Stmt> statements) {
        // Lists list of statements and resolves each one.
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        // Resolve single statement.
        stmt.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        // Begings scope, traverses bloack of statments, then discards the scope.
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override

}