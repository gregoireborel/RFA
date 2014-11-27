package com.example.gregoire.rfa;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class HomeActivity extends Activity implements NoticeDialogFragment.NoticeDialogListener
{
    private WebService              mWebService;
    private SharedPreferences       mPrefs;
    private String                  mEmail, mPassword;
    private ArrayAdapter<String>    mItemsAdapter;
    private DrawerLayout            mDrawer;
    private ListView                mDrawerList;
    private ActionBarDrawerToggle   mDrawerToggle;
    private ArrayList<Pair<String, Integer>> mMap = new ArrayList<Pair<String, Integer>>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.mWebService = new WebService("http://tomcat8-wokesmeed.rhcloud.com");
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = this.mPrefs.edit();
        editor.putBoolean("isLogged", true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.mEmail = extras.getString("email");
            editor.putString("email", this.mEmail);
            this.mPassword = extras.getString("password");
            editor.putString("password", this.mPassword);
        } else {
            this.mEmail = this.mPrefs.getString("email", "");
            this.mPassword = this.mPrefs.getString("password", "");
        }
        editor.commit();

        setContentView(R.layout.activity_home);
        _initMenu();
    }

    private void _initMenu()
    {
        this.mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        this.mItemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        this.mDrawerList = (ListView) findViewById(R.id.navigation_drawer);
        this.mDrawerList.setAdapter(this.mItemsAdapter);
        setUpDrawerToggle();

        new Thread()
        {
            public void run()
            {
                try {
                    if (mWebService.connectUser(mEmail, mPassword)) {
                        String feeds = mWebService.getFeeds();
                        JSONObject jsonObject = new JSONObject(feeds);
                        JSONArray jArray = jsonObject.getJSONArray("feeds");
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject row = jArray.getJSONObject(i);
                            mMap.add(new Pair<String, Integer>(row.getString("title"), row.getInt("id")));
                            mItemsAdapter.add(row.getString("title"));
                           mItemsAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        this.mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void setUpDrawerToggle()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        this.mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                this.mDrawer,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        )
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        // NB: required for the drawer indicator to show up!
        this.mDrawer.post(new Runnable()
        {
            @Override
            public void run()
            {
                mDrawerToggle.syncState();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            new Thread()
            {
                public void run()
                {
                    try {   mWebService.disconnectUser();   }
                    catch (Exception e) {   e.printStackTrace();    }
                }
            }.start();

            SharedPreferences.Editor editor = this.mPrefs.edit();
            editor.putBoolean("isLogged", false);
            editor.commit();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        else if (id == R.id.add_feed)
            askWhichFeed();
        return super.onOptionsItemSelected(item);
    }

    private void    askWhichFeed()
    {
        NoticeDialogFragment mDialog = new NoticeDialogFragment();
        mDialog.show(getFragmentManager(), "NoticeDialogFragment");
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            // Highlight the selected item, update the title, and close the drawer
            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(".....");
            mDrawer.closeDrawer(mDrawerList);
        }
    }

    public void onDialogPositiveClick(DialogFragment dialog) throws Exception
    {
        final EditText new_feed = (EditText) findViewById(R.id.add_feed_edit_text);
        System.out.println("New feed is " + new_feed.getText().toString());

        new Thread()
        {
            public void run()
            {
                try {
                    String addition_success = mWebService.addFeed(new_feed.getText().toString());
                    System.out.println("Addition success " + addition_success);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        dialog.dismiss();
    }
}