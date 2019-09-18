package main.java.communication;

import com.fazecast.jSerialComm.SerialPort;
import main.java.utility.Database;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class SerialHandler2 extends Thread{
    private SerialPort sp;
    private Database database;
    private JSONParser jsonParser;
    private static final String PORT = "COM8";
    private static final int TIME_OUT = 2000;       // Milliseconds to block while waiting for port open
    private static final int DATA_RATE = 115200;    // Data boudrate

    public SerialHandler2(Database database) {
        this.database = database;
        this.sp = SerialPort.getCommPort(PORT);
        this.sp.setComPortParameters(DATA_RATE, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        this.sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        this.jsonParser = new JSONParser();
    }

    @Override
    public void run() {
        boolean quit = false;
        while (!quit) {
            quit = connect();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    write();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    read();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean connect() {
        boolean open = this.sp.isOpen();
        if (open) {
            System.out.println("Connected to COM port.");
        } else {
            System.out.println("Could not find COM port.");
        }
        return open;
    }

    public synchronized void write() {
        try {
            this.sp.getOutputStream().write(this.database.getObjToSend().toString().getBytes(StandardCharsets.UTF_8));
            this.sp.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void read() {
        try {
            // Read in from Serial
            BufferedReader input = new BufferedReader(new InputStreamReader(this.sp.getInputStream()));
            String inputLine = input.readLine();
            Object obj = this.jsonParser.parse(inputLine);
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject != null) {
                this.database.putObj(jsonObject);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean close() {
        return this.sp.closePort();
    }
}
