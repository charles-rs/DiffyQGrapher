package AST;

import Exceptions.EvaluationException;

public class Function extends Node
{
	private Functions type;
	private Node v, base;
	public Function(Functions t, Node val)
	{
		v = val;
		type = t;
		base = null;
	}
	public Function(Node b, Node expt)
	{
		base = b;
		v = expt;
		type = Functions.POW;
	}

	public StringBuilder prettyPrint(StringBuilder sb)
	{
		if (type == Functions.POW)
		{
			base.prettyPrint(sb);
			sb.append("^");
			v.prettyPrint(sb);
		} else if (type == Functions.ABS)
		{
			sb.append("|");
			v.prettyPrint(sb);
			sb.append("|");
		} else
		{
			sb.append(type.stringRep);
			sb.append("(");
			v.prettyPrint(sb);
			sb.append(")");
		}
		return sb;
	}

	@Override
	public double eval(double x, double y, double a, double b, double t) throws EvaluationException
	{
		double val = v.eval(x, y, a, b, t);
		switch (type)
		{
			case SIN:
				return Math.sin(val);
			case COS:
				return Math.cos(val);
			case TAN:
				return Math.tan(val);
			case SEC:
				return 1/Math.cos(val);
			case CSC:
				return 1/Math.sin(val);
			case COT:
				return 1/Math.tan(val);
			case ASIN:
				return Math.asin(val);
			case ACOS:
				return Math.acos(val);
			case ATAN:
				return Math.atan(val);
			case ASEC:
				return Math.acos(1/val);
			case ACSC:
				return Math.asin(1/val);
			case ACOT:
				return Math.atan(1/val);
			case SINH:
				return Math.sinh(val);
			case COSH:
				return Math.cosh(val);
			case TANH:
				return Math.tanh(val);
			case SECH:
				return 1/Math.cosh(val);
			case CSCH:
				return 1/Math.sinh(val);
			case COTH:
				return 1/Math.tanh(val);
			case ASINH:
				return asinh(val);
			case ACOSH:
				return acosh(val);
			case ATANH:
				return atanh(val);
			case ASECH:
				return 1/acosh(val);
			case ACSCH:
				return 1/asinh(val);
			case ACOTH:
				return 1/atanh(val);
			case LOG:
				return Math.log10(val);
			case LN:
				return Math.log(val);
			case SQRT:
				return Math.sqrt(val);
			case POW:
				return Math.pow(base.eval(x, y, a, b, t), val);
			case IDEN:
				return val;
			case ABS:
				return Math.abs(val);
			case SIGN:
				if(val > 0) return 1; else return -1;
			default:
				throw new EvaluationException("Error evaluating a function");
		}

	}

