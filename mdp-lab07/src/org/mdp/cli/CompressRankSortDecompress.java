package org.mdp.cli;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.yars.nx.util.NxUtil;

/**
 * Compress the graph to ints 
 * Run pagerank
 * Sort by rank
 * Decompress the graph
 * 
 * @author Aidan
 */
public class CompressRankSortDecompress {
	
	public static int TICKS = 100000;
	
	public static String OID_COMPRESS = "links.oid";
	public static String OID_COMPRESS_D = "links.dict";
	public static String OID_DECOMPRESS = "ranks.oid";
	public static String RANKS = "ranks.oid";
	public static String SORT = "ranks.s.oid";
	public static String SORT_DEC = "ranks.s";
	public static String GZ_SUFFIX = ".gz";
	
	public static String[] DECOMP = { "0" };
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
		Option inO = new Option("i", "input file");
		inO.setArgs(1);
		inO.setRequired(true);
		
		Option ingzO = new Option("igz", "input file is GZipped");
		ingzO.setArgs(0);
		
		Option outO = new Option("o", "output directory");
		outO.setArgs(1);
		outO.setRequired(true);
		
		Option outgzO = new Option("ogz", "output file should be GZipped");
		outgzO.setArgs(0);
		
		Option helpO = new Option("h", "print help");
				
		Options options = new Options();
		options.addOption(inO);
		options.addOption(ingzO);
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
		String out = cmd.getOptionValue(outO.getOpt());
		boolean ogz = cmd.hasOption(outgzO.getOpt());
		
		File dirf = new File(out);
		dirf.mkdirs();
		
		OIDCompress.oidCompress(in, igz, addDirGz(out,OID_COMPRESS_D,ogz), ogz, addDirGz(out,OID_COMPRESS,ogz), ogz);
		PageRankGraph.pageRank(addDirGz(out,OID_COMPRESS,ogz), ogz, addDirGz(out,RANKS,ogz), ogz);
		SortByRank.sortByRank(addDirGz(out,RANKS,ogz), ogz, addDirGz(out,SORT,ogz), ogz);
		OIDDecompress.oidDecompress(addDirGz(out,SORT,ogz), ogz, addDirGz(out,OID_COMPRESS_D,ogz), ogz, addDirGz(out,SORT_DEC,ogz), ogz, DECOMP);
	}
	
	public static String unescapeNxExceptTab(String str){
		return NxUtil.unescape(str).replaceAll("\t", "\\t");
	}
	
	public static String addDirGz(String dir, String file, boolean gz){
		String path = dir+"/"+file;
		if(gz){
			path+=GZ_SUFFIX;
		}
		return path;
	}
}