package AST;

import Exceptions.EvaluationException;

public class Derivative extends Node
{
	Node diff, val;

	public Derivative(Node deriv, Node expr)
	{
		diff = deriv;
		val = expr;
	}
	public char getType()
	{
		return ((Deriv) diff).getSig();
	}
	@Override
	public StringBuilder prettyPrint(StringBuilder sb)
	{
		diff.prettyPrint(sb);
		val.prettyPrint(sb);
		return sb;
	}

	@Override
	public double eval(double x, double y, double a, double b, double t) throws EvaluationException
	{
		return val.eval(x, y, a, b, t);
	}

	@Override
	public Node differentiate(char c)
	{
		return val.differentiate(c);
	}

	@Override
	public Node collapse()
	{
		return new Derivative(diff, val.collapse());
	}

	@Override
	public Node clone()
	{
		return new Derivative(diff.clone(), val.clone());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Derivative)
		{
			return this.diff.equals(((Derivative) other).diff) && this.val.equals(((Derivative) other).val);
		} else return false;
	}
}
