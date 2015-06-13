package main;

import java.util.ArrayList;
import java.util.List;

import main.NeighborhoodGenerator.NeighborhoodMethod;
import main.PFSPInstance.InitializationMethod;

/**
 * 
 * Solver scontains the implementation of the Iterative Improvement (II)) and
 * Variable Neighborhood Descent (VND) in the functions
 * {@link #runIterativeImprovement(Pivot, NeighborhoodMethod, InitializationMethod)}
 * and
 * {@link #runFirstImprovementVnd(NeighborhoodMethodOrder, InitializationMethod)}
 * , respectively. The former calls methods {@link #runIterativeFirstImprovement(Solution, NeighborhoodMethod)} or
 * {@link #runIterativeBestImprovement(Solution, NeighborhoodMethod)}, based on the chosen Pivot method. The
 * neighborhood method is passed as a parameter, allowing this part to be
 * handled by the {@link NeighborhoodGenerator}.
 * 
 * @author Tom Jaspers
 * 
 */
public class IISolver {
	/* Enum to declare the 2 types of iterative improvement algorithms */
	enum Pivot {
		FIRST_IMPROVEMENT, BEST_IMPROVEMENT
	};

	/**
	 * Enum containing the 2 possible neighborhood method orders for the VND
	 * algorithm. Both of these are orderings of the same neighborhood methods
	 * used by the II algorithm.
	 * 
	 * @see NeighborhoodGenerator.NeighborhoodMethod
	 * @author Tom Jaspers
	 * 
	 */
	enum NeighborhoodMethodOrder {
		TRANSPOSE_EXCHANGE_INSERT, TRANSPOSE_INSERT_EXCHANGE
	};

	private PFSPInstance instance;

	public IISolver() {
	}
	
	public IISolver(PFSPInstance instance){
		this.instance = instance;
	}

	public void setInstance(PFSPInstance instance) {
		this.instance = instance;
	}

	/* START - Iterative improvement section */

	/**
	 * Wrapper method to run the iterative improvement algorithm with the chosen
	 * parameters
	 * 
	 * @param pivot
	 * @param neighborhoodMethod
	 * @param initializationMethod
	 * @return a Solution to the instance problem
	 */
	public Solution runIterativeImprovement(Pivot pivot,
			NeighborhoodMethod neighborhoodMethod,
			InitializationMethod initializationMethod) {
		Solution initialSolution = new Solution();
		initialSolution.jobsOrder = instance
				.getInitialSolution(initializationMethod);
		initialSolution.completionTimes = instance
				.calculateCompletionTimes(initialSolution.jobsOrder);
		initialSolution.weightedTardiness = instance
				.calculateWeightedTardiness(initialSolution.completionTimes);

		Solution solution = null;
		switch (pivot) {
		case FIRST_IMPROVEMENT:
			solution = runIterativeFirstImprovement(initialSolution,
					neighborhoodMethod);
			break;
		case BEST_IMPROVEMENT:
			solution = runIterativeBestImprovement(initialSolution,
					neighborhoodMethod);
			break;
		default:
			throw new RuntimeException("Unhandled pivot: " + pivot);
		}
		return solution;
	}

	/**
	 * Runs the iterative best-improvement algorithm on a given initial
	 * solution, following a given neighborhood generation method
	 * 
	 * @param initialSolution
	 * @param neighborhoodMethod
	 * @return a solution that is better or equal to the initial solution
	 */
	private Solution runIterativeBestImprovement(Solution initialSolution,
			NeighborhoodMethod neighborhoodMethod) {
		Solution solution = initialSolution;
		boolean improvement = true;
		while (improvement) {
			improvement = false;
			Solution improvingSolution = findBestImprovingNeighborSolution(
					solution, neighborhoodMethod);
			if (improvingSolution != null) {
				improvement = true;
				solution = improvingSolution;
			}
		}
		return solution;
	}

	/**
	 * Return the best improving solution over a current solution, given a
	 * neighborhood generation method. If there is no improving solution, null
	 * is returned.
	 * 
	 * @param currentSolution
	 * @param neighborhoodMethod
	 * @return the best improving solution, or null if none is found
	 */
	private Solution findBestImprovingNeighborSolution(
			Solution currentSolution, NeighborhoodMethod neighborhoodMethod) {
		Solution bestImprovingNeighborSolution = new Solution();
		int bestWeightedTardinessSoFar = currentSolution.weightedTardiness;
		boolean improvementFound = false;
		NeighborhoodGenerator neighborhoodGenerator = new NeighborhoodGenerator(
				currentSolution.jobsOrder, neighborhoodMethod);

		while (neighborhoodGenerator.hasNext()) {
			List<Integer> jobsOrder = neighborhoodGenerator.getNext();
			int[][] completionTimes = instance.reCalculateCompletionTimes(
					jobsOrder, currentSolution.completionTimes,
					neighborhoodGenerator.getLastIndexChanged());
			int weightedTardiness = instance
					.calculateWeightedTardiness(completionTimes);

			if (weightedTardiness < bestWeightedTardinessSoFar) {
				improvementFound = true;
				bestWeightedTardinessSoFar = weightedTardiness;
				bestImprovingNeighborSolution.jobsOrder = jobsOrder;
				bestImprovingNeighborSolution.completionTimes = completionTimes;
				bestImprovingNeighborSolution.weightedTardiness = weightedTardiness;
			}
		}
		if (improvementFound) {
			return bestImprovingNeighborSolution;
		}
		return null;
	}

