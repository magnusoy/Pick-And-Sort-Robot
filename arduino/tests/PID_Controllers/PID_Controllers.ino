// Including libraries
#include <PID.h>
#include <ODriveArduino.h>
#include "IO.h"
#include "PidParameters.h"
#include "OdriveParameters.h"


#define ODRIVE_SERIAL Serial1 // RX, TX (0, 1)
// Note: must also connect GND on ODrive to GND on Arduino!

// ODrive object
ODriveArduino odrive(ODRIVE_SERIAL);

// Vector storing the motor 1 and 2 encoder position
int motorPosition[] = {0, 0};

// PID X - Axis
double actualValueX = 0.0; double setValueX = 0.0; double outputValueX = 0.0;
PID pidX(kpX, kiX, kdX, REVERSE); // TODO: Change to correct direction

// PID Y - Axis
double actualValueY = 0.0; double setValueY = 0.0; double outputValueY = 0.0;
PID pidY(kpY, kiY, kdY, REVERSE); // TODO: Change to correct direction

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
  readMotorPositions();
  outputValueX = pidX.compute(actualValueX, setValueX);
  outputValueY = pidY.compute(actualValueX, setValueX);
  setMotorPosition(MOTOR_X, outputValueX);
  setMotorPosition(MOTOR_Y, outputValueY);
  edgeDetection();
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
    while (true) { }
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
