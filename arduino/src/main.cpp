// NEOPIXEL BEST PRACTICES for most reliable operation:
// - Add 1000 uF CAPACITOR between NeoPixel strip's + and - connections.
// - MINIMIZE WIRING LENGTH between microcontroller board and first pixel.
// - NeoPixel strip's DATA-IN should pass through a 300-500 OHM RESISTOR.
// - AVOID connecting NeoPixels on a LIVE CIRCUIT. If you must, ALWAYS
//   connect GROUND (-) first, then +, then data.
// - When using a 3.3V microcontroller with a 5V-powered NeoPixel strip,
//   a LOGIC-LEVEL CONVERTER on the data line is STRONGLY RECOMMENDED.
// (Skipping these may work OK on your workbench but can fail in the field)
#include "main.h"

Adafruit_NeoPixel strip(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);

void setup()
{
  Serial.begin(115200);
  Serial.setTimeout(100);

  strip.begin(); // INITIALIZE NeoPixel strip object (REQUIRED)
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

size_t encode(const uint8_t *decodedBuffer,
              size_t size,
              uint8_t *encodedBuffer)
{
  size_t read_index = 0;
  size_t write_index = 1;
  size_t code_index = 0;
  uint8_t code = 1;

  while (read_index < size)
  {
    if (decodedBuffer[read_index] == 0)
    {
      encodedBuffer[code_index] = code;
      code = 1;
      code_index = write_index++;
      read_index++;
    }
    else
    {
      encodedBuffer[write_index++] = decodedBuffer[read_index++];
      code++;

      if (code == 0xFF)
      {
        encodedBuffer[code_index] = code;
        code = 1;
        code_index = write_index++;
      }
    }
  }

  encodedBuffer[code_index] = code;

  return write_index;
}

size_t decode(const uint8_t *encodedBuffer,
              size_t size,
              uint8_t *decodedBuffer)
{
  if (size == 0)
    return 0;

  size_t read_index = 0;
  size_t write_index = 0;
  uint8_t code = 0;
  uint8_t i = 0;

  while (read_index < size)
  {
    code = encodedBuffer[read_index];

    if (read_index + code > size && code != 1)
    {
      return 0;
    }

    read_index++;

    for (i = 1; i < code; i++)
    {
      decodedBuffer[write_index++] = encodedBuffer[read_index++];
    }

    if (code != 0xFF && read_index != size)
    {
      decodedBuffer[write_index++] = '\0';
    }
  }

  return write_index;
}

static size_t getEncodedBufferSize(size_t unencodedBufferSize)
{
  return unencodedBufferSize + unencodedBufferSize / 254 + 1;
}

void handleSetBrightnessMessage(uint8_t decodedBuffer[])
{
  byte brightness = decodedBuffer[1];

  strip.setBrightness(brightness);
  strip.show();
}

void handleWriteLEDMessage(uint8_t decodedBuffer[])
{
  byte index = decodedBuffer[1];
  byte red = decodedBuffer[2];
  byte green = decodedBuffer[3];
  byte blue = decodedBuffer[4];

  uint32_t color = strip.Color(strip.gamma8(red), strip.gamma8(green), strip.gamma8(blue));

  strip.setPixelColor(index, color);
  strip.show();
}

void handleWriteAllMessage(uint8_t decodedBuffer[])
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

void handleHandshakeRequestMessage()
{
  uint8_t decodedBuffer[] = {handshakeResponse};
  size_t decodedBufferSize = sizeof(decodedBuffer) / sizeof(uint8_t);

  uint8_t encodedBuffer[getEncodedBufferSize(decodedBufferSize)];
  encode(decodedBuffer, decodedBufferSize, encodedBuffer);

  Serial.write(encodedBuffer, sizeof(encodedBuffer) / sizeof(uint8_t));
}

unsigned long time;
unsigned long lastPrint = 0;

void sendMessage(uint8_t *message, size_t size)
{
  size_t encodedSize = getEncodedBufferSize(size);
  uint8_t encodedBuffer[encodedSize];
  encode(message, size, encodedBuffer);
  Serial.write(encodedBuffer, encodedSize);
  Serial.write(0u);
}

void sendMessage(uint8_t *message)
{
  sendMessage(message, strlen((char *)message));
}

void sendMessagef(const char format[], ...)
{
  char buffer[50];
  va_list ap;
  va_start(ap, format);
  vsprintf(buffer, format, ap);
  va_end(ap);
  sendMessage((uint8_t *)buffer);
}

void loop()
{
  time = millis();
  if (time - lastPrint >= 1000)
  {
    // sendMessage((uint8_t *) "Ping", 4);
    lastPrint = time;
  }
  if (Serial.available())
  {
    uint8_t encodedBuffer[BUFFER_SIZE];
    size_t encodedSize = 0;
    while (encodedSize < BUFFER_SIZE)
    {
      if (Serial.available())
      {
        uint8_t b = Serial.read();
        if (b == 0)
          break;
        encodedBuffer[encodedSize++] = b;
      }
    }

    // Serial.write(encodedBuffer, encodedSize);
    // Serial.write(0u);

    uint8_t decodedBuffer[encodedSize];
    size_t decodedSize = decode(encodedBuffer, encodedSize, decodedBuffer);

    sendMessagef("Encoded size: %d", encodedSize);
    sendMessagef("Decoded size: %d", decodedSize);

    uint8_t header = decodedBuffer[0];
    if (header == setBrightness && decodedSize == setBrightnessSize)
    {
      handleSetBrightnessMessage(decodedBuffer);
    } 
    else if (header == writeLED && decodedSize == writeLEDSize)
    {
      handleWriteLEDMessage(decodedBuffer);
    }
    else if (header == writeAll && decodedSize == writeAllSize)
    {
      handleWriteAllMessage(decodedBuffer);
    }
    else if (header == handshakeRequest && decodedSize == handshakeRequestSize)
    {
      handleHandshakeRequestMessage();
    }
  }
}