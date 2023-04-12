package PathGenerators;

import javafx.geometry.Point2D;

public class ArcGenerator extends FinitePathGeneratorImpl {
    final double thetaEnd, radX, radY;
    double theta;
    final double thetaInc;
    Point2D center;

    ArcDirection dir;

    /**
     * Constructor for an arc generator
     *
     * @param center     the center point
     * @param thetaStart the starting theta
     * @param thetaEnd   the ending theta
     * @param dir
     * @implNote always goes anti-clockwise
     */
    protected ArcGenerator(double thetaInc, Point2D center, double radX, double radY, double thetaStart, double thetaEnd, ArcDirection dir) {
        super(thetaInc, center.add(new Point2D(radX * Math.cos(thetaStart), radY * Math.sin(thetaStart))));
        this.center = center;
        this.theta = thetaStart;
        switch (dir) {
            case ANTICLOCKWISE -> {
                if (thetaEnd < thetaStart)
                    thetaEnd += 2 * Math.PI;
            }
            case CLOCKWISE -> {
                if (thetaEnd > thetaStart)
                    thetaEnd -= 2 * Math.PI;
            }
        }

        this.thetaEnd = thetaEnd;
        this.radX = radX;
        this.radY = radY;
        this.thetaInc = dir.equals(ArcDirection.CLOCKWISE) ? thetaInc * -1 : thetaInc;
        this.dir = dir;
    }

    @Override
    public boolean done() {
        return switch (dir) {
            case ANTICLOCKWISE -> theta >= thetaEnd;
            case CLOCKWISE -> theta <= thetaEnd;
        };
    }


    @Override
    public Point2D next() {
        if (!done()) {
            theta += thetaInc;
            current = center.add(new Point2D(radX * Math.cos(theta), radY * Math.sin(theta)));
        }
        return current;
    }
}
