package FXObjects;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * abstract class representing a coordinate plane. Does lots of coordinate plane stuff, but has to be extended to do
 * anything truly useful
 *
 * Class keeps track of the current selected plane with the static variable selected.
 */
public abstract class CoordPlane extends Pane
{
	/**
	 * line objects for the axes
	 */
	private final Line xAxis, yAxis;
	/**
	 * border of the plane. changes colour for selection
	 */
	private final Rectangle border;
	/**
	 * the bufferedimage we draw everything on
	 */
	protected final BufferedImage canv;
	/**
	 * the image object for rendering
	 */
	private WritableImage fxImg;
	/**
	 * imageview to display the image
	 */
	protected ImageView vw;
	/**
	 * graphics2d object for drawing on canv
	 */
	protected final Graphics2D g;
	/**
	 * red dot the displays when a thread is doing a hard task
	 */
	protected Circle loading;
	/**
	 * the canvas where everything is drawn
	 */
	protected final Canvas c;
	/**
	 * the variables holding the current limits of the axes
	 */
	protected DoubleProperty xMin, xMax, yMin, yMax;

	/**
	 * stores the initial bounds for resetting
	 */
	private final double initZoom [];
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
	protected final Rectangle zoomBox;


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
		border = new Rectangle();
		border.setX(0);
		border.setY(0);
		border.widthProperty().bind(this.widthProperty());
		border.heightProperty().bind(this.heightProperty());
		border.setStrokeWidth(2);
		this.getChildren().addAll(border);
		xMin = new SimpleDoubleProperty(-5);
		yMin = new SimpleDoubleProperty(-5);
		xMax = new SimpleDoubleProperty(5);
		yMax = new SimpleDoubleProperty(5);
		initZoom = new double [] {-5D, 5D, -5D, 5D};

		loading = new Circle();
		loading.centerXProperty().bind(this.widthProperty().subtract(5));
		loading.setCenterY(5);
		loading.setRadius(5);
		loading.setFill(Color.RED);
		loading.setStroke(Color.RED);
		loading.setVisible(false);

		this.getChildren().add(loading);

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
		//getChildren().add(c);
		c.widthProperty().bind(this.widthProperty());
		c.heightProperty().bind(this.heightProperty());

		fxImg = new WritableImage((int) side, (int) side);

		canv = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
		g = canv.createGraphics();
		vw = new ImageView();
		vw.fitWidthProperty().bind(this.widthProperty());
		vw.fitHeightProperty().bind(this.heightProperty());
		SwingFXUtils.toFXImage(canv, fxImg);
		vw.setImage(fxImg);
		getChildren().add(vw);

		xAxis = new Line();
//		xAxis.startYProperty().bind(this.heightProperty().divide(2));
//		xAxis.endYProperty().bind(this.heightProperty().divide(2));
//		xAxis.setStartX(0);
//		xAxis.endXProperty().bind(this.widthProperty());
		yAxis = new Line();
//		yAxis.startXProperty().bind(this.widthProperty().divide(2));
//		yAxis.endXProperty().bind(this.widthProperty().divide(2));
//		yAxis.setStartY(0);
//		yAxis.endYProperty().bind(this.heightProperty());
		getChildren().addAll(xAxis, yAxis);

		Font font = new Font(8);

		Text xMinLbl = new Text();
		Text xMaxLbl = new Text();
		Text yMinLbl = new Text();
		Text yMaxLbl = new Text();
		xMinLbl.setFont(font);
		xMaxLbl.setFont(font);
		yMinLbl.setFont(font);
		yMaxLbl.setFont(font);

		xMinLbl.textProperty().bind(xMin.asString());
		xMaxLbl.textProperty().bind(xMax.asString());
		yMinLbl.textProperty().bind(yMin.asString());
		yMaxLbl.textProperty().bind(yMax.asString());
		getChildren().addAll(xMinLbl, xMaxLbl, yMinLbl, yMaxLbl);


		xMinLbl.xProperty().set(2);
		xMinLbl.yProperty().bind(this.heightProperty().divide(2).subtract(2));
		xMaxLbl.xProperty().bind(this.widthProperty().subtract(xMaxLbl.textProperty().length().multiply(5)));
		xMaxLbl.yProperty().bind(this.heightProperty().divide(2).subtract(4));
		yMinLbl.xProperty().bind(this.widthProperty().divide(2).add(2));
		yMinLbl.yProperty().bind(this.heightProperty().subtract(2));
		yMaxLbl.xProperty().bind(this.widthProperty().divide(2).add(2));
		yMaxLbl.yProperty().set(10);


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
							xMaxTemp = (xMax.get() - scrToNormX(zoomBox.getX() + zoomBox.getWidth())) *
									(xMax.get() - xMin.get()) / (scrToNormX(zoomBox.getX() + zoomBox.getWidth()) -
									scrToNormX(zoomBox.getX())) + xMax.get();
							xMinTemp = (xMin.get() - scrToNormX(zoomBox.getX())) * (xMax.get() - xMin.get()) /
									(scrToNormX(zoomBox.getX() + zoomBox.getWidth()) -
											scrToNormX(zoomBox.getX())) + xMin.get();

