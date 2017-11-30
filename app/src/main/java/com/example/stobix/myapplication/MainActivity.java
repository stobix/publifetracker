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
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;

import static android.util.Log.d;

    public class MainActivity extends AppCompatActivity
            implements
            DatePickerFragment.DatePickerHandler,
            TimePickerFragment.TimePickerHandler,
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
            sugarEntryGeneralAction(s,
                    (sugarEntry) -> dao.insert(sugarEntry),
                    (sugarEntry,dataAdapter) ->dataAdapter.add(sugarEntry)
            );
        }

        public void sugarEntryDeleted(@NotNull SugarEntry s){
            sugarEntryGeneralAction(s,
                    (sugarEntry) -> dao.delete(sugarEntry),
                    (sugarEntry,dataAdapter) -> dataAdapter.remove(sugarEntry)
            );
        }

        public void sugarEntryChanged(@NotNull SugarEntry s){
           sugarEntryGeneralAction(s,
                   (sugarEntry) -> dao.update(sugarEntry),
                   (sugarEntry, sugarEntryTableDataAdapter) -> {}
                   );
        }

        public void sugarEntryGeneralAction(
                @NotNull SugarEntry s,
                Consumer<SugarEntry> db_action,
                BiConsumer<SugarEntry, TableDataAdapter<SugarEntry>> table_action
        ){
            Handler table_data_handler =
                    new EntryHandler(
                            findViewById(R.id.tableView),
                            table_action
                    );
            (new Thread(
                    () -> {
                        db_action.accept(s);

                        Message msg = table_data_handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("entry", s);
                        msg.setData(bundle);
                        table_data_handler.sendMessage(msg);

                    }
            )).start();
        }

        private static class EntryHandler extends Handler {
            final SortableSugarEntryTableView tableView;
            final BiConsumer<SugarEntry, TableDataAdapter<SugarEntry>> sf;

            EntryHandler(
                    SortableSugarEntryTableView view,
                    BiConsumer<SugarEntry, TableDataAdapter<SugarEntry>> table_fun) {
                tableView = view;
                sf = table_fun;
            }

            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                SugarEntry s = b.getParcelable("entry");
                sf.accept(s,tableView.getDataAdapter());
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

            SortableSugarEntryTableView tv = findViewById(R.id.tableView);

            tv.addDataClickListener((row, sugarEntry) -> {
                d("SugarEntry", "Row " + row + " got clicked!");
                sugarEntryDeleted(sugarEntry);
                //open some dialog, maybe the entry creation dialog, to change element,
                // and let it call sugarEntryChanged(sugarEntry);
            });


            Handler db_data_handler = new UpdateHandler(this, tv);

            Runnable initiateDB = () -> {
                SugarEntryDatabase db =
                        Room.databaseBuilder(
                                getApplicationContext(),
                                SugarEntryDatabase.class,
                                "sugarApp").build();

                dao = db.userDao();

                List<SugarEntry> entries = dao.getAll();
                d("LOL","Entries "+entries.size());

                nextUID=dao.getMaxUID()+1;

                d("LOL","Max UID: "+nextUID);

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
