package FXObjects;

import Exceptions.RootNotFound;
import PathGenerators.FinitePathType;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.awt.*;

public class SaddleConHelper extends Thread
{
	private static OutputPlane o;
	private static Thread parent;
	private Point2D prev, next, prevOld;
	private SaddleConTransversal transversal;
	private sepStart s1, s2;
	private Color col;

	SaddleConHelper(Point2D st, Point2D nx, SaddleConTransversal transversal, sepStart s1, sepStart s2)
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
		while (o.in.inBounds(prev.getX(), prev.getY()) && !parent.isInterrupted())
		{
			try
			{
				next = o.saddleConFinitePath(s1, s2, prev.getX(), prev.getY(), FinitePathType.ARC, prevOld, transversal);
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