							yMaxTemp = (yMax.get() - scrToNormY(zoomBox.getY())) * (yMax.get() - yMin.get()) /
									(scrToNormY(zoomBox.getY() - zoomBox.getHeight()) -
											scrToNormY(zoomBox.getY())) + yMax.get();
							yMinTemp = (yMin.get() - scrToNormY(zoomBox.getY() + zoomBox.getHeight())) *
									(yMax.get() - yMin.get()) / (scrToNormY(zoomBox.getY() -
									zoomBox.getHeight()) - scrToNormY(zoomBox.getY())) + yMin.get();
						} else
						{
							xMaxTemp = scrToNormX(zoomBox.getX() + zoomBox.getWidth());
							xMinTemp = scrToNormX(zoomBox.getX());
							yMaxTemp = scrToNormY(zoomBox.getY());
							yMinTemp = scrToNormY(zoomBox.getY() + zoomBox.getHeight());
						}
						zoomBox.setWidth(0);
						zoomBox.setHeight(0);
						xMin.set(xMinTemp);
						yMin.set(yMinTemp);
						xMax.set(xMaxTemp);
						yMax.set(yMaxTemp);
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

	public void resetZoom()
	{
		xMin.set(initZoom [0]);
		xMax.set(initZoom [1]);
		yMin.set(initZoom [2]);
		yMax.set(initZoom [3]);
		updateForZoom();
		draw();
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
			border.setStroke(Color.BLUE);
		}
		else
		{
			border.setStroke(Color.BLACK);
		}
	}

	/**
	 * redraws everything in the window to the canv bufferedImage.
	 */
	public void draw()
	{
		if(Thread.interrupted()) return;
		drawAxes();
		if(Thread.interrupted()) return;
		drawBorders();
	}

	/**
	 * renders the canv bufferedImage to the screen
	 */
	public void render()
	{
		Platform.runLater(() ->
		{
			synchronized (canv)
			{
				fxImg = SwingFXUtils.toFXImage(canv, fxImg);
			}
			vw.setImage(fxImg);
		});
	}

	/**
	 * draws the axes with the appropriate labels.
	 */
	public void drawAxes()
	{

		String strxMin, strxMax, stryMin, stryMax;
		strxMin = String.valueOf(xMin.get());
		strxMax = String.valueOf(xMax.get());
		stryMin = String.valueOf(yMin.get());
		stryMax = String.valueOf(yMax.get());
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


		synchronized (g)
		{
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, canv.getWidth(), canv.getHeight());
		}

		double x0 = normToScrX(0);
		synchronized (yAxis)
		{
			yAxis.setVisible(inBounds(0, scrToNormY(this.getHeight() / 2)));
			yAxis.setStartX(x0);
			yAxis.setEndX(x0);
			yAxis.setStartY(0);
			yAxis.setEndY(this.getHeight());
		}

		double y0 = normToScrY(0);
		synchronized (xAxis)
		{
			xAxis.setVisible(inBounds(scrToNormX(this.getWidth() / 2), 0));
			xAxis.setStartY(y0);
			xAxis.setEndY(y0);
			xAxis.setStartX(0);
			xAxis.setEndX(this.getWidth());
		}

