package main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.PFSPInstance.InitializationMethod;

/**
 * Class that provides easy access to running benchmarks for all the algorithms
 * (with all the combinations of parameters).
 * 
 * @author Tom Jaspers
 * 
 */
public class Benchmarker {

	List<PFSPInstance> instances;
	String resultsPath;

	public Benchmarker(List<PFSPInstance> instances, String resultsPath) {
		this.instances = instances;
		this.resultsPath = resultsPath;
	}

	/**
	 * Main benchmarking function. Warms up the JVM,
	 * 
	 */
	public void benchmark() {
		System.out.print("Warming up the JVM..");
		warmupJvm(); // this shouldn't output any printing
		System.out.println("done!");
		System.out.println("Benchmarking IG vs SA..");
		bench();
	}

	/**
	 * Warms up the JVM by running both benchmarking algorithms on a reduced
	 * instance size.
	 */
	private void warmupJvm() {
		PrintStream original = System.out;
		try {
			/*
			 * Set System.out to an OutputStream that does nothing, to prevent
			 * the useless benchmarking results from the benchmark
			 */
			System.setOut(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// do nothing
				}
			}));
			/* Warm up with 1 one of the bigger instances */
			bench(instances.subList(instances.size() - 1, instances.size() - 1), false);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.setOut(original);
		}

	}

	/**
	 * Calls {@link #bench(List)} with the complete set of instances passed to
	 * the constructor.
	 */
	public void bench() {
		bench(this.instances, true);
	}

	/**
	 * Perform benchmark of the 2 algorithms with our chosen parameters. New
	 * seed every run, shared seed between the 2 algos.
	 */
	private void bench(List<PFSPInstance> instances, boolean report) {
		// ig params
		int d = 4;
		double t = 0.4;
		// sa params
		InitializationMethod initialMethod = InitializationMethod.SLACK_HEURISTIC;
		int temp = 150;
		double searchStepsMultiplier = 0.20;
		double coolingModifier = 1.45;
		// the rest
		int iterationsPerInstance = 5;
		SLSSolver solver = new SLSSolver();
		Solution solutionIG = null;
		Solution solutionSA = null;
		List<Tuple<Integer, Integer>> wtTuples = new ArrayList<Tuple<Integer, Integer>>();
		for (PFSPInstance instance : instances) {
			wtTuples.clear();
			solver.setInstance(instance);
			// Maximum run time = 100x run time VND
			long maximumRuntime = solver.calculateMaximumRuntime(100L);
			// We won't allow it to run longer than 2 minutes (don't have
			// dedicated pc to do this...)
			if (maximumRuntime > 120000) {
				maximumRuntime = 120000;
			}
			for (int i = 0; i < iterationsPerInstance; i++) {
				System.out.println(instance.getInstanceName() + " - run " + (i + 1) + "/"
						+ iterationsPerInstance);
				// We want the two algorithms to share the same seed per
				// iteration, so we pass it here
				Random rnd = new Random();
				// Do the actual calculation
				solutionIG = solver.iteratedGreedy(d, t, maximumRuntime, rnd);
				solutionSA = solver.simulatedAnnealing(initialMethod, temp, searchStepsMultiplier,
						coolingModifier, maximumRuntime, rnd);
				// Save the results
				wtTuples.add(new Tuple<Integer, Integer>(solutionIG.weightedTardiness,
						solutionSA.weightedTardiness));
			}
			if (report == true) {
				// Log the results to file
				logResults("IG_vs_SA-" + instance.getInstanceName(), wtTuples);
			}
		}
	}

	/**
	 * Get a solution quality trace for IG
	 */
	public void qrtdIG(List<PFSPInstance> instances, boolean report) {
		int iterationsPerInstance = 25;
		// Fixed params
		int d = 4;
		double t = 0.4;
		SLSSolver solver = new SLSSolver();
		Solution solution = null;
		for (PFSPInstance instance : instances) {
			solver.setInstance(instance);
			// Maximum run time = 10x(100x run time VND)
			long maximumRuntime = solver.calculateMaximumRuntime(1000L);
			// We won't allow it to run longer than 10 minutes (don't have
			// dedicated pc to do this...)
			if (maximumRuntime > 600000) {
				maximumRuntime = 600000;
			}
			System.out
					.println("Going to run for " + maximumRuntime / 1000 + " seconds / iteration");
			for (int i = 0; i < iterationsPerInstance; i++) {
				System.out.println(instance.getInstanceName() + " - run " + (i + 1) + "/"
						+ iterationsPerInstance);
				solution = solver.iteratedGreedy(d, t, maximumRuntime);
				logSolutionQualityTrace("IG_SQT_" + instance.getInstanceName() + "_run" + i,
						solution.qualityTraces);

			}
		}
	}

	/**
	 * Get a solution quality trace for SA
	 */
	public void qrtdSA(List<PFSPInstance> instances, boolean report) {
		int iterationsPerInstance = 25;
		// Fixed
		InitializationMethod initialMethod = InitializationMethod.SLACK_HEURISTIC;
		int t = 150;
		double searchStepsMultiplier = 0.20;
		double coolingModifier = 1.45;

		SLSSolver solver = new SLSSolver();
		Solution solution = null;
		for (PFSPInstance instance : instances) {
			solver.setInstance(instance);
			// Maximum run time = 10x(100x run time VND)
			long maximumRuntime = solver.calculateMaximumRuntime(1000L);
			// We won't allow it to run longer than 10 minutes (don't have
			// dedicated pc to do this...)
			if (maximumRuntime > 600000) {
				maximumRuntime = 600000;
			}
			System.out
					.println("Going to run for " + maximumRuntime / 1000 + " seconds / iteration");
			for (int i = 0; i < iterationsPerInstance; i++) {
				System.out.println(instance.getInstanceName() + " - run " + (i + 1) + "/"
						+ iterationsPerInstance);
				solution = solver.simulatedAnnealing(initialMethod, t, searchStepsMultiplier,
						coolingModifier, maximumRuntime);
				logSolutionQualityTrace("SA_SQT_" + instance.getInstanceName() + "_run" + i,
						solution.qualityTraces);

			}
		}
	}

	/**
	 * Write the solution quality traces to file
	 * 
	 * @param fileName
	 * @param solutionQualityTraces
	 */
	private void logSolutionQualityTrace(String fileName,
			List<SolutionQualityTrace> solutionQualityTraces) {
		final String fs = System.getProperty("file.separator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsPath
					+ fs + fileName + ".txt")));
			for (SolutionQualityTrace solutionQualityTrace : solutionQualityTraces) {
				writer.write(solutionQualityTrace.toString());
				writer.newLine();
				writer.flush();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Write the results of benchmark to a file
	 */
	private void logResults(String fileName, List<Tuple<Integer, Integer>> wtTuples) {
		final String fs = System.getProperty("file.separator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsPath
					+ fs + fileName + ".txt")));

			for (Tuple<Integer, Integer> tuple : wtTuples) {
				writer.write(tuple.toString());
				writer.newLine();
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Tuple class, needed this to hold the results from benchmarking the 2
	 * algorithms against eachother
	 * 
	 * @author tjs
	 * 
	 * @param <X>
	 * @param <Y>
	 */
	private class Tuple<X, Y> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return x + "," + y;
		}

	}
}
