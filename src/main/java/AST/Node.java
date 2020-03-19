package AST;

import Exceptions.EvaluationException;

public abstract class Node implements Cloneable
{
	public abstract StringBuilder prettyPrint(StringBuilder sb);

	public abstract double eval(double x, double y, double a, double b, double t) throws EvaluationException;

	public abstract Node differentiate(char c);

	public Node collapse()
	{
		return this;
	}

	public abstract Node clone();
}
