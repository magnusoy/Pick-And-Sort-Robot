const int EDGE_BUTTON_ONE = 2;
const int EDGE_BUTTON_TWO = 3;
const int EDGE_BUTTON_THREE = 4;
const int EDGE_BUTTON_FOUR = 5;


void setup() {
  Serial.begin(9600);

  pinMode(EDGE_BUTTON_ONE, INPUT);
  pinMode(EDGE_BUTTON_TWO, INPUT);
  pinMode(EDGE_BUTTON_THREE, INPUT);
  pinMode(EDGE_BUTTON_FOUR, INPUT);

}

void loop() {
  int buttonState1 = digitalRead(EDGE_BUTTON_ONE);
  int buttonState2 = digitalRead(EDGE_BUTTON_TWO);
  int buttonState3 = digitalRead(EDGE_BUTTON_THREE);
  int buttonState4 = digitalRead(EDGE_BUTTON_FOUR);
  Serial.print(buttonState1);
  Serial.print("|");
  Serial.print(buttonState2);
  Serial.print("|");
  Serial.print(buttonState3);
  Serial.print("|");
  Serial.println(buttonState4);

  delay(200);
}
