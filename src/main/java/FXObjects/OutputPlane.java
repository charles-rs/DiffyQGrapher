package FXObjects;

import AST.Derivative;
import Evaluation.CriticalPoint;
import Evaluation.EvalType;
import Evaluation.Evaluator;
import Evaluation.EvaluatorFactory;
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
	private List<CriticalPoint> criticalPoints;
	private List<Node> needsReset;
	double inc = .01;
	private double a, b;
	private Derivative dx, dy;
	public EvalType evalType;
	public ClickModeType clickMode = ClickModeType.DRAWPATH;


	public OutputPlane(double side, TextField tField)
	{
		super(side);
		evalType = EvalType.RungeKutta;
		initials = new LinkedList<>();
		criticalPoints = new LinkedList<>();
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

	public void clearObjects()
	{
		criticalPoints.clear();
		for(Node n : needsReset) n.setVisible(false);
		needsReset.clear();
	}
	public void updateA(double a)
	{
		this.a = a;
	}

	public void updateB(double b)
	{
		this.b = b;
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
		switch(clickMode)
		{
			case DRAWPATH:
				double x = scrToNormX(e.getX());
				double y = scrToNormY(e.getY());
				initCond temp = new initCond(x, y, t);
				initials.add(temp);
				drawGraph(temp);
				break;
			case FINDCRITICAL:
				Point2D start = new Point2D(scrToNormX(e.getX()), scrToNormY(e.getY()));
				try
				{
					CriticalPoint root = EvaluatorFactory.getEulerEval(dx, dy).findCritical(start, a, b, t);
					criticalPoints.add(root);
					drawGraphs();
				} catch (RootNotFound r)
				{
					System.out.println("Root not found");
				}

		}

	}

	private void labelCritical(CriticalPoint p)
	{
		if(inBounds(p.point))
		{
			c.getGraphicsContext2D().setFill(Color.RED);
			c.getGraphicsContext2D().fillOval(normToScrX(p.point.getX()) - 2.5, normToScrY(p.point.getY()) - 2.5, 5, 5);
			c.getGraphicsContext2D().setFill(Color.BLACK);

			Label text = new Label(p.type.getStringRep());
			text.setPadding(new Insets(2));
			text.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
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
			text.setVisible(true);
			needsReset.add(text);
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
			drawGraph(i);
		}
		for(CriticalPoint p : criticalPoints)
		{
			labelCritical(p);
		}
	}
	private void drawGraph(initCond init)
	{
		double xTemp, yTemp, x, y;
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
		drawArrow(x, y, initialDir.getX(), initialDir.getY());
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
		/*for(double ti = t; ti < 100 + t; ti+= inc)
		{
			//if(x > xMax || x < xMin || y > yMax || y < yMin) break;
			nextDir = eval.evaluate(x, y, a, b, t, inc);
			xTemp = x + inc * nextDir.getX();
			yTemp = y + inc * nextDir.getY();
			if(inBounds(x, y) || inBounds(xTemp, yTemp))
				gc.strokeLine(normToScrX(x), normToScrY(y), normToScrX(xTemp), normToScrY(yTemp));
			x = xTemp;
			y = yTemp;
		}
		x = init.x;
		y = init.y;
		for(double ti = t; ti > -100 + t; ti-= inc)
		{
			//if(x > xMax || x < xMin || y > yMax || y < yMin) break;
			nextDir = eval.evaluate(x, y, a, b, t, inc);
			xTemp = x - inc * nextDir.getX();
			yTemp = y - inc * nextDir.getY();
			gc.strokeLine(normToScrX(x), normToScrY(y), normToScrX(xTemp), normToScrY(yTemp));
			x = xTemp;
			y = yTemp;
		}*/
	}
	private boolean inBounds(double x, double y)
	{
		return x <= xMax && x >= xMin && y <= yMax && y >= yMin;
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


	public void clear()
	{
		initials.clear();
		criticalPoints.clear();
		draw();
	}
	@Override
	public void draw()
	{
		super.draw();
		drawGraphs();
	}

	public void resetZoom()
	{
		xMin = -5;
		xMax = 5;
		yMin = -5;
		yMax = 5;
		draw();
	}

	private class initCond
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
