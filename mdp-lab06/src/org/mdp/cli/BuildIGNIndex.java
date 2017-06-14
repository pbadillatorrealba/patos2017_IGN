package org.mdp.cli;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Main method to index plain-text IGN games descriptions from IGN using Lucene.
 * 
 * @author Grupo13
 */
public class BuildIGNIndex {

	public enum FieldNames {
		URL, TITLE, DESCRIPTION, GENRES, PLATAFORMS, IGN_SCORE, COMMUNITY_SCORE, REVIEW_URL, PUBLISHER, DEVELOPERS, RATING_CATEGORY, RELEASE_DATE, PRICE
	}

	public static int TICKS = 500;

	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
		Option inO = new Option("i", "input file");
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
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));

		indexTitleAndDescription(br, fDir);

		br.close();
	}

	public static void indexTitleAndDescription(BufferedReader input, File indexDir) throws IOException{
	  
	  //Open a Lucene directory over index dir
      Directory dir = FSDirectory.open(indexDir);
      
      //English analyser, a index write config with Version.LUCENE_48
      Analyzer analizer = new EnglishAnalyzer(Version.LUCENE_48);
      
      // Configures how to index will be written
      IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analizer);
      
      // We want to create a index -> Pass CREATE. 
      iwc.setOpenMode(OpenMode.CREATE);
      
      // Open a new indexwriter with given config and dir
      IndexWriter writer = new IndexWriter(dir,iwc);
      

    //starting reading the input data
      String line = input.readLine(); // Header...
      System.err.println("Header:... " + line);
      int read = 0;
      while ((line = input.readLine()) != null){
        read++;
        if (read%TICKS==0){
          System.err.println("... read " + read);
        }
        line = line.trim();
        if (!line.isEmpty()){
          String[] data = line.split("\t"); 
          /* Data index
          0.  'title',
          1.  'url',
          2.  'description',
          3.  'related_games',
            
          4.  'genres',
          5.  'platforms',
        
          6.  'ign_score',
          7.  'ign_score_phrase',
          8.  'community_score',
          9.  'community_score_phrase',
            
          10. 'publisher',
          11. 'developers',
          12. 'rating_category',
          13. 'rating_content',
            
          14. 'release_date',
          15. 'price',
    
          16. 'review_link',

          */
          // DEBUG...
          
          //if (data[4].length() > 50){
          //  System.out.println("Error in read " + read + " with title " + data[0] + ": " + data[4]);
          //  System.out.println("Actual: Title-" + data[0] + "...Description: " + data[2]  + "...URL: " + data[1] );

          // Si Title, description o URL no son vacios...
          if (data[0].length() > 3 && !data[0].isEmpty() && !data[1].isEmpty() && !data[2].isEmpty()){  
            Document d = new Document();
            
            // Note: in the following, to reference field names, 
            // use e.g., FieldNames.URL.name() to ensure consistency.
            
            //index URL as a string (stored), and title as text (stored)
            Field url = new StringField(FieldNames.URL.name(),data[1],Field.Store.YES);
            d.add(url);
            
            // title
            Field title = new TextField(FieldNames.TITLE.name(), data[0], Field.Store.YES);
            d.add(title);
            
            // description
            Field text = new TextField(FieldNames.DESCRIPTION.name(), data[2], Field.Store.YES);
            d.add(text);
            
            // genres
            Field genres = new TextField(FieldNames.GENRES.name(), data[4], Field.Store.YES);
            d.add(genres);
            
            // plataforms 
            Field plataforms = new TextField(FieldNames.PLATAFORMS.name(), data[5], Field.Store.YES);
            d.add(plataforms);
            
            // ign_score
            Field ign_score = new TextField(FieldNames.IGN_SCORE.name(), data[6], Field.Store.YES);
            d.add(ign_score);
            
            // community_socre
            Field community_score = new TextField(FieldNames.COMMUNITY_SCORE.name(), data[8], Field.Store.YES);
            d.add(community_score);
            
            // review_link
            Field review_url = new TextField(FieldNames.REVIEW_URL.name(), data[16], Field.Store.YES);
            d.add(review_url);
            
            // 
            Field publisher = new TextField(FieldNames.PUBLISHER.name(), data[10], Field.Store.YES);
            d.add(publisher);
            
            Field developers = new TextField(FieldNames.DEVELOPERS.name(), data[11], Field.Store.YES);
            d.add(developers);
            
            Field rating_category = new TextField(FieldNames.RATING_CATEGORY.name(), data[12], Field.Store.YES);
            d.add(rating_category);
            
            Field release_date = new TextField(FieldNames.RELEASE_DATE.name(), data[14], Field.Store.YES);
            d.add(release_date);
            
            Field price = new TextField(FieldNames.PRICE.name(), data[15], Field.Store.YES);
            d.add(price);
            
            // Add the document to the writer
            writer.addDocument(d);
            
          }  else {
            System.err.println("Skipping line : '"+line+"'");
          }
        }
      }
      
      // Close the write and print a done message with
      // number of lines read
      
      writer.close();
      System.out.println("Number of readed lines: " + read );
	}
}
