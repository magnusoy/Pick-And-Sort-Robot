import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Connects to the Server through a socket.
 * Can send text and expects replies from server.
 */
public class Client implements Runnable {

    // Define socket and input, output streams
    private InetAddress ip;                             // Host name to server
    private final Scanner in;                           // Scanner for userinput
    private final DataInputStream dataInputStream;      // Input from Serial
    private final DataOutputStream dataOutputStream;    // Output to Serial
    private Socket socket;                              // Connection socket

    /**
     * Client constructor.
     *
     * @param host where to connect
     * @param port communication port
     * @throws IOException
     */
    public Client(String host, int port) throws IOException {
        this.in = new Scanner(System.in);
        try {
            this.ip = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.socket = new Socket(this.ip, port);
        } catch (ConnectException ce) {
            ce.printStackTrace();
        }
        assert this.socket != null;
        this.dataInputStream = new DataInputStream(this.socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * Establishing connection and
     * communication with the server is now possible.
     */
    public void send(String data)  {
        try {
            //System.out.println(this.dataInputStream.readUTF());
            String header = "POST/Controller";
            //this.dataOutputStream.writeUTF(header + data);
            String dataToSend = header + data + "\n";
            //System.out.println(dataToSend);

            this.dataOutputStream.writeUTF(dataToSend);
            //String received = this.dataInputStream.readUTF();
            //System.out.println(received);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Disconnects the client from the server
     */
    public void disconnect(){
        this.in.close();

        try {
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }
}