const int LIMIT_SWITCH_Y_BOTTOM = 2;
const int LIMIT_SWITCH_Y_TOP = 3;
const int LIMIT_SWITCH_X_LEFT = 4;
const int LIMIT_SWITCH_X_RIGHT = 5;



void setup() {
  Serial.begin(9600);

  pinMode(LIMIT_SWITCH_Y_BOTTOM, INPUT);
  pinMode(LIMIT_SWITCH_Y_TOP, INPUT);
  pinMode(LIMIT_SWITCH_X_LEFT, INPUT);
  pinMode(LIMIT_SWITCH_X_RIGHT, INPUT);

}

void loop() {
  int buttonState1 = digitalRead(LIMIT_SWITCH_Y_BOTTOM);
  int buttonState2 = digitalRead(LIMIT_SWITCH_Y_TOP);
  int buttonState3 = digitalRead(LIMIT_SWITCH_X_LEFT);
  int buttonState4 = digitalRead(LIMIT_SWITCH_X_RIGHT);
  Serial.print(buttonState1);
  Serial.print("|");
  Serial.print(buttonState2);
  Serial.print("|");
  Serial.print(buttonState3);
  Serial.print("|");
  Serial.println(buttonState4);

  delay(200);
}
