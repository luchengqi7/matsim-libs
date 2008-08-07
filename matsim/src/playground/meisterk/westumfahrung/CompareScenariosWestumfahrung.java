package playground.meisterk.westumfahrung;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.matsim.analysis.CalcAverageTripLength;
import org.matsim.analysis.CalcLegTimes;
import org.matsim.basic.v01.Id;
import org.matsim.deqsim.EventsReaderDEQv1;
import org.matsim.events.Events;
import org.matsim.gbl.Gbl;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.Act;
import org.matsim.population.MatsimPlansReader;
import org.matsim.population.Person;
import org.matsim.population.Plans;
import org.matsim.population.PlansReaderI;
import org.matsim.population.algorithms.PersonIdRecorder;
import org.matsim.population.algorithms.PlanAverageScore;
import org.matsim.population.filters.ActLinkFilter;
import org.matsim.population.filters.PersonFilterI;
import org.matsim.population.filters.PersonIdFilter;
import org.matsim.population.filters.RouteLinkFilter;
import org.matsim.population.filters.SelectedPlanFilter;
import org.matsim.basic.v01.IdImpl;
import org.matsim.basic.v01.BasicPlanImpl.ActIterator;
import org.matsim.utils.misc.Time;

import playground.meisterk.facilities.ShopsOf2005ToFacilities;

/**
 * Compare two scenarios (network, plans, events) with each other.
 * Contains several analyses that were performed for the Westumfahrung Zurich study.
 * 
 * @author meisterk
 *
 */
public class CompareScenariosWestumfahrung {

	public class CaseStudyResult {

		private String name;
		private Plans plans;
		private CalcLegTimes calcLegTimes;
		private PlanAverageScore planAverageScore;
		private CalcAverageTripLength calcAverageTripLength;

		public CaseStudyResult(String name, Plans plans,
				CalcLegTimes calcLegTimes, PlanAverageScore planAverageScore,
				CalcAverageTripLength calcAverageTripLength) {
			super();
			this.name = name;
			this.plans = plans;
			this.calcLegTimes = calcLegTimes;
			this.planAverageScore = planAverageScore;
			this.calcAverageTripLength = calcAverageTripLength;
		}

		public String getName() {
			return name;
		}

		public Plans getRouteSwitchers() {
			return plans;
		}

		public CalcLegTimes getRouteSwitchersLegTimes() {
			return calcLegTimes;
		}

		public PlanAverageScore getRouteSwitchersAverageScore() {
			return planAverageScore;
		}

		public CalcAverageTripLength getCalcAverageTripLength() {
			return calcAverageTripLength;
		}


	}

	private static final Logger log = Logger.getLogger(ShopsOf2005ToFacilities.class);
	
	private Plans inputPlans = null;

	// transit agents have ids > 1'000'000'000
	private final String TRANSIT_PERSON_ID_PATTERN = "[0-9]{10}";
	private final String NON_TRANSIT_PERSON_ID_PATTERN = "[0-9]{1,9}";

	// compare 2 scenarios
	private String scenarioNameBefore = "before";
	private String scenarioNameAfter = "after";
	private String[] scenarioNames = new String[]{scenarioNameBefore, scenarioNameAfter};

	// analyses
	private final int TRANSIT_AGENTS_ANALYSIS_NAME = 0;
	private final int NON_TRANSIT_AGENTS_ANALYSIS_NAME = 1;
	private final int ROUTE_SWITCHERS_ANALYSIS_NAME = 2;
	private final int WESTSTRASSE_NEIGHBORS_ANALYSIS_NAME = 3;
	private TreeMap<Integer, String> analysisNames = new TreeMap<Integer, String>();

	// analysisRegions
	private HashSet<Id> weststrasseLinkIds = new HashSet<Id>();

	private TreeMap<String, String> scenarioNameInputFilenames = new TreeMap<String, String>();
	private TreeMap<String, String> plansInputFilenames = new TreeMap<String, String>();
	private TreeMap<String, String> eventsInputFilenames = new TreeMap<String, String>();
	private TreeMap<String, String> networkInputFilenames = new TreeMap<String, String>();
	private TreeMap<String, String> linkSetFilenames = new TreeMap<String, String>();
	private TreeMap<String, HashSet<Id>> linkSets = new TreeMap<String, HashSet<Id>>();
	private TreeMap<String, String> linkSetNames = new TreeMap<String, String>();

	private String scenarioComparisonFilename = null;
	private ArrayList<String> scenarioComparisonLines = new ArrayList<String>();

