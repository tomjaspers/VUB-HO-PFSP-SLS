package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementation of the {@link util.ListPermuter} interface, providing a way of
 * iterating through all possible permutations of the list by means of inserting
 * an item to another position in the list (i.e., moving the item in the list).
 * 
 * The total number of permutations possible is: (n-1) * (n-1)
 * 
 * @author Tom Jaspers
 * 
 */
public class InsertListPermuter implements ListPermuter {

	private List<Integer> initialNeighborhood;
	private int i;
	private int j;
	private int lastIndexChanged;

	public InsertListPermuter(List<Integer> initialNeighborhood) {
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
		if (i == initialNeighborhood.size() - 1 && j == initialNeighborhood.size() - 1) {
			return false;
		}
		return (i < initialNeighborhood.size() && j < initialNeighborhood.size());
	}

	
	/**
	 * @see util.ListPermuter
	 */
	@Override
	public List<Integer> getNext() {
		List<Integer> newList = null;

		while (hasNext() && newList == null) {
			if ((i != j) && ((i + 1) != j)) {
				newList = new ArrayList<Integer>(initialNeighborhood);
				ListUtil.move(newList, i, j);
			}

			lastIndexChanged = Math.min(i, j);

			j++;
			if (j == initialNeighborhood.size()) {
				i++;
				j = 0;
			}
		}
		return newList;
	}
	
	/**
	 * @see util.ListPermuter
	 */
	@Override
	public List<Integer> getUniformlyRandom() {
		List<Integer> newList = new ArrayList<Integer>(initialNeighborhood);
		
		Random rnd = new Random();
		
		i = rnd.nextInt(initialNeighborhood.size());
		j = rnd.nextInt(initialNeighborhood.size());
		/* Let's ensure that our random neighbor is a proper insert */
		while(!((i != j) && ((i + 1) != j))){
			i = rnd.nextInt(initialNeighborhood.size());
			j = rnd.nextInt(initialNeighborhood.size());
		}
		/* Perform the permutation */
		ListUtil.move(newList, i, j);
		lastIndexChanged = Math.min(i, j);
		
		return newList;
	}

	/**
	 * @see util.ListPermuter
	 */
	@Override
	public int getPossibleNumberOfPermutations() {
		/* (n-1) * (n-1) */
		if (initialNeighborhood != null) {
			return (initialNeighborhood.size() - 1) * (initialNeighborhood.size() - 1);
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
		this.j = 0;
	}
}
