package jlox;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.VoiceStatus;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                                List<Object> argumen) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
        });
    }

    void interpret(List<Stmt> statements) {
        // Takes in a program (list of statements) and interprets it.
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        // Interprets a Literal expression.
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        // Interprets a grouping expression.
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        // Evaluates an expression by passing the encapsulating Visitor to accept method.
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        // Executes a statement by passing the encapsulating Visitor to accept method.
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        // Store the outer environment.
        Environment previous = this.environment;

        try {
            // Update interpreter's environment to new local one and execute statements.
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            // Restore interpreter's environment to outer one.
            this.environment = previous;
        }
                      }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        // Interprets a block statement (in new environment).
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        // Interprets an expression statement.
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        // Interprets an if statement.
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        // Interprets a print statment.
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // Interprets a variable declaration statment.
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // Interprets and Unary expression.
        Object right = evaluate(expr.right);

        // Cast the right operand to a double at runtime (dynamic typing)
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        // Interprets variable expression.
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        // Checks if operand for Unary operator is a number.
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        // Checks if operands for Binary operators are numbers. 
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        // Checks truthiness of an Object.
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        // Checks if two Objects are equal.
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        // Turns a Double into a String.
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // Interprets a Binary expression.

        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        // Choose behaviour based on operator type.
        switch (expr.operator.type) {
            case BANG_EQUAL: return !isEqual(left,right);
            case EQUAL_EQUAL: return isEqual(left,right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;                            
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
               
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        /* Interprets a call expression. Evaluates the
         callee (usually a function identifier). Then collects arguments into a list
         before making the function call. */
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + 
                function.arity() + " arguments but got " +
                arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

}