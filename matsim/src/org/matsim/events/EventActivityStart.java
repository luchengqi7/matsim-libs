/* *********************************************************************** *
 * project: org.matsim.*
 * EventActivityStart.java
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
import org.matsim.population.Act;
import org.matsim.population.Person;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class EventActivityStart extends ActEvent {

	public EventActivityStart(double time, String agentId, Person agent, Link link, Act act) {
		super(time, agentId, agent, link, act);
	}

	public EventActivityStart(double time, String agentId, String linkId, int actId, String acttype) {
		super(time, agentId, linkId, actId, acttype);
	}

	@Override
	public Attributes getAttributes() {
		AttributesImpl impl = getAttributesImpl();
		//impl.addAttribute("","","Flag", "", Integer.toString(7));
		impl.addAttribute("","","type", "", "actstart");
		return impl;
	}

	@Override
	public String toString() {
		return asString() + "7\tactstart"+ " " + this.acttype;
	}

}
