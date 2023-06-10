package Main;

import FXObjects.InputPlane;
import FXObjects.OutputPlane;
import easyIO.EOF;
import easyIO.Scanner;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lwon.data.Dictionary;
import lwon.data.Text;
import lwon.parse.Parser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static Utils.Utils.textValue;

public class Saver {

    OutputPlane outP;
    InputPlane inP;
    TextArea inputArea;

    public Saver(OutputPlane outP, InputPlane inP, TextArea inputArea, Stage primaryStage, boolean write) {
        this.outP = outP;
        this.inP = inP;
        this.inputArea = inputArea;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select File");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setInitialFileName("equations.diffyq");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("DiffyQ files", "*.diffyq"));
        File selected;
        if (write) {
            selected = chooser.showSaveDialog(primaryStage);
        } else {
            selected = chooser.showOpenDialog(primaryStage);
        }
        if (selected != null) {
            try {
                if (write)
                    writeFile(selected);
                else
                    readFile(selected);
            } catch (Exception ignored) {
            }
        }
    }


    void writeFile(File f) throws IOException {
        var builder = new Dictionary.Builder();
        builder.put("a", new Text(Double.toString(inP.getA()), null));
        builder.put("b", new Text(Double.toString(inP.getB()), null));
        builder.put("dx", new Text(outP.getDx().toString(), null));
        builder.put("dy", new Text(outP.getDy().toString(), null));
        builder.put("inP", inP.toLwon());
        builder.put("outP", outP.toLwon());
        try (var fw = new FileWriter(f)) {
            fw.write(builder.build(null).toString());
        }
    }

    void readFile(File f) throws IOException, Parser.SyntaxError, EOF {
        Parser p = new Parser(new Scanner(new FileReader(f), f.getName()));
        var _topLevel = p.parse();
        if (_topLevel instanceof Dictionary topLevel) {
            double aVal = 0, bVal = 0;
            if (topLevel.get("a").get(0) instanceof Text t) {
                aVal = Double.parseDouble(t.value());
            }
            if (topLevel.get("b").get(0) instanceof Text t) {
                bVal = Double.parseDouble(t.value());
            }
            inP.updateA(aVal);
            outP.updateA(aVal);
            inP.updateB(bVal);
            outP.updateB(bVal);
            var dx = textValue(topLevel.get("dx").get(0));
            var dy = textValue(topLevel.get("dy").get(0));
            String eqns = dx + "\n" + dy + "\n";
            inputArea.setText(eqns);

            inP.fromLwon((Dictionary) topLevel.get("inP").get(0));
            outP.fromLwon((Dictionary) topLevel.get("outP").get(0));

        }

    }
}
