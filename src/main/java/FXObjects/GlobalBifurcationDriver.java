package FXObjects;


import Exceptions.RootNotFound;
import javafx.application.Platform;
import javafx.geometry.Point2D;

public class GlobalBifurcationDriver extends Thread {

    private static OutputPlane o;
    private final GlobalBifurcationFinder finder;
    public static Thread parent;
    private Point2D prev, prevOld;

    GlobalBifurcationFinder.Orientation orient;

    GlobalBifurcationDriver(GlobalBifurcationFinder finder, Point2D st, Point2D nx, GlobalBifurcationFinder.Orientation orient) {
        //setDaemon(true);
        System.out.println("st: " + st);
        System.out.println("nx: " + nx);
        prevOld = st;
        prev = nx;
        this.orient = orient;
        this.finder = finder;
    }

    static void init(OutputPlane _o, Thread _parent) {
        o = _o;
        parent = _parent;
    }

    static final Object lock = new Object();

    @Override
    public void run() {
        synchronized (lock) {
            System.out.println("_prevOld: " + prevOld);
            System.out.println("_prev:    " + prev);
        }
        o.in.drawLine(prevOld, prev, o.in.awtSemiStableColor, 3);
        Platform.runLater(o.in::render);
        while (o.in.inBounds(prev.getX(), prev.getY()) && !parent.isInterrupted()
                && !Thread.interrupted()) {
            try {
                var next = finder.globalBifPred(prev, prevOld, orient);
                //next = finder.semiStableFinitePath(prev.getX(), prev.getY(), FinitePathType.ARC, prevOld);
                //next = finder.semiStableMidpointPath(prev, prevOld);
                o.in.drawLine(prev, next, o.in.awtSemiStableColor, 3);
                Platform.runLater(o.in::render);
                prevOld = prev;
                prev = next;
                System.out.println(prev);
            } catch (RootNotFound r) {
                System.out.println("breaking");
                break;
            } catch (NullPointerException npe) {
                parent.interrupt();
                break;
            }
        }
    }
}
