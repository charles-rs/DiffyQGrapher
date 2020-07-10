package PathGenerators;

public interface LoopGenerator extends Generator
{
	/**
	 * how many times the generator has rounded the loop
	 * @return the number of times round the loop
	 */
	int numRounds();

	/**
	 * whether or not the generator has rounded a loop
	 * @return whether the generator has rounded a loop
	 */
	boolean completed();
}
