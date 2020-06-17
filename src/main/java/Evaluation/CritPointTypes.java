package Evaluation;

/**
 * The types of critical points
 */
public enum CritPointTypes
{
	SPIRALSOURCE("Spiral Source"),
	SPIRALSINK("Spiral Sink"),
	SADDLE("Saddle"),
	NODESINK("Node Sink"),
	CENTER("Center"),
	NODESOURCE("Node Source");


	private final String stringRep;
	CritPointTypes(String s)
	{
		stringRep = s;
	}

	public String getStringRep()
	{
		return stringRep;
	}

	public boolean isSink()
	{
		return this == SPIRALSINK || this == NODESINK;
	}
	public boolean isSource()
	{
		return this == SPIRALSOURCE || this == NODESOURCE;
	}
}
