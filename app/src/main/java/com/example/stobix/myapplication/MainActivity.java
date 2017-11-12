package com.example.stobix.myapplication;

import android.app.FragmentTransaction;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.util.Log.d;

    public class MainActivity extends AppCompatActivity implements DatePickerFragment.ClickedListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

        Random random = new Random();

        private String rndStr() {
            byte[] foo=new byte[30];
            random.nextBytes(foo);
            return String.format("%s", foo);
        }

        private int rndSgr() {
            return random.nextInt(300)+10;
        }

        private Date rndDat() {
            Date d = new Date();
            d.setMinutes(random.nextInt(60));
            d.setHours(random.nextInt(24));
            d.setMonth(random.nextInt(12));
            d.setDate(random.nextInt(31));
            d.setYear(random.nextInt(2)+115);
            return d;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            d("DB", "onCreate: Doing stuff");
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener( view ->
                            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show());


            Context c = this;

            Handler db_data_handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    Bundle b = msg.getData();
                    ArrayList<SugarEntry> arrayEntries=b.getParcelableArrayList("entries");
                    SortableSugarEntryTableView tableView = findViewById(R.id.tableView);
                    SugarEntryTableDataAdapter adapter = new SugarEntryTableDataAdapter(c,arrayEntries);
                    tableView.setDataAdapter(adapter);
                }
            };

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
                dao.insert(new SugarEntry(entries.size()+1,rndDat(),rndSgr(),rndStr()));
                entries = dao.getAll();

                Message msg=db_data_handler.obtainMessage();
                Bundle bundle=new Bundle();
                ArrayList<SugarEntry> arrayEntries=new ArrayList<>(entries);
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

        public void showNosePickerDialog(View view) {

            // TODO https://developer.android.com/guide/components/fragments.html#EventCallbacks
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }


        /**
         * A native method that is implemented by the 'native-lib' native library,
         * which is packaged with this application.
         */
        public native String stringFromJNI();

        @Override
        public void gotTheStuff(int year, int month, int day) {
            EditText t = findViewById(R.id.editText2);
            if(t!=null) {
                String s = year+" "+month+" "+day;
                d("CALLBACK",s);
                t.setText(s, TextView.BufferType.NORMAL);
            }
        }
    }
