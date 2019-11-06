
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {

  private static Functions f;

  public static void main(String[] args) throws IOException {

    f = new Functions(); 

    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

      String line;
      
      // read transition matrix A
      line = br.readLine();
      double[][] A = f.readMatrix(line); 
      
      // Read emission matrix B
      line = br.readLine();
      double[][] B = f.readMatrix(line);

      // read initial state probability pi
      line = br.readLine();
      double[] pi = f.readVector(line, 1);

      // read emissions O
      line = br.readLine();
      double[] emissions = f.readVector(line, 0);

      System.out.println("Convergence defined with Matrix Distance");
      f.baumWelchMatrixDistance(A, B, pi, emissions);
      System.out.println();
      System.out.println("Convergence defined with Log Prob");
      f.baumWelchLogProb(A, B, pi, emissions);
    }
  }




}
