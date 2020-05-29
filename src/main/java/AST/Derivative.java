package AST;

import Exceptions.EvaluationException;

/**
 * Class to represent one whole derivative.
 * Example: dy/dt = x^2 - a t + 4
 */
public class Derivative extends Node
{
	private final Node diff, val;

	public Derivative(Node deriv, Node expr)
	{
		diff = deriv;
		val = expr;
	}

	/**
	 * Returns the character that determines which derivative this is.
	 * @return The char of the upper differential, be it x or y.
	 */
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
