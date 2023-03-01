package FXObjects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.ProgressBar;
import java.util.ArrayList;
import Evaluation.EvaluatorFactory;

import java.awt.*;

public class BasinFinderDispatcher extends Thread {
    private OutputPlane o;
    private int[][] field;
    private int fieldWidth, fieldHeight;
    private int fieldOffsetX, fieldOffsetY;
    private AtomicInteger radius;
    private AtomicBoolean done;
    private java.awt.Color col;
    private final int[] critImg;
    private final double inc;
    private static float colNum;
    private ProgressBar bar;

    public BasinFinderDispatcher(OutputPlane o, Point2D crit, boolean pos) {
        this.o = o;
        this.fieldWidth = o.canv.getWidth() * 2;
        this.fieldHeight = o.canv.getHeight() * 2;
        this.fieldOffsetX = o.canv.getWidth() / 4;
        this.fieldOffsetY = o.canv.getHeight() / 4;
        this.field = new int[fieldWidth][fieldHeight];
        this.radius = new AtomicInteger(1);
        // this.maxFillRadius = new AtomicInteger(10);
        this.done = new AtomicBoolean(false);
        this.col = new java.awt.Color(
                Color.getHSBColor(colNum, 1, 1).getRGB() & ((~0) >>> 8) | (1 << 30), true);
        colNum += Math.PI;
        // synchronized (o.g) {
        // o.g.setColor(col);
        // o.g.fillRect(o.imgNormToScrX(crit.getX()), o.imgNormToScrY(crit.getY()), 1, 1);
        // }
        this.critImg = toFieldCoords(crit);
        if (inBounds(critImg))
            this.field[critImg[0]][critImg[1]] = 1;
        var inc = (o.xMax.get() - o.xMin.get()) / o.canv.getWidth();
        this.inc = (pos ? o.inc : -o.inc);
        bar = new ProgressBar();
    }

    @Override
    public void run() {
        if (!inBounds(critImg))
            return;

        bar.setProgress(0);
        bar.setVisible(true);
        Platform.runLater(() -> o.progressBarBox.getChildren().add(bar));

        int finder_count = 1;

        BetterBasinFinder[] finders = new BetterBasinFinder[finder_count];
        for (int i = 0; i < finder_count; ++i) {
            finders[i] = new BetterBasinFinder();
            finders[i].start();
        }
        for (int i = 0; i < finder_count; ++i) {
            try {
                finders[i].join();
            } catch (InterruptedException ignored) {
            }
        }
        bar.setProgress(.95);
        System.out.println("filling");
        synchronized (o.g) {
            // o.g.setColor(col);
            for (int i = 0; i < fieldWidth; ++i) {
                for (int j = 0; j < fieldHeight; ++j) {
                    if (field[i][j] > 0) {
                        o.g.setColor(col);
                        var pt = fromFieldCoords(new int[] {i, j});
                        o.g.fillRect(o.imgNormToScrX(pt.getX()), o.imgNormToScrY(pt.getY()), 1, 1);
                    } // else if (field[i][j] < 0) {
                      // o.g.setColor(new java.awt.Color(
                      // Color.getHSBColor((float) (colNum + Math.PI), 1, 1).getRGB()
                      // & ((~0) >>> 8) | (1 << 30),
                      // true));
                      // var pt = fromFieldCoords(new int[] {i, j});
                      // o.g.fillRect(o.imgNormToScrX(pt.getX()), o.imgNormToScrY(pt.getY()), 1, 1);
                      // }
                }
            }
        }
        bar.setProgress(1);
        o.render();
        Platform.runLater(() -> o.progressBarBox.getChildren().remove(bar));
        System.out.println("done");

    }

    private int[] toFieldCoords(Point2D pt) {
        var canvasCoord = o.imgNormToScreen(pt);
        canvasCoord[0] += fieldOffsetX;
        canvasCoord[1] += fieldOffsetY;
        return canvasCoord;
    }

    private boolean inBounds(int[] fieldCoords) {
        return fieldCoords[0] >= 0 && fieldCoords[0] < fieldWidth && fieldCoords[1] >= 0
                && fieldCoords[1] < fieldHeight;
    }

    private Point2D fromFieldCoords(int[] pt) {
        return new Point2D(o.imgScrToNormX(pt[0] - fieldOffsetX),
                o.imgScrToNormY(pt[1] - fieldOffsetY));
    }



    private class BetterBasinFinder extends Thread {

        double maxT = 300;

