package FXObjects;

import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
import Exceptions.RootNotFound;
import javafx.geometry.Point2D;

public class SaddleConFinder extends GlobalBifurcationFinder<SaddleConFinder.SaddleOrientation> {

    private final SaddleConTransversal trans;
    private SepStart s1, s2;

    public SaddleConFinder(OutputPlane o, SaddleConTransversal trans, SepStart s1, SepStart s2) {
        this.trans = trans.clone();
        this.s1 = s1.clone();
        this.s2 = s2.clone();
        this.o = o;
    }

    @Override
    public SaddleConFinder clone() {
        return new SaddleConFinder(o, trans, s1, s2);
    }

    @Override
    protected String getName() {
        return null;
    }


    @Override
    SaddleOrientation classify(Point2D p) throws RootNotFound {
        trans.update(p);
        s1 = s1.update(p, o);
        s2 = s2.update(p, o);
        final var st = trans.getStart();
        final var end = trans.getEnd();
        Evaluator eval = EvaluatorFactory.getBestEvaluator(o.getDx(), o.getDy());
        eval.initialise(s1.getStart(o.inc), 0, p.getX(), p.getY(), s1.getInc(o.inc));
        var n1 = eval.getNextIsectLn(st, end);
        eval.initialise(s2.getStart(o.inc), 0, p.getX(), p.getY(), s2.getInc(o.inc));
        var n2 = eval.getNextIsectLn(st, end);
        return new SaddleOrientation(n1.distance(st) < n2.distance(st));
    }

    public static class SaddleOrientation extends SigIntf {
        final boolean left;

        public SaddleOrientation(boolean left) {
            this.left = left;
        }

        @Override
        public boolean bifurcatesFrom(SigIntf _other) {
            if (_other instanceof SaddleOrientation other)
                return other.left != this.left;
            throw new ClassCastException();
        }

        @Override
        public boolean bifurcatesTo(SigIntf _other) {
            if (_other instanceof SaddleOrientation other)
                return other.left != this.left;
            throw new ClassCastException();
        }

        @Override
        public boolean isZero() {
            return left;
        }
    }

}
