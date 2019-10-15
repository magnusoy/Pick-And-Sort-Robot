// Including libraries
#include <ODriveArduino.h>
#include <ArduinoJson.h>
#include <ButtonTimer.h>

// For mapping pixels to counts
#define AXIS_X_LOWER 40
#define AXIS_X_HIGHER 26
#define AXIS_Y_LOWER 480
#define AXIS_Y_HIGHER 470

#define TOOL_OFFSET_X 1017.5
#define TOOL_OFFSET_Y 11575.8

#define MOTOR_SPEED_LIMIT 22000.0f
#define MOTOR_CURRENT_LIMIT 40.0f

#define MOTOR_X 0
#define MOTOR_Y 1

#define ODRIVE_SERIAL Serial1 // RX, TX (0, 1)

// ODrive object
ODriveArduino odrive(ODRIVE_SERIAL);

#define ACTIVE_END_SWITCH_TIME 20  // In millis

ButtonTimer SwitchFilter1(ACTIVE_END_SWITCH_TIME);
ButtonTimer SwitchFilter2(ACTIVE_END_SWITCH_TIME);
ButtonTimer SwitchFilter3(ACTIVE_END_SWITCH_TIME);
ButtonTimer SwitchFilter4(ACTIVE_END_SWITCH_TIME * 2);

// Vector storing the motor 1 and 2 encoder position
int motorPosition[] = {0, 0};

// Defining limit switches
const int LIMIT_SWITCH_Y_BOTTOM = 2;
const int LIMIT_SWITCH_Y_TOP = 3;
const int LIMIT_SWITCH_X_LEFT = 4;
const int LIMIT_SWITCH_X_RIGHT = 5;

// Defining valve operations
const int PISTON_DOWN = 6;
const int PISTON_UP = 7;
const int VACUUM = 8;

// Position control
float actualX = 0.0f;
float actualY = 0.0f;
float targetX = 0.0f;
float targetY = 0.0f;

// Variables storing object data
float targetXPixels = 0.0f;
float targetYPixels = 0.0f;

// Speed variables
int currentSpeed = MOTOR_SPEED_LIMIT;
int encoderXOffset = 0;
int encoderYOffset = 0;
int motorXEndCounts = 0;
int motorYEndCounts = 0;

#define HOME_POSITION_X 40000
#define HOME_POSITION_Y 4000

#define OBEJCT_POSITION_X 25000
#define OBJECT_POSITION_Y 50000

#define OBJECT_CONTAINER_X 20000
#define OBJECT_CONTAINER_Y 4000

#define CENTER_FRAME_X 40000
#define CENTER_FRAME_Y 55000

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
  Serial.println("Ready!");
  Serial.println("Send the character 'h' to go to home");
  Serial.println("Send the character 'o' to go to object");
  Serial.println("Send the character 'c' to go to center");
}

void loop() {
  if (Serial.available()) {
    char c = Serial.read();
    if (c == 'o') {
      setPosition(OBEJCT_POSITION_X, OBJECT_POSITION_Y);
      setToolPosition(targetX, targetY);
    }
    if (c == 'h') {
      setPosition(HOME_POSITION_X, HOME_POSITION_Y);
      setToolPosition(targetX, targetY);
    }

    if (c == 'c') {
      setPosition(CENTER_FRAME_X, CENTER_FRAME_Y);
      setToolPosition(targetX, targetY);
    }
  }
  readMotorPositions();
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
  Initialize limit switches to inputs.
*/
void initializeSwitches() {
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
  return outputX;
}

/**
  Converts pixels to counts,
  mapped to Y-Axis
*/
int convertFromPixelsToCountsY(int pixels) {
  int inputY = constrain(pixels, AXIS_Y_LOWER, AXIS_Y_HIGHER);
  int outputY = map(inputY, AXIS_Y_LOWER, AXIS_Y_HIGHER, motorYEndCounts, encoderYOffset);
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
  setMotorPosition(MOTOR_X, ynew);
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
