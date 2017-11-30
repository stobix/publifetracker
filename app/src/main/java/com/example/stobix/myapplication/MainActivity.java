package com.example.stobix.myapplication;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.persistence.room.Room;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static android.util.Log.d;

    public class MainActivity extends AppCompatActivity
            implements
            DatePickerFragment.DatePickerHandler,
            TimePickerFragment.TimePickerHandler,
            //NumberPicker.NumberPickerHandler,
            NumberPickerDialog.OnNumberSetListener,
            SugarEntryCreationActivity.OnSugarEntryEnteredHandler
    {

        private SugarEntryCreationActivity creationActivity;

        public void showEnterer() {
            d("SugarEntry show","weeeee");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            creationActivity = SugarEntryCreationActivity.Companion.newInstance(nextUID);
            creationActivity.show(ft, "dialog");

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


        public void sugarEntryDeleted(@NotNull SugarEntry s){
            Handler db_data_handler = new DeleteHandler(this, findViewById(R.id.tableView));
            (new Thread(
                    () -> {
                        dao.delete(s);

                        Message msg = db_data_handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("entry", s);
                        msg.setData(bundle);
                        db_data_handler.sendMessage(msg);

                    }
            )).start();
        }

        public void sugarEntryGeneralAction(@NotNull SugarEntry s, Consumer<SugarEntry> db_action, Consumer<SugarEntry> table_action){
            Handler db_data_handler =
                    new EntryHandler(
                            this,
                            findViewById(R.id.tableView),
                            table_action
                    );
            (new Thread(
                    () -> {
                        db_action.accept(s);

                        Message msg = db_data_handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("entry", s);
                        msg.setData(bundle);
                        db_data_handler.sendMessage(msg);

                    }
            )).start();
        }

        private static class DeleteHandler extends Handler {
            final MainActivity context;
            final SortableSugarEntryTableView tableView;

            DeleteHandler(MainActivity outer_context, SortableSugarEntryTableView view) {
                context = outer_context;
                tableView = view;
            }
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                SugarEntry s = b.getParcelable("entry");
                tableView.getDataAdapter().remove(s);
            }
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

        private static class EntryHandler extends Handler {
            final MainActivity context;
            final SortableSugarEntryTableView tableView;
            final Consumer<SugarEntry> sf;

            EntryHandler(MainActivity outer_context, SortableSugarEntryTableView view, Consumer<SugarEntry> database_fun) {
                context = outer_context;
                tableView = view;
                sf = database_fun;
            }
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                SugarEntry s = b.getParcelable("entry");
                sf.accept(s);
            }
        }

        private static class EntriesHandler extends Handler {
            final MainActivity context;
            final SortableSugarEntryTableView tableView;
            final Consumer<ArrayList<SugarEntry>> sf;

            EntriesHandler(MainActivity outer_context, SortableSugarEntryTableView view, Consumer<ArrayList<SugarEntry>> database_fun) {
                context = outer_context;
                tableView = view;
                sf = database_fun;
            }

            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                ArrayList<SugarEntry> s = b.getParcelableArrayList("entries");
                if (s != null)
                    sf.accept(s);
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
            fab.setOnClickListener(view -> showEnterer() );


            Handler db_data_handler = new UpdateHandler(this, findViewById(R.id.tableView));

            Runnable initiateDB = () -> {
                SugarEntryDatabase db =
                        Room.databaseBuilder(
                                getApplicationContext(),
                                SugarEntryDatabase.class,
                                "sugarApp").build();

                dao = db.userDao();

                List<SugarEntry> entries = dao.getAll();
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

        public void showDatePicker() {
            new DatePickerFragment().show(getSupportFragmentManager(), "datePicker");
        }

        public void showTimePicker() {
            new TimePickerFragment().show(getSupportFragmentManager(), "timePicker");
        }

        public void showNumberPicker() {
            new NumberPickerDialog(MainActivity.this,MainActivity.this).show();
        }

        @Override
        public void handleDate(int year, int month, int day) {
            creationActivity.handleDate(year,month,day);
        }
        @Override
        public void handleTime(int hour, int minute) {
            creationActivity.handleTime(hour, minute) ;
        }

        @Override
        public void onNumberSet(@NotNull NumberPickerDialog view, float number) {
            creationActivity.onNumberSet(view,number);

        }

    }
