package com.example.stobix.myapplication;

import android.util.Log;

import java.util.Comparator;

/**
 * Created by stobix on 9/4/17.
 */

public class LolComparatorsOmg {
    public static Comparator<LoL> getLolComparator(int n) {
        Log.d("COMP",""+n);
        switch (n) {
            case 0:
                return (a, b) -> a.getDate().compareTo(b.getDate());
                /*
                return new Comparator<LoL>() {
                    @Override
                    public int compare(LoL a, LoL b) {
                        return a.getDate().compareTo(b.getDate());
                    }
                };
                */
            case 1:
                return (a, b) -> a.getSugar() - b.getSugar();
            case 2:
                return (a, b) -> a.getExtra().compareTo(b.getExtra());
            default:
                return null;
        }
    }
}
