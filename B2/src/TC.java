import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

class TC extends Thread {
	PipedOutputStream toA;
		
	PipedInputStream fromA;
	PipedInputStream fromB;
		
	ObjectOutputStream writerC;
		
	TC() throws IOException {
		toA = new PipedOutputStream();
			
		fromA = new PipedInputStream();
		fromB = new PipedInputStream();
	}
		
	TC(PipedOutputStream outA, 
			PipedInputStream inA, PipedInputStream inB,
			ObjectOutputStream write) {
		toA = outA;
		fromA = inA;
		fromB = inB;
		writerC = write;
	}
		
	public void run() {
		System.out.println("C- Started");
		try {
		//OUTGOING
			//TC sends objects to TA
			Message m = new Message();
			m.mess = "Hi A!";
			String[] arr = {"It's", "C!"};
			m.line = arr;
			m.num = 33;
				
			System.out.println("C- Sending object to A { " + '\n' + m.toString());
			writerC = new ObjectOutputStream(toA);//conversion
			writerC.writeObject(m);
			writerC.flush();
			writerC.close();
				
				
		//INCOMING
			//TC receives primitive data from TA
			System.out.println("C- Receiving primitive from A: " + fromA.read());
			fromA.close();
			
			//TC receives primitive data from TB
			System.out.println("C- Receiving primitive from B: " + fromB.read());
			fromB.close();
		}
			
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}