        @Override
        public void run() {
            var e = EvaluatorFactory.getEvaluator(o.evalType, o.getDx(), o.getDy());
            while (!done.get()) {
                int rad = radius.getAndIncrement();
                synchronized (bar) {
                    System.out.println("setting progress to: "
                            + Math.pow((double) rad / (double) fieldWidth, .5));
                    bar.setProgress(Math.pow((double) rad / (double) fieldWidth, .5));
                }
                System.out.println(rad);
                double dTheta = 1.0 / (2 * Math.PI * rad);
                boolean foundAny = false;
                boolean anyWrong = false;
                for (double theta = 0; theta <= 2 * Math.PI; theta += dTheta) {
                    int imgX = (int) Math.round((rad * Math.cos(theta)));
                    int imgY = (int) Math.round((rad * Math.sin(theta)));
                    int[] fieldStart = {imgX + critImg[0], imgY + critImg[1]};
                    var start = fromFieldCoords(fieldStart);
                    // System.out.println(start);
                    e.initialise(start, o.getT(), o.a, o.b, inc);
                    var fieldPos = fieldStart;
                    var path = new ArrayList<int[]>();
                    path.add(fieldPos);
                    // byte found = 0;
                    int onBorderCount = 0;
                    int oobCount = 0;
                    while (inBounds(fieldPos) && onBorderCount < 999999 && oobCount < 9999999
                            && (field[fieldPos[0]][fieldPos[1]] < 2
                                    && field[fieldPos[0]][fieldPos[1]] > -2)
                            && e.getT() < maxT + o.getT() && e.getT() >= o.getT() - maxT) {
                        // int code = field[fieldPos[0]][fieldPos[1]];
                        // if (code != 0)
                        // if (found == 0)
                        // found = code;
                        // else if (found == code)
                        // ++foundCount;
                        // else {
                        // foundCount = 0;
                        // found = code;
                        // }
                        var next = e.next();
                        fieldPos = toFieldCoords(next);
                        // int[] tmp = {fieldPos[0], fieldPos[1]};
                        // tmp[0] -= critImg[0];
                        // tmp[1] -= critImg[1];
                        if (inBounds(fieldPos))
                            if (field[fieldPos[0]][fieldPos[1]] == 1) {
                                oobCount = 0;
                                ++onBorderCount;
                            } else if (field[fieldPos[0]][fieldPos[1]] == -1) {
                                onBorderCount = 0;
                                ++oobCount;
                            } else {
                                onBorderCount = 0;
                                oobCount = 0;
                            }
                        path.add(fieldPos);

                        // var mf = maxFillRadius.get();
                        // if (rad >= 10 && tmp[0] * tmp[0] + tmp[1] * tmp[1] < mf * mf) {
                        // // if (field[fieldPos[0]][fieldPos[1]] != 1)
                        // // throw new Error("oh shit");
                        // field[fieldPos[0]][fieldPos[1]] = 1;
                        // break;
                        // }

                    }
                    // synchronized (this)
                    {
                        if (rad < 10 || inBounds(fieldPos) && field[fieldPos[0]][fieldPos[1]] > 0) {
                            foundAny = true;
                            // System.out.println(fromFieldCoords(fieldPos));

                            for (var pt : path) {
                                field[pt[0]][pt[1]] = 1;// Math.max(field[pt[0]][pt[1]], 1);

                                int cnt = 0;
                                int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                                for (var offset : offsets) {
                                    offset[0] += pt[0];
                                    offset[1] += pt[1];
                                    if (inBounds(offset) && field[offset[0]][offset[1]] > 0)
                                        ++cnt;
                                }
                                if (cnt == 4)
                                    ++(field[pt[0]][pt[1]]);
                                // var normPt = fromFieldCoords(pt);
                                // System.out.println(normPt);
                                // synchronized (o.g) {
                                // o.g.setColor(col);
                                // o.g.fillRect(o.imgNormToScrX(normPt.getX()),
                                // o.imgNormToScrY(normPt.getY()), 1, 1);
                                // }
                            }
                        } else {
                            anyWrong = true;
                            for (var pt : path) {
                                if (inBounds(pt))
                                    field[pt[0]][pt[1]] = -1;
                                int cnt = 0;
                                int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                                for (var offset : offsets) {
                                    offset[0] += pt[0];
                                    offset[1] += pt[1];
                                    if (inBounds(offset) && field[offset[0]][offset[1]] < 0)
                                        ++cnt;
                                }
                                if (cnt == 4)
                                    field[pt[0]][pt[1]] = -2;
                            }
                        }
                    }
                }
                // if (!anyWrong && rad > 10)
                // maxFillRadius.set(Math.max(maxFillRadius.get(), rad));
                if (!foundAny || rad > fieldWidth && rad > fieldHeight) {
                    done.set(true);
                }
            }
        }

    }
}
