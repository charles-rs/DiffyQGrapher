package PathGenerators;

import javafx.geometry.Point2D;

public class SegmentGenerator extends FinitePathGeneratorImpl {
    private final Point2D end;
    private final int totalSteps;

    public SegmentGenerator(Point2D start, Point2D end, double incX, double incY) {
        super(0, start);
        this.end = end;
        //System.out.println("generating path from " + start + " to " + end);
        double len = start.distance(end);

        double theta = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
//        System.out.println("theta: " + theta);
//        System.out.println("cos: " + Math.cos(theta));
//        System.out.println("sin: " + Math.sin(theta));
        this.inc = Math.min(Math.abs(incX / Math.cos(theta)), Math.abs(incY / Math.sin(theta)));
        this.totalSteps = (int) Math.ceil(len / inc);
//        System.out.println("len: " + len);
//        System.out.println("inc: " + inc);
//        System.out.println("steps: " + totalSteps);
    }

    @Override
    public boolean done() {
        return steps >= totalSteps;
    }

    @Override
    public Point2D next() {
        if (!done()) {
            ++steps;
            double fracAlong = ((double) steps) / ((double) totalSteps);
            current = start.multiply(1 - fracAlong).add(end.multiply(fracAlong));
        }
        return current;
    }
}
