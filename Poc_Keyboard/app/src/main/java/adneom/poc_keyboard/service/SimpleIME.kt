package adneom.poc_keyboard.service

import adneom.poc_keyboard.R
import android.content.Context
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.*
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v4.content.FileProvider
import android.util.Log
import java.io.File


/**
 * Represents the service whis is called when key of keyboard is taped!
 * Created by gtshilombowanticale on 24-06-17.
 */

class SimpleIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    var kv : KeyboardView? = null
    var keyboard : Keyboard? = null

    var cap : Boolean = false

    //crate our Keaybaord View
    override fun onCreateInputView(): View {
        kv = layoutInflater.inflate(R.layout.layout_keybaord, null) as KeyboardView;
        keyboard = Keyboard(this,R.xml.qwerty)
        (kv as KeyboardView).setKeyboard(keyboard);
        (kv as KeyboardView).setOnKeyboardActionListener(this);
        return kv as KeyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        supportedContentMimeTypes(info)
    }

    override fun swipeRight() {}

    override fun onPress(primaryCode: Int) {}

    override fun onRelease(primaryCode: Int) {}

    override fun swipeLeft() {}

    override fun swipeUp() {}

    override fun swipeDown() {}

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection
        playClick(primaryCode)
        when(primaryCode) {
            Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1,0)
            Keyboard.KEYCODE_SHIFT -> {
                cap = !cap
                keyboard?.setShifted(cap)
                kv?.invalidateAllKeys()
            }
            Keyboard.KEYCODE_DONE -> {
                //ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                //val contentUri : Uri = FileProvider.getUriForFile(this,this.applicationContext.packageName)
            }
            else -> {
                var code : Char = primaryCode.toChar()
                if(Character.isLetter(code) && cap) {
                    code = Character.toUpperCase(code);
                }
                ic.commitText(code.toString(),1)
            }
        }
    }

    override fun onText(text: CharSequence?) {}


    private fun playClick(keyCode : Int) {
        val am : AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when(keyCode) {
            32 -> am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            Keyboard.KEYCODE_DONE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            10 -> am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            Keyboard.KEYCODE_DELETE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
            else -> am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    private fun supportedContentMimeTypes (editorInfo : EditorInfo?) {
        val mimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo)
        for(item in mimeTypes){
            Log.i("Adneom","supported mime is "+item)
        }
    }

    private fun createFile(){
       // var drawable : Drawable = this.getDrawable(R.drawable.dess)
    }

}