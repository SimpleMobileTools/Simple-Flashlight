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

         var _u = 200L
        val MSG = arrayListOf(_u)
        val editText_UserInput = findViewById<EditText>(R.id.editText_UserInput)
        val morseAlphabet = mapOf(
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

        var resultMorseCode = " "

        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]


        btn_morseFlash.setOnClickListener{
            val text_user = editText_UserInput.text

            System.out.println("Saisie Utilisateur ====> "+ text_user)

            resultMorseCode = text_user.map { if (it == ' ') "/" else morseAlphabet[it] }.joinToString(" ")
            textView_translation.setText(resultMorseCode)

            System.out.println("Traduction MORSE ====> "+ resultMorseCode)

            MSG.clear()

            System.out.println("MSG vide ====> " + MSG)

            for (i in resultMorseCode.indices) {
                if (resultMorseCode[i] == '/'){
                    cameraManager.setTorchMode(cameraId, false)
                    System.out.println("flash OFF")
                    MSG.add(_u*7)
                    Thread.sleep(_u*7)
                }
                else if (resultMorseCode[i]==' '){
                    cameraManager.setTorchMode(cameraId, false)
                    System.out.println("flash OFF")
                    if(resultMorseCode[i+1]!='/' && resultMorseCode[i-1]!='/'){
                        Thread.sleep(_u*3)
                        MSG.add(_u*3)
                    }}

                else if (resultMorseCode[i]=='-'){
                    cameraManager.setTorchMode(cameraId, true)
                    System.out.println("flash ON")
                    MSG.add(_u*3)
                    Thread.sleep(_u*3)
                    cameraManager.setTorchMode(cameraId, false)
                    System.out.println("flash Off")
                    if(i+1<resultMorseCode.length){
                        if(resultMorseCode[i+1]!=' '){
                            Thread.sleep(_u)
                            MSG.add(_u)
                        }}}

                else if (resultMorseCode[i]=='.'){
                    cameraManager.setTorchMode(cameraId, true)
                    System.out.println("flash ON")
                    MSG.add(_u)
                    Thread.sleep(_u)
                    cameraManager.setTorchMode(cameraId, false)
                    System.out.println("flash OFF")
                    if(i+1<resultMorseCode.length){
                        if(resultMorseCode[i+1]!=' '){
                            Thread.sleep(_u)
                            MSG.add(_u)
                        }}}
            }
            MSG.removeLast()
            MSG.add(_u*7)
            System.out.println("MESSAGE  FLASH  ====> " + MSG)
        }
    }
}
