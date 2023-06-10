package FXObjects;

import Exceptions.RootNotFound;
import PathGenerators.ArcDirection;
import PathGenerators.EllipseGenerator;
import PathGenerators.GeneratorFactory;
import PathGenerators.SegmentGenerator;
import Utils.MyClonable;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static FXObjects.GlobalBifurcationFinder.BifurcationType.*;

public abstract class GlobalBifurcationFinder<Signature extends GlobalBifurcationFinder.SigIntf, Context extends MyClonable> implements Cloneable {

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

    Context globalContext;


    protected abstract Color getColor();

    protected OutputPlane o;

    protected abstract String getName();

    @Override
    public abstract GlobalBifurcationFinder<Signature, Context> clone();

    public enum Orientation {LEFT, RIGHT}


    Signature classify(Point2D p) throws RootNotFound {
        return classify(p, globalContext);
    }

    abstract Signature classify(Point2D p, Context c) throws RootNotFound;

    RenderedCurve render = new RenderedCurve();

    void run() {

        Point2D st = globalBifSinglePoint(o.a, o.b);
        System.out.println("starting " + getName());
        if (st == null) {
            render = null;
            return;
        }
        render.start = st;
        render.color = getColor();

        System.out.println("yay");
        System.out.println(st);
        var sides = globalBifLoop(st);
        if (sides.getKey() == null || sides.getValue() == null) {
            System.out.println("oops, circle didn't work");
            render = null;
            return;
        }

        GlobalBifurcationDriver.init(o, Thread.currentThread());

        var s1 = new GlobalBifurcationDriver(this, st, sides.getKey(), Orientation.RIGHT, render.right);
        var s2 = new GlobalBifurcationDriver(this.clone(), st, sides.getValue(), Orientation.LEFT, render.left);
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


    Point2D globalBifSinglePoint(double a, double b) {
        Object lock = new Object();
        AtomicReference<Point2D> dest = new AtomicReference<>();
        AtomicBoolean found = new AtomicBoolean(false);

        double pxRad = 40;
        double xRad = pxRad * o.in.getPxX();
        double yRad = pxRad * o.in.getPxY();

        Thread[] threads = new Thread[8];
        var start = new Point2D(a, b);
        for (int i = 0; i < 8; ++i) {
            var finalI = i;
            threads[i] = new Thread(() ->
            {
                double θ = ((double) finalI) * (Math.PI / 4);
                var end = new Point2D(a + xRad * Math.cos(θ), b + yRad * Math.sin(θ));
                var gen = new SegmentGenerator(start, end,
                        o.in.getPxX() / 4, o.in.getPxY() / 4);
                var pOld = start;
                Context context = (Context) globalContext.clone(); // evil reflection hack

                var oldCounts = nullClassify(pOld, context);

                while (!gen.done()) {
                    var p = gen.next();
                    var newCounts = nullClassify(p, context);
                    switch (bif(oldCounts, newCounts)) {
                        case TO, FROM -> {
                            synchronized (lock) {
                                if (!found.get()) {
                                    found.set(true);
                                    dest.set(pOld.midpoint(p));
                                }
                            }
                        }
                    }
                    if (found.get())
                        return;
                    oldCounts = newCounts;
                    pOld = p;
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < 8; ++i) {
            try {
                if (threads[i] != null)
                    threads[i].join();
            } catch (InterruptedException ignored) {
            }
        }
        return dest.get();
    }


    double pxRad = 2;
    double thetaInc = .08 / pxRad;

    Signature nullClassify(Point2D pt, Context context) {
        try {
            return classify(pt, context);
        } catch (RootNotFound r) {
            return null;
        }
    }

    Signature nullClassify(Point2D pt) {
        return nullClassify(pt, globalContext);
    }

    enum BifurcationType {
        FROM, TO, NEITHER;
    }

    BifurcationType bif(Signature oldCount, Signature newCount) {
        if (oldCount != null && newCount != null) {
            if (oldCount.bifurcatesTo(newCount)) {
                return TO;
            } else if (oldCount.bifurcatesFrom(newCount)) {
                return FROM;
            } else {
                return NEITHER;
            }
        } else {
            return NEITHER;
        }
    }

    Pair<Point2D, Point2D> globalBifLoop(Point2D center) {
        var gen = new EllipseGenerator(thetaInc, center, pxRad * o.in.getPxX(), pxRad * o.in.getPxY());
        Signature oldCount = null, newCount = null;
        oldCount = nullClassify(gen.getCurrent());
        Point2D oldPoint = gen.getCurrent();
        Point2D newPoint;
        Point2D bif1 = null, bif2 = null;
        while (!gen.completed() && !Thread.interrupted()) {
            newPoint = gen.next();
            newCount = nullClassify(newPoint);
            var bif = bif(oldCount, newCount);
            boolean bifurcated = false;
            switch (bif) {
                case FROM -> {
                    bif1 = newPoint.midpoint(oldPoint);
                    bifurcated = true;
                }
                case TO -> {
                    bif2 = newPoint.midpoint(oldPoint);
                    bifurcated = true;
                }
            }
            if (bifurcated) {
                gen.advanceOneQuarter();
                oldPoint = gen.getCurrent();
                oldCount = nullClassify(oldPoint);
            } else {
                oldPoint = newPoint;
                oldCount = newCount;
            }
        }
        return new Pair<>(bif1, bif2);
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
