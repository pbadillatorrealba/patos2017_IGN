package org.mdp.cli;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Apply PageRank over a graph.
 * 
 * @author Aidan
 */
public class PageRankGraph {
  
  public static int TICKS = 100000;
  
  // damping factor
  public static double D = 0.85d;
  
  // number of iterations
  public static double ITERS = 10;
  
  public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{
    final Option inO = new Option("i", "input file");
    inO.setArgs(1);
    inO.setRequired(true);
    
    final Option ingzO = new Option("igz", "input file is GZipped");
    ingzO.setArgs(0);
    
    final Option outO = new Option("o", "output file");
    outO.setArgs(1);
    outO.setRequired(true);
    
    final Option outgzO = new Option("ogz", "output file should be GZipped");
    outgzO.setArgs(0);
    
    final Option helpO = new Option("h", "print help");
    
    final Options options = new Options();
    options.addOption(inO);
    options.addOption(ingzO);
    options.addOption(outO);
    options.addOption(outgzO);
    options.addOption(helpO);
    
    final CommandLineParser parser = new BasicParser();
    CommandLine cmd = null;
    
    try {
      cmd = parser.parse(options, args);
    } catch (final ParseException e) {
      System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("parameters:", options );
      return;
    }
    
    // print help options and return
    if (cmd.hasOption("h")) {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("parameters:", options );
      return;
    }
    
    // here we get the command line arguments
    final String in = cmd.getOptionValue(inO.getOpt());
    final boolean igz = cmd.hasOption(ingzO.getOpt());
    final String out = cmd.getOptionValue(outO.getOpt());
    final boolean ogz = cmd.hasOption(outgzO.getOpt());
    
    pageRank(in, igz, out, ogz);
  }
  
  public static void pageRank(String in, boolean igz, String out, boolean ogz) throws IOException{
    InputStream is = new FileInputStream(in);
    if(igz){
      is = new GZIPInputStream(is);
    }
    final BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
    
    // here we simply open the output
    OutputStream os = new FileOutputStream(out);
    if(ogz){
      os = new GZIPOutputStream(os);
    }
    final PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os),"utf-8"));
    
    System.err.println("Writing ranks to "+out);
    
    System.err.println("Reading from "+in);
    
    String line = null;
    
    // the format should be the output of an OIDCompress
    int read = 1;
    int size;
    try{
      size = Integer.parseInt(br.readLine());
    } catch(final Exception e){
      System.err.println("Graph size not on first line. Use OIDCompress to encode graph first.");
      br.close();
      pw.close();
      throw e;
    }
    
    
    // we will load the graph into memory (hopefully)
    final int[][] graph = new int[size][];
    
    while((line = br.readLine())!=null){
      line = line.trim();
      if(!line.isEmpty()){
        final String[] tab = line.split("\t");
        try{
          // from --links-> to
          final int from = Integer.parseInt(tab[0]);
          final int to = Integer.parseInt(tab[1]);
          
          // this will save the links a page links to
          // in an int[]
          //
          // everytime we find a new link, we need to
          // increase the array capacity
          //
          // could make more efficient than resizing array
          // every time (e.g., by requiring input to be sorted
          // by inlinking node) but okay for now ...
          int[] outlinks = graph[from];
          if(outlinks == null){
            outlinks = new int[0];
          }
          
          // increase capacity and add new out-link
          final int[] newoutlinks = new int[outlinks.length+1];
          System.arraycopy(outlinks, 0, newoutlinks, 0, outlinks.length);
          newoutlinks[outlinks.length] = to;
          graph[from] = newoutlinks;
        } catch(final Exception e){
          System.err.println("Error reading edge from line "+line);
        }
      }
      read++;
      if(read%TICKS==0) {
        System.err.println("... read "+read);
      }
    }
    System.err.println("Finished loading graph! Read "+read+" lines: "+graph.length+" nodes and "+read+" edges.");
    
    System.err.println("Ranking graph ...");
    // here we actually rank the graph
    final double[] ranks = rankGraph(graph);
    
    System.err.println("Writing output ...");
    int written = 0;
    for(written=0; written<ranks.length; written++){
      pw.println(written+"\t"+ranks[written]);
      if(written%TICKS==0) {
        System.err.println("... written "+written);
      }
    }
    System.err.println("Finished writing ranks! Wrote "+written+" ranks.");
    
    pw.close();
    br.close();
  }
  
  public static double[] rankGraph(int[][] graph){
    final int n = graph.length;
    final double[] ranks = new double[n];
    final double[] oldranks = new double[n];
    
    for (int i= 0 ;i<oldranks.length ;i++  ){
      oldranks[i] = (double)1/n;
    }
    
    for (int i =0 ; i < ITERS; i++){
      
      for(int reset =0; reset < ranks.length ; reset++){ // Resetea el valor de ranking a cero
        ranks[reset]=0;
      }
      
      for (int ind = 0 ; ind< n ;ind++ ){
        final int [] out = graph[ind];
        if (out != null){
          for (int ot = 0; ot < out.length;ot++ ){
            ranks[out[ot]]+= oldranks[ind]/out.length;
          }
        }
      }
      
      double r2 = 0 ;
      double r3  = 0;
      for (int j =0 ;j<n ; j++ ){
        if (graph[j] == null) {
          r2 += oldranks[j];
        } else {
          r3 += oldranks[j];
        }
      }
      r2 = r2/n;
      r3= (1 - D)*r3/n;
      
      for(int j =0; j < n ; j++){
        ranks[j] = D*ranks[j] + r2 + r3;
      }
      
      
      /*
       
      double x = 0 ;
      for (int j = 0 ; j<n; j++){
        x+= ranks[j];
      }
      final double[] diferencia = new double[n];
      for (int j = 0 ; j<n ;j++){
        diferencia[j] = Math.abs(ranks[j]- oldranks[j]);
      }
      
      System.out.println(x);
      System.out.println(Arrays.toString(diferencia));
      
       */
      
      for (int j = 0 ; j<n ;j++){
        oldranks[j] = ranks[j];
      }
      
    }
    
    return ranks;
  }
  
}