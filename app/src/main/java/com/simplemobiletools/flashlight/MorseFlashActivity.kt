package com.simplemobiletools.flashlight

import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_morse_flash.*

class MorseFlashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_morse_flash)

        /*
                  u = 200L
         DEFAULT LENGTH DASH = u * 3     ---> delai 3 pour un -
         DEFAULT DELAY LETTERS = u * 3   ---> delai 3 entre les lettres
         DEFAULT DELAY WORDS = u * 7     ---> delai entre les mots

         u = .(point) après u = pause     u*3= pause entre les lettre après u*3 = - (tiret)

                      .     .     .           -         -         -           .     .     .
    SOS = arrayListOf(u, u, u, u, u, u * 3, u * 3, u, u * 3, u, u * 3, u * 3, u, u, u, u, u, u * 7)
                         p     p      !            p         p          !        p     p     pause entre les mots
                                      V                                 V
                                   pause entre les lettres           pause entre les lettres


      pl = pause entre les lettres
      pm = pause entre les mots
                     .    p    .    p    .    pl   -    p    -    p    -   pl    .    p    .    p    .    pm
  MSG FLASH  ====> [200, 200, 200, 200, 200, 600, 600, 200, 600, 200, 600, 600, 200, 200, 200, 200, 200, 1400]
         */

         var _u = 200L //  DEFAULT LENGTH ONE UNIT MILLISECONDS
        val MSG = arrayListOf(_u) // Futur message flashing morse code

        val editText_UserInput = findViewById<EditText>(R.id.editText_UserInput)

        // for morse translator . and -
        val morseAlphabet = mapOf(
            // letters lowercase
            'a' to ".-",
            'b' to "-...",
            'c' to "-.-.",
            'd' to "-..",
            'e' to ".",
            'f' to "..-.",
            'g' to "--.",
            'h' to "....",
            'i' to "..",
            'j' to ".---",
            'k' to "-.-",
            'l' to ".-..",
            'm' to "--",
            'n' to "-.",
            'o' to "---",
            'p' to ".--.",
            'q' to "--.-",
            'r' to ".-.",
            's' to "...",
            't' to "-",
            'u' to "..-",
            'v' to "...-",
            'w' to ".--",
            'x' to "-..-",
            'y' to "-.--",
            'z' to "--..",
            // letters uppercase
            'A' to ".-",
            'B' to "-...",
            'C' to "-.-.",
            'D' to "-..",
            'E' to ".",
            'F' to "..-.",
            'G' to "--.",
            'H' to "....",
            'I' to "..",
            'J' to ".---",
            'K' to "-.-",
            'L' to ".-..",
            'M' to "--",
            'N' to "-.",
            'O' to "---",
            'P' to ".--.",
            'Q' to "--.-",
            'R' to ".-.",
            'S' to "...",
            'T' to "-",
            'U' to "..-",
            'V' to "...-",
            'W' to ".--",
            'X' to "-..-",
            'Y' to "-.--",
            'Z' to "--..",
            // numbers
            '1' to ".----",
            '2' to "..---",
            '3' to "...--",
            '4' to "....-",
            '5' to ".....",
            '6' to "-....",
            '7' to "--...",
            '8' to "---..",
            '9' to "----.",
            '0' to "-----"
        )

        var resultMorseCode = " " // futur result of translation

        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]


        btn_morseFlash.setOnClickListener{
            val text_user = editText_UserInput.text //get text of input

            System.out.println("Saisie Utilisateur ====> "+ text_user)

            resultMorseCode = text_user.map { if (it == ' ') "/" else morseAlphabet[it] }.joinToString(" ")
            textView_translation.setText(resultMorseCode) // set the result of translation on TextView

            System.out.println("Traduction MORSE ====> "+ resultMorseCode)

            MSG.clear()

            System.out.println("MSG vide ====> " + MSG)

            resultMorseCode.forEach {
                if (it == '/'){
                    MSG.add(_u*7) // DEFAULT DELAY WORDS = u * 7
                    cameraManager.setTorchMode(cameraId, false) // flash OFF
                    System.out.println("flash OFF")
                    Thread.sleep(_u*7)
                }

                else if (it==' '){
                    MSG.removeLast()
                    cameraManager.setTorchMode(cameraId, false) // flash OFF
                    Thread.sleep(_u*3)
                    MSG.add(_u*3) // DEFAULT DELAY LETTERS = u * 3
                }

                else if (it=='-'){
                    cameraManager.setTorchMode(cameraId, true) // flash ON
                    System.out.println("flash ON")
                    MSG.add(_u*3) // DEFAULT LENGTH DASH = u * 3
                    Thread.sleep(_u*3)
                    MSG.add(_u)
                    cameraManager.setTorchMode(cameraId, false) // flash OFF
                    System.out.println("flash OFF")
                    Thread.sleep(_u*3)
                }

                else if (it=='.'){
                    cameraManager.setTorchMode(cameraId, true) // flash ON
                    System.out.println("flash ON")
                    MSG.add(_u)
                    Thread.sleep(_u)
                    MSG.add(_u)
                    cameraManager.setTorchMode(cameraId, false) // flash OFF
                    System.out.println("flash OFF")
                    Thread.sleep(_u)
                }
            }
            MSG.removeLast()
            MSG.add(_u*7)
            System.out.println("MESSAGE  FLASH  ====> " + MSG)
        }
    }
}

//Je pense que c'est correct , mais reste à etre améliorer le flash marche sur mon téléphone
//on supprimera les commentaires par la suite
//I think it's correct , but still have to be improved the flash works correctly on my phone
//comments will be deleted later.

/*    OUTPUT CONSOLE ==> FOR SOS MESSAGE
I/System.out: Saisie Utilisateur ====> sos
I/System.out: Traduction MORSE ====> ... --- ...
I/System.out: MSG vide ====> []
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: MESSAGE  FLASH  ====> [200, 200, 200, 200, 200, 600, 600, 200, 600, 200, 600, 600, 200, 200, 200, 200, 200, 1400]


Je pense que c'est correct , mais reste à etre améliorer le flash marche sur mon téléphone
I think it's correct , but still have to be improved the flash works correctly on my phone

 */

/*    OUTPUT CONSOLE FOR MESSAGE ==> test sos (same TEST SOS)

I/System.out: Saisie Utilisateur ====> test sos
I/System.out: Traduction MORSE ====> - . ... - / ... --- ...
I/System.out: MSG vide ====> []
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: flash ON
I/System.out: flash OFF
I/System.out: MESSAGE  FLASH  ====> [600, 600, 200, 600, 200, 200, 200, 200, 200, 600, 600, 600, 600, 200, 200, 200, 200, 200, 600, 600, 200, 600, 200, 600, 600, 200, 200, 200, 200, 200, 1400]

 */
