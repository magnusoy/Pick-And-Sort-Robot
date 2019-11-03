#ifndef _TIMER_H_
#define _TIMER_H_

#if (ARDUINO >= 100)
#include "Arduino.h"
#else
#include "WProgram.h"
#endif


class Timer
{
public:
    Timer();
    bool hasTimerExpired();
    void startTimer(unsigned long duration);

private:
    // Time for next timeout, in milliseconds
    unsigned long nextTimeout;
}

#endif // _TIMER_H_