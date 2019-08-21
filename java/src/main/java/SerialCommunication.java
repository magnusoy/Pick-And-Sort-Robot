package main.java;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

public class SerialCommunication extends Thread {

    private int baudrate;
    private String port;
    private SerialPort serialPort;

    public SerialCommunication(int baudrate, String port) {
        this.baudrate = baudrate;
        this.port = port;
        this.serialPort = SerialPort.getCommPort(this.port);
        this.serialPort.setComPortParameters(this.baudrate, 8, 1, 0);
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
    }

    public boolean write(String data) {
        if (this.serialPort.isOpen()) {
            try {
                this.serialPort.getOutputStream().write(data.getBytes());
                this.serialPort.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.serialPort.closePort();
    }

    public String read() {
        return String.valueOf(this.serialPort.getOutputStream());
    }
}