	@Override
	public Node differentiate(char c)
	{
		Node diff = null;
		Node inner = v.clone();
		switch (type)
		{
			case SIN:
				diff = new Function(Functions.COS, inner);
				break;
			case COS:
				diff = new Term(new Value(-1.), new Function(Functions.SIN, inner), '*');
				break;
			case TAN:
				diff = new Function(new Function(Functions.SEC, inner), new Value(2.));
				break;
			case SEC:
				diff = new Term(new Function(Functions.SEC, inner), new Function(Functions.TAN, inner), '*');
				break;
			case CSC:
				diff = new Term(new Value(-1.), new Term(new Function(Functions.CSC, inner), new Function(Functions.COT, inner), '*'), '*');
				break;
			case COT:
				diff = new Term(new Value(-1.), new Function(new Function(Functions.CSC, inner), new Value(2.)), '*');
				break;
			case ASIN:
				diff = new Term(new Value(1.), new Function(Functions.SQRT, new Expression(new Value(1.), new Function(inner, new Value(2.)), '-')), '/');
				break;
			case ACOS:
				diff = new Term(new Value(-1.), new Function(Functions.SQRT, new Expression(new Value(1.), new Function(inner, new Value(2.)), '-')), '/');
				break;
			case ATAN:
				diff = new Term(new Value(1.),new Expression(new Value(1.), new Function(inner, new Value(2.)), '+'),'/');
				break;
			case ASEC:
				diff = new Term(new Value(1.), new Term(inner, new Function(Functions.SQRT, new Expression(new Function(inner, new Value(2.)), new Value(1.), '-')),'*'),'/');
				break;
			case ACSC:
				diff = new Term(new Value(-1.), new Term(new Function(Functions.ABS, inner), new Function(Functions.SQRT, new Expression(new Function(inner, new Value(2.)), new Value(1.), '-')), '*'),'/');
				break;
			case ACOT:
				diff = new Term(new Value(-1.), new Expression(new Value(1.), new Function(inner, new Value(2.)),'+'), '/');
				break;
			case SINH:
				diff = new Function(Functions.COSH, inner);
				break;
			case COSH:
				diff = new Function(Functions.SINH, inner);
				break;
			case TANH:
				diff = new Expression(new Value(-1.) , new Function(new Function(Functions.TANH, inner), new Value(2.)), '-');
				break;
			case SECH:
				diff = new Term(new Value(-1.), new Term(new Function(Functions.TANH, inner), new Function(Functions.SECH, inner), '*'), '*');
				break;
			case CSCH:
				diff = new Term(new Value(-1.), new Term(new Function(Functions.COTH, inner), new Function(Functions.CSCH, inner), '*'), '*');
				break;
			case COTH:
				diff = new Expression(new Value(-1.) , new Function(new Function(Functions.COTH, inner), new Value(2.)), '-');
				break;
			case ASINH:
				diff = new Term(new Value(1.), new Function(Functions.SQRT, new Expression(new Value(1.), new Function(inner, new Value(2.)), '+')) , '/');
				break;
			case ACOSH:
				diff = new Term(new Value(1.), new Function(Functions.SQRT, new Expression(new Function(inner, new Value(2.)), new Value(1.), '-')), '/');
				break;
			case ATANH:
				diff = new Term(new Value(1.), new Expression(new Value(1.), new Function(inner, new Value(2.)), '-'), '/');
				break;
			case ASECH:
				diff = new Term(new Value(-1.), new Term(inner, new Function(Functions.SQRT, new Expression(new Value(1.), new Function(inner, new Value(2.)), '-')), '*'), '/');
				break;
			case ACSCH:
				diff = new Term(new Value(-1.), new Term(new Function(Functions.ABS, inner), new Function(Functions.SQRT, new Expression(new Value(1.), new Function(inner, new Value(2.)), '+')), '*'),'/');
				break;
			case ACOTH:
				diff = new Term(new Value(-1.), new Expression(new Function(inner, new Value(2.)), new Value(1.), '-'), '/');
				break;
			case LOG:
				diff = new Term(new Value(1.), new Term(new Value(Math.log(10)), inner, '*'), '/');
				break;
			case LN:
				diff = new Term(new Value(1.), inner, '/');
				break;
			case SQRT:
				diff = new Term(new Value(1.), new Term(new Value(2.), new Function(Functions.SQRT, inner), '*'), '/');
				break;
			case POW:
				diff = new Term(inner.clone(), new Function(base.clone(), new Expression(inner, new Value(1.), '-')), '*');
				break;
			case IDEN:
				diff = new Value(1.);
				break;
			case SIGN:
				diff = new Value(0.);
				break;
			case ABS:
				diff = new Function(Functions.SIGN, inner);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + type);
		}
		if(type == Functions.POW)
			return new Term(diff, base.differentiate(c), '*');
		else
			return new Term(diff, inner.differentiate(c), '*');
	}

	@Override
	public Node clone()
	{
		if(type == Functions.POW)
			return new Function(base, v);
		else
			return new Function(type, v.clone());
	}

	@Override
	public Node collapse()
	{
		if(this.type == Functions.POW)
		{
			if(v instanceof Value && ((Value) v).type == ValueTypes.NUMBER)
			{
				if (((Value) v).val.equals(0.)) return new Value(1.);
				else if (((Value) v).val.equals(1.)) return base.collapse();
			}
			return new Function(base.collapse(), v.collapse());
		} else return new Function(type, v.collapse());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Function)
		{
			return this.type == ((Function) other).type && this.base.equals(((Function) other).base) && this.v.equals(((Function) other).v);
		} else return false;
	}

	private double asinh(double x)
	{
		return Math.log(x + Math.sqrt(x * x + 1));
	}
	private double acosh(double x)
	{
		return Math.log(x + Math.sqrt(x * x - 1));
	}
	private double atanh(double x)
	{
		return 0.5 * Math.log( (x + 1.0) / (x - 1.0));
	}

}
