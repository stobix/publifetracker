package stobix.app.lifetracker;

/**
 * Created by JoelE on 2017-12-14.
 */

public class ThemeListItem{
    String colorThemeName;
    int r_theme_value;
    public ThemeListItem(String colorThemeName, int r_theme_value){
        this.colorThemeName=colorThemeName;
        this.r_theme_value=r_theme_value;
    }
    public String toString(){
        return colorThemeName;
    }
    public int themeValue(){
        return r_theme_value;
    }

}