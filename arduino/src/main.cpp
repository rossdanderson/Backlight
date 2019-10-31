#include "main.h"
#include "state.h"
#include <PacketSerial.h>

PacketSerial serial;
unsigned long time;
unsigned long lastPrint = 0;
unsigned long lastMessage = 0;
uint8_t userDefinedBrightness = 255;
uint8_t currentBrightness = userDefinedBrightness;
state currentState = state::Disconnected;
Adafruit_NeoPixel strip(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);

void setup()
{
  serial.begin(115200);
  serial.setPacketHandler(&handlePacket);

  strip.begin();
  strip.show();

  // Run a test sequence
  strip.setBrightness(255);
  strip.fill(strip.Color(strip.gamma8(255), strip.gamma8(0), strip.gamma8(0)), 0, 0);
  strip.show();
  delay(250);
  strip.fill(strip.Color(strip.gamma8(0), strip.gamma8(255), strip.gamma8(0)), 0, 0);
  strip.show();
  delay(250);
  strip.fill(strip.Color(strip.gamma8(0), strip.gamma8(0), strip.gamma8(255)), 0, 0);
  strip.show();
  delay(250);
  strip.fill(strip.Color(strip.gamma8(0), strip.gamma8(0), strip.gamma8(0)), 0, 0);
  strip.show();
}

void handlePacket(const uint8_t *decodedBuffer, size_t decodedSize)
{
  bool handled = false;
  uint8_t header = decodedBuffer[0];
  if (header == setBrightness && decodedSize == setBrightnessSize)
  {
    handleSetBrightnessMessage(decodedBuffer);
    handled = true;
  }
  else if (header == writeLED && decodedSize == writeLEDSize)
  {
    handleWriteLEDMessage(decodedBuffer);
    handled = true;
  }
  else if (header == writeAll && decodedSize == writeAllSize)
  {
    handleWriteAllMessage(decodedBuffer);
    handled = true;
  }
  else if (header == handshakeRequest && decodedSize == handshakeRequestSize)
  {
    handleHandshakeRequestMessage();
    handled = true;
  }

  if (handled)
  {
    lastMessage = time;
  }
}

void handleSetBrightnessMessage(const uint8_t *decodedBuffer)
{
  if (currentState == state::Streaming)
  {
    userDefinedBrightness = decodedBuffer[1];
    currentBrightness = userDefinedBrightness;

    strip.setBrightness(currentBrightness);
    strip.show();
  }
}

void handleWriteLEDMessage(const uint8_t *decodedBuffer)
{
  if (currentState == state::Streaming)
  {
    byte index = decodedBuffer[1];
    byte red = decodedBuffer[2];
    byte green = decodedBuffer[3];
    byte blue = decodedBuffer[4];

    uint32_t color = strip.Color(strip.gamma8(red), strip.gamma8(green), strip.gamma8(blue));

    strip.setPixelColor(index, color);
    strip.show();
  }
}

void handleWriteAllMessage(const uint8_t *decodedBuffer)
{
  if (currentState == state::Streaming)
  {
    size_t count = (writeAllSize - 1) / 3;
    for (size_t i = 0; i < count; i++)
    {
      size_t p = i * 3;
      byte red = decodedBuffer[p + 1];
      byte green = decodedBuffer[p + 2];
      byte blue = decodedBuffer[p + 3];

      uint32_t color = strip.Color(strip.gamma8(red), strip.gamma8(green), strip.gamma8(blue));
      strip.setPixelColor(i, color);
    }
    strip.show();
  }
}

void handleHandshakeRequestMessage()
{
  currentState = state::Disconnected;

  uint8_t decodedBuffer[] = {handshakeResponse, LED_COUNT};
  serial.send(decodedBuffer, 2);

  currentState = state::Streaming;
  currentBrightness = userDefinedBrightness;
}

void sendMessage(const String &message)
{
  auto bufferSize = message.length() + 1;
  uint8_t buffer[bufferSize] = {print};
  memcpy(buffer + 1, message.c_str(), message.length());
  serial.send(buffer, bufferSize);
}

void loop()
{
  time = millis();
  serial.update();

  if (time - lastMessage > 30000)
  {
    currentState = state::Disconnected;
  }

  if (currentState == state::Disconnected)
  {
    if (time - lastMessage > 60000)
    {
      if (currentBrightness > 0)
      {
        strip.setBrightness(--currentBrightness);
        strip.show();
      }
    }
  }
}