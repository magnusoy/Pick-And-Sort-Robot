package main.java.utility;

import org.json.JSONObject;
import org.json.simple.JSONArray;

public class Database {

    private String command;
    private final ObjectHandler objectHandler;
    private JSONObject obj;

    public Database() {
        this.command = "";
        this.objectHandler = new ObjectHandler("..\\resources\\Objects\\objects.json");
        this.obj = new JSONObject();
    }

    public synchronized JSONArray getObjects() {
        return this.objectHandler.getAll();
    }

    public void putCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }

    public synchronized void putObj(JSONObject obj) {
        this.obj = obj;
    }

    public synchronized JSONObject getObj() {
        return this.obj;
    }
}
