#ifndef _STATES_H
#define _STATES_H

// Constants representing the states in the state machine
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

#endif // _STATES_H 
