package FXObjects;

import Evaluation.CritPointTypes;
import Evaluation.CriticalPoint;
import Exceptions.BadSaddleTransversalException;
import Exceptions.RootNotFound;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;


public class SaddleConTransversal implements Cloneable
{
	final boolean homo;
	CriticalPoint central;
	CriticalPoint saddle;
	CriticalPoint s1, s2;
	private  static OutputPlane o;
	final Point2D p1, p2;
//	final boolean stat;
	final Mode mode;
	public enum Mode
	{
		DYNAMIC, STATIC, FIXEDDIR
	};
	private SaddleConTransversal(CriticalPoint central, CriticalPoint saddle, CriticalPoint s1, CriticalPoint s2,
								 boolean homo, Point2D p1, Point2D p2, Mode mode)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.mode = mode;
		this.homo = homo;
		try
		{
			this.central = central.clone();
		} catch (NullPointerException ignored) {}
		try
		{
			this.saddle = saddle.clone();
		} catch (NullPointerException ignored) {}
		try
		{
			this.s1 = s1.clone();
			this.s2 = s2.clone();
		} catch (NullPointerException ignored) {}
	}
	public static void init (OutputPlane _o)
	{
		o = _o;
	}


	SaddleConTransversal(Point2D relativeDir, CriticalPoint saddle) throws BadSaddleTransversalException
	{
		if(saddle == null || saddle.type != CritPointTypes.SADDLE) throw new BadSaddleTransversalException();
		homo = true;
		mode = Mode.FIXEDDIR;
		p1 = relativeDir.normalize();
		p2 = null;
//		this.saddle = saddle.clone();
		this.saddle = saddle;
//		System.out.println("IS THE FUCKING SADDLE NULL " + (this.saddle == null));
	}

	SaddleConTransversal(Point2D p1, Point2D p2, boolean homo)
	{
		this.mode = Mode.STATIC;
		this.p1 = p1;
		this.p2 = p2;
		this.homo = homo;
	}

	SaddleConTransversal (CriticalPoint p1, CriticalPoint p2) throws BadSaddleTransversalException
	{
		this.p1 = null;
		this.p2 = null;
		mode = Mode.DYNAMIC;
		if(p1.type == CritPointTypes.SADDLE)
		{
			if(p2.type == CritPointTypes.SADDLE)
			{
				s1 = p1;
				s2 = p2;
				homo = false;
			}
			else
			{
				saddle = p1;
				central = p2;
				homo = true;
			}
		} else if (p2.type == CritPointTypes.SADDLE)
		{
			saddle = p2;
			central = p1;
			homo = true;
		} else throw new BadSaddleTransversalException();
	}
	Point2D getStart()
	{
		switch (mode)
		{
			case STATIC:
				return p1;
			case DYNAMIC:
				if (homo)
					return central.point;
				else
					return s1.point.midpoint(s2.point).add(4096,
							-4096D * ((s2.point.getX() - s1.point.getX()) / (s2.point.getY() - s1.point.getY())));
			case FIXEDDIR:
				return saddle.point.add(p1.multiply(.0001));
		}
		return null;
	}
	Point2D getEnd()
	{
		switch (mode)
		{
			case STATIC:
				return p2;
			case DYNAMIC:
				if (homo)
					return central.point.add(central.point.subtract(saddle.point).normalize().multiply(4096D));
				else
					return s1.point.midpoint(s2.point).add(-4096,
							4096D * ((s2.point.getX() - s1.point.getX()) / (s2.point.getY() - s1.point.getY())));
			case FIXEDDIR:
				return saddle.point.add(p1.multiply(4096));
		}
		return null;
	}

	public void update(Point2D p) throws RootNotFound
	{
		switch (mode)
		{
			case STATIC:
				return;
			case DYNAMIC:
				if (homo)
				{
					saddle = o.critical(saddle.point, p.getX(), p.getY());
					central = o.critical(central.point, p.getX(), p.getY());
				} else
				{
					s1 = o.critical(s1.point, p.getX(), p.getY());
					s2 = o.critical(s2.point, p.getX(), p.getY());
				}
				return;
			case FIXEDDIR:/*
				GraphicsContext g = o.labelCanv.getGraphicsContext2D();
				Point2D p1 = o.normToScr(getStart());
				Point2D p2 = o.normToScr(getEnd());
				g.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());*/
				if(saddle == null)
					System.out.println("encountered null saddle");
				saddle = o.critical(saddle.point, p.getX(), p.getY());
				if(saddle == null)
					System.out.println("WHAT THE ACtUAL fUcK");
				return;
		}
	}
	
	@Override
	public SaddleConTransversal clone()
	{
		return new SaddleConTransversal(central, saddle, s1, s2, homo, p1, p2, mode);
	}
}
