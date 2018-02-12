package stobix.view.containerview


import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.test_layout)
    }
}