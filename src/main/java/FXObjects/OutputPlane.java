package FXObjects;

import AST.Derivative;
import AST.Maths;
import AST.Value;
import Evaluation.*;
import Events.HopfPointSelected;
import Events.SaddleSelected;
import Exceptions.BadSaddleTransversalException;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import Settings.OutPlaneSettings;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import lwon.data.Array;
import lwon.data.Dictionary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class OutputPlane extends CoordPlane {
    /**
     * Bounds for calculating saddle connections
     */
    public double dSaddleXMin, dSaddleXMax, dSaddleYMin, dSaddleYMax;


    private final Line cycleLine;
    private CriticalPoint semiStableInner;
    private Point2D semiStableOuter;

    int limCycStep = 0;


    public VBox progressBarBox;

    private double t = 0;
    private List<InitCond> initials;
    private List<InitCond> isoclines;
    private List<CriticalPoint> criticalPoints;
    private List<Point2D> selectedCritPoints;
    private List<Point2D> horizIsos;
    private List<Point2D> vertIsos;
    private final List<SepStart> selectedSeps;
    private List<LimCycleStart> limCycles;
    double inc;
    volatile double a, b;
    private Derivative dx, dy;
    public EvalType evalType;
    private ClickModeType clickMode;
    private boolean drawSep = true;
    private CriticalPoint currentPoint = null;

    private Color solutionColor;
    private Color isoclineColor;
    private Color horizIsoColor;
    private Color vertIsoColor;
    private Color stblSeparatrixColor;
    private Color unstblSeparatrixColor;
    private Color criticalColor;
    private Color attrLimCycleColor;
    private Color repLimCycleColor;
    private Color divBifConvColor;
    private Color divBifDivColor;

    java.awt.Color awtSolutionColor;// = fromFXColor(solutionColor);
    private java.awt.Color awtIsoclineColor;// = fromFXColor(isoclineColor);
    private java.awt.Color awtHorizIsoColor;// = fromFXColor(horizIsoColor);
    private java.awt.Color awtVertIsoColor;// = fromFXColor(vertIsoColor);
    private java.awt.Color awtStblSeparatrixColor;// = fromFXColor(stblSeparatrixColor);
    private java.awt.Color awtUnstblSeparatrixColor;// = fromFXColor(unstblSeparatrixColor);
    private java.awt.Color awtCriticalColor;// = fromFXColor(criticalColor);
    private java.awt.Color awtAttrLimCycleColor;// = fromFXColor(attrLimCycleColor);
    private java.awt.Color awtRepLimCycleColor;// = fromFXColor(repLimCycleColor);
    java.awt.Color awtDivBifConvColor;
    java.awt.Color awtDivBifDivColor;

    public OutPlaneSettings settings;

    private Point2D saddleTravStart;
    private boolean saddleTravStarted = false;

    public InputPlane in;
    final Canvas labelCanv;

    private Thread limCycleArtist = new Thread(), limCycleUpdater = new Thread(),
            solutionArtist = new Thread();


    public OutputPlane(double side, TextField tField, OutPlaneSettings settings) {
        super(side);
        setClickMode(ClickModeType.DRAWPATH);
        limCycleArtist.setDaemon(true);
        limCycleUpdater.setDaemon(true);
        solutionArtist.setDaemon(true);
        labelCanv = new Canvas();
        labelCanv.widthProperty().bind(widthProperty());
        labelCanv.heightProperty().bind(heightProperty());
        getChildren().addAll(labelCanv);


        SaddleConTransversal.init(this);
        this.settings = settings;
        currentInstrCode = 0;
        updateSettings();

        cycleLine = new Line();
        this.getChildren().addAll(cycleLine);
        cycleLine.setVisible(false);

        dSaddleXMax = 25;// this.xMax.get();
        dSaddleXMin = -25;// this.xMin.get();
        dSaddleYMax = 25;// this.yMax.get();
        dSaddleYMin = -25;// this.yMin.get();

        evalType = EvalType.RKF45;
        initials = new ArrayList<>();
        criticalPoints = new CopyOnWriteArrayList<>();
        isoclines = new ArrayList<>();
        limCycles = new ArrayList<>();
        horizIsos = new ArrayList<>();
        vertIsos = new ArrayList<>();
        selectedCritPoints = new ArrayList<>();
        // artistArmy = new ArrayList<>(8);
        // for(int i = 0; i < 8; i++) artistArmy.add(new SolutionArtist());
        selectedSeps = new ArrayList<>(2);
        draw();
        // render();
        tField.setText(Double.toString(t));

        setOnKeyPressed((e) -> {
            var xdiff = (xMax.get() - xMin.get()) / 20;
            var ydiff = (yMax.get() - yMin.get()) / 20;
            if (e.getCode() == left) {
                xMin.set(xMin.get() - xdiff);
                xMax.set(xMax.get() - xdiff);
            } else if (e.getCode() == right) {
                xMin.set(xMin.get() + xdiff);
                xMax.set(xMax.get() + xdiff);
            } else if (e.getCode() == up) {
                yMin.set(yMin.get() + ydiff);
                yMax.set(yMax.get() + ydiff);
            } else if (e.getCode() == down) {
                yMin.set(yMin.get() - ydiff);
                yMax.set(yMax.get() - ydiff);
            }
            KeyCode temp = e.getCode();
            if (temp == left || temp == right || temp == up || temp == down) {
                draw();
                e.consume();
            }
        });
        labelCanv.toFront();
        loading.toFront();
        labelCanv.setVisible(true);



        /*
         * double px = (((this.xMax.get() - this.xMin.get()) + (this.yMax.get() -
         * this.yMin.get()))/2)/ (1024); FinitePathGenerator s =
         * GeneratorFactory.getFinitePathGenerator(FinitePathType.ARC, px, new Point2D(1, 1), 1D,
         * new Point2D(0, 0)); System.out.println(s.getCurrent()); while(!s.done()) {
         * drawLine(s.getCurrent(), s.next(), java.awt.Color.GREEN); drawLine(s.getCurrent(),
         * s.next(), java.awt.Color.RED); drawLine(s.getCurrent(), s.next(), java.awt.Color.BLUE);
         * System.out.println(s.next());
         *
         * } System.out.println(s.getCurrent()); render();
         */
    }

    private void initColors() {
        awtSolutionColor = fromFXColor(solutionColor);
        awtIsoclineColor = fromFXColor(isoclineColor);
        awtHorizIsoColor = fromFXColor(horizIsoColor);
        awtVertIsoColor = fromFXColor(vertIsoColor);
        awtStblSeparatrixColor = fromFXColor(stblSeparatrixColor);
        awtUnstblSeparatrixColor = fromFXColor(unstblSeparatrixColor);
        awtCriticalColor = fromFXColor(criticalColor);
        awtAttrLimCycleColor = fromFXColor(attrLimCycleColor);
        awtRepLimCycleColor = fromFXColor(repLimCycleColor);
        awtDivBifConvColor = fromFXColor(divBifConvColor);
        awtDivBifDivColor = fromFXColor(divBifDivColor);
    }

    public Derivative getDx() {
        return (Derivative) dx.clone();
    }

    public Derivative getDy() {
        return (Derivative) dy.clone();
    }

    public void updateSettings() {
        if (settings.staticInc)
            inc = settings.inc;
        else
            inc = ((xMax.get() - xMin.get()) / 512 + (yMax.get() - yMin.get()) / 512) / 2;
        solutionColor = settings.solutionColor;
        isoclineColor = settings.isoclineColor;
        horizIsoColor = settings.horizIsoColor;
        vertIsoColor = settings.vertIsoColor;
        stblSeparatrixColor = settings.stblSeparatrixColor;
        unstblSeparatrixColor = settings.unstblSeparatrixColor;
        criticalColor = settings.criticalColor;
        attrLimCycleColor = settings.attrLimCycleColor;
        repLimCycleColor = settings.repLimCycleColor;
        divBifConvColor = settings.divBifConvColor;
        divBifDivColor = settings.divBifDivColor;
        initColors();
        this.drawAxes = settings.drawAxes;
        drawAxes(false);

    }

    public void setClickMode(ClickModeType cl) {
        this.clickMode = cl;
        clickModeTxt.set(cl.toString());
        switch (cl) {
            case DRAWPATH -> fireUpdate(0);
            case DRAWISO -> fireUpdate(1);
            case FINDCRITICAL -> fireUpdate(2);
            case DRAWHORIZISO -> fireUpdate(3);
            case LINEARISATION -> fireUpdate(4);
            case SELECTSADDLE -> fireUpdate(5);
            case SELECTHOPFPOINT -> fireUpdate(6);
            case DRAWVERTISO -> fireUpdate(7);
            case DRAWBASIN -> fireUpdate(8);
            case DRAWCOBASIN -> fireUpdate(9);
            case FINDLIMCYCLE -> fireUpdate(10);
            case SELECTSEP -> fireUpdate(20);
            case SELECTHOMOCENTER -> fireUpdate(25);
            case SETTRAVERSAL -> fireUpdate(30);
            case SETDIRECTION -> fireUpdate(35);
            case SEMISTABLE -> fireUpdate(40);
        }
    }


    public ClickModeType getClickMode() {
        return clickMode;
    }

    public double getT() {
        return t;
    }

    @Override
    protected void updateForZoom() {
        if (!settings.staticInc)
            inc = ((xMax.get() - xMin.get()) / 512 + (yMax.get() - yMin.get()) / 512) / 2;
        // synchronized (basinG)
        // {
        // basinG.setColor(new java.awt.Color(255, 255, 255, 255));
        // basinG.clearRect(0, 0, basinImg.getWidth(), basinImg.getHeight());
        // }
        BasinFinder.reset();
    }

    @Override
    protected void updateForResize() {
        // clearReset();
        // for(CriticalPoint c : criticalPoints)
        // labelCritical(c);
        synchronized (labelCanv) {
            labelCanv.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
            for (var c : criticalPoints) {
                labelCritical(c);
            }
        }
    }

    public void clearObjects() {
        selectedCritPoints.clear();
        criticalPoints.clear();
        selectedSeps.clear();
        // clearReset();
        synchronized (labelCanv) {
            labelCanv.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        }
    }

    public void updateA(double a) {
        if (a != this.a) {
            this.a = a;
            Platform.runLater(this::draw);
            // draw();
        }
    }

    public void updateT(double t) {
        if (t != this.t) {
            this.t = t;
            Platform.runLater(this::draw);
        }
    }

    public void updateB(double b) {
        if (b != this.b) {
            this.b = b;
            Platform.runLater(this::draw);
            // draw();
        }
    }

    public void updateDX(Derivative temp) {
        dx = temp;
    }

    public void updateDY(Derivative temp) {
        dy = temp;
    }

    void addCritical(Point2D pt) {
        try {
            CriticalPoint root =
                    EvaluatorFactory.getBestEvaluator(dx, dy).findCritical(pt, a, b, t);
            boolean add = true;
            for (var existing : criticalPoints)
                if (existing.point.distance(root.point) < Math.ulp(20.0))
                    add = false;
            if (add) {
                criticalPoints.add(root);
                labelCritical(root);
                if (drawSep && root.type == CritPointTypes.SADDLE)
                    drawSep(root);
                render();
            }
        } catch (RootNotFound r) {
            // TODO better output system
            System.out.println("Root not found");
        }
    }


    @Override
    public void handleMouseClick(MouseEvent e) {
        double x = scrToNormX(e.getX());
        double y = scrToNormY(e.getY());
        double r2 = 1;// x * x + y * y;
        InitCond temp = new InitCond(x / r2, y / r2, t);
        Point2D pt = new Point2D(x / r2, y / r2);
        switch (clickMode) {
            case DRAWPATH -> {
                initials.add(temp);
                drawGraph(temp, true, awtSolutionColor);
                render();
            }
            case FINDCRITICAL -> {
                addCritical(pt);
            }
            case LINEARISATION -> {
                try {
                    CriticalPoint root = critical(pt);
                    new LinearisationWindow(root);
                } catch (RootNotFound r) {
                    System.out.println("Root not found");
                }
                setClickMode(ClickModeType.DRAWPATH);
            }
            case DRAWHORIZISO -> {
                drawHorizIso(pt);
                horizIsos.add(pt);
                setClickMode(ClickModeType.DRAWPATH);
                render();
            }
            case DRAWVERTISO -> {
                drawVertIso(new Point2D(x, y));
                vertIsos.add(pt);
                setClickMode(ClickModeType.DRAWPATH);
                render();
            }
            case DRAWISO -> {
                isoclines.add(temp);
                drawIso(temp);
                render();
            }
            case DRAWBASIN -> {
                drawBasin(pt);
                setClickMode(ClickModeType.DRAWPATH);
            }
            case DRAWCOBASIN -> {
                drawCoBasin(pt);
                setClickMode(ClickModeType.DRAWPATH);
            }
            case SELECTSADDLE -> {
                try {
                    Point2D p = getSaddle(pt);
                    fireEvent(new SaddleSelected(p));
                    selectedCritPoints.add(p);
                    drawSelectedCritPoints();
                    render();
                } catch (RootNotFound ignored) {
                }
            }
            case SELECTHOPFPOINT -> {
                try {
                    Point2D p = getPointForHopf(pt);
                    fireEvent(new HopfPointSelected(p));
                    selectedCritPoints.add(p);
                    drawSelectedCritPoints();
                    render();
                } catch (RootNotFound ignored) {
                }
            }
            case SELECTSEP -> {
                try {
                    CriticalPoint p = critical(pt);
                    if (selectedSeps.size() < 2) {
                        double tol = (xMax.get() - xMin.get()) / canv.getWidth();
                        Point2D p1 = new Point2D(
                                p.point.getX() + tol * p.matrix.getEigenVector(0).get(0),
                                p.point.getY() + tol * p.matrix.getEigenVector(0).get(1));
                        Point2D p2 = new Point2D(
                                p.point.getX() - tol * p.matrix.getEigenVector(0).get(0),
                                p.point.getY() - tol * p.matrix.getEigenVector(0).get(1));

                        Point2D p3 = new Point2D(
                                p.point.getX() + tol * p.matrix.getEigenVector(1).get(0),
                                p.point.getY() + tol * p.matrix.getEigenVector(1).get(1));
                        Point2D p4 = new Point2D(
                                p.point.getX() - tol * p.matrix.getEigenVector(1).get(0),
                                p.point.getY() - tol * p.matrix.getEigenVector(1).get(1));
                        double d1 = pt.distance(p1);
                        double d2 = pt.distance(p2);
                        double d3 = pt.distance(p3);
                        double d4 = pt.distance(p4);
                        double min = Math.min(Math.min(d1, d2), Math.min(d3, d4));
                        boolean firstPos = p.matrix.getEigenvalue(0).getReal() > 0;
                        if (d1 == min)
                            selectedSeps.add(new SepStart(p, true, firstPos));
                        else if (d2 == min)
                            selectedSeps.add(new SepStart(p, false, firstPos));
                        else if (d3 == min)
                            selectedSeps.add(new SepStart(p, true, !firstPos));
                        else
                            selectedSeps.add(new SepStart(p, false, !firstPos));
                        draw();
                        if (selectedSeps.size() == 2) {
                            if (e.isControlDown() || e.isMetaDown()) {
                                limCycStep = 0;
                                setClickMode(ClickModeType.SETTRAVERSAL);
                            } else if (e.isAltDown()) {
                                setClickMode(ClickModeType.SETDIRECTION);
                            } else {
                                if (selectedSeps.get(0).saddle.point
                                        .distance(selectedSeps.get(1).saddle.point) < inc / 100) {
                                    setClickMode(ClickModeType.SELECTHOMOCENTER);
                                } else {
                                    try {
                                        SaddleConTransversal transversal =
                                                new SaddleConTransversal(selectedSeps.get(0).saddle,
                                                        selectedSeps.get(1).saddle);
                                        in.artist = new Thread(() -> {
                                            Platform.runLater(() -> in.loading.setVisible(true));
                                            synchronized (selectedSeps) {
                                                synchronized (in) {
                                                    renderSaddleCon(new Point2D(a, b),
                                                            selectedSeps.get(0),
                                                            selectedSeps.get(1), true, transversal);
                                                    selectedSeps.clear();
                                                    System.out.println("\n\nclearing seps\n\n");
                                                    draw();
                                                }
                                            }
                                            Platform.runLater(() -> in.loading.setVisible(false));
                                        });
                                        in.artist.setDaemon(true);
                                        in.artist.start();


                                    } catch (BadSaddleTransversalException ignored) {
                                    }

                                    selectedCritPoints.clear();
                                    setClickMode(ClickModeType.DRAWPATH);
                                }

                            }
                        } else
                            fireUpdate(21);
                    }
                } catch (RootNotFound ignored) {
                }
            }
            case SETDIRECTION -> {
                try {
                    SaddleConTransversal transversal =
                            new SaddleConTransversal(pt, selectedSeps.get(0).saddle);
                    in.artist = new Thread(() -> {
                        Platform.runLater(() -> in.loading.setVisible(true));
                        synchronized (selectedSeps) {
                            synchronized (in) {
                                renderSaddleCon(new Point2D(a, b), selectedSeps.get(0),
                                        selectedSeps.get(1), true, transversal);
                                selectedSeps.clear();
                            }
                        }
                        Platform.runLater((() -> in.loading.setVisible(false)));
                    });
                    in.artist.setDaemon(true);
                    in.artist.start();
                    setClickMode(ClickModeType.DRAWPATH);
                } catch (BadSaddleTransversalException ignored) {
                }
            }
            case SETTRAVERSAL -> {
                if (!saddleTravStarted) {
                    saddleTravStart = pt;
                    saddleTravStarted = true;
                    fireUpdate(31);
                } else {
                    SaddleConTransversal transversal = new SaddleConTransversal(saddleTravStart, pt,
                            selectedSeps.get(0).saddle.point
                                    .distance(selectedSeps.get(1).saddle.point) < inc / 100);
                    in.artist = new Thread(() -> {
                        Platform.runLater(() -> in.loading.setVisible(true));
                        synchronized (selectedSeps) {
                            synchronized (in) {
                                renderSaddleCon(new Point2D(a, b), selectedSeps.get(0),
                                        selectedSeps.get(1), true, transversal);
                                selectedSeps.clear();
                            }
                        }
                        Platform.runLater(() -> in.loading.setVisible(false));
                    });
                    in.artist.setDaemon(true);
                    in.artist.start();
                    saddleTravStarted = false;
                    setClickMode(ClickModeType.DRAWPATH);
                }
            }
            case SELECTHOMOCENTER -> {
                try {
                    CriticalPoint center = critical(pt);
                    try {
                        SaddleConTransversal transversal =
                                new SaddleConTransversal(selectedSeps.get(0).saddle, center);
                        in.artist = new Thread(() -> {
                            Platform.runLater(() -> in.loading.setVisible(true));
                            synchronized (selectedSeps) {
                                synchronized (in) {
                                    renderSaddleCon(new Point2D(a, b), selectedSeps.get(0),
                                            selectedSeps.get(1), true, transversal);

                                }
                            }
                            Platform.runLater(() -> in.loading.setVisible(false));
                        });
                        in.artist.setDaemon(true);
                        in.artist.start();

                    } catch (BadSaddleTransversalException ignored) {
                    }

                    selectedCritPoints.clear();
                    setClickMode(ClickModeType.DRAWPATH);
                } catch (RootNotFound ignored) {
                }
            }
            case FINDLIMCYCLE -> {
                switch (limCycStep) {
                    case 0:
                        cycleLine.setStartX(e.getX());
                        cycleLine.setStartY(e.getY());
                        fireUpdate(11);
                        limCycStep = 1;
                        break;
                    case 1:
                        cycleLine.setEndX(e.getX());
                        cycleLine.setEndY(e.getY());
                        cycleLine.setVisible(true);
                        fireUpdate(12);
                        limCycStep = 2;
                        break;
                    case 2:
                        limCycleArtist.interrupt();
                        limCycleArtist = new Thread(() -> {
                            Platform.runLater(() -> loading.setVisible(true));
                            drawNewLimCycle(pt,
                                    scrToNorm(new Point2D(cycleLine.getStartX(),
                                            cycleLine.getStartY())),
                                    scrToNorm(
                                            new Point2D(cycleLine.getEndX(), cycleLine.getEndY())),
                                    false);
                            Platform.runLater(() -> loading.setVisible(false));
                        });
                        limCycleArtist.setDaemon(true);
                        limCycleArtist.start();
                        limCycStep = 0;
                        setClickMode(ClickModeType.DRAWPATH);
                        break;
                }
            }
            case SEMISTABLE -> {
                switch (limCycStep) {
                    case 0:
                        try {
                            semiStableInner = critical(pt);
                            var loc = normToScr(semiStableInner.point);
                            cycleLine.setStartX(loc.getX());
                            cycleLine.setStartY(loc.getY());
                            limCycStep = 1;
                            fireUpdate(41);
                        } catch (RootNotFound r) {
                            setClickMode(ClickModeType.DRAWPATH);
                        }
                        break;
                    case 1:
                        cycleLine.setEndX(e.getX());
                        cycleLine.setEndY(e.getY());
                        semiStableOuter = pt;
                        cycleLine.setVisible(true);
                        in.artist = new Thread(() -> {
                            try {
                                Platform.runLater(() -> in.loading.setVisible(true));
                                synchronized (in) {
                                    renderSemiStable();
                                }
                                Platform.runLater(() -> in.loading.setVisible(false));
                            } finally {

                                cycleLine.setVisible(false);
                            }
                        });
                        in.artist.setDaemon(true);
                        in.artist.start();
                        limCycStep = 0;
                        setClickMode(ClickModeType.DRAWPATH);
                }
            }
        }

    }

    public void interrupt() {
        if (GlobalBifurcationDriver.parent != null) {
            GlobalBifurcationDriver.parent.interrupt();
        }
    }


    public void renderSemiStable() {
        var finder = new SemiStableFinder(this, semiStableInner, semiStableOuter);
        finder.run();
        in.renderedSemiStable.add(finder.render);
    }

    private LimCycleStart updateLimCycle(final LimCycleStart lc, double a, double b)
            throws RootNotFound {
        Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
        if (lc.isPositive) {
            eval.initialise(lc.st, 0, a, b, inc);
        } else {
            eval.initialise(lc.st, 0, a, b, -inc);
        }
        eval.advance(2);
        Point2D p1 = lc.st;
        Point2D p2 = eval.getNextIsectLn(lc.refLine[0], lc.refLine[1]);
        while (p2.distance(p1) > inc / 1000 && !Thread.interrupted()) {
            p1 = p2;
            p2 = eval.getNextIsectLn(lc.refLine[0], lc.refLine[1]);
        }
        return new LimCycleStart(p2, lc.isPositive, lc.refLine[0], lc.refLine[1]);
    }


    private void updateLimCycles() {
        limCycleUpdater.interrupt();
        limCycleUpdater = new Thread(() -> {
            Platform.runLater(() -> loading.setVisible(true));
            ArrayList<LimCycleStart> temp = new ArrayList<>();
            for (LimCycleStart lc : limCycles) {
                try {
                    temp.add(updateLimCycle(lc, a, b));
                    drawLimCycle(lc);
                    render();
                } catch (RootNotFound ignored) {
                }
            }
            limCycles = temp;
            if (!Thread.interrupted())
                render();
            Platform.runLater(() -> loading.setVisible(false));
        });
        limCycleUpdater.setDaemon(true);
        limCycleUpdater.start();
    }

    /**
     * draws a limit cycle based on a starting point and a direction
     *
     * @param lc the starting point and direction for the limit cycle
     */
    private void drawLimCycle(LimCycleStart lc) {
        if (lc.isPositive) {
            drawGraphBack(new InitCond(lc.st), false, '+', awtAttrLimCycleColor, 3);
        } else {
            drawGraphBack(new InitCond(lc.st), false, '-', awtRepLimCycleColor, 3);
        }
    }

    /**
     * finds a new limit cycle and draws it based on the provided information
     *
     * @param start the point to start looking at
     * @param lnSt  one end of the transversal
     * @param lnNd  the other end of the transversal
     * @param add   whether or not to add it to the list of limit cycles
     * @return whether or not a limit cycle was found
     */
    private boolean drawNewLimCycle(Point2D start, Point2D lnSt, Point2D lnNd, boolean add) {
        // get to the line
        Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
        eval.initialise(start, 0, a, b, inc);
        Point2D p1 = start;
        Point2D p2 = eval.next();
        Point2D isect = null;
        while (eval.getT() < 100) {
            try {
                isect = Intersections.getIntersection(p1, p2, lnSt, lnNd);
                break;
            } catch (RootNotFound r) {
                p1 = p2;
                p2 = eval.next();
            }
        }
        if (isect == null) {
            eval.initialise(start, 0, a, b, -inc);
            while (eval.getT() < 100) {
                try {
                    isect = Intersections.getIntersection(p1, p2, lnSt, lnNd);
                    break;
                } catch (RootNotFound r) {
                    p1 = p2;
                    p2 = eval.next();
                }
            }
        }
        if (isect == null) {
            cycleLine.setVisible(false);
            return false;
        }
        p1 = isect;
        eval.initialise(isect, 0, a, b, eval.getInc());
        try {
            p2 = eval.getNextIsectLn(lnSt, lnNd);
        } catch (RootNotFound r) {
            eval.initialise(isect, 0, a, b, -eval.getInc());
            try {
                p2 = eval.getNextIsectLn(lnSt, lnNd);
            } catch (RootNotFound r2) {
                cycleLine.setVisible(false);
                return false;
            }
        }
        boolean haveFlipped = false;
        try {
            while (inBounds(p2) && !Thread.interrupted()) {
                try {
                    p1 = p2;
                    p2 = eval.getNextIsectLn(lnSt, lnNd);
                    if (Math.abs(eval.getT()) < 3 * Math.abs(eval.getInc()))
                        p2 = eval.getNextIsectLn(lnSt, lnNd);
                    eval.resetT();
                    if (p1.distance(p2) < inc / 10000) {
                        if (eval.getInc() > 0)
                            drawGraphBack(new InitCond(p2), false, '+', awtAttrLimCycleColor, 4);
                        else
                            drawGraphBack(new InitCond(p2), false, '-', awtRepLimCycleColor, 4);
                        synchronized (canv) {
                            render();
                        }
                        cycleLine.setVisible(false);
                        if (add) {
                            limCycles.add(new LimCycleStart(p2, eval.getInc(), lnSt, lnNd));
                        }
                        return true;
                    }
                } catch (RootNotFound r) {
                    if (haveFlipped)
                        throw new RootNotFound();
                    else {
                        haveFlipped = true;
                        eval.initialise(isect, 0, a, b, -eval.getInc());
                    }
                }
            }
        } catch (RootNotFound ignored) {
            if (add) {
                cycleLine.setVisible(false);
            }
        }
        cycleLine.setVisible(false);
        return false;
    }


    /**
     * shades a divergence bifurcation
     */
    public void drawDivBif(boolean pos) {
        DivergenceFinder.init(this, pos);
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            new DivergenceFinder().start();
        }
    }

    /**
     * draws the basin for the point that is the critical point starting at st. does nothing if it
     * doesn't solve to a sink.
     *
     * @param st the starting point for finding the basin
     */
    public void drawBasin(Point2D st) {
        Evaluator tmp = EvaluatorFactory.getBestEvaluator(dx, dy);
        tmp.initialise(st, t, a, b, inc);
        var crit = tmp.advance(10000);
        var f = new BasinFinderDispatcher(this, crit, true);
        f.start();
    }

    /**
     * draws the cobasin for the point that is the critical point starting at st. does nothing if it
     * doesn't solve to a source.
     *
     * @param st the starting point for finding the cobasin
     */
    public void drawCoBasin(Point2D st) {
        Evaluator tmp = EvaluatorFactory.getBestEvaluator(dx, dy);
        tmp.initialise(st, t, a, b, -inc);
        var crit = tmp.advance(10000);
        var f = new BasinFinderDispatcher(this, crit, false);
        f.start();
    }


    public void renderSaddleCon(Point2D start, final SepStart s1, final SepStart s2, boolean add,
                                SaddleConTransversal transversal) {
        selectedSeps.clear();
        draw();
        var clone = transversal.clone();
        var finder = new SaddleConFinder(this, transversal, s1, s2);
        finder.run();
        if (finder.render != null) {
            in.addRendderedSaddleCon(finder.render, clone);
            in.draw();
        }
    }


    /**
     * whether or not the point (x,y) is in bounds for saddle connection purposes
     *
     * @param x the x coord
     * @param y the y coord
     * @return whether or not (x, y) is in bounds
     */
    private boolean inBoundsSaddle(double x, double y) {
        return x <= dSaddleXMax && x >= dSaddleXMin && y <= dSaddleYMax && y >= dSaddleYMin;
    }

    /**
     * whether or not the point p is in bounds for saddle connection purposes
     *
     * @param p the piont in question
     * @return whether or not it's in bounds
     */
    boolean inBoundsSaddle(Point2D p) {
        if (p != null)
            return inBoundsSaddle(p.getX(), p.getY());
        return false;
    }

    private Point2D getPointForHopf(Point2D start) throws RootNotFound {
        CriticalPoint temp = critical(start);
        if (temp.type != CritPointTypes.NODESOURCE && temp.type != CritPointTypes.SPIRALSOURCE
                && temp.type != CritPointTypes.NODESINK && temp.type != CritPointTypes.SPIRALSINK
                && temp.type != CritPointTypes.CENTER)
            throw new RootNotFound();
        else
            return temp.point;
    }

    private Point2D getSaddle(Point2D start) throws RootNotFound {
        CriticalPoint temp = critical(start);
        if (temp.type != CritPointTypes.SADDLE)
            throw new RootNotFound();
        else
            return temp.point;
    }

    private CriticalPoint critical(Point2D start) throws RootNotFound {
        return EvaluatorFactory.getBestEvaluator(dx, dy).findCritical(start, a, b, t);
    }

    CriticalPoint critical(Point2D start, double a, double b) throws RootNotFound {
        return EvaluatorFactory.getBestEvaluator(dx, dy).findCritical(start, a, b, 0);
    }

    CriticalPoint critical(Point2D start, Point2D p) throws RootNotFound {
        return critical(start, p.getX(), p.getY());
    }

    private void updateCritical() {
        List<CriticalPoint> temp = new ArrayList<>();
        for (CriticalPoint c : criticalPoints) {
            if (Thread.interrupted())
                return;
            try {
                temp.add(critical(c.point));
            } catch (RootNotFound ignored) {
            }
        }
        criticalPoints = temp;
        List<Point2D> temp1 = new ArrayList<>();
        for (Point2D c : selectedCritPoints) {
            if (Thread.interrupted())
                return;
            try {
                temp1.add(critical(c).point);
            } catch (RootNotFound ignored) {
            }
        }
        selectedCritPoints = temp1;
    }

    private void labelCritical(CriticalPoint p) {
        double r2 = 1;// p.point.getX() * p.point.getX() + p.point.getY() * p.point.getY();
        p.point = p.point.multiply(1 / r2);
        if (inBounds(p.point)) {
            // c.getGraphicsContext2D().setFill(criticalColor);
            // c.getGraphicsContext2D().fillOval(normToScrX(p.point.getX()) - 2.5,
            // normToScrY(p.point.getY()) - 2.5, 5, 5);
            synchronized (g) {
                g.setColor(awtCriticalColor);
                g.fillOval(imgNormToScrX(p.point.getX()) - 5, imgNormToScrY(p.point.getY()) - 5, 10,
                        10);
            }
            // Label text = new Label(p.type.getStringRep());
            // text.setPadding(new Insets(2));
            // text.setBorder(new Border(new BorderStroke(criticalColor, BorderStrokeStyle.SOLID,
            // null, new BorderWidths(1))));
            // this.getChildren().add(text);
            double x, y;
            x = normToScrX(p.point.getX()) + 8;
            y = normToScrY(p.point.getY()) - 10;
            // text.setLayoutX(normToScrX(p.point.getX()) + 8);
            // text.setLayoutY(normToScrY(p.point.getY()) - 24);
            final Text t = new Text(p.type.getStringRep());

            // if (text.getLayoutY() < 0)
            // {
            // text.setLayoutY(normToScrY(p.point.getY()) + 4);
            // }
            if (y < 0)
                y = normToScrY(p.point.getY()) + 18;
            // if (text.getLayoutX() + t.getLayoutBounds().getWidth() + 4 > this.getWidth())
            // {
            // text.setLayoutX(normToScrX(p.point.getX()) - 12 - t.getLayoutBounds().getWidth());
            // }
            if (x + t.getLayoutBounds().getWidth() + 4 > this.getWidth())
                x = normToScrX(p.point.getX()) - 8 - t.getLayoutBounds().getWidth();
            // text.setTextFill(criticalColor);

            // text.setVisible(true);
            synchronized (labelCanv) {
                GraphicsContext gc = labelCanv.getGraphicsContext2D();
                gc.setStroke(criticalColor);
                gc.setLineWidth(.5);
                gc.setFont(new javafx.scene.text.Font(10));
                gc.strokeText(t.getText(), x, y);
            }
            // synchronized (needsReset)
            // {
            // needsReset.add(text);
            // }
        }
    }

    private void drawGraphs() {
        for (InitCond i : initials) {
            if (Thread.interrupted())
                return;
            drawGraph(i, true, awtSolutionColor);
        }

    }

    void drawGraph(InitCond init, boolean arrow, java.awt.Color color) {
        System.out.println(Thread.currentThread());
        drawGraphBack(init, arrow, '1', color);
    }

    private boolean ptIsNan(Point2D pt) {
        return Double.isNaN(pt.getX()) || Double.isNaN(pt.getY());
    }

    private void drawGraphBack(InitCond init, boolean arrow, char dir, java.awt.Color color,
                               float width) {
        double x, y;
        x = init.x;
        y = init.y;
        t = init.t;
        Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
        Point2D initialDir =
                EvaluatorFactory.getEvaluator(EvalType.Euler, dx, dy).evaluate(x, y, a, b, t, inc);
        if (arrow)
            drawArrow(x, y, initialDir.getX(), initialDir.getY(), color);
        Point2D prev;
        Point2D next;
        eval.initialise(x, y, t, a, b, inc);
        prev = new Point2D(x, y);
        if (dir != '-')
            for (int i = 0; i < 1000; ++i) {
                if (Thread.interrupted()) {
                    return;
                }
                next = eval.next();
                if (ptIsNan(next))
                    break;
                if (inBounds(prev) || inBounds(next))
                    drawLine(prev, next, color, width);
                prev = next;
            }
        eval.initialise(x, y, t, a, b, -inc);
        prev = new Point2D(x, y);
        if (dir != '+')
            for (int i = 0; i < 1000; ++i) {
                if (Thread.interrupted()) {
                    return;
                }
                next = eval.next();
                if (ptIsNan(next))
                    break;
                if (inBounds(prev) || inBounds(next))
                    drawLine(prev, next, color, width);
                prev = next;
            }

    }

    private void drawGraphBack(InitCond init, boolean arrow, char dir, java.awt.Color color) {
        drawGraphBack(init, arrow, dir, color, 1);
    }

    private void drawIsoclines() {
        for (InitCond c : isoclines) {
            drawIso(c);
        }
        for (Point2D pt : horizIsos) {
            drawHorizIso(pt);
        }
        for (Point2D pt : vertIsos) {
            drawVertIso(pt);
        }
    }

    private void drawIso(InitCond init) {
        try {
            double val = dy.eval(init.x, init.y, a, b, t) / dx.eval(init.x, init.y, a, b, t);
            AST.Node slope = Maths.minus(Maths.divide(dy, dx), new Value(val));
            drawIsoHelper(slope, new Point2D(init.x, init.y), awtIsoclineColor);


        } catch (EvaluationException ignored) {
        }
    }

    private void drawHorizIso(Point2D pt) {
        try {
            AST.Node thing = Maths.divide(dy, dy.differentiate('y')).collapse();
            double yOld = pt.getY();
            double y = pt.getY();
            double x = pt.getX();
            for (int i = 0; i < 10; i++) {
                yOld = y;
                y = y - thing.eval(x, y, a, b, t);
            }
            if (Math.abs(y - yOld) < .000001) {
                drawIsoHelper(dy, new Point2D(x, y), awtHorizIsoColor);
            } else {
                thing = Maths.divide(dy, dy.differentiate('x')).collapse();
                double xOld = pt.getX();
                y = pt.getY();
                x = pt.getX();
                for (int i = 0; i < 10; i++) {
                    xOld = x;
                    x = x - thing.eval(x, y, a, b, t);

                }
                if (Math.abs(x - xOld) < .000001) {
                    drawIsoHelper(dy, new Point2D(x, y), awtHorizIsoColor);
                }
            }
        } catch (EvaluationException ignored) {
        }
    }

    private void drawVertIso(Point2D pt) {
        try {
            AST.Node thing = Maths.divide(dx, dx.differentiate('x')).collapse();
            double xOld = pt.getX();
            double y = pt.getY();
            double x = pt.getX();
            for (int i = 0; i < 10; i++) {
                xOld = x;
                x = x - thing.eval(x, y, a, b, t);

            }
            if (Math.abs(x - xOld) < .000001) {
                drawIsoHelper(dx, new Point2D(x, y), awtVertIsoColor);
            } else {
                thing = Maths.divide(dx, dx.differentiate('y')).collapse();
                double yOld = pt.getY();
                y = pt.getY();
                x = pt.getX();
                for (int i = 0; i < 10; i++) {
                    yOld = y;
                    y = y - thing.eval(x, y, a, b, t);
                }
                if (Math.abs(y - yOld) < .000001) {
                    drawIsoHelper(dx, new Point2D(x, y), awtVertIsoColor);
                }
            }
        } catch (EvaluationException ignored) {
        }
    }

    private void drawIsoHelper(AST.Node slope, Point2D init, java.awt.Color color) {
        try {
            boolean firstTime = true;
            boolean isX = true;
            Point2D first, second;
            first = init;
            AST.Node slopeDeriv = slope.differentiate('y');
            AST.Node thing = Maths.divide(slope, slopeDeriv);
            double sign = 1;
            double tol = 30.;
            long time = System.nanoTime();
            double xinc = 1.5 * (xMax.get() - xMin.get()) / canv.getWidth();
            double yinc = 1.5 * (yMax.get() - yMin.get()) / canv.getHeight();
            for (int j = 0; j < 2; j++) {
                while (inBounds(first) && System.nanoTime() - time < 100000000) {
                    if (isX) {
                        double x = first.getX() + xinc;
                        double yOld = first.getY();
                        double y = first.getY();
                        for (int i = 0; i < 10; i++) {
                            yOld = y;
                            y = y - thing.eval(x, y, a, b, t);
                        }
                        if (Math.abs(y - yOld) < .00001
                                && ((Math.abs((y - first.getY()) / (x - first.getX())) < tol)
                                || firstTime)) {
                            firstTime = false;
                            if (Math.abs((y - first.getY()) / (x - first.getX())) > 1) {
                                isX = false;
                                slopeDeriv = slope.differentiate('x');
                                thing = Maths.divide(slope, slopeDeriv);
                                sign = Math.signum((y - first.getY()) / (x - first.getX()));
                            }
                            second = new Point2D(x, y);
                            drawLine(first, second, color);
                            first = second;
                        } else
                            break;
                    } else {
                        if (sign == 0.0)
                            break;
                        double y = first.getY() + (yinc * sign);
                        double xOld = first.getX();
                        double x = first.getX();
                        for (int i = 0; i < 10; i++) {
                            xOld = x;
                            x = x - thing.eval(x, y, a, b, t);
                        }
                        if (Math.abs(x - xOld) < .00001
                                && (Math.abs((y - first.getY()) / (x - first.getX())) > 1 / tol)) {
                            if (Math.abs((y - first.getY()) / (x - first.getX())) < 1) {
                                isX = true;
                                slopeDeriv = slope.differentiate('y');
                                thing = Maths.divide(slope, slopeDeriv);
                            }
                            second = new Point2D(x, y);
                            drawLine(first, second, color);
                            first = second;
                        } else
                            break;
                    }
                }
                xinc = -xinc;
                yinc = -yinc;
                first = init;
            }
        } catch (EvaluationException ignored) {
        }
    }


    private void drawArrow(double x, double y, double dx, double dy, java.awt.Color color) {
        double angle = (Math.atan(-dy / dx));
        if (dx < 0)
            angle += Math.PI;
        // double xScr = normToScrX(x);
        // double yScr = normToScrY(y);
        // gc.save();
        // gc.transform(new Affine(new Rotate(angle, xScr, yScr)));
        // gc.strokeLine(xScr, yScr, xScr - 5, yScr + 3);
        // gc.strokeLine(xScr, yScr, xScr - 5, yScr - 3);
        // gc.restore();
        int xScr = imgNormToScrX(x);
        int yScr = imgNormToScrY(y);
        double finalAngle = angle;
        // Platform.runLater(() ->
        {
            synchronized (g) {
                g.setStroke(new BasicStroke(1));
                g.setColor(color);
                AffineTransform saveAt = g.getTransform();
                g.rotate(finalAngle, xScr, yScr);
                g.drawLine(xScr, yScr, xScr - 10, yScr + 6);
                g.drawLine(xScr, yScr, xScr - 10, yScr - 6);
                g.setTransform(saveAt);
            }
        } // );

    }

    public void drawSeparatrices() {
        drawSep = true;
        for (CriticalPoint c : criticalPoints)
            drawSep(c);
        render();
    }

    @Override
    public void clear() {
        initials.clear();
        criticalPoints.clear();
        selectedSeps.clear();
        selectedCritPoints.clear();
        isoclines.clear();
        vertIsos.clear();
        horizIsos.clear();
        limCycles.clear();
        synchronized (labelCanv) {
            labelCanv.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        }
        draw();
    }

    private void drawSep(CriticalPoint c) {
        double tol = 2 * (xMax.get() - xMin.get()) / this.getWidth();
        try {
            if (c.type == CritPointTypes.SADDLE) {
                boolean temp;
                char sn;
                InitCond point1 =
                        new InitCond(c.point.getX() + tol * c.matrix.getEigenVector(0).get(0),
                                c.point.getY() + tol * c.matrix.getEigenVector(0).get(1), t);
                InitCond point3 =
                        new InitCond(c.point.getX() - tol * c.matrix.getEigenVector(0).get(0),
                                c.point.getY() - tol * c.matrix.getEigenVector(0).get(1), t);
                Point2D initl = new Point2D(point1.x, point1.y);
                // temp = tester.next().subtract(initl).angle(initl) < .1;
                temp = c.matrix.getEigenvalue(0).getReal() > 0;
                java.awt.Color tempCol;
                if (!temp) {
                    tempCol = (awtStblSeparatrixColor);
                    sn = '-';
                } else {
                    tempCol = (awtUnstblSeparatrixColor);
                    sn = '+';
                }
                drawGraphBack(point1, false, sn, tempCol);
                drawGraphBack(point3, false, sn, tempCol);

                if (temp) {
                    tempCol = (awtStblSeparatrixColor);
                    sn = '-';
                } else {
                    tempCol = (awtUnstblSeparatrixColor);
                    sn = '+';
                }
                InitCond point2 =
                        new InitCond(c.point.getX() + tol * c.matrix.getEigenVector(1).get(0),
                                c.point.getY() + tol * c.matrix.getEigenVector(1).get(1), t);
                InitCond point4 =
                        new InitCond(c.point.getX() - tol * c.matrix.getEigenVector(1).get(0),
                                c.point.getY() - tol * c.matrix.getEigenVector(1).get(1), t);
                drawGraphBack(point2, false, sn, tempCol);
                drawGraphBack(point4, false, sn, tempCol);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void drawSelectedCritPoints() {
        for (Point2D p : selectedCritPoints) {
            g.drawOval(imgNormToScrX(p.getX()) - 8, imgNormToScrY(p.getY()) - 8, 16, 16);
        }
    }

    @Override
    public synchronized void draw() {

        solutionArtist.interrupt();
        solutionArtist = new Thread(() -> {
            super.draw();
            updateCritical();
            synchronized (labelCanv) {
                labelCanv.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
            }
            for (CriticalPoint p : criticalPoints) {
                if (Thread.interrupted())
                    return;
                // Platform.runLater(() -> labelCritical(p));
                labelCritical(p);
            }

            drawGraphs();
            drawIsoclines();
            if (drawSep) {
                for (CriticalPoint c : criticalPoints) {
                    if (Thread.interrupted())
                        return;
                    drawSep(c);
                }

            }
            drawSelectedCritPoints();
            double inc = 2 * (xMax.get() - xMin.get()) / this.getWidth();
            for (SepStart s : selectedSeps) {
                if (Thread.interrupted())
                    return;
                synchronized (g) {
                    if (!s.posEig()) {
                        drawGraphBack(
                                new InitCond(s.getStart(inc).getX(), s.getStart(inc).getY(), 0),
                                false, '-', awtStblSeparatrixColor, 2);

                    } else {
                        drawGraphBack(
                                new InitCond(s.getStart(inc).getX(), s.getStart(inc).getY(), 0),
                                false, '+', awtUnstblSeparatrixColor, 2);
                    }
                }
            }
            if (!Thread.interrupted())
                render();
            // updateLimCycles();
        });
        solutionArtist.setDaemon(true);
        solutionArtist.start();

    }

    @Override
    public boolean writePNG(File f) {
        BufferedImage temp = new BufferedImage(canv.getWidth(), canv.getHeight(), canv.getType());
        Graphics2D g2 = temp.createGraphics();
        g2.drawImage(canv, 0, 0, null);
        if (settings.writeCriticalText) {
            g2.setColor(awtCriticalColor);
            for (CriticalPoint p : this.criticalPoints) {
                if (inBounds(p.point)) {
                    int x, y;
                    x = imgNormToScrX(p.point.getX()) + 8;
                    y = imgNormToScrY(p.point.getY()) - 12;
                    g2.setFont(g2.getFont().deriveFont(12F));
                    int w = g2.getFontMetrics().stringWidth(p.type.getStringRep());

                    if (y <= 0) {
                        y = imgNormToScrY(p.point.getY()) + 16;
                    }
                    if (x + w + 4 > temp.getWidth()) {
                        x = imgNormToScrX(p.point.getX()) - 12 - w;
                    }
                    g2.drawString(p.type.getStringRep(), x, y);
                }
            }
            g2.setColor(java.awt.Color.BLACK);
        }
        if (settings.drawAxes) {
            int x0 = imgNormToScrX(0);
            int y0 = imgNormToScrY(0);
            g2.drawLine(x0, 0, x0, temp.getHeight());
            g2.drawLine(0, y0, temp.getWidth(), y0);
        }
        try {
            ImageIO.write(temp, "png", f);
            return true;
        } catch (IOException | NullPointerException oof) {
            return false;
        }
    }

    private static Array initCondToLwon(List<InitCond> conds) {
        var builder = new Array.Builder();
        for (int i = 0; i < conds.size(); ++i) {
            builder.set(new int[]{i}, conds.get(i).toLwon());
        }
        return builder.build(null);
    }

    private static List<InitCond> initCondFromLwon(Array arr) {
        var lst = new ArrayList<InitCond>();
        for (var elem : arr) {
            if (elem instanceof Dictionary d)
                lst.add(new InitCond(d));
            else break;
        }
        return lst;
    }

    public Dictionary toLwon() {
        var builder = new Dictionary.Builder();
        builder.put("initials", initCondToLwon(initials));
        builder.put("isoclines", initCondToLwon(isoclines));
        builder.put("horizIsos", RenderedCurve.arrayOfPoints(horizIsos));
        builder.put("vertIsos", RenderedCurve.arrayOfPoints(vertIsos));
        var critPoints = criticalPoints.stream().map(pt -> pt.point).toList();
        builder.put("critPoints", RenderedCurve.arrayOfPoints(critPoints));
        return builder.build(null);
    }

    public void fromLwon(Dictionary lwon) {
        clear();
        initials = initCondFromLwon((Array) lwon.get("initials").get(0));
        isoclines = initCondFromLwon((Array) lwon.get("isoclines").get(0));
        horizIsos = RenderedCurve.pointsOfArray((Array) lwon.get("horizIsos").get(0));
        vertIsos = RenderedCurve.pointsOfArray((Array) lwon.get("vertIsos").get(0));
        var critPoints = RenderedCurve.pointsOfArray((Array) lwon.get("critPoints").get(0));
        for (Point2D pt : critPoints) {
            addCritical(pt);
        }
        draw();
    }

}
