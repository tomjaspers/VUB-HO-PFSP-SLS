package util;

import java.util.List;

/**
 * Interface for any class that wishes to implement any kind of iterable
 * permutations on a list.
 * 
 * @author Tom Jaspers
 * 
 */
public interface ListPermuter {

	/**
	 * Returns a boolean value indicating whether or not there is still a
	 * permutation left. If all possible permutations have been iterated
	 * through, this will return false.
	 */
	public boolean hasNext();

	/**
	 * Returns the next permutation of the list.
	 */
	public List<Integer> getNext();
	
	/**
	 * Returns a uniformly randomly chosen permutation
	 */
	public List<Integer> getUniformlyRandom();

	/**
	 * Returns the total possible number of permutations that the particular
	 * permuter will be able to provide.
	 * 
	 */
	public int getPossibleNumberOfPermutations();

	/**
	 * Returns the lowest index of the changed items in the list.
	 */
	public int getLastIndexChanged();

	/**
	 * Resets the iteration counters for the permuter.
	 */
	public void resetCounters();

	/**
	 * Sets the initial neighborhood list that serves as a basis from which the
	 * permutations will be made.
	 */
	public void setInitialNeighborhood(List<Integer> neighborhood);
}
