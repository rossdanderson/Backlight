#pragma once

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

#define BUFFER_SIZE 255
#ifdef MEGA
    #define LED_PIN 6
#else
    #define LED_PIN 0
#endif
#define LED_COUNT 60

const byte writeLED = 0;
const size_t writeLEDSize = 5;

const byte writeAll = 1;
const size_t writeAllSize = 181; // 60 * 3 + 1

const byte heartbeat = 2;

const byte heartbeatAck = 3;

const byte handshakeRequest = 4;
const size_t handshakeRequestSize = 1;

const byte handshakeResponse = 5;

const byte print = 6;

const byte setBrightness = 7;
const size_t setBrightnessSize = 2;