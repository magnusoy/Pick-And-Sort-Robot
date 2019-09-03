#ifndef _PidParameters_H
#define _PidParameters_H

// PID X - Axis
#define PIDX_OUTPUT_LOW 0
#define PIDX_OUTPUT_HIGH 0 // TODO: Change to correct value
#define PIDX_OUTPUT_OFFSET 0 // TODO: Change to correct value
#define PIDX_UPDATE_TIME 10 // In millis
double kpX = 0.0;
double kiX = 0.0;
double kdX = 0.0;

// PID Y - Axis
#define PIDY_OUTPUT_LOW 0
#define PIDY_OUTPUT_HIGH 0 // TODO: Change to correct value
#define PIDY_OUTPUT_OFFSET 0 // TODO: Change to correct value
#define PIDY_UPDATE_TIME 10 // In millis
double kpY = 0.0;
double kiY = 0.0;
double kdY = 0.0;

#endif // _PidParameters_H 
