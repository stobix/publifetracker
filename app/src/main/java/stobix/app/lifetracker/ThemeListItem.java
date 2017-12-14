package stobix.app.lifetracker;

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
    // TODO Return the three "primary colors" for the theme.
    public int color1(){return 0;}
    public int color2(){return 0;}
    public int color3(){return 0;}

}