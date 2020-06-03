package Exceptions;

/**
 * Exception for any time when we are looking for solution and don't find it.
 */
public class RootNotFound extends Exception
{
	public boolean offTheScreen;
	public RootNotFound()
	{
		super();
		offTheScreen = false;
	}
	public RootNotFound(boolean off)
	{
		super();
		offTheScreen = off;
	}
}
