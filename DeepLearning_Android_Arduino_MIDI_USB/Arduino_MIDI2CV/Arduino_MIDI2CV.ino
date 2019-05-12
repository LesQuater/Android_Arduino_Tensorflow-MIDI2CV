#include <LiquidCrystal_I2C.h>
#include <Wire.h> 
#include <SoftwareSerial.h>

char i = '0';
unsigned char x;
String Channel ="";
String Note ="";
String Velocity ="";
boolean NoteON = false;
boolean NoteOFF = false;
boolean Attack = false;
boolean Decay = false;
boolean ControlChang = false;
boolean DS,C,S,T = false;
boolean GENERATE = false;
boolean L1 = false;

boolean Polyfonic = false;
#define kBTPinRx        10
#define kBTPinTx        11
boolean start = true;
boolean BL_start = true;
boolean USB_start = true;

String Attack_Value = "";
String Decay_Value = "";

LiquidCrystal_I2C lcd(0x27, 20, 4);
SoftwareSerial _btSerial(kBTPinRx, kBTPinTx);
String _btBuffer = "";
String USBBuffer = "";
String msg = "";

void setup() {
  // put your setup code here, to run once:
  _btSerial.begin(9600);
  while (!_btSerial) {;}
  Serial.begin(9600);
  while (!Serial) {;}
  lcd.init();
}

void loop() {

  
  if(_btSerial.available()){
    if(BL_start){
    start = false;
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("Bluetooth message");
    BL_start = false;
    }
    while (_btSerial.available() > 0)
      {
          char letter = (char)_btSerial.read();
          if (letter != '\r' && letter != '\n')
          {
              _btBuffer += String(letter);
          }
          else if (letter == '\n')
          {
              lcd.setCursor(0,1);
              lcd.print(_btBuffer);
              _btBuffer="";

              _btSerial.println("Bien reçu !");
          }
      }
  }

  
  else if(Serial.available()){
    
    if(USB_start){
    start = false;
    lcd.clear();
    lcd.setCursor(3,2);
    lcd.print("USB message");
    USB_start = false;
    delay(600);
    lcd.clear();
    }
    
      
      x = Serial.read();
      if(x =="generate" || GENERATE){
        GENERATE = true;
        lcd.clear();
        lcdFixGen();
        if(x=="play"){
          L1 = true;
          }
        if(L1){
          lcd.setCursor(0,2);
          lcd.print(x);
          L1 = false;
          }
        if(x == "stop"){
          GENERATE = false;
          }
      }
      else if(x != '.' && x != "generate")
      {
        lcd.clear();
        lcdFix();
        //char letter = (char)Serial.read();
        //msg = msg + ";"+ String(letter);
        //lcd.print(msg);
        //lcd.noAutoscroll();
        
       switch(i){
        case '0': // Type de message
          switch(x){
            case 0x90: // Note On
              NoteON = true;
            break;
            case 0x80: // Note Off
              NoteOFF = true;
            break;
            case 0xA0: // Polyfonic aftertouch
              break;
            case 0xD0: // Channel Pressure
            break;
            case 0xC0: // Program change 
            break;
            case 0xB0: // Control change
              ControlChang = true;
            break;
            case 0xF0: // Pitch Bending
            break;
            case 0xAA:
              DS =true;
            break;
            case 0xBB:
              C =true;
            break;
            case 0xCC:
              S =true;
            break;
            case 0xDD:
              T =true;
            break;
            default:
            break;
            }
            if(NoteON || NoteOFF ||ControlChang || DS || C || S || T){
            i = '1';
            }
        break;
        case '1': // Channel
            Channel = x;
            i = '2';
        break;
        case '2': // 
            i = '3';
            if(NoteON || NoteOFF){
              Note = x;
              }
             else if(ControlChang){
                 if(x == 0x49){
                    Attack = true;
                  }
                 else if(x == 0x4B){
                  Decay = true;
                  }
                }
        break;
        case '3':
            i = '4';
            if(NoteON || NoteOFF){
              Velocity = x;
              
              }
              else if(Attack){
                Attack_Value = x;

                }
              else if(Decay){
                Decay_Value = x;
                }
            
        break;
        case '4':
          i = '0';
          NoteON = false;
          NoteOFF = false;
          Attack = false;
          Decay = false;
          ControlChang = false;
          T,DS,S,C =false;
        break;
        }
  }
  else if(x == '.'&& x != "generate"){
      i = '0';
      NoteON = false;
      NoteOFF = false;
      Attack = false;
      Decay = false;
      ControlChang = false;
      
  }
  }
  
  else if (start){
  lcd.backlight();
  lcd.setCursor(3,0);
  lcd.print(" Not Connected !");
  }
  
}


void lcdFixGen(){
  lcd.setCursor(2,0);
  lcd.print("MUSIC GENERATION");
  }

void lcdFix(){

  lcd.setCursor(0,2);
  lcd.print("Decay:");
  lcd.setCursor(10,2);
  lcd.print("Attack:");
  lcd.setCursor(0,3);
  lcd.print("Wave:");
  lcd.setCursor(15,1);
  lcd.print("V:"+Velocity);
  lcd.setCursor(18,1);
  lcd.print(Attack_Value);
  lcd.setCursor(6,2);
  lcd.print(Decay_Value);
  lcd.setCursor(7,1);
  lcd.print(Note);
  if(NoteON = true){
              lcd.setCursor(0,1);
              lcd.print("N_ON:");
  }
  if(NoteOFF == true){
              lcd.setCursor(0,1);
              lcd.print("N_OFF:");
  }
  if(!NoteOFF||!NoteON){
              lcd.setCursor(0,1);
              lcd.print("Null:");
    }


  if(DS){
              lcd.setCursor(6,3);
              lcd.print("Dent-Scie");
  }
  else if(S){
              lcd.setCursor(6,3);
              lcd.print("Sinus");
  }
 else if(C){
              lcd.setCursor(6,3);
              lcd.print("Carré");
    }
 else if(T){
              lcd.setCursor(6,3);
              lcd.print("Triangle");
  }
  else{
              lcd.setCursor(6,3);
              lcd.print("");
              
    }
}
