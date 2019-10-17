/**
  The purpose of this project is to communicate with Odrive through the Hardware serial.
  At the same time, it will receive and send data to Jetson Nano through the Software serial.
  With the received data it will, by using PID controllers,
  drive the motors to the correct position to pick up the figures.
  -----------------------------------------------------------
  Libraries used:
  Odrive - https://github.com/madcowswe/ODrive/tree/master/Arduino/ODriveArduino
  ArduinoJSON - https://github.com/bblanchon/ArduinoJson
  ButtonTimer - https://github.com/magnusoy/Arduino-ButtonTimer-Library
  -----------------------------------------------------------
  Code by: Magnus Kvendseth Øye, Vegard Solheim, Petter Drønnen
  Date: 15.10-2019
  Version: 2.1
  Website: https://github.com/magnusoy/Pick-And-Sort-Robot
*/


// Including libraries
#include <ODriveArduino.h>
#include <ArduinoJson.h>
#include <ButtonTimer.h>
#include "IO.h"
#include "OdriveParameters.h"
#include "States.h"
#include "Commands.h"


#define UPDATE_SERIAL_TIME 100 // In millis
#define ACTIVE_END_SWITCH_TIME 20  // In millis

ButtonTimer SwitchFilter1(ACTIVE_END_SWITCH_TIME);
ButtonTimer SwitchFilter2(ACTIVE_END_SWITCH_TIME);
ButtonTimer SwitchFilter3(ACTIVE_END_SWITCH_TIME);
ButtonTimer SwitchFilter4(ACTIVE_END_SWITCH_TIME);
ButtonTimer EmergencySwitch(ACTIVE_END_SWITCH_TIME);

// For mapping pixels to counts
#define AXIS_X_LOWER 40
#define AXIS_X_HIGHER 480
#define AXIS_Y_LOWER 26
#define AXIS_Y_HIGHER 470

#define TOOL_OFFSET_X 10017.5
#define TOOL_OFFSET_Y 11575.8


#define ODRIVE_SERIAL Serial1 // RX, TX (0, 1)

// ODrive object
ODriveArduino odrive(ODRIVE_SERIAL);

// Vector storing the motor 1 and 2 encoder position
int motorPosition[] = {0, 0};

// Time for next timeout, in milliseconds
unsigned long nextTimeout = 0;

// A variable holding the current state
int currentState = S_READY; // S_IDLE

// Position control
float actualX = 0.0f;
float actualY = 0.0f;
float targetX = 0.0f;
float targetY = 0.0f;
float manualX = 0.0f;
float manualY = 0.0f;

// Variables storing object data
int objectType = 0;
int objectsRemaining = 0;
int oldCommand = 0;
int recCommand = 0;
float targetXPixels = 0.0f;
float targetYPixels = 0.0f;

// Manual control variables
float inputX = 0.0f;
float inputY = 0.0f;
float motorSpeed = 0.0f;
boolean pick = false;
boolean drop = false;

// Speed variables
int currentSpeed = MOTOR_SPEED_LIMIT;
int encoderXOffset = 0;
int encoderYOffset = 0;
int motorXEndCounts = 0;
int motorYEndCounts = 0;

void setup() {
  // Initialize Serial ports
  startSerial();

  initializeSwitches();
  initializeValveOperations();

  // Initialize motor parameters
  configureMotors();
  resetValves();
  calibreateMotors();
  encoderCalibration();

  // Wait to proceed until Server connects
  while (!Serial);
}

