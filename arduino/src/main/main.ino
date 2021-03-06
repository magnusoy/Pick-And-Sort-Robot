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
  Date: 03.11-2019
  Version: 2.4
  Website: https://github.com/magnusoy/Pick-And-Sort-Robot
*/


// Including libraries
#include <ODriveArduino.h>
#include <ArduinoJson.h>
#include <ButtonTimer.h>
#include <Ticker.h>
#include "IO.h"
#include "OdriveParameters.h"
#include "States.h"
#include "Commands.h"
#include "CoordinatesAndOffsets.h"


// Durations and intervals in millis
#define UPDATE_SERIAL_INTERVAL 100
#define ACTIVE_END_SWITCH_DURATION 15
#define VACCUM_DELAY_DURATION 400
#define PICK_DELAY_DURATION 400
#define DROP_DELAY_DURATION 400

// Defining button filters
ButtonTimer switchFilter1(ACTIVE_END_SWITCH_DURATION);
ButtonTimer switchFilter2(ACTIVE_END_SWITCH_DURATION);
ButtonTimer switchFilter3(ACTIVE_END_SWITCH_DURATION);
ButtonTimer switchFilter4(ACTIVE_END_SWITCH_DURATION);
ButtonTimer emergencySwitch(ACTIVE_END_SWITCH_DURATION);

// Defining pick and drop timers
Ticker pickAndDropTimer;
Ticker vacuumTimer;
Ticker completedTimer;

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
int numberOfPlacedSquares = 0;
int numberOfPlacedCircles = 0;
int numberOfPlacedRectangles = 0;
int numberOfPlacedTriangles = 0;
int oldCommand = 0;
int recCommand = 0;
float targetXPixels = 0.0f;
float targetYPixels = 0.0f;

boolean vacuum_timer_started = false;

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
  flushSerial();

  // Read motor position at any given state
  readMotorPositions();

  // State machine
  switch (currentState) {
    case S_IDLE:
      readJSONDocumentFromSerial();
      if (isCommandValid(CALIBRATE)) {
        newChangeStateTo(S_READY);
      }
      break;

    case S_CALIBRATION:
      calibreateMotors();
      changeStateTo(S_READY);
      break;

    case S_READY:
      readJSONDocumentFromSerial();
      if (isCommandValid(START) && isCommandChanged()) {
        if ((areThereMoreObjects()) && (!isContainerFull(objectType))) {
          targetX = convertFromPixelsToCountsX(targetXPixels);
          targetY = convertFromPixelsToCountsY(targetYPixels);
          setToolPosition(targetX, targetY);
          newChangeStateTo(S_MOVE_TO_OBJECT);
        }
      } else if (isCommandValid(MANUAL_CONTROL)) {
        newChangeStateTo(S_MANUAL);
      } else if (isCommandValid(RESET)) {
        setMotorsInControlMode();
        emptyContainers();
        oldCommand = recCommand;
      }
      break;

    case S_MOVE_TO_OBJECT:
      updateManualPosition();
      if (isCommandValid(RESET)) {
        newChangeStateTo(S_READY);
      }
      if (onTarget()) {
        pickAndDropTimer.startTimer(PICK_DELAY_DURATION);
        changeStateTo(S_PICK_OBJECT);
      }
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
        pickAndDropTimer.startTimer(DROP_DELAY_DURATION);
        changeStateTo(S_DROP_OBJECT);
      }
      break;

    case S_DROP_OBJECT:
      if (dropObject()) {
        completedTimer.startTimer(500);
        changeStateTo(S_COMPLETED);
      }
      break;

    case S_COMPLETED:
      readJSONDocumentFromSerial();
      if (completedTimer.hasTimerExpired()) {
        if (onTarget()) {

          if (!areThereMoreObjects()) {
            objectSorter(HOME);
            setToolPosition(targetX, targetY);
            changeStateTo(S_RESET);
          } else if (targetXPixels == HOME_POSITION_X) {
            targetX = convertFromPixelsToCountsX(targetXPixels);
            targetY = convertFromPixelsToCountsY(targetYPixels);
            setToolPosition(targetX, targetY);
            changeStateTo(S_READY);
          } else {
            targetX = convertFromPixelsToCountsX(targetXPixels);
            targetY = convertFromPixelsToCountsY(targetYPixels);
            setToolPosition(targetX, targetY);
            changeStateTo(S_MOVE_TO_OBJECT);
          }
        }
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

      manualX += (currentSpeed * inputX);
      manualY += (currentSpeed * inputY);
      setMotorPosition(MOTOR_X, manualX);
      setMotorPosition(MOTOR_Y, manualY);

      setMotorSpeedFromController();

      if (pick) manualPickObject();
      if (drop) manualDropObject();

      if (isCommandValid(AUTOMATIC_CONTROL)) {
        newChangeStateTo(S_READY);
      } else if (isCommandValid(CONFIGURE)) {
        newChangeStateTo(S_CONFIGURE);
      }
      break;

    case S_CONFIGURE:
      // TODO: Add ODrive configurations
      if (isCommandValid(RESET)) {
        newChangeStateTo(S_IDLE);
      }
      break;

    default:
      // Tries to change state to S_IDLE
      terminateMotors();
      changeStateTo(S_IDLE);
      break;
  }
  updateManualPosition();
  emergencyStop();
  edgeDetection();
  writeToSerial(UPDATE_SERIAL_INTERVAL);
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
  serializeJson(doc, Serial);
  Serial.print("\n");
}