	/**
	 * Runs the iterative first-improvement algorithm on a given initial
	 * solution, following a given neighborhood generation method
	 * 
	 * @param initialSolution
	 * @param neighborhoodMethod
	 * @return a Solution that is better (or equal to) the initial Solution
	 */
	public Solution runIterativeFirstImprovement(Solution initialSolution,
			NeighborhoodMethod neighborhoodMethod) {
		Solution solution = initialSolution;

		NeighborhoodGenerator neighborhoodGenerator = new NeighborhoodGenerator(
				initialSolution.jobsOrder, neighborhoodMethod);

		boolean improvement = true;
		while (improvement) {
			improvement = false;
			neighborhoodGenerator.resetCounters();
			while (neighborhoodGenerator.hasNext()) {
				List<Integer> jobsOrder = neighborhoodGenerator.getNext();
				int[][] completionTimes = instance.reCalculateCompletionTimes(
						jobsOrder, solution.completionTimes,
						neighborhoodGenerator.getLastIndexChanged());
				int weightedTardiness = instance
						.calculateWeightedTardiness(completionTimes);

				if (weightedTardiness < solution.weightedTardiness) {
					improvement = true;

					solution.jobsOrder = jobsOrder;
					solution.completionTimes = completionTimes;
					solution.weightedTardiness = weightedTardiness;

					neighborhoodGenerator.setInitialNeighborhood(jobsOrder);
				}
			}
		}
		return solution;
	}

	/* END - Iterative improvement section */

	/* START - Variable Neighborhood Descent section */

	public Solution runFirstImprovementVnd(
			NeighborhoodMethodOrder neighborhoodMethodOrder,
			InitializationMethod initializationMethod) {
		/* Construct initial solution */
		Solution initialSolution = new Solution();
		initialSolution.jobsOrder = instance
				.getInitialSolution(initializationMethod);
		initialSolution.completionTimes = instance
				.calculateCompletionTimes(initialSolution.jobsOrder);
		initialSolution.weightedTardiness = instance
				.calculateWeightedTardiness(initialSolution.completionTimes);

		/* Construct the correct order of neighborhoodMethods */
		List<NeighborhoodMethod> neighborhoodMethods = new ArrayList<>(3);
		if (neighborhoodMethodOrder == NeighborhoodMethodOrder.TRANSPOSE_EXCHANGE_INSERT) {
			neighborhoodMethods.add(NeighborhoodMethod.TRANSPOSE);
			neighborhoodMethods.add(NeighborhoodMethod.EXCHANGE);
			neighborhoodMethods.add(NeighborhoodMethod.INSERT);
		} else if (neighborhoodMethodOrder == NeighborhoodMethodOrder.TRANSPOSE_INSERT_EXCHANGE) {
			neighborhoodMethods.add(NeighborhoodMethod.TRANSPOSE);
			neighborhoodMethods.add(NeighborhoodMethod.INSERT);
			neighborhoodMethods.add(NeighborhoodMethod.EXCHANGE);
		} else {
			throw new RuntimeException("Unhandled NeighborhoodMethodOrder: "
					+ neighborhoodMethodOrder);

		}

		/* Run the actual algorithm */
		return runFirstImprovementVnd(initialSolution, neighborhoodMethods);
	}

	private Solution runFirstImprovementVnd(Solution initialSolution,
			List<NeighborhoodMethod> neighborhoodMethods) {
		Solution currentSolution = initialSolution;

		int i = 0;
		while (i < neighborhoodMethods.size()) {
			NeighborhoodMethod neighborhood = neighborhoodMethods.get(i);
			Solution improvingNeighborSolution = findFirstImprovingNeighborSolution(
					currentSolution, neighborhood);
			if (improvingNeighborSolution == null) {
				i++;
			} else {
				currentSolution = improvingNeighborSolution;
				i = 0;
			}
		}
		return currentSolution;
	}

	private Solution findFirstImprovingNeighborSolution(
			Solution currentSolution, NeighborhoodMethod neighborhoodMethod) {
		Solution firstImprovingNeighborSolution = null;
		boolean improvementFound = false;

		NeighborhoodGenerator neighborhoodGenerator = new NeighborhoodGenerator(
				currentSolution.jobsOrder, neighborhoodMethod);

		while (!improvementFound && neighborhoodGenerator.hasNext()) {
			List<Integer> jobsOrder = neighborhoodGenerator.getNext();
			int[][] completionTimes = instance.reCalculateCompletionTimes(
					jobsOrder, currentSolution.completionTimes,
					neighborhoodGenerator.getLastIndexChanged());
			int weightedTardiness = instance
					.calculateWeightedTardiness(completionTimes);

			if (weightedTardiness < currentSolution.weightedTardiness) {
				improvementFound = true;

				firstImprovingNeighborSolution = new Solution();
				firstImprovingNeighborSolution.jobsOrder = jobsOrder;
				firstImprovingNeighborSolution.completionTimes = completionTimes;
				firstImprovingNeighborSolution.weightedTardiness = weightedTardiness;
			}
		}
		return firstImprovingNeighborSolution;
	}

	/* END - Variable Neighborhood Descent section */


}
