package xbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a collection of keys.
 */
public class Keys implements Iterable<Key>{

    private ArrayList<Key> keys;

    Keys() {
        this.keys = new ArrayList<>();
    }

    /**
     * Adds a new key
     * @param key the key to add
     */
    public void addKey(Key key){
        keys.add(key);
    }

    /**
     * Gets a key with the specified @code name.
     * @param name the name of the wanted key.
     * @return the requested key.
     * @throws NoSuchElementException when the requested key does not exist.
     */
    public Key getKey(String name)throws NoSuchElementException{
        Key foundKey = null;

        for (Key key : keys) {
            if (key.getName().equals(name)) foundKey = key;
        }

        if (foundKey == null) throw new NoSuchElementException();

        return foundKey;
    }

    @Override
    public Iterator<Key> iterator() {
        return keys.iterator();
    }
}
