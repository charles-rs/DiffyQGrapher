package FXObjects;


import Exceptions.RootNotFound;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.util.ArrayList;

public class GlobalBifurcationDriver extends Thread {

    private static OutputPlane o;
    private final GlobalBifurcationFinder finder;
    public static Thread parent;
    private Point2D prev, prevOld;

    GlobalBifurcationFinder.Orientation orient;

    ArrayList<Point2D> render;

    GlobalBifurcationDriver(GlobalBifurcationFinder finder, Point2D st, Point2D nx, GlobalBifurcationFinder.Orientation orient, ArrayList<Point2D> render) {
        //setDaemon(true);
        System.out.println("st: " + st);
        System.out.println("nx: " + nx);
        prevOld = st;
        prev = nx;
        this.orient = orient;
        this.finder = finder;
        this.render = render;
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
        o.in.drawLine(prevOld, prev, finder.getColor(), 3);
        Platform.runLater(o.in::render);
        render.add(prevOld);
        while (o.in.inBounds(prev.getX(), prev.getY()) && !parent.isInterrupted()
                && !Thread.interrupted()) {
            try {
                render.add(prev);
                var next = finder.globalBifPred(prev, prevOld, orient);
                //next = finder.semiStableFinitePath(prev.getX(), prev.getY(), FinitePathType.ARC, prevOld);
                //next = finder.semiStableMidpointPath(prev, prevOld);
                o.in.drawLine(prev, next, finder.getColor(), 3);
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
