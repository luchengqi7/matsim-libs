/* *********************************************************************** *
 * project: org.matsim.*
 * RemoveDuplicatePlans.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

/**
 * 
 */
package playground.johannes.eut;

import org.apache.log4j.Logger;
import org.matsim.controler.events.BeforeMobsimEvent;
import org.matsim.controler.listener.BeforeMobsimListener;
import org.matsim.population.Leg;
import org.matsim.population.Person;
import org.matsim.population.Plan;

/**
 * @author illenberger
 *
 */
public class RemoveDuplicatePlans implements BeforeMobsimListener {

	private static final Logger log = Logger.getLogger(RemoveDuplicatePlans.class);
	
//	private Map<Person, Plan> selected;
//	
//	public void notifyIterationStarts(IterationStartsEvent event) {
//		selected = new HashMap<Person, Plan>();
//		for(Person p : event.getControler().getPopulation()) {
//			selected.put(p, p.getSelectedPlan());
//		}
//
//	}

	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		int counter = 0;
		for (Person p : event.getControler().getPopulation()) {
			Plan selected = p.getSelectedPlan();
			int cnt = p.getPlans().size();
			for(int i = 0; i < cnt; i++) {
				Plan plan = p.getPlans().get(i);
				if(selected != plan) {
					if(comparePlans(selected, plan)) {
						p.getPlans().remove(i);
						i--;
						cnt--;
						counter++;
					}
				}
			}
			
			
		}
		log.warn("Removed " + counter +" plans.");
	}

	private boolean comparePlans(Plan plan1, Plan plan2) {
		if (plan1.getActsLegs().size() > 1 && plan2.getActsLegs().size() > 1) {
			boolean plansDiffer = false;
			
			for (int i = 1; i < plan1.getActsLegs().size(); i += 2) {
				Leg leg2 = (Leg) plan2.getActsLegs().get(i);
				Leg leg1 = (Leg) plan1.getActsLegs().get(i);
				/*
				 * Compare sequence of nodes.
				 */
				if (leg2.getRoute().getRoute().equals(
						leg1.getRoute().getRoute())) {
					/*
					 * Compare departure times.
					 */
					if (leg2.getDepTime() != leg1.getDepTime()) {
						plansDiffer = true;
						break;
					}
				} else {
					plansDiffer = true;
					break;
				}
			}
			
			return !plansDiffer;
			
		} else
			return false;
	}
}
