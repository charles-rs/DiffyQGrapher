package Evaluation;

import AST.Derivative;

public class EvaluatorFactory
{
	public static Evaluator getEulerEval(Derivative dx, Derivative dy)
	{
		return new EulerEvaluator(dx, dy);
	}
	public static Evaluator getEulerMidEval(Derivative dx, Derivative dy)
	{
		return new MidEulerEvaluator(dx, dy);
	}
	public static Evaluator getRungeKuttaEval(Derivative dx, Derivative dy)
	{
		return new MidEulerEvaluator(dx, dy);
	}
}
