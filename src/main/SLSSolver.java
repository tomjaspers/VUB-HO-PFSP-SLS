package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import util.ExpLookUpTable;
import main.IISolver.NeighborhoodMethodOrder;
import main.IISolver.Pivot;
import main.NeighborhoodGenerator.NeighborhoodMethod;
import main.PFSPInstance.InitializationMethod;

public class SLSSolver {

	PFSPInstance instance;
	IISolver iiSolver;

	public SLSSolver() {
		this.iiSolver = new IISolver();
	}

	public SLSSolver(PFSPInstance instance) {
		this();
		this.instance = instance;
	}

	public void setInstance(PFSPInstance instance) {
		this.instance = instance;
	}

	/**
	 * Calculates the maximum run time as a multiple of 1 run of the first
	 * improvement VND with the transpose-exchange-insert neighborhood, using
	 * the slack heuristic for initialization
	 * 
	 * @param multiplier
	 * @return
	 */
	public long calculateMaximumRuntime(long multiplier) {
		iiSolver.setInstance(this.instance);

		long startTime, endTime;
		startTime = System.currentTimeMillis();
		@SuppressWarnings("unused")
		Solution solution = iiSolver.runFirstImprovementVnd(
				NeighborhoodMethodOrder.TRANSPOSE_EXCHANGE_INSERT,
				InitializationMethod.SLACK_HEURISTIC);
		endTime = System.currentTimeMillis();

		/*
		 * We want to run SLS for 100 times as long as VND takes to reach local
		 * optimum See "Implementation exercise sheet 2" (pdf)
		 */
		return (endTime - startTime) * multiplier;
	}

	/**
	 * Runs the SA algorithm with a new Random()
	 * {@link #simulatedAnnealing(InitializationMethod, double, double, double, long, Random)}
	 */
	public Solution simulatedAnnealing(InitializationMethod initialMethod, double t,
			double searchStepsMultiplier, double coolingModifier, long maximumRuntime) {
		Random rnd = new Random();
		return simulatedAnnealing(initialMethod, t, searchStepsMultiplier, coolingModifier,
				maximumRuntime, rnd);
	}

	/**
	 * Runs the IG algorithms with a new Random()
	 * {@link #iteratedGreedy(int, double, long, Random)}
	 */
	public Solution iteratedGreedy(int d, double t, long maximumRuntime) {
		Random rnd = new Random();
		return iteratedGreedy(d, t, maximumRuntime, rnd);
	}

	/**
	 * Implementation of the Simulated Annealing (SA) algorithm for PFSP.
	 * 
	 * See report for parameter explanation and design choices.
	 * 
	 */
	public Solution simulatedAnnealing(InitializationMethod initialMethod, double t,
			double searchStepsMultiplier, double coolingModifier, long maximumRuntime, Random rnd) {
		List<SolutionQualityTrace> solutionQualityTraces = new LinkedList<>();
		int iterationCounter = 1; // for the mod check to avoid initial temp
									// drop
		iiSolver.setInstance(this.instance);
		/* Start timing */
		long startTime = System.currentTimeMillis();
		double temperature = instance.getTemperature(t);
		double initialTemperature = temperature;
		/* Construct an initial solution and perform local search */
		Solution currentSolution = iiSolver.runIterativeImprovement(Pivot.FIRST_IMPROVEMENT,
				NeighborhoodMethod.INSERT, initialMethod);
		Solution bestSolution = currentSolution;
		ExpLookUpTable lookUpTable = new ExpLookUpTable(temperature,
				(int) Math.ceil(currentSolution.weightedTardiness * 0.01));
		NeighborhoodGenerator neighborhoodGenerator = new NeighborhoodGenerator(
				currentSolution.jobsOrder, NeighborhoodMethod.INSERT);
		int searchStepsPerTemperature = (int) (neighborhoodGenerator.getNeighborhoodSize()
				* instance.getNumberOfJobs() * searchStepsMultiplier);
		/* SA search */
		while ((System.currentTimeMillis() - startTime) < maximumRuntime) {
			/* Get a uniformly random neighboring solution */
			Solution proposedSolution = new Solution();
			proposedSolution.jobsOrder = neighborhoodGenerator.getUniformlyRandom();
			proposedSolution.completionTimes = instance.reCalculateCompletionTimes(
					proposedSolution.jobsOrder, currentSolution.completionTimes,
					neighborhoodGenerator.getLastIndexChanged());
			proposedSolution.weightedTardiness = instance
					.calculateWeightedTardiness(proposedSolution.completionTimes);

			/* We'd like to keep track of the best solution as well */
			if (proposedSolution.weightedTardiness < bestSolution.weightedTardiness) {
				bestSolution.jobsOrder = new ArrayList<Integer>(proposedSolution.jobsOrder);
				bestSolution.completionTimes = proposedSolution.completionTimes;
				bestSolution.weightedTardiness = proposedSolution.weightedTardiness;

				solutionQualityTraces.add(new SolutionQualityTrace(bestSolution.weightedTardiness,
						iterationCounter, System.currentTimeMillis() - startTime));
			}

			/* Acceptance criterion */
			if (proposedSolution.weightedTardiness < currentSolution.weightedTardiness) {
				currentSolution = proposedSolution;
				neighborhoodGenerator.setInitialNeighborhood(currentSolution.jobsOrder);

			} else if (rnd.nextDouble() <= lookUpTable.getExp(proposedSolution.weightedTardiness
					- currentSolution.weightedTardiness)) {
				currentSolution = proposedSolution;
				neighborhoodGenerator.setInitialNeighborhood(currentSolution.jobsOrder);
			}

			/* Update temperature according to annealing schedule */
			if (0 == (iterationCounter % searchStepsPerTemperature)) {
				/* Adjust the temperature according to our cooling schedule */
				temperature = temperature
						/ (1 + (temperature / initialTemperature * coolingModifier));
				/*
				 * Update the look-up table for the new temperature (also clears
				 * it)
				 */
				lookUpTable.setTemperature(temperature);
			}

			/*
			 * Increment the counter (used for keeping solution quality trace,
			 * and for schedule)
			 */
			iterationCounter++;
		}
		bestSolution.runtime = System.currentTimeMillis() - startTime;
		bestSolution.iterations = iterationCounter - 1; // it was initialized at
														// 1
		bestSolution.qualityTraces = solutionQualityTraces;
		return bestSolution;
	}

