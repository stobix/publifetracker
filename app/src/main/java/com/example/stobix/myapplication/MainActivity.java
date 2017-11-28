package com.example.stobix.myapplication;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.util.Log.d;

    public class MainActivity extends AppCompatActivity
            implements
            DatePickerFragment.DatePickerHandler,
            TimePickerFragment.TimePickerHandler,
            SugarEntryCreationActivity.OnSugarEntryEnteredHandler
    {

        Random random = new Random();

        private String rndStr() {
            byte[] foo=new byte[30];
            random.nextBytes(foo);
            return String.format("%s", foo);
        }

        private int rndSgr() {
            return random.nextInt(300)+10;
        }

        @SuppressWarnings("deprecation")
        private Date rndDat() {
            Date d = new Date();
            d.setMinutes(random.nextInt(60));
            d.setHours(random.nextInt(24));
            d.setMonth(random.nextInt(12));
            d.setDate(random.nextInt(31));
            d.setYear(random.nextInt(2)+115);
            return d;
        }

        protected int sugarIndex ;

        SugarEntry createNewTempEntry(){
            d("TempEntry","Create");
            SugarEntry s=new SugarEntry();
            s.setUid(sugarIndex);
            return s;
        }

        void submitSugarEntry(SugarEntry s){
            if(s.getEpochTimestamp()!=0L){
                if(s.getUid() != 0){
                    d("TempEntry","Submit");
                    // when successful:
                    // sugarIndex++;
                    // s.setUid(sugarIndex) or createNewTempEntry()
                }
            }
        }

        public void setSugarIndex(int sugarIndex) {
            d("TempEntry","Setting index to "+sugarIndex);
            this.sugarIndex = sugarIndex;
        }

        public int getSugarIndex(){ return sugarIndex; }

        public void showEnterer() {
            d("SugarEntry show","weeeee");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            SugarEntryCreationActivity newFragment = SugarEntryCreationActivity.Companion.newInstance(nextUID);
            newFragment.show(ft, "dialog");

        }

        @Override
        public void onSugarEntryEntered(@NotNull SugarEntry s) {
            nextUID++;
            Handler db_data_handler = new InsertHandler(this, findViewById(R.id.tableView));
            (new Thread(
                    () -> {
                        dao.insert(s);

                        Message msg = db_data_handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("entry", s);
                        msg.setData(bundle);
                        db_data_handler.sendMessage(msg);
                    }
            ) ).start();
        }


        private static class InsertHandler extends Handler {
            final MainActivity context;
            final SortableSugarEntryTableView tableView;

            InsertHandler(MainActivity outer_context, SortableSugarEntryTableView view) {
                context = outer_context;
                tableView = view;
            }
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                SugarEntry s = b.getParcelable("entry");
                tableView.getDataAdapter().add(s);
            }
        }

        // Made static (i.e. no outer scope references) to prevent memory issues, since lint complained about the anonymous class instance.
        // See https://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
        private static class UpdateHandler extends Handler {
            final MainActivity context;
            final SortableSugarEntryTableView tableView;

            UpdateHandler(MainActivity outer_context, SortableSugarEntryTableView view) {
                context = outer_context;
                tableView = view;
            }


            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                ArrayList<SugarEntry> arrayEntries = b.getParcelableArrayList("entries");
                context.setSugarIndex(arrayEntries.size());
                if(arrayEntries!=null)
                    tableView.setDataAdapter(new SugarEntryTableDataAdapter(context, arrayEntries));
            }
        }

        private SugarEntryDao dao;
        private int nextUID;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(view ->
                    showEnterer()
                    //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //        .setAction("Action", null).show()
            );


            Handler db_data_handler = new UpdateHandler(this, findViewById(R.id.tableView));

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

                dao = db.userDao();

                List<SugarEntry> entries = dao.getAll();
                // TODO remove this line at some point, when entry adding functionality is present
                dao.insert(new SugarEntry(entries.size() + 1, rndDat(), rndSgr(), rndStr()));
                entries = dao.getAll();
                d("LOL","Entries"+entries.size());
                nextUID=entries.size()+1;

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


        /*
           TODO Have something like a "class global" state/handler that receives a date or time to be inserted into the database after the user submits all fields.
            Don't reset the date thing after submission.
         */

        @Override
        public void handleDate(int year, int month, int day) {
            EditText t = findViewById(R.id.editText2);
            if (t != null) {
                String s = year + " " + month + " " + day;
                t.setText(s, TextView.BufferType.NORMAL);
            }
            showTimePicker(t);
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
