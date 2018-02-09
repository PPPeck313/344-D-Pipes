import java.io.*;
/**
  This program demonstrates how multiple threads communicate
  through pipes.
*/
public class PipeTest { 
	public static void main(String args[]) { 
		try { 
			/* set up pipes */
			PipedOutputStream pout1 = new PipedOutputStream();
			
			PipedInputStream pin1 = new PipedInputStream(pout1);
			PipedOutputStream pout2 = new PipedOutputStream();
			
			PipedInputStream pin2 = new PipedInputStream(pout2);
	
			/* construct threads */
			Producer prod = new Producer(pout1);
			Filter filt = new Filter(pin1, pout2);
			Consumer cons = new Consumer(pin2);
	
			/* start threads */
			prod.start();
			filt.start();
			cons.start();
		}
		
		catch (IOException e){}
	}
}