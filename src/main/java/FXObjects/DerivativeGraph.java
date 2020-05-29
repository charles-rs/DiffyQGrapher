package FXObjects;

import AST.Derivative;
import Exceptions.EvaluationException;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

/**
 * class to represent graphs of individual derivatives.
 */

public class DerivativeGraph extends CoordPlane
{
	/**
	 * the derivative to graph
	 */
	private final Derivative derivative;
	/**
	 * inc: the increment to draw with
	 * a, b, x, y, t: the current input variables.
	 */
	private final double inc, a, b;
	private double x, y, t;
	/**
	 * the variable we are graphing with respect to
	 */
	private final char var;
	/**
	 * the text fields for input
	 */
	private TextField xInput, yInput, tInput;
	public DerivativeGraph(double side, Derivative d, char var, double a, double b, double x, double y, double t,
						   TextField xInput, TextField yInput, TextField tInput)
	{
		super(side);
		derivative = d;
		inc = (xMax - xMin) / 5000.;
		this.var = var;
		this.a = a;
		this.b = b;
		this.x = x;
		this.y = y;
		this.t = t;
		this.xInput = xInput;
		this.yInput = yInput;
		this.tInput = tInput;
		//TODO: make these grey out better, currently hard to tell they aren't editable and feels like a bug
		switch (var)
		{
			case 'x':
				xInput.setEditable(false);
				break;
			case 'y':
				yInput.setEditable(false);
				break;
			case 't':
				tInput.setEditable(false);
				break;
		}
		xInput.setOnKeyPressed((k) ->
		{
			if(k.getCode() == KeyCode.ENTER)
			{
				try
				{
					this.x = Double.parseDouble(xInput.getText());
				} catch (NumberFormatException ignored){}
				draw();
			}
		});
		yInput.setOnKeyPressed((k) ->
		{
			if(k.getCode() == KeyCode.ENTER)
			{
				try
				{
					this.y = Double.parseDouble(yInput.getText());
				} catch (NumberFormatException ignored) {}
				draw();
			}
		});
		tInput.setOnKeyPressed((k) ->
		{
			if(k.getCode() == KeyCode.ENTER)
			{
				try
				{
					this.t = Double.parseDouble(tInput.getText());
				} catch (NumberFormatException ignored) {}
				draw();
			}
		});
	}

	@Override
	protected void updateForZoom(){}

	/**
	 * draws by moving along the axis and calculating a value. Linearly interpolates in segments of size inc
	 */
	@Override
	public void draw()
	{
		super.draw();
		try
		{
			double increment = xMin;
			double temp, next;
			switch (var)
			{
				case 'x':
					temp = derivative.eval(increment, y, a, b, t);
					increment += inc;
					next = derivative.eval(increment, y, a, b, t);
					while(increment <= xMax)
					{
						gc.strokeLine(normToScrX(increment - inc), normToScrY(temp),
								normToScrX(increment), normToScrY(next));
						temp = next;
						increment += inc;
						next = derivative.eval(increment, y, a, b, t);
					}
					break;
				case 'y':
					temp = derivative.eval(x, increment, a, b, t);
					increment += inc;
					next = derivative.eval(x, increment, a, b, t);
					while(increment <= xMax)
					{
						gc.strokeLine(normToScrX(increment - inc), normToScrY(temp),
								normToScrX(increment), normToScrY(next));
						temp = next;
						increment += inc;
						next = derivative.eval(x, increment, a, b, t);
					}
					break;
				case 't':
					temp = derivative.eval(x, y, a, b, increment);
					increment += inc;
					next = derivative.eval(x, y, a, b, increment);
					while(increment <= xMax)
					{
						gc.strokeLine(normToScrX(increment - inc), normToScrY(temp),
								normToScrX(increment), normToScrY(next));
						temp = next;
						increment += inc;
						next = derivative.eval(x, y, a, b, increment);
					}
					break;
			}
		} catch (EvaluationException ignored){}
	}

	@Override
	public void handleMouseClick(MouseEvent e)
	{}
}