//		g.drawLine(0, imgNormToScrY(0), canv.getWidth(), imgNormToScrY(0));
//		g.drawLine(imgNormToScrX(0), 0, imgNormToScrX(0), canv.getHeight());
//		g.drawString(strxMin, 2, canv.getHeight()/2 - 2);
//		g.drawString(stryMin, canv.getWidth()/2 + 2, canv.getHeight() - 2);
//		g.drawString(stryMax, canv.getWidth()/2 + 2, 12);
//		g.drawString(strxMax, canv.getWidth() - 7 * strxMax.length(), canv.getHeight()/2 - 4);

	}

	/**
	 * Checks whether the point (x, y) lies within the window
	 * @param x the x coord
	 * @param y the y coord
	 * @return true if they are in bounds, false otherwise
	 */
	protected boolean inBounds(double x, double y)
	{
		return x <= xMax.get() && x >= xMin.get() && y <= yMax.get() && y >= yMin.get();
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
		return (x / c.getWidth() + (xMin.get()/(xMax.get() - xMin.get()))) * (xMax.get() - xMin.get());
	}
	/**
	 * converts a screen (pixel) coordinate to the current mathematical coordinates.
	 * @param y the y value to convert.
	 * @return the y value in mathematical coordinates
	 */
	protected double scrToNormY(double y)
	{
		return (y / c.getHeight() + (yMax.get() / (yMin.get() - yMax.get()))) * (yMin.get() - yMax.get());
	}

	protected double imgScrToNormX(int x)
	{
		return ((double) x / (double) canv.getWidth() + (xMin.get()/(xMax.get() - xMin.get()))) * (xMax.get() - xMin.get());
	}

	protected double imgScrToNormY(int y)
	{
		return ((double) y / (double) canv.getHeight() + (yMax.get() / (yMin.get() - yMax.get()))) * (yMin.get() - yMax.get());
	}

	/**
	 * converts a screen (pixel) point to the current mathematical coordinates
	 * @param other the point to convert
	 * @return the converted point
	 */
	protected Point2D scrToNorm(Point2D other)
	{
		return new Point2D(scrToNormX(other.getX()), scrToNormY(other.getY()));
	}

	/**
	 * converts a mathematical coordinate to the screen (pixel) coordinates.
	 * @param x the x value to convert.
	 * @return the x value in screen (pixel) coordinates
	 */
	protected double normToScrX(double x)
	{
		return (x / (xMax.get() - xMin.get()) - (xMin.get()/(xMax.get() - xMin.get()))) * c.getWidth();
	}
	/**
	 * converts a mathematical coordinate to the screen (pixel) coordinates.
	 * @param y the y value to convert.
	 * @return the y value in screen (pixel) coordinates
	 */
	protected double normToScrY(double y)
	{
		return (y / (yMin.get() - yMax.get()) - (yMax.get() / (yMin.get() - yMax.get()))) * c.getHeight();
	}

	protected int imgNormToScrX(double x)
	{
		return (int) Math.round((x / (xMax.get() - xMin.get()) - (xMin.get()/(xMax.get() - xMin.get()))) * canv.getHeight());
	}
	protected int imgNormToScrY(double y)
	{
		return (int) Math.round((y / (yMin.get() - yMax.get()) - (yMax.get() / (yMin.get() - yMax.get()))) * canv.getHeight());
	}
	/**
	 * converts a mathematical point to the screen (pixel) coordinate point
	 * @param other the point to convert
	 * @return the converted point
	 */
	protected Point2D normToScr(Point2D other)
	{
		return new Point2D(normToScrX(other.getX()), normToScrY(other.getY()));
	}
	/**
	 * draws a line from (x1, y1) to (x2, y2), with the inputs in mathematical coordinates
	 * @param x1 the x coord of the first point
	 * @param y1 the y coord of the first point
	 * @param x2 the x coord of the second point
	 * @param y2 the y coord of the second point
	 * @param color the color to draw in
	 */
	protected void drawLine(double x1, double y1, double x2, double y2, java.awt.Color color)
	{
		drawLine(x1, y1, x2, y2, color, 1);
	}

	protected void drawLine(double x1, double y1, double x2, double y2, java.awt.Color color, float width)
	{
		if(!Thread.interrupted())
		Platform.runLater(() ->
		{
			synchronized (g)
			{
				g.setStroke(new BasicStroke(width));
				g.setColor(color);
				g.drawLine(imgNormToScrX(x1), imgNormToScrY(y1), imgNormToScrX(x2), imgNormToScrY(y2));
			}
		});
	}
	protected void drawLine(Point2D p1, Point2D p2, java.awt.Color color, float width)
	{
		drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), color, width);
	}

	/**
	 * draws a line between p1 and p2, with input in mathematical coordinates
	 * @param p1 the first point
	 * @param p2 the second point
	 * @param color
	 */
	protected void drawLine(Point2D p1, Point2D p2, java.awt.Color color)
	{
		drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), color);
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

	/**
	 * writes this to the provided file
	 * @param f the file to write to
	 * @return whether or not the write succeeded
	 */
	public abstract boolean writePNG(File f);


	protected java.awt.Color fromFXColor(Color c)
	{
		if(c == null) return java.awt.Color.white;
		return new java.awt.Color(
				(float) c.getRed(),
				(float) c.getGreen(),
				(float) c.getBlue(),
				(float) c.getOpacity());
	}
}
