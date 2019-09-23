package xbox;

import org.json.JSONObject;

public interface ControllerListener {

    void gotData(JSONObject jsonObject);

    void connected();

    void disconnected();

}
