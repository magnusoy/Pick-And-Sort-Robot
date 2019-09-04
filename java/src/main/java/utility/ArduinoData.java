package main.java.utility;

import org.json.JSONObject;

public class ArduinoData {
    private JSONObject data;
    private boolean available = false;


    public synchronized void put(JSONObject obj) {
        while (available) {
            try {
                wait();
            }
            catch (InterruptedException ei) {
                ei.printStackTrace();
            }
        }
        data = obj;
        available = true;
        notifyAll();
    }

    public synchronized JSONObject get() {

        while (!available) {
            try {
                wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        available = false;
        notifyAll();
        return data;
    }
}
