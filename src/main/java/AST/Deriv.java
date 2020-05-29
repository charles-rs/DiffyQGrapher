package AST;


import Exceptions.EvaluationException;

/**
 * A class to represent the left hand side of a derivative, for example "dx/dt"
 * (really there isn't any information here beyond one character)
 * invariant: sig is either x or y
 */
public class Deriv extends Node
{
	private final char sig;

	public Deriv(char c)
	{
		sig = c;
	}

	/**
	 * Gets the character that represents the upper differential of this derivative
	 * @return the char (x or y)
	 */
	public char getSig()
	{
		return sig;
	}
	@Override
	public StringBuilder prettyPrint(StringBuilder sb)
	{
		sb.append('d');
		sb.append(sig);
		sb.append("/dt = ");
		return sb;
	}

	@Override
	public double eval(double x, double y, double a, double b, double t) throws EvaluationException
	{
		 throw new EvaluationException("You may not evaluate this kind of node");
	}

	@Override
	public Node differentiate(char c)
	{
		throw new Error("You may not differentiate this kind of node");
	}


	@Override
	public Node clone()
	{
		return new Deriv(sig);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Deriv)
		{
			return this.sig == ((Deriv) other).sig;
		} else return false;
	}
}
