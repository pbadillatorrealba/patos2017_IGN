package org.mdp.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mdp.cli.BuildWikiIndex.FieldNames;

/**
 * Main method to search articles using Lucene.
 * 
 * @author Aidan
 */
public class SearchWikiIndex {

	public static final HashMap<String,Float> BOOSTS = new HashMap<String,Float>();
	static {
		BOOSTS.put(FieldNames.ABSTRACT.name(), 1f); //<- default
		BOOSTS.put(FieldNames.TITLE.name(), 5f); 
	}

	public static final int DOCS_PER_PAGE  = 10;

	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
		Option inO = new Option("i", "input index directory");
		inO.setArgs(1);
		inO.setRequired(true);

		Options options = new Options();
		options.addOption(inO);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}

		// print help options and return
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}

		String in = cmd.getOptionValue(inO.getOpt());
		System.err.println("Opening directory at  "+in);

		startSearchApp(in);
	}

	/**
	 * 
	 * @param in : the location of the index directory
	 * @throws IOException
	 */
	public static void startSearchApp(String in) throws IOException {
		// TODO open a reader for the directory


		// TODO open a searcher over the reader

		
		// TODO use the same analyser as the build

		// TODO create a multi-field query parser for title and abstract
		// set BOOSTS values above

		// opens utf-8 stream from command line
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "utf-8"));

		while (true) {
			System.out.println("Enter a keyword search phrase:");

			String line = br.readLine();
			if(line!=null){
				line = line.trim();
				if(!line.isEmpty()){
					try{
						// TODO parse query
						
						
						// TODO print raw query and parsed query object
						
						// TODO get DOCS_PER_PAGE hits 
						
						
						// TODO print number of matching documents
						
						// TODO for each hit, get its details and print them (title, abstract, etc.)

					} catch(Exception e){
						System.err.println("Error with query '"+line+"'");
						e.printStackTrace();
					}
				}
			}

		}
	}
}