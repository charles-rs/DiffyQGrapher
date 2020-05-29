package AST;

/**
 * enum for every supported type of function
 * Every function has a string representation for pretty printing ease.
 */

public enum Functions
{
	SIN("sin"),
	COS("cos"),
	TAN("tan"),
	SEC("sec"),
	CSC("csc"),
	COT("cot"),

	ASIN("arcsin"),
	ACOS("arccos"),
	ATAN("arctan"),
	ASEC("arcsec"),
	ACSC("arccsc"),
	ACOT("arccot"),

	SINH("sinh"),
	COSH("cosh"),
	TANH("tanh"),
	SECH("sech"),
	CSCH("csch"),
	COTH("coth"),

	ASINH("arcsinh"),
	ACOSH("arccosh"),
	ATANH("arctanh"),
	ASECH("arcsech"),
	ACSCH("arccsch"),
	ACOTH("arccoth"),

	LOG("log"),
	LN("ln"),

	SQRT("sqrt"),

	POW("^"),

	ABS("|"),
	SIGN("sign"),

	IDEN("");
	String stringRep;


	Functions(String st)
	{
		stringRep = st;
	}

}
