package xbox;

/**
 * Represents a single axis on a xbox controller.
 */
public class Axis extends Key {

    private double value = 0.0; // axis value goes from -1 to 1, where zero is resting position.

    Axis(String name){
        super(name);
    }

    /**
     * Gets the value of the axis. Axis value goes from -1 to 1, where zero is resting position.
     * @return the value of the axis.
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the value of the axis. Axis value goes from -1 to 1, where zero is resting position.
     * @param value the new axis value.
     */
    public void setValue(double value) {
        this.value = value;
    }
}
