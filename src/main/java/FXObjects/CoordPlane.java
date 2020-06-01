package FXObjects;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * abstract class representing a coordinate plane. Does lots of coordinate plane stuff, but has to be extended to do
 * anything truly useful
 *
 * Class keeps track of the current selected plane with the static variable selected.
 */
public abstract class CoordPlane extends Pane
{
	/**
	 * the canvas where everything is drawn
	 */
	protected final Canvas c;
	/**
	 * the variables holding the current limits of the axes
	 */
	protected double xMin, xMax, yMin, yMax;
	/**
	 * this is just and alias for c.getGraphicsContext2D() since we use it a lot
	 */
	protected final GraphicsContext gc;
	/**
	 * the currently active plane
	 */
	static public CoordPlane selected;
	/**
	 * The keycodes for navigation. changed in settings
	 */
	protected KeyCode right, left, up, down;
	/**
	 * The rectangle object that is shown during zooming
	 */
	protected Rectangle zoomBox;


	/**
	 * Constructor for a CoordPlane with the provided side length.
	 * Initialises the edges to 5 and -5 for both variables.
	 * Sets the navigation keys to the arrow keys.
	 * Binds the canvas to the dimensions of the plane.
	 * Event filter:
	 * takes mouse input, checks if the current window is selected, and if so zooms.
	 * if ctrl is held, zooms out, otherwise zooms in.
	 * If the drawn zoom box is too small, the program assumes that the user meant to click, but just to be safe throws
	 * away the event and lets the user try again.
	 * The zoomBox is only visible during zooming so that it isn't annoying in the way
	 * @param side the side length (in pixels)
	 */
	public CoordPlane (double side)
	{
		xMin = -5;
		yMin = -5;
		xMax = 5;
		yMax = 5;

		right = KeyCode.RIGHT;
		left = KeyCode.LEFT;
		up = KeyCode.UP;
		down = KeyCode.DOWN;

		setMinSize(100, 100);
		setWidth(side);
		setHeight(side);

		setPrefHeight(side);
		setPrefWidth(side);
		c = new Canvas(side, side);
		gc = c.getGraphicsContext2D();
		getChildren().add(c);
		c.widthProperty().bind(this.widthProperty());
		c.heightProperty().bind(this.heightProperty());

		/*
		this.widthProperty().addListener((obs, oldVal, newVal) ->
		{
			setHeight((Double) newVal);
			if(oldVal.doubleValue() != newVal.doubleValue())
			{
				System.out.println(oldVal);
				System.out.println(newVal);
				drawAxes();
			}
		});
		this.heightProperty().addListener((obs, oldVal, newVal) ->
		{
			setWidth((Double) newVal);
			if(oldVal.doubleValue() != newVal.doubleValue())
				drawAxes();
		});*/
		zoomBox = new Rectangle(0,0,0,0);
		zoomBox.setVisible(false);
		zoomBox.setStroke(Color.BLACK);
		zoomBox.setFill(Color.TRANSPARENT);
		this.getChildren().add(zoomBox);
		zoomBox.toFront();

		addEventFilter(MouseEvent.ANY, mouseEvent ->
		{
			if(this == selected)
			{

				if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED)
				{
					zoomBox.setX(mouseEvent.getX());
					zoomBox.setY(mouseEvent.getY());
					zoomBox.setVisible(true);

				}
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED && zoomBox.isVisible())
				{
					zoomBox.setWidth(mouseEvent.getX() - zoomBox.getX());
					zoomBox.setHeight(mouseEvent.getY() - zoomBox.getY());

				}
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED && zoomBox.getHeight() != 0
						&& zoomBox.getWidth() != 0)
				{
					zoomBox.setVisible(false);
					if(zoomBox.getWidth() > 5 && zoomBox.getHeight() > 5)
					{
						double xMinTemp, xMaxTemp, yMinTemp, yMaxTemp;
						if(mouseEvent.isControlDown())
						{
							xMaxTemp = (xMax - scrToNormX(zoomBox.getX() + zoomBox.getWidth())) * (xMax - xMin) / (scrToNormX(zoomBox.getX() + zoomBox.getWidth()) - scrToNormX(zoomBox.getX())) + xMax;
							xMinTemp = (xMin - scrToNormX(zoomBox.getX())) * (xMax - xMin) / (scrToNormX(zoomBox.getX() + zoomBox.getWidth()) - scrToNormX(zoomBox.getX())) + xMin;

							yMaxTemp = (yMax - scrToNormY(zoomBox.getY())) * (yMax - yMin) / (scrToNormY(zoomBox.getY() - zoomBox.getHeight()) - scrToNormY(zoomBox.getY())) + yMax;
							yMinTemp = (yMin - scrToNormY(zoomBox.getY() + zoomBox.getHeight())) * (yMax - yMin) / (scrToNormY(zoomBox.getY() - zoomBox.getHeight()) - scrToNormY(zoomBox.getY())) + yMin;
						} else
						{
							xMaxTemp = scrToNormX(zoomBox.getX() + zoomBox.getWidth());
							xMinTemp = scrToNormX(zoomBox.getX());
							yMaxTemp = scrToNormY(zoomBox.getY());
							yMinTemp = scrToNormY(zoomBox.getY() + zoomBox.getHeight());
						}
						zoomBox.setWidth(0);
						zoomBox.setHeight(0);
						xMin = xMinTemp;
						yMin = yMinTemp;
						xMax = xMaxTemp;
						yMax = yMaxTemp;
						updateForZoom();
						draw();
					}
					mouseEvent.consume();
				}
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED)
				{
					handleMouseClick(mouseEvent);
				}
			}
			else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED)
			{
				selectMe(mouseEvent);
			}
		});
		setVisible(true);
		c.setVisible(true);

	}

	/**
	 * Runs right before the redraw that happens after a zoom event. This provides subclasses a way to do important
	 * things that are dependent on the current dimensions. Runs as late as it does so that the dimensions of the
	 * current window are already in their zoomed state.
	 */
	protected abstract void updateForZoom();

	/**
	 * selects the current window. Also bothers with shifting the JavaFX focus.
	 * @param e is the mouse event that fired the selection. Consumes so that no action is taken in the newly selected
	 *          window.
	 */
	protected void selectMe(MouseEvent e)
	{
			requestFocus();
			CoordPlane temp = selected;
			selected = this;
			e.consume();
			if(temp != null)
				temp.drawBorders();
			drawBorders();
	}

	/**
	 * draws the borders of the window. Black for unselected windows, and blue for the selected one
	 */
	public void drawBorders()
	{
		if(this == selected)
		{
			gc.setFill(Color.BLUE);
		}
		gc.fillRect(0,0,2,c.getHeight());
		gc.fillRect(0,0,c.getWidth(),2);
		gc.fillRect(0, c.getHeight() - 2, c.getWidth(), 2);
		gc.fillRect(c.getWidth() - 2, 0, 2, c.getHeight());
		if(this == selected) gc.setFill(Color.BLACK);
	}

	/**
	 * redraws everything in the window. There may be a time when it is optimised to only redraw the necessary parts
	 * of the window, but it hasn't been a performance issue.
	 */
	public void draw()
	{
		drawAxes();
		drawBorders();
	}

	/**
	 * draws the axes with the appropriate labels.
	 */
	public void drawAxes()
	{
		String strxMin, strxMax, stryMin, stryMax;
		strxMin = String.valueOf(xMin);
		strxMax = String.valueOf(xMax);
		stryMin = String.valueOf(yMin);
		stryMax = String.valueOf(yMax);
		try
		{
			strxMin = strxMin.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		try
		{
			strxMax = strxMax.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		try
		{
			stryMin = stryMin.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		try
		{
			stryMax = stryMax.substring(0, 8);
		} catch (StringIndexOutOfBoundsException ignored) {}
		gc.clearRect(0, 0, c.getWidth(), c.getHeight());
		gc.strokeLine(0, normToScrY(0), c.getWidth(), normToScrY(0));
		gc.strokeLine(normToScrX(0), 0, normToScrX(0), c.getHeight());
		gc.fillText(strxMin, 2, c.getHeight()/2 - 2);
		gc.fillText(stryMin, c.getWidth()/2 + 2, c.getHeight() - 2);
		gc.fillText(stryMax, c.getWidth()/2 + 2, 12);
		gc.fillText(strxMax, c.getWidth() - 7 * strxMax.length(), c.getHeight()/2 - 4);
	}

	/**
	 * Checks whether the point (x, y) lies within the window
	 * @param x the x coord
	 * @param y the y coord
	 * @return true if they are in bounds, false otherwise
	 */
	protected boolean inBounds(double x, double y)
	{
		return x <= xMax && x >= xMin && y <= yMax && y >= yMin;
	}

	/**
	 * determines whether or not the point p is in bounds in the window.
	 * @param p the point to check
	 * @return true if inside, false if outside.
	 */
	protected boolean inBounds(Point2D p)
	{
		return inBounds(p.getX(), p.getY());
	}

	/**
	 * takes appropriate action when a mouseevent comes through.
	 * @param e the mouseevent to pass along
	 */
	public abstract void handleMouseClick(MouseEvent e);

	/**
	 * converts a screen (pixel) coordinate to the current mathematical coordinates.
	 * @param x the x value to convert.
	 * @return the x value in mathematical coordinates
	 */
	protected double scrToNormX(double x)
	{
		return (x / c.getWidth() + (xMin/(xMax - xMin))) * (xMax - xMin);
	}
	/**
	 * converts a screen (pixel) coordinate to the current mathematical coordinates.
	 * @param y the y value to convert.
	 * @return the y value in mathematical coordinates
	 */
	protected double scrToNormY(double y)
	{
		return (y / c.getHeight() + (yMax / (yMin - yMax))) * (yMin - yMax);
	}

	/**
	 * converts a mathematical coordinate to the screen (pixel) coordinates.
	 * @param x the x value to convert.
	 * @return the x value in screen (pixel) coordinates
	 */
	protected double normToScrX(double x)
	{
		return (x / (xMax - xMin) - (xMin/(xMax - xMin))) * c.getWidth();
	}
	/**
	 * converts a mathematical coordinate to the screen (pixel) coordinates.
	 * @param y the y value to convert.
	 * @return the y value in screen (pixel) coordinates
	 */
	protected double normToScrY(double y)
	{
		return (y / (yMin - yMax) - (yMax / (yMin - yMax))) * c.getHeight();
	}

	/**
	 * draws a line from (x1, y1) to (x2, y2), with the inputs in mathematical coordinates
	 * @param x1 the x coord of the first point
	 * @param y1 the y coord of the first point
	 * @param x2 the x coord of the second point
	 * @param y2 the y coord of the second point
	 */
	protected void drawLine(double x1, double y1, double x2, double y2)
	{
		Platform.runLater(() ->
		{
			gc.strokeLine(normToScrX(x1), normToScrY(y1), normToScrX(x2), normToScrY(y2));
		});
	}

	/**
	 * draws a line between p1 and p2, with input in mathematical coordinates
	 * @param p1 the first point
	 * @param p2 the second point
	 */
	protected void drawLine(Point2D p1, Point2D p2)
	{
		drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	/**
	 * Draws a line with mathematical coordinates on another canvas
	 * @param x1 the x coord of the first point
	 * @param y1 the y coord of the first point
	 * @param x2 the x coord of the second point
	 * @param y2 the y coord of the second point
	 * @param can the canvas to draw on
	 */
	protected void drawLine(double x1, double y1, double x2, double y2, Canvas can)
	{
		can.getGraphicsContext2D().strokeLine(normToScrX(x1), normToScrY(y1), normToScrX(x2), normToScrY(y2));
	}

	/**
	 * Draws a line with mathematical coordinates from Point2Ds on another canvas
	 * @param p1 the first point
	 * @param p2 the second point
	 * @param can the canvas to draw on
	 */
	protected void drawLine(Point2D p1, Point2D p2, Canvas can)
	{
		drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), can);
	}

	/**
	 * Clears the coordinate plane and redraws
	 */
	public abstract void clear();
}
