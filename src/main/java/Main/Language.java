package Main;

public enum Language
{
	ENGLISH,
	PIRATE,
	SPANISH;



	public static Language fromString(String s)
	{
		switch (s)
		{

			case "pi":
			case "PI":
				return PIRATE;
			case "es":
			case "ES":
				return SPANISH;
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
			case SPANISH:
				return "es";
			case ENGLISH:
			default:
				return "en";
		}
	}
}