/**
  Reads content sent from the Teensy and
  flushes it, as it is for no use.
*/
void flushSerial() {
  if (Serial.available() > 0) {
    const size_t capacity = 15 * JSON_ARRAY_SIZE(2) + JSON_ARRAY_SIZE(10) + 11 * JSON_OBJECT_SIZE(3) + 520;
    DynamicJsonDocument doc(capacity);
    DeserializationError error = deserializeJson(doc, Serial);
    if (error) {
      return;
    }
    JsonObject obj = doc.as<JsonObject>();
  }
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

    int offsetY = 0;
    int offsetX = 2;
    if ((objectType == 14) || (objectType == 13)) {
      offsetY = 3;
    }

    if (obj.containsKey("x")) {
      targetXPixels = obj["x"];
      targetYPixels = obj["y"];
      if (targetXPixels > 220) {
        offsetX = -2;
      }
      targetXPixels += offsetX;
      targetYPixels += offsetY;
    } else {
      targetXPixels = HOME_POSITION_X;
      targetYPixels = HOME_POSITION_Y;
    }

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
void newChangeStateTo(int newState) {
  if (isCommandChanged()) {
    oldCommand = recCommand;
    currentState = newState;
  }
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
  Sets the motors in closed loop
  control mode.
*/
void setMotorsInControlMode() {
  int requested_state;
  requested_state = ODriveArduino::AXIS_STATE_CLOSED_LOOP_CONTROL;
  odrive.run_state(MOTOR_X, requested_state, false); // don't wait
  requested_state = ODriveArduino::AXIS_STATE_CLOSED_LOOP_CONTROL;
  odrive.run_state(MOTOR_Y, requested_state, false); // don't wait
}

/**
  Read current motor position from Odrive.
  Storing them in the global motorPosition
  variable.
*/
void readMotorPositions() {
  for (int motorNumber = 0; motorNumber < 2; ++motorNumber) {
    ODRIVE_SERIAL << "r axis" << motorNumber << ".encoder.pos_estimate\n";
    motorPosition[motorNumber] = odrive.readFloat();
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
    currentSpeed += (MOTOR_SPEED_MULTIPLIER * motorSpeed);
  }
  currentSpeed = constrain(currentSpeed, MOTOR_SPEED_LOWER, MOTOR_SPEED_UPPER);
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
  int buttonState1 = switchFilter1.isSwitchOn(LIMIT_SWITCH_X_LEFT);
  int buttonState2 = switchFilter2.isSwitchOn(LIMIT_SWITCH_X_RIGHT);
  int buttonState3 = switchFilter3.isSwitchOn(LIMIT_SWITCH_Y_BOTTOM);
  int buttonState4 = switchFilter4.isSwitchOn(LIMIT_SWITCH_Y_TOP);
  if (buttonState1 || buttonState2
      || buttonState3 || buttonState4) {
    terminateMotors();
    changeStateTo(S_READY);
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

  @param object is the type
*/
void objectSorter(int object) {
  int sortState = object - 10;
  int x;
  int y;
  switch (sortState) {
    case HOME:
      // Home position
      x = convertFromPixelsToCountsX(HOME_POSITION_X);
      y = convertFromPixelsToCountsY(HOME_POSITION_Y);
      setPosition(x, y);
      break;

    case SQUARE:
      // Square position
      if (numberOfPlacedSquares == 0) {
        x = convertFromPixelsToCountsX(SQUARE_SORTED_X);
        y = convertFromPixelsToCountsY(SQUARE_SORTED_Y);
      } else if (numberOfPlacedSquares == 1) {
        x = convertFromPixelsToCountsX(SQUARE_SORTED_X + 35);
        y = convertFromPixelsToCountsY(SQUARE_SORTED_Y - 30);
      } else {
        x = convertFromPixelsToCountsX(HOME_POSITION_X);
        y = convertFromPixelsToCountsY(HOME_POSITION_Y);
      }
      setPosition(x, y);
      numberOfPlacedSquares += 1;
      break;

    case CIRCLE:
      // Circle position
      if (numberOfPlacedCircles == 0) {
        x = convertFromPixelsToCountsX(CIRCLE_SORTED_X);
        y = convertFromPixelsToCountsY(CIRCLE_SORTED_Y);
      } else if (numberOfPlacedCircles == 1) {
        x = convertFromPixelsToCountsX(CIRCLE_SORTED_X + 35);
        y = convertFromPixelsToCountsY(CIRCLE_SORTED_Y - 30);
      } else {
        x = convertFromPixelsToCountsX(HOME_POSITION_X);
        y = convertFromPixelsToCountsY(HOME_POSITION_Y);
      }
      setPosition(x, y);
      numberOfPlacedCircles += 1;
      break;

    case RECTANGLE:
      // Rectangle position
      if (numberOfPlacedRectangles == 0) {
        x = convertFromPixelsToCountsX(RECTANGLE_SORTED_X);
        y = convertFromPixelsToCountsY(RECTANGLE_SORTED_Y);
      } else if (numberOfPlacedRectangles == 1) {
        x = convertFromPixelsToCountsX(RECTANGLE_SORTED_X + 35);
        y = convertFromPixelsToCountsY(RECTANGLE_SORTED_Y - 30 );
      } else {
        x = convertFromPixelsToCountsX(HOME_POSITION_X);
        y = convertFromPixelsToCountsY(HOME_POSITION_Y);
      }
      setPosition(x, y);
      numberOfPlacedRectangles += 1;
      break;

    case TRIANGLE:
      // Triangle position
      if (numberOfPlacedTriangles == 0) {
        x = convertFromPixelsToCountsX(TRIANGLE_SORTED_X);
        y = convertFromPixelsToCountsY(TRIANGLE_SORTED_Y);
      } else if (numberOfPlacedTriangles == 1) {
        x = convertFromPixelsToCountsX(TRIANGLE_SORTED_X + 35);
        y = convertFromPixelsToCountsY(TRIANGLE_SORTED_Y - 30 );
      } else {
        x = convertFromPixelsToCountsX(HOME_POSITION_X);
        y = convertFromPixelsToCountsY(HOME_POSITION_Y);
      }
      setPosition(x, y);
      numberOfPlacedTriangles += 1;
      break;

    default:
      changeStateTo(S_IDLE);
      break;
  }
}

/**
  Pick object sequence.

  @return true when completed
*/
boolean pickObject() {
  boolean picked = false;

  if (pickAndDropTimer.hasTimerExpired()) {
    digitalWrite(PISTON_DOWN, LOW);
    digitalWrite(PISTON_UP, HIGH);
    if (!vacuum_timer_started) {
      vacuum_timer_started = true;
      vacuumTimer.startTimer(VACCUM_DELAY_DURATION);
    }

  } else {
    digitalWrite(PISTON_UP, LOW);
    digitalWrite(PISTON_DOWN, HIGH);
    digitalWrite(VACUUM, HIGH);
  }
  if ((pickAndDropTimer.hasTimerExpired()) && (vacuumTimer.hasTimerExpired())) {
    picked = true;
    vacuum_timer_started = false;
  }
  return picked;
}

/**
  Drop object sequence.

  @return true when completed
*/
boolean dropObject() {
  boolean dropped = false;

  if (pickAndDropTimer.hasTimerExpired()) {
    digitalWrite(PISTON_DOWN, LOW);
    digitalWrite(VACUUM, LOW);
    digitalWrite(PISTON_UP, HIGH);
    if (!vacuum_timer_started) {
      vacuum_timer_started = true;
      vacuumTimer.startTimer(VACCUM_DELAY_DURATION);
    }
  } else {
    digitalWrite(PISTON_UP, LOW);
    digitalWrite(PISTON_DOWN, HIGH);
  }
  if ((pickAndDropTimer.hasTimerExpired()) && (vacuumTimer.hasTimerExpired())) {
    dropped = true;
    vacuum_timer_started = false;
  }
  return dropped;
}

/**
  Manual Pick object sequence.
*/
void manualPickObject() {
  digitalWrite(PISTON_UP, LOW);
  digitalWrite(PISTON_DOWN, HIGH);
  digitalWrite(VACUUM, HIGH);
  delay(200);
  digitalWrite(PISTON_DOWN, LOW);
  digitalWrite(PISTON_UP, HIGH);
  delay(300);
  pick = false;
  drop = false;
}

/**
  Manual Drop object sequence.
*/
void manualDropObject() {
  digitalWrite(PISTON_UP, LOW);
  digitalWrite(PISTON_DOWN, HIGH);
  delay(200);
  digitalWrite(PISTON_DOWN, LOW);
  digitalWrite(VACUUM, LOW);
  digitalWrite(PISTON_UP, HIGH);
  delay(200);
  pick = false;
  drop = false;
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

  @param pixels, raw pixels from
         camera in Y axis

  @return pixels mapped to counts
*/
int convertFromPixelsToCountsX(int pixels) {
  int outputX = map(pixels, AXIS_X_LOWER, AXIS_X_HIGHER, encoderXOffset - TOOL_OFFSET_X, motorXEndCounts + TOOL_OFFSET_X);
  outputX = constrain(outputX, encoderXOffset, motorXEndCounts);
  return outputX + OFFSET_X;
}

/**
  Converts pixels to counts,
  mapped to Y-Axis

  @param pixels, raw pixels from
         camera in Y axis

  @return pixels mapped to counts
*/
int convertFromPixelsToCountsY(int pixels) {
  int outputY = map(pixels, AXIS_Y_LOWER, AXIS_Y_HIGHER, encoderYOffset + TOOL_OFFSET_Y, motorYEndCounts - TOOL_OFFSET_Y);
  return outputY + OFFSET_Y;
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
  int errorX = abs(targetX - actualX);
  int errorY = abs(targetY - actualY);
  return ((errorX < 75) && (errorY < 75)) ? true : false;
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
    if (switchFilter1.isSwitchOn(LIMIT_SWITCH_X_LEFT)) {
      encoderXOffset = counts;
      break;
    }
    setMotorPosition(MOTOR_X, counts);
  }
  for (int counts = 0; counts < 80000; counts += 10) {
    if (switchFilter2.isSwitchOn(LIMIT_SWITCH_Y_BOTTOM)) {
      encoderYOffset = counts;
      break;
    }
    setMotorPosition(MOTOR_Y, counts);
  }
  for (int counts = 0; counts < 80000; counts += 10) {
    int positionX = encoderXOffset + counts;

    if (switchFilter3.isSwitchOn(LIMIT_SWITCH_X_RIGHT)) {
      motorXEndCounts = positionX;
      break;
    }
    setMotorPosition(MOTOR_X, positionX);
  }
  for (int counts = 0; counts > -120000; counts -= 10) {
    int positionY = encoderYOffset + counts;
    if (switchFilter4.isSwitchOn(LIMIT_SWITCH_Y_TOP)) {
      motorYEndCounts = positionY;
      break;
    }
    setMotorPosition(MOTOR_Y, positionY);
  }
  delay(500);
  setMotorPosition(MOTOR_X, encoderXOffset + 38000);
  setMotorPosition(MOTOR_Y, encoderYOffset - 4000);
}

/**
  Sets both of the motors to the given positions.

  @param x, x - axis set point
  @param y, y - axis set point
*/
void setToolPosition(double x, double y) {
  setMotorPosition(MOTOR_X, x);
  setMotorPosition(MOTOR_Y, y);
}

/**
  Checks if emergency stop is pressed.

  @return true if pressed,
         else false
*/
void emergencyStop() {
  if (emergencySwitch.isSwitchOn(EMERGENCY_STOP_BUTTON)) {
    terminateMotors();
    resetValves();
    changeStateTo(S_READY);
  }
}

/**
  Reset the number of stored objects
  in the containers.
*/
void emptyContainers() {
  numberOfPlacedSquares = 0;
  numberOfPlacedCircles = 0;
  numberOfPlacedRectangles = 0;
  numberOfPlacedTriangles = 0;
}

/**
  Checks if the type to be sorted is full.

  @param type as integer

  @return full, true if it is full
                else false
*/
boolean isContainerFull(int type) {
  boolean full = false;
  int typeState = type - 10;
  switch (typeState) {
    case SQUARE:
      if (numberOfPlacedSquares >= 2) {
        full = true;
      }
      break;

    case CIRCLE:
      if (numberOfPlacedCircles >= 2) {
        full = true;
      }
      break;

    case RECTANGLE:
      if (numberOfPlacedRectangles >= 2) {
        full = true;
      }
      break;

    case TRIANGLE:
      if (numberOfPlacedTriangles >= 2) {
        full = true;
      }
      break;

    default:
      break;
  }
  return full;
}

/**
  Template for printing
  to ODrive v3.6
*/
template<class T> inline Print& operator <<(Print & obj,     T arg) {
  obj.print(arg);
  return obj;
}

/**
  Template for printing
  to ODrive v3.6
*/
template<>        inline Print& operator <<(Print & obj, float arg) {
  obj.print(arg, 4);
  return obj;
}
