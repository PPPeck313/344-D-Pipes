import java.io.*;

//2b. Using pipes create communication between three threads TA and TB and TC
public class Peck_4 {		
	public static void main(String args[]) throws IOException {
		System.out.println("Connecting pipes...");
		PipedOutputStream posAC = new PipedOutputStream();
		PipedInputStream pisCA = new PipedInputStream(posAC);
		
		PipedOutputStream posBA = new PipedOutputStream();
		PipedInputStream pisAB = new PipedInputStream(posBA);
		
		PipedOutputStream posBC = new PipedOutputStream();
		PipedInputStream pisCB = new PipedInputStream(posBC);
		
		PipedOutputStream posCA = new PipedOutputStream();
		PipedInputStream pisAC = new PipedInputStream(posCA);
		
		ObjectOutputStream oos = null;//b, C
		ObjectInputStream ois = null;//A
		
		System.out.println("Initializing threads...");
		TA a = new TA(posAC, pisAB, pisAC, ois);
		TB b = new TB(posBA, posBC, oos);
		TC c = new TC(posCA, pisCA, pisCB, oos);
		
		System.out.println("Starting threads...");
		a.start();
		b.start();
		c.start();
	}
}