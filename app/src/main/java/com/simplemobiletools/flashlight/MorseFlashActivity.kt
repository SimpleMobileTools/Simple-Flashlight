package com.simplemobiletools.flashlight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_morse_flash.*

class MorseFlashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_morse_flash)


        val editText_InputUser = findViewById<EditText>(R.id.editText_User)

        val morseAlphabet = mapOf(
            // les minuscule

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
            // les majuscules
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
            // les nombres
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
        // Lors de l'appuie sur le bouton le texte de l'user est traduit en Morse
        //RESTE A UTILISER LA TRADUCTION DES . et des -, POUR UTILISER LE FLASH

        button_morseFlash.setOnClickListener{
            val text_user = editText_InputUser.text
            System.out.println(text_user)
            val result = text_user.map { if (it == ' ') "/" else morseAlphabet[it] }.joinToString(" ")
            System.out.println(result)
            textView_translation.setText(result)
        }
    }
}
