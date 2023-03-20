package Evaluation;

import AST.Derivative;

/**
 * Factory that produces evaluators
 */
public class EvaluatorFactory {
    /**
     * gets a new evaluator
     * 
     * @param ty the type of the desired evaluator
     * @param dx the x derivative
     * @param dy the y derivative
     * @return the new evaluator
     */
    public static Evaluator getEvaluator(EvalType ty, Derivative dx, Derivative dy) {
        switch (ty) {
            case Euler:
                return new EulerEvaluator(dx, dy);
            case MidEuler:
                return new MidEulerEvaluator(dx, dy);
            case RungeKutta:
                return new RungeKuttaEvaluator(dx, dy);
            case RKF45:
                return new RKF45eval(dx, dy);
            default:
                throw new Error();
        }
    }

    /**
     * gets the best evaluator available
     * 
     * @param dx the x derivative
     * @param dy the y derivative
     * @return the new evaluator
     */
    public static Evaluator getBestEvaluator(Derivative dx, Derivative dy) {
        return new RKF45eval(dx, dy);
    }
}
