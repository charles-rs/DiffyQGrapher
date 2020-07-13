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
		this.thetaCenter = (thetaLeft - thetaRight);
		current = new MidpointSegment(
				center.add(r * Math.cos(thetaLeft), r * Math.sin(thetaLeft)),
				center.add(r * Math.cos(thetaRight), r * Math.sin(thetaRight)),
				center.add(r * Math.cos(thetaCenter), r * Math.sin(thetaCenter)));
		this.center = center;
	}

	@Override
	public MidpointSegment getNext(Side s)
	{
		switch (s)
		{
			case LEFT:
				thetaRight = thetaCenter;
				thetaCenter = thetaLeft - thetaRight;
				current = new MidpointSegment(current.left, current.center,
						center.add(radius * Math.cos(thetaCenter), radius * Math.sin(thetaCenter)));
				break;
			case RIGHT:
				thetaLeft = thetaCenter;
				thetaCenter = thetaLeft - thetaRight;
				current = new MidpointSegment(current.center, current.right,
						center.add(radius * Math.cos(thetaCenter), radius * Math.sin(thetaCenter)));
				break;
			default:
				throw new IllegalArgumentException();
		}
		return current;
	}
}
