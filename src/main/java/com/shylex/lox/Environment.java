package com.shylex.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Object get(Token name) {
        if(values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // Recursively check the enclosing (parent) scopes to find the variable
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
                "Undefined Variable '" +name.lexeme + "'.");
    }

    /**
     * Gets a variable from a scope up the ladder of locals
     * @param distance How many rungs up the ladder the scope is
     * @param name The name of the variable to get
     * @return The variable from the map
     */
    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    /**
     * Assigns a variable in a local scope
     * @param distance how many scopes up the ladder the var to be assigned to is
     * @param name The identifier of the variable to assign to
     * @param value The value to assign to the variable
     */
    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    /**
     * Walks up the enclosing environments to the required scope, and then returns the needed environment
     * @param distance How many steps to take up the ladder
     * @return The environment at the distance up the ladder from the current
     */
    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    void assign(Token name, Object value) {
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

}
