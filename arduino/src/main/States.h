#ifndef _STATES_H
#define _STATES_H

// Constants representing the states in the state machine
enum states {
  S_IDLE,
  S_CALIBRATION,
  S_READY,
  S_MOVE_TO_OBJECT,
  S_PICK_OBJECT,
  S_MOVE_TO_DROP,
  S_DROP_OBJECT,
  S_COMPLETED,
  S_RESET,
  S_MANUAL,
  S_CONFIGURE
};

enum sortStates {
  HOME,
  SQUARE,
  CIRCLE,
  RECTANGLE,
  TRIANGLE
};

/**
  const int S_IDLE = 0;
  const int S_CALIBRATION = 1;
  const int S_READY = 2;
  const int S_MOVE_TO_OBJECT = 3;
  const int S_PICK_OBJECT = 4;
  const int S_MOVE_TO_DROP = 5;
  const int S_DROP_OBJECT = 6;
  const int S_COMPLETED = 7;
  const int S_RESET = 8;
  const int S_MANUAL = 9;
  const int S_CONFIGURE = 10;
*/
#endif // _STATES_H 
