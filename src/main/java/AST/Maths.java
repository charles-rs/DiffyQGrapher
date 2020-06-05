package AST;

/**
 * a few basic math functions for the AST.
 * British name since 'Math' is kinda taken.
 */

public class Maths
{
	/**
	 * Makes a new node dividing a by b
	 * @param a the numerator
	 * @param b the denominator
	 * @return a brand new fraction: a/b
	 */
	public static Node divide(Node a, Node b)
	{
		return new Term(a, b, '/');
	}

	/**
	 * Makes a new node multiplying a by b
	 * @param a the first factor
	 * @param b the second factor
	 * @return a brand new product: ab
	 */
	public static Node mult(Node a, Node b)
	{
		return new Term(a, b, '*');
	}

	/**
	 * Makes a new sum adding a to b
	 * @param a the first term
	 * @param b the second term
	 * @return a brand new sum: a + b
	 */
	public static Node add(Node a, Node b)
	{
		return new Expression(a, b, '+');
	}

	/**
	 * Makes a new difference subtracting b from a
	 * @param a the first term
	 * @param b the term that gets subtracted
	 * @return a brand new difference: a - b
	 */
	public static Node minus(Node a, Node b)
	{
		return new Expression(a, b, '-');
	}
}
