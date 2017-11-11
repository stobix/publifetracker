package com.example.stobix.myapplication;

import android.provider.BaseColumns;

/**
 * Created by stobix on 11/2/17.
 */

final class Headerfil {

    private Headerfil(){}

    public static class Databasnamn implements BaseColumns{
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_SUGAR;
        public static final String COLUMN_NAME_EXTRA = "extra";

        static {
            COLUMN_NAME_SUGAR = "subtitle";
        }
    }
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " +Databasnamn.TABLE_NAME + " (" +
                    Databasnamn._ID + " INTEGER PRIMARY KEY," +
                    Databasnamn.COLUMN_NAME_DATE + " datetime," +
                    Databasnamn.COLUMN_NAME_SUGAR + " int(3)," +
                    Databasnamn.COLUMN_NAME_EXTRA + "text";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Databasnamn.TABLE_NAME;
}
