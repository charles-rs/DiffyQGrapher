package Exceptions;

/**
 * exception for parsing non-parsable things
 */
public class SyntaxError extends Exception
{
	public SyntaxError(String message) {
		super(message);
	}
}
