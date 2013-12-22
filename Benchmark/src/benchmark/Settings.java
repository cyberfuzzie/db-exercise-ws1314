package benchmark;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author cyberfuzzie
 */
public class Settings {
    private final ArrayDeque<String> urls;
    private final ArrayDeque<Integer> urlProbability;
    private ConcurrentHashMap<String,AtomicInteger> urlStatistics;
    
    private final double averageWait;
    
    public Settings(double averageWaitTime) {
        this.averageWait = averageWaitTime;
        urls = new ArrayDeque<>();
        urls.add("http://cyberfuzzie.suroot.com/wis/www-content/?page=auswertung");
        urls.add("http://cyberfuzzie.suroot.com/wis/www-content/?page=mitgliederbundestag");
        urls.add("http://cyberfuzzie.suroot.com/wis/www-content/?page=wahlkreis&bl=9&wk=474");
        urls.add("http://cyberfuzzie.suroot.com/wis/www-content/?page=wahlkreissieger");
        urls.add("http://cyberfuzzie.suroot.com/wis/www-content/?page=ueberhangmandate");
        urls.add("http://cyberfuzzie.suroot.com/wis/www-content/?page=knappstesieger");
        urlProbability = new ArrayDeque<>();
        urlProbability.add(25);
        urlProbability.add(10);
        urlProbability.add(25);
        urlProbability.add(10);
        urlProbability.add(10);
        urlProbability.add(20);
        urlStatistics = new ConcurrentHashMap<>();
        for (String url : urls) {
            urlStatistics.put(url, new AtomicInteger(0));
        }
    }
    
    public String getRandomUrl() {
        int rnd = (int)(Math.floor(Math.random() * 100));
        String url;
        Iterator<Integer> probIt = urlProbability.iterator();
        Iterator<String> urlIt = urls.iterator();
        do {
            rnd -= probIt.next();
            url = urlIt.next();
        } while (rnd >= 0);
        urlStatistics.get(url).incrementAndGet();
        return url;
    }
    
    public double getRandomWaitTime() {
        double rnd = Math.random() * 0.4 + 0.8;
        return rnd * averageWait;
    }
    
    public ArrayDeque<String> getUrls() {
        return urls;
    }
    
    public ConcurrentHashMap<String,AtomicInteger> getStatistics() {
        return urlStatistics;
    }
}
