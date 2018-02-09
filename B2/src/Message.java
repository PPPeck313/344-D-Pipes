import java.io.Serializable;

public class Message  implements Serializable{
  public String mess;
  public String[] line;
  public int num;
  
  @Override
  public String toString() {
    String s = "Message: " + mess +"\nwith an array: ";
    for ( int i = 0 ; i < line.length ; i++ ) {
      s += line[i] + " ";
    } 

    s += "\nand a magic #: " + num + " }";

    return s;  
  }
  
}
