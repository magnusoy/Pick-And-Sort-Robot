/**
  The purpose of this project is to communicate with Odrive through the Hardware serial.
  At the same time, it will receive and send data to Jetson Nano through the Software serial.
  With the received data it will, by using PID controllers,
  drive the motors to the correct position to pick up the figures.
  -----------------------------------------------------------
  Libraries used:
  PID - https://github.com/magnusoy/Arduino-PID-Library
  Odrive - https://github.com/madcowswe/ODrive/tree/master/Arduino/ODriveArduino
  ArduinoJSON - https://github.com/bblanchon/ArduinoJson
  -----------------------------------------------------------
  Code by: Magnus Kvendseth Øye, Vegard Solheim
  Date: 01.09-2019
  Version: 1.2
  Website: https://github.com/magnusoy/Pick-And-Sort-Robot
*/


// Including libraries
#include <PID.h>
#include <ODriveArduino.h>
#include <ArduinoJson.h>
#include "IO.h"
#include "PidParameters.h"
#include "OdriveParameters.h"

#define UPDATE_SERIAL_TIME 100 // In millis


#define ODRIVE_SERIAL Serial1 // RX, TX (0, 1)
// Note: must also connect GND on ODrive to GND on Arduino!

// ODrive object
ODriveArduino odrive(ODRIVE_SERIAL);

// Vector storing the motor 1 and 2 encoder position
int motorPosition[] = {0, 0};

// Variables for recieving data
boolean newData = false;
const byte numChars = 32;
char receivedChars[numChars]; // An array to store the received data

// Time for next timeout, in milliseconds
unsigned long nextTimeout = 0;

// PID X - Axis
double actualValueX = 0.0; double setValueX = 0.0; double outputValueX = 0.0;
PID pidX(kpX, kiX, kdX, REVERSE); // TODO: Change to correct direction

// PID Y - Axis
double actualValueY = 0.0; double setValueY = 0.0; double outputValueY = 0.0;
PID pidY(kpY, kiY, kdY, REVERSE); // TODO: Change to correct direction

// Constants representing the states in the state machine
const int S_IDLE = 0;
const int S_CALIBRATION = 1;
const int S_READY = 2;
const int S_MOVE_TO_OBJECT = 3;
const int S_PICK_OBJECT = 4;
const int S_MOVE_TO_DROP = 5;
const int S_DROP_OBJECT = 6;
const int S_COMPLETED = 7;
const int S_RESET = 8;
// A variable holding the current state
int currentState = S_IDLE;

// Variables storing object data
int objectType = 0;
int objectsRemaining = 0;


void setup() {
  // Initialize Serial ports
  startSerial();

  // Initialize limit switches
  initializeSwitches();

  // Initialize motor parameters
  configureMotors();

  // Initialize PID X and Y with correct parameters
  pidX.setUpdateTime(PIDX_UPDATE_TIME);
  pidY.setUpdateTime(PIDY_UPDATE_TIME);
  pidX.setOutputOffset(PIDX_OUTPUT_OFFSET);
  pidY.setOutputOffset(PIDY_OUTPUT_OFFSET);
  pidX.setOutputLimits(PIDX_OUTPUT_LOW, PIDX_OUTPUT_HIGH);
  pidY.setOutputLimits(PIDY_OUTPUT_LOW, PIDY_OUTPUT_HIGH);
}

void loop() {
  // State machine
  switch (currentState) {
    case S_IDLE:
      if (isValidCommand('c')) {
        changeStateTo(S_CALIBRATION);
      }
      break;

    case S_CALIBRATION:
      calibreateMotors();
      changeStateTo(S_READY);
      break;

    case S_READY:
      readJSONDocuemntFromSerial();
      if (isValidCommand('g')) {
        changeStateTo(S_MOVE_TO_OBJECT);
      }
      break;

    case S_MOVE_TO_OBJECT:
      readMotorPositions();
      outputValueX = pidX.compute(actualValueX, setValueX);
      outputValueY = pidY.compute(actualValueX, setValueX);
      setMotorPosition(MOTOR_X, outputValueX);
      setMotorPosition(MOTOR_Y, outputValueY);

      if ((actualValueX == setValueX) &&
          (actualValueY == setValueY)) {
        changeStateTo(S_PICK_OBJECT);
      }
      break;

    case S_PICK_OBJECT: {
        boolean pickedUp = pickObject();
        if (pickedUp) {
          objectSorter(objectType);
          changeStateTo(S_MOVE_TO_DROP);
        }
      }
      break;

    case S_MOVE_TO_DROP:
      readMotorPositions();
      outputValueX = pidX.compute(actualValueX, setValueX);
      outputValueY = pidY.compute(actualValueX, setValueX);
      setMotorPosition(MOTOR_X, outputValueX);
      setMotorPosition(MOTOR_Y, outputValueY);

      if ((actualValueX == setValueX) &&
          (actualValueY == setValueY)) {
        changeStateTo(S_DROP_OBJECT);
      }
      break;

    case S_DROP_OBJECT: {
        boolean dropped = dropObject();
        if (dropped) {
          changeStateTo(S_COMPLETED);
        }
      }
      break;

    case S_COMPLETED:
      readJSONDocuemntFromSerial();
      if (objectsRemaining == 0) {
        objectSorter(0);
        changeStateTo(S_RESET);
      } else {
        objectSorter(objectType);
        changeStateTo(S_MOVE_TO_OBJECT);
      }
      break;

    case S_RESET:
      readMotorPositions();
      outputValueX = pidX.compute(actualValueX, setValueX);
      outputValueY = pidY.compute(actualValueX, setValueX);
      setMotorPosition(MOTOR_X, outputValueX);
      setMotorPosition(MOTOR_Y, outputValueY);

      if ((actualValueX == setValueX) &&
          (actualValueY == setValueY)) {
        changeStateTo(S_READY);
      }
      break;

    default:
      // Tries to change state to S_IDLE
      changeStateTo(S_IDLE);
      break;
  }
  edgeDetection();
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
  doc["x"] = actualValueX;
  doc["y"] = actualValueY;
  serializeJson(doc, Serial);
}

