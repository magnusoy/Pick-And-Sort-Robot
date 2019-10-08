const int PISTON_DOWN = 6;
const int PISTON_UP = 7;
const int VACUUM = 8;

void setup() {
  // Initialize Serial ports
  startSerial();
  initializeValveOperations();
}

void loop() {
  if (Serial.available()) {
    char c = Serial.read();

    if (c == 'd') {
      digitalWrite(PISTON_UP, LOW);
      digitalWrite(PISTON_DOWN, HIGH);
    }
    if (c == 'u') {
      digitalWrite(PISTON_DOWN, LOW);
      digitalWrite(PISTON_UP, HIGH);
    }

    if (c == 'v') {
      digitalWrite(VACUUM, HIGH);
    }
    if (c == 'b') {
      digitalWrite(VACUUM, LOW);
    }
    if (c == 'r') {
      digitalWrite(PISTON_UP, LOW);
      digitalWrite(PISTON_DOWN, LOW);
      digitalWrite(VACUUM, LOW);
    }
  }
}



/**
  Start Serial communication with ODrive,
  and Jetson Nano.
*/
void startSerial() {
  // Start Serial Communication
  Serial.begin(115200);
  while (!Serial);

  Serial.println("Ready!");
  Serial.println("Send the character 'd' to go down");
  Serial.println("Send the character 'u' to go up");
  Serial.println("Send the character 'v' to vacuum on");
  Serial.println("Send the character 'b' to vacuum off");
  Serial.println("Send the character 'r' to reset ");
  
}



/**
  Initialize valve operations to outputs.
*/
void initializeValveOperations() {
  pinMode(PISTON_DOWN, OUTPUT);
  pinMode(PISTON_UP, OUTPUT);
  pinMode(VACUUM, OUTPUT);
}
