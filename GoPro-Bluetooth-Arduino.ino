#include <Servo.h> // bibliothèque du servomoteur

#include <SoftwareSerial.h>

Servo pan ; // création du servomoteur de rotation en pan
Servo tilt; // création du servomoteur de rotation en tilt

SoftwareSerial mavoieserie(10, 11);
long serialA; // variable de reception de donnée via RX

int anglePan = 90;
int angleTilt = 90;

void setup()
{
  mavoieserie.begin(115200); // initialisation de la connexion série (avec le module bluetooth)
  Serial.begin(9600);
  setupBlueToothConnection(); // démarrage liason série bluetooth cf fonction en bas

  pan.attach(8); // initialisation du moteur pan, il est branché sur la sortie 8
  tilt.attach(9); // initialisation du moteur tilt, il est branché sur la sortie 9

}

void loop() {  
  if (mavoieserie.available()){
      
      serialA = mavoieserie.read();
      Serial.println(serialA);
      if (serialA <100){
        anglePan = serialA*5; // l'angle en pan voulu est les trois premiers chiffres du nombre recu. Si serialA = 120105 alors anglePan = 120
        pan.write(anglePan); // commande du moteur pan
        tilt.write(angleTilt); // commande du moteur tilt
      } 
      else if (serialA == 255){
        if (anglePan <90){
           for (int i = 0; i <= 90-anglePan; i++){
              pan.write(anglePan+i);
              delay(10);
            }
        } else if (anglePan > 90){
           for (int i = 0; i <= anglePan-90; i++){
              pan.write(anglePan-i);
              delay(10);
            }
        }
        if (angleTilt<90){
           for (int i = 0; i <= 90-angleTilt; i++){
              tilt.write(angleTilt+i);
              delay(10);
            }
        } else if (angleTilt > 90){
           for (int i = 0; i <= angleTilt-90; i++){
              tilt.write(angleTilt-i);
              delay(10);
            }
        }
        anglePan = 90;
        angleTilt = 90;
      }
      else if (serialA == 253){
        for (int i = 0; i <= anglePan; i++){
          pan.write(anglePan-i);
          delay(10);
        }
        for (int i = 0; i< 180 ; i++){
          pan.write(i);
          
          tilt.write(90);
          delay(50);
        }
        anglePan = 180;
      }  
      else if (serialA == 254){
         for (int i = 0; i <= angleTilt; i++){
          tilt.write(angleTilt-i);
          delay(10);
        }
        for (int i = 0; i< 180 ; i++){
          tilt.write(i);
          pan.write(90);
          delay(50);
        }
        angleTilt = 180;
      }    
      else {
        angleTilt = (serialA-100)*5; // l'angle en tilt voulu est les trois derniers chiffres du nombre recu. Pour l'exemple précedent, angleTilt = 105
        tilt.write(angleTilt); // commande du moteur tilt
        pan.write(anglePan); // commande du moteur pan
      }
      Serial.println(anglePan);
      Serial.println(angleTilt);
    }
  
}

//void serialEvent(){ // si arduino reçois quelquechose en sur l'entrée RX
//  serialA = mavoieserie.read(); // stocker la valeur reçue dans la variable serialA
//}

void setupBlueToothConnection() // fonction de configuration du module bluetooth
{
  mavoieserie.begin(115200); //vitesse de bluetooth
  mavoieserie.print("\r\n+STBD=115200\r\n"); // fixe la vitesse du bluetooth
  mavoieserie.print("\r\n+STWMOD=0\r\n"); // bluetooth en mode esclave
  mavoieserie.print("\r\n+STNA=Arduino"); // nom de l'appareil
  mavoieserie.print("\r\n+STPIN=0000\r\n");// code pin "0000"
  mavoieserie.print("\r\n+STOAUT=1\r\n"); // permet les appareils de se connecter à l'arduino
  mavoieserie.print("\r\n+STAUTO=0\r\n"); // l'autoconnexion est empêchée
  delay(2000);
  mavoieserie.print("\r\n+INQ=1\r\n"); // rend le mode esclave non modifiable 
  delay(2000); 
  mavoieserie.flush();
}  

