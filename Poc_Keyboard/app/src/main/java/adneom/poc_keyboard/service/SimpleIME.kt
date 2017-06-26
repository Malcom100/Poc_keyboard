package adneom.poc_keyboard.service

import adneom.poc_keyboard.R
import android.content.ClipDescription
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
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import android.content.ContentValues.TAG
import android.content.Intent
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.os.Build
import android.R.attr.mimeType
import android.R.attr.description
import android.support.v13.view.inputmethod.InputContentInfoCompat






/**
 * Represents the service whis is called when key of keyboard is taped!
 * Created by gtshilombowanticale on 24-06-17.
 */

class SimpleIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    var kv : KeyboardView? = null
    var keyboard : Keyboard? = null

    var cap : Boolean = false

    val nameFile : String = "image.png"

    var fileOutput : File? = null

    var editorInfo : EditorInfo? = null

    var myMimes : Array<String>? = null

    override fun onCreate() {
        super.onCreate()

        createFile()
    }

    //create our Keaybaord View
    override fun onCreateInputView(): View {
        kv = layoutInflater.inflate(R.layout.layout_keybaord, null) as KeyboardView;
        keyboard = Keyboard(this,R.xml.qwerty)
        (kv as KeyboardView).setKeyboard(keyboard);
        (kv as KeyboardView).setOnKeyboardActionListener(this);
        return kv as KeyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        editorInfo = info
        supportedContentMimeTypes(editorInfo)
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
                val contentUri : Uri = FileProvider.getUriForFile(this,this.applicationContext.packageName,fileOutput)

                // As you as an IME author are most likely to have to implement your own content provider
                // to support CommitContent API, it is important to have a clear spec about what
                // applications are going to be allowed to access the content that your are going to share.
                val flag: Int
                if (Build.VERSION.SDK_INT >= 25) {
                    // On API 25 and later devices, as an analogy of Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    // you can specify InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION to give
                    // a temporary read access to the recipient application without exporting your content
                    // provider.
                    flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
                } else {
                    // On API 24 and prior devices, we cannot rely on
                    // InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION. You as an IME author
                    // need to decide what access control is needed (or not needed) for content URIs that
                    // you are going to expose. This sample uses Context.grantUriPermission(), but you can
                    // implement your own mechanism that satisfies your own requirements.
                    flag = 0
                    try {
                        // TODO: Use revokeUriPermission to revoke as needed.
                        grantUriPermission(
                                editorInfo!!.packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (e: Exception) {
                        Log.e(TAG, "grantUriPermission failed packageName=" + editorInfo!!.packageName
                                + " contentUri=" + contentUri, e)
                    }

                }

                val inputContentInfoCompat = InputContentInfoCompat(
                        contentUri,
                        ClipDescription("test image", arrayOf<String>("image/png")),
                        null /* linkUrl */)

                InputConnectionCompat.commitContent(getCurrentInputConnection(), getCurrentInputEditorInfo(), inputContentInfoCompat, flag, null)
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
        fileOutput = File (filesDir,nameFile)
        var resourceReader : InputStream =  this.resources.openRawResource(R.drawable.dessert_android)
        var dataWriter : OutputStream = FileOutputStream(fileOutput)

        var buf : ByteArray = ByteArray(1024)
        var len : Int = resourceReader.read(buf)
        while ( len > 0) {
            dataWriter.write(buf,0,len)
            len = resourceReader.read(buf)
        }
        dataWriter.flush()
        dataWriter.close()
        //Log.i("Adneom", fileOutput!!.name.plus(fileOutput!!.path))
    }

}