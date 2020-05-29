package AST;

import Exceptions.EvaluationException;

/**
 * Abstract class of nodes in the AST for parsing differential equations.
 * Can calculate derivatives, collapse out (some) pointless nodes, and pretty print to a stringbuilder.
 * Maybe will eventually pretty print to LaTeX as well
 */
public abstract class Node implements Cloneable
{
	/**
	 * Prettyprints the node (including all it's children) to the specified stringbuilder
	 * @param sb the stringbuilder to print to
	 * @return the resulting stringbuilder
	 */
	public abstract StringBuilder prettyPrint(StringBuilder sb);

	/**
	 * Evaluates the node with the provided input.
	 * @param x the x value
	 * @param y the y value
	 * @param a the a value
	 * @param b the b value
	 * @param t the t value
	 * @return the evaluated result
	 * @throws EvaluationException whenever something goes wrong (specifically a malformed node)
	 */
	public abstract double eval(double x, double y, double a, double b, double t) throws EvaluationException;

	/**
	 * Returns the derivative of the node with respect to the given char
	 * @param c the variable to differentiate with respect to. If this isn't a, b, x, y, or t, returns 0 (as is
	 *             mathematically correct)
	 * @return the derivative
	 */
	public abstract Node differentiate(char c);

	/**
	 * Kills unnecessary children
	 * @return the collapsed node
	 */
	public Node collapse()
	{
		return this;
	}

	@Override
	public abstract Node clone();
}
