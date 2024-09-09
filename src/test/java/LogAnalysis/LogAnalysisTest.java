package LogAnalysis;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class LogAnalysisTest {

    private void cleanEnviorment(String filename){
        File correctOutput = new File(filename);
        if(correctOutput.exists()){
            correctOutput.delete();
        }
    }
    @Test
    void testCorrect(){
        // prepare enviorment
        String outputfile = "data/test/correctoutput.txt";
        cleanEnviorment(outputfile);
        //Begin test
        assertDoesNotThrow( () ->{
            LogAnalysis logAnalysis = new LogAnalysis();
            logAnalysis.aggregate("data/test/logs.txt", "data/lookup.csv", outputfile);
        });
        // benchmark(correct) values
        HashMap<String, Integer> correctTagCounts = new HashMap<>();
        correctTagCounts.put("sv_p2", 2);
        correctTagCounts.put("sv_p1", 2);
        correctTagCounts.put("sv_p5", 2);
        correctTagCounts.put("email", 3);
        correctTagCounts.put("testtag", 1);
        correctTagCounts.put("testtagtwo", 1);
        correctTagCounts.put("Untagged", 9);

        HashMap<String, Integer> correctPairCounts = new HashMap<>();
        correctPairCounts.put("993,tcp", 1);
        correctPairCounts.put("33,rsvp-e2e-ignore", 1);
        correctPairCounts.put("110,tcp", 1);
        correctPairCounts.put("49154,tcp", 1);
        correctPairCounts.put("80,tcp", 1);
        correctPairCounts.put("143,tcp", 1);
        correctPairCounts.put("49156,tcp", 1);
        correctPairCounts.put("49158,tcp", 1);
        correctPairCounts.put("1024,tcp", 1);
        correctPairCounts.put("49153,tcp", 1);
        correctPairCounts.put("68,gmtp", 1);
        correctPairCounts.put("42,reserved", 1);
        correctPairCounts.put("49155,tcp", 1);
        correctPairCounts.put("25,tcp", 1);
        correctPairCounts.put("443,tcp", 1);
        correctPairCounts.put("68,udp", 1);
        correctPairCounts.put("23,tcp", 1);
        correctPairCounts.put("49157,tcp", 1);
        correctPairCounts.put("3389,tcp", 2);

        assertDoesNotThrow( () -> {
            HashMap<String, Integer> testCounts = new HashMap<>();
            String line;
            BufferedReader logInput = new BufferedReader(new FileReader(outputfile));
            logInput.readLine(); // buffer
            logInput.readLine();
            //read tag values
            while (!(line = logInput.readLine()).equals("Port/Protocol Combination Counts:")) { // end of tag location
                String[] values = line.split(",");
                testCounts.put(values[0], Integer.valueOf(values[1]));
            }
            //check tag values
            assertEquals(testCounts.size(), correctTagCounts.size());
            for(String key : testCounts.keySet()){
                System.out.println(key);
                assertEquals(testCounts.get(key), correctTagCounts.get(key));
            }
            testCounts = new HashMap<>();
            logInput.readLine();
            //read pair values
            while ((line = logInput.readLine()) != null) { // end of tag location
                String[] values = line.split(",");
                testCounts.put(values[0] + "," + values[1], Integer.valueOf(values[2]));
            }
            //check pair values
            assertEquals(testCounts.size(), correctPairCounts.size());
            for(String key : testCounts.keySet()){
                System.out.println(key);
                assertEquals(testCounts.get(key), correctPairCounts.get(key));
            }
            logInput.close();
        });
    }

    @Test
    void testEmptyFiles(){
        //clean up past test
        String outputfile= "data/test/emptyOutput.txt";
        cleanEnviorment(outputfile);
        //begin test
        assertDoesNotThrow( () ->{
            LogAnalysis logAnalysis = new LogAnalysis();
            logAnalysis.aggregate("data/test/emptyLog.txt", "data/lookup.csv", outputfile);
        });
        assertDoesNotThrow( () -> {
            HashMap<String, Integer> testCounts = new HashMap<>();
            String line;
            BufferedReader logInput = new BufferedReader(new FileReader(outputfile));
            logInput.readLine();
            logInput.readLine();
            while (!(line = logInput.readLine()).equals("Port/Protocol Combination Counts:")) { // end of tag location
                String[] values = line.split(",");
                testCounts.put(values[0], Integer.valueOf(values[1]));
            }
            assertEquals(testCounts.size(), 0); // empty tags
            logInput.readLine();
            while ((line = logInput.readLine()) != null) { // end of tag location
                String[] values = line.split(",");
                testCounts.put(values[0] + "," + values[1], Integer.valueOf(values[2]));
            }
            assertEquals(testCounts.size(), 0); // empty pairs
            logInput.close();
        } );
    }
    @Test
    void testNoCollidingFilesTest(){
        String doublePath = "data/test/doubleoutput.txt";
        String singlePath = "data/test/singleoutput.txt";
        cleanEnviorment(doublePath);
        cleanEnviorment(singlePath);
        //Begin test
        LogAnalysis logAnalysis = new LogAnalysis();
        assertDoesNotThrow( () ->{
            logAnalysis.aggregate("data/test/logs.txt", "data/lookup.csv", doublePath);
        });
        assertDoesNotThrow( () ->{
            logAnalysis.aggregate("data/test/logs.txt", "data/lookup.csv", singlePath);
        });
        //the duplicate
        assertThrows(FileAlreadyExistsException.class, ()->
                    logAnalysis.aggregate("data/test/logs.txt", "data/lookup.csv", doublePath));
    }

    @Test
    void testInvalidInput(){
        //Begin test
        LogAnalysis logAnalysis = new LogAnalysis();
        assertThrows(IOException.class, ()->
                logAnalysis.aggregate("InvalidLog", "data/lookup.csv", "placeholder.txt"));
        assertThrows(IOException.class, ()->
                logAnalysis.aggregate("data/test/logs.txt", "Invalidlookup", "placeholder.txt"));
        Exception nullEx = assertThrows(NullPointerException.class, ()->
                logAnalysis.aggregate(null, "Invalidlookup", "placeholder.txt"));
        assertEquals(nullEx.getMessage(), "Invalid filename: logFileName");
        nullEx = assertThrows(NullPointerException.class, ()->
                logAnalysis.aggregate("data/test/logs.txt", null, "placeholder.txt"));
        assertEquals(nullEx.getMessage(), "Invalid filename: lookupFileName");
        nullEx = assertThrows(NullPointerException.class, ()->
                logAnalysis.aggregate(null, null, "placeholder.txt"));
        assertEquals(nullEx.getMessage(), "Invalid filename: lookupFileName logFileName");
    }
}