/* *********************************************************************** *
 * project: org.matsim.*
 * MATSimNet2ShapeDemo.java
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
package playground.yu.utils.qgis;

import org.apache.log4j.Logger;
import org.matsim.gbl.Gbl;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.utils.geometry.geotools.MGC;
import org.matsim.utils.gis.matsim2esri.network.CapacityBasedWidthCalculator;
import org.matsim.utils.gis.matsim2esri.network.FeatureGeneratorBuilder;
import org.matsim.utils.gis.matsim2esri.network.LanesBasedWidthCalculator;
import org.matsim.utils.gis.matsim2esri.network.LineStringBasedFeatureGenerator;
import org.matsim.utils.gis.matsim2esri.network.Network2ESRIShape;
import org.matsim.utils.gis.matsim2esri.network.PolygonFeatureGenerator;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This class is a copy of main() from
 * org.matsim.utils.gis.matsim2esri.network.Network2ESRIShape and can convert a
 * MATSim-network to a QGIS .shp-file (link or polygon)
 * 
 * @author ychen
 * 
 */
public class MATSimNet2QGISDemo implements X2QGIS {

	public static void main(final String[] args) {
		String netfile = "../schweiz-ivtch-SVN/baseCase/network/ivtch-osm.xml";
		String outputFileLs = "../schweiz-ivtch-SVN/baseCase/network/ivtch-osm_Links.shp";
		String outputFileP = "../schweiz-ivtch-SVN/baseCase/network/ivtch-osm_Polygon.shp";
		String coordinateSys = ch1903;

		Gbl.createConfig(null);
		Gbl.getConfig().global().setCoordinateSystem(coordinateSys);

		Logger log = Logger.getLogger(Network2ESRIShape.class);
		log.info("loading network from " + netfile);
		final NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(netfile);
		log.info("done.");

		FeatureGeneratorBuilder builder = new FeatureGeneratorBuilder(network);
		builder
				.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		builder.setWidthCoefficient(0.5);
		builder.setWidthCalculatorPrototype(LanesBasedWidthCalculator.class);
		new Network2ESRIShape(network, outputFileLs, builder).write();

		CoordinateReferenceSystem crs = MGC.getCRS(coordinateSys);
		builder.setWidthCoefficient(0.001);
		builder.setFeatureGeneratorPrototype(PolygonFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(CapacityBasedWidthCalculator.class);
		builder.setCoordinateReferenceSystem(crs);
		new Network2ESRIShape(network, outputFileP, builder).write();
	}
}
