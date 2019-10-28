#ifndef _STATES_H
#define _STATES_H

// Enums representing the states in the state machine
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

// Enums representing sorting states
enum sortStates {
  HOME,
  SQUARE,
  CIRCLE,
  RECTANGLE,
  TRIANGLE
};

#endif // _STATES_H 
