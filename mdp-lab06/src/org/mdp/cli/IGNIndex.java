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
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
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
public class IGNIndex {

	public enum FieldNames {
		URL, TITLE, MODIFIED, DESCRIPTION
	}

	public static int TICKS = 10000;

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

		indexTitleAndAbstract(br, fDir);

		br.close();
	}

	public static void indexTitleAndAbstract(BufferedReader input, File indexDir) throws IOException{
	  
	  //Open a Lucene directory over index dir
      Directory dir = FSDirectory.open(indexDir);
      
      //Spanish analyser, a index write config with Version.LUCENE_48
      Analyzer analizer = new SpanishAnalyzer(Version.LUCENE_48);
      
      // Configures how to index will be written
      IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analizer);
      
      // We want to create a index -> Pass CREATE. 
      iwc.setOpenMode(OpenMode.CREATE);
      
      // Open a new indexwriter with given config and dir
      IndexWriter writer = new IndexWriter(dir,iwc);
      
      String line = null;
      int read = 0;
      while ((line = input.readLine()) != null){
        read ++;
        if (read%TICKS== 0){
          System.err.println("... read" + read);
        }
      }
	  
	  /*		
		//starting reading the input data
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
				
				// tabs[0] is URL, tabs[1] is title, tabs[2] is abstract
				// some articles may not have an abstract
				// if a line does not have at least a title, skip it
				if(tabs.length>=2){
					//create a Lucene document
					Document d = new Document();
					
					// Note: in the following, to reference field names, 
					// use e.g., FieldNames.URL.name() to ensure consistency.
					
					//index URL as a string (stored), and title as text (stored)
					Field url = new StringField(FieldNames.URL.name(),tabs[0],Field.Store.YES);
					d.add(url);
					
					
	                //TODO if available, index abstract as text (stored)
					Field title = new TextField(FieldNames.TITLE.name(), tabs[1], Field.Store.YES);
					d.add(title);
					
					
					// Saco un texto y le digo que no lo almacene...
					if (tabs.length>2){
  					  Field text = new TextField(FieldNames.ABSTRACT.name(), tabs[2], Field.Store.NO);
  					  d.add(text);
					}
					
	                //TODO also index the time the document was added (unstored)
                    //       (System.currentTimeMilliseconds())
					Field modified = new LongField(FieldNames.MODIFIED.name(), System.currentTimeMillis(), Field.Store.NO);
					d.add(modified);
					
					//TODO add the document to the writer
					writer.addDocument(d);
					
				} else{
					System.err.println("Skipping partial line : '"+line+"'");
				}
				
			}
		}
		
		// Close the write and print a done message with
		// number of lines read
		
		writer.close();
		System.out.println("Number of readed lines: " + read );
		
	*/
		
	}
}