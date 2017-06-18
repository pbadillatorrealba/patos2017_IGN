package org.mdp.cli;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.yars.nx.util.NxUtil;

/**
 * Convert full string URLs to numeric Object IDs.
 * 
 * @author Aidan
 */
public class OIDCompress {
	
	public static int TICKS = 100000;
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
		Option inO = new Option("i", "input file");
		inO.setArgs(1);
		inO.setRequired(true);
		
		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);
		
		Option dictO = new Option("d", "output directory file");
		dictO.setArgs(1);
		dictO.setRequired(true);
		
		Option dictgzO = new Option("dgz", "output directory file should be GZipped");
		dictgzO.setArgs(0);
		
		Option outO = new Option("o", "output file");
		outO.setArgs(1);
		outO.setRequired(true);
		
		Option outgzO = new Option("ogz", "output file should be GZipped");
		outgzO.setArgs(0);
		
		Option helpO = new Option("h", "print help");
				
		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
		options.addOption(dictO);
		options.addOption(dictgzO);
		options.addOption(outO);
		options.addOption(outgzO);
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
		
		String in = cmd.getOptionValue(inO.getOpt());
		boolean igz = cmd.hasOption(ingzO.getOpt());
		String dict = cmd.getOptionValue(dictO.getOpt());
		boolean dgz = cmd.hasOption(dictgzO.getOpt());
		String out = cmd.getOptionValue(outO.getOpt());
		boolean ogz = cmd.hasOption(outgzO.getOpt());
		
		oidCompress(in, igz, dict, dgz, out, ogz);
	}
	
	public static void oidCompress(String in, boolean igz, String dict, boolean dgz, String out, boolean ogz) throws IOException{
		InputStream is = new FileInputStream(in);
		if(igz){
			is = new GZIPInputStream(is);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
		
		System.err.println("Reading from "+in);
		
		OutputStream dos = new FileOutputStream(dict);
		if(dgz){
			dos = new GZIPOutputStream(dos);
		}
		PrintWriter dpw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(dos),"utf-8"));
		
		System.err.println("Writing dictionary to "+dict);
		
		
		OutputStream os = new FileOutputStream(out);
		if(ogz){
			os = new GZIPOutputStream(os);
		}
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os),"utf-8"));
		
		System.err.println("Writing encoded output to "+out);
		
		// this will map strings to integer IDs
		// the tree map is sorted
		String line = null;
		TreeMap<String,Integer> allNodes = new TreeMap<String,Integer>();
		
		System.err.println("Preparing dictionary ...");
		
		// first we simply read the strings into the map
		// with the value one
		//
		// we need to read both the left and right string
		int read = 0;
		while((line = br.readLine())!=null){
			String[] tab = line.split("\t");
			allNodes.put(tab[0],1);
			allNodes.put(tab[1],1);
			read++;
			if(read%TICKS==0)
				System.err.println("... read "+read);
		}
		
		br.close();
		System.err.println("Read "+read+". Loaded dictionary of size "+allNodes.size()+". Writing to file ...");
		
		
		int oid =  0;
		// the first line of dictionary will give the number of terms
		// this may come in handy when reading it later
		dpw.println(allNodes.size());
		
		for(String key:allNodes.keySet()){
			// we update the ID based on the nodes
			// order in the set
			allNodes.put(key, oid);	
			
			// and print the ID to the dictionary
			dpw.println(oid+"\t"+key);
			
			oid++;
			if(oid%TICKS==0)
				System.err.println("... written "+oid);
		}
		System.err.println("Written dictionary of size "+oid+" to file.");
		
		// we have finished writing the dictionary
		// (the file with the map from strings to integers)
		// now we need to actually encode the input file
		
		is = new FileInputStream(in);
		if(igz){
			is = new GZIPInputStream(is);
		}
		br = new BufferedReader(new InputStreamReader(is, "utf-8"));
		
		System.err.println("Reading again from "+in+" to encode ...");
		
		read = 0;
		line = null;
		
		// print the number of nodes as the first line
		pw.println(allNodes.size());
		while((line = br.readLine())!=null){
			// then print the encoded edges
			line = line.trim();
			if(!line.isEmpty()){
				String[] tab = line.split("\t");
				int oid1 = allNodes.get(tab[0]);
				int oid2 = allNodes.get(tab[1]);
				pw.println(oid1+"\t"+oid2);
				read++;
				if(read%TICKS==0)
					System.err.println("... read "+read);
			}
		}
		
		
		System.err.println("Finished! Written "+read+" encoded lines with "+allNodes.size()+" nodes");
		
		dpw.close();
		pw.close();
		br.close();
	}
	
	public static String unescapeNxExceptTab(String str){
		return NxUtil.unescape(str).replaceAll("\t", "\\t");
	}
}