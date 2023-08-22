#include <Arduino_BuiltIn.h>

#include <thing.h>
#include <tuxp.h>
#include <debug.h>
#include <mcu_board_adaptation.h>
#include <radio_module_adaptation.h>
#include <arduino_unique_id_generator.h>

// For my Arduino Micro board.
/*#define LED_PIN 5
#define SWITCH_RED_BUTTON_PIN 2
#define SWITCH_YELLOW_BUTTON_PIN 3
#define SWITCH_GREEN_BUTTON_PIN 4*/

// For my Arduino UNO R3 board.
#define LED_PIN 7
#define SWITCH_RED_BUTTON_PIN 8
#define SWITCH_YELLOW_BUTTON_PIN 9
#define SWITCH_GREEN_BUTTON_PIN 10

/*enum SwitchState {
  RED = 0,
  YELLOW,
  GREEN
};
enum SwitchState lastSwitchState;*/

#define MODEL_NAME "SL-02"

#define SWITCH_STATE_RED 0
#define SWITCH_STATE_YELLOW 1
#define SWITCH_STATE_GREEN 2

uint8_t lastSwitchState;

void setup() {
  configureMcuBoard(MODEL_NAME);
  configureRadioModule();
  
  registerThingIdLoader(loadThingId);
  registerRegistrationCodeLoader(loadRegistrationCode);

  registerThingProtocolsConfigurer(configureThingProtocolsImpl);
  
  // resetThing();
  pinMode(LED_PIN, OUTPUT);
  configureSwitchModule();
  
  toBeAThing();
}

char *loadThingId() {
  return generateThingIdUsingUniqueIdLibrary(MODEL_NAME);
}

char *loadRegistrationCode() {
  return "abcdefghigkl";
}

bool isRedSwitchPressed() {
  int redSwitchState = digitalRead(SWITCH_RED_BUTTON_PIN);
  return redSwitchState == LOW;
}

bool isYellowSwitchPressed() {
  int yellowSwitchState = digitalRead(SWITCH_YELLOW_BUTTON_PIN);
  return yellowSwitchState == LOW;
}

bool isGreenSwitchPressed() {
  int greenSwitchState = digitalRead(SWITCH_GREEN_BUTTON_PIN);
  return greenSwitchState == LOW;
}

void configureSwitchModule() {
  pinMode(SWITCH_RED_BUTTON_PIN, INPUT_PULLUP);
  pinMode(SWITCH_YELLOW_BUTTON_PIN, INPUT_PULLUP);
  pinMode(SWITCH_GREEN_BUTTON_PIN, INPUT_PULLUP);

  lastSwitchState = SWITCH_STATE_RED;
}

bool checkSwitchState() {
  if (lastSwitchState != SWITCH_STATE_RED && isRedSwitchPressed()) {
    printSwitchStateChange(lastSwitchState, SWITCH_STATE_RED);

    lastSwitchState = SWITCH_STATE_RED;
    return true;
  } else if (lastSwitchState != SWITCH_STATE_YELLOW && isYellowSwitchPressed()) {
    printSwitchStateChange(lastSwitchState, SWITCH_STATE_YELLOW);

    lastSwitchState = SWITCH_STATE_YELLOW;
    return true;
  } else if (lastSwitchState != SWITCH_STATE_GREEN && isGreenSwitchPressed()) {
    printSwitchStateChange(lastSwitchState, SWITCH_STATE_GREEN);

    if (lastSwitchState == SWITCH_STATE_RED)
      fireSwitchStateChangedEvent(SWITCH_STATE_RED, SWITCH_STATE_GREEN);

    lastSwitchState = SWITCH_STATE_GREEN;
    return true;
  } else {
    return false;
  }
}

void fireSwitchStateChangedEvent(uint8_t previous, uint8_t current) {
#ifdef ENABLE_DEBUG
  Serial.println("Enter fireSwitchStateChangedEvent.");
#endif

  ProtocolName pnSwitchStateChanged = {{0xf7, 0x01}, 0x04};
  Protocol event = createProtocol(pnSwitchStateChanged);
  addIntAttribute(&event, 0x05, previous);
  addIntAttribute(&event, 0x06, current);

  TinyId requestId;
  int result = makeTinyId(getLanId(), REQUEST, millis(), requestId);
  if (result != 0) {
#ifdef ENABLE_DEBUG
    Serial.println(F("Error occurred in makingTinyId(). Error number: "));
    Serial.print(result);
    Serial.println(F("."));
#endif
  }

  result = notify(requestId, &event);
  if (result != 0) {
#ifdef ENABLE_DEBUG
    Serial.println(F("Error occurred in notify(). Error number: "));
    Serial.print(result);
    Serial.println(F("."));
#endif
  }
  releaseProtocol(&event);
}

void printSwitchStateChange(uint8_t last, uint8_t current) {
#ifdef ENABLE_DEBUG
  Serial.print("Swtich state changed from ");
  Serial.print(last);
  Serial.print(" to ");
  Serial.print(current);
  Serial.println(".");
#endif
}

void turnLedOn() {
  digitalWrite(LED_PIN, HIGH);
}

void turnLedOff() {
  digitalWrite(LED_PIN, LOW);
}

void flashLed() {
  digitalWrite(LED_PIN, HIGH);
  delay(200);
  digitalWrite(LED_PIN, LOW);
}

int8_t processFlash(Protocol *protocol) {
  if (lastSwitchState != SWITCH_STATE_YELLOW)
    return -1;

  int repeat;
  if (!getIntAttributeValue(protocol, 0x01, &repeat)) {
    repeat = 1;
  }

  if (repeat <= 0 || repeat > 8)
    return -2;

  for (int i = 0; i < repeat; i++) {
    flashLed();
    delay(500);
  }

  return 0;
}

int8_t processTurnOn(Protocol *protocol) {
  if (lastSwitchState != SWITCH_STATE_YELLOW)
    return -1;
    
  turnLedOn();
  return 0;
}

int8_t processTurnOff(Protocol *protocol) {
  if (lastSwitchState != SWITCH_STATE_YELLOW)
    return -1;
  
  turnLedOff();
  return 0;
}

int8_t processResetThing(Protocol *protocol) {
  resetThing();

#ifdef ENABLE_DEBUG
  Serial.println(F("Thing has reset."));
#endif

  return 0;
}

void configureThingProtocolsImpl() {
  ProtocolName pnResetThing = {0xf8, 0x02, 0x09};
  registerActionProtocol(pnResetThing, processResetThing, false);

  ProtocolName pnFlash = {0xf7, 0x01, 0x00};
  registerActionProtocol(pnFlash, processFlash, false);

  ProtocolName pnTurnOn = {0xf7, 0x01, 0x02};
  registerActionProtocol(pnTurnOn, processTurnOn, false);

  ProtocolName pnTurnOff = {0xf7, 0x01, 0x03};
  registerActionProtocol(pnTurnOff, processTurnOff, false);
}

void controlLedByLastSwitchState() {
  switch(lastSwitchState) {
    case SWITCH_STATE_RED:
      turnLedOff();
      break;
    case SWITCH_STATE_YELLOW:
      turnLedOff();
      break;
    case SWITCH_STATE_GREEN:
      turnLedOn();
      break;
    default:
      break;
  }
}

void loop() {
  if (checkSwitchState()) {
    controlLedByLastSwitchState();
  }

  int result = doWorksAThingShouldDo();
  if (result != 0) {
#ifdef ENABLE_DEBUG
    Serial.print(F("Error occurred when the thing does the works it should do. Error number: "));
    Serial.print(result);
    Serial.println(F("."));    
#endif
  }
}
