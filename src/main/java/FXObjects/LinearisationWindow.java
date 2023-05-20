package FXObjects;

import Evaluation.CriticalPoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.ejml.data.Complex_F64;

public class LinearisationWindow extends Stage {
    private static final Tooltip click_to_copy = new Tooltip("Click to copy LaTeX");
    private static final Tooltip copied = new Tooltip("Copied!");

    public LinearisationWindow(CriticalPoint c) {
        setTitle("Linearisation of the " + c.type.getStringRep() + " (" + c.point.getX() + ", " + c.point.getY() + ")");

        GridPane main = new GridPane();
        main.setPadding(new Insets(5));
        main.setVgap(10);
        HBox jacob = new HBox(), eig = new HBox(), pt = new HBox();
        Matrix jacobian = new Matrix(c.jacob.get(0, 0), c.jacob.get(0, 1),
                c.jacob.get(1, 0), c.jacob.get(1, 1));
        Label lblJacob = new Label("Jacobian:");
        jacob.getChildren().addAll(jacobian);
        jacob.setAlignment(Pos.CENTER);
        jacob.setSpacing(5);
        Label lblEig = new Label("Diagonalisation:");
        Matrix eigMat = new Matrix(toString(c.matrix.getEigenvalue(0)), "0",
                "0", toString(c.matrix.getEigenvalue(1)));
        eig.getChildren().addAll(eigMat);
        Label lblPt = new Label("Point:");
        Point point = new Point(c.point.getX(), c.point.getY());
        pt.getChildren().addAll(point);
        GridPane.setConstraints(lblJacob, 0, 0);
        GridPane.setConstraints(lblEig, 0, 1);
        GridPane.setConstraints(lblPt, 0, 2);
        GridPane.setConstraints(jacob, 1, 0);
        GridPane.setConstraints(eig, 1, 1);
        GridPane.setConstraints(pt, 1, 2);
        main.getChildren().addAll(lblJacob, jacob, lblEig, eig, lblPt, pt);


        Scene scene = new Scene(main);
        setScene(scene);
        show();
    }

    private static class Matrix extends Pane {
        private String vals[];
        private Line left, right, topLeft, topRight, bottomLeft, bottomRight;
        private Label l11, l12, l21, l22;
        private HBox main;
        private GridPane grid;
        private Group leftP, rightP;

        public Matrix(double a, double b, double c, double d) {
            this(String.valueOf(a), String.valueOf(b), String.valueOf(c), String.valueOf(d));
        }

