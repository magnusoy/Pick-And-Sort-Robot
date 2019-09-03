package main.java.communication;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;
import org.json.JSONObject;

public class ArduinoHandler extends Thread implements SerialPortEventListener  {
    SerialPort serialPort;
    /** The port we're normally going to use. */
    private static final String PORT_NAMES[] = {
            "COM4"// Windows
    };
    /**
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private BufferedReader input;
    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 9600;
    /** The jsonObject that the Arduino sends over the Serial */
    private JSONObject jsonObject;
    @Override
    public void run() {
        CommPortIdentifier quit = null ;
        while (null == quit){
            quit = initialize();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

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
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine=input.readLine();
                this.jsonObject = new JSONObject(inputLine);
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
    /**
     * Sends an JSONObject to the arduino
     */
    public synchronized void sendData(JSONObject data){
        try {
            output.write(data.toString().getBytes());
            // StringWriter outputData = new StringWriter();
            //data.writeJSONString(outputData);
            // String jsonText = outputData.toString();
            //System.out.print(jsonText);

        }catch (Exception e){
            System.err.println(e.toString());

        }
    }

    public synchronized JSONObject getJsonObject() {
        return this.jsonObject;
    }
}