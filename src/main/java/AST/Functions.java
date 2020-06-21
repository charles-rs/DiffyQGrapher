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

	Functions inverse()
	{
		switch (this)
		{
			case SEC:
				return ASEC;
			case CSC:
				return ACSC;
			case COT:
				return ACOT;
			case COS:
				return ACOS;
			case SIN:
				return ASIN;
			case TAN:
				return ATAN;
			case ACOS:
				return COS;
			case ACOT:
				return COT;
			case ACSC:
				return CSC;
			case ASEC:
				return SEC;
			case ASIN:
				return SIN;
			case ATAN:
				return TAN;
			case COSH:
				return ACOSH;
			case COTH:
				return ACOTH;
			case CSCH:
				return ACSCH;
			case IDEN:
				return IDEN;
			case SECH:
				return ASECH;
			case SINH:
				return ASINH;
			case TANH:
				return ATANH;
			case ACOSH:
				return COSH;
			case ACOTH:
				return COTH;
			case ACSCH:
				return CSCH;
			case ASECH:
				return SECH;
			case ASINH:
				return SINH;
			case ATANH:
				return TANH;
		}
		throw new UnsupportedOperationException();
	}


	Functions(String st)
	{
		stringRep = st;
	}

	String latexRep ()
	{
		switch(this)
		{
			case ABS:
			case POW:
				throw new UnsupportedOperationException();
			case SECH:
			case CSCH:
			case COTH:
				return "\\text{" + this.stringRep + "}";
			case ASECH:
			case ACSCH:
			case ACOTH:
			case ACOS:
			case ASIN:
			case ATAN:
			case ASEC:
			case ACSC:
			case ACOT:
			case ASINH:
			case ACOSH:
			case ATANH:
				return this.inverse().latexRep() + "^{-1}";
			case SQRT:
				return "\\sqrt";
			case SIGN:
				return "\\text{sgn}";
			case IDEN:
				return "";
			default:
				return "\\" + this.stringRep;
		}
	}

}
