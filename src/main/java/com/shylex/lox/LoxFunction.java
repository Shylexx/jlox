package com.shylex.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Environment closure;
    private final Stmt.Function declaration;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure,
                boolean isInitializer) {
        this.closure = closure;
        this.declaration = declaration;
        this.isInitializer = isInitializer;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment,
                isInitializer);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // Create a new scope for function call to happen in
        // Use the closure env as the parent in order to use the captured environment
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

        try {
            // Call function in its new scope
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            // Early returns in class initializers return 'this'
            if (isInitializer) return closure.getAt(0, "this");

            // We catch return values as exceptions, to unwind to the top of the function.
            return returnValue.value;
        }

        // Constructor always returns 'this'
        if(isInitializer) return closure.getAt(0, "this");

        // Returns 'nil' if no return value is caught from execution
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
