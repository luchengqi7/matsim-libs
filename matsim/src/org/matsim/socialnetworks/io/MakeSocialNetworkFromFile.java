package org.matsim.socialnetworks.io;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.log4j.Logger;
import org.matsim.population.Person;
import org.matsim.population.Plans;
import org.matsim.socialnetworks.socialnet.SocialNetEdge;
import org.matsim.socialnetworks.socialnet.SocialNetwork;

//structured this way, one can read several edge files in for a set of plans
//and the edges will be added accordingly. so nets can be superposed.

public class MakeSocialNetworkFromFile {

	private Plans plans;
	private SocialNetwork snet;
	private final Logger log = Logger.getLogger(MakeSocialNetworkFromFile.class);

	public MakeSocialNetworkFromFile(SocialNetwork snet, Plans plans){
		// initialize a new SocialNetwork -- uses config params
		this.plans=plans;
		this.snet=snet;
	}
	public void read(String fileName, int iterToLoad){
		BufferedReader br = null;

		try // If an error occurs, go to the "catch" block
		{   // FileInputStream fis = new FileInputStream (fileName);
			FileReader fis = new FileReader (fileName);
			br = new BufferedReader (fis);

			// Open the file that is the first command line parameter
			// FileInputStream fstream = new FileInputStream(args[0]);

			// Convert our input stream to a DataInputStream
			// DataInputStream in = new DataInputStream(fstream);

			// Continue to read lines while there are still some left to read
			String thisLineOfData=null;
			int i=0; // counter for the number of lines
			while ((thisLineOfData=br.readLine()) != null)
			{
				if(i>0){ // first line is the header with the variable names
//					iter tlast tfirst dist egoid alterid purpose timesmet
					String[] s;
					String patternStr = " ";// separator in "edge.txt" is a space
					s = thisLineOfData.split(patternStr);

					int iter = Integer.valueOf(s[0]).intValue();
//					int tlast = Integer.valueOf(s[1]).intValue();//JH this will be 0 in a read-in network and incremented during the new run
//					int tfirst = Integer.valueOf(s[2]).intValue();//JH this will be 0 in a read-in network
//					double  dist= Double.valueOf(s[3]).doubleValue();//JH this will be recalculated automatically
					String egoId = s[4];
					String alterId = s[5];
					String purpose = s[6];
					int timesmet = Integer.valueOf(s[7]).intValue();

					if(iterToLoad == iter){
						//fill the social network
						Person person1 = plans.getPerson(egoId);
						Person person2 = plans.getPerson(alterId);
						snet.makeSocialContact(person1, person2, 0, purpose);
						SocialNetEdge thisEdge = person1.getKnowledge().getEgoNet().getEgoLink(person2);
						thisEdge.setNumberOfTimesMet(timesmet);
					}
				}
				i++;
				if(i%1000==0){log.info("   Edge "+i);}
			}

			// Close the input stream
			br.close();
		}

		// Handle errors in opening the file
		catch (Exception e)
		{   System.err.println(" File input error: "+fileName);
		}
	}
}
