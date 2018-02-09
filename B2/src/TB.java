import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedOutputStream;

class TB extends Thread {
	PipedOutputStream toA;
	PipedOutputStream toC;
	
	ObjectOutputStream writerA;
		
	TB() throws IOException {
		toA = new PipedOutputStream();
		toC = new PipedOutputStream();
	}
		
	TB(PipedOutputStream outA, PipedOutputStream outC, 
			ObjectOutputStream write) {
		toA = outA;
		toC = outC;
		writerA = write;
	}
		
	public void run() { 
		System.out.println("B- Started");
		try {
		//OUTGOING
			//TB will send objects to TA
			Message m = new Message();
			m.mess = "Hi A!";
			String[] arr = {"It's", "B!"};
			m.line = arr;
			m.num = 22;
				
			System.out.println("B- Sending object to A { " + '\n' + m.toString());
			writerA = new ObjectOutputStream(toA);//conversion
			writerA.writeObject(m);
			writerA.flush();
			writerA.close();
				
			//TB will send primitive data to TC
			System.out.println("B- Sending primitive to C: " + 2);
			toC.write(2);
			toC.flush();
			toC.close();
				
				
		//INCOMING
			////
		}
			
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}