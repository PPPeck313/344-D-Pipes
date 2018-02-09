import java.io.*;
import java.util.*;

/**
  A thread that writes random numbers to an output stream.
*/
class Producer extends Thread {
  	private DataOutputStream out;
  	private Random rand = new Random();
  	
    /**
      Constructs a producer thread.
      @param os the output stream
    */
	public Producer(OutputStream os) { 
		out = new DataOutputStream(os);
	}
	
	public void run() { 
		while (true) { 
			try { 
				double num = rand.nextDouble();
				out.writeDouble(num);
				out.flush();
				sleep(Math.abs(rand.nextInt() % 1000));
			}
     
			catch(Exception e) { 
				System.out.println("Error: " + e);
			}
		}
	}
}