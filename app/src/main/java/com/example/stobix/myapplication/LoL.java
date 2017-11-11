package com.example.stobix.myapplication;

import java.util.Date;

/**
 * Created by stobix on 9/1/17.
 */

class LoL {

    public LoL(Date date, int blood, String extra){
        this.date=date;
        this.sugar=blood;
        this.extra=extra;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getSugar() {
        return sugar;
    }

    public float getSugarF() {
        return sugar/10f;
    }

    public void setSugar(int sugar) {
        this.sugar = sugar;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    private Date date ;
    private int sugar ;
    private String extra;

}
