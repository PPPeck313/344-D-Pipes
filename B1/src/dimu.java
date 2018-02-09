import Utilities.*;
import Synchronization.*;

class Message { 
   public int number, id;
   public Message(int number, int id) { this.number = number; this.id = id;}
}

class Node extends MyObject implements Runnable {

   private static final int MAIN = 0, REQUESTS = 1, REPLIES = 2;
   private int whichOne = 0;

   private int id = -1;
   private int numNodes = -1;
   private int napOutsideCS = 0; // both are in
   private int napInsideCS = 0;  // milliseconds
   private MessagePassing[] requestChannel = null;
   private MessagePassing[] replyChannel = null;
   private MessagePassing requestsToMe = null;
   private MessagePassing repliesToMe = null;
   private int number = 0;
   private int highNumber = 0;
   private boolean requesting = false;
   private int replyCount = 0;
   private BinarySemaphore s = new BinarySemaphore(1);
   private BinarySemaphore wakeUp = new BinarySemaphore(0);
   private boolean[] deferred = null;

   public Node(String name, int id, int numNodes,
         int napOutsideCS, int napInsideCS,
         MessagePassing[] requestChannel, MessagePassing replyChannel[],
         MessagePassing requestsToMe, MessagePassing repliesToMe) {
      super(name + " " + id);
      this.id = id;
      this.numNodes = numNodes;
      this.napOutsideCS = napOutsideCS;
      this.napInsideCS = napInsideCS;
      this.requestChannel = requestChannel;
      this.replyChannel = replyChannel;
      this.requestsToMe = requestsToMe;
      this.repliesToMe = repliesToMe;
      deferred = new boolean[numNodes];
      for (int i = 0; i < numNodes; i++) deferred[i] = false;
      System.out.println(getName() + " is alive, napOutsideCS="
         + napOutsideCS + ", napInsideCS=" + napInsideCS);
      new Thread(this).start();
   }

   public void run() { // start three different threads in the same object
      int meDo = whichOne++;
      if (meDo == MAIN) {
         new Thread(this).start();
         main();
      } else if (meDo == REQUESTS) {
         new Thread(this).start();
         handleRequests();
      } else if (meDo == REPLIES) {
         handleReplies();
      }
   }

   private void chooseNumber() {
      P(s);
      requesting = true;
      number = highNumber + 1;
      V(s);
   }

   private void sendRequest() {
      replyCount = 0;
      for (int j = 0; j < numNodes; j++) if (j != id)
         send(requestChannel[j], new Message(number, id));
   }

   private void waitForReply() {
      P(wakeUp);
   }

   private void replyToDeferredNodes() {
      P(s);
      requesting = false;
      V(s);
      for (int j = 0; j < numNodes; j++) {
         if (deferred[j]) {
            deferred[j] = false;
            send(replyChannel[j], id);
         }
      }
   }

   private void outsideCS() {
      int napping;
      napping = ((int) random(napOutsideCS)) + 1;
      System.out.println("age()=" + age() + ", " + getName()
         + " napping outside CS for " + napping + " ms");
      nap(napping);
   }

   private void insideCS() {
      int napping;
      napping = ((int) random(napInsideCS)) + 1;
      System.out.println("age()=" + age() + ", " + getName()
         + " napping inside CS for " + napping + " ms");
      nap(napping);
   }

   private void main() {
      while (true) {
         outsideCS();
         System.out.println("age()=" + age() + ", node " + id
            + " wants to enter its critical section");
         chooseNumber();               // PRE-PROTOCOL
         sendRequest();                //      "
         waitForReply();               //      "
         insideCS();
         System.out.println("age()=" + age() + ", node " + id
            + " has now left its critical section");
         replyToDeferredNodes();       // POST-PROTOCOL
      }
   }

   private void handleRequests() {
      while (true) {
         Message m = (Message) receive(requestsToMe);
         int receivedNumber = m.number;
         int receivedID = m.id;
         highNumber = Math.max(highNumber, receivedNumber);
         P(s);
         boolean decideToDefer = requesting && (number < receivedNumber
            || (number == receivedNumber && id < receivedID));
         if (decideToDefer) deferred[receivedID] = true;
         else send(replyChannel[receivedID], id);
         V(s);
      }
   }

   private void handleReplies() {
      while (true) {
         int receivedID = receiveInt(repliesToMe);
         replyCount++;
         if (replyCount == numNodes - 1) V(wakeUp);
      }
   }
}

class DistributedMutualExclusion extends MyObject {

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Un:R:");
      String usage = "Usage: -n numNodes -R runTime"
         + " napOutsideCS[i] napInsideCS[i] i=0,1,...";
      go.optErr = true;
      int ch = -1;
      int numNodes = 5;
      int runTime = 60;      // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'n')
            numNodes = go.processArg(go.optArgGet(), numNodes);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("DistributedMutualExclusion: numNodes="
         + numNodes + ", runTime=" + runTime);

      // process non-option command line arguments
      int[] napOutsideCS = new int[numNodes];
      int[] napInsideCS = new int[numNodes];
      int argNum = go.optIndexGet();
      for (int i = 0; i < numNodes; i++) {
         napOutsideCS[i] = go.tryArg(argNum++, 8);
         napInsideCS[i] = go.tryArg(argNum++, 2);
      }
      // create communication channels
      MessagePassing[] requestChannel = null, replyChannel = null,
         requestChannelS = null, requestChannelR = null,
         replyChannelS = null, replyChannelR = null;
      requestChannel = new MessagePassing[numNodes];
      replyChannel = new MessagePassing[numNodes];
      requestChannelS = new MessagePassing[numNodes];
      replyChannelS = new MessagePassing[numNodes];
      requestChannelR = new MessagePassing[numNodes];
      replyChannelR = new MessagePassing[numNodes];
      for (int i = 0; i < numNodes; i++) {
         requestChannel[i] = new AsyncMessagePassing();
         replyChannel[i] = new AsyncMessagePassing();
         requestChannelS[i] = new MessagePassingSendOnly(requestChannel[i]);
         replyChannelS[i] = new MessagePassingSendOnly(replyChannel[i]);
         requestChannelR[i] = new MessagePassingReceiveOnly(requestChannel[i]);
         replyChannelR[i] = new MessagePassingReceiveOnly(replyChannel[i]);
      }

      // create the Nodes (they start their own threads)
      for (int i = 0; i < numNodes; i++)
         new Node("Node", i, numNodes,
            napOutsideCS[i]*1000, napInsideCS[i]*1000,
            requestChannelS, replyChannelS,
            requestChannelR[i], replyChannelR[i]);
      System.out.println("All Nodes created");

      // let the Nodes run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}