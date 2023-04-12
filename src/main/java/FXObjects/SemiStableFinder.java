package FXObjects;

import Evaluation.CriticalPoint;
import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
import Exceptions.RootNotFound;
import PathGenerators.*;
import javafx.geometry.Point2D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

class SemiStableFinder {
    OutputPlane o;
    CriticalPoint inner;
    Point2D outer;

    double incX, incY;

    public SemiStableFinder(@NotNull OutputPlane o, @NotNull CriticalPoint inner, @NotNull Point2D outer) {
        this.o = o;
        this.inner = inner;
        this.outer = outer;
        incX = (o.xMax.get() - o.xMin.get()) / 512;
        incY = (o.yMax.get() - o.yMin.get()) / 512;
    }

    private static class CycleCounts {
        public final int stable, unstable;

        public CycleCounts(int s, int u) {
            stable = s;
            unstable = u;
        }

        /**
         * other has the cycles.
         */
        public boolean bifurcatesFrom(CycleCounts other) {
            return this.stable == other.stable + 1 && this.unstable == other.unstable + 1;
        }

        public boolean bifurcatesTo(CycleCounts other) {
            return this.stable == other.stable - 1 && this.unstable == other.unstable - 1;
        }

        public boolean bifurcates(CycleCounts other) {
            return bifurcatesFrom(other) || bifurcatesTo(other);
        }

        public boolean isZero() {
            return (stable | unstable) == 0;
        }

        @Override
        public String toString() {
            return "(" + stable + ", " + unstable + ")";
        }
    }

    void updateInner(Point2D params) throws RootNotFound {
        var eval = EvaluatorFactory.getBestEvaluator(o.getDx(), o.getDy());
        var tmp = eval.findCritical(inner.point, params.getX(), params.getY(), 0);
        var diff = tmp.point.subtract(inner.point);
        inner = tmp;
        outer = outer.add(diff);
    }

