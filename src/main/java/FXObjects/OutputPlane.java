package FXObjects;

import AST.Derivative;
import AST.Maths;
import AST.Value;
import Evaluation.*;
import Exceptions.EvaluationException;
import Exceptions.RootNotFound;
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
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

import java.util.LinkedList;
import java.util.List;


public class OutputPlane extends CoordPlane
{
	private double t = 0;
	private List<initCond> initials;
	private List<initCond> isoclines;
	private List<CriticalPoint> criticalPoints;
	private List<Node> needsReset;
	double inc = .01;
	private double a, b;
	private Derivative dx, dy;
	public EvalType evalType;
	public ClickModeType clickMode = ClickModeType.DRAWPATH;
	private boolean drawSep = false;

	private Color solutionColor = Color.BLACK;
	private Color isoclineColor = Color.BLUE;
	private Color separatrixColor = Color.LIMEGREEN;
	private Color criticalColor = Color.RED;


	public OutputPlane(double side, TextField tField)
	{
		super(side);
		evalType = EvalType.RungeKutta;
		initials = new LinkedList<>();
		criticalPoints = new LinkedList<>();
		isoclines = new LinkedList<>();
		needsReset = new LinkedList<>();
		draw();
		tField.setText(Double.toString(t));
		tField.setOnKeyPressed((e) ->
		{
			if(e.getCode() == KeyCode.ENTER)
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
			if(e.getCode() == left)
			{
				xMin -= (xMax - xMin)/20;
				xMax -= (xMax - xMin)/20;
			}
			else if(e.getCode() == right)
			{
				xMin += (xMax - xMin)/20;
				xMax += (xMax - xMin)/20;
			}
			else if(e.getCode() == up)
			{
				yMin += (yMax - yMin)/20;
				yMax += (yMax - yMin)/20;
			}
			else if(e.getCode() == down)
			{
				yMin -= (yMax - yMin)/20;
				yMax -= (yMax - yMin)/20;
			}
			KeyCode temp = e.getCode();
			if(temp == left || temp == right || temp == up || temp == down)
			{
				draw();
				e.consume();
			}
		});
	}

	public Derivative getDx()
	{
		return (Derivative) dx.clone();
	}
	public Derivative getDy()
	{
		return (Derivative) dy.clone();
	}


	public void clearObjects()
	{
		criticalPoints.clear();
		for(Node n : needsReset) n.setVisible(false);
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
		initCond temp = new initCond(x, y, t);
		switch(clickMode)
		{
			case DRAWPATH:
				initials.add(temp);
				drawGraph(temp, true);
				break;
			case FINDCRITICAL:
				Point2D start = new Point2D(x, y);
				try
				{
					CriticalPoint root = EvaluatorFactory.getEulerEval(dx, dy).findCritical(start, a, b, t);
					criticalPoints.add(root);
					drawGraphs();
				} catch (RootNotFound r)
				{
					//TODO better output system
					System.out.println("Root not found");
				}
				break;
			case DRAWISO:
				isoclines.add(temp);
				drawIso(temp);
				break;
		}

	}

	private CriticalPoint critical(Point2D start) throws RootNotFound
	{
		return EvaluatorFactory.getEulerEval(dx, dy).findCritical(start, a, b, t);
	}
	private void updateCritical()
	{
		List<CriticalPoint> temp = new LinkedList<>();
		for (CriticalPoint c : criticalPoints)
		{
			try
			{
				temp.add(critical(c.point));
			} catch (RootNotFound ignored) {}
		}
		criticalPoints = temp;
	}

