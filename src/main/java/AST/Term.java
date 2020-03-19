package AST;

import Exceptions.EvaluationException;

public class Term extends Node
{
	private Node a;
	private Node b;
	private char op;

	public Term(Node a, Node b, char op)
	{
		this.a = a;
		this.b = b;
		this.op = op;
	}

	@Override
	public StringBuilder prettyPrint(StringBuilder sb)
	{
		a.prettyPrint(sb);
		sb.append(" " + op + " ");
		b.prettyPrint(sb);

		return sb;
	}

	@Override
	public double eval(double x, double y, double a, double b, double t) throws EvaluationException
	{
		switch (op)
		{
			case '*':
				return this.a.eval(x, y, a, b, t) * this.b.eval(x, y, a, b, t);
			case '/':
				return this.a.eval(x, y, a, b, t) / this.b.eval(x, y, a, b, t);
			default:
				throw new EvaluationException("Error evaluating a Term");
		}
	}

	@Override
	public Node differentiate(char c)
	{
		if(op == '*')
			return new Expression(new Term(a.clone(), b.differentiate(c), '*'), new Term(a.differentiate(c), b.clone(), '*'), '+');
		else if(op == '/')
			return new Term(new Expression(new Term(b.clone(), a.differentiate(c), '*'), new Term(a.clone(), b.differentiate(c), '*'), '-'), new Function(b.clone(), new Value(2.)), '/');
		else throw new IllegalStateException("bad op");
	}

	@Override
	public Node clone()
	{
		return new Term(a.clone(), b.clone(), op);
	}

	private Node rotateBack()
	{
		if(b instanceof Term)
		{
			return new Term(new Term(a, ((Term) b).a, op), ((Term) b).b, ((Term) b).op);
		}
		else return this;
	}
	public Node rotate()
	{
		Node temp = this;
		while(((Term) temp).b instanceof Term)
		{
			temp = ((Term)temp).rotateBack();
		}
		return temp;
	}

	@Override
	public Node collapse()
	{
		Node collapsedA = a.collapse();
		Node collapsedB = b.collapse();
		if(collapsedA instanceof Value && ((Value) collapsedA).type == ValueTypes.NUMBER && ((Value) collapsedA).val.equals(0.) ||
				collapsedB instanceof Value && ((Value) collapsedB).type == ValueTypes.NUMBER && ((Value) collapsedB).val.equals(0.) && this.op == '*')
		{
			return new Value(0.);
		} else if(collapsedA instanceof Value && ((Value) collapsedA).type == ValueTypes.NUMBER && ((Value) collapsedA).val.equals(1.) && this.op == '*')
		{
			return collapsedB;
		} else if(collapsedB instanceof Value && ((Value) collapsedB).type == ValueTypes.NUMBER && ((Value) collapsedB).val.equals(1.))
		{
			return collapsedA;
		}
		else if(collapsedA instanceof Value && ((Value) collapsedA).type == ValueTypes.NUMBER &&
				collapsedB instanceof Value && ((Value) collapsedB).type == ValueTypes.NUMBER)
		{
			if(op == '*')
				return new Value(((Value) collapsedA).val * ((Value) collapsedB).val);
			else if (op == '/')
				return new Value(((Value) collapsedA).val / ((Value) collapsedB).val);
			else return null;
		} else
		{
			return new Term(collapsedA, collapsedB, this.op);
		}
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Term)
		{
			return this.a.equals(((Term) other).a) && this.b.equals(((Term) other).b) && this.op == ((Term) other).op;
		} else return false;
	}
}