void loop() {
  // Safety procedure
  if (isCommandValid(STOP)) {
    changeStateTo(S_READY);
  }

  // Read motor position at any given state
  readMotorPositions();

  // State machine
  switch (currentState) {
    case S_IDLE:
      readJSONDocumentFromSerial();
      if (isCommandValid(CALIBRATE)) {
        changeStateTo(S_READY);
      }
      break;

    case S_CALIBRATION:
      calibreateMotors();
      changeStateTo(S_READY); // S_READY
      break;

    case S_READY:
      readJSONDocumentFromSerial();
      if (isCommandValid(START) ||
          isCommandValid(AUTOMATIC_CONTROL)) {
        targetX = convertFromPixelsToCountsX(targetXPixels);
        targetY = convertFromPixelsToCountsY(targetYPixels);
        setToolPosition(targetX, targetY);
        changeStateTo(S_MOVE_TO_OBJECT);
      } else if (isCommandValid(MANUAL_CONTROL)) {
        changeStateTo(S_MANUAL);
      } else if (isCommandValid(CONFIGURE)) {
        changeStateTo(S_CONFIGURE);
      } else if (isCommandValid(RESET)) {
        changeStateTo(S_IDLE);
      }
      break;

    case S_MOVE_TO_OBJECT:
      updateManualPosition();
      //if (onTarget()) {
      //  changeStateTo(S_PICK_OBJECT);
      //}
      break;

    case S_PICK_OBJECT:
      if (pickObject()) {
        objectSorter(objectType);
        setToolPosition(targetX, targetY);
        changeStateTo(S_MOVE_TO_DROP);
      }
      break;

    case S_MOVE_TO_DROP:
      updateManualPosition();
      if (onTarget()) {
        changeStateTo(S_DROP_OBJECT);
      }
      break;

    case S_DROP_OBJECT:
      if (dropObject()) changeStateTo(S_COMPLETED);
      break;

    case S_COMPLETED:
      readJSONDocumentFromSerial();

      if (areThereMoreObjects()) {
        objectSorter(0);
        setToolPosition(targetX, targetY);
        changeStateTo(S_RESET);
      } else {
        objectSorter(objectType);
        targetX = convertFromPixelsToCountsX(targetXPixels);
        targetY = convertFromPixelsToCountsY(targetYPixels);
        setToolPosition(targetX, targetY);
        changeStateTo(S_MOVE_TO_OBJECT);
      }
      break;

    case S_RESET:
      updateManualPosition();
      if (onTarget()) {
        changeStateTo(S_READY);
      }
      break;

    case S_MANUAL:
      readJSONDocumentFromSerial();

      manualX += (100 * inputX);
      manualY += (100 * inputY);
      setMotorPosition(MOTOR_X, manualX);
      setMotorPosition(MOTOR_Y, manualY);

      setMotorSpeedFromController();

      if (pick) pickObject();
      if (drop) dropObject();

      if (isCommandValid(AUTOMATIC_CONTROL)) {
        changeStateTo(S_READY);
      } else if (isCommandValid(CONFIGURE)) {
        changeStateTo(S_CONFIGURE);
      }
      break;

    case S_CONFIGURE:
      // TODO: Add ODrive configurations
      if (isCommandValid(RESET)) {
        changeStateTo(S_IDLE);
      }
      break;

    default:
      // Tries to change state to S_IDLE
      terminateMotors();
      changeStateTo(S_IDLE);
      break;
  }
  emergencyStop();
  //edgeDetection();
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
  DynamicJsonDocument doc(220);
  doc["state"] = currentState;
  doc["x"] = actualX;
  doc["y"] = actualY;
  doc["command"] = recCommand;
  doc["manX"] = manualX;
  serializeJson(doc, Serial);
  Serial.print("\n");
}

/**
  Read JSON from Serial and parses it.
*/
void readJSONDocumentFromSerial() {
  if (Serial.available() > 0) {
    const size_t capacity = 15 * JSON_ARRAY_SIZE(2) + JSON_ARRAY_SIZE(10) + 11 * JSON_OBJECT_SIZE(3) + 520;
    DynamicJsonDocument doc(capacity);
    DeserializationError error = deserializeJson(doc, Serial);
    if (error) {
      return;
    }
    JsonObject obj = doc.as<JsonObject>();

    recCommand = obj["command"];
    objectType = obj["type"];
    objectsRemaining = obj["size"];

    targetXPixels = obj["x"];
    targetYPixels = obj["y"];

    inputX = obj["manX"];
    inputY = obj["manY"];
    motorSpeed = obj["speed"];
    pick = obj["pick"];
    drop = obj["drop"];
  }
}

/**
  Receives input from Serial and check if
  the data corresponds the valid command.

  @param inputCommand, valid command int

  @return true if input matches,
          else false
*/
boolean isCommandValid(int inputCommand) {
  return (recCommand == inputCommand) ? true : false;
}

/**
  Change only command on new change.

  @return true if changed,
          else false
*/
boolean isCommandChanged() {
  return (recCommand != oldCommand) ? true : false;
}

/**
   Change the state of the statemachine to the new state
   given by the parameter newState only if the command
   received is changed.

   @param newState The new state to set the statemachine to
*/
void changeStateTo(int newState) {
  if (isCommandChanged() || (newState == S_MANUAL)) {
    currentState = newState;
  }
}

/**
   Checks if the timer has expired. If the timer has expired,
   true is returned. If the timer has not yet expired,
   false is returned.

   @return true if timer has expired, false if not
*/
boolean timerHasExpired() {
  return (millis() > nextTimeout) ? true : false;
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
}


