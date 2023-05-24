#include <Arduino_BuiltIn.h>

#include <tacp.h>
#include <thing.h>
#include <mcu_board_adaptation.h>
#include <radio_module_adaptation.h>

#include <OneWire.h>
#include <DallasTemperature.h>

#define TEMPERATURE_OUT_PIN 5

OneWire oneWire(TEMPERATURE_OUT_PIN);
DallasTemperature temperture(&oneWire);

void setup() {
  char modelName[] = "STR-01";
  configureMcuBoard(modelName);
  configureRadioModule();

  // resetThing();
  
  registerThingProtocolsConfigurer(configureThingProtocolsImpl);
  toBeAThing();

  temperture.begin();
}

int8_t processResetThing(Protocol *protocol) {
  resetThing();

#ifdef ENABLE_DEBUG
  Serial.println(F("Thing has reset."));
#endif

  return 0;
}

int8_t acquireCelsiusDegree(Protocol *protocol) {
  temperture.requestTemperatures();
  float celsiusDegree = temperture.getTempCByIndex(0);
  addFloatAttribute(protocol, 0x01, celsiusDegree);
  
  return 0;
}

void configureThingProtocolsImpl() {
  ProtocolName pnResetThing = {0xf8, 0x02, 0x09};
  registerActionProtocol(pnResetThing, processResetThing, false);

  ProtocolName pnAquireCelsiusDegree = {0xf7, 0x03, 0x00};
  registerDataProtocol(pnAquireCelsiusDegree, acquireCelsiusDegree, 2000);
}

void loop() {
  int result = doWorksAThingShouldDo();
  if (result != 0) {
#ifdef ENABLE_DEBUG
    Serial.print(F("Error occurred when the thing does the works it should do. Error number: "));
    Serial.print(result);
    Serial.println(F("."));    
#endif
  }
}