	private void labelCritical(CriticalPoint p)
	{
		if(inBounds(p.point))
		{
			c.getGraphicsContext2D().setFill(criticalColor);
			c.getGraphicsContext2D().fillOval(normToScrX(p.point.getX()) - 2.5, normToScrY(p.point.getY()) - 2.5, 5, 5);


			Label text = new Label(p.type.getStringRep());
			text.setPadding(new Insets(2));
			text.setBorder(new Border(new BorderStroke(criticalColor, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
			this.getChildren().add(text);
			text.setLayoutX(normToScrX(p.point.getX()) + 8);
			text.setLayoutY(normToScrY(p.point.getY()) - 24);
			final Text t = new Text(text.getText());

			if(text.getLayoutY() < 0)
			{
				text.setLayoutY(normToScrY(p.point.getY()) + 4);
			}
			if(text.getLayoutX() + t.getLayoutBounds().getWidth() + 4 > this.getWidth())
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
		for(Node n : needsReset)
		{
			n.setVisible(false);
		}
		needsReset.clear();
		for(initCond i : initials)
		{
			drawGraph(i, true);
		}
		for(CriticalPoint p : criticalPoints)
		{
			labelCritical(p);
		}
	}
	private void drawGraph(initCond init, boolean arrow)
	{
		gc.setStroke(solutionColor);
		double x, y;
		x = init.x;
		y = init.y;
		t = init.t;
		Evaluator eval;
		switch (evalType)
		{
			case Euler:
				eval = EvaluatorFactory.getEulerEval(dx, dy);
				break;
			case MidEuler:
				eval = EvaluatorFactory.getEulerMidEval(dx, dy);
				break;
			case RungeKutta:
			default:
				eval = EvaluatorFactory.getRungeKuttaEval(dx, dy);
		}
		Point2D initialDir = eval.evaluate(x, y, a, b, t, inc);
		if(arrow) drawArrow(x, y, initialDir.getX(), initialDir.getY());
		Point2D prev;
		Point2D next;
		eval.initialise(x, y, t, a, b, inc);
		prev = new Point2D(x, y);

		while(eval.getT() < 100 + t)
		{
			next = eval.next();
			if(inBounds(prev) || inBounds(next))
				gc.strokeLine(normToScrX(prev.getX()), normToScrY(prev.getY()), normToScrX(next.getX()), normToScrY(next.getY()));
			prev = next;
		}
		eval.initialise(x, y, t, a, b, -inc);
		prev = new Point2D(x, y);
		while(eval.getT() > t - 100)
		{
			next = eval.next();
			if(inBounds(prev) || inBounds(next))
				gc.strokeLine(normToScrX(prev.getX()), normToScrY(prev.getY()), normToScrX(next.getX()), normToScrY(next.getY()));
			prev = next;
		}
		gc.setStroke(Color.BLACK);
	}
	private void drawIsoclines()
	{
		for (initCond c : isoclines)
		{
			drawIso(c);
		}
	}
	private void drawIso(initCond init)
	{
		gc.setStroke(isoclineColor);
		try
		{
			Point2D first, second;
			first = new Point2D(init.x, init.y);
			double inc = (xMax - xMin)/c.getWidth();
			double val = dy.eval(init.x, init.y, a, b, t) / dx.eval(init.x, init.y, a, b, t);
			AST.Node slope = Maths.minus(Maths.divide(dy, dx), new Value(val));
			AST.Node slopeDeriv = slope.differentiate('y');
			AST.Node thing = Maths.divide(slope, slopeDeriv);
			while(inBounds(first))
			{
				double x = first.getX() + inc;
				double yOld = first.getY();
				double y = first.getY();
				for(int i = 0; i < 10; i++)
				{
					yOld = y;
					y = y - thing.eval(x, y, a, b, t);
				}
				if(Math.abs(y - yOld) < .00001)
				{
					second = new Point2D(x, y);
					gc.strokeLine(normToScrX(first.getX()), normToScrY(first.getY()), normToScrX(second.getX()), normToScrY(second.getY()));
					first = second;
				} else break;
			}
			first = new Point2D(init.x, init.y);
			while(inBounds(first))
			{
				double x = first.getX() - inc;
				double yOld = first.getY();
				double y = first.getY();
				for(int i = 0; i < 10; i++)
				{
					yOld = y;
					y = y - thing.eval(x, y, a, b, t);
				}
				if(Math.abs(y - yOld) < .00001)
				{
					second = new Point2D(x, y);
					gc.strokeLine(normToScrX(first.getX()), normToScrY(first.getY()), normToScrX(second.getX()), normToScrY(second.getY()));
					first = second;
				} else break;
			}
		} catch (EvaluationException ignored){}
		gc.setStroke(Color.BLACK);
	}

	private boolean inBounds(Point2D p)
	{
		return inBounds(p.getX(), p.getY());
	}
	private void drawArrow(double x, double y, double dx, double dy)
	{
		double angle = Math.toDegrees(Math.atan(-dy/dx));
		if (dx < 0) angle += 180;
		double xScr = normToScrX(x);
		double yScr = normToScrY(y);
		gc.save();
		gc.transform(new Affine(new Rotate(angle, xScr, yScr)));
		gc.strokeLine(xScr, yScr, xScr - 5, yScr + 3);
		gc.strokeLine(xScr, yScr, xScr - 5, yScr - 3);
		gc.restore();

	}

	public void drawSeparatrices()
	{
		drawSep = true;
		draw();
	}

	public void clear()
	{
		initials.clear();
		criticalPoints.clear();
		isoclines.clear();
		draw();
	}
	@Override
	public void draw()
	{
		super.draw();
		updateCritical();
		drawGraphs();
		drawIsoclines();
		if(drawSep)
		{
			double tol = .000000001;
			gc.setStroke(separatrixColor);
			for (CriticalPoint c : criticalPoints)
			{
				try
				{
					if (c.type == CritPointTypes.SADDLE)
					{
						initCond point1 = new initCond(c.point.getX() + tol * c.matrix.getEigenVector(0).get(0),
								c.point.getY() + tol * c.matrix.getEigenVector(0).get(1), t);

						initCond point2 = new initCond(c.point.getX() + tol * c.matrix.getEigenVector(1).get(0),
								c.point.getY() + tol * c.matrix.getEigenVector(1).get(1), t);
						drawGraph(point1, false);
						drawGraph(point2, false);
						initCond point3 = new initCond(c.point.getX() - tol * c.matrix.getEigenVector(0).get(0),
								c.point.getY() - tol * c.matrix.getEigenVector(0).get(1), t);
						initCond point4 = new initCond(c.point.getX() - tol * c.matrix.getEigenVector(1).get(0),
								c.point.getY() - tol * c.matrix.getEigenVector(1).get(1), t);
						drawGraph(point3, false);
						drawGraph(point4, false);
					}
				} catch (NullPointerException ignored){}
			}
			gc.setStroke(Color.BLACK);

		}

	}

	public void resetZoom()
	{
		xMin = -5;
		xMax = 5;
		yMin = -5;
		yMax = 5;
		draw();
	}

	private static class initCond
	{
		public double x, y, t;
		public initCond(double x, double y, double t)
		{
			this.x = x;
			this.y = y;
			this.t = t;
		}
	}

}
