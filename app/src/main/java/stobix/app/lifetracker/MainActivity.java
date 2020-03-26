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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.codecrafters.tableview.SortingOrder;
import de.codecrafters.tableview.TableDataAdapter;
import kotlin.Pair;
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
        public SugarEntryDao dao;

        public int currentTheme=R.style.Theme_Zimmik_NoActionBar;
        //
        // TODO Put more color themes here, extract this to a build variant common file.
        //
        static ArrayList<ThemeListItem> COLOR_THEMES =new ArrayList<ThemeListItem>(){{
            add(new ThemeListItem( "Zimmik", R.style.Theme_Zimmik_NoActionBar));
            add(new ThemeListItem( "Joel", R.style.Joel_NoActionBar));
            add(new ThemeListItem("Mad!",R.style.Theme_Mad_NoActionBar));
            add(new ThemeListItem("Cold",R.style.Theme_Cold_NoActionBar));
            add(new ThemeListItem("Spring",R.style.Theme_Spring_NoActionBar));
            add(new ThemeListItem("Cotton",R.style.Theme_Cotton_NoActionBar));
            add(new ThemeListItem("Nicecream",R.style.Theme_Nicecream_NoActionBar));
            add(new ThemeListItem("Strong",R.style.Theme_Strong_NoActionBar));
            add(new ThemeListItem("Fuel",R.style.Theme_Fuel_NoActionBar));
            add(new ThemeListItem("Neonight",R.style.Theme_Neonight_NoActionBar));
            add(new ThemeListItem("Hope",R.style.Hope_NoActionBar));
            add(new ThemeListItem("Hjul",R.style.Hjul_NoActionBar));
        }};
        @Override
        protected void onCreate(Bundle savedInstanceState) {

            SharedPreferences preferences = getSharedPreferences("colorsNstuff",MODE_PRIVATE);
            boolean useTheme = preferences.getBoolean("useTheme",false);
            if(useTheme) {
                currentTheme = preferences.getInt("theme", R.style.Theme_Zimmik_NoActionBar);
                Log.d("theme", "setting theme to "+currentTheme);
            }
            setTheme(currentTheme);
            SharedPreferences visibilityPrefs = getSharedPreferences("visibility",MODE_PRIVATE);
            listIconsDisplayed= visibilityPrefs.getBoolean("show table icons",false);

            // Don't use savedInstanceState before setting the color theme! It can lead to a vicious
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
                        SugarEntryCreationActivity.newEditInstance(sugarEntry);
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
                d("initiateDB","Entries "+entries.size());

                Message msg = db_data_handler.obtainMessage();
                Bundle bundle = new Bundle();
                ArrayList<SugarEntry> arrayEntries = new ArrayList<>(entries);
                bundle.putParcelableArrayList("entries", arrayEntries);
                msg.setData(bundle);
                db_data_handler.sendMessage(msg);
            };

            Thread dbInitThread = new Thread(initiateDB);
            dbInitThread.start();

            BuildVariantSpecificCode.onLoad(this);

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
            //((MenuItem)findViewById(R.id.action_toggle_list_icons)).setChecked(listIconsDisplayed);
            MenuItem showHideIconIcon = menu.findItem(R.id.action_toggle_list_icons);
            showHideIconIcon.setChecked(listIconsDisplayed);
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
            int värde = item.getItemId();
            Pair<Boolean, Boolean> result = BuildVariantSpecificCode.handleMenu(this,värde);
            if(result.component1())
                return result.component2();
            else if(result.component2()){
                return super.onOptionsItemSelected(item);
            } else {
                switch(värde) {
                    default:
                        return true;
                }
            }
        }

        public void doChangeIconVisibility(){
            SharedPreferences preferences = getSharedPreferences("visibility",MODE_PRIVATE);
            boolean showIcons = preferences.getBoolean("show table icons",false);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("show table icons",!showIcons);
            editor.apply();
            restartMe();
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

            SugarEntryCreationActivity creationActivityFlupp = SugarEntryCreationActivity.newCreationInstance();
            creationActivityFlupp.show(ft, "dialog");

        }

        // Called from the sugar entry dialog when the user clicks 'add'
        @Override
        public void onSugarEntryEntered(@NotNull SugarEntry s) {
            sugarEntryGeneralAction(s,
                    (sugarEntry) -> {
                        while (dao.entryExists(sugarEntry.getTimestamp())) {
                            Log.d("entry", "incrementing sugar");
                            sugarEntry.setTimestamp(sugarEntry.getTimestamp() + 1);
                        }
                        Log.d("entry", "inserting "+sugarEntry.getTimestamp()+" into db");
                        dao.insert(sugarEntry);
                    },
                    (sugarEntry,dataAdapter) -> {
                        Log.d("entry", "inserting "+sugarEntry.getTimestamp()+" into table");
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
        public void onSugarEntryChanged(@NotNull SugarEntry s, long originalTimestamp){
           sugarEntryGeneralAction(s,
                   (sugarEntry) ->{
                       // turns out there was a penalty for getting rid of the sugarID: we now have to move the sugar entry manually before updating it, since otherwise we'll lose track of it.
                       if(s.getTimestamp() != originalTimestamp) {
                           Log.d("entry update", "timestamp changed ("+originalTimestamp+" → "+s.getTimestamp()+")");
                            // Try to update the timestamp to the new timestamp
                           while (dao.updateTimestamp(originalTimestamp, sugarEntry.getTimestamp()) < 1) {
                               // If we fail to update the timestamp, the new timestamp is already occupied, and we try the next timestamp millisecond for availability
                               Log.d("entry update", "entry " + sugarEntry.getTimestamp() + " occupied, increasing…");
                               sugarEntry.setTimestamp(sugarEntry.getTimestamp() + 1);
                           }
                       }
                       dao.update(sugarEntry);
                   }
                   ,
                   (sugarEntry, dataAdapter) -> dataAdapter.notifyDataSetChanged()
                   );
        }

        /*
        public Consumer<String> lol() {
            return (s) -> {} ;
        }

        Consumer<String> kaka = lol();

        kaka.accept(s);
        */

        public void convertExtrasToCategory(){
            TableDataAdapter<SugarEntry> x = tableView.getDataAdapter();
            List<SugarEntry> y = x.getData();

            Log.d("convert", "Have "+y.size()+" elements before");
            for(int i = 0; i<y.size();i++) {
                SugarEntry data = y.get(i);
                String extra = data.getExtra();
                String category = data.getCategory();
                if(extra == null) {
                    Log.d("convert","empty extra");
                    continue;
                }
                if(category != null && !data.getCategory().isEmpty()) {
                    Log.d("convert","non-empty cat: "+data.getCategory());
                    continue;
                }
                String sleepstr = getString(R.string.EntryCreatorSleepString);

                String catstr="";
                if(category != null)
                    catstr=category;
                if(extra.equals("Jobb") || extra.equals("jobb")){
                    Log.d("convert","cat jobb");
                    data.setCategory("Jobb");
                    data.setExtra(null);
                } else if(extra.startsWith("Jobb ")){
                    Log.d("convert","cat jobb extra");
                    data.setCategory("Jobb");
                    String jobbextra = extra.substring(5);
                    if(jobbextra.equals(""))
                        jobbextra=null;
                    data.setExtra(jobbextra);
                } else if (extra.equals(sleepstr)){
                    Log.d("convert","cat sleep ");
                   data.setCategory(sleepstr);
                } else if(extra.startsWith(sleepstr+": ")){
                    Log.d("convert","cat sleep extra");
                    data.setCategory(sleepstr);
                    String sömnextra = extra.substring(sleepstr.length()+2);
                    if(sömnextra.equals(""))
                        sömnextra=null;
                    data.setExtra(sömnextra);
                } else {
                    Log.d("convert", "meh: ("+ catstr + ") " + extra);
                }
                onSugarEntryChanged(data,data.getTimestamp());
                // y.set(i,data);
            }
            Log.d("convert", "Have "+y.size()+" elements after");
            // x.clear();
            //x.addAll(y);
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


        public static class MainHandler extends Handler {
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
        public static class DataLoadHandler extends Handler {
            final MainActivity mainActivity;
            final SortableSugarEntryTableView tableView;

            DataLoadHandler(MainActivity outer_context, SortableSugarEntryTableView view) {
                mainActivity = outer_context;
                tableView = view;
            }

            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                ArrayList<SugarEntry> arrayEntries = b.getParcelableArrayList("entries");
                if(arrayEntries!=null) {
                    tableView.setDataAdapter(new SugarEntryTableDataAdapter(mainActivity, arrayEntries));
                    tableView.sort(0, SortingOrder.DESCENDING);
                }
            }
        }



        public void showDatePicker(int token, DateHandler date) {
            DatePickerFragment datePickerFragment=new DatePickerFragment();
            Bundle b = new Bundle();
            b.putInt("token",token);
            b.putParcelable("date",date);
            datePickerFragment.setArguments(b);
            datePickerFragment.show(getSupportFragmentManager(), "datePicker");
        }

        public void showTimePicker(int token, DateHandler date) {
            showTimePicker(token,date.getHour(),date.getMinute());
        }

        public void showTimePicker(int token, int hour, int minute) {
            TimePickerFragment t = new TimePickerFragment();
            Bundle b = new Bundle();
            b.putInt("hour",hour);
            b.putInt("minute",minute);
            b.putInt("token",token);
            t.setArguments(b);
            t.show(getSupportFragmentManager(), "timePicker");
        }

        private SugarEntryCreationActivity creationActivity(){
            return (SugarEntryCreationActivity) getFragmentManager().findFragmentByTag("dialog");
        }

        @Override
        public void handleDate(int token, DateHandler date) {
            creationActivity().handleDate(token, date.getYear(),date.getMonth(),date.getDay());
        }
        @Override
        public void handleTime(int token, int hour, int minute) {
            creationActivity().handleTime(token, hour, minute) ;
        }

        // Used to glue together all pieces of code that handles import/export of the database to/from a JSON file
        public FileActions fa = new FileActions(this);

        @Override
        public void handleFileCreated(@NotNull Uri uri) {
            Log.i("file","created URI: "+uri.toString());
            // FIXME get the version number set in some config file instead!!
            SugarEntryGsonWrapper wrapper = new SugarEntryGsonWrapper(tableView.getDataAdapter().getData());
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
                        Log.i("file",""+entries.size()+" entries");
                        Log.i("file","adding all entries to the db");

                        Message m =restarter.obtainMessage();
                        switch (what) {
                            case "replace":
                                List<SugarEntry> tries = dao.getAll();
                                List<FloatyIntBucket> buckits = dao.getAllSugarBuckets();
                                Log.d("replace","Old entries: "+tries.size()+" buckets: "+buckits.size());

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


        private static DisplayMetrics displayMetrics = null;

        public synchronized DisplayMetrics getScreenMetrics(){
            if (displayMetrics == null) {
                displayMetrics = new DisplayMetrics();
                Display d = getWindowManager().getDefaultDisplay();
                d.getRealMetrics(displayMetrics);
            }
            return displayMetrics;
        }

        private boolean listIconsDisplayed=false;

        // XXX Possible bug source: There is nary any need for a lock here, but the user MIGHT click on the menu item the exact same micro second the rows are redisplayed ...
        public boolean displaysListIcons(){
            return listIconsDisplayed;
        }


    }
