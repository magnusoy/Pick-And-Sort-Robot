#include "Ticker.h"

/**
  Timer Constructor.
*/
Ticker::Ticker() {}

/**
   Starts the timer and set the timer to expire after the
   number of milliseconds given by the parameter duration.
   
   @param duration The number of milliseconds until the timer expires.
*/
void Ticker::startTimer(unsigned long duration)
{
    this->nextTimeout = millis() + duration;
}

/**
   Checks if the timer has expired. If the timer has expired,
   true is returned. If the timer has not yet expired,
   false is returned.

   @return true if timer has expired, false if not
*/
bool Ticker::hasTimerExpired()
{
    return (millis() > this->nextTimeout) ? true : false;
}