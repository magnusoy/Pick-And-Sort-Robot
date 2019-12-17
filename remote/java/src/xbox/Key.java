package xbox;

/**
 * Represents a generic key
 */
public class Key {
    private String name; // name of the key

    Key(String name){
        this.name = name;
    }

    /**
     * Gets the name of the key,
     * @return the name of the key.
     */
    public String getName() {
        return name;
    }
}
