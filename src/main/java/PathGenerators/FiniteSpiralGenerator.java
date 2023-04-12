package PathGenerators;

import javafx.geometry.Point2D;

public class FiniteSpiralGenerator extends SpiralGenerator implements FinitePathGenerator {
    final double maxTurns;

    protected FiniteSpiralGenerator(double inc, Point2D start, double radX, double radY, double maxTurns) {
        super(inc, start, radX, radY);
        this.maxTurns = maxTurns;
    }

    @Override
    public boolean done() {
        return theta / (2 * Math.PI) >= maxTurns;
    }

    @Override
    public Point2D next() {
        ++steps;
        if (!done())
            return super.next();
        else return current;
    }

}
