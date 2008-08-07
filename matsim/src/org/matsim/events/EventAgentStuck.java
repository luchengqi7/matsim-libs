/* *********************************************************************** *
 * project: org.matsim.*
 * EventAgentStuck.java
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

package org.matsim.events;

import org.matsim.network.Link;
import org.matsim.population.Leg;
import org.matsim.population.Person;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class EventAgentStuck extends AgentEvent {

	public EventAgentStuck(double time, String agentId, int legId, String linkId, Person agent, Leg leg, Link link) {
		super(time, agentId, legId, linkId, agent, leg, link);
	}

	public EventAgentStuck(double time, String agentId, int legId, String linkId) {
		super(time, agentId, legId, linkId);
	}

	@Override
	public Attributes getAttributes() {
		AttributesImpl impl = getAttributesImpl();
		//impl.addAttribute("","","Flag", "", Integer.toString(3));
		impl.addAttribute("","","type", "", "stuckAndAbort");
		return impl;
	}

	@Override
	public String toString() {
		return asString() + "3\tstuckAndAbort";
	}

}
