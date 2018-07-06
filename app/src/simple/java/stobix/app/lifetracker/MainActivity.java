package stobix.app.lifetracker;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.codecrafters.tableview.SortingOrder;
import de.codecrafters.tableview.TableDataAdapter;
import stobix.utils.ColorHandler;
import stobix.utils.DateHandler;

import static android.util.Log.d;


public class MainActivity extends AppCompatActivity
        implements
        DatePickerFragment.DatePickerHandler,
        TimePickerFragment.TimePickerHandler,
        SugarEntryCreationActivity.OnSugarEntryEnteredHandler,
        SugarEntryCreationActivity.OnSugarEntryChangedHandler,
        SugarEntryCreationActivity.OnSugarEntryDeletedHandler,
        FileActions.FileCreateHandler,
        FileActions.FileOpenHandler
    {

        // Used to handle callback "bleed through"
        //private SugarEntryCreationActivity creationActivity;

        // Since the table view ref never changes,
        // we can just assign it to this variable once instead of doing a lengthy lookup
        // each time in each function.
        private SortableSugarEntryTableView tableView;

        // Used for all data base related operations once initiated in onCreate
        private SugarEntryDao dao;

        @Override
        protected void onCreate(Bundle savedInstanceState) {



            SharedPreferences preferences = getSharedPreferences("colorsNstuff",MODE_PRIVATE);
            boolean useTheme = preferences.getBoolean("useTheme",false);
            if(useTheme) {
                int themeVal = preferences.getInt("theme", R.style.Theme_Zimmik_NoActionBar);
                Log.d("theme", "setting theme to "+themeVal);
                setTheme(themeVal);
            } else
                setTheme(R.style.Theme_Zimmik_NoActionBar);


            // DOn't use savedInstanceState before setting the color theme! It can lead to a vicious
            // loop if the previously selected theme didn't work. I think. ;)
            super.onCreate(savedInstanceState);
            // TODO Add something that sets a default functioning theme if the set theme crashes the app!
            // try
            setContentView(R.layout.activity_main);
            // catch ... -> setTheme(alwaysWorking)
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            tableView = findViewById(R.id.tableView);


            FloatingActionButton fab = findViewById(R.id.fab);
            Log.d("VERSION",""+Build.VERSION.SDK_INT );
            fab.setImageResource(R.drawable.ic_add_24dp);

            fab.setOnClickListener(view -> showSugarEntryCreationDialog() );

            tableView.addDataClickListener((row, sugarEntry) -> {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                SugarEntryCreationActivity creationActivityFlupp =
                        SugarEntryCreationActivity.newInstance(sugarEntry);
                creationActivityFlupp.show(ft, "dialog");

            });

            tableView.addDataLongClickListener( (rowIndex, sugarEntry) -> {
                onSugarEntryEntered(sugarEntry.copyToCurrent());
                return true;
            });

            /*
            tv.addDataLongClickListener((row,sugarEntry) -> {
                sugarEntryDeleted(sugarEntry);
                return true;
            });
            */


            Handler db_data_handler = new DataLoadHandler(this, tableView);



            Runnable initiateDB = () -> {

                SugarEntryDatabase db =DatabaseHandler.buildSugarDatabase(getApplicationContext());

                dao = db.userDao();

                List<SugarEntry> entries = dao.getAll();
                d("LOL","Entries "+entries.size());

                Message msg = db_data_handler.obtainMessage();
                Bundle bundle = new Bundle();
                ArrayList<SugarEntry> arrayEntries = new ArrayList<>(entries);
                bundle.putParcelableArrayList("entries", arrayEntries);
                msg.setData(bundle);
                db_data_handler.sendMessage(msg);
            };

            Thread dbInitThread = new Thread(initiateDB);
            dbInitThread.start();

            BuildVairantSpecificCode.onLoad(this);

            int[] array = {R.attr.colorPrimary};
        TypedArray attrs  = getTheme().obtainStyledAttributes(array);
        int colIn = attrs.getIndex(0);
        int col = attrs.getColor(colIn,0);
        Log.d("ferger","main " + " "+ col);
        attrs.recycle();

        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }


        /**
         * From the super class documentation:
         *
         * This hook is called whenever an item in your options menu is selected.
         * The default implementation simply returns false to have the normal
         * processing happen (calling the item's Runnable or sending a message to
         * its Handler as appropriate).  You can use this method for any items
         * for which you would like to do processing without those other
         * facilities.
         *
         * <p>Derived classes should call through to the base class for it to
         * perform the default menu handling.</p>
         *
         * @return boolean Return false to allow normal menu processing to
         *         proceed, true to consume it here.
         */

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.

            switch(item.getItemId()) {
                /*
                case R.id.action_settings:
                    Log.i("MenuClick", "onOptionsItemSelected: ");
                    return true;

                case R.id.action_colors:

                    ColorPickerDialogBuilder
                            .with(this)
                            .setTitle("Choose color")
                            .initialColor(0xFFFFFFFF)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .density(12)
                            .setOnColorSelectedListener(selectedColor ->
                                    d("COLOR", "Color selected:"
                                            + Integer.toHexString(selectedColor)))
                            .setPositiveButton("ok", (dialog, selectedColor, allColors) ->
                                // TODO Use the color for something.
                                    d("COLOR", "Color confirmed:"
                                            + Integer.toHexString(selectedColor)))
                            .setNegativeButton("cancel", (dialog, which) ->
                                    d("COLOR", "Color aborted"))
                            .build()
                            .show();

                    return true;
                    */


                case R.id.show_stats:
                    Handler h = new MainHandler(
                            this,
                            (main,b) ->{
                                String message = b.getString("message");
                                AlertDialog.Builder builder = new AlertDialog.Builder(main);
                                builder.setTitle(R.string.stat_window_title)
                                        .setMessage(message)
                                        .setPositiveButton(android.R.string.yes,
                                                (dialog, which) -> {
                                                })
                                        .setIcon(android.R.drawable.ic_dialog_info)
                                        .show();
                            });

                    Runnable getStats = () -> {
                        Message m = h.obtainMessage();
                        Bundle b = new Bundle();
                        String message="";

                        // Get all sugar levels mean
                        List<Long> levels  = dao.getAllSugarLevels();
                        Long s = 0L;
                        for (Long l : levels) s += l;
                        message += getString(R.string.stat_window_header_total_bs)+"\n";
                        if ( s == 0 )
                            message += getString(R.string.stat_window_no_bs)+"\n";
                        else {
                            double totAvg = s.doubleValue() / (levels.size()*10);
                            message += String.format(Locale.getDefault(),
                                    "\t"+getString(R.string.stat_window_avg_bs)+"\n" , totAvg);
                            message += "\t"+getString(R.string.stat_window_bs_entries)+levels.size()+"\n";
                        }

                        // Get previous 30 days sugar levels mean
                        DateHandler dateHandler = new DateHandler();
                        levels = dao.getAllSugarLevels(
                                dateHandler.clone().subtractDays(30).getTimestamp(),
                                dateHandler.getTimestamp()
                        );
                        message += getString(R.string.stat_window_header_total_bs_30)+"\n";
                        s=0L;
                        for (Long l : levels) s += l;
                        if ( s == 0 )
                            message += getString(R.string.stat_window_no_bs)+"\n";
                        else {
                            double totAvg = s.doubleValue() / (levels.size()*10);
                            message += String.format(Locale.getDefault(),"\t"+
                                    getString(R.string.stat_window_avg_bs)+"\n" , totAvg);
                            message += "\t"+getString(R.string.stat_window_bs_entries)+levels.size()+"\n";
                        }

                        b.putString("message",message);
                        m.setData(b);
                        h.sendMessage(m);
                    };
                    Thread getStatsT= new Thread(getStats);
                    getStatsT.start();
                    return true;

                case R.id.show_graphs:
                    MainHandler graphHandler =
                            new MainHandler(this,(mainActivity, bundle) -> {
                                Log.d("graph","got bundle");
                                Intent i = new Intent(this, FullscreenGraphActivity.class);
                                i.putExtras(bundle);
                                startActivity(i);
                            });

                    new Thread(
                            () -> {
                                Log.d("graph","got request");
                                Message m = graphHandler.obtainMessage();
                                Bundle b = new Bundle();
                                List<SugarEntry> entries = dao.getAllSugarPoints();
                                Log.d("graph"," request");
                                ArrayList<SugarEntry> entryArrayList = new ArrayList<>(entries);
                                b.putParcelableArrayList("entries", entryArrayList);
                                ArrayList<Integer> colorArrayList = new ArrayList<>();
                                ColorHandler c = new ColorHandler(this);
                                ArrayList<Integer> colorsIDs = new ArrayList<>();
                                colorsIDs.add(android.R.attr.textColorPrimary);
                                colorsIDs.add(android.R.attr.textColorSecondary);
                                colorsIDs.add(android.R.attr.textColorTertiary);
                                colorsIDs.add(R.attr.colorPrimary);
                                colorsIDs.add(R.attr.table_data_row_odd);
                                colorsIDs.add(R.attr.table_data_row_even);
                                Collections.sort(colorsIDs);
                                Map<Integer,Integer> colMap;
                                c.withColorMap(colorsIDs,
                                        colorMap -> {
                                            ArrayList<Integer> colorValues = new ArrayList<>();
                                            // These are in an array instead of sending individiual
                                            // values simply because this works and the other
                                            // method didn't
                                            colorValues.add(0,colorMap.get(android.R.attr.textColorPrimary));
                                            colorValues.add(1,colorMap.get(android.R.attr.textColorSecondary));
                                            colorValues.add(2,colorMap.get(android.R.attr.textColorSecondary));
                                            colorValues.add(3,colorMap.get(R.attr.colorPrimary));
                                            colorValues.add(4,colorMap.get(R.attr.table_data_row_even));
                                            colorValues.add(5,colorMap.get(R.attr.table_data_row_odd));
                                            b.putIntegerArrayList("colors",colorValues);
                                            return null;
                                        }
                                );

                                m.setData(b);
                                Log.d("graph","sending bundle");
                                graphHandler.sendMessage(m);
                            }
                            ).start();

                    return true;

                case R.id.show_sugar_weekly:
                    MainHandler newGraphHandler =
                            new MainHandler(this,(mainActivity, bundle) -> {
                                Log.d("graph","got bundle");
                                Intent i = new Intent(this, DependentBarLineGraphActivity.class);
                                i.putExtras(bundle);
                                startActivity(i);
                            });

                    new Thread(
                            () -> {
                                Log.d("graph","got request");
                                Message m = newGraphHandler.obtainMessage();
                                Bundle b = new Bundle();
                                List<FloatyIntBucket> entries = dao.getAllSugarBuckets();
                                Log.d("graph"," request");
                                ArrayList<FloatyIntBucket> entryArrayList = new ArrayList<>(entries);
                                b.putParcelableArrayList("entries", entryArrayList);
                                b.putString("value_type","floatyInt10");
                                m.setData(b);
                                Log.d("graph","sending bundle");
                                newGraphHandler.sendMessage(m);
                            }
                    ).start();
                    return true;

                case R.id.show_weight_weekly:
                    MainHandler weightGraphHandler =
                            new MainHandler(this,(mainActivity, bundle) -> {
                                Log.d("weight graph","got bundle");
                                Intent i = new Intent(this, DependentBarLineGraphActivity.class);
                                i.putExtras(bundle);
                                startActivity(i);
                            });

                    new Thread(
                            () -> {
                                Log.d("weight graph","got request");
                                Message m = weightGraphHandler.obtainMessage();
                                Bundle b = new Bundle();
                                List<FloatyIntBucket> entries = dao.getAllWeightBuckets();
                                Log.d("weight graph"," entries: "+entries.toString());
                                ArrayList<FloatyIntBucket> entryArrayList = new ArrayList<>(entries);
                                b.putParcelableArrayList("entries", entryArrayList);
                                b.putString("value_type","floatyInt10");
                                b.putDouble("colorHighPoint",90);
                                b.putDouble("colorMidPoint",85);
                                b.putDouble("colorLowPoint",80);
                                b.putBoolean("keepLowZero",false);
                                m.setData(b);
                                Log.d("weight graph","sending bundle");
                                weightGraphHandler.sendMessage(m);
                            }
                    ).start();
                    return true;

                case R.id.action_switch_theme:

                    ArrayList<ThemeListItem> c = new ArrayList<>();
                    //
                    // TODO Put more color themes here, extract this to a build variant common file.
                    //
                    c.add(new ThemeListItem( "Zimmik", R.style.Theme_Zimmik_NoActionBar));
                    c.add(new ThemeListItem( "Joel", R.style.Joel_NoActionBar));
                    c.add(new ThemeListItem("Mad!",R.style.Theme_Mad_NoActionBar));
                    c.add(new ThemeListItem("Cold",R.style.Theme_Cold_NoActionBar));
                    c.add(new ThemeListItem("Spring",R.style.Theme_Spring_NoActionBar));
                    c.add(new ThemeListItem("Cotton",R.style.Theme_Cotton_NoActionBar));
                    c.add(new ThemeListItem("Nicecream",R.style.Theme_Nicecream_NoActionBar));
                    c.add(new ThemeListItem("Strong",R.style.Theme_Strong_NoActionBar));
                    c.add(new ThemeListItem("Fuel",R.style.Theme_Fuel_NoActionBar));
                    c.add(new ThemeListItem("Neonight",R.style.Theme_Neonight_NoActionBar));
                    c.add(new ThemeListItem("Hope",R.style.Hope_NoActionBar));
                    c.add(new ThemeListItem("Hjul",R.style.Hjul_NoActionBar));

                    new ThemePickerDialog(this, c).show();

                    return true;

                case R.id.action_import_db:
                    fa.userReplaceDb();
                    return true;

                case R.id.action_merge_db:
                    fa.userMergeDb();
                    return true;

                case R.id.action_export_db:
                    fa.userCreateFile();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }

        }


        public void doSetTheme(int androidResourceThemeValue){
                    SharedPreferences.Editor editor = getSharedPreferences("colorsNstuff",MODE_PRIVATE).edit();
                    editor.putBoolean("useTheme",true);
                    editor.putInt("theme",androidResourceThemeValue);
                    editor.apply();
                    restartMe();
        }

        // Show the dialog for creating a SugarEntry
        public void showSugarEntryCreationDialog() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            SugarEntryCreationActivity creationActivityFlupp = SugarEntryCreationActivity.newInstance();
            creationActivityFlupp.show(ft, "dialog");

        }

        // Called from the sugar entry dialog when the user clicks 'add'
        @Override
        public void onSugarEntryEntered(@NotNull SugarEntry s) {
            sugarEntryGeneralAction(s,
                    (sugarEntry) -> {
                        while (dao.entryExists(sugarEntry.getEpochTimestamp())) {
                            Log.d("entry", "incrementing sugar");
                            sugarEntry.setEpochTimestamp(sugarEntry.getEpochTimestamp() + 1);
                        }
                        Log.d("entry", "inserting "+sugarEntry.getEpochTimestamp()+" into db");
                        dao.insert(sugarEntry);
                    },
                    (sugarEntry,dataAdapter) -> {
                        Log.d("entry", "inserting "+sugarEntry.getEpochTimestamp()+" into table");
                        dataAdapter.add(sugarEntry);
                    }
            );
        }

        // Called when the user has selected a sugar entry for deletion.
        //
        // The function is an "Are you sure you want to delete this" glue before
        // sugarEntryDeleted is called
        @Override
        public void onSugarEntryDeleted(@NotNull SugarEntry s) {
            // Taken from https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android#2115770
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete entry")
                    .setMessage(R.string.sugar_entry_delete_confirmation)
                    .setPositiveButton(android.R.string.yes,
                            (dialog, which) -> sugarEntryDeleted(s))
                    .setNegativeButton(android.R.string.no,
                            (dialog, which) -> {
                        // do nothing
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Called when the user has selected a sugar entry for deletion, and clicked "yes" on the "are you sure" dialog.
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



        // Made static (i.e. no outer scope references) to prevent memory issues,
        // since lint complained about the anonymous class instance.
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


        private static class MainHandler extends Handler {
            final MainActivity activity;
            final BiConsumer<MainActivity,Bundle> bundleConsumer;
            MainHandler(MainActivity a,BiConsumer<MainActivity,Bundle> messageHandler) {
                activity=a;
                bundleConsumer=messageHandler;
            }
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                bundleConsumer.accept(activity,b);
            }
        }


        // Made static (i.e. no outer scope references) to prevent memory issues,
        // since lint complained about the anonymous class instance.
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
                if(arrayEntries!=null) {
                    tableView.setDataAdapter(new SugarEntryTableDataAdapter(context, arrayEntries));
                    tableView.sort(0, SortingOrder.DESCENDING);
                }
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

        private SugarEntryCreationActivity creationActivity(){
            return (SugarEntryCreationActivity) getFragmentManager().findFragmentByTag("dialog");
        }

        @Override
        public void handleDate(int year, int month, int day) {
            creationActivity().handleDate(year,month,day);
        }
        @Override
        public void handleTime(int hour, int minute) {
            creationActivity().handleTime(hour, minute) ;
        }

        // Used to glue together all pieces of code that handles import/export of the database to/from a JSON file
        private FileActions fa = new FileActions(this);

        @Override
        public void handleFileCreated(@NotNull Uri uri) {
            Log.i("file","created URI: "+uri.toString());
            // FIXME get the version number set in some config file instead!!
            SugarEntryGsonWrapper wrapper = new SugarEntryGsonWrapper(2,tableView.getDataAdapter().getData());
            String json = wrapper.toJSON();
            Log.i("file (json)",json);
            fa.putTextInUri(uri,json);
        }

        @Override
        public void handleFileOpened(@NotNull Uri uri,@NotNull String what) {

                    // TODO Add a spinning disc view or something until the db has finished reloading.
                    MainHandler restarter = new MainHandler(this,
                            (mainActivity, bundle) -> mainActivity.restartMe()
                    );
                    new Thread(() -> {
                        // Gson procedure taken from http://www.vogella.com/tutorials/JavaLibrary-Gson/article.html
                        Log.i("file","opened URI: "+uri.toString());
                        String text = fa.readTextFromUri(uri);
                        Log.i("file","opened file: ");
                        SugarEntryGsonWrapper wrappedEntries = SugarEntryGsonWrapper.fromJSON(text);
                        List<SugarEntry> entries = wrappedEntries.getEntries();
                        Log.i("file","file version: "+wrappedEntries.getVersion());
                        Log.i("file",""+entries.size()+" entries");
                        Log.i("file","adding all entries to the db");

                        Message m =restarter.obtainMessage();
                        switch (what) {
                            case "replace":
                                dao.clear_sugar_entries();
                                dao.insertAll(entries);
                                break;
                            case "merge":


                                List<SugarEntry> currentEntries=tableView.getDataAdapter().getData();
                                if(currentEntries.isEmpty()) {
                                    // Nothing to merge into, just insert.
                                    dao.insertAll(entries);
                                } else {
                                    Log.d("entries",currentEntries.toString());
                                    Log.d("candidates",entries.toString());
                                    List<SugarEntry> mergeables =
                                            SugarEntryMerger.getMergeables(
                                                    currentEntries,
                                                    entries);
                                    Log.d("mergeables",mergeables.toString());
                                    dao.insertAll(mergeables);
                                }
                        }
                        Log.i("file","db done");
                        restarter.sendMessage(m);
                    }).start();

        }

        public void restartMe(){
            Intent intent = getIntent();
            finish();
            startActivity(intent);

        }


        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent resultData) {
            if(!fa.handleFileAction(requestCode,resultCode,resultData)){
                if(fa.isFileAction(requestCode))
                    Log.d("Activity result","file activity aborted/failed: "+requestCode);
                else
                    Log.e("Activity result","unknown activity result: "+requestCode);
            }
        }

    }
