package FXObjects;

import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
import Exceptions.NoMoreXException;
import javafx.geometry.Point2D;

import java.util.concurrent.atomic.AtomicBoolean;


public class BasinFinder extends Thread
{
	private static OutputPlane o;
	private static Point2D crit;
	private static AtomicBoolean doneLeft, doneRight;
	private static Double left, right;
	private static Side s;
	private static double inc;
	private static java.awt.Color col;


	@Override
	public void run()
	{
		Evaluator e = EvaluatorFactory.getEvaluator(o.evalType, o.getDx(), o.getDy());
		try
		{
			boolean foundAny;
			double x;
			xAndSide XAS;
			while(true)
			{
				XAS = getNextX();
				x = XAS.x;
				foundAny = false;
				double incY = (o.yMax.get() - o.yMin.get())/512D; //TODO change 512 to var
				for (double y = o.yMin.get(); y < o.yMax.get(); y += incY)
				{
					e.initialise(x, y, o.getT(), o.a, o.b, inc);
					while (o.inBoundsSaddle(e.getCurrent()) && e.getT() < 100)
					{
						if (e.next().distance(crit) < inc)
						{
							foundAny = true;
							synchronized (o.g)
							{
								o.g.setColor(col);
								o.g.fillRect(o.imgNormToScrX(x), o.imgNormToScrY(y), 2, 2);
							}
							break;
						}
					}
					}
				o.render();
				if(!foundAny)
				{
					switch (XAS.s)
					{
						case RIGHT:
							doneRight.set(true);
							break;
						case LEFT:
							doneLeft.set(true);
							break;
					}
				}
			}
		} catch (NoMoreXException x)
		{
			System.out.println("done");
		}


	}

	public static void init(OutputPlane _o, Point2D _crit)
	{
		doneLeft = new AtomicBoolean(false);
		doneRight = new AtomicBoolean(false);
		o = _o;
		crit = _crit;
		s = Side.LEFT;
		inc = (o.yMax.get() - o.yMin.get())/512D;
		right = crit.getY();
		left = crit.getY() - inc;
		col = new java.awt.Color(((crit.hashCode()) & ((~0) >>> 8)) | (1 << 30), true);
	}

	private synchronized static xAndSide getNextX() throws NoMoreXException
	{
		if(doneLeft.get() && doneRight.get()) throw new NoMoreXException();
		double temp = 0;
		Side sTemp = s;
		switch (s)
		{
			case LEFT:
				synchronized (left)
				{
					temp = left;
					left += inc;
				}
				if(!doneRight.get()) s = Side.RIGHT;
				break;
			case RIGHT:
				synchronized (right)
				{
					temp = right;
					right -= inc;
				}
				if(!doneLeft.get()) s = Side.LEFT;
				break;
		}


		return new xAndSide(temp, sTemp);
	}

	private enum Side
	{
		LEFT,RIGHT;
	}
	static class xAndSide
	{
		double x;
		Side s;
		xAndSide(double x, Side s)
		{
			this.x = x;
			this.s = s;
		}
	}
}
