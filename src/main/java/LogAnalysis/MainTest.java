package LogAnalysis;

import java.io.IOException;

public class MainTest {
    public static void main(String[] args){
        try {
            LogAnalysis test = new LogAnalysis();
            test.aggregate("data/logs/logs.txt", "data/lookup.csv", "output.txt");
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
