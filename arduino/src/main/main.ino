/**
  Project description...
  -----------------------------------------------------------
  Libraries used:
  PID - https://github.com/magnusoy/Arduino-PID-Library
  Odrive - https://github.com/madcowswe/ODrive/tree/master/Arduino/ODriveArduino
  Wire - https://github.com/arduino/ArduinoCore-avr/tree/master/libraries/Wire
  SoftwareSerial - https://github.com/arduino/ArduinoCore-avr/tree/master/libraries/SoftwareSerial
  DigitalWriteFast - https://github.com/NicksonYap/digitalWriteFast
  -----------------------------------------------------------
  Code by: Magnus Kvendseth Øye,
  Date: 24.08-2019
  Version: 1.0
  Website: https://github.com/magnusoy/Pick-And-Sort-Robot
*/


// Including libraries
#include <PID.h>
#include <Wire.h>
#include <SoftwareSerial.h>
#include <ODriveArduino.h>
//#include <digitalWriteFast.h>


// Printing with stream operator
template<class T> inline Print& operator <<(Print &obj,     T arg) {
  obj.print(arg);
  return obj;
}

template<>        inline Print& operator <<(Print &obj, float arg) {
  obj.print(arg, 4);
  return obj;
}

#define UPDATE_SERIAL_TIME 100 // In millis


#define ODRIVE_SERIAL Serial1 // RX, TX (0, 1)
// Note: you must also connect GND on ODrive to GND on Arduino!

// ODrive object
ODriveArduino odrive(ODRIVE_SERIAL);
#define MOTOR_SPEED_LIMIT 22000.0f
#define MOTOR_CURRENT_LIMIT 10.0f

// Vector storing the motor 1 and 2 encoder position
int motorPosition[] = {0, 0};

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
const int S_CALIBRATION = 1;
const int S_READY = 2;
const int S_RUNNING = 3;
// A variable holding the current state
int currentState = S_IDLE;

// Defining limit switches
const int LIMIT_SWITCH_X_LEFT = 2;
const int LIMIT_SWITCH_X_RIGHT = 3;
const int LIMIT_SWITCH_Y_BOTTOM = 4;
const int LIMIT_SWITCH_Y_TOP = 5;


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
  switch (currentState) {
    case S_IDLE:
      // TODO: Wait for input from user to proceed

      //changeStateTo(S_CALIBRATION);
      break;

    case S_CALIBRATION:
      calibreateMotors();
      changeStateTo(S_READY);
      break;

    case S_READY:
      // Wait for input from user to proceed

      //changeStateTo(S_RUNNING);
      break;

    case S_RUNNING:
      // TODO: Do something

      // If user input is exit, change state
      //changeStateTo(S_READY);
      break;

    default:
      // Tries to change state to S_IDLE
      changeStateTo(S_IDLE);
      break;
  }
  edgeDetection();
  writeToSerial();
}


/**
  docstring
*/
void writeToSerial() {
  if (timerHasExpired()) {
    // TODO: Send data

    startTimer(UPDATE_SERIAL_TIME);
  }
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

/**
  Start Serial communication with ODrive,
  and Jetson Nano.
*/
void startSerial() {
  // ODrive uses 115200 baud
  ODRIVE_SERIAL.begin(115200);
  // Start Serial Communication
  Serial.begin(115200);
  TWBR = 12; // Or 24 for 32-bit
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
