// package FXObjects;

// import java.util.ArrayList;
// import Evaluation.Evaluator;
// import Evaluation.EvaluatorFactory;
// import javafx.geometry.Point2D;


// public class BetterBasinFinder extends Thread {
// private OutputPlane o;

// private BasinFinderDispatcher dispatcher;

// @Override
// public void run() {
// var e = EvaluatorFactory.getEvaluator(o.evalType, o.getDx(), o.getDy());
// while (dispatcher.done.get()) {
// int radius = dispatcher.radius.getAndIncrement();
// double dTheta = 1.0 / (2 * Math.PI * radius);
// for (double theta = 0; theta <= 2 * Math.PI; theta += dTheta) {
// int imgX = (int) (radius * Math.cos(theta));
// int imgY = (int) (radius * Math.sin(theta));
// int[] fieldStart = {imgX + dispatcher.critImg[0], imgY + dispatcher.critImg[1]};
// var start = dispatcher.fromFieldCoords(fieldStart);
// var fieldPos = fieldStart;
// var path = new ArrayList<int[]>();
// e.initialise(start, o.getT(), o.a, o.b, dispatcher.inc);
// while (dispatcher.field[fieldPos[0]][fieldPos[1]] == 0
// && dispatcher.inBounds(fieldPos)) {
// path.add(imgPos);

// }
// }
// }
// }

// }
