#include <ArduinoJson.h>

// Time for next timeout, in milliseconds
unsigned long nextTimeout = 0;

#define UPDATE_SERIAL_TIME 1000 // In millis

// Variables storing object data
int objectType = 0;
int objectsRemaining = 0;

// Position control
float actualX = 200;
float actualY = 200;
float targetX = 0;
float targetY = 0;

char received;

int currentState = 5;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(13, OUTPUT);

}

void loop() {
  // put your main code here, to run repeatedly:
  readJSONDocuemntFromSerial();

  if (received == 's') {
    digitalWrite(13, HIGH);
  }

  /**
    if (targetX != targetY) {
    digitalWrite(13, HIGH);
    } else {
    digitalWrite(13, LOW);
    }
  */

  writeToSerial(UPDATE_SERIAL_TIME);
}

/**
  Writes periodically to the Serial.

  @param updateTime in millis
*/
void writeToSerial(unsigned long updateTime) {
  if (timerHasExpired()) {
    sendJSONDocumentToSerial();
    startTimer(updateTime);
  }
}

/**
  Generate a JSON document and sends it
  over Serial.
*/
void sendJSONDocumentToSerial() {
  DynamicJsonDocument doc(64);
  doc["state"] = currentState;
  doc["x"] = actualX;
  doc["y"] = actualY;
  serializeJson(doc, Serial);
  Serial.print("\n");
}

/**
  Read JSON from Serial and parses it.
*/
void readJSONDocuemntFromSerial() {
  if (Serial.available() > 0) {
    const size_t capacity = 10 * JSON_ARRAY_SIZE(2) + JSON_ARRAY_SIZE(10) + 11 * JSON_OBJECT_SIZE(3) + 220;
    DynamicJsonDocument doc(capacity);
    DeserializationError error = deserializeJson(doc, Serial);
    if (error) {
      return;
    }
    JsonObject obj = doc.as<JsonObject>();

    objectType = obj["type"];
    objectsRemaining = obj["num"];
    targetX = obj["x"];
    targetY = obj["y"];

    if(obj.containsKey("command")) {
      received = obj["command"];
    }
  }
}

/**
   Checks if the timer has expired. If the timer has expired,
   true is returned. If the timer has not yet expired,
   false is returned.

   @return true if timer has expired, false if not
*/
boolean timerHasExpired() {
  boolean hasExpired = false;
  if (millis() > nextTimeout) {
    hasExpired = true;
  }
  return hasExpired;
}

/**
   Starts the timer and set the timer to expire after the
   number of milliseconds given by the parameter duration.

   @param duration The number of milliseconds until the timer expires.
*/
void startTimer(unsigned long duration) {
  nextTimeout = millis() + duration;
}
