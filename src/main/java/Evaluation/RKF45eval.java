package Evaluation;

import AST.Derivative;
import Exceptions.EvaluationException;
import javafx.geometry.Point2D;

public class RKF45eval extends Evaluator {
    private double dynInc;
    protected RKF45eval(Derivative dx, Derivative dy) {
        super(dx, dy);
    }

    @Override
    public Point2D evaluate(double x, double y, double a, double b, double t, double inc) {
        throw new UnsupportedOperationException();
    }
    private Point2D nextHelp(boolean first)
    {
        double x1, x2, x3, x4, x5, x6, y1, y2, y3, y4, y5, y6, xFive, yFive, xSix, ySix, s;
        Point2D p5, p6;
        try
        {
            x1 = dynInc * dx.eval(x, y, a, b, t);
            y1 = dynInc * dy.eval(x, y, a, b, t);
            x2 = dynInc * dx.eval(x + .25 * x1, y + .25 * y1, a, b, t + .25 * dynInc);
            y2 = dynInc * dy.eval(x + .25 * x1, y + .25 * y1, a, b, t + .25 * dynInc);
            x3 = dynInc * dx.eval(x + .09375 * x1 + .28125 * x2, y + .09375 * y1 + .28125 * y2, a, b, t + .375 * dynInc);
            y3 = dynInc * dy.eval(x + .09375 * x1 + .28125 * x2, y + .09375 * y1 + .28125 * y2, a, b, t + .375 * dynInc);
            x4 = dynInc * dx.eval(x + (1932./2197.) * x1 - (7200./2197.) * x2 + (7296./2197.) * x3, y + (1932./2197.) * y1 - (7200./2197.) * y2 + (7296./2197.) * y3, a, b, t + (12./13.) * dynInc);
            y4 = dynInc * dy.eval(x + (1932./2197.) * x1 - (7200./2197.) * x2 + (7296./2197.) * x3, y + (1932./2197.) * y1 - (7200./2197.) * y2 + (7296./2197.) * y3, a, b, t + (12./13.) * dynInc);
            x5 = dynInc * dx.eval(x + (439./216.) * x1 - 8 * x2 + (3680./513) * x3 - (845./4104.) * x4, y + (439./216.) * y1 - 8 * y2 + (3680./513) * y3 - (845./4104.) * y4, a, b, t + dynInc);
            y5 = dynInc * dy.eval(x + (439./216.) * x1 - 8 * x2 + (3680./513) * x3 - (845./4104.) * x4, y + (439./216.) * y1 - 8 * y2 + (3680./513) * y3 - (845./4104.) * y4, a, b, t + dynInc);
            x6 = dynInc * dx.eval(x - (8./27.) * x1 + 2 * x2 - (3544./2565.) * x3 + (1859./4104.) * x4 - (11./40.) * x5, y - (8./27.) * y1 + 2 * y2 - (3544./2565.) * y3 + (1859./4104.) * y4 - (11./40.) * y5, a, b, t + .5 * dynInc);
            y6 = dynInc * dy.eval(x - (8./27.) * x1 + 2 * x2 - (3544./2565.) * x3 + (1859./4104.) * x4 - (11./40.) * x5, y - (8./27.) * y1 + 2 * y2 - (3544./2565.) * y3 + (1859./4104.) * y4 - (11./40.) * y5, a, b, t + .5 * dynInc);
            xFive = x + (25./216.) * x1 + (1048./2565.) * x3 + (2197./4101.) * x4 - .2 * x5;
            yFive = y + (25./216.) * y1 + (1048./2565.) * y3 + (2197./4101.) * y4 - .2 * y5;
            xSix = x + (16./135.) * x1 + (6656./12825.) * x3 + (28561./56430.) * x4 - .18 * x5 + (2./55.) * x6;
            ySix = y + (16./135.) * y1 + (6656./12825.) * y3 + (28561./56430.) * y4 - .18 * y5 + (2./55.) * y6;
            p5 = new Point2D(xFive, yFive);
            p6 = new Point2D(xSix, ySix);
            s = Math.pow((inc * dynInc) / (2 * p5.subtract(p6).magnitude()), .25);
            if(Math.abs(s - 1) < .05 || !first)
                return p5;
            else
            {
                dynInc *= s;
                return this.nextHelp(false);
            }
        } catch (EvaluationException e)
        {
            return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
        } catch (NullPointerException n)
        {
            return Point2D.ZERO;
        }

    }
    @Override
    public Point2D next()
    {
        Point2D tmp = nextHelp(true);
        System.out.println(tmp);
        return nextHelp(true);
    }


    @Override
    public void initialise(double x, double y, double t, double a, double b, double inc) {
        super.initialise(x, y, t, a, b, inc);
        this.dynInc = inc;
    }
}
