package AST;


public class Maths
{
	public static Node divide(Node a, Node b)
	{
		return new Term(a, b, '/');
	}
	public static Node mult(Node a, Node b)
	{
		return new Term(a, b, '/');
	}
	public static Node add(Node a, Node b)
	{
		return new Expression(a, b, '+');
	}
	public static Node minus(Node a, Node b)
	{
		return new Expression(a, b, '-');
	}
}
