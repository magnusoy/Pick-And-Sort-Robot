package xbox;

/**
 * @author ib, modified by Vegard Solheim
 * Uses the producer-consumer programming pattern so that data must be
 * added (@code put() ) before it can be extracted (@code get() ).
 * Generics is used so the storage box can accomodate different types of objects.
 */
public class StorageBox<T> {
    private T content;                  // object to be stored
    private boolean available = false;  // flag

    /**
     * Gets the object from storage box.
     * @return the object from storage box.
     */
    public synchronized T get() {
        while (!available) {
            // value not available, wait for producer
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // reading value and resetting flag, wake up other threads (producer)
        available = false;
        notifyAll();
        return content;
    }

    /**
     * Stores an object in the storage box
     * @param t the object to store.
     */
    public synchronized void put(T t) {
        while (available) {
            try {
                // value not consumed yet, wait for value to be consumed
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        content = t; // store value
        available = true; // now available for consumer
        notifyAll();      // wake up other threads (consumer)
    }
}