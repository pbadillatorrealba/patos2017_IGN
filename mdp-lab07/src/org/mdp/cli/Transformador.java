package org.mdp.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Transformador {
  public static final String SEPARATOR=",";
  public static final String QUOTE="\"";
  
  public static void main(String[] args) throws IOException {
    
    BufferedReader br = null;
    BufferedWriter bw = null;
    try {
      
      br =new BufferedReader(new FileReader("test/medio/links/juegos.csv"));
      bw = new BufferedWriter (new FileWriter("test/medio/links/links1.txt"));
      String line = br.readLine();
      line = br.readLine();
      System.out.println(line);
      while (null!=line) {
        final String [] fields = line.split(";");
        final String[] juegos= fields[1].split(",");
        for (int x = 0; x < juegos.length ; x++){
          bw.write(fields[0] + "\t"+ juegos[x] +"\n");
        }
        line = br.readLine();
      }
      
    }
    catch (final Exception e) {
    } finally {
      if (null!=br) {
        br.close();
        bw.close();
        
      }
    }
  }
}
