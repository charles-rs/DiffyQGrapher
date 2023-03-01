package FXObjects;

import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
import Exceptions.NoMoreXException;
import javafx.geometry.Point2D;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashSet;

public class BasinFinder extends Thread {

    private static OutputPlane o;
    private static Point2D crit;
    private static AtomicBoolean doneLeft, doneRight;
    private static Double left, right;
    private static final Object leftLock = new Object(), rightLock = new Object();
    private static Side s;
    private static double inc;
    private static float colNum;
    private static java.awt.Color col;

    public BasinFinder() {
        setDaemon(true);
    }

    @Override
    public void run() {
        Evaluator e = EvaluatorFactory.getEvaluator(o.evalType, o.getDx(), o.getDy());
        try {
            boolean foundAny;
            double x;
            xAndSide XAS;
            double maxT = 100;
            while (true) {
                XAS = getNextX();
                x = XAS.x;
                foundAny = false;
                double incY = (o.yMax.get() - o.yMin.get()) / o.canv.getHeight();
                for (double y = o.yMin.get(); y < o.yMax.get(); y += incY) {
                    e.initialise(x, y, o.getT(), o.a, o.b, inc);
                    while (o.inBoundsSaddle(e.getCurrent()) && e.getT() < maxT + o.getT()
                            && e.getT() >= o.getT() - maxT) {
                        // System.out.println("inc: " + inc + "\nincY: " + incY);
                        if (e.next().distance(crit) < incY) {
                            foundAny = true;
                            synchronized (o.g) {
                                o.g.setColor(col);
                                o.g.fillRect(o.imgNormToScrX(x), o.imgNormToScrY(y), 1, 1);
                            }
                            break;
                        }
                        if (e.getCurrent().distance(e.next()) < 100 * Math.ulp(1.0))// Math.abs(incY
                                                                                    // /
                            //
                            // 10000))
                            break;
                    }
                }
                o.render();
                if (!foundAny) {
                    switch (XAS.s) {
                        case RIGHT:
                            doneRight.set(true);
                            break;
                        case LEFT:
                            doneLeft.set(true);
                            break;
                    }
                }
            }
        } catch (NoMoreXException ignored) {
        }

    }

    public static void reset() {
        colNum = 0;
    }

    public static void init(OutputPlane _o, Point2D _crit, boolean posDir) {
        doneLeft = new AtomicBoolean(false);
        doneRight = new AtomicBoolean(false);
        o = _o;
        crit = _crit;
        s = Side.LEFT;
        inc = (o.xMax.get() - o.xMin.get()) / o.canv.getWidth();
        if (!posDir)
            inc *= -1;
        right = crit.getX();
        left = crit.getX() - 1 * inc;
        col = new Color(Color.getHSBColor(colNum, 1, 1).getRGB() & ((~0) >>> 8) | (1 << 30), true);
        colNum += Math.PI;
    }

    private synchronized static xAndSide getNextX() throws NoMoreXException {
        if (doneLeft.get() && doneRight.get())
            throw new NoMoreXException();
        double temp = 0;
        Side sTemp = s;
        switch (s) {
            case LEFT:
                synchronized (leftLock) {
                    temp = left;
                    left -= inc;
                    if (left < o.xMin.get())
                        doneLeft.set(true);
                }
                if (!doneRight.get())
                    s = Side.RIGHT;
                break;
            case RIGHT:
                synchronized (rightLock) {
                    temp = right;
                    right += inc;
                    if (right > o.xMax.get())
                        doneRight.set(true);
                }
                if (!doneLeft.get())
                    s = Side.LEFT;
                break;
        }

        return new xAndSide(temp, sTemp);
    }

    private enum Side {
        LEFT, RIGHT;
    }

    private static class xAndSide {
        double x;
        Side s;

        xAndSide(double x, Side s) {
            this.x = x;
            this.s = s;
        }
    }
}
