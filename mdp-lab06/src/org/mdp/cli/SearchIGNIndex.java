package org.mdp.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.util.*;

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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.mdp.cli.BuildIGNIndex.FieldNames;

/**
 * Main method to search articles using Lucene.
 * 
 * @author Aidan
 */
public class SearchIGNIndex {

  public static final HashMap<String, Float> BOOSTS = new HashMap<String, Float>();
  static {
    BOOSTS.put(FieldNames.DESCRIPTION.name(), 1f); // <- default
    BOOSTS.put(FieldNames.TITLE.name(), 5f);
  }

  public static final int DOCS_PER_PAGE = 5;

  public static void main(String args[]) throws IOException, ClassNotFoundException,
      AlreadyBoundException, InstantiationException, IllegalAccessException {
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
      formatter.printHelp("parameters:", options);
      return;
    }

    // print help options and return
    if (cmd.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("parameters:", options);
      return;
    }

    String in = cmd.getOptionValue(inO.getOpt());
    System.err.println("Opening directory at  " + in);

    startSearchApp(in);
  }

  /**
   * 
   * @param in : the location of the index directory
   * @throws IOException
   */
  public static void startSearchApp(String in) throws IOException {


    // Open a reader for the directory
    File fDir = new File(in);
    if (fDir.exists()) {
      if (fDir.isFile()) {
        throw new IOException("Cannot open directory at " + in + " since its already a file.");
      }
    } else {
      if (!fDir.mkdirs()) {
        throw new IOException(
            "Cannot open directory at " + in + ". Try create the directory manually.");
      }
    }

    Directory dir = FSDirectory.open(fDir);
    IndexReader rd = DirectoryReader.open(dir);
    // Open a searcher over the reader
    IndexSearcher searcher = new IndexSearcher(rd);

    // Use the same analyser as the build
    Analyzer analyzer = new SpanishAnalyzer(Version.LUCENE_48);
    // Create a multi-field query parser for title and abstract

    MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_48,
        new String[] {FieldNames.TITLE.name(), FieldNames.DESCRIPTION.name()}, analyzer, BOOSTS);
    // set BOOSTS values above

    // opens utf-8 stream from command line
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "utf-8"));

    // TODO : Limpiar el codigo
    while (true) {
      System.out.println("Enter a keyword search phrase or enter to close:");

      String line = br.readLine();
      if (line != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          try {
            // parse query
            Query query = queryParser.parse(line);

            HashMap<String, HashMap<String, String>> documentsMap =
                new HashMap<String, HashMap<String, String>>();

            // Print raw query and parsed query object
            // DEBUG:
            //System.out.println("Running Query: " + line);
            //System.out.println("Parsed Query: " + query.toString(line));


            // get DOCS_PER_PAGE hits
            TopDocs results = searcher.search(query, DOCS_PER_PAGE);
            ScoreDoc[] hits = results.scoreDocs;


            //
            if (hits.length != 0) {


              // Print number of matching documents
              System.out.println("Matching Documents: " + results.totalHits);
              // TODO

              int showingDocs = hits.length > 10 ? 10 : hits.length;
              System.out.println("Showing top " + showingDocs + " results:\n\n");



              // For each hit, get its details and print them (title, abstract, etc.)
              for (int i = 0; i < hits.length; i++) {
                Document doc = searcher.doc(hits[i].doc);

                // Save document data in HashMap
                HashMap<String, String> documentMap = new HashMap<String, String>();
                documentMap.put("title", doc.get(FieldNames.TITLE.name()));
                documentMap.put("url", doc.get(FieldNames.URL.name()));
                documentMap.put("description", doc.get(FieldNames.DESCRIPTION.name()));
                documentMap.put("genre", doc.get(FieldNames.GENRES.name()));
                documentMap.put("plataforms", doc.get(FieldNames.PLATAFORMS.name()));
                documentMap.put("ign_score", doc.get(FieldNames.IGN_SCORE.name()));
                documentMap.put("community_score", doc.get(FieldNames.COMMUNITY_SCORE.name()));
                documentMap.put("review_url", doc.get(FieldNames.REVIEW_URL.name()));

                // Set document in documents storage.
                documentsMap.put("result" + i, documentMap);

                // Print Documents Result.
                System.out.println("Result " + (i + 1) + ":");
                System.out.println("\tTitle: " + documentMap.get("title"));
                // TODO implementar una breve descripción
                System.out.println("\tDescription: " + documentMap.get("description") + "\n");

              }

              while (true) {
                System.out
                    .println("Enter the number of the desired game or 0 to return to search menu:");

                int option = -100;

                // TODO : Verificar que sea menor que DOCS_PER_PAGE y ademas que sea menor que el
                // numero de juegos encontrados en la query.
                while ((option = Integer.parseInt(br.readLine()) - 1) < -1
                    || option >= showingDocs) {
                  System.out.println("Enter a valid game number or 0 to return to search menu:");
                }

                // Break with 0.
                if (option == -1) {
                  break;
                }
                // TODO Verificar si los campos son nulos para no imprimirlos...

                HashMap<String, String> selectedResult = documentsMap.get("result" + option);

                System.out.println("\tTitle: " + selectedResult.get("title"));
                System.out.println("\tDescription: " + selectedResult.get("description"));
                System.out.println("\tUrl: " + selectedResult.get("url"));
                System.out.println("\tGenre: " + selectedResult.get("genre"));
                System.out.println("\tPlataform(s): " + selectedResult.get("plataforms"));
                System.out.println("\tIGN Score: " + selectedResult.get("ign_score"));
                System.out.println("\tCommunity Score: " + selectedResult.get("community_score"));
                System.out.println("\tReview URL: " + selectedResult.get("review_url"));
                System.out.println("");
              }
            } else {
              System.out.println("Matching Documents: 0");
            }

          } catch (Exception e) {
            System.err.println("Error with query '" + line + "'");
            e.printStackTrace();
          }
          
        } else {
          break;
        }
      }
    }
  }
}
