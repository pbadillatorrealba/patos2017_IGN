package org.mdp.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.rmi.AlreadyBoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Main method to boost the ranks of documents in the inverted index
 * according to their PageRank score.
 * 
 * @author Aidan
 */
public class BoostRanks {

	public enum FieldNames {
	  URL, TITLE, DESCRIPTION, GENRES, PLATAFORMS, IGN_SCORE, COMMUNITY_SCORE, REVIEW_URL, PUBLISHER, DEVELOPERS, RATING_CATEGORY, RELEASE_DATE, PRICE, RANK
    }

	public static int TICKS = 10000;

	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
		Option inO = new Option("i", "input ranks file");
		inO.setArgs(1);
		inO.setRequired(true);

		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);

		Option outO = new Option("o", "output index directory");
		outO.setArgs(1);

		Option helpO = new Option("h", "print help");

		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
		options.addOption(outO);
		options.addOption(helpO);

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

		String dir = cmd.getOptionValue("o");
		System.err.println("Opening directory at  "+dir);
		File fDir = new File(dir);
		if(fDir.exists()){
			if(fDir.isFile()){
				throw new IOException("Cannot open directory at "+dir+" since its already a file.");
			} 
		} else{
			if(!fDir.mkdirs()){
				throw new IOException("Cannot open directory at "+dir+". Try create the directory manually.");
			}
		}
		
		String in = cmd.getOptionValue(inO.getOpt());
		System.err.println("Opening input at  "+in);
		InputStream is = new FileInputStream(in);
		if(cmd.hasOption(ingzO.getOpt())){
			is = new GZIPInputStream(is);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));

		// this just loads a map of 
		Map<String,Double> ranks = loadRanks(br);
		br.close();
		
		boostRanks(ranks, fDir);
	}
	
	/**
	 * Loads the ranks from the input file into memory and returns
	 * a map from article URLs to PageRanks.
	 * 
	 * @param input
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static Map<String,Double> loadRanks(BufferedReader input) throws NumberFormatException, IOException{
		Map<String,Double> ranks = new HashMap<String,Double>();
		
		String line = null;
		int read = 0;
		while((line = input.readLine())!=null){
			read++;
			if(read%TICKS==0){
				System.err.println("... read "+read);
			}
			
			line = line.trim();
			
			if(!line.isEmpty()){
				String[] tabs = line.split("\t");
				ranks.put(tabs[0],Double.parseDouble(tabs[1]));
			}
		}
		
		System.err.println("Finished! Read "+read+" ranks");
		return ranks;
	}

	/** 
	 * Opens the Lucene inverted index at the given location and uses the ranks
	 * to boast their scores in the search results.
	 * @param ranks
	 * @param indexDir
	 * @throws IOException
	 */
	public static void boostRanks(Map<String,Double> ranks, File indexDir) throws IOException{
		Directory dir = FSDirectory.open(indexDir);
		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_48);

		// open writer 
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		iwc.setOpenMode(OpenMode.APPEND);
		IndexWriter writer = new IndexWriter(dir, iwc);
		
		// open reader
		IndexReader reader = DirectoryReader.open(FSDirectory.open(indexDir));
		
		int read = 0;
		for (int i=0; i<reader.maxDoc(); i++) {
			
			read++;
			if(read%TICKS==0){
				System.err.println("... read "+read);
			}
			
			Document doc = reader.document(i);
			IndexableField urlf = doc.getField(FieldNames.URL.name());
			
			String url = urlf.stringValue();
			Double rankD = ranks.get(url);
			
			if(rankD==null){
				rankD = 0d;
			}
			
			float boost = getBoost(rankD);
			
			Field rankField = new DoubleField(FieldNames.RANK.name(), rankD, Field.Store.YES);
			doc.add(rankField);
			
			IndexableField title = doc.getField(FieldNames.TITLE.name());
			((Field)title).setBoost(boost);

			Term urlT = new Term(FieldNames.URL.name(),url);
			writer.updateDocument(urlT,doc);
			
			
			
			
		}		
				
		System.err.println("Finished! Read "+read+" docs");
		
		writer.close();
	}
	
	/**
	 * Converts a raw PageRank score into a reasonable
	 * boost value. Uses some "magic numbers". Other values
	 * are of course possible.																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																								`																									`																								
	 * 
	 * @param rank
	 * @return
	 */
	public static float getBoost(double rank){
		return ((float)Math.log(rank * 100000) + 1);
	}
}