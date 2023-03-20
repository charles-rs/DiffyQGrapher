package FXObjects;


import Exceptions.RootNotFound;
import javafx.application.Platform;
import javafx.geometry.Point2D;

public class SemiStableHelper extends Thread {
    private static OutputPlane o;
    private static Thread parent;
    private static Point2D lnSt, lnNd;
    private Point2D prev, next, prevOld;

    SemiStableHelper(Point2D st, Point2D nx) {
        setDaemon(true);
        prev = st;
        next = nx;
    }

    static void init(OutputPlane _o, Thread _parent, Point2D _lnSt, Point2D _lnNd) {
        o = _o;
        parent = _parent;
        lnSt = _lnSt;
        lnNd = _lnNd;
    }

    @Override
    public void run() {
        o.in.drawLine(prev, next, o.in.awtSemiStableColor, 3);
        Platform.runLater(o.in::render);
        prevOld = prev;
        prev = next;
        while (o.in.inBounds(prev.getX(), prev.getY()) && !parent.isInterrupted()
                && !Thread.interrupted()) {
            try {
                next = o.semiStableMidpointPath(lnSt, lnNd, prev, prevOld);
                o.in.drawLine(prev, next, o.in.awtSemiStableColor, 3);
                Platform.runLater(o.in::render);
                prevOld = prev;
                prev = next;
                System.out.println(prev);
            } catch (RootNotFound r) {
                System.out.println("breaking");
                break;
            }
        }
    }
}
