package FXObjects;

import Exceptions.RootNotFound;
import PathGenerators.ArcDirection;
import PathGenerators.EllipseGenerator;
import PathGenerators.FinitePathType;
import PathGenerators.GeneratorFactory;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.awt.*;

public abstract class GlobalBifurcationFinder<Signature extends GlobalBifurcationFinder.SigIntf> implements Cloneable {

    public static abstract class SigIntf {
        public abstract boolean bifurcatesFrom(SigIntf other);

        public abstract boolean bifurcatesTo(SigIntf other);

        public boolean bifurcates(SigIntf other) {
            return bifurcatesFrom(other) || bifurcatesTo(other);
        }

        public abstract boolean isZero();

        public boolean malformed() {
            return false;
        }
    }


    protected abstract Color getColor();

    protected OutputPlane o;

    protected abstract String getName();

    @Override
    public abstract GlobalBifurcationFinder<Signature> clone();

    public enum Orientation {LEFT, RIGHT}

    abstract Signature classify(Point2D p) throws RootNotFound;

    RenderedCurve render = new RenderedCurve();

    void run() {

        Point2D st;
        System.out.println("starting " + getName());
        try {
            //st = semiStableFinitePath(lnSt, lnNd, a, b, FinitePathType.SPIRAL, null);
            st = globalBifFinitePath(o.a, o.b, FinitePathType.SPIRAL, null);
            render.start = st;
            render.color = getColor();
        } catch (RootNotFound r) {
            render = null;
            return;
        }
        System.out.println("yay");
        System.out.println(st);
        Point2D[] sides;

        try {
            //sides = semiStableLoop(lnSt, lnNd, st, LoopType.CIRCLE);
            sides = globalBifLoop(st);
        } catch (RootNotFound r) {
            System.out.println("oops, circle didn't work");
            render = null;
            return;
        }

        GlobalBifurcationDriver.init(o, Thread.currentThread());

        var s1 = new GlobalBifurcationDriver(this, st, sides[0], Orientation.RIGHT, render.right);
        var s2 = new GlobalBifurcationDriver(this.clone(), st, sides[1], Orientation.LEFT, render.left);
        s1.start();
        s2.start();
        try {
            s1.join();
        } catch (InterruptedException i) {
            s1.interrupt();
            s2.interrupt();
        }
        try {
            s2.join();
        } catch (InterruptedException i) {
            s1.interrupt();
            s2.interrupt();
        }
        o.in.render();

    }


    Point2D globalBifFinitePath(double a, double b,
                                FinitePathType tp, Point2D prev) throws RootNotFound {

        var p = new Point2D(a, b);
        var cycleCounts = classify(p);
        System.out.println("starting with " + cycleCounts);
        double px = ((o.in.xMax.get() - o.in.xMin.get() + o.in.yMax.get() - o.in.yMin.get()) / 2)
                / ((o.in.canv.getWidth() + o.in.canv.getHeight()) / 2D);
        double pxX = o.in.getPxX();
        double pxY = o.in.getPxY();
        if (tp.equals(FinitePathType.SPIRAL)) {
            pxX *= 1;
            pxY *= 1;
        }
        var s = GeneratorFactory.getFinitePathGenerator(tp, pxX, pxY, p, 50, prev);
        var pOld = p;
        p = s.next();
        while (!Thread.interrupted() && !s.done()) {
            var count = classify(p);
            System.out.println("starting with " + cycleCounts);
            System.out.println("now with " + count);
            if (count.bifurcates(cycleCounts))
                break;
            cycleCounts = count;
            p = s.next();
        }
        if (s.done())
            throw new RootNotFound();
        return p.midpoint(pOld);
    }

    double pxRad = 2;
    double thetaInc = .08 / pxRad;

    Point2D[] globalBifLoop(Point2D center) throws RootNotFound {
        var gen = new EllipseGenerator(thetaInc, center, pxRad * o.in.getPxX(), pxRad * o.in.getPxY());
        Signature count1;
        System.out.println("center: " + center);
        System.out.println("start: " + gen.getCurrent());
        var count2 = classify(gen.getCurrent());
        var temp = new Point2D[2];
        int bifCount = 0;
        Point2D next = gen.getCurrent();
        Point2D prev;
        while (!gen.completed() && !Thread.interrupted()) {
            System.out.println("prev: " + next);
            prev = next;
            next = gen.next();
            System.out.println("next: " + next);
            System.out.println("center: " + center);
            count1 = count2;
            count2 = classify(next);
            System.out.println("count: " + count2);
            if (count1.bifurcatesFrom(count2)) {
                temp[0] = gen.getCurrent().midpoint(prev);
                gen.advanceOneQuarter();
                ++bifCount;
            } else if (count1.bifurcatesTo(count2)) {
                temp[1] = gen.getCurrent().midpoint(prev);
                gen.advanceOneQuarter();
                ++bifCount;
            }
            if (bifCount == 2) break;
        }
        if (bifCount < 2)
            throw new RootNotFound();
        System.out.println("CENTER: " + center);
        System.out.println("FIRST: " + temp[0]);
        System.out.println("SECOND: " + temp[1]);
        return temp;
    }

    /**
     * @param prev    the previous point on the bifurcation
     * @param prevOld the anteprevious point on the bifurcation
     * @param orient  current orientation. LEFT means there are 2 cycles on the left
     */
    public Point2D globalBifPred(Point2D prev, Point2D prevOld, Orientation orient) throws RootNotFound {
        var diff = prev.subtract(prevOld);
        var predict = prev.add(diff);
        var count = classify(predict);

        var theta = Math.atan2(diff.getY(), diff.getX());
        if (theta < 0)
            theta += 2 * Math.PI;
        System.out.println("prev:    " + prev);
        System.out.println("prevOld: " + prevOld);
        System.out.println("angle: " + theta);

        var sign = switch (orient) {
            case LEFT -> count.isZero() ? 1 : -1;
            case RIGHT -> count.isZero() ? -1 : 1;
        };


        var gen = GeneratorFactory.getArcGenerator(thetaInc, prev, o.in.getPxX() * pxRad, o.in.getPxY() * pxRad,
                theta, theta + sign * Math.PI / 1.1,
                sign == 1 ? ArcDirection.ANTICLOCKWISE : ArcDirection.CLOCKWISE
        );
        while (!gen.done()) {
            var old = gen.getCurrent();
            var new_count = classify(gen.next());
            final boolean debugOneCycle = false;
            if (debugOneCycle && new_count.malformed()) {
                System.out.println("BAD: " + gen.getCurrent());

                o.updateA(gen.getCurrent().getX());
                o.in.updateA(gen.getCurrent().getX());
                o.updateB(gen.getCurrent().getY());
                o.in.updateB(gen.getCurrent().getY());
                Platform.runLater(o::render);
                Platform.runLater(o.in::render);
                markPoint(gen.getCurrent());
            }
            //o.in.drawLine(old, gen.getCurrent(), o.in.awtHopfBifColor);
            System.out.println("count: " + count);
            System.out.println("newcount: " + new_count);
            if (new_count.bifurcates(count))
                return gen.getCurrent().midpoint(old);
            count = new_count;
        }
        throw new RootNotFound();
    }

    void markPoint(Point2D pt) throws RootNotFound {
    }

}
