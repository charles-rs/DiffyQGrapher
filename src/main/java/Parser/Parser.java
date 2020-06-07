package Parser;

import AST.*;
import Exceptions.SyntaxError;

public class Parser
{

	/*public static void main(String[] args) throws SyntaxError
	{
		Reader r = new StringReader("dx/dt = 1 + 2 + a + 3 + 4\n");
		Tokenizer t = new Tokenizer(r);
		Derivative dr = parseDerivative(t);
		Node d = dr.differentiate('y');
		System.out.println(dr.prettyPrint(new StringBuilder()));
		System.out.println(dr.collapse().prettyPrint(new StringBuilder()));
		//System.out.println(d.prettyPrint(new StringBuilder()));
		//System.out.println(d.collapse().prettyPrint(new StringBuilder()));
		try
		{
			System.out.println(d.eval(.8, .3, 2, 14, 4));
			System.out.println(d.collapse().eval(.8, .3, 2, 14, 4));
		} catch (EvaluationException ex)
		{
			ex.printStackTrace();
		}
	}*/


	static public Derivative parseDerivative(Tokenizer t) throws SyntaxError
	{
		while(t.peek().getType() == TokenType.NL)
			consume(t, TokenType.NL);
		Derivative temp = new Derivative(parseDeriv(t), parseExpression(t));
		consume(t, TokenType.NL);
		return temp;
	}


	private static Node parseExpression(Tokenizer t) throws SyntaxError
	{
		Node temp = parseExpressionBack(t);
		if(temp instanceof Expression)
			return ((Expression) temp).rotate();
		else return temp;
	}

	private static Node parseExpressionBack(Tokenizer t) throws SyntaxError
	{
		Node temp = parseTerm(t);
		char op;
		if(t.peek().getType() == TokenType.PLUS)
		{
			consume(t, TokenType.PLUS);
			op = '+';
		}
		else if(t.peek().getType() == TokenType.MINUS)
		{
			consume(t, TokenType.MINUS);
			op = '-';
		}
		else return temp;
		if(t.peek().getType() != TokenType.RPAREN &&
				t.peek().getType() != TokenType.NL &&
				t.peek().getType() != TokenType.EOF &&
				t.peek().getType() != TokenType.DX &&
				t.peek().getType() != TokenType.DY)
			return new Expression(temp, parseExpressionBack(t), op);
		else return temp;
	}

	private static Node parseTerm(Tokenizer t) throws SyntaxError
	{
		Node temp = parseTermBack(t);
		if(temp instanceof Term)
			return ((Term) temp).rotate();
		else return temp;
	}


	private static Node parseTermBack(Tokenizer t) throws SyntaxError
	{
		Node temp = parseFunction(t);
		char op;
		if(t.peek().getType() == TokenType.DIV)
		{
			consume(t, TokenType.DIV);
			op = '/';
		}
		else op = '*';
		if(t.peek().getType() == TokenType.MUL) consume(t, TokenType.MUL);
		if(t.peek().getType() != TokenType.RPAREN && t.peek().getType() != TokenType.MINUS &&
				t.peek().getType() != TokenType.PLUS && t.peek().getType() != TokenType.NL && t.peek().getType() != TokenType.EOF)
			return new Term(temp, parseTermBack(t), op);
		else return temp;
	}


	private static Node parseFunction(Tokenizer t) throws SyntaxError
	{
		Token peek;
		Node temp;
		if(t.peek().getCategory() == TokenCategory.FUNC)
		{
			peek = t.next();
			temp = parseValue(t);
		}
		else
		{
			temp =  parseValue(t);
			if(t.peek().getType() == TokenType.POW)
			{
				consume(t, TokenType.POW);
				return new Function(temp, parseValue(t));
			}
			else return temp;
		}
		switch(peek.getType())
		{

			case SQRT:
				return new Function(Functions.SQRT, temp);
			case SIN:
				return new Function(Functions.SIN, temp);
			case COS:
				return new Function(Functions.COS, temp);
			case TAN:
				return new Function(Functions.TAN, temp);
			case SEC:
				return new Function(Functions.SEC, temp);
			case CSC:
				return new Function(Functions.CSC, temp);
			case COT:
				return new Function(Functions.COT, temp);
			case ASIN:
				return new Function(Functions.ASIN, temp);
			case ACOS:
				return new Function(Functions.ACOS, temp);
			case ATAN:
				return new Function(Functions.ATAN, temp);
			case ASEC:
				return new Function(Functions.ASEC, temp);
			case ACSC:
				return new Function(Functions.ACSC, temp);
			case ACOT:
				return new Function(Functions.ACOT, temp);
			case SINH:
				return new Function(Functions.SINH, temp);
			case COSH:
				return new Function(Functions.COSH, temp);
			case TANH:
				return new Function(Functions.TANH, temp);
			case SECH:
				return new Function(Functions.SECH, temp);
			case CSCH:
				return new Function(Functions.CSCH, temp);
			case COTH:
				return new Function(Functions.COTH, temp);
			case ASINH:
				return new Function(Functions.ASINH, temp);
			case ACOSH:
				return new Function(Functions.ACOSH, temp);
			case ATANH:
				return new Function(Functions.ATANH, temp);
			case ASECH:
				return new Function(Functions.ASECH, temp);
			case ACSCH:
				return new Function(Functions.ACSCH, temp);
			case ACOTH:
				return new Function(Functions.ACOTH, temp);
			case LOG:
				return new Function(Functions.LOG, temp);
			case LN:
				return new Function(Functions.LN, temp);
			default:
				throw new SyntaxError("bad function");
		}
	}

	private static Node parseDeriv(Tokenizer t) throws SyntaxError
	{
		Token temp = t.next();
		Deriv builder;
		if(temp.getType() == TokenType.DY)
			builder = new Deriv('y');
		else if (temp.getType() == TokenType.DX)
			builder = new Deriv('x');
		else throw new SyntaxError("bad differential: " + temp.toString());
		consume(t, TokenType.DIV);
		consume(t, TokenType.DT);
		consume(t, TokenType.EQ);
		return builder;
	}

	private static Node parseValue(Tokenizer t) throws SyntaxError
	{
		Token temp = t.next();
		switch (temp.getType())
		{
			case NUM:
				return new Value(temp.toNumToken().getValue());
			case A:
				return new Value(ValueTypes.A);
			case B:
				return new Value(ValueTypes.B);
			case T:
				return new Value(ValueTypes.T);
			case E:
				return new Value(Math.E);
			case PI:
				return new Value(Math.PI);
			case X:
				return new Value(ValueTypes.X);
			case Y:
				return new Value(ValueTypes.Y);
			case LPAREN:
				Node temp1 =  parseExpression(t);
				consume(t, TokenType.RPAREN);
				return temp1;
			case MINUS:
				return new Term(new Value(-1.0), parseValue(t), '*');
			default:
				throw new SyntaxError(temp.toString() + " is not a valid value");
		}
	}


	private static void consume(Tokenizer t, TokenType tt) throws SyntaxError
	{
		if (t.peek().getType() == tt) t.next();
		else throw new SyntaxError("tried to consume the wrong type");
	}

}
