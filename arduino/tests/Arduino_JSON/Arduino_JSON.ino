#include <ArduinoJson.h>

unsigned long nextSendTime = millis();
unsigned long startTime;
int id = 0;
int state = 0;
void setup() {
  // Initialize serial port
  Serial.begin(2000000);
  pinMode(13, OUTPUT);
  
// while (!Serial) continue;
//  unsigned long startTime = millis();
//  
//  const size_t capacity = 10 * JSON_ARRAY_SIZE(2) + JSON_ARRAY_SIZE(10) + 11 * JSON_OBJECT_SIZE(3) + 220;
//  DynamicJsonDocument docRcv(capacity);
//
//  const char* jsonRcv = "{\"state\":1,\"PetterSucks\":false,\"bricks\":[{\"shape\":\"cir\",\"ID\":1,\"crd\":[1280,720]},{\"shape\":\"rec\",\"ID\":2,\"crd\":[1280,720]},{\"shape\":\"sq\",\"ID\":3,\"crd\":[1280,720]},{\"shape\":\"tri\",\"ID\":4,\"crd\":[1280,720]},{\"shape\":\"tri\",\"ID\":4,\"crd\":[1280,720]},{\"shape\":\"tri\",\"ID\":4,\"crd\":[1280,720]},{\"shape\":\"tri\",\"ID\":4,\"crd\":[1280,720]},{\"shape\":\"tri\",\"ID\":4,\"crd\":[1280,720]},{\"shape\":\"tri\",\"ID\":4,\"crd\":[1280,720]},{\"shape\":\"tri\",\"ID\":4,\"crd\":[1280,720]}]}";
//
//  // Deserialize the JSON document
//  DeserializationError error = deserializeJson(docRcv, jsonRcv);
//
//  // Test if parsing succeeds.
//  if (error) {
//    Serial.print(F("deserializeJson() failed: "));
//    Serial.println(error.c_str());
//    return;
//  }
//
//  JsonObject obj = docRcv.as<JsonObject>();
//
//  if (obj.containsKey("PetterSucks")) {
//    boolean petterSucks = obj.getMember("PetterSucks");
//    Serial.print("Petter sucks: ");
//    Serial.println(petterSucks ? "YES" : "NO");
//  }
//
//  // Extract bricks from JSON document
//  JsonArray bricks = docRcv["bricks"].as<JsonArray>();
//  Serial.println();
//
//  // Go through all bricks
//  for (JsonObject brick : bricks) {
//    const char* brickShape = brick["shape"];
//    int brickID = brick["ID"];
//    int brickCoord_X = brick["crd"][0];
//    int brickCoord_Y = brick["crd"][1];
//
//    // Print brick info to Serial
//    Serial.println("This is a brick: ");
//    Serial.print("Shape: ");
//    Serial.println(brickShape);
//    Serial.print("ID: ");
//    Serial.println(brickID);
//    Serial.print("Coordinate: x = ");
//    Serial.print(brickCoord_X);
//    Serial.print(", y = ");
//    Serial.println(brickCoord_Y);
//  }

//  unsigned long stopTime = millis();
//  Serial.print("Program executed in: ");
//  Serial.println(stopTime - startTime);
}

void loop() {

  if (serialSendTimer(1000)){ // Send every 1000ms
    sendJSONSerial();
    //printProgramExecutionTime();
    digitalWrite(13, false);
    receiveJSONSerial();  
  }
}

/**
 * Timer that returns true once every specified interval
 */
boolean serialSendTimer(unsigned long sendInterval){
  boolean serialSend = false;
  
  if (millis() > nextSendTime){
    serialSend = true;
    nextSendTime = millis() + sendInterval;
  }
  return serialSend;
}

/**
 * Send JSON formatted data over serial
 */
void sendJSONSerial(){
  // https://arduinojson.org/v6/assistant/ 
  // Use the assistant to generate a serialization program from JSON input
  
  const size_t capacity = JSON_ARRAY_SIZE(3) + JSON_OBJECT_SIZE(3) + 3*JSON_OBJECT_SIZE(4);
  DynamicJsonDocument doc(capacity);
  
  doc["state"] = state;
  doc["speed"] = 100;
  doc["petter"] = "Drønnen";
  
  JsonArray objects = doc.createNestedArray("objects");
  
  JsonObject objects_0 = objects.createNestedObject();
  objects_0["type"] = random(0,99);
  objects_0["ID"] = id++;
  objects_0["x"] = 1280;
  objects_0["y"] = 720;
  
  JsonObject objects_1 = objects.createNestedObject();
  objects_1["type"] = random(0,99);
  objects_1["ID"] = id++;
  objects_1["x"] = 1280;
  objects_1["y"] = 720;
  
  JsonObject objects_2 = objects.createNestedObject();
  objects_2["type"] = id++;
  objects_2["ID"] = random(0,99);
  objects_2["x"] = 1280;
  objects_2["y"] = 720;
  
  serializeJson(doc, Serial);
}

/**
 * print what is received
 */
void receiveJSONSerial2(){
  if (Serial.available()){
    String myString = Serial.readString();
    Serial.print("This just in: ");
    Serial.println(myString);
  }
}
/**
 * Receive JSON formatted data over serial
 */
void receiveJSONSerial(){
  boolean received = false;
  if (Serial.available()){
    const size_t capacity = 10 * JSON_ARRAY_SIZE(2) + JSON_ARRAY_SIZE(10) + 11 * JSON_OBJECT_SIZE(3) + 220;
    DynamicJsonDocument docRcv(capacity);
    // Deserialize the JSON document
    DeserializationError error = deserializeJson(docRcv, Serial);
    
    // Test if parsing succeeds.
    if (error) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.c_str());
      return;
    }
  
    JsonObject obj = docRcv.as<JsonObject>();

    if (obj.containsKey("state")){
      int state = obj.getMember("state");
      changeStateTo(state);
      received = true;
    }
  
    if (obj.containsKey("PetterSucks")) {
      boolean petterSucks = obj.getMember("PetterSucks");
      Serial.print("Petter sucks: ");
      Serial.println(petterSucks ? "YES" : "NO");
      received = true;
    }
  
    // Extract bricks from JSON document
    if (obj.containsKey("objects")){
      JsonArray objects = docRcv["objects"].as<JsonArray>();
      Serial.println();
      
      // Go through all bricks
      for (JsonObject object : objects) {
        int objectType = object["type"];
        int objectID = object["ID"];
        int objectCoord_X = object["x"];
        int objectCoord_Y = object["y"];

        received = true;
        
        // Print brick info to Serial
        Serial.println("This is a object: ");
        Serial.print("type: ");
        Serial.println(objectType);
        Serial.print("ID: ");
        Serial.println(objectID);
        Serial.print("Coordinate: x = ");
        Serial.print(objectCoord_X);
        Serial.print(", y = ");
        Serial.println(objectCoord_Y);
      }
    }
  }

  if (received){
    digitalWrite(13, true);
  }
}



/**
 * Prints the program execution time to serial
 */
void printProgramExecutionTime(){
  unsigned long now = millis();
  Serial.println();
  Serial.print("Program Executed in: ");
  Serial.println(now-startTime);
  startTime = now;
}

void changeStateTo(int newState){
  state = newState;
}
