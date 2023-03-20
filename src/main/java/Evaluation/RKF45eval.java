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


    static final double[] _A = {0., 2. / 9., 1. / 3., 3. / 4., 1., 5. / 6.};
    static final double[][] _B = {{}, {2. / 9.}, {1. / 12., 1. / 4.},
            {69. / 128., -243. / 128., 135. / 64.}, {-17. / 12., 27. / 4., -27. / 5., 16. / 15.},
            {65. / 432., -5. / 16., 13. / 16., 4. / 27., 5. / 144.}};

    static final double[] _C = {1. / 9., 0, 9. / 20., 16. / 45., 1. / 12.};
    static final double[] _CH = {47. / 450., 0, 12. / 25., 32. / 225., 1. / 30., 6. / 25.};
    static final double[] _CT = {1. / 150., 0, -3. / 100., 16. / 75., 1. / 20., -6. / 25.};

    private static double A(int i) {
        return _A[i - 1];
    }

    private static double B(int i, int j) {
        return _B[i - 1][j - 1];
    }

    private static double C(int i) {
        return _C[i - 1];
    }

    private static double CH(int i) {
        return _CH[i - 1];
    }

    private static double CT(int i) {
        return _CT[i - 1];
    }

    private Point2D nextHelper() {
        double h = dynInc;
        double x1, x2, x3, x4, x5, x6, y1, y2, y3, y4, y5, y6;
        try {
            x1 = h * dx.eval(x, y, a, b, t + A(1) * h);
            y1 = h * dy.eval(x, y, a, b, t + A(1) * h);

            x2 = h * dx.eval(x + B(2, 1) * x1, y + B(2, 1) * y1, a, b, t + A(2) * h);
            y2 = h * dy.eval(x + B(2, 1) * x1, y + B(2, 1) * y1, a, b, t + A(2) * h);

            x3 = h * dx.eval(x + B(3, 1) * x1 + B(3, 2) * x2, y + B(3, 1) * y1 + B(3, 2) * y2, a, b,
                    t + A(3) * h);
            y3 = h * dy.eval(x + B(3, 1) * x1 + B(3, 2) * x2, y + B(3, 1) * y1 + B(3, 2) * y2, a, b,
                    t + A(3) * h);

            x4 = h * dx.eval(x + B(4, 1) * x1 + B(4, 2) * x2 + B(4, 3) * x3,
                    y + B(4, 1) * y1 + B(4, 2) * y2 + B(4, 3) * y3, a, b, t + A(4) * h);
            y4 = h * dy.eval(x + B(4, 1) * x1 + B(4, 2) * x2 + B(4, 3) * x3,
                    y + B(4, 1) * y1 + B(4, 2) * y2 + B(4, 3) * y3, a, b, t + A(4) * h);

            x5 = h * dx.eval(x + B(5, 1) * x1 + B(5, 2) * x2 + B(5, 3) * x3 + B(5, 4) * x4,
                    y + B(5, 1) * y1 + B(5, 2) * y2 + B(5, 3) * y3 + B(5, 4) * y4, a, b,
                    t + A(5) * h);
            y5 = h * dy.eval(x + B(5, 1) * x1 + B(5, 2) * x2 + B(5, 3) * x3 + B(5, 4) * x4,
                    y + B(5, 1) * y1 + B(5, 2) * y2 + B(5, 3) * y3 + B(5, 4) * y4, a, b,
                    t + A(5) * h);

            x6 = h * dx.eval(
                    x + B(6, 1) * x1 + B(6, 2) * x2 + B(6, 3) * x3 + B(6, 4) * x4 + B(6, 5) * x5,
                    y + B(6, 1) * y1 + B(6, 2) * y2 + B(6, 3) * y3 + B(6, 4) * y4 + B(6, 5) * y5, a,
                    b, t + A(6) * h);
            y6 = h * dy.eval(
                    x + B(6, 1) * x1 + B(6, 2) * x2 + B(6, 3) * x3 + B(6, 4) * x4 + B(6, 5) * x5,
                    y + B(6, 1) * y1 + B(6, 2) * y2 + B(6, 3) * y3 + B(6, 4) * y4 + B(6, 5) * y5, a,
                    b, t + A(6) * h);
        } catch (EvaluationException e) {
            return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
        } catch (NullPointerException n) {
            return Point2D.ZERO;
        }

        double xnext =
                x + CH(1) * x1 + CH(2) * x2 + CH(3) * x3 + CH(4) * x4 + CH(5) * x5 + CH(6) * x6;
        double ynext =
                y + CH(1) * y1 + CH(2) * y2 + CH(3) * y3 + CH(4) * y4 + CH(5) * y5 + CH(6) * y6;

        double TE = new Point2D(
                CT(1) * x1 + CT(2) * x2 + CT(3) * x3 + CT(4) * x4 + CT(5) * x5 + CT(6) * x6,
                CT(1) * y1 + CT(2) * y2 + CT(3) * y3 + CT(4) * y4 + CT(5) * y5 + CT(6) * y6)
                        .magnitude();
        dynInc = 0.9 * h * Math.pow(inc / TE, .2);
        // System.out.println("inc is now: " + dynInc);
        if (TE > inc)
            return nextHelper();
        x = xnext;
        y = ynext;
        t += h;
        return new Point2D(xnext, ynext);

    }

    private Point2D nextHelp(boolean first) {
        double x1, x2, x3, x4, x5, x6, y1, y2, y3, y4, y5, y6, xFive, yFive, xSix, ySix, s;
        Point2D p5, p6;
        try {
            x1 = dynInc * dx.eval(x, y, a, b, t);
            y1 = dynInc * dy.eval(x, y, a, b, t);
            x2 = dynInc * dx.eval(x + .25 * x1, y + .25 * y1, a, b, t + .25 * dynInc);
            y2 = dynInc * dy.eval(x + .25 * x1, y + .25 * y1, a, b, t + .25 * dynInc);
            x3 = dynInc * dx.eval(x + .09375 * x1 + .28125 * x2, y + .09375 * y1 + .28125 * y2, a,
                    b, t + .375 * dynInc);
            y3 = dynInc * dy.eval(x + .09375 * x1 + .28125 * x2, y + .09375 * y1 + .28125 * y2, a,
                    b, t + .375 * dynInc);
            x4 = dynInc * dx.eval(
                    x + (1932. / 2197.) * x1 - (7200. / 2197.) * x2 + (7296. / 2197.) * x3,
                    y + (1932. / 2197.) * y1 - (7200. / 2197.) * y2 + (7296. / 2197.) * y3, a, b,
                    t + (12. / 13.) * dynInc);
            y4 = dynInc * dy.eval(
                    x + (1932. / 2197.) * x1 - (7200. / 2197.) * x2 + (7296. / 2197.) * x3,
                    y + (1932. / 2197.) * y1 - (7200. / 2197.) * y2 + (7296. / 2197.) * y3, a, b,
                    t + (12. / 13.) * dynInc);
            x5 = dynInc * dx.eval(
                    x + (439. / 216.) * x1 - 8 * x2 + (3680. / 513) * x3 - (845. / 4104.) * x4,
                    y + (439. / 216.) * y1 - 8 * y2 + (3680. / 513) * y3 - (845. / 4104.) * y4, a,
                    b, t + dynInc);
            y5 = dynInc * dy.eval(
                    x + (439. / 216.) * x1 - 8 * x2 + (3680. / 513) * x3 - (845. / 4104.) * x4,
                    y + (439. / 216.) * y1 - 8 * y2 + (3680. / 513) * y3 - (845. / 4104.) * y4, a,
                    b, t + dynInc);
            x6 = dynInc * dx.eval(
                    x - (8. / 27.) * x1 + 2 * x2 - (3544. / 2565.) * x3 + (1859. / 4104.) * x4
                            - (11. / 40.) * x5,
                    y - (8. / 27.) * y1 + 2 * y2 - (3544. / 2565.) * y3 + (1859. / 4104.) * y4
                            - (11. / 40.) * y5,
                    a, b, t + .5 * dynInc);
            y6 = dynInc * dy.eval(
                    x - (8. / 27.) * x1 + 2 * x2 - (3544. / 2565.) * x3 + (1859. / 4104.) * x4
                            - (11. / 40.) * x5,
                    y - (8. / 27.) * y1 + 2 * y2 - (3544. / 2565.) * y3 + (1859. / 4104.) * y4
                            - (11. / 40.) * y5,
                    a, b, t + .5 * dynInc);
            xFive = x + (25. / 216.) * x1 + (1048. / 2565.) * x3 + (2197. / 4101.) * x4 - .2 * x5;
            yFive = y + (25. / 216.) * y1 + (1048. / 2565.) * y3 + (2197. / 4101.) * y4 - .2 * y5;
            xSix = x + (16. / 135.) * x1 + (6656. / 12825.) * x3 + (28561. / 56430.) * x4 - .18 * x5
                    + (2. / 55.) * x6;
            ySix = y + (16. / 135.) * y1 + (6656. / 12825.) * y3 + (28561. / 56430.) * y4 - .18 * y5
                    + (2. / 55.) * y6;
            p5 = new Point2D(xFive, yFive);
            p6 = new Point2D(xSix, ySix);
            s = Math.pow((inc * dynInc) / (2 * p5.subtract(p6).magnitude()), .25);
            if (Math.abs(s - 1) < .05 || !first) {
                x = xFive;
                y = yFive;
                t += dynInc;
                return p5;
            } else {
                dynInc *= s;
                return this.nextHelp(false);
            }
        } catch (EvaluationException e) {
            return new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
        } catch (NullPointerException n) {
            return Point2D.ZERO;
        }

    }

    @Override
    public Point2D next() {
        Point2D tmp = nextHelper();
        // System.out.println(tmp);
        return tmp;
    }


    @Override
    public void initialise(double x, double y, double t, double a, double b, double inc) {
        super.initialise(x, y, t, a, b, inc);
        this.dynInc = inc;
        this.inc = Math.abs(inc) / 10000;
        // System.out.println("inc: " + inc);
    }
}
