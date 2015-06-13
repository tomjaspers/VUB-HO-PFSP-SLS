package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the {@link util.ListPermuter} interface, providing a way of
 * iterating through all possible permutations of the list by means of
 * transposing 2 items in the list (i.e., swapping 2 items' positions granted
 * that these 2 items are next to each other)
 * 
 * The total number of permutations possible is: n-1
 * 
 * @author Tom Jaspers
 * 
 */
public class TransposeListPermuter implements ListPermuter {

	private List<Integer> initialNeighborhood;
	private int i;
	private int lastIndexChanged;

	public TransposeListPermuter(List<Integer> initialNeighborhood) {
		this.initialNeighborhood = initialNeighborhood;
		resetCounters();
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public void setInitialNeighborhood(List<Integer> neighborhood) {
		this.initialNeighborhood = neighborhood;
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public boolean hasNext() {
		return i < (initialNeighborhood.size() - 1);
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public List<Integer> getNext() {
		List<Integer> newList = new ArrayList<Integer>(initialNeighborhood);
		Collections.swap(newList, i, (i + 1));

		lastIndexChanged = i;

		i++;
		return newList;
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public int getPossibleNumberOfPermutations() {
		/* n-1 */
		if (initialNeighborhood != null) {
			return initialNeighborhood.size() - 1;
		}
		return 0;
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public int getLastIndexChanged() {
		return this.lastIndexChanged;
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public void resetCounters() {
		this.i = 0;
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public List<Integer> getUniformlyRandom() {
		// TODO getUniformlyRandom() for TransposeListPermuter
		System.err.println("TransposeListPermuter#getUniformlyRandom() not yet implemented");
		return null;
	}

}