/**
  Calibreates motors.
  Be aware the motors will move during this process!
*/
void calibreateMotors() {
  int requested_state;
  requested_state = ODriveArduino::AXIS_STATE_MOTOR_CALIBRATION;
  requested_state = ODriveArduino::AXIS_STATE_MOTOR_CALIBRATION;
  odrive.run_state(MOTOR_X, requested_state, true);
  requested_state = ODriveArduino::AXIS_STATE_ENCODER_OFFSET_CALIBRATION;
  odrive.run_state(MOTOR_X, requested_state, true);
  requested_state = ODriveArduino::AXIS_STATE_CLOSED_LOOP_CONTROL;
  odrive.run_state(MOTOR_X, requested_state, false); // don't wait
  delay(1000);
  requested_state = ODriveArduino::AXIS_STATE_MOTOR_CALIBRATION;
  odrive.run_state(MOTOR_Y, requested_state, true);
  requested_state = ODriveArduino::AXIS_STATE_ENCODER_OFFSET_CALIBRATION;
  odrive.run_state(MOTOR_Y, requested_state, true);
  requested_state = ODriveArduino::AXIS_STATE_CLOSED_LOOP_CONTROL;
  odrive.run_state(MOTOR_Y, requested_state, false); // don't wait
  delay(1000);

  readMotorPositions();
  updateManualPosition();
}

/**
  Read current motor position from Odrive.
  Storing them in the global motorPosition
  variable.
*/
void readMotorPositions() {
  static const unsigned long duration = 5;
  unsigned long start = millis();
  while (millis() - start < duration) {
    for (int motorNumber = 0; motorNumber < 2; ++motorNumber) {
      ODRIVE_SERIAL << "r axis" << motorNumber << ".encoder.pos_estimate\n";
      motorPosition[motorNumber] = odrive.readFloat();
    }
  }
  actualX = motorPosition[0];
  actualY = motorPosition[1];
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
  Change the speed of the motor.

  @param speedLimit, the new speed to be set
*/
void setMotorSpeedFromController() {
  if (motorSpeed != 0) {
    currentSpeed += (100 * motorSpeed);
    for (int axis = 0; axis < 2; ++axis) {
      ODRIVE_SERIAL << "w axis" << axis << ".controller.config.vel_limit " << MOTOR_SPEED_LIMIT << '\n';
    }
  }
}

/**
  Initialize limit switches to inputs.
*/
void initializeSwitches() {
  pinMode(EMERGENCY_STOP_BUTTON, INPUT);
  pinMode(LIMIT_SWITCH_X_LEFT, INPUT);
  pinMode(LIMIT_SWITCH_X_RIGHT, INPUT);
  pinMode(LIMIT_SWITCH_Y_BOTTOM, INPUT);
  pinMode(LIMIT_SWITCH_Y_TOP, INPUT);
}

/**
  Initialize valve operations to outputs.
*/
void initializeValveOperations() {
  pinMode(PISTON_DOWN, OUTPUT);
  pinMode(PISTON_UP, OUTPUT);
  pinMode(VACUUM, OUTPUT);
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
    terminateMotors();
    currentState = S_READY;
  }
}

/**
  Change the set value of the
  PID X, and Y.

  @param x, new x position
  @param y, new y position
*/
void setPosition(float x, float y) {
  targetX = x;
  targetY = y;
}

