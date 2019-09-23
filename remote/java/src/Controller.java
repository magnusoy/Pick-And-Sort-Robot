import com.github.strikerx3.jxinput.*;
import com.github.strikerx3.jxinput.enums.XInputBatteryDeviceType;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.JSONObject;
import xbox.ControllerListener;
import xbox.XboxController;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Timer;

public class Controller {

    @FXML
    TextArea console;
    private PrintStream ps ;
    private Timer timer;

    private boolean connectedToController = false;
    private boolean ConnectedToServer = false;
    private XboxController xboxController;
    private String controllerSelectionLabelText = "Xbox Controller Selection";

    public void initialize() {
        ps = new PrintStream(new Console(console));
        System.setOut(ps);
        System.setErr(ps);
        System.out.println("Application Initialized");
        controllerSelectionLabelText = xboxControllerLabel.getText();
        updateNumberOfAvailableControllers();
    }

    ControllerListener controllerListener = new ControllerListener() {
        @Override
        public void gotData(JSONObject jsonObject) {
            System.out.println("Got data!");
            System.out.println(jsonObject.toString(2));
        }

        @Override
        public void connected() {
        }

        @Override
        public void disconnected() {
            System.out.println("Disconnected");
        }
    };

    @FXML
    Button startVibrationButton;
    private boolean vibration = false;
    public void startVibration(){

        if (!vibration){
            xboxController.setVibration(65535, 65535);
            vibration = !vibration;
        }else{
            xboxController.setVibration(0, 0);
            vibration = !vibration;
        }

        startVibrationButton.setText((vibration? "Stop Vibration" : "Start Vibration"));
    }

    @FXML
    Label xboxControllerLabel;

    private void updateNumberOfAvailableControllers(){
        int numberOfControllers = 0;

        try {

            XInputDevice14[] devices14 = XInputDevice14.getAllDevices();
            numberOfControllers = Array.getLength(devices14);
            // Don't count the devices that are null
            for (int i = 0; i<4; i++){
                if (devices14[i].getBatteryInformation(XInputBatteryDeviceType.GAMEPAD) == null){
                    numberOfControllers -= 1;
                }
            }

        } catch (XInputNotLoadedException e) {
            System.out.println("No Available Controllers");
        }

        xboxControllerLabel.setText(controllerSelectionLabelText + " (" + numberOfControllers
                + (numberOfControllers == 1 ? " available controller)" : " available controllers)"));
    }

    @FXML
    Button controllerRefreshButton;

    public void controllerRefresh(){
        updateNumberOfAvailableControllers();
    }

    @FXML
    TextField controllerNumberTextField;
    @FXML
    TextField controllerHandleTextField;
    @FXML
    Button controllerConnectButton;

    /**
     * Connects to the xBox controller specified in the GUI choice box. If a controller is
     * already connected, a new call will result in disconnection from the connected device.
     */
    public void connectToController() {

        if (!connectedToController){

            try{
                //int deviceNumber = Integer.parseInt(controllerNumberTextField.getText());
                xboxController = new XboxController(0, controllerListener, 1000);

            }catch (Exception e){
                System.out.println("No Controllers Available");
            }

            timer = new Timer();
            timer.scheduleAtFixedRate(xboxController, 0, 1);
            connectedToController = !connectedToController;
            System.out.println("Connected to controller");
            System.out.println("Battery Level: " + xboxController.getBatteryLevel());

        }else {
            try{
                xboxController.disconnect();
                timer.cancel();
                timer.purge();
                timer = null;

            }catch (Exception e){
                System.out.println("I'm your Exception e");
            }

            connectedToController = !connectedToController;
            System.out.println("Disconnected from controller");
        }

        controllerNumberTextField.setDisable(connectedToController);
        controllerHandleTextField.setDisable(connectedToController);
        controllerRefreshButton.setDisable(connectedToController);
        controllerConnectButton.setText(connectedToController ? "Disconnect" : "Connect");
        startVibrationButton.setDisable(!connectedToController);
    }

    @FXML
    TextField serverIPTextField;
    @FXML
    TextField serverPortTextField;
    @FXML
    Button serverConnectButton;

    /**
     * Connects to server with IP and Port from GUI. If the application
     * is already connected, a new call will result in disconnection
     */
    public void connectToServer() {

        if (!ConnectedToServer){

            serverConnectButton.setText("Disconnect");
            ConnectedToServer = !ConnectedToServer;

        }else{


            serverConnectButton.setText("Connect");
            ConnectedToServer = !ConnectedToServer;
        }

        serverIPTextField.setDisable(ConnectedToServer);
        serverPortTextField.setDisable(ConnectedToServer);
    }

    public void clearConsole(){
        console.clear();
    }


    /**
     * Class for redirecting print output stream to GUI console
     */
    public class Console extends OutputStream {
        private TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> console.appendText(valueOf));
        }

        public void write(int b) throws IOException {
            appendText(String.valueOf((char)b));
        }
    }
}
