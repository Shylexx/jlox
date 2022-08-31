package com.shylex.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shylex.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Advances a character and then parses it into a token based on the character it is or matches
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                // If two slashes, line comment
                if(match('/')) {
                    // A comment goes til the end of the line
                    // Peek next letter and continue skipping letters until end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            }
            // Skip whitespace
            case ' ', '\r', '\t' -> {}
            // Skip newline char, but increment line number
            case '\n' -> line++;

            // Literals

            case '"' -> string();

            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected Character.");
                }
            }
        }
    }

    /**
     * Logic for parsing whether the next token is a string literal or not
     */
    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated String.");
            return;
        }

        // The closing "
        advance();

        // Trim the " off of the string value
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Parses a number token
     */
    private void number() {
        while  (isDigit(peek())) advance();

        // Check that the decimal is not trailing
        if(peek() == '.' && isDigit(peekNext())) {
            // Consume the decimal point
            advance();

            // Parse the second half of the floating point number
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start,current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    /**
     * Checks if the input is a digit
     * @param c The input to check
     * @return true if input is digit, false if not
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Matches the next character in the current source and consumes it as well as returning true if the parameter matches it
     * @param expected The character to compare to
     * @return Whether the next character in the source matches the parameter.
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        // If next character matches param, consume it and return true
        current++;
        return true;
    }

    /**
     * Checks the next character in the current source file and returns it
     * @return the next character in the current file
     */
    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Checks two characters ahead in the current source file and returns the character
     * @return two characters ahead in current source
     */
    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Moves forward a character in the source and returns the next character, 'consuming' it.
     * @return the next character
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Adds a token to the arraylist of tokens
     * @param type the token to add
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }


}
