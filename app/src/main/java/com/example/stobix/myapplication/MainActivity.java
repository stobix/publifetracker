package com.example.stobix.myapplication;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.util.Log.d;

    public class MainActivity extends AppCompatActivity
            implements DatePickerFragment.DatePickerHandler, TimePickerFragment.TimePickerHandler {

        // Made static (i.e. no outer scope references) to prevent memory issues, since lint complained about the anonymous class instance.
        // See https://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
        private static class DBHandler extends Handler {
            final Context context;
            final SortableSugarEntryTableView tableView;

            DBHandler(Context outer_context, SortableSugarEntryTableView view) {
                context = outer_context;
                tableView = view;
            }

            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                ArrayList<SugarEntry> arrayEntries = b.getParcelableArrayList("entries");
                if(arrayEntries!=null)
                    tableView.setDataAdapter(new SugarEntryTableDataAdapter(context, arrayEntries));
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(view ->
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show());


            Handler db_data_handler = new DBHandler(this, findViewById(R.id.tableView));

            /*
            Runnable initiateDB = new Runnable(){
                public void run() {

                }
            }
            */
            Runnable initiateDB = () -> {
                SugarEntryDatabase db =
                        Room.databaseBuilder(
                                getApplicationContext(),
                                SugarEntryDatabase.class,
                                "sugarApp").build();

                SugarEntryDao dao = db.userDao();

                List<SugarEntry> entries = dao.getAll();
                // TODO remove this line at some point, when entry adding functionality is present
                // dao.insert(new SugarEntry(entries.size() + 1, rndDat(), rndSgr(), rndStr()));
                // entries = dao.getAll();

                Message msg = db_data_handler.obtainMessage();
                Bundle bundle = new Bundle();
                ArrayList<SugarEntry> arrayEntries = new ArrayList<>(entries);
                bundle.putParcelableArrayList("entries", arrayEntries);
                msg.setData(bundle);
                db_data_handler.sendMessage(msg);
            };

            Thread dbInitThread = new Thread(initiateDB);
            dbInitThread.start();

        }

        // TODO Add a method for adding an entry to the database

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        public void showDatePicker(View view) {
            new DatePickerFragment().show(getSupportFragmentManager(), "datePicker");
        }

        public void showTimePicker(View view) {
            new TimePickerFragment().show(getSupportFragmentManager(), "timePicker");
        }


        @Override
        public void handleDate(int year, int month, int day) {
            EditText t = findViewById(R.id.editText2);
            if (t != null) {
                String s = year + " " + month + " " + day;
                t.setText(s, TextView.BufferType.NORMAL);
            }
        }

        @Override
        public void handleTime(int hour, int minute) {
            EditText t = findViewById(R.id.editText2);
            if (t != null) {
                String s = hour + " " + minute;
                t.setText(s, TextView.BufferType.NORMAL);
            }

        }
    }