    Point2D[] semiStableLoop(Point2D center) throws RootNotFound {
//        double px = ((o.in.xMax.get() - o.in.xMin.get() + o.in.yMax.get() - o.in.yMin.get()) / 2)
//                / ((o.in.getWidth() + o.in.getHeight()) / 2D);
        //var gen = GeneratorFactory.getLoopGenerator(LoopType.CIRCLE, px, center, 3);
        var gen = new EllipseGenerator(Math.PI / 16, center, 5 * o.in.getPxX(), 5 * o.in.getPxY());
        CycleCounts count1;
        System.out.println("center: " + center);
        System.out.println("start: " + gen.getCurrent());
        var count2 = countCycles(gen.getCurrent());
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
            count2 = countCycles(next);
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


    public enum NoCycles {LEFT, RIGHT}

    /**
     * @param prev    the previous point on the bifurcation
     * @param prevOld the anteprevious point on the bifurcation
     * @param orient  current orientation. LEFT means there are 2 cycles on the left
     */
    public Point2D semiStablePred(Point2D prev, Point2D prevOld, NoCycles orient) throws RootNotFound {
        var diff = prev.subtract(prevOld);
        var predict = prev.add(diff);
        var count = countCycles(predict);

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

        double rad = 5;
        var gen = GeneratorFactory.getArcGenerator(1 / rad, prev, o.in.getPxX() * rad, o.in.getPxY() * rad,
                theta, theta + sign * Math.PI / 2,
                sign == 1 ? ArcDirection.ANTICLOCKWISE : ArcDirection.CLOCKWISE
        );
        while (!gen.done()) {
            var old = gen.getCurrent();
            var new_count = countCycles(gen.next());
            o.in.drawLine(old, gen.getCurrent(), o.in.awtHopfBifColor);
            System.out.println("count: " + count);
            System.out.println("newcount: " + new_count);
            if (new_count.bifurcates(count))
                return gen.getCurrent().midpoint(old);
            count = new_count;
        }
        throw new RootNotFound();
    }

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

    Point2D semiStableFinitePath(double a, double b,
                                 FinitePathType tp, Point2D prev) throws RootNotFound {

        var p = new Point2D(a, b);
        var cycleCounts = countCycles(p);
        System.out.println("starting with " + cycleCounts.stable + ", " + cycleCounts.unstable);
        double px = ((o.in.xMax.get() - o.in.xMin.get() + o.in.yMax.get() - o.in.yMin.get()) / 2)
                / ((o.in.canv.getWidth() + o.in.canv.getHeight()) / 2D);
        double pxX = 5 * o.in.getPxX();
        double pxY = 5 * o.in.getPxY();
        if (tp.equals(FinitePathType.SPIRAL)) {
            pxX *= 2;
            pxY *= 2;
        }
        var s = GeneratorFactory.getFinitePathGenerator(tp, pxX, pxY, p, 15, prev);
        var pOld = p;
        p = s.next();
        while (!Thread.interrupted() && !s.done()) {
            var count = countCycles(p);
            System.out.println("starting with " + cycleCounts.stable + ", " + cycleCounts.unstable);
            System.out.println("now with " + count.stable + ", " + count.unstable);
            if (count.bifurcates(cycleCounts))
                break;
            cycleCounts = count;
//            if (count.stable == 0 && count.unstable == 1) {
//                System.out.println(p);
//                throw new RuntimeException();
//            }
            p = s.next();
        }
        if (s.done())
            throw new RootNotFound();
        return p.midpoint(pOld);
    }


    private enum CycleDir {
        BACKWARD, FORWARD, UNKNOWN
    }

    private static class CycleDirWrap {
        public CycleDir val;

        public CycleDirWrap(CycleDir v) {
            val = v;
        }
    }

    CycleCounts countCycles(Point2D p) throws RootNotFound {
        updateInner(p);
        var stableCycles = new ArrayList<Point2D>();
        var unstableCycles = new ArrayList<Point2D>();
        var path = new SegmentGenerator(inner.point, outer, incX, incY);
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
        while (!path.done()) {
            eval.resetT();
            eval.movePoint(path.next());
            eval.next();
            //System.out.println("state is now: " + dir);
            var prev = path.getCurrent();
            //System.out.println(prev);
            try {
                var next = eval.getNextIsectLn(inner.point, outer);
                //System.out.println(next);
                if (next.distance(inner.point) < prev.distance(inner.point)) {
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
                }
            } catch (RootNotFound r) {
                //dir.val = CycleDir.UNKNOWN;
                eval.negate();
            }
        }
        stableCycles.removeIf((Point2D pt) -> pt.distance(inner.point) < 5 * path.getInc());
        unstableCycles.removeIf((Point2D pt) -> pt.distance(inner.point) < 5 * path.getInc());
        //System.out.println(new CycleCounts(stableCycles.size(), unstableCycles.size()));

        if (eval.getInc() < 0)
            eval.negate();
        checkPoint(unstableCycles, path, eval);
        eval.negate();
        checkPoint(stableCycles, path, eval);
//        for (var pt : stableCycles) {
//            var scrCoords = o.normToScr(pt);
//            var circ = new Circle(scrCoords.getX(), scrCoords.getY(), 3);
//            circ.setVisible(true);
//            circ.setFill(new Color(0, 1, 0, 1));
//            Platform.runLater(() -> o.getChildren().add(circ));
//        }
//        for (var pt : unstableCycles) {
//            var scrCoords = o.normToScr(pt);
//            var circ = new Circle(scrCoords.getX(), scrCoords.getY(), 3);
//            circ.setVisible(true);
//            circ.setFill(new Color(0, 0, 1, 1));
//            Platform.runLater(() -> o.getChildren().add(circ));
//        }
//
//        if (true) throw new RuntimeException();
        return new CycleCounts(stableCycles.size(), unstableCycles.size());
    }

    private void checkPoint(ArrayList<Point2D> cycles, SegmentGenerator path, Evaluator eval) {
        var mistakes = new ArrayList<Point2D>();

        for (var p : cycles) {
            eval.resetT();
            eval.movePoint(p);
            //System.out.println("starting: " + eval.getCurrent());
            eval.next();
            //System.out.println("now: " + eval.getCurrent());

            try {
                var next = eval.getNextIsectLn(inner.point, outer);
                //System.out.println("from " + p + " to " + next);
                if (next.distance(p) > path.getInc()) {
                    //System.out.println("mistake 1");
                    throw new RootNotFound();
                }
            } catch (RootNotFound r) {
                //System.out.println(p + " was a mistake");
                mistakes.add(p);
            }
        }
        for (var p : mistakes)
            cycles.remove(p);
    }
}
