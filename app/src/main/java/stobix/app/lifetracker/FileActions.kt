package stobix.app.lifetracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.util.Log
import java.io.*

class FileActions (val activity: Activity, val createHandler: FileCreateHandler, val openHandler: FileOpenHandler){

    constructor(activity: Activity): this(activity,activity as FileCreateHandler,activity as FileOpenHandler)

    fun userOpenFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/json" // Required, apparently.
        startActivityForResult(activity, intent, OPEN_REQUEST, null)
    }

    fun userCreateFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/json" // Required, apparently.
        intent.putExtra(Intent.EXTRA_TITLE,"export.json")
        //intent.putExtra(Intent.EXTRA_TEXT,"export.json")
        startActivityForResult(activity, intent, CREATE_REQUEST, null)
    }

    fun isFileAction(requestCode: Int) = when(requestCode){
        OPEN_REQUEST, CREATE_REQUEST -> true
        else -> false

    }

    private fun systemGetUserOpenedFile(resultCode: Int, resultData: Intent?) : Boolean {
        if(resultCode == Activity.RESULT_OK){
            val uri: Uri = resultData?.data ?: return false
            openHandler.handleFileOpened(uri)
            return true
        } else return false
    }

    private fun systemGetUserCreatedFile(resultCode: Int, resultData: Intent?) : Boolean {
        if(resultCode == Activity.RESULT_OK){
            val uri: Uri = resultData?.data ?: return false
            createHandler.handleFileCreated(uri)
            return true
        } else return false
    }

    fun handleFileAction(requestCode: Int ,resultCode: Int,resultData: Intent?): Boolean =
        when(requestCode){
            OPEN_REQUEST -> {
                systemGetUserOpenedFile(resultCode,resultData)
            }
            CREATE_REQUEST -> {
                systemGetUserCreatedFile(resultCode,resultData)
            }
            else -> false
        }

    interface FileCreateHandler {
        fun handleFileCreated(uri: Uri)
    }

    interface FileOpenHandler {
        fun handleFileOpened(uri: Uri)
    }

    fun readTextFromUri(uri: Uri): String{
        val inputStream = activity.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        do {
            line = reader.readLine()
            Log.d("file","read line: $line")
            if(line!=null)
                stringBuilder.append(line)
        } while (line !=null)
        inputStream.close()
        val result = stringBuilder.toString()
        Log.d("file", "read result: $result")
        return result
    }

    fun putTextInUri(uri: Uri,text: String){
        val pfd: ParcelFileDescriptor? = activity.contentResolver.openFileDescriptor(uri,"w")
        if(pfd == null) {
            Error("couldn't open file!")
        } else {
            //val ops = activity.contentResolver.openOutputStream(uri,"w+")
            val os = FileOutputStream(pfd.fileDescriptor)
            os.write(text.toByteArray())
            os.close()
            pfd.close()
        }
    }

    companion object {
        @JvmField val OPEN_REQUEST: Int = 42
        @JvmField val CREATE_REQUEST: Int = 43
    }
}

