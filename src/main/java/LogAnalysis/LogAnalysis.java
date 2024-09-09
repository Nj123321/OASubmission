package LogAnalysis;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LogAnalysis {

    private final static HashMap<String, String> protocolMapping;

    static{
        Logger.info("initialize mapping from protocol name to protocol integer representation in logs in protocol-numbers.csv");
        protocolMapping = new HashMap<>();
        try {
            //file is downloaded from aws and has to be manually updated
            BufferedReader protocolReader = new BufferedReader(new FileReader("data/protocol-numbers.csv"));
            String buffer;
            while ((buffer = protocolReader.readLine()) != null) {
                buffer = buffer.toLowerCase();
                String[] protocolLayer = buffer.split(",");
                // some edge cases in csv protocol defintions of missing protocol name
                if(protocolLayer[0] != null && protocolLayer[1] != null && !protocolLayer[1].isEmpty()){
                    protocolMapping.put(protocolLayer[0], protocolLayer[1]);
                }
            }
            protocolReader.close();
        } catch (IOException e){
            Logger.error("error in reading in protocol mapping csv");
        }
    }

    public void Parser(){}
    /**
     * Creates two files, one file contains the counts of every tag in the  AWS Flow Logs in logFileName
     * specified by the matchset defined in matchFileName, and another file for every pair of port/protocol(in this case)
     * @param logFileName filepath for Flow Log Records
     * @param lookupFileName filepath for match set/table in CSV
     */
    public void aggregate(String logFileName, String lookupFileName, String outputFileName) throws IOException, FileAlreadyExistsException {
        if(logFileName == null || lookupFileName == null){
            String invalidFileName = "";
            if(lookupFileName == null){
                invalidFileName += "lookupFileName";
            }
            if(logFileName == null){
                if(lookupFileName == null)
                    invalidFileName += " ";
                invalidFileName += "logFileName";
            }
            Logger.error("Error in input file name: " + invalidFileName);
            throw new NullPointerException("Invalid filename: " + invalidFileName);
        }
        if(protocolMapping.size() == 0){ // failed to intailize in static intializer
            return; // already notified in log
        }
        HashMap<String, String>tagMapping = new HashMap<>();
        HashMap<String, Integer> pairCount = new HashMap<>(); // keep in mind buffer overflow
        HashMap<String, Integer> tagCount = new HashMap<>();
        Logger.info("Reading in lookup file" + lookupFileName);
        BufferedReader matchInput = new BufferedReader(new FileReader(lookupFileName));
        String line = matchInput.readLine();
        while ((line = matchInput.readLine()) != null) {
            line = line.toLowerCase();
            String[] tags = line.split(",");
            String[] bufff = new String[]{tags[0], tags[1].toLowerCase()};
            tagMapping.put(
                    hasher(bufff),
                    tags[2]
            );
        }
        matchInput.close();
        Logger.info("Parsing log file" + logFileName);
        BufferedReader logInput = new BufferedReader(new FileReader(logFileName));
        while ((line = logInput.readLine()) != null) {
            String[] logs;
            line = line.toLowerCase();
            if ((logs = parseEntry(line)) != null) { // checks for valid log entrys
                String[] pair = new String[]{logs[6], logs[7]};
                // Count Tag in log
                String tag = tagMapping.get(hasher(pair));
                if(tag == null){
                    tag = "Untagged";
                }
                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                //Count pair in log
                pairCount.put(hasher(pair), pairCount.getOrDefault(hasher(pair), 0) + 1);
            } else { //skin & log record if invalid
                Logger.error("warning! in parsing record: " + line);
            }
        }
        logInput.close();
        Logger.info("Constructing counts of tags for output" + outputFileName);
        List<String> output = new ArrayList<>();
        output.add("Tag Counts:");
        output.add("Tag,Count");
        for(String pairs : tagCount.keySet()){
            output.add(pairs + "," + String.valueOf(tagCount.get(pairs)));
        }
        Logger.info("Constructing unique protocol/port combinations for output" + outputFileName);
        output.add("Port/Protocol Combination Counts:");
        output.add("Port,Protocol,Count");
        for(String pairs : pairCount.keySet()){
            output.add(String.join(",", unHasher(pairs)) + "," + String.valueOf(pairCount.get(pairs)));
        }
        Logger.info("Printing output to file: " + outputFileName);
        printToFile(outputFileName, output);
    }

    /**
     * Hashes multiple Strings together by joining with "@", assumes values themselves
     * do not contain "@"
     * @param values values to hash into string
     * @return      the string of value's hashed
     */
    private String hasher(String[] values){
        return String.join("@", values);
    }
    /**
     * Un-hashes multiple Strings that have been joined together with "@", assumes values themselves
     * do not contain "@"
     * @param hash a hashed encoding created with "@" delimiter
     * @return      values in the hash
     */
    private String[] unHasher(String hash){
        return hash.split("@");
    }

    /**
     * Parses and returns AWS Flow log record entries, and returns null if entry is invalid (currently only
     * checks valid and existing dstport and protocol values)
     * @param entry a String representation of a log entry.
     * @return string array of values in the entry
     */
    private String[] parseEntry(String entry){
        String[] parsed = entry.split(" ");
        // invalid data points for dstport, and protocol only
        if(parsed.length != 14 || parsed[6] == null || !isPosInteger(parsed[6])
            || parsed[7] == null || parsed[7].isEmpty() || !protocolMapping.containsKey(parsed[7])) {
            return null;
        }
        parsed[7] = protocolMapping.get(parsed[7]);
        return parsed;
    }
    /**
     * Checks if str is a valid string representation of a number
     * @param str a string
     * @return true if str is numeric, false if it is not
     */
    private boolean isPosInteger(String str){
        if(str == null || str.isEmpty()){
            return false;
        }
        for(int i = 0; i < str.length(); i++){
            if (Character.digit(str.charAt(i), 10) < 0){
                return false;
            }
        }
        return true;
    }

    /**
     * Creates new file with contents of lines
     * @param fname outputFile name
     * @Param lines List of lines to be printed in sequential order
     */
    private static void printToFile(String fname, List<String> lines) throws IOException{
        File fout = new File(fname);
        if(fout.exists()){
            throw new FileAlreadyExistsException("Error: cannot write to an existing file: " + fname + " please delete current file or change outputfile name");
        }
        FileOutputStream fos = new FileOutputStream(fname);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        for(String s : lines) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }
}
