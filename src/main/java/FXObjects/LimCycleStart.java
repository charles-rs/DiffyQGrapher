package FXObjects;


import javafx.geometry.Point2D;

/**
 * This is a class to represent the starts of limit cycles. They can be solved out given a point and a sign.
 */
public class LimCycleStart
{
	/**
	 * the starting point
	 */
	public Point2D st;

	public Point2D refLine [] = new Point2D [2];

	/**
	 * whether to solve in the positive or negative direction
	 */
	public boolean isPositive;

	/**
	 * Construct a new limit cycle from a point and a boolean sign
	 * @param st the starting point
	 * @param isPos whether to solve in the positive direction or not
	 * @param lnStart the start of the refLine
	 * @param lnEnd the end of the refLine
	 */
	public LimCycleStart(Point2D st, boolean isPos, Point2D lnStart, Point2D lnEnd)
	{
		this.st = st;
		this.isPositive = isPos;
		refLine [0] = lnStart;
		refLine [1] = lnEnd;
	}

	/**
	 * Construct a new limit cycle from a point and an increment
	 * @param st the starting point
	 * @param evalInc the increment used to find it. This will have the same sign as the limit cycle
	 * @param lnStart the start of the refLine
	 * @param lnEnd the end of the refLine
	 */
	public LimCycleStart (Point2D st, double evalInc, Point2D lnStart, Point2D lnEnd)
	{
		  this(st, evalInc > 0, lnStart, lnEnd);
	}
}
