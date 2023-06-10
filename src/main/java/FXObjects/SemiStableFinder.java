package FXObjects;

import Evaluation.CriticalPoint;
import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
import Exceptions.RootNotFound;
import PathGenerators.SegmentGenerator;
import Utils.MyClonable;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

class SemiStableFinder extends GlobalBifurcationFinder<SemiStableFinder.CycleCounts, SemiStableFinder.CycleContext> {

    @Override
    protected String getName() {
        return "semistable";
    }


    @Override
    protected java.awt.Color getColor() {
        return o.in.awtSemiStableColor;
    }


    double incX, incY;

    public SemiStableFinder(@NotNull OutputPlane o, @NotNull CriticalPoint inner, @NotNull Point2D outer) {
        this.o = o;
        this.globalContext = new CycleContext(inner, outer);
        incX = (o.xMax.get() - o.xMin.get()) / 512;
        incY = (o.yMax.get() - o.yMin.get()) / 512;
    }


    @Override
    public SemiStableFinder clone() {
        return new SemiStableFinder(o, globalContext.inner.clone(), globalContext.outer);
    }

    public class CycleContext implements MyClonable {

        public CycleContext(CriticalPoint inner, Point2D outer) {
            this.inner = inner;
            this.outer = outer;
        }

        CriticalPoint inner;
        Point2D outer;

        void updateInner(Point2D params) throws RootNotFound {
            var eval = EvaluatorFactory.getBestEvaluator(SemiStableFinder.this.o.getDx(), SemiStableFinder.this.o.getDy());
            var tmp = eval.findCritical(inner.point, params.getX(), params.getY(), 0);
            var diff = tmp.point.subtract(inner.point);
            inner = tmp;
            outer = outer.add(diff);
        }

        @Override
        public CycleContext clone() {
            return new CycleContext(inner.clone(), outer);
        }


    }

    public static class CycleCounts extends SigIntf {
        public final int stable, unstable;

        public CycleCounts(int s, int u) {
            stable = s;
            unstable = u;
        }

        /**
         * other has the cycles.
         */
        @Override
        public boolean bifurcatesFrom(SigIntf _other) {
            if (_other instanceof CycleCounts other)
                return this.stable == other.stable + 1 && this.unstable == other.unstable + 1;
            throw new ClassCastException();
        }

        @Override
        public boolean bifurcatesTo(SigIntf _other) {
            if (_other instanceof CycleCounts other)
                return this.stable == other.stable - 1 && this.unstable == other.unstable - 1;
            throw new ClassCastException();
        }

        public boolean isZero() {
            return (stable | unstable) == 0;
        }

        @Override
        public String toString() {
            return "(" + stable + ", " + unstable + ")";
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof CycleCounts oth) {
                return stable == oth.stable && unstable == oth.unstable;
            }
            return false;
        }
    }


