/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dynagent.run;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimAgent.State;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;

import com.google.common.base.Preconditions;

/**
 * DynActivityEngine is not an ActivityEngine (as only one is allowed)
 */
public class DynActivityEngine implements MobsimEngine, ActivityHandler {
	public final static String COMPONENT_NAME = "DynActivityEngine";

	private InternalInterface internalInterface;

	private final List<DynAgent> dynAgents = new LinkedList<>();
	private final List<DynAgent> newDynAgents = new ArrayList<>();// will to be handled in the next timeStep

	// See handleActivity for the reason for this.
	private boolean beforeFirstSimStep = true;

	@Override
	public void doSimStep(double time) {
		beforeFirstSimStep = false;
		dynAgents.addAll(newDynAgents);
		newDynAgents.clear();

		Iterator<DynAgent> dynAgentIter = dynAgents.iterator();
		while (dynAgentIter.hasNext()) {
			DynAgent agent = dynAgentIter.next();
			Preconditions.checkState(agent.getState() == State.ACTIVITY);
			agent.doSimStep(time);
			// ask agents about the current activity end time;
			double currentEndTime = agent.getActivityEndTime();

			if (currentEndTime == Double.POSITIVE_INFINITY) { // agent says: stop simulating me
				unregisterAgentAtActivityLocation(agent);
				internalInterface.getMobsim().getAgentCounter().decLiving();
				dynAgentIter.remove();
			} else if (currentEndTime <= time) { // the agent wants to end the activity NOW
				unregisterAgentAtActivityLocation(agent);
				agent.endActivityAndComputeNextState(time);
				internalInterface.arrangeNextAgentState(agent);
				dynAgentIter.remove();
			}
		}
	}

	@Override
	public boolean handleActivity(MobsimAgent agent) {
		if (!(agent instanceof DynAgent)) {
			return false; // (this means "I am not responsible").
		}

		double endTime = agent.getActivityEndTime();
		double currentTime = internalInterface.getMobsim().getSimTimer().getTimeOfDay();

		if (endTime == Double.POSITIVE_INFINITY) {
			// This is the last planned activity.
			// So the agent goes to sleep.
			internalInterface.getMobsim().getAgentCounter().decLiving();
		} else if (endTime <= currentTime && !beforeFirstSimStep) {
			// This activity is already over (planned for 0 duration)
			// So we proceed immediately.
			agent.endActivityAndComputeNextState(currentTime);
			internalInterface.arrangeNextAgentState(agent);
		} else {
			// The agent commences an activity on this link.
			if (beforeFirstSimStep) {
				dynAgents.add((DynAgent)agent);
			} else {
				newDynAgents.add((DynAgent)agent);
			}

			internalInterface.registerAdditionalAgentOnLink(agent);
		}

		return true;
	}

	@Override
	public void afterSim() {
		dynAgents.clear();
	}

	@Override
	public void setInternalInterface(InternalInterface internalInterface) {
		this.internalInterface = internalInterface;
	}

	private void unregisterAgentAtActivityLocation(final MobsimAgent agent) {
		Id<Person> agentId = agent.getId();
		Id<Link> linkId = agent.getCurrentLinkId();
		if (linkId != null) { // may be bushwacking
			internalInterface.unregisterAdditionalAgentOnLink(agentId, linkId);
		}
	}

	@Override
	public void onPrepareSim() {
	}

	@Override
	public void rescheduleActivityEnd(MobsimAgent agent) {
	}
}
