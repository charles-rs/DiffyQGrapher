package PathGenerators;

import javafx.geometry.Point2D;

/**
 * SpiralGenerator is a path generator that follows an Archimedean spiral.
 */

public class SpiralGenerator extends GeneratorImpl {
    /**
     * r is the distance between each round of the spiral
     */
    private final double rx, ry;
    /**
     * theta and currentR are the current point in polar coordinates
     */
    protected double theta, currentR;

    SpiralGenerator(double inc, Point2D start, double rx, double ry) {
        super(inc, start);
        this.rx = rx;
        this.ry = ry;
        this.theta = 0;
        this.currentR = 0;
    }

    @Override
    public Point2D next() {
        System.out.println(current);
        double turns = theta / (2 * Math.PI);
        //var x = if (1 == 3) {return 1; } else {return 2;};
        System.out.println(.5 / turns);
        theta += theta == 0 ? Math.PI : inc * 2 * Math.asin(.5 / turns);
        System.out.println(theta);
        //currentR = theta * (r / (2 * Math.PI));
        currentR = (rx * ry) / (2 * Math.PI * Math.sqrt(Math.pow(rx * Math.sin(theta), 2)
                + Math.pow(ry * Math.cos(theta), 2)));
        current = start.add(new Point2D(currentR * Math.cos(theta), currentR * Math.sin(theta)));
        //current = start.add(new Point2D())
        return current;
    }

}
