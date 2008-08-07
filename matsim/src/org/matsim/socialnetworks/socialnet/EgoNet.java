/* *********************************************************************** *
 * project: org.matsim.*
 * EgoNet.java
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

package org.matsim.socialnetworks.socialnet;

import java.util.ArrayList;
import java.util.Iterator;

import org.matsim.gbl.Gbl;
import org.matsim.population.Person;

/*
 * The purpose of the EgoNet class is to avoid extending the Person class.
 * An EgoNet consists of an ArrayList of SocialNetEdges that connect to Persons.
 * The ID number of the EgoNet is the ID of a Person (unique identifier).
 * The EgoNets are mapped in EgoNetMap, which is a TreeMap<Integer, EgoNet>
 * in which the integer is the Person.getId() that the EgoNet corresponds to.
 */
public class EgoNet {
    private int id;

    private ArrayList<SocialNetEdge> egoLinks;

    public EgoNet(int id) {
	this.id = id;
	egoLinks = new ArrayList<SocialNetEdge>();
    }
    public EgoNet() {
	egoLinks = new ArrayList<SocialNetEdge>();
    }

    public void addEgoLink(SocialNetEdge link) {
	egoLinks.add(link);
    }

    public void removeEgoLink(SocialNetEdge link){
	egoLinks.remove(link);
    }

    public SocialNetEdge getEgoLink(Person p2) {
	SocialNetEdge myEdge = null;
	Iterator<SocialNetEdge> linkIter = egoLinks.iterator();
	while (linkIter.hasNext()) {
	    myEdge = linkIter.next();
	    if (myEdge.getPersonTo().equals(p2)) {
		break;
	    }
	}
	return myEdge;
    }

    /**
     * @param person
     * @return <code>true</code> if person is already part of our social context
     */
    public boolean knows( Person person ) {
	for( SocialNetEdge edge : egoLinks ){
	    if( edge.getPersonFrom().equals(person) )
		return true;
	    if( edge.getPersonTo().equals(person))
		return true;
	}
	return false;
    }

    public int getOutDegree(){
	return this.getEgoLinks().size();
    }

    public Person getRandomPerson( Person me ) {
	int size = egoLinks.size();
	if( size == 0 )
	    return null;
	SocialNetEdge edge = egoLinks.get( Gbl.random.nextInt(size));
	if( edge.getPersonFrom().equals(me) )
	    return edge.getPersonTo();
	return edge.getPersonFrom();
    }

    public ArrayList<SocialNetEdge> getEgoLinks() {
	return this.egoLinks;
    }

    public int getId() {
	return id;
    }

    public ArrayList<Person> getAlters() {
	ArrayList<Person> alterList= new ArrayList<Person>();
	for (SocialNetEdge myEdge : egoLinks) {
	    Person me = myEdge.getPersonTo();
	    alterList.add(me);
	}
	return alterList;
    }
}
