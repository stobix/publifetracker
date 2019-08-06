package stobix.app.lifetracker;

import android.support.v7.app.AppCompatActivity
import android.widget.CheckBox;

import stobix.view.containerview.ContainerView;

/**
 * Created by stobix on 2/2/18.
 */

class BuildVairantSpecificCode {
    companion object {
      @JvmStatic  fun onLoad(c:AppCompatActivity) {
            val v: ContainerView = c.findViewById(R.id.containerView2);

            c.findViewById<CheckBox>(R.id.checkTags).setOnClickListener {b -> v.showIntDescriptions=(b as CheckBox).isChecked}
            c.findViewById<CheckBox>(R.id.checkDescr).setOnClickListener{b -> v.showStringDescriptions=(b as CheckBox).isChecked}
          //((View b) -> v.setShowDescriptions(((CheckBox) b).isChecked()))
            c.findViewById<CheckBox>(R.id.checkRecur).setOnClickListener{b -> v.showContents=(b as CheckBox).isChecked}
          //((View b) -> v.setShowContents(((CheckBox) b).isChecked()))
            c.findViewById<CheckBox>(R.id.checkRecDesc).setOnClickListener{b -> v.showContentDescriptions=(b as CheckBox).isChecked}
          //((View b) -> v.setShowContentDescriptions(((CheckBox) b).isChecked()))
            c.findViewById<CheckBox>(R.id.checkRecDeep).setOnClickListener{b -> v.maxRecurLevel=if((b as CheckBox).isChecked) 2 else 1}
          //((View b) -> v.setRecurLevel(((CheckBox) b).isChecked() ? 2 : 1) )



        }


    }
}
