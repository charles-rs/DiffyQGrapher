package AST;

import Exceptions.EvaluationException;

/**
 * Class to represent one addition operation.
 * Contains an operator and two children.
 */
public class Expression extends Node
{
	private final Node a, b;
	private final char op;

	public Expression(Node one, Node two, char c)
	{
		a = one;
		b = two;
		op = c;
	}

	@Override
	public StringBuilder prettyPrint(StringBuilder sb)
	{
		sb.append("(");
		a.prettyPrint(sb);
		sb.append(" " + op + " ");
		b.prettyPrint(sb);
		sb.append(")");
		return sb;
	}

	@Override
	public double eval(double x, double y, double a, double b, double t) throws EvaluationException
	{
		switch (op)
		{
			case '+':
				return this.a.eval(x, y, a, b, t) + this.b.eval(x, y, a, b, t);
			case '-':
				return this.a.eval(x, y, a, b, t) - this.b.eval(x, y, a, b, t);
			default:
				throw new EvaluationException("Error evaluating an expression");
		}
	}

	@Override
	public Node differentiate(char c)
	{
		return new Expression(a.differentiate(c), b.differentiate(c), op);
	}

	@Override
	public Node collapse()
	{
		Node collapsedA = a.collapse();
		Node collapsedB = b.collapse();
		if (collapsedB instanceof Value && ((Value) collapsedB).type == ValueTypes.NUMBER && ((Value) collapsedB).val.equals(0.))
		{
			return collapsedA;
		} else if (collapsedA instanceof Value && ((Value) collapsedA).type == ValueTypes.NUMBER && ((Value) collapsedA).val.equals(0.) && this.op == '+')
		{
			return collapsedB;
		} else if(collapsedA instanceof Value && ((Value) collapsedA).type == ValueTypes.NUMBER &&
				collapsedB instanceof Value && ((Value) collapsedB).type == ValueTypes.NUMBER)
		{
			if(op == '+')
				return new Value(((Value) collapsedA).val + ((Value) collapsedB).val);
			else if (op == '-')
				return new Value(((Value) collapsedA).val - ((Value) collapsedB).val);
			else return new Value(0.);
		} else
		{
			return new Expression(collapsedA, collapsedB, this.op);
		}
	}

	@Override
	public Node clone()
	{
		return new Expression(a.clone(), b.clone(), op);
	}

	private Node rotateBack()
	{
		if (b instanceof Expression)
		{
			return new Expression(new Expression(a, ((Expression) b).a, op), ((Expression) b).b, ((Expression) b).op);
		} else return this;
	}

	public Node rotate()
	{
		Node temp = this;
		while (((Expression) temp).b instanceof Expression)
		{
			temp = ((Expression) temp).rotateBack();
		}
		return temp;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Expression)
		{
			return this.a.equals(((Expression) other).a) && this.b.equals(((Expression) other).b) && this.op == ((Expression) other).op;
		} else return false;
	}
}
