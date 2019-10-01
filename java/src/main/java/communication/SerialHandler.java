package main.java.communication;
import java.io.*;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.json.JSONObject;

import main.java.utility.Database;


/**
 * SerialHandler communicates with the Teensy through
 * the UART protocol.
 */
public class SerialHandler extends Thread implements SerialPortEventListener  {
    private SerialPort serialPort;
    /** The port we're normally going to use. */
    private static final String[] PORT_NAMES = {
            "COM8"// Windows
    };

    private BufferedReader input;                   // Read in from Serial
    private OutputStream output;                    // Write out to Serial
    private static final int TIME_OUT = 2000;       // Milliseconds to block while waiting for port open
    private static final int DATA_RATE = 115200;    // Data boudrate
    private JSONObject jsonFromTeensy;              // Received JSON from teensy
    private Database database;                      // Shared resource between classes


    /**
     * SerialHandler constructor initializes
     * the shared resource.
     *
     * @param database, Shared resource
     */
    public SerialHandler(Database database) {
        this.database = database;
    }

    /**
     * Run SerialHandler in a thread.
     */
    @Override
    public void run() {
        CommPortIdentifier quit = null ;
        while (null == quit){
            quit = initialize();
            try {
                Thread.sleep(TIME_OUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the serialport.
     * Establishing a connection and setting
     * parameters.
     *
     * @return portId identifier
     */
    public CommPortIdentifier initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
        }
        if (portId != null){
            try {
                // open serial port, and use class name for the appName.
                serialPort = (SerialPort) portId.open(this.getClass().getName(),
                        TIME_OUT);

                // set port parameters
                serialPort.setSerialPortParams(DATA_RATE,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                // open the streams
                input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                output = serialPort.getOutputStream();

                // add event listeners
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        } return portId;
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Handle an event on the serial port.
     * Read the data and stores it in the database.
     *
     * @param oEvent DATA_AVAILABLE, when there are data in the buffer
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                this.jsonFromTeensy = new JSONObject(input.readLine());
                database.setJsonFromTeensy(this.jsonFromTeensy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Ignore all the other eventTypes
    }


    /**
     * Sends an JSONObject to the serial.
     *
     * @param data json to be sent
     */
    public synchronized void sendData(JSONObject data){
        if (data != null) {
            try {
                output.write(data.toString().getBytes(StandardCharsets.UTF_8));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns data received from Teensy
     * as JSON.
     *
     * @return data received from Teensy as JSON
     */
    public synchronized JSONObject getJsonFromTeensy() {
        JSONObject temp;
        if (this.jsonFromTeensy != null) {
            temp = this.jsonFromTeensy;
        } else {
            temp = new JSONObject();
        }
        return temp;
    }
}