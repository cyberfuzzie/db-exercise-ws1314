package benchmark;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cyberfuzzie
 */
public class Terminal extends Thread {
    private final Settings s;
    private boolean running;
    private int requestCount;
    private long overallTime;
    
    public Terminal(Settings settings) {
        super();
        
        this.s = settings;
    }
    
    @Override
    public void run() {
        running = true;
        requestCount = 0;
        overallTime = 0;
        waitRandom();
        while (running) {
            try {
                String url = s.getRandomUrl();
//                System.out.println("Getting URL: " + url);
                long startTime = System.currentTimeMillis();
                new URL(url).getContent();
                long endTime = System.currentTimeMillis();
                overallTime += (endTime - startTime);
                requestCount++;
                waitRandom();
            } catch (MalformedURLException ex) {
                Logger.getLogger(Terminal.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Terminal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
/*        String stats = "Terminal " + this + ":\n" + 
                       "    " + requestCount + " requests\n" + 
                       "    in " + overallTime + " ms\n" + 
                       "    average request time: " + (overallTime / requestCount);
        Logger.getLogger(Terminal.class.getName()).log(Level.INFO, stats);*/
    }
    
    private synchronized void waitRandom() {
        try {
            this.wait((long)(1000 * s.getRandomWaitTime()));
        } catch (InterruptedException ex) {
        }
    }
    
    public void requestStop() {
        running = false;
        this.interrupt();
    }
    
    public Map.Entry<Integer,Long> getResults() {
        return new AbstractMap.SimpleEntry<>(requestCount,overallTime);
    }
}
