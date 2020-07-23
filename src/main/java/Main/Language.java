package Main;

public enum Language
{
	ENGLISH,
	PIRATE;



	public static Language fromString(String s)
	{
		switch (s)
		{

			case "pi":
			case "PI":
				return PIRATE;
			case "en":
			case "EN":
			default:
				return ENGLISH;
		}
	}

	@Override
	public String toString()
	{
		switch (this)
		{
			case PIRATE:
				return "pi";
			case ENGLISH:
			default:
				return "en";
		}
	}
}
