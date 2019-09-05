package main.java.utility;

import org.json.JSONObject;
import org.json.simple.JSONArray;

public class Database {

    private String command;
    private final ObjectHandler objectHandler;
    private JSONObject obj;
    private  JSONObject objToSend;

    public Database() {
        this.command = "";
        this.objectHandler = new ObjectHandler("..\\resources\\Objects\\objects.json");
        this.obj = new JSONObject();
        this.objToSend = new JSONObject();
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
        JSONObject temp;
        if (this.obj != null) {
            temp = this.obj;
        } else {
            temp = new JSONObject();
        }
        return temp;
    }

    public synchronized void putObjToSend(JSONObject obj) {
        obj.put("type", "circle");
        obj.put("x", new Integer(200));
        obj.put("y", new Integer(300));
        obj.put("num", new Integer(1));
        if (this.obj != null) {
            this.objToSend = obj;
        }
    }

    public synchronized JSONObject getObjToSend() {
        JSONObject temp;
        if (this.objToSend != null) {
            temp = this.objToSend;
            if (!this.command.isEmpty()) {
                temp.put("command", this.command);
            }
            // Just because path planning is not made yet.
            temp.put("type", "circle");
            temp.put("x", new Integer(200));
            temp.put("y", new Integer(300));
            temp.put("num", new Integer(1));
            //
        } else {
            temp = new JSONObject();
        }
        return temp;
    }

}
