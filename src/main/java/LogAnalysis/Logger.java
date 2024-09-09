package LogAnalysis;
/*
Centralized logging information
 */
public class Logger {
    public static void info(String msg){
        System.out.println(msg);
    }
    public static void error(String msg){
        System.err.println(msg);
    }
}
