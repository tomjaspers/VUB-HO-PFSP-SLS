package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the {@link util.ListPermuter} interface, providing a way of
 * iterating through all possible permutations of the list by means of
 * exchanging 2 items in the list (i.e., swapping the 2 items' positions)
 * 
 * The total number of permutations possible is: n * (n-1) / 2
 * 
 * @author Tom Jaspers
 * 
 */
public class ExchangeListPermuter implements ListPermuter {

	private List<Integer> initialNeighborhood;
	private int i;
	private int j;
	private int lastIndexChanged;

	public ExchangeListPermuter(List<Integer> initialNeighborhood) {
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
		return (i < initialNeighborhood.size() && j < initialNeighborhood.size());
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public List<Integer> getNext() {
		List<Integer> newList = new ArrayList<Integer>(initialNeighborhood);
		Collections.swap(newList, i, j);

		lastIndexChanged = i;

		j++;
		if (j == initialNeighborhood.size()) {
			i++;
			j = i + 1;
		}
		return newList;
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public int getPossibleNumberOfPermutations() {
		/* (n * (n-1) / 2) */
		if (initialNeighborhood != null) {
			return initialNeighborhood.size() * (initialNeighborhood.size() - 1) / 2;
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
		this.j = (i + 1);
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public List<Integer> getUniformlyRandom() {
		// TODO getUniformlyRandom() for ExchangeListPermuter
		System.err.println("ExchangeListPermuter#getUniformlyRandom() not yet implemented");
		return null;
	}
}