/**
  Read JSON from Serial and parses it.
*/
void readJSONDocuemntFromSerial() {
  DynamicJsonDocument doc(64);
  DeserializationError error = deserializeJson(doc, Serial);
  if (error) {
    changeStateTo(S_IDLE);
    return;
  }
  objectType = doc["type"];
  objectsRemaining = doc["num"];
  setValueX = doc["x"];
  setValueY = doc["y"];
}

/**
  Receives input from Serial and check if
  the data corresponds the valid command.

  @param inputCommand, valid command char

  @return true if input matches,
          else false
*/
boolean isValidCommand(char inputCommand) {
  char received = ' ';
  boolean valid = false;

  if (Serial.available()) {
    received = Serial.read();
  }
  if (received == inputCommand) {
    valid = true;
  }
  return valid;
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

/**
  Start Serial communication with ODrive,
  and Jetson Nano.
*/
void startSerial() {
  // ODrive uses 115200 baud
  ODRIVE_SERIAL.begin(115200);
  // Start Serial Communication
  Serial.begin(115200);
  while (!Serial);
}


/**
  Calibreates motors.
  Be aware the motors will move during this process.
*/
void calibreateMotors() {
  int requested_state;
  for (int motorNumber = 0; motorNumber < 2; ++motorNumber) {
    requested_state = ODriveArduino::AXIS_STATE_MOTOR_CALIBRATION;
    odrive.run_state(motorNumber, requested_state, true);

    requested_state = ODriveArduino::AXIS_STATE_ENCODER_OFFSET_CALIBRATION;
    odrive.run_state(motorNumber, requested_state, true);

    requested_state = ODriveArduino::AXIS_STATE_CLOSED_LOOP_CONTROL;
    odrive.run_state(motorNumber, requested_state, false); // don't wait
  }
}

/**
  Read current motor position from Odrive.
  Storing them in the global motorPosition
  variable.
*/
void readMotorPositions() {
  static const unsigned long duration = 10000;
  unsigned long start = millis();
  while (millis() - start < duration) {
    for (int motorNumber = 0; motorNumber < 2; ++motorNumber) {
      ODRIVE_SERIAL << "r axis" << motorNumber << ".encoder.pos_estimate\n";
      motorPosition[motorNumber] = odrive.readFloat();
    }
  }
  actualValueX = motorPosition[0];
  actualValueY = motorPosition[1];
}

/**
   Set the given motor to the assigned position.

   @param motorNumber, Specify motor {0, 1}
   @param pos, Position to drive to
*/
void setMotorPosition(const int motorNumber, double pos) {
  odrive.SetPosition(motorNumber, pos);
}

/**
  Configure motor parameters.
*/
void configureMotors() {
  for (int axis = 0; axis < 2; ++axis) {
    ODRIVE_SERIAL << "w axis" << axis << ".controller.config.vel_limit " << MOTOR_SPEED_LIMIT << '\n';
    ODRIVE_SERIAL << "w axis" << axis << ".motor.config.current_lim " << MOTOR_CURRENT_LIMIT << '\n';
  }
}

/**
  Initialize limit switches to inputs.
*/
void initializeSwitches() {
  pinMode(LIMIT_SWITCH_X_LEFT, INPUT);
  pinMode(LIMIT_SWITCH_X_RIGHT, INPUT);
  pinMode(LIMIT_SWITCH_Y_BOTTOM, INPUT);
  pinMode(LIMIT_SWITCH_Y_TOP, INPUT);
}

/**
  Changes state to S_IDLE if any
  limit switch is pressed.
*/
void edgeDetection() {
  int buttonState1 = digitalRead(LIMIT_SWITCH_X_LEFT);
  int buttonState2 = digitalRead(LIMIT_SWITCH_X_RIGHT);
  int buttonState3 = digitalRead(LIMIT_SWITCH_Y_BOTTOM);
  int buttonState4 = digitalRead(LIMIT_SWITCH_Y_TOP);

  if (buttonState1 || buttonState2
      || buttonState3 || buttonState4) {
    changeStateTo(S_IDLE);
  }
}

/**
  Change the set value of the
  PID X, and Y.

  @param x, new x position
  @param y, new y position
*/
void setPosition(float x, float y) {
  setValueX = x;
  setValueY = y;
}


/**
  Assigns the drop coordinates based
  on the object type.
*/
void objectSorter(int object) {
  switch (object) {
    case 0:
      setPosition(100, 100);
      break;

    case 1:
      setPosition(100, 100);
      break;

    case 2:
      setPosition(100, 100);
      break;

    case 3:
      setPosition(100, 100);
      break;

    case 4:
      setPosition(100, 100);
      break;

    default:
      changeStateTo(S_IDLE);
      break;
  }
}

/**
  Pick object sequence.
*/
boolean pickObject() {
  //TODO: Create sequence
  return true;
}

/**
  Drop object sequence.
*/
boolean dropObject() {
  //TODO: Create sequence
  return true;
}

/**
  Templates for printing
  to ODrive v3.6
*/
template<class T> inline Print& operator <<(Print &obj,     T arg) {
  obj.print(arg);
  return obj;
}

template<>        inline Print& operator <<(Print &obj, float arg) {
  obj.print(arg, 4);
  return obj;
}
