package PathGenerators;

import javafx.geometry.Point2D;

public class MidpointArcGenerator extends MidpointPathGeneratorImpl
{
	final double radius;
	double thetaLeft, thetaRight, thetaCenter;
	final Point2D center;
	MidpointArcGenerator(Point2D center, double prec, double thetaLeft, double thetaRight, double r)
	{
		super(prec);
		this.radius = r;
		this.thetaLeft = thetaLeft;
		this.thetaRight = thetaRight;
		this.thetaCenter = (thetaLeft + thetaRight)/2;
		this.center = center;
		current = new MidpointSegment(fromPolar(thetaLeft), fromPolar(thetaRight), fromPolar(thetaCenter));

	}

	@Override
	public MidpointSegment getNext(Side s)
	{
		switch (s)
		{
			case LEFT:
				thetaRight = thetaCenter;
				thetaCenter = (thetaLeft + thetaRight)/2;
				current = new MidpointSegment(current.left, current.center, fromPolar(thetaCenter));
				break;
			case RIGHT:
				thetaLeft = thetaCenter;
				thetaCenter = (thetaLeft + thetaRight)/2;
				current = new MidpointSegment(current.center, current.right, fromPolar(thetaCenter));
				break;
			default:
				throw new IllegalArgumentException();
		}
		System.out.println("thetaLeft: " + thetaLeft + "\nthetaCenter: " + thetaCenter + "\nthetaRight: " + thetaRight);
		return current;
	}

	@Override
	public void refine()
	{
		thetaRight += Math.asin(precision/(2 * radius));//(thetaCenter - thetaRight)/10;
		thetaLeft -= Math.asin(precision/(2 * radius));//(thetaLeft - thetaCenter/10);
		current = new MidpointSegment(fromPolar(thetaLeft), fromPolar(thetaRight), current.center);
		System.out.println("refining to left: " + thetaLeft + "and right: " + thetaRight);
	}

	@Override
	public void refine(Side s)
	{
		switch (s)
		{
			case LEFT:
				thetaRight = (thetaCenter + thetaRight)/2;
				thetaCenter = (thetaLeft + thetaRight)/2;
				current = new MidpointSegment(current.left, fromPolar(thetaRight), fromPolar(thetaCenter));
				break;
			case RIGHT:
				thetaLeft = (thetaCenter + thetaLeft)/2;
				thetaCenter = (thetaLeft + thetaRight)/2;
				current = new MidpointSegment(fromPolar(thetaLeft), current.right, fromPolar(thetaCenter));
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	private Point2D fromPolar(double theta)
	{
		return center.add(radius * Math.cos(theta), radius * Math.sin(theta));
	}
}