        public Matrix(String x11, String x12, String x21, String x22) {
            System.out.println("constructing matrix with: " + x11 + ", " + x12 +
                    ", " + x21 + ", " + x22);
            main = new HBox();
            getChildren().add(main);
            grid = new GridPane();
            left = new Line();
            right = new Line();
            topLeft = new Line();
            topRight = new Line();
            bottomLeft = new Line();
            bottomRight = new Line();
            leftP = new Group();
            rightP = new Group();
            leftP.getChildren().addAll(left, topLeft, bottomLeft);
            rightP.getChildren().addAll(right, topRight, bottomRight);
            left.setStrokeWidth(2);
            right.setStrokeWidth(2);
            main.setSpacing(5);
            vals = new String[]{x11, x12, x21, x22};
            l11 = new Label(x11);
            l12 = new Label(x12);
            l21 = new Label(x21);
            l22 = new Label(x22);
            GridPane.setConstraints(l11, 0, 0);
            GridPane.setConstraints(l12, 1, 0);
            GridPane.setConstraints(l21, 0, 1);
            GridPane.setConstraints(l22, 1, 1);
            grid.setHgap(5);
            grid.setVgap(5);
            grid.getChildren().addAll(l11, l12, l21, l22);

//			setHeight(l11.getHeight() + l21.getHeight());
//			setWidth(Math.max(l11.getWidth() + l12.getWidth(), l21.getWidth() + l22.getWidth()));

            left.setStartX(0);
            left.setEndX(0);
            left.startYProperty().bind(grid.heightProperty());
            left.setEndY(0);
            topLeft.startXProperty().bind(left.startXProperty());
            topLeft.endXProperty().bind(left.startXProperty().add(4));
            topLeft.setStartY(0);
            topLeft.setEndY(0);
            bottomLeft.startXProperty().bind(left.endXProperty());
            bottomLeft.endXProperty().bind(left.endXProperty().add(4));
            bottomLeft.startYProperty().bind(left.startYProperty());
            bottomLeft.endYProperty().bind(left.startYProperty());
            right.startXProperty().bind(grid.widthProperty());
            right.endXProperty().bind(grid.widthProperty());
            right.startYProperty().bind(grid.heightProperty());
            right.setEndY(0);
            topRight.startXProperty().bind(right.startXProperty());
            topRight.endXProperty().bind(right.startXProperty().subtract(4));
            topRight.startYProperty().set(0);
            topRight.endYProperty().set(0);
            bottomRight.startXProperty().bind(right.startXProperty());
            bottomRight.endXProperty().bind(right.startXProperty().subtract(4));
            bottomRight.startYProperty().bind(right.startYProperty());
            bottomRight.endYProperty().bind(right.startYProperty());
            Tooltip.install(main, click_to_copy);
            setOnMouseClicked(e ->
            {
                Tooltip.install(main, copied);
                Tooltip.uninstall(main, click_to_copy);
                copied.setShowDuration(new Duration(2000));
                copied.show(main, 0, 0);
                ClipboardContent temp = new ClipboardContent();
                temp.putString("\\begin{bmatrix}\n" + vals[0] + " & " + vals[1] + " \\\\ " + vals[2] + " & " + vals[3] + "\n\\end{bmatrix}");
                Clipboard.getSystemClipboard().setContent(temp);
            });
            main.getChildren().addAll(leftP, grid, rightP);
        }
    }

    private static class Point extends Pane {
        private double x, y;
        private QuadCurve left, right;
        private Label lblX, lblY;
        private VBox mainV;
        private HBox mainH;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
            lblX = new Label(String.valueOf(x));
            lblY = new Label(String.valueOf(y));
            mainH = new HBox();
            mainV = new VBox();
            getChildren().addAll(mainH);
            left = new QuadCurve();
            right = new QuadCurve();
            mainH.getChildren().addAll(left, mainV, right);
            mainV.getChildren().addAll(lblX, lblY);
            left.startXProperty().set(4);
            left.startYProperty().set(0);
            left.controlXProperty().set(0);
            left.controlYProperty().bind(heightProperty().divide(2));
            left.endXProperty().set(4);
            left.endYProperty().bind(heightProperty());
            right.startXProperty().bind(widthProperty().subtract(4));
            right.startYProperty().set(0);
            right.controlXProperty().bind(widthProperty());
            right.controlYProperty().bind(heightProperty().divide(2));
            right.endXProperty().bind(widthProperty().subtract(4));
            right.endYProperty().bind(heightProperty());
            Tooltip.install(mainH, click_to_copy);

            setOnMouseClicked(e ->
            {
                Tooltip.install(mainH, copied);
                Tooltip.uninstall(mainH, click_to_copy);
                copied.setShowDuration(new Duration(2000));
                copied.show(mainH, 0, 0);
                ClipboardContent temp = new ClipboardContent();
                temp.putString("\\begin{pmatrix}\n" + x + " \\\\ " + y + "\n\\end{pmatrix}");
                Clipboard.getSystemClipboard().setContent(temp);
            });
        }
    }

    private String toString(Complex_F64 num) {
        if (num.isReal()) return String.valueOf(num.getReal());
        else if (num.getReal() == 0) return num.getImaginary() + "i";
        else return num.getReal() + " + " + num.getImaginary() + "i";
    }
}
