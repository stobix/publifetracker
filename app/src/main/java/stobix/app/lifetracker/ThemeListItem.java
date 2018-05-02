package stobix.app.lifetracker;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

public class ThemeListItem{
    private String colorThemeName;
    private int r_theme_value,
            mcolor1=Color.BLACK,
            mcolor2 =Color.BLUE,
            mcolor3 =Color.WHITE,
            mtextcolor =Color.BLACK,
            mbackcolor=Color.WHITE;
    private boolean
            isColor1Set=false,
            isColor2Set=false,
            isColor3Set=false,
            isBackColorSet=false,
            isTextColorSet=false;


    public ThemeListItem(String colorThemeName, int r_theme_value){
        this.colorThemeName=colorThemeName;
        this.r_theme_value=r_theme_value;
    }
    public ThemeListItem(
            String colorThemeName,
            int r_theme_value,
            Context ctx,
            int color1, int color2, int color3,
            int text_color, int background_color){
        this(colorThemeName,r_theme_value);
        mcolor1 = ContextCompat.getColor(ctx,color1);
        mcolor2 = ContextCompat.getColor(ctx,color2);
        mcolor3 = ContextCompat.getColor(ctx,color3);
        mtextcolor = ContextCompat.getColor(ctx,text_color);
        mbackcolor= ContextCompat.getColor(ctx,background_color);

    }
    public String toString(){
        return colorThemeName;
    }
    public String getColorThemeName() {return colorThemeName;}
    public int getThemeResourceValue(){
        return r_theme_value;
    }
    // TODO Return the three "primary colors" for the theme.
    public ThemeListItem setColor1(int color){
        mcolor1 = color;
        isColor1Set=true;
        return this;
    }
    public ThemeListItem setColor2(int color){
        mcolor2 = color;
        isColor2Set=true;
        return this;
    }
    public ThemeListItem setColor3(int color){
        mcolor3 = color;
        isColor3Set=true;
        return this;
    }
    public ThemeListItem setTextColor(int color){
        mtextcolor = color;
        isTextColorSet=true;
        return this;
    }
    public ThemeListItem setBackgroundColor(int color){
        mbackcolor = color;
        isBackColorSet=true;
        return this;
    }

    public int getColor1(){return mcolor1;}
    public int getColor2(){return mcolor2;}
    public int getColor3(){return mcolor3;}
    public int getTextColor(){return mtextcolor;}
    public int getBackgroundColor(){return mbackcolor;}

}