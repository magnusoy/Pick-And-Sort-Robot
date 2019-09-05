/**
 Serial communication functions.
 */

#define UPDATE_SERIAL_TIME 1000 // In millis

// Time for next timeout, in milliseconds
unsigned long nextTimeout = 0;


void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);

}

void loop() {
  // put your main code here, to run repeatedly:
  if (isValidCommand('s')) {
    Serial.println("Starting...");
  }
  writeToSerial(UPDATE_SERIAL_TIME);

}


/**
  Writes periodically to the Serial.

  @param updateTime in millis
*/
void writeToSerial(unsigned long updateTime) {
  if (timerHasExpired()) {
    Serial.println("Hello");
    startTimer(updateTime);
  }
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

  if (Serial.available() > 0) {
    received = Serial.read();
  }
  if (received == inputCommand) {
    valid = true;
  }
  return valid;
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
