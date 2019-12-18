import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Connects to the Server through a socket.
 * Can send text and expects replies from server.
 */
public class Client implements Runnable {

    // Define socket and input, output streams
    private InetAddress ip;                             // Host name to server
    private int port;
    private volatile boolean connected = false;                  // Connection flag
    private DataInputStream dataInputStream;            // Input from Serial
    private DataOutputStream dataOutputStream;          // Output to Serial
    private Socket socket;                              // Connection socket
    private String header;                              // Header when sending to server
    private volatile boolean newData = false;
    private ReentrantLock lock;                         // Adds threads safe operation on new data to send
    private String dataToSend;

    /**
     * Client constructor.
     *
     * @param host where to connect
     * @param port communication port
     * @throws IOException When a connection can not be made.
     */
    public Client(String host, int port, String header) throws IOException {
        this.ip = InetAddress.getByName(host);
        this.port = port;
        this.header = header;
        this.lock = new ReentrantLock();

        if (ip.isReachable(1000)){
            try {
                this.socket = new Socket(this.ip, port);
            } catch (ConnectException ce) {
                ce.printStackTrace();
                System.out.println("Could not connect to server...");
            }
        }else{
            // Ip cannot be reached
            throw new IOException();
        }

        assert this.socket != null;
        this.dataInputStream = new DataInputStream(this.socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * Connects the client to the already given ip address and port number.
     *
     * @throws IOException when connection can not be made.
     */
    public void connect() throws IOException{
        connected = ip.isReachable(1000); // Check if the host is reachable before trying to connect.
         if (connected){
             this.socket = new Socket(this.ip, this.port); // Try to connect.
         }else throw new IOException(); // Host is not reachable
        // Add the new socket to in and out streams so we can more easily interface with it.
        this.dataInputStream = new DataInputStream(this.socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * Disconnects the client from the host.
     */
    public void disconnect(){
        try {
            this.dataInputStream.close();
            this.dataOutputStream.close();
            this.connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a string of data to the host.
     *
     * @param data data to be sent
     */
    public void send(String data){
        lock.lock();
        this.dataToSend = header + data + "\n";
        this.newData = true;
        lock.unlock();
        notify(); // Notifies the client so it can send the new data
    }

    /**
     *
     */
    @Override
    public void run(){
        while(this.connected){
            if(newData){
                try {
                    lock.lock();
                    this.dataOutputStream.writeUTF(this.dataToSend);
                    lock.unlock();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}