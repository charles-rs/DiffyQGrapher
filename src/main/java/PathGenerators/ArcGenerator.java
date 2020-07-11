package PathGenerators;

import javafx.geometry.Point2D;

public class ArcGenerator extends FinitePathGeneratorImpl
{
	final double thetaEnd, radius;
	double theta;
	double thetaInc;
	Point2D center;

	/**
	 * Constructor for an arc generator
	 * @implNote always goes anti-clockwise
	 * @param inc the increment along the arc
	 * @param center the center point
	 * @param rad the radius
	 * @param thetaStart the starting theta
	 * @param thetaEnd the ending theta
	 */
	protected ArcGenerator(double inc, Point2D center, double rad, double thetaStart, double thetaEnd)
	{
		super(inc, center.add(new Point2D(rad * Math.cos(thetaStart), rad * Math.sin(thetaStart))));
		this.center = center;
		this.theta = thetaStart;
		if(thetaEnd < thetaStart)
			thetaEnd += 2 * Math.PI;
		this.thetaEnd = thetaEnd;
		this.radius = rad;
		this.thetaInc = Math.asin(inc/(2 * rad));
	}

	@Override
	public boolean done()
	{
		return theta >= thetaEnd;
	}


	@Override
	public Point2D next()
	{
		if(!done())
		{
			theta += thetaInc;
			current = center.add(new Point2D(radius * Math.cos(theta), radius * Math.sin(theta)));
		}
		return current;
	}
}
