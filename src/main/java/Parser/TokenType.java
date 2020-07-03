package Parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public enum TokenType


{
	EQ(TokenCategory.OTHER, "="),
	PLUS(TokenCategory.ADDOP, "+"),
	MINUS(TokenCategory.ADDOP, "-"),
	MUL(TokenCategory.MULOP, "*"),
	DIV(TokenCategory.MULOP, "/"),
	MOD(TokenCategory.MULOP, "mod"),
	LPAREN(TokenCategory.OTHER, "("),
	RPAREN(TokenCategory.OTHER, ")"),
	SEMICOLON(TokenCategory.OTHER, ";"),
	NUM(TokenCategory.OTHER, "<number>"),
	SIN(TokenCategory.FUNC, "sin"),
	COS(TokenCategory.FUNC, "cos"),
	TAN(TokenCategory.FUNC, "tan"),
	SEC(TokenCategory.FUNC, "sec"),
	CSC(TokenCategory.FUNC, "csc"),
	COT(TokenCategory.FUNC, "cot"),

	ASIN(TokenCategory.FUNC, "arcsin"),
	ACOS(TokenCategory.FUNC, "arccos"),
	ATAN(TokenCategory.FUNC, "arctan"),
	ASEC(TokenCategory.FUNC, "arcsec"),
	ACSC(TokenCategory.FUNC, "arccsc"),
	ACOT(TokenCategory.FUNC, "arccot"),

	SINH(TokenCategory.FUNC, "sinh"),
	COSH(TokenCategory.FUNC, "cosh"),
	TANH(TokenCategory.FUNC, "tanh"),
	SECH(TokenCategory.FUNC, "sech"),
	CSCH(TokenCategory.FUNC, "csch"),
	COTH(TokenCategory.FUNC, "coth"),

	ASINH(TokenCategory.FUNC, "arcsinh"),
	ACOSH(TokenCategory.FUNC, "arccosh"),
	ATANH(TokenCategory.FUNC, "arctanh"),
	ASECH(TokenCategory.FUNC, "arcsech"),
	ACSCH(TokenCategory.FUNC, "arccsch"),
	ACOTH(TokenCategory.FUNC, "arccoth"),
	LOG(TokenCategory.FUNC, "log"),
	LN(TokenCategory.FUNC, "ln"),

	SQRT(TokenCategory.FUNC, "sqrt"),

	//TODO: ADD MORE FUNCTIONS
	POW(TokenCategory.OTHER, "^"),

	PI(TokenCategory.CONSTANT, "pi"),
	E(TokenCategory.CONSTANT, "e"),
	A(TokenCategory.CONSTANT, "a"),
	B(TokenCategory.CONSTANT, "b"),
	T(TokenCategory.CONSTANT, "t"),
	X(TokenCategory.CONSTANT, "x"),
	Y(TokenCategory.CONSTANT, "y"),
	R(TokenCategory.CONSTANT, "r"),
	//TODO: MAYBE ADD MORE CONSTANTS

	DY(TokenCategory.OTHER, "dy"),
	DX(TokenCategory.OTHER, "dx"),
	DT(TokenCategory.OTHER, "dt"),

	NL(TokenCategory.OTHER, "\n"),
	ERROR(TokenCategory.OTHER, "[error]"),
	EOF(TokenCategory.OTHER, "EOF");

	/**
	 * Maps the string representation of a token to its enum.
	 */
	private static final Map<String, TokenType> stringToTypeMap;

	// static initializer to initialize the values of stringToTypeMap
	static
	{
		final Map<String, TokenType> temp = new HashMap<>();
		for (TokenType t : TokenType.values())
		{
			temp.put(t.stringRep, t);
		}
		stringToTypeMap = Collections.unmodifiableMap(temp);
	}

	/**
	 * The category of this TokenType.
	 */
	private final TokenCategory category;

	/**
	 * String representation of this TokenType.
	 */
	private final String stringRep;

	/**
	 * Constructs a new {@code TokenType} with category {@code cat} and string representation {@code
	 * s}.
	 *
	 * @param tcat token category, checks {@code tcat != null}
	 * @param s    string representation of this token, check {@code s != null}
	 */
	private TokenType(TokenCategory tcat, String s)
	{
		assert tcat != null : "TokenType must have a category";
		assert s != null : "TokenType must have a string representation";
		category = tcat;
		stringRep = s;
	}

	/**
	 * Returns this {@code TokenType}'s category.
	 *
	 * @return this {@code TokenType}'s category
	 */
	public TokenCategory category()
	{
		return category;
	}

	/**
	 * Returns the {@code TokenType} that is represented by the string {@code rep}.
	 *
	 * @param rep the string representing the {@code TokenType}, checks {@code rep} indeed
	 *            represents a valid {@code TokenType}
	 * @return the {@code TokenType} represented by the string {@code rep}
	 */
	public static TokenType getTypeFromString(String rep)
	{
		return stringToTypeMap.get(rep);
	}

	@Override
	public String toString()
	{
		return stringRep;
	}
}

