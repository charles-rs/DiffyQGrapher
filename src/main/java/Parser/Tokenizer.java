package Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A Tokenizer turns a Reader into a stream of tokens that can be iterated over using a {@code for}
 * loop.
 */
public class Tokenizer implements Iterator<Token> {


    /**
     * Lookahead buffer. Contains characters already read from the {@code Reader} but not processed
     * into a token yet.
     */
    private final LookAheadBuffer in;

    /**
     * The line number of the input file. Always equals 1 + the number of new line characters
     * previously encountered.
     */
    private int lineNumber;

    /**
     * Queue of tokens produced by this Tokenizer but not yet read.
     */
    private final Queue<Token> tokens = new LinkedList<>();

    /**
     * StringBuilder used to scan keywords and numbers.
     */
    private final StringBuilder sb = new StringBuilder();

    /**
     * Create a Tokenizer that reads from the specified reader
     *
     * @param r The source from which the Tokenizer lexes input into Tokens
     */
    public Tokenizer(Reader r) {
        // need lookahead capacity 2 for -->
        in = new LookAheadBuffer(2, new BufferedReader(r));
        lineNumber = 1;
    }

    /**
     * Returns {@code true} if there are more meaningful tokens to be read. In other words, returns
     * {@code true} if {@link #next} would return a non-EOF token.
     *
     * @return {@code true} if there are more meaningful tokens
     * @throws TokenizerIOException if an IOException was thrown while trying to read from the
     *                              source Reader
     */
    @Override
    public boolean hasNext() {
        return !(peek() instanceof Token.EOFToken);
    }

    /**
     * Returns the next unread token. If the input is exhausted, returns the EOF token.
     *
     * @return the next unread token or EOF
     * @throws TokenizerIOException if an IOException was thrown while trying to read from the
     *                              source Reader
     */
    @Override
    public Token next() {
        peek();
        return tokens.poll();
    }

    /**
     * Returns the next available token without consuming the token. If there are no more tokens
     * available, returns the EOF token.
     *
     * @return the next token without consuming it
     * @throws TokenizerIOException if an IOException was thrown while trying to read from the
     *                              source Reader
     */
    public Token peek() {
        if (tokens.isEmpty()) {
            try {
                lexOneToken();
            } catch (IOException e) {
                throw new TokenizerIOException(e);
            }
        }
        return tokens.peek();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    int lineNumber() {
        return lineNumber;
    }

    /**
     * Constructs one token from the input source and pushes it onto the token queue. A token is
     * always produced, but it may be an error token or an end-of-file (EOF) token.
     *
     * @throws IOException if an IOException was thrown when trying to read from the source Reader
     */
    private void lexOneToken() throws IOException {

        char c = in.next();

        // consume whitespace
        while (Character.isWhitespace(c) && c != '\n') {
            c = in.next();
        }

        switch (c) {
            case '\n':
                addToken(TokenType.NL);
                break;
            case LookAheadBuffer.EOF:
                addEOFToken();
                break;
            case '(':
                addToken(TokenType.LPAREN);
                break;
            case ')':
                addToken(TokenType.RPAREN);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '=':
                addToken(TokenType.EQ);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '*':
                addToken(TokenType.MUL);
                break;
            case '/':
                lexSlash();
                break;
            case '-':
                lexDash();
                break;
            case '^':
                lexPow();
                break;
            case 'a':
                lexA(c);
                break;
            default:
                if (Character.isLetter(c)) lexKeyword(c);
                else if (Character.isDigit(c)) lexNum(c);
                else addErrorToken(String.format("Unrecognized character %c", c));
        }
    }


    private void lexA(char c) throws IOException {
        if (Character.isLetter(in.peek())) lexKeyword(c);
        else addToken(TokenType.A);
    }

    /**
     * lexes "/ ... ". May only be called when the previously read char is '/'.
     * Either division or a comment
     * impl notes: pretty weird use of a while loop but it works
     */
    private void lexSlash() throws IOException {
        if (in.peek() == '/') {
            while (in.peek() != 127 && in.next() != '\n') {
                //System.out.println((int) in.next());
            }
            ;
            lexOneToken();
        } else addToken(TokenType.DIV);
    }


    private void lexPow() throws IOException {
        addToken(TokenType.POW);
    }

    /**
     * Lexes a hyphen character '-'. to a minus
     *
     * @throws IOException if an IOException was thrown when trying to read from the source Reader
     */
    private void lexDash() throws IOException {
        addToken(TokenType.MINUS);
    }

    /**
     * Lexes a keyword. May be called only when the previously read character is a letter. Scans the
     * keyword and finds it in the keyword table.
     *
     * @throws IOException if an IOException was thrown when trying to read from the source Reader
     */
    private void lexKeyword(char c) throws IOException {
        sb.setLength(0);
        sb.append(c);
        c = in.peek();
        while (Character.isLetter(c)) {
            sb.append(c);
            c = in.scanAndPeek();
        }
        TokenType tt = TokenType.getTypeFromString(sb.toString());
        if (tt != null) addToken(tt);
        else {
            for (char ch : sb.toString().toCharArray()) {
                if (ch == 'a' || ch == 'b' || ch == 'x' || ch == 'y') {
                    addToken(TokenType.getTypeFromString(String.valueOf(ch)));
                } else {
                    addErrorToken(String.format("Unrecognized keyword %s", sb));
                    break;
                }
            }
        }

    }

    /**
     * Lexes a number. May be called only when the previously read character is a digit. Scans the
     * number and produces a number token.
     *
     * @throws IOException if an IOException was thrown when trying to read from the source Reader
     */
    private void lexNum(char c) throws IOException {
        sb.setLength(0);
        sb.append(c);
        c = in.peek();
        boolean decimal = false;
        while (Character.isDigit(c) || (c == '.' && !decimal)) {
            if (c == '.') decimal = true;
            sb.append(c);
            c = in.scanAndPeek();
        }
        try {
            double val = Double.parseDouble(sb.toString());
            tokens.add(new Token.NumToken(val, lineNumber));
        } catch (NumberFormatException e) {
            addErrorToken(String.format("Number expected, got %s", sb.toString()));
        }
    }

    /**
     * Pushes a token of the given type.
     *
     * @param tokenType the type of the token to pushed, not {@code null}
     */
    private void addToken(TokenType tokenType) {
        tokens.add(new Token(tokenType, lineNumber));
    }

    /**
     * Read the next character and push a token of the given type if it is the expected character.
     * If not, push an error token.
     *
     * @param expected The expected next character
     * @param tt       The {@code TokenType} to push on success
     * @throws IOException if an IOException was thrown when trying to read from the source Reader
     */
    private void consume(char expected, TokenType tt) throws IOException {
        char c = in.next();
        if (c == expected) addToken(tt);
        else addErrorToken(String.format("Expected %c, got %c", expected, c));
    }

    /**
     * Pushes an error token with the given message.
     */
    private void addErrorToken(String message) {
        tokens.add(new Token.ErrorToken(message, lineNumber));
    }

    /**
     * Pushes an and-of-file token.
     */
    private void addEOFToken() {
        tokens.add(new Token.EOFToken("EOF", lineNumber));
    }

    /**
     * Helper exception to indicate an IO exception while tokenizing. This is a RuntimeException,
     * which means it does not have to be declared in the method header.
     */
    static class TokenizerIOException extends RuntimeException {
        TokenizerIOException(Throwable cause) {
            super(cause);
        }
    }
}