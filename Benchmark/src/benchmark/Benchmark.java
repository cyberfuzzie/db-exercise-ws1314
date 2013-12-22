package benchmark;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cyberfuzzie
 */
public class Benchmark {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            help();
        } else {
            int numTerminals = Integer.parseInt(args[0]);
            double avgWaitTime = Double.parseDouble(args[1]);
            doBenchmark(numTerminals, avgWaitTime);
        }
    }
    
    private static void help() {
        System.out.println("Command Line:");
        System.out.println("  Benchmark <numTerminals> <avgWaitTime>");
    }

    private static void doBenchmark(int numTerminals, double avgWaitTime) {
        ArrayDeque<Terminal> terminals = new ArrayDeque<>();
        Settings s = new Settings(avgWaitTime);
        for (int i=0; i<numTerminals; i++) {
            Terminal t = new Terminal(s);
            t.start();
            terminals.add(t);
        }
        System.out.println("Bechmark running - press enter to stop");
        try {
            System.in.read();
        } catch (IOException ex) {
            System.out.println("Benchmark aborted by Exception");
            return;
        }
        System.out.println("Stopping Benchmark client...");
        int requestCount = 0;
        long overallTime = 0;
        try {
            while (true) {
                Terminal t = terminals.removeFirst();
                t.requestStop();
                try {
                    t.join();
                    Map.Entry<Integer,Long> result = t.getResults();
                    requestCount += result.getKey();
                    overallTime += result.getValue();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Benchmark.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (NoSuchElementException e) {}
        System.out.println("");
        System.out.println("Overall Execution statistics:");
        System.out.println("    Requests performed: " + requestCount);
        System.out.println("    Overall execution time: " + overallTime + " ms");
        System.out.println("    Avg execution time/request: " + (overallTime / requestCount) + " ms");
        System.out.println("");
        System.out.println("Request distribution:");
        ConcurrentHashMap<String,AtomicInteger> statistics = s.getStatistics();
        for (String url : s.getUrls()) {
            System.out.println("    " + statistics.get(url).get() + " " + url);
        }
    }
}
