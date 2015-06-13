package main;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import main.PFSPInstance.InitializationMethod;

/**
 * Main class of the program, handling all CLI input to call the proper methods.
 * 
 * @author Tom Jaspers
 * 
 */
public class Flowshop {

	/**
	 * Main entry function for the program (a bit of a mess with all the CLI
	 * processing..)
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		// GENERAL args
		options.addOption("help", false, "Display this help");
		options.addOption("run", false, "Run a choice of algorithm on a single instance");
		options.addOption("sqt", false,
				"Perform and log a solution quality trace on the 6 instances");
		options.addOption("bench", false, "Perform the benchmarks of the two algorithms");
		options.addOption("results", true,
				"Complete path pointing to folder where results will be written");
		options.addOption("sls", true, "Choice of SLS algorithm");
		options.addOption("instance", true, "Instance to load");
		options.addOption("time", true, "Maximum runtime");
		// SA args
		options.addOption("sa_init", true, "SA: Initialization method");
		options.addOption("sa_t", true, "SA: Initial starting temperature");
		options.addOption("sa_steps", true, "SA: Multiplier for the search steps per temperature");
		options.addOption("sa_cooling", true, "SA: Modifier that dictates the cooling behaviour");
		// IG args
		options.addOption("ig_t", true, "IG: Multiplier for the initial starting temperature");
		options.addOption("ig_d", true, "IG: destruction parameter");
		// create the command line parser
		CommandLineParser parser = new BasicParser();
		HelpFormatter helpFormatter = new HelpFormatter();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			// check for null or help
			if (args == null || args.length == 0 || line.hasOption("help")) {
				helpFormatter.printHelp("flowshop", options);
				return;
			}

			// Initialize vars
			SLSSolver solver = new SLSSolver();
			String sls = null;
			PFSPInstance instance = null;
			long maxRuntime = 0l;
			// sa vars
			InitializationMethod init = null;
			int temp = 0;
			double steps = 0.0;
			double coolingModifier = 0.0;
			// ig vars
			double t = 0.0;
			int d = 0;

			if (line.hasOption("run")) {
				// Check for the SLS algorithm (required)
				if (line.hasOption("sls")) {
					sls = line.getOptionValue("sls");
				} else {
					throw new ParseException("sls should be set");
				}
				if (line.hasOption("sa_init")) {
					String init_string = line.getOptionValue("sa_init");
					if (init_string.equals("random")) {
						init = InitializationMethod.RANDOM_PERMUTATION;
					} else if (init_string.equals("slack")) {
						init = InitializationMethod.SLACK_HEURISTIC;
					}
				} else {
					init = InitializationMethod.RANDOM_PERMUTATION;
				}
				// Check for initial temperature (optional)
				if (line.hasOption("sa_t")) {
					temp = Integer.parseInt(line.getOptionValue("sa_t"));
				} else {
					temp = 150; // [100, 200]
				}
				// Check for search steps multiplier
				if (line.hasOption("sa_steps")) {
					steps = Double.parseDouble(line.getOptionValue("sa_steps"));
				} else {
					steps = 0.20; // [0.10, 0.25]
				}
				if (line.hasOption("sa_cooling")) {
					coolingModifier = Double.parseDouble(line.getOptionValue("sa_cooling"));
				} else {
					coolingModifier = 1.45; // [1.00, 2.00]
				}
				// Check for d
				if (line.hasOption("ig_d")) {
					d = Integer.parseInt(line.getOptionValue("ig_d"));
				} else {
					d = 4;
				}
				// Check for d
				if (line.hasOption("ig_t")) {
					t = Double.parseDouble(line.getOptionValue("ig_t"));
				} else {
					t = 0.4;
				}
				// Check for the time (optional)
				if (line.hasOption("time")) {
					maxRuntime = 1000l * Long.parseLong(line.getOptionValue("time"));
				} else {
					System.out.println("Calculating maximum run time (from VND run)...");
					maxRuntime = solver.calculateMaximumRuntime(100L);
				}
				// Check for the instance (required)
				if (line.hasOption("instance")) {
					instance = new PFSPInstance(line.getOptionValue("instance"));
					solver.setInstance(instance);
				} else {
					throw new ParseException("instance should be set");
				}
				Solution solution = null;
				if (sls.equals("sa")) {
					solution = solver.simulatedAnnealing(init, temp, steps, coolingModifier,
							maxRuntime);
				} else if (sls.equals("ig")) {
					solution = solver.iteratedGreedy(d, t, maxRuntime);
				} else {
					throw new ParseException("invalid sls algorithm: " + sls);
				}
				// Output results
				System.out.print(solution.weightedTardiness);
			} else if (line.hasOption("sqt")) {
				// Check for the SLS algorithm (required)
				if (line.hasOption("sls")) {
					sls = line.getOptionValue("sls");
				} else {
					throw new ParseException("sls should be set");
				}
				String resultsPath = "";
				if (line.hasOption("results")) {
					resultsPath = line.getOptionValue("results");
				} else {
					throw new ParseException("results should be set");
				}

				System.out.println("Generating solution quality traces");
				List<PFSPInstance> instances = new ArrayList<PFSPInstance>();
				instances.add(initializeInstances("instances/", "50x20_1").get(0));
				instances.add(initializeInstances("instances/", "60x20_1").get(0));
				instances.add(initializeInstances("instances/", "70x20_1").get(0));
				instances.add(initializeInstances("instances/", "80x20_1").get(0));
				instances.add(initializeInstances("instances/", "90x20_1").get(0));
				instances.add(initializeInstances("instances/", "100x20_1").get(0));

				Benchmarker benchmarker = new Benchmarker(instances, resultsPath);
				if (sls.equals("sa")) {
					benchmarker.qrtdSA(instances, true);
				} else if (sls.equals("ig")) {
					benchmarker.qrtdIG(instances, true);
				} else {
					throw new ParseException("invalid sls algorithm: " + sls);
				}
			} else if (line.hasOption("bench")) {
				String resultsPath = "";
				if (line.hasOption("results")) {
					resultsPath = line.getOptionValue("results");
				} else {
					throw new ParseException("results should be set");
				}

				List<PFSPInstance> instances = initializeInstances("instances/");
				Benchmarker benchmarker = new Benchmarker(instances, resultsPath);
				benchmarker.benchmark();
			} else {
				throw new ParseException("No proper action selected");
			}

		} catch (ParseException exp) {
			System.err.println("Unexpected parse exception: " + exp.getMessage());
		} catch (Exception e) {
			System.err.println("Unexpected exception: " + e.getMessage());
		}
	}

	/**
	 * Load all instances in a given folder (assuming naming conventions of
	 * instance files)
	 */
	public static List<PFSPInstance> initializeInstances(String path) {
		return initializeInstances(path, null);
	}

	/**
	 * Load an instance by its filename and path
	 */
	public static List<PFSPInstance> initializeInstances(String path, String instanceFileName) {
		List<PFSPInstance> instances = new ArrayList<PFSPInstance>();

		/* If instanceFileName is not set, we just load all of the instances */
		if (instanceFileName == null) {
			for (int numJobs = 50; numJobs <= 100; numJobs += 10) {
				for (int variation = 1; variation <= 10; variation++) {
					instanceFileName = numJobs + "x20_" + variation;
					try {
						instances.add(new PFSPInstance(path + instanceFileName));
					} catch (Exception e) {
						System.err.println("Failed to load instance: " + instanceFileName);
						// e.printStackTrace();
					}
				}
			}
		} else {
			/* If instanceFileName is set, we load just this one instance */
			try {
				instances.add(new PFSPInstance(path + instanceFileName));
			} catch (Exception e) {
				System.err.println("Failed to load instance: " + instanceFileName);
				// e.printStackTrace();
			}
		}

		return instances;
	}

}
