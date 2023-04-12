package PathGenerators;

import javafx.geometry.Point2D;


/**
 * Factory class for path generators
 */
public class GeneratorFactory {
    /**
     * gets a new spiral generator for analysing pixels
     *
     * @param pxX   the size of one pixel in the x direction
     * @param pxY   pixel size in the y direction
     * @param start the start point
     * @return the spiral path generator starting at start and covering pixels of size px
     */
    public static Generator getSpiralGenerator(double pxX, double pxY, Point2D start) {
        if (start == null) start = Point2D.ZERO;
        return new SpiralGenerator(1, start, pxX, pxY);
    }


    /**
     * Gets a circle loop generator that advances by pixels with a center and a radius
     *
     * @param px     the size of a pixel
     * @param center the center of the loop
     * @param radius the radius
     * @return the new circle generator
     */
    public static LoopGenerator getCircleLoopGenerator(double px, Point2D center, double radius) {
        return new CircleLoopGenerator(px / 2, center, radius);
    }

    /**
     * Gets a circle loop generator where the radius is an integer number of pixels
     *
     * @param px     the size of one pixel
     * @param center the center of the circle
     * @param pxRad  the number of pixels for the radius
     * @return the new circle generator
     */
    public static LoopGenerator getCircleLoopGenerator(double px, Point2D center, int pxRad) {
        return getCircleLoopGenerator(px, center, px * pxRad);
    }

    /**
     * Gets a new loop generator of the provided type
     *
     * @param l      the type of the loop generator
     * @param px     the size of a pixel
     * @param center the center of the loop
     * @param radius the radius
     * @return the new loop generator
     */
    public static LoopGenerator getLoopGenerator(LoopType l, double px, Point2D center, double radius) {
        switch (l) {
            case CIRCLE:
                return new CircleLoopGenerator(px / 2, center, radius);
            case ELLIPSE:
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Gets a loop generator where the radius is an integer number of pixels
     *
     * @param l      the type of the loop generator
     * @param px     the size of one pixel
     * @param center the center of the circle
     * @param pxRad  the number of pixels for the radius
     * @return the new loop generator
     */
    public static LoopGenerator getLoopGenerator(LoopType l, double px, Point2D center, int pxRad) {
        return getLoopGenerator(l, px, center, px * pxRad);
    }

    /**
     * Gets a finite path generator with the provided information
     *
     * @param ty      the type of finite path generator
     * @param center  the center point
     * @param maxDist the max distance from the start for a spiral generator (thrown out for other types
     * @return the new finite path generator
     */
    public static FinitePathGenerator getFinitePathGenerator(
            FinitePathType ty, double pxX, double pxY, Point2D center,
            double maxDist, Point2D old) {
        switch (ty) {
            case ARC:
                Point2D diff = center.subtract(old);
                double th = Math.atan2(diff.getY(), diff.getX());
                System.out.println("calculated theta: " + th);
                System.out.println("diff: " + diff);
                return getArcGenerator(pxX, center, 4 * pxX, 4 * pxY, th - Math.PI / 2, th + Math.PI / 2, ArcDirection.ANTICLOCKWISE);
            case SPIRAL:
                return getFiniteSpiralGenerator(pxX, pxY, center, maxDist);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Creates a new finite Archimedean spiral generator
     *
     * @param pxX      size of a pixel in the x direction
     * @param pxY      size of a pixel in the y direction
     * @param center   the center point
     * @param maxTurns the number of rounds to give up after
     * @return the new spiral generator
     */
    public static FinitePathGenerator getFiniteSpiralGenerator(double pxX, double pxY, Point2D center, double maxTurns) {
        return new FiniteSpiralGenerator(1, center, pxX, pxY, maxTurns);
    }

    /**
     * Constructs a new arc generator with the provided params
     *
     * @param center     the center point
     * @param thetaStart the starting angle
     * @param thetaEnd   the ending angle
     * @return the new arc generator
     */


    public static FinitePathGenerator getArcGenerator(
            double thetaInc, Point2D center, double radX, double radY, double thetaStart, double thetaEnd, ArcDirection dir) {
        return new ArcGenerator(thetaInc, center, radX, radY, thetaStart, thetaEnd, dir);
    }

    public static MidpointPathGenerator getMidpointArcGenerator(
            double px, Point2D center, double thetaLeft, double thetaRight) {
        return new MidpointArcGenerator(center, px, thetaLeft, thetaRight, px);
    }

    public static MidpointPathGenerator getMidpointArcGenerator(
            double px, Point2D center, Point2D old) {
        Point2D diff = center.subtract(old);
        double th = Math.atan2(diff.getY(), diff.getX());
        System.out.println("calculated theta: " + th);
        System.out.println("diff: " + diff);
        return getMidpointArcGenerator(px, center, th + Math.PI / 2, th - Math.PI / 2);
    }


}
