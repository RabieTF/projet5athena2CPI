//FirebaseESP8266.h must be included before ESP8266WiFi.h
#include "FirebaseESP8266.h"  // Install Firebase ESP8266 library
#include <ESP8266WiFi.h>
#include <DHT.h>    // Install DHT11 Library and Adafruit Unified Sensor Library


#define FIREBASE_HOST "https://project101-1b151-default-rtdb.firebaseio.com/"                          // the project name address from firebase id
#define FIREBASE_AUTH "nuKujnbIhXlPkG4hamo76QqfwLPD99H1c0sGK0Av"            // the secret key generated from firebase
#define WIFI_SSID "Shadow"
#define WIFI_PASSWORD "bibi1004"

#define DHTPIN 2    // Connect Data pin of DHT to D2


#define DHTTYPE    DHT11
DHT dht(DHTPIN, DHTTYPE);

//Define FirebaseESP8266 data object
FirebaseData firebaseData;
FirebaseData ledData;

FirebaseJson json;


void setup()
{

  Serial.begin(9600);

  dht.begin();
 
  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

}

void sensorUpdate(){
  
  float h = dht.readHumidity();
  // Read temperature as Celsius (the default)
  float t = dht.readTemperature();
  // Read temperature as Fahrenheit (isFahrenheit = true)
  float f = dht.readTemperature(true);

  // Check if any reads failed 
  if (isnan(h) || isnan(t) || isnan(f)) {
    Serial.println(F("Failed to read from DHT sensor!"));
    return;
  }

  Serial.print(F("Humidity: "));
  Serial.print(h);
  Serial.print(F("%  Temperature: "));
  Serial.print(t);
  Serial.print(F("C  ,"));
  Serial.print(f);
  Serial.println(F("F  "));

  if (Firebase.setFloat(firebaseData, "/Temperature", t))
  {
    Serial.println("PASSED");
    
  }
  else
  {
    Serial.println("FAILED");
    
  }

  if (Firebase.setFloat(firebaseData, "/Humidity", h))
  {
    Serial.println("PASSED");
  }
  else
  {
    Serial.println("FAILED");
    
  }
}
void loop() {
  sensorUpdate();
  delay(7000);
}
