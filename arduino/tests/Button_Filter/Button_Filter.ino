#define ACTIVE_END_SWITCH_TIME 1000  // In millis

// Defining button pin
const int BTN = 3;

// Time for next timeout, in milliseconds
unsigned long nextButtonTimeout = 0;

// Storing old button state
int oldBtnState;

void setup() {
  Serial.begin(9600);
  pinMode(BTN, INPUT);
}

void loop() {
  if (isSwitchOn(BTN)) {
    Serial.println("ON");
  }
}

/**
   Starts the timer and set the timer to expire after the
   number of milliseconds given by the parameter duration.

   @param duration The number of milliseconds until the timer expires.
*/
void startButtonTimer(unsigned long duration) {
  nextButtonTimeout = millis() + duration;
}

/**
   Checks if the timer has expired. If the timer has expired,
   true is returned. If the timer has not yet expired,
   false is returned.

   @return true if timer has expired, false if not
*/
boolean buttonTimerHasExpired() {
  return (millis() > nextButtonTimeout) ? true : false;
}

/**
  Check if a switch is HIGH for longer
  than the given duration.

  @param btn, end switch

  @return true if its high over the
          exceeded time limit,
          else false
*/
boolean isSwitchOn(int btn) {
  int btnState = digitalRead(btn);
  if (btnState != oldBtnState) {
    oldBtnState = btnState;
    startButtonTimer(ACTIVE_END_SWITCH_TIME);
  }
  return ((buttonTimerHasExpired()) && (btnState)) ? true : false;
}
