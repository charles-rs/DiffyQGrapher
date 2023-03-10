package FXObjects;

import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
import Exceptions.NoMoreXException;

import java.awt.*;

public class DivergenceFinder extends Thread {
    private static OutputPlane o;
    private static Double xCurrent;
    private static double inc;
    private static double incY;
    private static Color divCol;
    private static Color convCol;
    private static boolean pos;

    public DivergenceFinder() {
        setDaemon(true);
    }

    @Override
    public void run() {
        Evaluator e = EvaluatorFactory.getEvaluator(o.evalType, o.getDx(), o.getDy());
        try {
            double x;
            boolean converged;
            while (true) {
                x = getNextX();
                for (double y = o.yMin.get(); y < o.yMax.get(); y += incY) {
                    converged = false;
                    if (pos)
                        e.initialise(x, y, o.getT(), o.a, o.b, inc);
                    else
                        e.initialise(x, y, o.getT(), o.a, o.b, -inc);
                    while (e.getCurrent().distance(x, y) < (o.xMax.get() - o.xMin.get() * 10)) {
                        if (e.getT() > o.settings.tDist + o.getT()
                                || e.getT() < o.getT() - o.settings.tDist) {
                            converged = true;
                            break;
                        }

                        if (e.getCurrent().distance(e.next()) < Math.abs(inc / 10000))
                            break;
                    }
                    synchronized (o.g) {
                        if (converged)
                            o.g.setColor(convCol);
                        else
                            o.g.setColor(divCol);
                        o.g.fillRect(o.imgNormToScrX(x), o.imgNormToScrY(y), 1, 1);
                    }
                }
                o.render();
            }
        } catch (NoMoreXException ex) {
            System.out.println("done");
        }
    }

    public static void init(OutputPlane o_, boolean pos_) {
        o = o_;
        xCurrent = o.xMin.get();
        pos = pos_;
        inc = (o.xMax.get() - o.xMin.get()) / o.canv.getWidth();
        incY = (o.yMax.get() - o.yMin.get()) / o.canv.getHeight();
        divCol = new Color(o.awtDivBifDivColor.getRGB() & ((~0) >>> 8) | (1 << 30), true);
        convCol = new Color(o.awtDivBifConvColor.getRGB() & ((~0) >>> 8) | (1 << 30), true);
    }

    private synchronized static double getNextX() throws NoMoreXException {
        if (xCurrent > o.xMax.get())
            throw new NoMoreXException();
        double temp = xCurrent;
        xCurrent += inc;
        return temp;
    }
}
