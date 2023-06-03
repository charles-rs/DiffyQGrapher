package FXObjects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class CoordPlaneEditWindow extends Stage {
    CoordPlane plane;

    public CoordPlaneEditWindow(CoordPlane plane) {
        setTitle("Edit Bounds");
        this.plane = plane;
        VBox v = new VBox();


        StackPane pane = new StackPane();
        TextField xMin = new TextField();
        TextField xMax = new TextField();
        TextField yMin = new TextField();
        TextField yMax = new TextField();
        xMin.setText(plane.xMin.getValue().toString());
        yMin.setText(plane.yMin.getValue().toString());
        xMax.setText(plane.xMax.getValue().toString());
        yMax.setText(plane.yMax.getValue().toString());

        var fields = new TextField[]{xMin, xMax, yMin, yMax};
        for (var f : fields) {
            f.textProperty().addListener(((observable, oldValue, newValue) -> {
                if (!newValue.matches("-?[0-9]*(\\.)?[0-9]*")) {
                    f.setText(oldValue);
                }
            }));
            f.setMaxWidth(120);
        }
        Button saveButton = new Button("save");
        saveButton.setOnAction((a) -> {
            plane.yMin.set(Double.parseDouble(yMin.getText()));
            plane.xMin.set(Double.parseDouble(xMin.getText()));
            plane.yMax.set(Double.parseDouble(yMax.getText()));
            plane.xMax.set(Double.parseDouble(xMax.getText()));
            plane.updateForZoom();
            plane.drawAxes(false);
            this.close();
        });


//        AnchorPane.setLeftAnchor(xMin, 1.);
//        AnchorPane.setRightAnchor(xMax, 1.0);
//        AnchorPane.setBottomAnchor(yMin, 1.0);
//        AnchorPane.setTopAnchor(yMax, 1.0);

        pane.getChildren().addAll(xMin, xMax, yMin, yMax);
        pane.setMinSize(300, 200);
        pane.setPadding(new Insets(5));

        StackPane.setAlignment(xMin, Pos.CENTER_LEFT);
        StackPane.setAlignment(xMax, Pos.CENTER_RIGHT);
        StackPane.setAlignment(yMin, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(yMax, Pos.TOP_CENTER);

        v.getChildren().addAll(pane, saveButton);
        v.setPadding(new Insets(5));
        VBox.setVgrow(pane, Priority.ALWAYS);
        var scene = new Scene(v);

        setAlwaysOnTop(true);
        setScene(scene);
        show();

    }
}