	private String routeSwitchersListFilename = null;
	private ArrayList<String> routeSwitchersLines = new ArrayList<String>();


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CompareScenariosWestumfahrung compareScenarios = new CompareScenariosWestumfahrung();
		compareScenarios.run(args);

	}

	private void run(String[] args) {

		log.info("Processing command line parameters...");
		this.processArgs(args);
		log.info("Processing command line parameters...done.");
		System.out.flush();
		log.info("Init...");
		this.init();
		log.info("Init...done.");
		System.out.flush();
		log.info("Performing analyses...");
		this.doAnalyses();
		log.info("Performing analyses...done.");
		System.out.flush();
		log.info("Writing out results...");
		this.writeResults();
		log.info("Writing out results...done.");
		System.out.flush();

	}

	private void writeResults() {

		File scenarioComparisonFile = new File(scenarioComparisonFilename);
		File routeSwitchersFile = new File(routeSwitchersListFilename);
		try {
			FileUtils.writeLines(scenarioComparisonFile, "UTF-8", scenarioComparisonLines);
			FileUtils.writeLines(routeSwitchersFile, "UTF-8", routeSwitchersLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void processArgs(String[] args) {

		List<String> lines = new ArrayList<String>();
		File scenarioNameInputFile = null;
		
		if (args.length != 10) {
			System.out.println("Usage:");
			System.out.println("java CompareScenarios args");
			System.out.println("");
			System.out.println("args[0]: name_before.txt");
			System.out.println("args[1]: network_before.xml");
			System.out.println("args[2]: plans_before.xml.gz");
			System.out.println("args[3]: events_before.dat");
			System.out.println("args[4]: linkset_before.txt");
			System.out.println("args[5]: name_after.txt");
			System.out.println("args[6]: network_after.xml");
			System.out.println("args[7]: plans_after.xml.gz");
			System.out.println("args[8]: events_after.dat");
			System.out.println("args[9]: linkset_after.txt");
			System.out.println("");
			System.exit(-1);
		} else {
			int argsIndex = 0;
			for (int ii=0; ii<=1; ii++) {
				scenarioNameInputFile = new File(args[argsIndex]);
				try {
					lines = FileUtils.readLines(scenarioNameInputFile, "UTF-8");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				switch(ii) {
				case 0:
					scenarioNameBefore = lines.get(0);
					scenarioNames[ii] = lines.get(0);
					break;
				case 1:
					scenarioNameAfter = lines.get(0);
					scenarioNames[ii] = lines.get(0);
					break;
				}
				argsIndex++;
				networkInputFilenames.put(scenarioNames[ii], args[argsIndex]);
				argsIndex++;
				plansInputFilenames.put(scenarioNames[ii], args[argsIndex]);
				argsIndex++;
				eventsInputFilenames.put(scenarioNames[ii], args[argsIndex]);
				argsIndex++;
				linkSetFilenames.put(scenarioNames[ii], args[argsIndex]);
				argsIndex++;
			}
		}

	}

	private void init() {

		analysisNames.put(new Integer(TRANSIT_AGENTS_ANALYSIS_NAME), "transit");
		analysisNames.put(new Integer(NON_TRANSIT_AGENTS_ANALYSIS_NAME), "non transit");
		analysisNames.put(new Integer(ROUTE_SWITCHERS_ANALYSIS_NAME), "route switchers");
		analysisNames.put(new Integer(WESTSTRASSE_NEIGHBORS_ANALYSIS_NAME), "weststrasse neighbors");

		List<String> lines = new ArrayList<String>();

		File linkSetFile = null;
		HashSet<Id> linkSet = null;

		for (String scenarioName : scenarioNames) {
			
			linkSetFile = new File(linkSetFilenames.get(scenarioName));
			try {
				lines = FileUtils.readLines(linkSetFile, "UTF-8");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			linkSet = new HashSet<Id>();
			for (String line : lines) {
				try {
					linkSet.add(new IdImpl(Integer.parseInt(line, 10)));
				} catch (NumberFormatException e) {
					log.info("Reading in " + line + " link set...");
					linkSetNames.put(scenarioName, line);
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			linkSets.put(scenarioName, linkSet);
		}
		
		// build up weststrasse
		linkSetFile  = new File("input/linksets/weststrasse.txt");
		try {
			lines = FileUtils.readLines(linkSetFile, "UTF-8");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		linkSet = new HashSet<Id>();
		for (String line : lines) {
			try {
				weststrasseLinkIds.add(new IdImpl(Integer.parseInt(line)));
			} catch (NumberFormatException e) {
				log.info("Reading in " + line + " link set...");
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		// output file names
		scenarioComparisonFilename = 
			"output/" + 
			scenarioNameBefore + 
			"_vs_" + 
			scenarioNameAfter + 
			"__" + 
			linkSetNames.get(scenarioNameBefore) +
			"_to_" +
			linkSetNames.get(scenarioNameAfter) +
			"__summary" +
			".txt";
		routeSwitchersListFilename = 
			"output/" + 
			scenarioNameBefore + 
			"_vs_" + 
			scenarioNameAfter + 
			"__" + 
			linkSetNames.get(scenarioNameBefore) +
			"_to_" +
			linkSetNames.get(scenarioNameAfter) +
			"__routeswitchers" +
			".txt";

	}

	/**
	 * Gets all agents that use a set of links in one plans file and use another set in another plans file.
	 * For example. Find all agents that use the Westtangente in a scenario with out the Westumfahrung, that
	 * switch to the Westumfahrung in a case study where the Westumfahrung was included in the scenario.
	 * 
	 * Summarize their average trip travel times, the scores of their selected plans, and their home locations.
	 */
	private void doAnalyses() {

		TreeMap<Integer, TreeMap<String, PersonIdRecorder>> personIdRecorders = new TreeMap<Integer, TreeMap<String, PersonIdRecorder>>();

		for (Integer analysis : analysisNames.keySet()) {
			personIdRecorders.put(analysis, new TreeMap<String, PersonIdRecorder>());
		}
		TreeMap<String, Plans> scenarioPlans = new TreeMap<String, Plans>();
		TreeMap<String, NetworkLayer> scenarioNetworks = new TreeMap<String, NetworkLayer>();

		PersonIdRecorder personIdRecorder = null;
		PersonFilterI filterAlgorithm = null;
		
		for (String scenarioName : scenarioNames) {

			NetworkLayer network = new NetworkLayer();
			new MatsimNetworkReader(network).readFile(networkInputFilenames.get(scenarioName));
			scenarioNetworks.put(scenarioName, network);
			Gbl.getWorld().setNetworkLayer(network);

			//Plans plans = playground.meisterk.MyRuns.initMatsimAgentPopulation(plansInputFilenames.get(scenarioName), false, null);
			Plans plans = new Plans(false);
			PlansReaderI plansReader = new MatsimPlansReader(plans);
			plansReader.readFile(plansInputFilenames.get(scenarioName));
			plans.printPlansCount();

			scenarioPlans.put(scenarioName, plans);

			for (Integer analysis : analysisNames.keySet()) {

				personIdRecorder = new PersonIdRecorder();

				// distinguish person filtering by analysis type
				switch(analysis.intValue()) {
				case TRANSIT_AGENTS_ANALYSIS_NAME:
					filterAlgorithm = new PersonIdFilter(TRANSIT_PERSON_ID_PATTERN, personIdRecorder);
					break;
				case NON_TRANSIT_AGENTS_ANALYSIS_NAME:
					filterAlgorithm = new PersonIdFilter(NON_TRANSIT_PERSON_ID_PATTERN, personIdRecorder);
					break;
				case ROUTE_SWITCHERS_ANALYSIS_NAME:
					RouteLinkFilter routeLinkFilter = new RouteLinkFilter(personIdRecorder);
					filterAlgorithm = new SelectedPlanFilter(routeLinkFilter);

					for (Id linkId : linkSets.get(scenarioName)) {
						routeLinkFilter.addLink(linkId);
					}
					break;
				case WESTSTRASSE_NEIGHBORS_ANALYSIS_NAME:
					ActLinkFilter homeAtTheWeststrasseFilter = new ActLinkFilter(".*h.*", personIdRecorder);
					filterAlgorithm = new SelectedPlanFilter(homeAtTheWeststrasseFilter);

					for (Id linkId : weststrasseLinkIds) {
						homeAtTheWeststrasseFilter.addLink(linkId);
					}
					break;
				default:
					break;
				}

				personIdRecorders.get(analysis).put(scenarioName, personIdRecorder);
				plans.addAlgorithm(filterAlgorithm);
			}
			plans.runAlgorithms();

		}

		// make this nicer, because all analyses are of the same kind :-)
		HashSet<Id> routeSwitchersPersonIds = (HashSet<Id>) personIdRecorders.get(ROUTE_SWITCHERS_ANALYSIS_NAME).get(scenarioNameAfter).getIds().clone();
		routeSwitchersPersonIds.retainAll(personIdRecorders.get(ROUTE_SWITCHERS_ANALYSIS_NAME).get(scenarioNameBefore).getIds());

		HashSet<Id> neighborsPersonIds = personIdRecorders.get(WESTSTRASSE_NEIGHBORS_ANALYSIS_NAME).get(scenarioNameBefore).getIds();
		HashSet<Id> transitAgentsIds = personIdRecorders.get(TRANSIT_AGENTS_ANALYSIS_NAME).get(scenarioNameBefore).getIds();
		HashSet<Id> nonTransitAgentsIds = personIdRecorders.get(NON_TRANSIT_AGENTS_ANALYSIS_NAME).get(scenarioNameBefore).getIds();

		log.info("Agents before: " + personIdRecorders.get(ROUTE_SWITCHERS_ANALYSIS_NAME).get(scenarioNameBefore).getIds().size());
		log.info("Agents after: " + personIdRecorders.get(ROUTE_SWITCHERS_ANALYSIS_NAME).get(scenarioNameAfter).getIds().size());
		log.info("Route switchers: " + routeSwitchersPersonIds.size());
		log.info("number of neighbors: " + neighborsPersonIds.size());
		log.info("number of transit agents: " + transitAgentsIds.size());
		log.info("number of non transit agents: " + nonTransitAgentsIds.size());

		Iterator<Id> personIterator = null;
		HashSet<Id> subPop = new HashSet<Id>();
		for (Integer analysis : analysisNames.keySet()) {

			ArrayList<CaseStudyResult> results = new ArrayList<CaseStudyResult>();
			for (String scenarioName : scenarioNames) {

				// choose right network
				Gbl.getWorld().setNetworkLayer(scenarioNetworks.get(scenarioName));

				Plans plansSubPop = new Plans(false);
				switch(analysis.intValue()) {
				case TRANSIT_AGENTS_ANALYSIS_NAME:
					personIterator = transitAgentsIds.iterator();
					break;
				case NON_TRANSIT_AGENTS_ANALYSIS_NAME:
					personIterator = nonTransitAgentsIds.iterator();
					break;
				case ROUTE_SWITCHERS_ANALYSIS_NAME:
					personIterator = routeSwitchersPersonIds.iterator();
					break;
				case WESTSTRASSE_NEIGHBORS_ANALYSIS_NAME:
					personIterator = neighborsPersonIds.iterator();
					break;
				default:
					break;
				}

				while(personIterator.hasNext()) {
					try {
						plansSubPop.addPerson(scenarioPlans.get(scenarioName).getPerson(personIterator.next()));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				Act homeActivity = null;
				if (analysis.intValue() == ROUTE_SWITCHERS_ANALYSIS_NAME) {
					if (scenarioName.equals(scenarioNames[0])) {
						routeSwitchersLines.add("person\thome_link\thome_x\thome_y");
						for (Person person : plansSubPop.getPersons().values()) {
							ActIterator actIterator = person.getSelectedPlan().getIteratorAct();
							while (actIterator.hasNext()) {
								homeActivity = (Act) actIterator.next();
								if (Pattern.matches(".*h.*", homeActivity.getType())) {
									continue;
								}
							}
							routeSwitchersLines.add(new String(
									person.getId() + "\t" +
									homeActivity.getLinkId().toString() + "\t" +
									homeActivity.getCoord().getX() + "\t" +
									homeActivity.getCoord().getY()
							));
						}
					}
				}

				PlanAverageScore planAverageScore = new PlanAverageScore();
				plansSubPop.addAlgorithm(planAverageScore);
				CalcAverageTripLength calcAverageTripLength = new CalcAverageTripLength();
				plansSubPop.addAlgorithm(calcAverageTripLength);
				plansSubPop.runAlgorithms();

				Events events = new Events();

				CalcLegTimes calcLegTimes = new CalcLegTimes(plansSubPop);
				events.addHandler(calcLegTimes);

				results.add(new CaseStudyResult(scenarioName, plansSubPop, calcLegTimes, planAverageScore, calcAverageTripLength));

				EventsReaderDEQv1 eventsReader = new EventsReaderDEQv1(events);
				log.info("events filename: " + eventsInputFilenames.get(scenarioName));
				eventsReader.readFile(eventsInputFilenames.get(scenarioName));

			}
			scenarioComparisonLines.add("Analysis: " + analysisNames.get(analysis));
			this.writeComparison(results);
			scenarioComparisonLines.add("");

		}

	}

	private void writeComparison(List<CaseStudyResult> results) {

		scenarioComparisonLines.add("casestudy\tn_{agents}\tscore_{avg}\tt_{trip, avg}\td_{trip, avg}[m]");

		for (CaseStudyResult result : results) {

			scenarioComparisonLines.add( 
					result.getName() + "\t" + 
					result.getRouteSwitchers().getPersons().size() + "\t" + 
					result.getRouteSwitchersAverageScore().getAverage() + "\t" + 
					Time.writeTime(result.calcLegTimes.getAverageTripDuration()) + "\t" +
					result.getCalcAverageTripLength().getAverageTripLength()
			);

		}

	}
}
