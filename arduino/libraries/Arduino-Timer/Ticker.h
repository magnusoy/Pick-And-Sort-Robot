#ifndef _TICKER_H_
#define _TICKER_H_

#if (ARDUINO >= 100)
#include "Arduino.h"
#else
#include "WProgram.h"
#endif


class Ticker
{
public:
    Ticker();
    bool hasTimerExpired();
    void startTimer(unsigned long duration);

private:
    // Time for next timeout, in milliseconds
    unsigned long nextTimeout;
};

#endif // _TICKER_H_