package PathGenerators;

public interface FinitePathGenerator extends Generator
{
	/**
	 * whether or not the generator has completed
	 * @return true when finished
	 */
	boolean done();
}
