package stobix.app.lifetracker

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import kotlinx.android.synthetic.main.theme_picker_item_view.view.*

class ColorEditorActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_color_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        var img = findViewById<ImageView>(R.id.colorEditorPreviewImage);
    }
}