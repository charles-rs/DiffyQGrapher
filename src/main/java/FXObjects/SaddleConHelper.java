package FXObjects;

import AST.Node;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.awt.*;

public class SaddleConHelper extends Thread
{
	private static OutputPlane o;
	private static Thread parent;
	private Point2D prev, next, prevOld;
	private SaddleConTransversal transversal;
	private SepStart s1, s2;
	private Color col;

	SaddleConHelper(Point2D st, Point2D nx, SaddleConTransversal transversal, SepStart s1, SepStart s2)
	{
		prev = st;
		next = nx;
		this.transversal = transversal;
		this.s1 = s1;
		this.s2 = s2;
		if(transversal.homo) col = o.in.awtHomoSaddleConColor;
		else col = o.in.awtHeteroSaddleConColor;
	}
	static void init(OutputPlane _o, Thread _parent)
	{
		o = _o;
		parent = _parent;
	}

	@Override
	public void run()
	{

		o.in.drawLine(prev, next, col, 3);
		Platform.runLater(o.in::render);
		prevOld = prev;
		prev = next;
		Node tr = o.getDy().diff('y').add(o.getDx().diff('x'));
		double prevTr, nextTr = 0;
		boolean excepted = false;
		if(transversal.homo)
		{
			try
			{
				prevTr = tr.eval(s1.saddle.point.getX(), s1.saddle.point.getY(), prevOld.getX(), prevOld.getY(), 0);
				nextTr = tr.eval(s1.saddle.point.getX(), s1.saddle.point.getY(), prev.getX(), prev.getY(), 0);
				if (Math.signum(prevTr) != Math.signum(nextTr))
					o.in.degenSaddleCons.add(prevOld.midpoint(prev));

			} catch (EvaluationException e)
			{
				excepted = true;
			}
		}
		while (o.in.inBounds(prev.getX(), prev.getY()) && !parent.isInterrupted() && !Thread.interrupted())
		{
			try
			{

				next = o.saddleConMidpointPath(s1, s2, prev, prevOld, transversal);
				s1 = s1.updateSaddle(o.critical(s1.saddle.point, next));
				s2 = s2.updateSaddle(o.critical(s2.saddle.point, next));
				if(transversal.homo && !excepted)
				{
					try
					{
						prevTr = nextTr;
						nextTr = tr.eval(s1.saddle.point.getX(), s1.saddle.point.getY(), next.getX(), next.getY(), 0);
						if (Math.signum(prevTr) != Math.signum(nextTr))
							o.in.degenSaddleCons.add(prevOld.midpoint(prev));
					} catch (EvaluationException e)
					{
						excepted = true;
					}
				}
				transversal.update(next);
				o.in.drawLine(prev, next, col, 3);
				Platform.runLater(o.in::render);
				prevOld = prev;
				prev = next;
				System.out.println(prev);
			} catch (RootNotFound r)
			{
				System.out.println("breaking");
				break;
			}
		}
	}
}
