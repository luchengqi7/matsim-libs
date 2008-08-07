/* *********************************************************************** *
 * project: org.matsim.*
 * RoutingTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.run;

import java.io.File;

import org.matsim.basic.v01.IdImpl;
import org.matsim.config.Config;
import org.matsim.config.ConfigWriter;
import org.matsim.gbl.Gbl;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.Act;
import org.matsim.population.MatsimPlansReader;
import org.matsim.population.Person;
import org.matsim.population.Plan;
import org.matsim.population.Plans;
import org.matsim.population.PlansWriter;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.world.World;

/**
 * Simple test case to ensure that {@link org.matsim.run.XY2Links} functions properly, e.g. really 
 * writes out modified plans. It does <em>not</em> test that {@link org.matsim.population.algorithms.XY2Links}
 * works correctly, e.g. that it assigns the right links.
 *
 * @author mrieser
 */
public class XY2LinksTest extends MatsimTestCase {

	public void testMain() throws Exception {
		Config config = loadConfig(null);
		final String NETWORK_FILE = "test/scenarios/equil/network.xml";
		final String PLANS_FILE_TESTINPUT = getOutputDirectory() + "plans.in.xml";
		final String PLANS_FILE_TESTOUTPUT = getOutputDirectory() + "plans.out.xml";
		final String CONFIG_FILE = getOutputDirectory() + "config.xml";

		// prepare data like world and network
		World world = Gbl.createWorld();
		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).parse(NETWORK_FILE);
		world.setNetworkLayer(network);
		
		// create one person with missing link in act
		Plans population = new Plans(Plans.NO_STREAMING);
		Person person = new Person(new IdImpl("1"));
		population.addPerson(person);
		Plan plan = person.createPlan(true);
		plan.createAct("h", 50, 25, null, 0, 3600, 3600, false);
		
		// write person to file
		new PlansWriter(population, PLANS_FILE_TESTINPUT, "v4").write();

		// prepare config for test
		config.network().setInputFile(NETWORK_FILE);
		config.plans().setInputFile(PLANS_FILE_TESTINPUT);
		config.plans().setOutputFile(PLANS_FILE_TESTOUTPUT);
		new ConfigWriter(config, CONFIG_FILE).write();
		Gbl.reset(); // needed to delete the global config etc for the test
		
		// some pre-tests
		assertFalse("Output-File should not yet exist.", new File(PLANS_FILE_TESTOUTPUT).exists());
		
		// now run the tested class
		XY2Links.main(new String[] {CONFIG_FILE});
		
		// now perform some tests
		assertTrue("no output generated.", new File(PLANS_FILE_TESTOUTPUT).exists());
		Plans population2 = new Plans(Plans.NO_STREAMING);
		new MatsimPlansReader(population2).parse(PLANS_FILE_TESTOUTPUT);
		assertEquals("wrong number of persons.", 1, population2.getPersons().size());
		Person person2 = population2.getPerson(new IdImpl("1"));
		assertNotNull("person 1 missing", person2);
		assertEquals("wrong number of plans in person 1", 1, person2.getPlans().size());
		Plan plan2 = person2.getPlans().get(0);
		Act act2 = (Act) plan2.getActsLegs().get(0);
		assertNotNull("no link assigned.", act2.getLink());
	}

}