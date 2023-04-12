package PathGenerators;

import javafx.geometry.Point2D;

public class EllipseGenerator extends LoopGeneratorImpl {
    double theta;
    final double rX, rY;

    public EllipseGenerator(double inc, Point2D center, double rX, double rY) {
        super(inc, center, rX);
        this.rX = rX;
        this.rY = rY;
        theta = 0;
        synchTheta();
    }

    private void synchTheta() {
        if (theta > Math.PI * 2) {
            ++rounds;
            theta -= Math.PI * 2;
        }
        System.out.println("theta: " + theta);
        current = center.add(new Point2D(rX * Math.cos(theta), rY * Math.sin(theta)));
    }

    @Override
    public Point2D next() {
        theta += inc;
        synchTheta();
        return current;
    }

    @Override
    public Point2D advanceOneQuarter() {
        theta += Math.PI / 2;
        synchTheta();
        return current;
    }
}
