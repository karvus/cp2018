package cp.week8;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public static class CityCache
{
    /*
 	- Create a static class for caching the names of some cities in a static field.
	- Initialise the static field with some cities, e.g., Copenhagen and Odense.
	- Start two threads that each adds some (different) cities.
	- The two threads will share the static field, potentially having problems.
	- Make the static field for city names a ThreadLocal to make it local to threads.
	*/

    private static ThreadLocal<List<String>> cities =
        new ThreadLocal<List<String>>() {
            @Override public List<String> initialValue() {
                return new ArrayList<String>();
            }
        };

    public void add(String city) {
        cities.get().add(city);
    }

    public void printCities(String tname) {
        for (String s : cities.get()) {
            System.out.println("["+tname+"] " + s);
        }
    }

    public static void main(String[] args) {
        CityCache cc = new CityCache();
        cc.add("Odense");
        cc.add("Copenhagen");

        Thread t1 = new Thread(() -> {
            cc.add("Trondheim");
            cc.add("Oslo");
            cc.printCities("t1");
        });
        Thread t2 = new Thread(() -> {
            cc.add("Trondheim");
            cc.add("Oslo");
            cc.printCities("t2");
        });
        t1.start();
        t2.start();
        cc.printCities("t0");
    }
}
