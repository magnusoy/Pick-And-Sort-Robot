const int EDGE_BUTTON_ONE = 2;
const int EDGE_BUTTON_TWO = 3;


void setup() {
  Serial.begin(9600);
  
  pinMode(EDGE_BUTTON_ONE, INPUT);
  pinMode(EDGE_BUTTON_TWO, INPUT);

}

void loop() {
  int buttonState1 = digitalRead(EDGE_BUTTON_ONE);
  int buttonState2 = digitalRead(EDGE_BUTTON_TWO);
  Serial.print(buttonState1);
  Serial.print("|");
  Serial.println(buttonState2);
  delay(200);
}
