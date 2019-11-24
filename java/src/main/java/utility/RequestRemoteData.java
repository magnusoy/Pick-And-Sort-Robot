package main.java.utility;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * RequestRemoteData fetches data from the remote REST-API Server
 * hosted by the remote object detection server.
 */
public class RequestRemoteData {

    private static final String REMOTE_URL = "http://83.243.168.10:5000/";  // End point
    private URL url;                                                        // URL object
    private JSONArray content;                                              // Stores fetched data

    /**
     * RequestRemoteData constructor, initializes
     * the URL that stores the data.
     */
    public RequestRemoteData() {
        try {
            this.url = new URL(REMOTE_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.content = new JSONArray();
    }

    /**
     * Fetches data from the REST Server.
     * Stores the data as JSON in content.
     */
    public void update() {
        JSONArray result = new JSONArray();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) this.url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert conn != null;
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() > 5) {
                    String[] lines = line.replace("\\", "").split("-");
                    for (String json : lines) {
                        result.put(json);
                    }
                }
            }

            bufferedReader.close();
        } catch (IOException | StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        this.content = result;
    }

    /**
     * Returns all of the stored data.
     *
     * @return All of the stored data
     */
    public JSONArray getAll() {
        return this.content;
    }

    /**
     * Returns number of objects detected.
     *
     * @return number of json strings
     */
    public int getSize() {
        return this.content.length();
    }

    /**
     * Returns JSONObject from given index.
     *
     * @param index where to extract object
     * @return JSONObject from given index.
     */
    public JSONObject get(int index) {
        String data;
        JSONObject jsonObject = null;
        if (this.content.length() > index) {
            String jsonAsString = this.content.get(index).toString();
            data = jsonAsString.substring(jsonAsString.indexOf('{'));
            jsonObject = new JSONObject(data);
        } else {
            jsonObject = new JSONObject();
        }
        return jsonObject;
    }
}
