import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

class TA extends Thread {
	PipedOutputStream toC;
		
	PipedInputStream fromB;
	PipedInputStream fromC;

	ObjectInputStream readerA;
		
	TA() throws IOException {
		toC = new PipedOutputStream();
			
		fromB = new PipedInputStream();
		fromC = new PipedInputStream();
	}
		
	TA(PipedOutputStream outC, 
			PipedInputStream inB, PipedInputStream inC,
			ObjectInputStream read) {
		toC = outC;
		fromB = inB;
		fromC = inC;
		readerA = read;
	}
		

	public void run() { 
		System.out.println("A- Started");
		try {
		//OUTGOING
			//TA sends primitive data to TC
			System.out.println("A- Sending primitive to C: " + 1);
			toC.write(1);
			toC.flush();
			toC.close();
				
				
		//INCOMING
			//TA receives objects from TB
			readerA = new ObjectInputStream(fromB);//conversion
			System.out.println("A- Receiving object from B { " + '\n' + readerA.readObject().toString());
			readerA.close();
			
			//TA receives objects from TC
			readerA = new ObjectInputStream(fromC);//conversion
			System.out.println("A- Receiving object from C { " + '\n' + readerA.readObject().toString());
			readerA.close();
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