/*
    Point2D semiStableMidpointPath(Point2D prev1, Point2D prev2) throws RootNotFound {
        double px = ((o.in.xMax.get() - o.in.xMin.get() + o.in.yMax.get() - o.in.yMin.get()) / 2)
                / ((o.in.canv.getWidth() + o.in.canv.getHeight()) / 2D);
        var gen = GeneratorFactory.getMidpointArcGenerator(px, prev1, prev2);
        CycleCounts left = null, right = null, center = null;
        int dir = 4;
        while ((!gen.done() || dir > 1) && !Thread.interrupted()) {
            switch (dir) {
                case 0:
                    left = center;
                    center = countCycles(gen.getCurrentPoint());
                    break;
                case 1:
                    right = center;
                    center = countCycles(gen.getCurrentPoint());
                    break;
                case 2:
                    right = countCycles(gen.getCurrent().right);
                    center = countCycles(gen.getCurrent().center);
                    break;
                case 3:
                    left = countCycles(gen.getCurrent().left);
                    center = countCycles(gen.getCurrent().center);
                    break;
                default:
                    left = countCycles(gen.getCurrent().left);
                    right = countCycles(gen.getCurrent().right);
                    center = countCycles(gen.getCurrent().center);
            }
            System.out.println("left: " + left + ", " + gen.getCurrent().left);
            System.out.println("right: " + right + ", " + gen.getCurrent().right);

            if (!left.bifurcatesFrom(right)) {
                System.out.println("no variation");
                throw new RootNotFound();
            }
            if (left.bifurcatesFrom(center)) {
                gen.getNext(Side.LEFT);
                dir = 1;
            } else if (right.bifurcatesFrom(center)) {
                gen.getNext(Side.RIGHT);
                dir = 0;
            } else {
                gen.refine();
                dir = 4;
            }
        }
        if (!gen.done()) {
            System.out.println("defaulting out");
            throw new RootNotFound();
        } else if (dir == 0 || dir == 1)
            return gen.getCurrentPoint();
        else throw new RootNotFound();
    }
*/

    private enum CycleDir {
        BACKWARD, FORWARD, UNKNOWN
    }

    private static class CycleDirWrap {
        public CycleDir val;

        public CycleDirWrap(CycleDir v) {
            val = v;
        }
    }


    CycleCounts classify(Point2D p, CycleContext context) throws RootNotFound {
        return countCycles(p, context, false);
    }

    @Override
    void markPoint(Point2D pt) throws RootNotFound {
        countCycles(pt, globalContext, true);
    }

    CycleCounts countCycles(Point2D p, CycleContext context, boolean mark) throws RootNotFound {
        context.updateInner(p);
        var stableCycles = new ArrayList<Point2D>();
        var unstableCycles = new ArrayList<Point2D>();
        var path = new SegmentGenerator(context.outer, context.inner.point, incX, incY);
        path.advance(1);
        final var dir = new CycleDirWrap(CycleDir.UNKNOWN);
        var eval = EvaluatorFactory.getBestEvaluator(o.getDx(), o.getDy());
        eval.initialise(path.getCurrent(), 0, p.getX(), p.getY(), o.inc);
        Consumer<Point2D> forwardToBack = (Point2D pt) -> {
            if (dir.val == CycleDir.FORWARD)
                stableCycles.add(pt);
            dir.val = CycleDir.BACKWARD;
        };
        Consumer<Point2D> backwardToFore = (Point2D pt) -> {
            if (dir.val == CycleDir.BACKWARD)
                unstableCycles.add(pt);
            dir.val = CycleDir.FORWARD;
        };
        int steps = 0;
        while (!path.done()) {
            ++steps;
            //System.out.println("state is now: " + dir);
            var prev = path.getCurrent();
            eval.resetT();
            eval.movePoint(path.getCurrent());
            eval.next();
            //System.out.println(prev);
            try {
                var next = eval.getNextIsectLn(context.inner.point, context.outer);
                //System.out.println(next);
                if (next.distance(context.outer) < prev.distance(context.outer)) {
                    if (eval.getInc() > 0D) {
                        forwardToBack.accept(prev);
                    } else {
                        backwardToFore.accept(prev);
                    }
                } else {
                    if (eval.getInc() > 0D) {
                        backwardToFore.accept(prev);
                    } else {
                        forwardToBack.accept(prev);
                    }
                    eval.negate();
                }
                do {
                    path.next();
//                    System.out.println("current: " + path.getCurrent());
//                    System.out.println("next: " + next);
//                    System.out.println("advancing");
                } while (!path.done() && path.getCurrent().distance(context.outer) < next.distance(context.outer));
            } catch (RootNotFound r) {
                //dir.val = CycleDir.UNKNOWN;
                eval.negate();
            }
        }
        System.out.println("steps: " + steps);
        stableCycles.removeIf((Point2D pt) -> pt.distance(context.inner.point) < 5 * path.getInc());
        unstableCycles.removeIf((Point2D pt) -> pt.distance(context.inner.point) < 5 * path.getInc());
        //System.out.println(new CycleCounts(stableCycles.size(), unstableCycles.size()));

        if (eval.getInc() < 0)
            eval.negate();
        checkPoint(stableCycles, path, eval, context);
        eval.negate();
        checkPoint(unstableCycles, path, eval, context);
        if (mark) {
            for (var pt : stableCycles) {
                var scrCoords = o.normToScr(pt);
                var circ = new Circle(scrCoords.getX(), scrCoords.getY(), 3);
                circ.setVisible(true);
                circ.setFill(new Color(0, 1, 0, 1));
                Platform.runLater(() -> o.getChildren().add(circ));
            }
            for (var pt : unstableCycles) {
                var scrCoords = o.normToScr(pt);
                var circ = new Circle(scrCoords.getX(), scrCoords.getY(), 3);
                circ.setVisible(true);
                circ.setFill(new Color(0, 0, 1, 1));
                Platform.runLater(() -> o.getChildren().add(circ));
            }
            var in = o.normToScr(context.inner.point);
            var out = o.normToScr(context.outer);
            var line = new Line(in.getX(), in.getY(), out.getX(), out.getY());
            Platform.runLater(() -> o.getChildren().add(line));

            throw new RuntimeException();
        }
        return new CycleCounts(stableCycles.size(), unstableCycles.size());
    }

    private void checkPoint(ArrayList<Point2D> cycles, SegmentGenerator path, Evaluator eval, CycleContext context) {
        var mistakes = new ArrayList<Point2D>();

        for (var p : cycles) {
            eval.resetT();
            eval.movePoint(p);
            //System.out.println("starting: " + eval.getCurrent());
            eval.next();
            //System.out.println("now: " + eval.getCurrent());

            try {
                var next = eval.getNextIsectLn(context.inner.point, context.outer);
                //System.out.println("from " + p + " to " + next);
                if (next.distance(p) > 3 * path.getInc()) {
                    System.out.println("pathinc: " + path.getInc());
                    System.out.println("mistake 1: " + next.distance(p));
                    throw new RootNotFound();
                }
            } catch (RootNotFound r) {
                System.out.println(p + " was a mistake");
                mistakes.add(p);
            }
        }
        for (var p : mistakes)
            cycles.remove(p);
    }
}
