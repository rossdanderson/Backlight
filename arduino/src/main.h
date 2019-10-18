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

void handlePacket(const uint8_t *buffer, size_t size);
void handleSetBrightnessMessage(const uint8_t *decodedBuffer);
void handleWriteLEDMessage(const uint8_t *decodedBuffer);
void handleWriteAllMessage(const uint8_t *decodedBuffer);
void handleHandshakeRequestMessage();
void sendMessage(const String &message);

const uint16_t writeLED = 0;
const size_t writeLEDSize = 5;

const uint16_t writeAll = 1;
const size_t writeAllSize = 181; // 60 * 3 + 1

const uint16_t heartbeat = 2;

const uint16_t heartbeatAck = 3;

const uint16_t handshakeRequest = 4;
const size_t handshakeRequestSize = 1;

const uint16_t handshakeResponse = 5;

const uint16_t print = 6;

const uint16_t setBrightness = 7;
const size_t setBrightnessSize = 2;