package main;

import java.util.ArrayList;
import java.util.List;

import util.ExchangeListPermuter;
import util.InsertListPermuter;
import util.ListPermuter;
import util.TransposeListPermuter;

/**
 * Provides access to easy generation of list permutations, based on the
 * neighborhood method.
 * 
 * @author Tom Jaspers
 * 
 */
public class NeighborhoodGenerator {
	/**
	 * Enum containing the 3 different supported NeighborhoodMethods: transpose,
	 * exchange, and insert. These 3 are (respectively) implemented by:
	 * {@link util.TransposeListPermuter}, {@link util.ExchangeListPermuter},
	 * {@link util.InsertListPermuter}
	 * 
	 * @author Tom Jaspers
	 * 
	 */
	enum NeighborhoodMethod {
		TRANSPOSE, EXCHANGE, INSERT
	};

	private ListPermuter listPermuter;

	public NeighborhoodGenerator(List<Integer> initialNeighborhood, NeighborhoodMethod neighborhoodMethod) {
		switch (neighborhoodMethod) {
		case TRANSPOSE:
			listPermuter = new TransposeListPermuter(initialNeighborhood);
			break;
		case EXCHANGE:
			listPermuter = new ExchangeListPermuter(initialNeighborhood);
			break;
		case INSERT:
			listPermuter = new InsertListPermuter(initialNeighborhood);
			break;
		default:
			throw new RuntimeException("Undefined NeighborhoodMethod: " + neighborhoodMethod);
		}
	}

	public void setInitialNeighborhood(List<Integer> neighborhood) {
		listPermuter.setInitialNeighborhood(neighborhood);
	}

	/**
	 * Returns whether the underlying listPermuter has a new permutation
	 * 
	 * @see util.ListPermuter
	 */
	public boolean hasNext() {
		return listPermuter.hasNext();
	}

	/**
	 * Returns the next permutation from the underlying listPermuter
	 * 
	 * @see util.ListPermuter
	 */
	public List<Integer> getNext() {
		return listPermuter.getNext();
	}
	
	/**
	 * Returns a uniformly random permutation from the underlying listPermuter
	 */
	public List<Integer> getUniformlyRandom(){
		return listPermuter.getUniformlyRandom();
	}

	/**
	 * Returns a list containing all possible permutations that the listPermuter
	 * can find.
	 */
	public List<List<Integer>> getAll() {
		List<List<Integer>> allPermutations = new ArrayList<List<Integer>>();
		while (hasNext()) {
			List<Integer> next = getNext();
			if (next != null) {
				allPermutations.add(next);
			}
		}
		return allPermutations;
	}

	/**
	 * Returns the lowest index of the changed items
	 * 
	 * @see util.ListPermuter
	 */
	public int getLastIndexChanged() {
		return listPermuter.getLastIndexChanged();
	}

	/**
	 * Resets the counter of the underlying listPermuter
	 * 
	 * @see util.ListPermuter
	 */
	public void resetCounters() {
		listPermuter.resetCounters();
	}
	
	public int getNeighborhoodSize(){
		return listPermuter.getPossibleNumberOfPermutations();
	}

}
