package FXObjects;

import AST.Derivative;
import AST.Maths;
import AST.Value;
import Evaluation.*;
import Events.SaddleSelected;
import Events.SourceOrSinkSelected;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.ejml.simple.SimpleMatrix;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class OutputPlane extends CoordPlane
{
	/**
	 * Bounds for calculating saddle connections
	 */
	public double dSaddleXMin, dSaddleXMax, dSaddleYMin, dSaddleYMax;

	private Line cycleLine;
	int limCycStep = 0;

	private double t = 0;
	private final List<InitCond> initials;
	private final List<InitCond> isoclines;
	private List<CriticalPoint> criticalPoints;
	private List<Point2D> selectedCritPoints;
	private final List<Point2D> horizIsos;
	private final List<Point2D> vertIsos;
	private final List<Node> needsReset;
	private final List<sepStart> selectedSeps;
	private List<LimCycleStart> limCycles;
	double inc = .01;
	volatile double a, b;
	private Derivative dx, dy;
	public EvalType evalType;
	public ClickModeType clickMode = ClickModeType.DRAWPATH;
	private boolean drawSep = false;
	private CriticalPoint currentPoint = null;

	private Color solutionColor = Color.BLACK;
	private Color isoclineColor = Color.BLUE;
	private Color horizIsoColor = Color.PURPLE;
	private Color vertIsoColor = Color.ORANGE;
	private Color stblSeparatrixColor = Color.ORANGERED;
	private Color unstblSeparatrixColor = Color.DARKCYAN;
	private Color criticalColor = Color.RED;
	private Color attrLimCycleColor = Color.GREEN;
	private Color repLimCycleColor = Color.MAGENTA;

	private java.awt.Color awtSolutionColor = fromFXColor(solutionColor);
	private java.awt.Color awtIsoclineColor = fromFXColor(isoclineColor);
	private java.awt.Color awtHorizIsoColor = fromFXColor(horizIsoColor);
	private java.awt.Color awtVertIsoColor = fromFXColor(vertIsoColor);
	private java.awt.Color awtStblSeparatrixColor = fromFXColor(stblSeparatrixColor);
	private java.awt.Color awtUnstblSeparatrixColor = fromFXColor(unstblSeparatrixColor);
	private java.awt.Color awtCriticalColor = fromFXColor(criticalColor);
	private java.awt.Color awtAttrLimCycleColor = fromFXColor(attrLimCycleColor);
	private java.awt.Color awtRepLimCycleColor = fromFXColor(repLimCycleColor);

	public InputPlane in;

	private Thread limCycleArtist = new Thread(), limCycleUpdater = new Thread();

	public OutputPlane(double side, TextField tField)
	{
		super(side);

		cycleLine = new Line();
		this.getChildren().add(cycleLine);
		cycleLine.setVisible(false);

		dSaddleXMax = this.xMax.get();
		dSaddleXMin = this.xMin.get();
		dSaddleYMax = this.yMax.get();
		dSaddleYMin = this.yMin.get();

		evalType = EvalType.RungeKutta;
		initials = new ArrayList<>();
		criticalPoints = new ArrayList<>();
		isoclines = new ArrayList<>();
		limCycles = new ArrayList<>();
		needsReset = new ArrayList<>();
		horizIsos = new ArrayList<>();
		vertIsos = new ArrayList<>();
		selectedCritPoints = new ArrayList<>();
		selectedSeps = new ArrayList<>(2);
		draw();
//		render();
		tField.setText(Double.toString(t));

		tField.setOnKeyPressed((e) ->
		{
			if (e.getCode() == KeyCode.ENTER)
			{
				try
				{
					t = Double.parseDouble(tField.getText());
					drawAxes();
				} catch (NumberFormatException n)
				{
					tField.setText(Double.toString(t));
				}
			}
		});
		setOnKeyPressed((e) ->
		{
			if (e.getCode() == left)
			{
				xMin.set(xMin.get() - (xMax.get() - xMin.get()) / 20);
				xMax.set(xMax.get() - (xMax.get() - xMin.get()) / 20);
			} else if (e.getCode() == right)
			{
				xMin.set(xMin.get() + (xMax.get() - xMin.get()) / 20);
				xMax.set(xMax.get() + (xMax.get() - xMin.get()) / 20);
			} else if (e.getCode() == up)
			{
				yMin.set(yMin.get() + (yMax.get() - yMin.get()) / 20);
				yMax.set(yMax.get() + (yMax.get() - yMin.get()) / 20);
			} else if (e.getCode() == down)
			{
				yMin.set(yMin.get() - (yMax.get() - yMin.get()) / 20);
				yMax.set(yMax.get() - (yMax.get() - yMin.get()) / 20);
			}
			KeyCode temp = e.getCode();
			if (temp == left || temp == right || temp == up || temp == down)
			{
				draw();
				e.consume();
			}
		});
		loading.toFront();

	}



	public Derivative getDx()
	{
		return (Derivative) dx.clone();
	}

	public Derivative getDy()
	{
		return (Derivative) dy.clone();
	}

	public double getT()
	{
		return t;
	}
	@Override
	protected void updateForZoom()
	{
		inc = ((xMax.get() - xMin.get())/1024 + (yMax.get() - yMin.get())/1024)/2;
	}

	public void clearObjects()
	{
		selectedCritPoints.clear();
		criticalPoints.clear();
		selectedSeps.clear();
		for (Node n : needsReset) n.setVisible(false);
		needsReset.clear();
	}

	public void updateA(double a)
	{
		this.a = a;
		draw();
	}

	public void updateB(double b)
	{
		this.b = b;
		draw();
	}

	public void updateDX(Derivative temp)
	{
		dx = temp;
	}

	public void updateDY(Derivative temp)
	{
		dy = temp;
	}

	@Override
	public void handleMouseClick(MouseEvent e)
	{
		double x = scrToNormX(e.getX());
		double y = scrToNormY(e.getY());
		InitCond temp = new InitCond(x, y, t);
		Point2D pt = new Point2D(x, y);
		switch (clickMode)
		{

			case DRAWPATH:
				initials.add(temp);
				drawGraph(temp, true, awtSolutionColor);
				render();
				break;
			case FINDCRITICAL:
				try
				{
					CriticalPoint root = EvaluatorFactory.getBestEvaluator(dx, dy).findCritical(pt, a, b, t);
					criticalPoints.add(root);
					labelCritical(root);
//					drawGraphs();
					render();
				} catch (RootNotFound r)
				{
					//TODO better output system
					System.out.println("Root not found");
				}
				break;
			case DRAWHORIZISO:
				drawHorizIso(pt);
				horizIsos.add(pt);
				clickMode = ClickModeType.DRAWPATH;
				render();
				break;
			case DRAWVERTISO:
				drawVertIso(new Point2D(x, y));
				vertIsos.add(pt);
				clickMode = ClickModeType.DRAWPATH;
				render();
				break;
			case DRAWISO:
				isoclines.add(temp);
				drawIso(temp);
				render();
				break;
			case DRAWBASIN:
				drawBasin(pt);
				clickMode = ClickModeType.DRAWPATH;
				break;
			case SELECTSADDLE:
				try
				{
					Point2D p = getSaddle(pt);
					fireEvent(new SaddleSelected(p));
					selectedCritPoints.add(p);
					drawSelectedCritPoints();
					render();
				} catch (RootNotFound ignored)
				{
				}
				break;
			case SELECTSOURCEORSINK:
				try
				{
					Point2D p = getSourceOrSink(pt);
					fireEvent(new SourceOrSinkSelected(p));
					selectedCritPoints.add(p);
					drawSelectedCritPoints();
					render();
				} catch (RootNotFound ignored)
				{
				}
				break;
			case SELECTSEP:
				try
				{
					CriticalPoint p = critical(pt);
					if (selectedSeps.size() < 2)
					{
						double tol = (xMax.get() - xMin.get())/c.getWidth();
						Point2D p1 = new Point2D(p.point.getX() + tol * p.matrix.getEigenVector(0).get(0),
								p.point.getY() + tol * p.matrix.getEigenVector(0).get(1));
						Point2D p2 = new Point2D(p.point.getX() - tol * p.matrix.getEigenVector(0).get(0),
								p.point.getY() - tol * p.matrix.getEigenVector(0).get(1));

						Point2D p3 = new Point2D(p.point.getX() + tol * p.matrix.getEigenVector(1).get(0),
								p.point.getY() + tol * p.matrix.getEigenVector(1).get(1));
						Point2D p4 = new Point2D(p.point.getX() - tol * p.matrix.getEigenVector(1).get(0),
								p.point.getY() - tol * p.matrix.getEigenVector(1).get(1));
						double d1 = pt.distance(p1);
						double d2 = pt.distance(p2);
						double d3 = pt.distance(p3);
						double d4 = pt.distance(p4);
						double min = Math.min(Math.min(d1, d2), Math.min(d3, d4));
						boolean firstPos = p.matrix.getEigenvalue(0).getReal() > 0;
						if (d1 == min)
							selectedSeps.add(new sepStart(p, true, firstPos));
						else if (d2 == min)
							selectedSeps.add(new sepStart(p, false, firstPos));
						else if (d3 == min)
							selectedSeps.add(new sepStart(p, true, !firstPos));
						else
							selectedSeps.add(new sepStart(p, false, !firstPos));
						draw();
						if (selectedSeps.size() == 2)
						{
							//
							try
							{
								PrintWriter f = new PrintWriter(new File("output.text"));
								synchronized (selectedSeps)
								{
									for (sepStart s :selectedSeps)
									{
										f.println("Sepstart: ");
										f.println("is positive direction: " + s.posDir());
										f.println("Saddle pt: " + s.saddle.point);
										f.println("Start pt: " + s.getStart(.01));
									}
								}
								f.close();
							} catch (FileNotFoundException ignored) {}


							in.artist = new Thread(() ->
							{
								Platform.runLater(() -> in.loading.setVisible(true));
								synchronized (selectedSeps)
								{
									synchronized (in)
									{
										renderSaddleCon(new Point2D(a, b), selectedSeps.get(0), selectedSeps.get(1), true);
										selectedSeps.clear();
									}
								}
								Platform.runLater(() -> in.loading.setVisible(false));
							});
							in.artist.start();

							selectedCritPoints.clear();
							clickMode = ClickModeType.DRAWPATH;
						}
					}
				} catch (RootNotFound ignored)
				{
				}
				break;
			case FINDLIMCYCLE:
				switch (limCycStep)
				{
					case 0:
						cycleLine.setStartX(e.getX());
						cycleLine.setStartY(e.getY());
						limCycStep = 1;
						break;
					case 1:
						cycleLine.setEndX(e.getX());
						cycleLine.setEndY(e.getY());
						cycleLine.setVisible(true);
						limCycStep = 2;
						break;
					case 2:
						limCycleArtist.interrupt();
						limCycleArtist = new Thread(() ->
						{
							Platform.runLater(() -> loading.setVisible(true));
								drawNewLimCycle(pt,
										scrToNorm(new Point2D(cycleLine.getStartX(), cycleLine.getStartY())),
										scrToNorm(new Point2D(cycleLine.getEndX(), cycleLine.getEndY())), true);
							Platform.runLater(() -> loading.setVisible(false));
						});

						limCycleArtist.start();
						limCycStep = 0;
						clickMode = ClickModeType.DRAWPATH;
						break;
				}
				break;
			case SEMISTABLE:
				switch (limCycStep)
				{
					case 0:
						cycleLine.setStartX(e.getX());
						cycleLine.setStartY(e.getY());
						limCycStep = 1;
						break;
					case 1:
						cycleLine.setEndX(e.getX());
						cycleLine.setEndY(e.getY());
						cycleLine.setVisible(true);
						in.artist = new Thread(() ->
						{
							try
							{
								Platform.runLater(() -> in.loading.setVisible(true));
								synchronized (in)
								{
									renderSemiStable(
											scrToNorm(new Point2D(cycleLine.getStartX(), cycleLine.getStartY())),
											scrToNorm(new Point2D(cycleLine.getEndX(), cycleLine.getEndY())),
											new Point2D(a, b), true);
								}
								Platform.runLater(() -> in.loading.setVisible(false));
							} finally
							{

								cycleLine.setVisible(false);
							}
						});
						in.artist.start();
						limCycStep = 0;
						clickMode = ClickModeType.DRAWPATH;
				}
		}

	}


	public void renderSemiStable(Point2D lnSt, Point2D lnNd, Point2D start, boolean add)
	{
		double aInc = 5D * (in.xMax.get() - in.xMin.get()) / in.getWidth();
		double bInc = 5D * (in.yMax.get() - in.yMin.get()) / in.getHeight();
		double otherInc = 0D;
		Point2D prev;
		Point2D next;
		Point2D temp;
		Point2D st = null;
		Point2D diff;
		int throwCount = 0;
		int code = 0;
		for(int i = 0; i < 4; i++)
		{
			try
			{
				st = semiStable(lnSt, lnNd, start.getX(), start.getY(), i);
				code = i;
				break;
			} catch (RootNotFound ignored) {}
		}
		if(st == null)
			return;
		System.out.println("yay");
		System.out.println(st);
		boolean isA;
		if(add)
		{
			in.semiStables.add(new SemiStableStart(lnSt, lnNd, st));
		}
		for (int i = 0; i < 3; i++)
		{
			prev = st;
			while (in.inBounds(prev.getX(), prev.getY()) && !Thread.interrupted())
			{
				isA = (code & 1) == 1;
				if (isA) temp = new Point2D(prev.getX() + otherInc, prev.getY() + bInc);
				else temp = new Point2D(prev.getX() + aInc, prev.getY() + otherInc);
				try
				{
					System.out.println("i: " + i);
					next = semiStable(lnSt, lnNd, temp.getX(), temp.getY(), code);
					in.drawLine(prev, next, in.awtSemiStableColor);
					Platform.runLater(in::render);
					diff = next.subtract(prev);

					if(isA)
					{
						if(Math.abs(diff.getY()/diff.getX()) < 1D)
							isA = false;
					} else
					{
						if(Math.abs(diff.getY()/diff.getX()) > 1D)
							isA = true;
					}
					if(isA)
					{
						otherInc = bInc * diff.getX()/diff.getY();
					} else
					{
						otherInc = aInc * diff.getY()/diff.getX();
					}

					prev = next;
					System.out.println(prev);
					System.out.println(isA);
					throwCount = 0;
				} catch (RootNotFound r)
				{

					if (prev.distance(st) < Math.min(aInc, bInc)) break;
					if (throwCount >= 4)
					{
						System.out.println("breaking");
						break;
					} else
					{
						code = (code + 1) % 4;
						throwCount++;
					}
				}
			}
			aInc = -aInc;
			bInc = -bInc;
			code = (code + 2) % 4;
		}
		in.gc.setStroke(Color.BLACK);

		Platform.runLater(this::draw);
		in.render();
	}

	/**
	 * Finds a semistable limit cycle bifurcation with the provided start info
	 * @param lnSt
	 * @param lnNd
	 * @param a
	 * @param b
	 * @param code least significant bit is isA, 2nd least is lookPos
	 * @return
	 * @throws RootNotFound
	 */
	private Point2D semiStable(Point2D lnSt, Point2D lnNd, double a, double b, int code) throws RootNotFound
	{
		boolean isA, lookPos;
		isA = (code & 1) == 1;
		lookPos = (code & 2) == 2;
		double finA = Double.MAX_VALUE, finB = Double.MAX_VALUE;
		if(!lookPos)
		{
			finA = -finA;
			finB = -finB;
		}
		double tol, incA, incB;
		if(isA)
		{
			tol = (in.xMax.get() - in.xMin.get())/2048D;
			if(lookPos)
				incA = tol * 16D;
			else incA = - tol * 16D;
			incB = 0D;
			finA = a + 8 * incA;

		} else
		{
			tol = (in.yMax.get() - in.yMin.get())/2048D;
			incA = 0D;
			if(lookPos)
				incB = tol * 16D;
			else incB = - tol * 16D;
			finB = b + 8 * incB;
		}
		int current = hasLimCycle(lnSt, lnNd, a, b);
		if(current != 0 && current != 2)
		{
			System.out.println("bad initial state");
			throw new RootNotFound();
		}
		while(Math.max(Math.abs(incA), Math.abs(incB)) > tol && !Thread.interrupted())
		{
			System.out.println("a: " + a + "\nb: " + b);
			int cd = hasLimCycle(lnSt, lnNd, a + incA, b + incB);
			if(cd == current)
			{
				a += incA;
				b += incB;
			} else if (cd != 0 && cd != 1) throw new RootNotFound(); else
			{
				incA /= 2D;
				incB /= 2D;
			}
			if(lookPos && (a > finA || b > finB) || !lookPos && (a < finA || b < finB))
			{
				System.out.println("went out of bounds");
				throw new RootNotFound();
			}
		}
		if(current == 0)
		{
			a += incA;
			b += incB;
		}
		if(hasLimCycle(lnSt, lnNd, a, b) == 0)
		{
			System.out.println("what the actual fuck");
			throw new RootNotFound();
		}
		Point2D p1 = null, p2 = null;
		Evaluator e1, e2;
		e1 = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		e2 = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		e1.initialise(lnSt, 0, a, b, -inc);
		e2.initialise(lnNd, 0, a, b, inc);
		e1.next();
		e2.next();
		try
		{
			getNextIsectLn(e1, lnSt, lnNd);
		} catch (RootNotFound r)
		{
			System.out.println("negating");
			e1.negate();
			e2.negate();
			e1.advance(4);
			e2.advance(4);
		}
		try
		{
			for (int i = 0; i < 20; i++)
			{
				p1 = getNextIsectLn(e1, lnSt, lnNd);
				p2 = getNextIsectLn(e2, lnSt, lnNd);
			}
		} catch (RootNotFound r)
		{
			System.out.println("problem.");
			throw new RootNotFound();
		}
//		if(p1.distance(p2) > .2 * lnSt.distance(lnNd))
//		{
//			System.out.println("wrong end");
//			throw new RootNotFound();
//		}
		System.out.println("returning");
		return new Point2D(a, b);
	}

	private int hasLimCycle(Point2D lnSt, Point2D lnNd, double a, double b)
	{
		Evaluator e1 = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		Evaluator e2 = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		e1.initialise(lnSt, 0, a, b, inc);
		e1.initialise(lnNd, 0, a, b, -inc);
		try
		{
			e1.next();
			getNextIsectLn(e1, lnSt, lnNd);
		} catch (RootNotFound r)
		{
			e1.initialise(lnSt, 0, a, b, -inc);
			e1.next();
			try
			{
				getNextIsectLn(e1, lnSt, lnNd);
			} catch (RootNotFound r1)
			{
				return -1;
			}
		}
		try
		{
			e2.next();
			getNextIsectLn(e2, lnSt, lnNd);
		} catch (RootNotFound r)
		{
			e2.initialise(lnNd, 0, a, b, inc);
			e2.next();
			try
			{
				getNextIsectLn(e2, lnSt, lnNd);
			} catch (RootNotFound r1)
			{
				return -1;
			}
		}
		System.out.println("got here");
		Point2D pOld1, pNew1, pOld2, pNew2;
		pNew1 = lnSt;
		pNew2 = lnNd;
		try
		{
			for (int i = 0; i < 20; i++)
			{
				pOld1 = pNew1;
				pOld2 = pNew2;
				pNew1 = getNextIsectLn(e1, lnSt, lnNd);
				pNew2 = getNextIsectLn(e2, lnSt, lnNd);
				if (pOld1.distance(pNew1) < inc / 1000 && pOld2.distance(pNew2) < inc / 1000)
					break;
			}
		} catch (RootNotFound r)
		{
			return 0;
		}
		if(Math.signum(e1.getInc()) == Math.signum(e2.getInc()))
			return 1;
		else return 2;
	}



	private LimCycleStart updateLimCycle(final LimCycleStart lc, double a, double b) throws RootNotFound
	{
		Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		if(lc.isPositive)
		{
			eval.initialise(lc.st, 0, a, b, inc);
		} else
		{
			eval.initialise(lc.st, 0, a, b, -inc);
		}
		eval.advance(2);
		Point2D p1 = lc.st;
		Point2D p2 = getNextIsectLn(eval, lc.refLine[0], lc.refLine[1]);
		while(p2.distance(p1) > inc/1000 && !Thread.interrupted())
		{
			eval.resetT();
			p1 = p2;
			p2 = getNextIsectLn(eval, lc.refLine[0], lc.refLine[1]);
		}
		return new LimCycleStart(p2, lc.isPositive, lc.refLine[0], lc.refLine[1]);
	}


	private void updateLimCycles()
	{
			limCycleUpdater.interrupt();
			limCycleUpdater = new Thread(() ->
			{
				Platform.runLater(() -> loading.setVisible(true));
				ArrayList<LimCycleStart> temp = new ArrayList<>();
				for (LimCycleStart lc : limCycles)
				{
					try
					{
						temp.add(updateLimCycle(lc, a, b));
						drawLimCycle(lc);
						render();
					} catch (RootNotFound ignored)
					{
					}
				}
				limCycles = temp;
				if (!Thread.interrupted())
					render();
				Platform.runLater(() -> loading.setVisible(false));
			});
			limCycleUpdater.start();
	}
	private void drawLimCycle(LimCycleStart lc)
	{
		synchronized (g)
		{
			g.setStroke(new BasicStroke(3));
			if (lc.isPositive)
			{
				drawGraphBack(new InitCond(lc.st), false, '+', awtAttrLimCycleColor);
			} else
			{
				drawGraphBack(new InitCond(lc.st), false, '-', awtRepLimCycleColor);
			}
			g.setStroke(new BasicStroke(1));
		}
	}

	private boolean drawNewLimCycle(Point2D start, Point2D lnSt, Point2D lnNd, boolean add)
	{
		//get to the line
		Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		eval.initialise(start, 0, a, b, inc);
		Point2D p1 = start;
		Point2D p2 = eval.next();
		Point2D isect = null;
		while (eval.getT() < 100)
		{
			try
			{
				isect = getIntersection(p1, p2, lnSt, lnNd);
				break;
			} catch (RootNotFound r)
			{
				p1 = p2;
				p2 = eval.next();
			}
		}
		if(isect == null)
		{
			eval.initialise(start, 0, a, b, -inc);
			while (eval.getT() < 100)
			{
				try
				{
					isect = getIntersection(p1, p2, lnSt, lnNd);
					break;
				} catch (RootNotFound r)
				{
					p1 = p2;
					p2 = eval.next();
				}
			}
		}
		if(isect == null) return false;
		p1 = isect;
		eval.initialise(isect, 0, a, b, eval.getInc());
		try
		{
			p2 = getNextIsectLn(eval, lnSt, lnNd);
		} catch (RootNotFound r)
		{
			eval.initialise(isect, 0, a, b, -eval.getInc());
			try
			{
				p2 = getNextIsectLn(eval, lnSt, lnNd);
			} catch (RootNotFound r2)
			{
				return false;
			}
		}
		boolean haveFlipped = false;
		try
		{
			while(inBounds(p2) && !Thread.interrupted())
			{
				try
				{
					p1 = p2;
					p2 = getNextIsectLn(eval, lnSt, lnNd);
					if (Math.abs(eval.getT()) < 3 * Math.abs(eval.getInc()))
						p2 = getNextIsectLn(eval, lnSt, lnNd);
					eval.resetT();
					if (p1.distance(p2) < inc / 10000)
					{
						synchronized (g)
						{
							g.setStroke(new BasicStroke(4));
							if (eval.getInc() > 0)
								drawGraphBack(new InitCond(p2), false, '+', awtAttrLimCycleColor);
							else
								drawGraphBack(new InitCond(p2), false, '-', awtRepLimCycleColor);
							g.setStroke(new BasicStroke(1));
						}
						synchronized (canv)
						{
							render();
						}
						cycleLine.setVisible(false);
						if(add)
						{
							limCycles.add(new LimCycleStart(p2, eval.getInc(), lnSt, lnNd));
						}
						return true;
					}
				} catch (RootNotFound r)
				{
					if(haveFlipped)
						throw new RootNotFound();
					else
					{
						haveFlipped = true;
						eval.initialise(isect, 0, a, b, -eval.getInc());
					}
				}
			}
		} catch (RootNotFound ignored)
		{
			if(add)
			{
				cycleLine.setVisible(false);
			}
		}
		return false;
	}

	/**
	 * gets the next intersection of the provided evaluator with the provided line
	 * @param eval the evaluator to use
	 * @param st the start of the line
	 * @param nd the end of the line
	 * @return the next intersection with the line
	 * @throws RootNotFound if it doesn't intersect
	 */
	private Point2D getNextIsectLn(Evaluator eval, Point2D st, Point2D nd) throws RootNotFound
	{
		eval.resetT();
		Point2D p1 = eval.getCurrent();
		Point2D p2 = eval.next();
		double tInc = eval.getInc();
		//TODO maybe add another setting for limcycle bounds
		while (Math.abs(eval.getT()) < 80)
		{
			try
			{
				return getIntersection(p1, p2, st, nd);
			} catch (RootNotFound r)
			{
				p1 = p2;
				p2 = eval.next();
			}
			CriticalPoint temp = null;
			try
			{
				temp = critical(p2);
			} catch (RootNotFound ignored) {}
			if(temp != null)
			{
//				System.out.println("point: " + p2 + "\nroot: " + temp.point + "\ninc: " + tInc);
				if ((tInc >= 0 && temp.type.isSink()) || (tInc <= 0 && temp.type.isSource()))
				{
//					System.out.println("testing");
					if (p2.distance(temp.point) < inc / 10) throw new RootNotFound();
				}
			}
		}
		throw new RootNotFound();
	}

	/**
	 * gets the next intersection of the provided evaluator with the cycle line
	 * @param eval the evaluator to use
	 * @return the next intersection with the cycle line
	 * @throws RootNotFound if it doesn't intersect
	 */
	private Point2D getNextIsectCyc(Evaluator eval) throws RootNotFound
	{
		return getNextIsectLn(eval,
				scrToNorm(new Point2D(cycleLine.getStartX(), cycleLine.getStartY())),
				scrToNorm(new Point2D(cycleLine.getEndX(), cycleLine.getEndY())));
		/*Point2D p1 = eval.getCurrent();
		Point2D p2 = eval.next();
		double tInc = eval.getInc();
		//TODO maybe add another setting for limcycle bounds
		while (inBoundsSaddle(p2) && eval.getT() < 100)
		{
			try
			{

				return getIntersectionCycleLine(p1, p2);
			} catch (RootNotFound r)
			{
				p1 = p2;
				p2 = eval.next();
			}
			CriticalPoint temp = null;
			try
			{
				temp = critical(p2);
			} catch (RootNotFound ignored) {}
			if(temp != null)
			{
				if (tInc > 0 && temp.type.isSink() || tInc < 0 && temp.type.isSource())
				{
					if (p2.distance(temp.point) < inc / 10) throw new RootNotFound();
				}
			}
		}
		throw new RootNotFound();*/
	}
	public void drawBasin(Point2D st)
	{
		Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		Point2D crit;
		System.out.println("got here");
		try
		{
			crit = critical(st).point;
		} catch (RootNotFound r)
		{
			System.out.println("haha no");
			return;
		}
		BasinFinder.init(this, crit);
		for(int i = 0; i < 8; i++)
		{
			new BasinFinder().start();
		}




//		double x = xMin.get();
//		double y = yMin.get();
//		while(x < xMax.get())
//		{
//			while(y < yMax.get())
//			{
//				eval.initialise(x, y, t, a, b, inc);
//				while(inBoundsSaddle(eval.getCurrent()) && eval.getT() < 100)
//				{
//					if(eval.next().distance(crit) < inc) break;
//				}
//				if(eval.next().distance(crit) < inc)
//				{
//					synchronized (g)
//					{
//						g.setColor(col);
//						g.fillRect(imgNormToScrX(x), imgNormToScrY(y), 2, 2);
//					}
//				}
//				y += (yMax.get() - yMin.get())/512;
//			}
//			y = yMin.get();
//			x += (xMax.get() - xMin.get())/512;

	}

	
	private SimpleMatrix getDerivsOfSol(Point2D p, double a, double b)
	{
		AST.Node res[] = new AST.Node[2];
		AST.Node derivAB[][] = new AST.Node[2][2];
		derivAB[0][0] = dx.differentiate('a').collapse();
		derivAB[0][1] = dx.differentiate('b').collapse();
		derivAB[1][0] = dy.differentiate('a').collapse();
		derivAB[1][1] = dy.differentiate('b').collapse();

		AST.Node derivXY[][] = new AST.Node[2][2];
		derivXY[0][0] = dx.differentiate('x').collapse();
		derivXY[0][1] = dx.differentiate('y').collapse();
		derivXY[1][0] = dy.differentiate('x').collapse();
		derivXY[1][1] = dy.differentiate('y').collapse();

		SimpleMatrix dab = new SimpleMatrix(2, 2);
		SimpleMatrix dxy = new SimpleMatrix(2, 2);
		try
		{
			for (int i = 0; i < 2; i++)
			{
				for (int j = 0; j < 2; j++)
				{
					dab.set(i, j, derivAB[i][j].eval(p.getX(), p.getY(), a, b, 0));
					dxy.set(i, j, derivXY[i][j].eval(p.getX(), p.getY(), a, b, 0));
				}
			}
			return dab;//.invert().negative().mult(dxy);
		} catch (EvaluationException r)
		{
			return null;
		}
	}

//	private SimpleMatrix getDerivOfLine(sepStart s1, double a, double b) throws RootNotFound
//	{
//		SimpleMatrix V = SimpleMatrix.identity(2);
//		SimpleMatrix D = getDerivsOfSol(s1.start, a, b);
//		SimpleMatrix Vp = D.mult(V);
//		Point2D iSect = null;
//		double x = s1.start.getX();
//		double y = s1.start.getY();
//		double t0 = sepIntersect(s1, a, b, line, iSect);
//		Evaluator eval = EvaluatorFactory.getRungeKuttaEval(dx, dy);
//		Point2D next = s1.start;
//		if (s1.positive)
//			eval.initialise(x, y, 0, a, b, inc);
//		else
//			eval.initialise(x, y, 0, a, b, -inc);
//		while (eval.getT() < t0)
//		{
//			next = eval.next();
//			V = V.plus(Vp.scale(eval.getInc()));
//			Vp = getDerivsOfSol(next, a, b);
//		}
//		Point2D temp = line[0].subtract(line[1]);
//		SimpleMatrix fin = new SimpleMatrix(2, 1);
//		fin.set(0, 0, next.getX());
//		fin.set(1, 0, next.getY());
//		SimpleMatrix res = new SimpleMatrix(2,2);
//		res.set(0, 0, temp.getX() * V.get(0, 0)*temp.getX() + V.get(1, 0)*temp.getY());
//		res.set(0, 1, temp.getX() * V.get(0, 1) * temp.getX() + V.get(1,1)*temp.getY());
//		res.set(1, 0, temp.getY() * V.get(0, 0)*temp.getX() + V.get(1, 0)*temp.getY());
//		res.set(1, 1, temp.getY() * V.get(0, 1) * temp.getX() + V.get(1,1)*temp.getY());
//		return res;
//	}




	public void renderSaddleCon(Point2D start, final sepStart s1, final sepStart s2, boolean add)
	{
		try
		{
			PrintWriter out = new PrintWriter("output.text");
			java.awt.Color tempCol;
			if (s1.saddle.point.distance(s2.saddle.point) < .000001 * ((xMax.get() - xMin.get()) + (yMax.get() - yMin.get()))/2)
				tempCol = (in.awtHomoSaddleConColor);
				//				in.saddleCanvas.getGraphicsContext2D().setStroke(in.homoSaddleConColor);
			else tempCol = (in.awtHeteroSaddleConColor);
			in.g.setStroke(new BasicStroke(3));
//			in.g.setColor(java.awt.Color.RED);

			double aInc = 1D * (in.xMax.get() - in.xMin.get()) / in.getWidth();
			double bInc = 1D * (in.yMax.get() - in.yMin.get()) / in.getHeight();
			AST.Node tr = Maths.add(dx.differentiate('x'), dy.differentiate('y')).collapse();
			// This is a variable that helps us guess better for the next value (using euler's method)
			double otherInc = 0D;
			Point2D prev;
			Point2D next;
			Point2D temp;
			Point2D st;
			Point2D diff;
			//This variable helps deal with switching isA. On the first rootNotFound switch it, but if that doesn't fix it
			//just go ahead and fail.
			boolean justThrew = false;
			//This one lets us skip a point, if for whatever reason we can't find a connection at one increment, skip over
			//a single point, but not more than once
			boolean justFailed = true;
			boolean isA = false;
			try
			{
				st = saddleConnection(s1, s2, isA, start.getX(), start.getY());
				if (add)
				{
					in.saddleCons.add(new SaddleCon(st, s1, s2));
					add = false;
				}
			} catch (RootNotFound r)
			{
				try
				{
					isA = !isA;
					st = saddleConnection(s1, s2, isA, start.getX(), start.getY());
					if (add)
					{
						in.saddleCons.add(new SaddleCon(st, s1, s2));
						add = false;
					}
				} catch (RootNotFound r1)
				{
					return;
				}
			}
			for (int i = 0; i < 2; i++)
			{
//			System.out.println("AINC: " + aInc);
//			System.out.println("BINC: " + bInc);

				prev = st;
				while (in.inBounds(prev.getX(), prev.getY()) && !Thread.interrupted())
				{

					if (isA) temp = new Point2D(prev.getX() + otherInc, prev.getY() + bInc);
					else temp = new Point2D(prev.getX() + aInc, prev.getY() + otherInc);
					try
					{
						next = saddleConnection(s1, s2, isA, temp.getX(), temp.getY());
//						System.out.println(i);
//						if(next.subtract(prev).getY()/next.subtract(prev).getX() < .5)
//						{
//							for(int ix = (int) Math.round(prev.getX()); ix < Math.round(next.getX()); ix++)
//							{
//
//							}
//						} else
//						{
//
//						}
//						in.drawLine(prev, next, in.saddleCanvas);
						in.drawLine(prev, next, tempCol);
						Platform.runLater(in::render);
//						in.render();
//
//						sg.drawLine(
//								(int) normToScrX(prev.getX()),
//								(int) normToScrY(prev.getY()),
//								(int) normToScrX(next.getX()),
//								(int) normToScrY(next.getY()));
//						in.saddleImageView.setImage(SwingFXUtils.toFXImage(in.saddleImageBuf, null));
						diff = next.subtract(prev);
//					if(isA)
//					{
//						if(diff.getX() == 0D || diff.getY()/diff.getX() > .5)
//							isA = false;
//					} else
//					{
//						if(diff.getY() == 0D || diff.getX()/diff.getY() > .5)
//							isA = true;
//					}

//					if(isA)
//					{
//						if(diff.getX() < .1 * inc)
//						{
//							isA = false;
//							otherInc = 0;
//						}
//						else
//							otherInc = aInc * (diff.getY()/diff.getX());
//					} else
//					{
//						if(diff.getY() < .1 * inc)
//						{
//							isA = true;
//							otherInc = 0;
//						}
//						else
//							otherInc = bInc * (diff.getX()/diff.getY());
//					}
						if (s1.saddle.point.distance(s2.saddle.point) < .000001 * ((xMax.get() - xMin.get()) + (yMax.get() - yMin.get()))/2)
						{
							System.out.println("they are the same");
							try
							{
								double prevEval = tr.eval(s1.saddle.point.getX(), s1.saddle.point.getY(), prev.getX(), prev.getY(), 0);
								out.println("Point: " + prev);
								out.println("Trace: " + prevEval + "\n");
								double nextEval = tr.eval(s1.saddle.point.getX(), s1.saddle.point.getY(), next.getX(), next.getY(), 0);
								if (prevEval == 0D || prevEval == -0D) in.degenSaddleCons.add(prev);
								else if (nextEval == 0D || nextEval == -0D) in.degenSaddleCons.add(next);
								else if (Math.signum(prevEval) != Math.signum(nextEval))
								{
									double factor = Math.abs(prevEval) / (Math.abs(nextEval) + Math.abs(prevEval));
									in.degenSaddleCons.add(prev.add(next.subtract(prev).multiply(factor)));
									System.out.println("DEGEN DEGEN DEGEN");
								}

							} catch (EvaluationException ignored)
							{
							}
						}
						prev = next;
						System.out.println(prev);
						System.out.println(isA);
						justThrew = false;
					} catch (RootNotFound r)
					{
						System.out.println("off the scrn? " + r.offTheScreen);
						if (r.offTheScreen)
						{
							System.out.println("did the thing");
							break;
						}
						if (prev.distance(st) < Math.min(aInc, bInc)) break;
						if (justThrew)
						{
//						if(justFailed)
							{
								System.out.println("breaking");
								break;
//						} else
//						{
//							justFailed = true;
							}
						} else
						{
							isA = !isA;
							justThrew = true;
						}
					}
				}
				aInc = -aInc;
				bInc = -bInc;
//			in.saddleCanvas.getGraphicsContext2D().setStroke(Color.TURQUOISE);
			}
			in.gc.setStroke(Color.BLACK);
			in.drawDegenSaddleCons();

			Platform.runLater(this::draw);
			out.close();
			in.render();
		} catch (FileNotFoundException ignored) {}
	}

	/**
	 * Sets the bounds for calculating saddle connections
	 * @param xmn the new x min
	 * @param xmx the new x max
	 * @param ymn the new y min
	 * @param ymx the new y max
	 */
	public void setSaddleBounds(double xmn, double xmx, double ymn, double ymx)
	{
		this.dSaddleYMax = ymx;
		this.dSaddleYMin = ymn;
		this.dSaddleXMin = xmn;
		this.dSaddleXMax = xmx;
	}

	/**
	 * whether or not the point (x,y) is in bounds for saddle connection purposes
	 * @param x the x coord
	 * @param y the y coord
	 * @return whether or not (x, y) is in bounds
	 */
	private boolean inBoundsSaddle(double x, double y)
	{
		return x <= dSaddleXMax && x >= dSaddleXMin && y <= dSaddleYMax && y >= dSaddleYMin;
	}

	/**
	 * whether or not the point p is in bounds for saddle connection purposes
	 * @param p the piont in question
	 * @return whether or not it's in bounds
	 */
	boolean inBoundsSaddle(Point2D p)
	{
		if(p != null)
			return inBoundsSaddle(p.getX(), p.getY());
		return false;
	}
	private double minDist(final sepStart sep, final Point2D other, final double at, final double bt, boolean firstTry) throws RootNotFound
	{
		boolean shortcut = false;
		double mins [] = new double[5];
		double min = Double.MAX_VALUE;
//		System.out.println(sep.getStart(.01));
		Evaluator eval1 = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		Evaluator eval2 = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		Evaluator eval;
//		double in;
//		if(sep.posEig()) in = inc;
//		else in = -inc;
		double factor = 1D;
		eval1.initialise(sep.getStart(Math.abs(inc)), 0, at, bt, inc);
		eval2.initialise(sep.getStart(Math.abs(inc)), 0, at, bt, -inc);
		Point2D prev = sep.getStart(Math.abs(inc));
		Point2D next1 = eval1.next();
		Point2D next2 = eval2.next();
		if(!sep.saddle.point.equals(other))
		{
			if (next1.distance(sep.saddle.point) < next2.distance(sep.saddle.point))
			{
				eval = eval2;
			} else
			{
				eval = eval1;
			}
		} else
		{
			if(sep.posEig())
			{
				eval = eval1;
			} else
			{
				eval = eval2;
			}
		}
		Point2D next = eval.next();
		int i = 0;
		boolean approaching = false;
//		approaching = !sep.saddle.point.equals(other);
//		LinkedList<Point2D> record = new LinkedList<>();
//		if(!firstTry)
//			eval.initialise(sep.getStart(factor * (xMax - xMin)/c.getWidth()), 0, at, bt, -in);
		while(inBoundsSaddle(prev) && eval.getT() < 100 && !Thread.interrupted() && i < 4)
//		while (next.distance(other) < prev.distance(other) || !approaching)
		{
//			System.out.println(prev);
//			record.add(prev);
			prev = next;
			next = eval.next();
			if(prev.equals(next))
			{
				System.out.println("is this the problem?");
				break;
			}
			if (!approaching && next.distance(other) <= prev.distance(other))
			{
				approaching = true;
			} else if(approaching && next.distance(other) >= prev.distance(other))
			{
				if(shortcut) return prev.distance(other);
				approaching = false;
				i++;
			}
			if(approaching)
			{
				double d = next.distance(other);

				if(d < min)
				{
					min = d;
					mins[i] = d;
				}
			}


		}
		if (min == Double.MAX_VALUE)
		{

			System.out.println("____________________________");
			System.out.println("a: " + at);
			System.out.println("b: " + bt);
			System.out.println("Start: " + sep.getStart(factor * (xMax.get() - xMin.get())/this.getWidth()));
			System.out.println("Goal: " + other);
			System.out.println("approaching?: " + approaching);
			System.out.println("first try? " + firstTry);
			System.out.println("off the screen at " + next);
			System.out.println("____________________________");
//				return 0;
			if (firstTry)
			{
				
//				try
//				{
//					PrintWriter out = new PrintWriter("output.text");
//					out.println(sep.saddle.point);
//					out.println(in);
//					out.println();
//					for(Point2D pt : record) out.println(pt);
//					out.close();
//				} catch (FileNotFoundException ignored) {}
//					System.out.println("flipping");

				return minDist(sepStart.flip(sep), other, at, bt, false);
//				return minDist(sep, other, at, bt, false);
			}
			throw new RootNotFound(true);
		}
//		System.out.println("mindistprnt: \nfound: " + prev + "\nother: " + other + "\nstart: " + sep.saddle.point +
//				"\na: " + a + "\nb: " + b + "\n-----------------------");
//		for(int n = 0; n < i; n++)
//			if (mins[n] < min) min = mins[n];
		return min;
	}
	private void assertSaddle(final sepStart s1, final sepStart s2) throws RootNotFound
	{
		if(s1.saddle.type != CritPointTypes.SADDLE)
		{
			System.out.println("not a saddle at " + s1.saddle.point);
			throw new RootNotFound();
		}
		if(s2.saddle.type != CritPointTypes.SADDLE)
		{
			System.out.println("not a saddle at " + s2.saddle.point);
			throw new RootNotFound();
		}
	}

	private Point2D saddleConnection(final sepStart s1init, final sepStart s2init, boolean isA, double at, double bt) throws RootNotFound
	{
		sepStart s1 = s1init.clone();
		sepStart s2 = s2init.clone();
		long time = System.nanoTime();
		double inc;
		double old;
		if(isA)
		{
			old = at;
			inc = (in.xMax.get() - in.xMin.get()) / 10000;
		}
		else
		{
			old = bt;
			inc = (in.yMax.get() - in.yMin.get())/10000;
		}
		double tol;
		if(isA)
			tol = (in.xMax.get() - in.xMin.get())/(1 * in.getWidth());
		else tol = (in.yMax.get() - in.yMin.get())/(1 * in.getHeight());
		Point2D saddle1 = s1.saddle.point;
		Point2D saddle2 = s2.saddle.point;
		assertSaddle(s1, s2);
		double dist1;
		double dist2;
		Point2D sad;
		sepStart sep;
		if(minDist(s1, saddle2, at, bt, true) > minDist(s2, saddle1, at, bt, true))
		{
			sep = s1;
			sad = saddle2;
		}
		else
		{
			sep = s2;
			sad = saddle1;
		}
		if(isA)
			inc = (in.xMax.get() - in.xMin.get())/10000.;//.000001;
		else inc = (in.yMax.get() - in.yMin.get())/10000.;
		dist1 = minDist(sep, sad, at, bt, true);
		while (dist1 > tol)
		{
			if(Thread.interrupted()) throw new RootNotFound();
			if(System.nanoTime() - time > 5e9) throw new RootNotFound();
//			System.out.println("A': " + at);
//			System.out.println("B': " + bt);
//			System.out.println("dist: " + dist1);
			if(isA) at += inc;
			else bt += inc;
			s1 = s1.updateSaddle(critical(s1.saddle.point, at, bt));
			s2 = s2.updateSaddle(critical(s2.saddle.point, at, bt));
			saddle1 = s1.saddle.point;
			saddle2 = s2.saddle.point;
			try
			{
				assertSaddle(s1, s2);
			} catch (RootNotFound r)
			{
				System.out.println("doesn't fail");
				throw new RootNotFound();
			}
			if(minDist(s1, saddle2, at, bt, true) > minDist(s2, saddle1, at, bt, true))
			{
				sep = s1;
				sad = saddle2;
			}
			else
			{
				sep = s2;
				sad = saddle1;
			}
			dist2 = minDist(sep, sad, at, bt, true);
			if(dist2 - dist1 == 0 && dist1 > 2 * tol)
			{
				System.out.println("throwing. Distance: " + dist1 +
						"\nb: " + bt +
						"\na: " + at +
						"\nisA: " + isA +
						"\ninc: " + inc +
						"\ntol: " + tol);
				throw new RootNotFound();
			}

			if (dist2 > dist1)
			{
//				System.out.println("flippity do");
				inc = -(inc * .5);
//				if(Math.abs(inc) <= .000000000000000001)
//				{
//					System.out.println("throwing");
//					throw new RootNotFound();
//				}
			}
			//if we aren't converging fast enough increase the increment
			else if(dist1 - dist2 < .001 * dist1)
			{
//				System.out.println("what the fuck.");
//				inc *= 1.1;
			}
			dist1 = dist2;
			if(at < 2 * in.xMin.get() - in.xMax.get() || at > 2 * in.xMax.get() - in.xMin.get() ||
			bt < 2 * in.yMin.get() - in.yMax.get() || bt > 2 * in.yMax.get() - in.yMin.get())
				throw new RootNotFound(true);
		}
//		System.out.println(new Point2D(at, bt));
		double aInc = (in.xMax.get() - in.xMin.get()) / in.getWidth();
		double bInc = (in.yMax.get() - in.yMin.get()) / in.getHeight();
		if(isA)
		{
			if(Math.abs((at - old)/bInc) > 2.)
			{
				System.out.println((at - old)/bInc);
//				throw new RootNotFound();
			}
		} else if (Math.abs((bt - old)/aInc) > 2.)
		{
			System.out.println((bt - old) / aInc);
//			throw new RootNotFound();
		}
//		System.out.println("isA? " + isA);
		return new Point2D(at, bt);

//		SimpleMatrix start = new SimpleMatrix(2, 1);
//		start.setColumn(0, 0, at, bt);
//		SimpleMatrix deriv1 = getDerivOfLine(s1, at, bt);
//		SimpleMatrix deriv2 = getDerivOfLine(s2, at, bt);
//		SimpleMatrix D = deriv1.plus(deriv2);
//		Point2D isect1, isect2;
//		isect1 = null;
//		isect2 = null;
//		sepIntersect(s1, at, bt, line, isect1);
//		sepIntersect(s2, at, bt, line, isect2);
//		//maybedo fix null pointer exception here
//		double dist = isect1.distance(isect2);
//		SimpleMatrix prev = null;
//
//		for(int i = 0; i < 15; i++)
//		{
//			prev = start;
//			start = start.minus(D.invert().mult(start));
//			at = start.get(0,0);
//			bt = start.get(1,0);
//			deriv1 = getDerivOfLine(s1, at, bt);
//			deriv2 = getDerivOfLine(s2, at, bt);
//			D = deriv1.plus(deriv2);
//
//		}
//		if(Math.abs(prev.minus(start).get(0, 0)) < tol && Math.abs(prev.minus(start).get(1,0)) < tol)
//		{
//			System.out.println("found one");
//			return new Point2D(at, bt);
//		}
//		//else throw new RootNotFound();
//		else return null;

	}


	private double sepIntersect(sepStart s, double a, double b, Point2D[] ln, Point2D res) throws RootNotFound
	{
		double x, y;
		x = s.getStart((xMax.get() - xMin.get())/this.getWidth()).getX();
		y = s.getStart((yMax.get() - yMin.get())/this.getHeight()).getY();
		Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		Point2D prev;
		Point2D next;
		prev = new Point2D(x, y);
		Point2D temp;
		if (s.posDir())
			eval.initialise(x, y, t, a, b, inc);
		else
			eval.initialise(x, y, t, a, b, -inc);
		while (eval.getT() < 100 + t)
		{
			next = eval.next();
			if (orientation(prev, ln) == 0)
			{
				res = prev;
				return eval.getT();
			} else if (Math.signum(orientation(prev, ln)) != Math.signum(orientation(next, ln)))
			{
				temp = prev.midpoint(next);
				double dot = (temp.getX() - ln[0].getX()) * (ln[1].getX() - ln[0].getX()) +
						(temp.getY() - ln[0].getY()) * (ln[1].getY() - ln[0].getY());
				if (0 <= dot && dot < Math.pow(ln[0].distance(ln[1]), 2))
					if (Math.signum(temp.getX() - ln[0].getX()) != Math.signum(temp.getX() - ln[1].getX()) &&
							Math.signum(temp.getY() - ln[0].getY()) != Math.signum(temp.getY() - ln[1].getY()))//!Double.isNaN(temp.getX()) && !Double.isNaN(temp.getY()))
					{
						//System.out.println(temp);
						res = prev.midpoint(next);
						return eval.getT();
					}
			}
			prev = next;
		}
		throw new RootNotFound();
	}

	private double orientation(Point2D p, Point2D ln[])
	{
		return (p.getX() - ln[0].getX()) * (ln[1].getY() - ln[0].getY()) -
				(p.getY() - ln[0].getY()) * (ln[1].getX() - ln[0].getX());
	}

	private Point2D getSourceOrSink(Point2D start) throws RootNotFound
	{
		CriticalPoint temp = critical(start);
		if (temp.type != CritPointTypes.NODESOURCE && temp.type != CritPointTypes.SPIRALSOURCE &&
				temp.type != CritPointTypes.NODESINK && temp.type != CritPointTypes.SPIRALSINK)
			throw new RootNotFound();
		else return temp.point;
	}

	private Point2D getSaddle(Point2D start) throws RootNotFound
	{
		CriticalPoint temp = critical(start);
		if (temp.type != CritPointTypes.SADDLE) throw new RootNotFound();
		else return temp.point;
	}

	private CriticalPoint critical(Point2D start) throws RootNotFound
	{
		return EvaluatorFactory.getBestEvaluator(dx, dy).findCritical(start, a, b, t);
	}
	private CriticalPoint critical(Point2D start, double a, double b) throws RootNotFound
	{
		return EvaluatorFactory.getBestEvaluator(dx, dy).findCritical(start, a, b, 0);
	}

	private void updateCritical()
	{
		List<CriticalPoint> temp = new LinkedList<>();
		for (CriticalPoint c : criticalPoints)
		{
			try
			{
				temp.add(critical(c.point));
			} catch (RootNotFound ignored)
			{
			}
		}
		criticalPoints = temp;
		List<Point2D> temp1 = new LinkedList<>();
		for (Point2D c : selectedCritPoints)
		{
			try
			{
				temp1.add(critical(c).point);
			} catch (RootNotFound ignored) {}
		}
		selectedCritPoints = temp1;
	}

	private void labelCritical(CriticalPoint p)
	{
		if (inBounds(p.point))
		{
//			c.getGraphicsContext2D().setFill(criticalColor);
//			c.getGraphicsContext2D().fillOval(normToScrX(p.point.getX()) - 2.5, normToScrY(p.point.getY()) - 2.5, 5, 5);
			synchronized (g)
			{
				g.setColor(awtCriticalColor);
				g.fillOval(imgNormToScrX(p.point.getX()) - 5, imgNormToScrY(p.point.getY()) - 5, 10, 10);
			}
			Label text = new Label(p.type.getStringRep());
			text.setPadding(new Insets(2));
			text.setBorder(new Border(new BorderStroke(criticalColor, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
			this.getChildren().add(text);
			text.setLayoutX(normToScrX(p.point.getX()) + 8);
			text.setLayoutY(normToScrY(p.point.getY()) - 24);
			final Text t = new Text(text.getText());

			if (text.getLayoutY() < 0)
			{
				text.setLayoutY(normToScrY(p.point.getY()) + 4);
			}
			if (text.getLayoutX() + t.getLayoutBounds().getWidth() + 4 > this.getWidth())
			{
				text.setLayoutX(normToScrX(p.point.getX()) - 12 - t.getLayoutBounds().getWidth());
			}
			text.setTextFill(criticalColor);

			text.setVisible(true);
			needsReset.add(text);
			c.getGraphicsContext2D().setFill(Color.BLACK);
		}
	}

	private void drawGraphs()
	{
		gc.setStroke(solutionColor);


		for (Node n : needsReset)
		{
			n.setVisible(false);
		}
		needsReset.clear();
		for (InitCond i : initials)
		{
			drawGraph(i, true, awtSolutionColor);
		}
		for (CriticalPoint p : criticalPoints)
		{
			labelCritical(p);
		}

		gc.setStroke(Color.BLACK);
	}

	private void drawGraph(InitCond init, boolean arrow, java.awt.Color color)
	{
		drawGraphBack(init, arrow, '1', color);
	}

	private void drawGraphBack(InitCond init, boolean arrow, char dir, java.awt.Color color)
	{
		double x, y;
		x = init.x;
		y = init.y;
		t = init.t;
		Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		Point2D initialDir = eval.evaluate(x, y, a, b, t, inc);
		if (arrow) drawArrow(x, y, initialDir.getX(), initialDir.getY());
		Point2D prev;
		Point2D next;
		eval.initialise(x, y, t, a, b, inc);
		prev = new Point2D(x, y);
		if (dir != '-')
			while (eval.getT() < 100 + t)
			{
				next = eval.next();
				if (inBounds(prev) || inBounds(next))
						drawLine(prev, next, color);
				prev = next;
			}
		eval.initialise(x, y, t, a, b, -inc);
		prev = new Point2D(x, y);
		if (dir != '+')
			while (eval.getT() > t - 100)
			{
				next = eval.next();
				if (inBounds(prev) || inBounds(next))
					synchronized (gc)
					{
						drawLine(prev, next, color);
						//gc.strokeLine(normToScrX(prev.getX()), normToScrY(prev.getY()), normToScrX(next.getX()), normToScrY(next.getY()));
					}
				prev = next;
			}

	}

	private void drawIsoclines()
	{
		for (InitCond c : isoclines)
		{
			drawIso(c);
		}
		for (Point2D pt : horizIsos)
		{
			drawHorizIso(pt);
		}
		for (Point2D pt : vertIsos)
		{
			drawVertIso(pt);
		}
	}

	private void drawIso(InitCond init)
	{
		gc.setStroke(isoclineColor);
		try
		{
			double val = dy.eval(init.x, init.y, a, b, t) / dx.eval(init.x, init.y, a, b, t);
			AST.Node slope = Maths.minus(Maths.divide(dy, dx), new Value(val));
			drawIsoHelper(slope, new Point2D(init.x, init.y), awtIsoclineColor);


		} catch (EvaluationException ignored)
		{
		}
		gc.setStroke(Color.BLACK);
	}

	private void drawHorizIso(Point2D pt)
	{
		gc.setStroke(horizIsoColor);
		g.setColor(awtHorizIsoColor);
		try
		{
			AST.Node thing = Maths.divide(dy, dy.differentiate('y')).collapse();
			double yOld = pt.getY();
			double y = pt.getY();
			double x = pt.getX();
			for (int i = 0; i < 10; i++)
			{
				yOld = y;
				y = y - thing.eval(x, y, a, b, t);
			}
			if (Math.abs(y - yOld) < .000001)
			{
				drawIsoHelper(dy, new Point2D(x, y), awtHorizIsoColor);
			} else
			{
				thing = Maths.divide(dy, dy.differentiate('x')).collapse();
				double xOld = pt.getX();
				y = pt.getY();
				x = pt.getX();
				for (int i = 0; i < 10; i++)
				{
					xOld = x;
					x = x - thing.eval(x, y, a, b, t);

				}
				if (Math.abs(x - xOld) < .000001)
				{
					drawIsoHelper(dy, new Point2D(x, y), awtHorizIsoColor);
				}
			}
		} catch (EvaluationException ignored)
		{
		}
		gc.setStroke(Color.BLACK);
	}

	private void drawVertIso(Point2D pt)
	{
		gc.setStroke(vertIsoColor);
		try
		{
			AST.Node thing = Maths.divide(dx, dx.differentiate('x')).collapse();
			double xOld = pt.getX();
			double y = pt.getY();
			double x = pt.getX();
			for (int i = 0; i < 10; i++)
			{
				xOld = x;
				x = x - thing.eval(x, y, a, b, t);

			}
			if (Math.abs(x - xOld) < .000001)
			{
				drawIsoHelper(dx, new Point2D(x, y), awtVertIsoColor);
			} else
			{
				thing = Maths.divide(dx, dx.differentiate('y')).collapse();
				double yOld = pt.getY();
				y = pt.getY();
				x = pt.getX();
				for (int i = 0; i < 10; i++)
				{
					yOld = y;
					y = y - thing.eval(x, y, a, b, t);
				}
				if (Math.abs(y - yOld) < .000001)
				{
					drawIsoHelper(dx, new Point2D(x, y), awtVertIsoColor);
				}
			}
		} catch (EvaluationException ignored)
		{
		}
		gc.setStroke(Color.BLACK);
	}

	private void drawIsoHelper(AST.Node slope, Point2D init, java.awt.Color color)
	{
		try
		{
			boolean firstTime = true;
			boolean isX = true;
			Point2D first, second;
			first = init;
			AST.Node slopeDeriv = slope.differentiate('y');
			AST.Node thing = Maths.divide(slope, slopeDeriv);
			double sign = 1;
			double tol = 30.;
			long time = System.nanoTime();
			double xinc = 1.5 * (xMax.get() - xMin.get()) / this.getWidth();
			double yinc = 1.5 * (yMax.get() - yMin.get()) / c.getHeight();
			for (int j = 0; j < 2; j++)
			{
				while (inBounds(first) && System.nanoTime() - time < 100000000)
				{
					if (isX)
					{
						double x = first.getX() + xinc;
						double yOld = first.getY();
						double y = first.getY();
						for (int i = 0; i < 10; i++)
						{
							yOld = y;
							y = y - thing.eval(x, y, a, b, t);
						}
						if (Math.abs(y - yOld) < .00001 && ((Math.abs((y - first.getY()) / (x - first.getX())) < tol) || firstTime))
						{
							firstTime = false;
							if (Math.abs((y - first.getY()) / (x - first.getX())) > 1)
							{
								isX = false;
								slopeDeriv = slope.differentiate('x');
								thing = Maths.divide(slope, slopeDeriv);
								sign = Math.signum((y - first.getY()) / (x - first.getX()));
							}
							second = new Point2D(x, y);
							drawLine(first, second, color);
							//gc.strokeLine(normToScrX(first.getX()), normToScrY(first.getY()), normToScrX(second.getX()), normToScrY(second.getY()));
							first = second;
						} else break;
					} else
					{
						if (sign == 0.0) break;
						double y = first.getY() + (yinc * sign);
						double xOld = first.getX();
						double x = first.getX();
						for (int i = 0; i < 10; i++)
						{
							xOld = x;
							x = x - thing.eval(x, y, a, b, t);
						}
						if (Math.abs(x - xOld) < .00001 && (Math.abs((y - first.getY()) / (x - first.getX())) > 1 / tol))
						{
							if (Math.abs((y - first.getY()) / (x - first.getX())) < 1)
							{
								isX = true;
								slopeDeriv = slope.differentiate('y');
								thing = Maths.divide(slope, slopeDeriv);
							}
							second = new Point2D(x, y);
							drawLine(first, second, color);
							//gc.strokeLine(normToScrX(first.getX()), normToScrY(first.getY()), normToScrX(second.getX()), normToScrY(second.getY()));
							first = second;
						} else break;
					}
				}
				xinc = -xinc;
				yinc = -yinc;
				first = init;
			}
		} catch (EvaluationException ignored)
		{
		}
	}


	private void drawArrow(double x, double y, double dx, double dy)
	{
		double angle = (Math.atan(-dy / dx));
		if (dx < 0) angle += Math.PI;
//		double xScr = normToScrX(x);
//		double yScr = normToScrY(y);
//		gc.save();
//		gc.transform(new Affine(new Rotate(angle, xScr, yScr)));
//		gc.strokeLine(xScr, yScr, xScr - 5, yScr + 3);
//		gc.strokeLine(xScr, yScr, xScr - 5, yScr - 3);
//		gc.restore();
		int xScr = imgNormToScrX(x);
		int yScr = imgNormToScrY(y);
		AffineTransform saveAt = g.getTransform();
		g.rotate(angle, xScr, yScr);
		g.drawLine(xScr, yScr, xScr - 10, yScr + 6);
		g.drawLine(xScr, yScr, xScr - 10, yScr - 6);
		g.setTransform(saveAt);

	}

	public void drawSeparatrices()
	{
		drawSep = true;
		draw();
	}

	@Override
	public void clear()
	{
		initials.clear();
		criticalPoints.clear();
		selectedSeps.clear();
		selectedCritPoints.clear();
		isoclines.clear();
		vertIsos.clear();
		horizIsos.clear();
		limCycles.clear();
		draw();
	}

	private void drawSep(CriticalPoint c)
	{
		double tol = 2 * (xMax.get() - xMin.get())/this.getWidth();
		try
		{
			if (c.type == CritPointTypes.SADDLE)
			{
				boolean temp;
				char sn;
				InitCond point1 = new InitCond(c.point.getX() + tol * c.matrix.getEigenVector(0).get(0),
						c.point.getY() + tol * c.matrix.getEigenVector(0).get(1), t);
				InitCond point3 = new InitCond(c.point.getX() - tol * c.matrix.getEigenVector(0).get(0),
						c.point.getY() - tol * c.matrix.getEigenVector(0).get(1), t);
				Point2D initl = new Point2D(point1.x, point1.y);
//				temp = tester.next().subtract(initl).angle(initl) < .1;
				temp = c.matrix.getEigenvalue(0).getReal() > 0;
				java.awt.Color tempCol;
				if (!temp)
				{
					gc.setStroke(stblSeparatrixColor);
					tempCol = (awtStblSeparatrixColor);
					sn = '-';
				}
				else
				{
					tempCol = (awtUnstblSeparatrixColor);
					gc.setStroke(unstblSeparatrixColor);
					sn = '+';
				}
				drawGraphBack(point1, false, sn, tempCol);
				drawGraphBack(point3, false, sn, tempCol);

				if (temp)
				{
					tempCol = (awtStblSeparatrixColor);
					gc.setStroke(stblSeparatrixColor);
					sn = '-';
				}
				else
				{
					tempCol = (awtUnstblSeparatrixColor);
					gc.setStroke(unstblSeparatrixColor);
					sn = '+';
				}
				InitCond point2 = new InitCond(c.point.getX() + tol * c.matrix.getEigenVector(1).get(0),
						c.point.getY() + tol * c.matrix.getEigenVector(1).get(1), t);
				InitCond point4 = new InitCond(c.point.getX() - tol * c.matrix.getEigenVector(1).get(0),
						c.point.getY() - tol * c.matrix.getEigenVector(1).get(1), t);
				drawGraphBack(point2, false, sn, tempCol);
				drawGraphBack(point4, false, sn, tempCol);
			}
		} catch (NullPointerException ignored)
		{
		}
	}

	private void drawSelectedCritPoints()
	{
		gc.setStroke(criticalColor);
		for (Point2D p : selectedCritPoints)
		{
			gc.strokeOval(normToScrX(p.getX()) - 8, normToScrY(p.getY()) - 8, 16, 16);
		}
		gc.setStroke(Color.BLACK);
	}

	@Override
	public void draw()
	{
		super.draw();
		updateCritical();
		drawGraphs();
		drawIsoclines();
		if (drawSep)
		{
			for (CriticalPoint c : criticalPoints)
			{
				drawSep(c);
			}

		}
		drawSelectedCritPoints();
		gc.setLineWidth(2);
		g.setStroke(new BasicStroke(2));
		double inc = 2 * (xMax.get() - xMin.get())/this.getWidth();
		for (sepStart s : selectedSeps)
		{
			if (!s.posEig())
			{
				gc.setStroke(stblSeparatrixColor);
				drawGraphBack(new InitCond(s.getStart(inc).getX(), s.getStart(inc).getY(), 0), false, '-', awtStblSeparatrixColor);
			} else
			{
				gc.setStroke(unstblSeparatrixColor);
				drawGraphBack(new InitCond(s.getStart(inc).getX(), s.getStart(inc).getY(), 0), false, '+', awtUnstblSeparatrixColor);
			}
		}
		g.setStroke(new BasicStroke(1));
		gc.setLineWidth(1);
		gc.setStroke(Color.BLACK);
		render();
		updateLimCycles();
	}
	@Override
	public boolean writePNG(File f)
	{
		BufferedImage temp = new BufferedImage(canv.getWidth(), canv.getHeight(), canv.getType());
		Graphics2D g2 = temp.createGraphics();
		g2.drawImage(canv, 0, 0, null);
		g2.setColor(awtCriticalColor);
		for(CriticalPoint p : this.criticalPoints)
		{
			if(inBounds(p.point))
			{
				int x, y;
				x = imgNormToScrX(p.point.getX()) + 8;
				y = imgNormToScrY(p.point.getY()) - 12;
				g2.setFont(g2.getFont().deriveFont(12F));
				int w = g2.getFontMetrics().stringWidth(p.type.getStringRep());

				if (y <= 0)
				{
					y = imgNormToScrY(p.point.getY()) + 16;
				}
				if (x + w + 4 > temp.getWidth())
				{
					x = imgNormToScrX(p.point.getX()) - 12 - w;
				}
				g2.drawString(p.type.getStringRep(), x, y);
			}
		}
		g2.setColor(java.awt.Color.BLACK);
		int x0 = imgNormToScrX(0);
		int y0 = imgNormToScrY(0);
		g2.drawLine(x0, 0, x0, temp.getHeight());
		g2.drawLine(0, y0, temp.getWidth(), y0);
		try
		{
			ImageIO.write(temp, "png", f);
			return true;
		} catch (IOException | NullPointerException oof)
		{
			return false;
		}
	}


	private static class InitCond
	{
		public double x, y, t;

		public InitCond(double x, double y, double t)
		{
			this.x = x;
			this.y = y;
			this.t = t;
		}

		public InitCond(Point2D p)
		{
			this.t = t;
			this.x = p.getX();
			this.y = p.getY();
		}
	}


	/**
	 * test if q lies on segment pr if they are colinear
	 * @param p start
	 * @param q point to test
	 * @param r end
	 * @return whether or not q is on pr
	 */
	boolean onSegment(Point2D p, Point2D q, Point2D r)
	{
		return q.getX() <= Math.max(p.getX(), r.getX()) && q.getX() >= Math.min(p.getX(), r.getX()) &&
				q.getY() <= Math.max(p.getY(), r.getY()) && q.getY() >= Math.min(p.getY(), r.getY());
	}

	/** To find orientation of ordered triplet (p, q, r).
	 * The function returns following values
	 * 0 --> p, q and r are colinear
	 * 1 --> Clockwise
	 * 2 --> Counterclockwise
	 */
	int orientation(Point2D p, Point2D q, Point2D r)
	{
		double val = (q.getY() - p.getY()) * (r.getX() - q.getX()) -
				(q.getX() - p.getX()) * (r.getY() - q.getY());

		if (val == 0) return 0; // colinear

		return (val > 0)? 1: 2; // clock or counterclock wise
	}

	/**
	* The main function that returns true if line segment 'p1q1' and 'p2q2' intersect.
	 * @param p1 first start
	 * @param q1 first end
	 * @param p2 second start
	 * @param q2  second end
	 * @return the intersection
	 * @throws RootNotFound if they dont intersect
	 */
	Point2D getIntersection(Point2D p1, Point2D q1, Point2D p2, Point2D q2) throws RootNotFound
	{
		// Find the four orientations needed for general and
		// special cases
		int o1 = orientation(p1, q1, p2);
		int o2 = orientation(p1, q1, q2);
		int o3 = orientation(p2, q2, p1);
		int o4 = orientation(p2, q2, q1);

		// General case
		if (o1 != o2 && o3 != o4)
			return new Point2D(
					((p1.getX() * q1.getY() - p1.getY() * q1.getX()) * (p2.getX() - q2.getX()) -
							(p1.getX() - q1.getX()) * (p2.getX() * q2.getY() - p2.getY() * q2.getX())) /
							((p1.getX() - q1.getX()) * (p2.getY() -
									q2.getY()) - (p1.getY() - q1.getY()) * (p2.getX() - q2.getX())),
					((p1.getX() * q1.getY() - p1.getY() * q1.getX()) * (p2.getY() - q2.getY()) -
							(p1.getY() - q1.getY()) * (p2.getX() * q2.getY() - p2.getY() * q2.getX())) /
							((p1.getX() - q1.getX()) * (p2.getY() - q2.getY()) -
									(p1.getY() - q1.getY()) * (p2.getX() - q2.getX())));

		// Special Cases
		// p1, q1 and p2 are colinear and p2 lies on segment p1q1
		if (o1 == 0 && onSegment(p1, p2, q1)) return p2;

		// p1, q1 and q2 are colinear and q2 lies on segment p1q1
		if (o2 == 0 && onSegment(p1, q2, q1)) return q2;

		// p2, q2 and p1 are colinear and p1 lies on segment p2q2
		if (o3 == 0 && onSegment(p2, p1, q2)) return p1;

		// p2, q2 and q1 are colinear and q1 lies on segment p2q2
		if (o4 == 0 && onSegment(p2, q1, q2)) return q1;

		throw new RootNotFound();
	}

	@Deprecated
	private Point2D getIntersectionCycleLine(Point2D p1, Point2D p2) throws RootNotFound
	{
		return getIntersection(scrToNorm(new Point2D(cycleLine.getStartX(), cycleLine.getStartY())),
				scrToNorm(new Point2D(cycleLine.getEndX(), cycleLine.getEndY())),
				p1, p2);
	}
	/*
	private Point2D semiStable(LimCycleStart l1, LimCycleStart l2, boolean isA, double a, double b) throws RootNotFound
	{
		double incT;
		boolean flipped = false;
		if(isA)
		{
			incT = (in.xMax.get() - in.xMin.get())/800;
		} else
		{
			incT = (in.yMax.get() - in.yMin.get())/800;
		}
		while(l1.st.distance(l2.st) > inc/100 && !Thread.interrupted())
		{

			LimCycleStart temp1, temp2;
			if(isA)
				a += incT;
			else
				b += incT;
			try
			{
				temp1 = updateLimCycle(l1, a, b);
				temp2 = updateLimCycle(l2, a, b);
				System.out.println("Dist: " + l1.st.distance((l2.st)));
				System.out.println("Start1: " + l1.st);
				System.out.println("(a, b): (" + a + ", " + b + ")");
				System.out.println("inc: " + incT);
				if(temp1.st.distance(temp2.st) > l1.st.distance(l2.st))
				{
					if(!flipped)
					{
						System.out.println("flipping");
						incT = -incT;
						flipped = true;
					} else throw new RootNotFound(true);
				} else
				{
					l1 = temp1;
					l2 = temp2;
				}
			} catch (RootNotFound r)
			{
				if(!r.offTheScreen)
				{
					if (isA)
					{
						a -= incT;
					} else
					{
						b -= incT;
					}
					incT = incT / 10;
					System.out.println("WENT OFF");
				}
//				else throw new RootNotFound();
			}

			if(isA)
			{
				if( incT < (in.xMax.get() - in.xMin.get())/200000)
					throw new RootNotFound();
			} else
			{
				if(incT < (in.yMax.get() - in.yMin.get())/200000)
					throw new RootNotFound();
			}

}
		System.out.println("found one!");
				return new Point2D(a, b);
				}
	 */
	@Deprecated
	void findSemiStable(Point2D start, Point2D lnSt, Point2D lnNd, double a, double b, boolean add) throws RootNotFound
	{
		System.out.println(lnSt);
		System.out.println(lnNd);
		Evaluator eval = EvaluatorFactory.getEvaluator(evalType, dx, dy);
		eval.initialise(start, 0, a, b, inc);
		LimCycleStart stbl, unstbl;
		Point2D isectStbl = null, isectUnstbl = null;
		Point2D p1 = start;
		Point2D p2 = eval.next();
		while (eval.getT() < 100 && !Thread.interrupted())
		{
			try
			{
				isectStbl = getIntersection(p1, p2, lnSt, lnNd);
				break;
			} catch (RootNotFound r)
			{
				p1 = p2;
				p2 = eval.next();
			}
		}
		eval.initialise(start,0, a, b, -inc);
		p1 = start;
		p2 = eval.next();
		while(eval.getT() < 100 && !Thread.interrupted())
		{
			try
			{
				isectUnstbl = getIntersection(p1, p2, lnSt, lnNd);
				break;
			} catch (RootNotFound r)
			{
				p1 = p2;
				p2 = eval.next();
			}
		}
		System.out.println("Initialised both");
		if(isectStbl == null || isectUnstbl == null)
			throw new RootNotFound();
		p1 = isectStbl;
		eval.initialise(isectStbl, 0, a, b, inc);
		p2 = getNextIsectLn(eval, lnSt, lnNd);
		while(p1.distance(p2) > inc/1000 && !Thread.interrupted())
		{
			p1 = p2;
			p2 = getNextIsectLn(eval, lnSt, lnNd);
			eval.resetT();
		}
		System.out.println("hopefully i get here");
		stbl = new LimCycleStart(p2, true, lnSt, lnNd);
		p1 = isectUnstbl;
		eval.initialise(isectUnstbl, 0, a, b, -inc);
		try
		{
			p2 = getNextIsectLn(eval, lnSt, lnNd);
		} catch (RootNotFound r)
		{
			System.out.println(isectUnstbl);
		}
		while(p1.distance(p2) > inc/1000 && !Thread.interrupted())
		{
			p1 = p2;
			try
			{
				p2 = getNextIsectLn(eval, lnSt, lnNd);
				eval.resetT();
			} catch (RootNotFound r)
			{
				System.out.println("well it went wrong at " + eval.getT());
				System.out.println("with the inc of: " + eval.getInc());
				throw new RootNotFound();
			}
		}

		System.out.println("def didn't get here....");
		unstbl = new LimCycleStart(p2, false, lnSt, lnNd);
		renderSemiStable(lnSt, lnNd, new Point2D(a, b), true);
	}

}
