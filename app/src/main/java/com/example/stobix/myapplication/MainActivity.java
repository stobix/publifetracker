package com.example.stobix.myapplication;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;
import kotlin.Pair;

import static android.util.Log.d;

    public class MainActivity extends AppCompatActivity
            implements
            DatePickerFragment.DatePickerHandler,
            TimePickerFragment.TimePickerHandler,
            NumberPickerFragment.NumberPickedHandler,
            NumberPickerFragment.NumberClearedHandler,
            SugarEntryCreationActivity.OnSugarEntryEnteredHandler,
            SugarEntryCreationActivity.OnSugarEntryChangedHandler
    {

        // Used to handle callback "bleed through"
        private SugarEntryCreationActivity creationActivity;

        // Since the table view ref never changes,
        // we can just assign it to this variable once instead of doing a lengthy lookup
        // each time in each function.
        private SortableSugarEntryTableView tableView;

        // Used for all data base related operatoins once initiated in onCreate
        private SugarEntryDao dao;

        // Used to keep track of the next UID that a SugarEntry can have to not
        // crash the data base on insertion.
        private int nextUID;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            tableView = findViewById(R.id.tableView);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(view -> showSugarEntryCreationDialog() );

            SortableSugarEntryTableView tv = tableView;

            tv.addDataClickListener((row, sugarEntry) -> {
                // TODO
                //open some dialog, maybe the entry creation dialog, to change element,
                // and let it call sugarEntryChanged(sugarEntry);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                creationActivity = SugarEntryCreationActivity.newInstance(sugarEntry);
                creationActivity.show(ft, "dialog");

                //sugarEntryDeleted(sugarEntry);
            });


            Handler db_data_handler = new DataLoadHandler(this, tv);

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
                Log.i("MenuClick", "onOptionsItemSelected: ");
                return true;
            }

            if (id == R.id.action_colors){

                ColorPickerDialogBuilder
                        .with(this)
                        //.with(context)
                        .setTitle("Choose color")
                        .initialColor(0xFFFFFFFF)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                d("COLOR","Color selected:"+Integer.toHexString(selectedColor));
                                //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                d("COLOR","Pos click");
                                //changeBackgroundColor(selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                d("COLOR","Neg click");
                            }
                        })
                        .build()
                        .show();

            }

            return super.onOptionsItemSelected(item);
        }

        // Show the dialog for creating a SugarEntry
        public void showSugarEntryCreationDialog() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            creationActivity = SugarEntryCreationActivity.newInstance(nextUID);
            creationActivity.show(ft, "dialog");

        }

        // Called from the sugar entry dialog when the user clicks 'add'
        @Override
        public void onSugarEntryEntered(@NotNull SugarEntry s) {
            nextUID++;
            sugarEntryGeneralAction(s,
                    (sugarEntry) -> dao.insert(sugarEntry),
                    (sugarEntry,dataAdapter) -> dataAdapter.add(sugarEntry)
            );
        }

        /*

        Old function call temporarily added for instructional reasons:
        public void sugarEntryDeleted(@NotNull SugarEntry s){
            sugarEntryGeneralAction(s,
                    new Consumer<SugarEntry>() {
                        @Override
                        public void accept(SugarEntry sugarEntry) {
                            dao.delete(sugarEntry);
                        }
                    },
                    new BiConsumer<SugarEntry, TableDataAdapter<SugarEntry>>() {
                        @Override
                        public void accept(SugarEntry sugarEntry, TableDataAdapter<SugarEntry> dataAdapter) {
                            dataAdapter.remove(sugarEntry);
                        }
                    }
            );
        }
        */

        // Called when the user has selected a sugar entry for deletion.
        public void sugarEntryDeleted(@NotNull SugarEntry s){
            sugarEntryGeneralAction(s,
                    (sugarEntry) -> dao.delete(sugarEntry),
                    (sugarEntry,dataAdapter) -> dataAdapter.remove(sugarEntry)
            );
        }

        // Called when the user has changed a sugar entry and pressed 'submit changes'
        public void onSugarEntryChanged(@NotNull SugarEntry s){
           sugarEntryGeneralAction(s,
                   (sugarEntry) -> dao.update(sugarEntry),
                   (sugarEntry, dataAdapter) -> dataAdapter.notifyDataSetChanged()
                   );
        }


        // An abstraction of all data base followed by table adapter related consumer
        // actions that can be performed on a SugarEntry.
        public void sugarEntryGeneralAction(
                @NotNull SugarEntry s,
                Consumer<SugarEntry> db_action,
                BiConsumer<SugarEntry, TableDataAdapter<SugarEntry>> table_action
        ){
            Handler table_data_handler =
                    new EntryHandler(
                            tableView,
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



        // Made static (i.e. no outer scope references) to prevent memory issues, since lint complained about the anonymous class instance.
        // See https://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
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
        private static class DataLoadHandler extends Handler {
            final MainActivity context;
            final SortableSugarEntryTableView tableView;

            DataLoadHandler(MainActivity outer_context, SortableSugarEntryTableView view) {
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

        public void showDatePicker(int year,int month,int day) {
            DatePickerFragment datePickerFragment=new DatePickerFragment();
            Bundle b = new Bundle();
            b.putInt("year",year);
            b.putInt("month",month);
            b.putInt("day",day);
            datePickerFragment.setArguments(b);
            datePickerFragment.show(getSupportFragmentManager(), "datePicker");
        }

        public void showTimePicker(int hour, int minute) {
            TimePickerFragment t = new TimePickerFragment();
            Bundle b = new Bundle();
            b.putInt("hour",hour);
            b.putInt("minute",minute);
            t.setArguments(b);
            t.show(getSupportFragmentManager(), "timePicker");
        }

        public void showNumberPicker(int val, int frac, int min, int max){
            NumberPickerFragment n = new NumberPickerFragment();
            Bundle b = new Bundle();
            b.putInt("value",val);
            b.putInt("fraction",frac);
            b.putInt("min",min);
            b.putInt("max",max);
            n.setArguments(b);
            n.show(getSupportFragmentManager(), "numberPicker");
        }

        /*
        public void showNumberPicker() {
            new NumberPickerDialog(MainActivity.this,MainActivity.this).show();
        }
        */

        @Override
        public void handleDate(int year, int month, int day) {
            creationActivity.handleDate(year,month,day);
        }
        @Override
        public void handleTime(int hour, int minute) {
            creationActivity.handleTime(hour, minute) ;
        }

        @Override
        public void handleNumber(@NotNull Pair<Integer, Integer> number) {
            creationActivity.onNumberSet(number);
        }
        @Override
        public void handleNumberClear() {
            creationActivity.onNumberClear();
        }

    }
