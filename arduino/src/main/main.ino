/**
  Project description...
  -----------------------------------------------------------
  Libraries used:
  PID - https://github.com/magnusoy/Arduino-PID-Library
  Odrive - https://github.com/madcowswe/ODrive/tree/master/Arduino/ODriveArduino
  Wire - https://github.com/arduino/ArduinoCore-avr/tree/master/libraries/Wire
  SoftwareSerial - https://github.com/arduino/ArduinoCore-avr/tree/master/libraries/SoftwareSerial
  -----------------------------------------------------------
  Code by: Magnus Kvendseth Øye,
  Date: 24.08-2019
  Version: 1.0
  Website: https://github.com/magnusoy/Pick-And-Sort-Robot
*/

#include <PID.h>
#include <Wire.h>
#include <SoftwareSerial.h>
#include <ODriveArduino.h>

//#define HWSERIAL Serial1 // RX, TX (0, 1)
//See limitations of Arduino SoftwareSerial
SoftwareSerial serial(10, 11);

// Variables for recieving data
boolean newData = false;
const byte numChars = 32;
char receivedChars[numChars]; // An array to store the received data

// Time for next timeout, in milliseconds
unsigned long nextTimeout = 0;

// PID X - Axis
#define PIDX_OUTPUT_LOW 0
#define PIDX_OUTPUT_HIGH 0 // TODO: Change to correct value
#define PIDX_OUTPUT_OFFSET 0 // TODO: Change to correct value
#define PIDX_UPDATE_TIME 10 // In millis

double kpX = 0.0; double kiX = 0.0; double kdX = 0.0;
double actualValueX = 0.0; double setValueX = 0.0; double outputValueX = 0.0;
PID pidX(kpX, kiX, kdX, REVERSE); // TODO: Change to correct direction

// PID Y - Axis
#define PIDY_OUTPUT_LOW 0
#define PIDY_OUTPUT_HIGH 0 // TODO: Change to correct value
#define PIDY_OUTPUT_OFFSET 0 // TODO: Change to correct value
#define PIDY_UPDATE_TIME 10 // In millis

double kpY = 0.0; double kiY = 0.0; double kdY = 0.0;
double actualValueY = 0.0; double setValueY = 0.0; double outputValueY = 0.0;
PID pidY(kpY, kiY, kdY, REVERSE); // TODO: Change to correct direction

// Constants representing the states in the state machine
const int S_IDLE = 0;
const int S_ONE = 1;
const int S_TWO = 2;
// A variable holding the current state
int currentState = S_IDLE;

void setup() {
  // Start Serial Communication
  Serial.begin(115200);
  TWBR = 12; // Or 24 for 32-bit

  // Initialize PID X and Y with correct parameters
  pidX.setUpdateTime(PIDX_UPDATE_TIME);
  pidY.setUpdateTime(PIDY_UPDATE_TIME);
  pidX.setOutputOffset(PIDX_OUTPUT_OFFSET);
  pidY.setOutputOffset(PIDY_OUTPUT_OFFSET);
  pidX.setOutputLimits(PIDX_OUTPUT_LOW, PIDX_OUTPUT_HIGH);
  pidY.setOutputLimits(PIDY_OUTPUT_LOW, PIDY_OUTPUT_HIGH);
}

void loop() {
  switch (currentState) {
    case S_IDLE:

      break;

    case S_ONE:

      break;

    case S_TWO:

      break;

    default:

      break;

  }
}


/**
  docstring
*/
void writeToSerial() {

}

/**
  docstring
*/
void writeToOdrive() {

}

/**
  docstring
*/
void readFromOdrive() {

}


/**
  Reads from Serialport.
*/
void readFromSerial() {
  static byte ndx = 0;
  char endMarker = '\n';
  char rc;

  while ((Serial.available() > 0) && (!newData)) {
    rc = Serial.read();

    if (rc != endMarker) {
      receivedChars[ndx] = rc;
      ndx++;
      if (ndx >= numChars) {
        ndx = numChars - 1;
      }
    } else {
      receivedChars[ndx] = '\0'; // Terminate the string
      ndx = 0;
      newData = true;
    }
  }
}

/**
  Fetches the value from a substring,
  wich is seperated with a given symbol.
  
  @param data your String to be seperated
  @param seperator your symbol to seperate by
  @param index where your value is located
  
  @return substring before seperator
*/
String getValueFromSerial(String data, char separator, int index) {
  int found = 0;
  int strIndex[] = { 0, -1 };
  int maxIndex = data.length() - 1;

  for (int i = 0; i <= maxIndex && found <= index; i++) {
    if (data.charAt(i) == separator || i == maxIndex) {
      found++;
      strIndex[1] = (i == maxIndex) ? i + 1 : i;
    }
  }
  strIndex[0] = strIndex[1] + 1;
  return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}

/**
   Change the state of the statemachine to the new state
   given by the parameter newState

   @param newState The new state to set the statemachine to
*/
void changeStateTo(int newState) {
  currentState = newState;
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
