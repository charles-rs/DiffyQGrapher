package AST;

import Exceptions.EvaluationException;

public class Value extends Node
{
	ValueTypes type;
	Double val;
	public Value(ValueTypes v)
	{
		type = v;
		val = null;
	}
	public Value(Double b)
	{
		type = ValueTypes.NUMBER;
		val = b;
	}
	@Override
	public StringBuilder prettyPrint(StringBuilder sb)
	{
		switch (type)
		{
			case NUMBER:
				sb.append(val);
				break;
			case A:
				sb.append("a");
				break;
			case B:
				sb.append("b");
				break;
			case T:
				sb.append("t");
				break;
			case X:
				sb.append("x");
				break;
			case Y:
				sb.append("y");
				break;
		}
		return sb;
	}

	@Override
	public double eval(double x, double y, double a, double b, double t) throws EvaluationException
	{
		switch (type)
		{
			case NUMBER:
				return val;
			case A:
				return a;
			case B:
				return b;
			case T:
				return t;
			case X:
				return x;
			case Y:
				return y;
			default:
				throw new EvaluationException("Error evaluating a value");
		}
	}

	@Override
	public Node differentiate(char c)
	{
		switch (type)
		{
			case X:
				if(c == 'x') return new Value(1.);
				else return new Value(0.);
			case Y:
				if(c == 'y') return new Value(1.);
				else return new Value(0.);
			case T:
				if(c == 't') return new Value(1.);
				else return new Value(0.);
			default:
				return new Value(0.);
		}
	}

	@Override
	public Node clone()
	{
		if(type == ValueTypes.NUMBER)
			return new Value(val);
		else
			return new Value(type);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Value)
		{
			return this.type.equals(((Value) other).type) && this.val.equals(((Value) other).val);
		} else return false;
	}
}
