package stobix.app.lifetracker;

public class ThemeListItem{
    private String colorThemeName;
    private int themeResourceValue;

    ThemeListItem(String colorThemeName, int themeResourceValue){
        this.colorThemeName=colorThemeName;
        this.themeResourceValue = themeResourceValue;
    }

    public String toString(){ return colorThemeName; }
    public String getColorThemeName() {return colorThemeName;}
    public int getThemeResourceValue(){ return themeResourceValue; }
}