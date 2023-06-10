package FXObjects;

import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
import Exceptions.RootNotFound;
import Utils.MyClonable;
import javafx.geometry.Point2D;

import java.awt.*;

public class SaddleConFinder extends GlobalBifurcationFinder<SaddleConFinder.SaddleOrientation, SaddleConFinder.SaddleContext> {

    @Override
    protected Color getColor() {
        if (globalContext.trans.homo)
            return o.in.awtHomoSaddleConColor;
        else
            return o.in.awtHeteroSaddleConColor;
    }


    public SaddleConFinder(OutputPlane o, SaddleConTransversal trans, SepStart s1, SepStart s2) {
        globalContext = new SaddleContext(trans.clone(), s1.clone(), s2.clone());
        this.o = o;
    }

    @Override
    public SaddleConFinder clone() {
        return new SaddleConFinder(o, globalContext.trans, globalContext.s1, globalContext.s2);
    }

    @Override
    protected String getName() {
        return "saddlecon";
    }


    @Override
    SaddleOrientation classify(Point2D p, SaddleContext context) throws RootNotFound {
        context.trans.update(p);
        context.s1 = context.s1.update(p, o);
        context.s2 = context.s2.update(p, o);
        final var st = context.trans.getStart();
        final var end = context.trans.getEnd();
        Evaluator eval = EvaluatorFactory.getBestEvaluator(o.getDx(), o.getDy());
        eval.initialise(context.s1.getStart(o.inc), 0, p.getX(), p.getY(), context.s1.getInc(o.inc));
        var n1 = eval.getNextIsectLn(st, end);
        eval.initialise(context.s2.getStart(o.inc), 0, p.getX(), p.getY(), context.s2.getInc(o.inc));
        var n2 = eval.getNextIsectLn(st, end);
        return new SaddleOrientation(n1.distance(st) > n2.distance(st));
    }

    public static class SaddleContext implements MyClonable {
        private SaddleConTransversal trans;
        private SepStart s1, s2;

        public SaddleContext(SaddleConTransversal trans, SepStart s1, SepStart s2) {
            this.trans = trans;
            this.s1 = s1;
            this.s2 = s2;
        }


        @Override
        public SaddleContext clone() {
            return new SaddleContext(trans.clone(), s1.clone(), s2.clone());
        }
    }

    public static class SaddleOrientation extends SigIntf {
        final boolean left;

        public SaddleOrientation(boolean left) {
            this.left = left;
        }

        @Override
        public boolean bifurcatesFrom(SigIntf _other) {
            if (_other instanceof SaddleOrientation other)
                return !this.left && other.left;
            throw new ClassCastException();
        }

        @Override
        public boolean bifurcatesTo(SigIntf _other) {
            if (_other instanceof SaddleOrientation other)
                return this.left && !other.left;
            throw new ClassCastException();
        }

        @Override
        public boolean isZero() {
            return left;
        }

        @Override
        public String toString() {
            return Boolean.toString(left);
        }
    }

}
