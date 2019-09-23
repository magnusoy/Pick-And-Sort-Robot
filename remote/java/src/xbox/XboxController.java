package xbox;

import com.github.strikerx3.jxinput.*;
import com.github.strikerx3.jxinput.enums.XInputBatteryDeviceType;
import com.github.strikerx3.jxinput.enums.XInputButton;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import com.github.strikerx3.jxinput.listener.SimpleXInputDeviceListener;
import com.github.strikerx3.jxinput.listener.XInputDeviceListener;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.pow;

public class XboxController extends TimerTask {

    private JSONObject jsonObject;
    private Timer sendDataTimer;

    private XInputDevice14 device = null;
    private XInputAxes axes = null;
    private ControllerListener controllerListener;


    private boolean stop = false;
    private ReentrantLock lock = new ReentrantLock();
    private int deviceNumber;

    /**
     * Gets the device number for this xbox controller
     * @return the xbox controller device number
     */
    public int getDeviceNumber() {
        return deviceNumber;
    }

    /**
     *
     * @param deviceNumber the number that identifies the controller.
     * @param listener the subscriber of controller commands
     */
    public XboxController(int deviceNumber, ControllerListener listener, int refreshTime) {
        this.deviceNumber = deviceNumber;
        this.controllerListener = listener;
        jsonObject = new JSONObject();

        try {
            // Get the device with specified device number
            device = XInputDevice14.getDeviceFor(this.deviceNumber);
            // Add a listener so we can receive events when changes happen.
            device.addListener(deviceListener);

            axes = device.getComponents().getAxes();

            sendDataTimer = new Timer();
            sendDataTimer.scheduleAtFixedRate(new TimerTask() {
                @Override // This run method is used for getting axis state and sending
                        // json data to the listener on a fixed cycle set by the @code refreshRate
                public void run() {
                    if(!stop){

                        jsonObject.append("Left X", roundTo3Decimal(axes.lx));
                        jsonObject.append("Left Y", roundTo3Decimal(axes.ly));

                        jsonObject.append("Right X", roundTo3Decimal(axes.rx));
                        jsonObject.append("Right Y", roundTo3Decimal(axes.ry));

                        jsonObject.append("Left T", roundTo3Decimal(axes.lt));
                        jsonObject.append("Right T", roundTo3Decimal(axes.rt));

                        controllerListener.gotData(jsonObject);
                    }
                    // Re-initialize json every time data is sent to avoid duplicating data
                    jsonObject = new JSONObject();
                }
                private double roundTo3Decimal(float value){
                    return ((float)((int)(pow(value,3)*1000)))/1000.0;
                }
            }, 10, refreshTime);


        } catch (XInputNotLoadedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Polls the device as long as the thread is not being stopped
        if (!stop){
            device.poll();
        }else {
            sendDataTimer.cancel();
            sendDataTimer.purge();
            sendDataTimer = null;
        }
    }

    private XInputDeviceListener deviceListener = new SimpleXInputDeviceListener() {
        @Override
        public void connected() {
            controllerListener.connected();
        }

        @Override
        public void disconnected() {
            controllerListener.disconnected();
        }

        @Override
        public void buttonChanged(final XInputButton button, final boolean pressed) {

            System.out.print("Button " + button.name());
            System.out.println(" "+ (pressed ? "Pressed" : "Released"));

            jsonObject.append(button.name(), pressed);
        }
    };

    public String getBatteryLevel(){
        String batteryLevel = ("" + device.getBatteryInformation(XInputBatteryDeviceType.GAMEPAD).getLevel());
        return batteryLevel;
    }

    /**
     * Gets the controller data(button state, joystick coordinates, battery level, vibrationState) in JSON format
     */
    public void getStateAsJSON(){

    }

    public void disconnect(){
        lock.lock();
        try{
            stop = true;
            device.removeListener(deviceListener);
        }finally {
            lock.unlock();
        }
    }

    public void setVibration(int left, int right){
        device.setVibration(left, right);
    }
}
