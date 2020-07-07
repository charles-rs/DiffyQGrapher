package FXObjects;

import java.util.concurrent.BlockingQueue;

public class SolutionArtist extends Thread
{
	private static OutputPlane o;
	private static BlockingQueue<InitCond> q;

	public static void init(OutputPlane _o, BlockingQueue<InitCond> _q)
	{
		o = _o;
		q = _q;
	}
	public void run()
	{
		try
		{
			InitCond i = q.take();
			o.drawGraph(i, true, o.awtSolutionColor);
			o.render();
		} catch (InterruptedException ignored) {System.out.println("oops");}
		System.out.println(this + "finished");
	}

}