/**
  Assigns the drop coordinates based
  on the object type.
*/
void objectSorter(int object) {
  switch (object) {
    case 0:
      // Home position
      setPosition(39100, 2300);
      break;

    case 1:
      // Square position
      setPosition(39100, 2300);
      break;

    case 2:
      // Circle position
      setPosition(39100, 2300);
      break;

    case 3:
      // Rectangle position
      setPosition(39100, 2300);
      break;

    case 4:
      // Triangle position
      setPosition(39100, 2300);
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
  digitalWrite(PISTON_UP, LOW);
  digitalWrite(PISTON_DOWN, HIGH);
  digitalWrite(VACUUM, HIGH);
  delay(20);
  digitalWrite(PISTON_DOWN, LOW);
  digitalWrite(PISTON_UP, HIGH);
  return true;
}

/**
  Drop object sequence.
*/
boolean dropObject() {
  digitalWrite(PISTON_UP, LOW);
  digitalWrite(PISTON_DOWN, HIGH);
  digitalWrite(VACUUM, LOW);
  delay(20);
  digitalWrite(PISTON_DOWN, LOW);
  digitalWrite(PISTON_UP, HIGH);
  return true;
}

/**
  Reset valves to init position.
*/
void resetValves() {
  digitalWrite(PISTON_DOWN, LOW);
  digitalWrite(PISTON_UP, HIGH);
  digitalWrite(VACUUM, LOW);
  digitalWrite(PISTON_UP, LOW);
}

/**
  Converts pixels to counts,
  mapped to X-Axis
*/
int convertFromPixelsToCountsX(int pixels) {
  int inputX = constrain(pixels, AXIS_X_LOWER, AXIS_X_HIGHER);
  int outputX = map(inputX, AXIS_X_LOWER, AXIS_X_HIGHER, encoderXOffset, motorXEndCounts);
  outputX += TOOL_OFFSET_X;
  return outputX;
}

/**
  Converts pixels to counts,
  mapped to Y-Axis
*/
int convertFromPixelsToCountsY(int pixels) {
  int inputY = constrain(pixels, AXIS_Y_LOWER, AXIS_Y_HIGHER);
  int outputY = map(inputY, AXIS_Y_LOWER, AXIS_Y_HIGHER, motorYEndCounts, encoderYOffset);
  //int outputY = map(inputY, AXIS_Y_LOWER, AXIS_Y_HIGHER, encoderYOffset, motorYEndCounts);
  outputY += TOOL_OFFSET_Y;
  return outputY;
}

/**
  Stop motors immediately.
*/
void terminateMotors() {
  int requested_state;
  requested_state = ODriveArduino::AXIS_STATE_IDLE;
  odrive.run_state(MOTOR_X, requested_state, true);
  odrive.run_state(MOTOR_Y, requested_state, true);
}

/**
  Updates manual position to what
  it currenty is. This to eliminate
  any big leaps with change to
  manual control.
*/
void updateManualPosition() {
  manualX = actualX;
  manualY = actualY;
}

/**
  Checks if robot is on target.

  @return true if error is 0
         else false
*/
boolean onTarget() {
  float errorX = abs(targetX - actualX);
  float errorY = abs(targetY - actualY);
  return ((errorX == 0) && (errorY == 0)) ? true : false;
}

/**
  Checks if there are more objects
  in the field.

  @return true if found,
          else false
*/
boolean areThereMoreObjects() {
  return (objectsRemaining != 0) ? true : false;
}

/**
  Calibrates the encoders. Driving
  them to end switches and stores
  the offset.
*/
void encoderCalibration() {
  for (int counts = 0; counts > -80000; counts -= 10) {
    if (SwitchFilter1.isSwitchOn(LIMIT_SWITCH_X_LEFT)) {
      encoderXOffset = counts;
      break;
    }
    setMotorPosition(MOTOR_X, counts);
  }
  for (int counts = 0; counts < 80000; counts += 10) {
    if (SwitchFilter2.isSwitchOn(LIMIT_SWITCH_Y_BOTTOM)) {
      encoderYOffset = counts;
      break;
    }
    setMotorPosition(MOTOR_Y, counts);
  }
  for (int counts = 0; counts < 80000; counts += 10) {
    int positionX = encoderXOffset + counts;

    if (SwitchFilter3.isSwitchOn(LIMIT_SWITCH_X_RIGHT)) {
      motorXEndCounts = positionX;
      break;
    }
    setMotorPosition(MOTOR_X, positionX);
  }
  for (int counts = 0; counts > -120000; counts -= 10) {
    int positionY = encoderYOffset + counts;
    if (SwitchFilter4.isSwitchOn(LIMIT_SWITCH_Y_TOP)) {
      motorYEndCounts = positionY;
      break;
    }
    setMotorPosition(MOTOR_Y, positionY);
  }
  delay(500);
  setMotorPosition(MOTOR_X, encoderXOffset + 39100);
  setMotorPosition(MOTOR_Y, encoderYOffset - 4000);
}

/**
  TODO: Add comment
*/
void setToolPosition(double x, double y) {
  double xnew = encoderXOffset + TOOL_OFFSET_X + x;
  double ynew = encoderYOffset - TOOL_OFFSET_Y - y;
  setMotorPosition(MOTOR_X, xnew);
  setMotorPosition(MOTOR_Y, ynew);
}

/**
  Checks if emergency stop is pressed.

  @return true if pressed,
         else false
*/
void emergencyStop() {
  if (EmergencySwitch.isSwitchOn(EMERGENCY_STOP_BUTTON)) {
    terminateMotors();
    currentState = S_READY;
  }
}

/**
  Template for printing
  to ODrive v3.6
*/
template<class T> inline Print& operator <<(Print &obj,     T arg) {
  obj.print(arg);
  return obj;
}

/**
  Template for printing
  to ODrive v3.6
*/
template<>        inline Print& operator <<(Print &obj, float arg) {
  obj.print(arg, 4);
  return obj;
}
