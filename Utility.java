import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utility {
    // page brake utility function
    //            if (i % 10 == 0)
    //                enterToContinue();
    public static void enterToContinue(){
        System.out.print(" Press Enter to see the next 10 tokens.");
        try{
            BufferedReader tempHalt = new BufferedReader(new InputStreamReader(System.in));
            tempHalt.readLine();
        } catch (IOException e){
            System.out.println(e);
        }
    }
}
