package main.java.utility;

import org.json.JSONObject;
import org.json.simple.JSONArray;

public class Database {

    private Integer command;
    private final ObjectHandler objectHandler;
    private JSONObject obj;
    private  JSONObject objToSend;

    public Database() {
        this.command = 0;
        this.objectHandler = new ObjectHandler("..\\resources\\Objects\\objects.json");
        this.obj = new JSONObject();
        this.objToSend = new JSONObject();
    }

    public synchronized JSONArray getObjects() {
        return this.objectHandler.getAll();
    }

    public void putCommand(Integer command) {
        this.command = command;
    }

    public Integer getCommand() {
        return this.command;
    }

    public synchronized void putObj(JSONObject obj) {
        this.obj = obj;
    }

    public synchronized JSONObject getObj() {
        JSONObject temp;
        if (this.obj != null) {
            temp = this.obj;
        } else {
            temp = new JSONObject();
        }
        return temp;
    }

    public synchronized JSONObject getObjToSend() {
        this.objToSend.put("command", this.command);
        // Just because path planning is not made yet.
        this.objToSend.put("type", new Integer(1));
        this.objToSend.put("x", new Integer(200));
        this.objToSend.put("y", new Integer(300));
        this.objToSend.put("num", new Integer(1));
        this.command = 0;
        return this.objToSend;
    }

}