	/**
	 * Implementation of the Iterated Greedy (IG) algorithm for PFSP
	 * 
	 * See report for parameter explanation and design choices.
	 * 
	 * See paper for pseudo code notation: 
	 * π = currentSolution 
	 * πb = bestSolution
	 * π' = reconstructedSolution 
	 * π'' = searchedReconstructedSolution
	 * 
	 */
	public Solution iteratedGreedy(int d, double t, long maximumRuntime, Random rnd) {
		/* Prepare to run */
		List<SolutionQualityTrace> solutionQualityTraces = new LinkedList<>();
		int iterationCounter = 0;
		iiSolver.setInstance(this.instance);
		/* Start timing */
		long startTime = System.currentTimeMillis();
		double temperature = instance.getTemperature(t);

		/* Construct an initial solution and perform local search */
		Solution currentSolution = iiSolver.runIterativeImprovement(Pivot.FIRST_IMPROVEMENT,
				NeighborhoodMethod.INSERT, InitializationMethod.SLACK_HEURISTIC);
		Solution bestSolution = currentSolution;
		/* Construct the lookup table for the Exp */
		ExpLookUpTable lookUpTable = new ExpLookUpTable(temperature,
				(int) Math.ceil(currentSolution.weightedTardiness * 0.01));
		/* Start iterated greedy */
		while ((System.currentTimeMillis() - startTime) < maximumRuntime) {
			/* Destruct & Reconstruct to create s' */
			Solution reconstructedSolution = new Solution();
			reconstructedSolution.jobsOrder = new ArrayList<Integer>(currentSolution.jobsOrder);
			/* Destruction: randomly remove d jobs */
			int[] removedJobs = new int[d];
			for (int i = 0; i < d; i++) {
				int randomIndex = rnd.nextInt(reconstructedSolution.jobsOrder.size());
				removedJobs[i] = reconstructedSolution.jobsOrder.remove(randomIndex);
			}
			/* After removing jobs, reset and recalculate the completion times */
			reconstructedSolution.completionTimes = instance
					.calculateCompletionTimes(reconstructedSolution.jobsOrder);
			reconstructedSolution.weightedTardiness = instance
					.calculateWeightedTardiness(reconstructedSolution.completionTimes);

			/* Construction: optimally insert the removed jobs one by one */
			for (int i = 0; i < d; i++) {
				int newJob = removedJobs[i];
				reconstructedSolution = insertJobOptimally(reconstructedSolution, newJob);
			}

			/* Local search s' to get s'' */
			Solution searchedReconstructedSolution = iiSolver.runIterativeFirstImprovement(
					reconstructedSolution, NeighborhoodMethod.INSERT);

			/* Acceptance criterion */
			if (searchedReconstructedSolution.weightedTardiness < currentSolution.weightedTardiness) {
				currentSolution = searchedReconstructedSolution;
				/* Check if new best solution */
				if (currentSolution.weightedTardiness < bestSolution.weightedTardiness) {
					bestSolution = currentSolution;
					solutionQualityTraces.add(new SolutionQualityTrace(
							bestSolution.weightedTardiness, iterationCounter, System
									.currentTimeMillis() - startTime));
				}
			} else if (rnd.nextDouble() <= lookUpTable
					.getExp(searchedReconstructedSolution.weightedTardiness
							- currentSolution.weightedTardiness)) {
				currentSolution = searchedReconstructedSolution;
			}
			/* Increment the counter (used for keeping solution quality trace) */
			iterationCounter++;
		}
		bestSolution.runtime = System.currentTimeMillis() - startTime;
		bestSolution.iterations = iterationCounter;
		bestSolution.qualityTraces = solutionQualityTraces;
		return bestSolution;
	}

	private Solution insertJobOptimally(Solution initialSolution, int newJob) {
		Solution bestSolution = new Solution();
		bestSolution.weightedTardiness = Integer.MAX_VALUE;

		List<Integer> simulatedJobsOrder = new ArrayList<Integer>(initialSolution.jobsOrder);
		for (int i = 0; i <= initialSolution.jobsOrder.size(); i++) {
			simulatedJobsOrder.add(i, newJob);
			int[][] simulatedCompletionTimes = instance.reCalculateCompletionTimes(
					simulatedJobsOrder, initialSolution.completionTimes, i);
			int simulatedWeightedTardiness = instance
					.calculateWeightedTardiness(simulatedCompletionTimes);
			if (simulatedWeightedTardiness < bestSolution.weightedTardiness) {
				bestSolution.jobsOrder = new ArrayList<Integer>(simulatedJobsOrder);
				bestSolution.completionTimes = simulatedCompletionTimes;
				bestSolution.weightedTardiness = simulatedWeightedTardiness;
			}
			simulatedJobsOrder.remove(i);
		}
		return bestSolution;
	}

}
