package com.example.gregoire.rfa;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends Activity implements NoticeDialogFragment.NoticeDialogListener
{
    private WebService              mWebService;
    private SharedPreferences       mPrefs;
    private String                  mEmail, mPassword;
    private ArrayList<String>       mData;
    private ArrayAdapter<String>    mItemsAdapter;
    private DrawerLayout            mDrawer;
    private ListView                mDrawerList;
    private ActionBarDrawerToggle   mDrawerToggle;
    private ArrayList<Pair<String, Integer>> mMap = new ArrayList<Pair<String, Integer>>();
    private ProgressDialog          pDialog;
    private int                     mCurrentFeed = -1;
    private int                     mCurrentPosition = -1;
    private List<Fragment>          mFragmentList;
    static public List<ArrayList<String>> mPostsLists;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9)
       {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
       }

        this.mWebService = new WebService("http://tomcat8-wokesmeed.rhcloud.com");
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = this.mPrefs.edit();
        editor.putBoolean("isLogged", true);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            this.mEmail = extras.getString("email");
            editor.putString("email", this.mEmail);
            this.mPassword = extras.getString("password");
            editor.putString("password", this.mPassword);
        }
        else
        {
            this.mEmail = this.mPrefs.getString("email", "");
            this.mPassword = this.mPrefs.getString("password", "");
        }
        editor.apply();

        setContentView(R.layout.activity_home);
        _initMenu();
    }

    private void _initMenu()
    {
        this.mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        this.mDrawer.setScrimColor(getResources().getColor(android.R.color.transparent));
        this.mData = new ArrayList<String>();
        this.mDrawerList = (ListView) findViewById(R.id.navigation_drawer);
        this.mFragmentList = new ArrayList<Fragment>();
        this.mPostsLists = new ArrayList<ArrayList<String>>();

        setUpDrawerToggle();
        new GetPosts().execute();

        this.mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawer.openDrawer(mDrawerList);
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
            try {   mWebService.disconnectUser();   }
            catch (Exception e) {   e.printStackTrace();    }

            SharedPreferences.Editor editor = this.mPrefs.edit();
            editor.putBoolean("isLogged", false);
            editor.apply();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        else if (id == R.id.delete_feed)
            deleteFeed();
        else if (id == R.id.add_feed)
            askWhichFeed();
        return super.onOptionsItemSelected(item);
    }

    private void deleteFeed()
    {
        new DeleteFeed().execute();
    }

    private void askWhichFeed()
    {
        NoticeDialogFragment    noticeDialogFragment = new NoticeDialogFragment();
        noticeDialogFragment.show(getFragmentManager(), "NoticeDialogFragment");
    }

    public void onDialogPositiveClick(DialogFragment dialog, String feed_url) throws Exception
    {
        dialog.dismiss();
        if (feed_url.isEmpty())
            Toast.makeText(getApplicationContext(), "Error: can't add feed. Is the URL correct?", Toast.LENGTH_SHORT).show();
        else
            new AddFeed().execute(feed_url);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            // Highlight the selected item, update the title, and close the drawer
            // update selected item and title, then close the drawer
            displayView(position);
        }
    }

    private void displayView(int position)
    {
        getFragmentManager().beginTransaction().replace(R.id.frame_container, this.mFragmentList.get(position)).commit();

        setTitle(this.mMap.get(position).first);
        this.mCurrentFeed = this.mMap.get(position).second;
        this.mCurrentPosition = position;

        this.mDrawerList.setItemChecked(position, true);
        this.mDrawerList.setSelection(position);
        this.mDrawer.closeDrawer(this.mDrawerList);
    }

    private class   GetPosts extends AsyncTask<Void, Void, String>
    {
        ArrayList<String> mFeedList = new ArrayList<String>();

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... arg0)
        {
            try {
                if (mWebService.connectUser(mEmail, mPassword))
                {
                    String feeds = mWebService.getFeeds();
                    JSONObject jsonObject = new JSONObject(feeds);
                    JSONArray jArray = jsonObject.getJSONArray("feeds");
                    for (int i = 0; i < jArray.length(); i++)
                    {
                        JSONObject row = jArray.getJSONObject(i);
                        mMap.add(new Pair<String, Integer>(row.getString("title"), row.getInt("id")));
                        mFeedList.add(row.getString("title"));
                        String feed_posts = mWebService.getFeedPosts(row.getInt("id"));
                        // Create and attach List View
                        if (!feed_posts.equals("401") && !feed_posts.equals("404"))
                        {
                            JSONObject jO = new JSONObject(feed_posts);
                            JSONArray jA = jO.getJSONArray("posts");
                            ArrayList<String> postsList = new ArrayList<String>();
                            for (int j = 0; j < jA.length(); j++)
                            {
                                JSONObject r = jA.getJSONObject(j);
                                postsList.add("<a href=\"" + r.getString("link") + "\">" + r.getString("title") + "</a>");
                            }
                            mPostsLists.add(postsList);
                            Fragment fragment = new HomeFragment();
                            Bundle b = new Bundle();
                            b.putInt("index", i);
                            fragment.setArguments(b);
                            mFragmentList.add(fragment);
                        }
                    }
                    return "success";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "failed";
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            if (result.equals("success"))
                setFeedListAdapter(mFeedList);
        }

    }

    private void    setFeedListAdapter(ArrayList<String> feedList)
    {
        this.mData = feedList;
        this.mItemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.mData);
        this.mDrawerList.setAdapter(this.mItemsAdapter);
    }

    private class   AddFeed extends AsyncTask<String, Void, String>
    {
        String  mTitle;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            String json = null;
            try
            {
                json = mWebService.addFeed("http://" + params[0]);
            } catch (Exception e) { e.printStackTrace();    }

            if (json.equals("401") || json.equals("404"))
                return "failed";
            else
            {
                JSONObject jsonObject;
                try
                {
                    jsonObject = new JSONObject(json);
                    mMap.add(new Pair<String, Integer>(jsonObject.getString("title"), jsonObject.getInt("id")));
                    mTitle = jsonObject.getString("title");

                    String feed_content = mWebService.getFeedPosts(jsonObject.getInt("id"));
                    if (!feed_content.equals("401") && !feed_content.equals("404"))
                    {
                        JSONObject jO = new JSONObject(feed_content);
                        JSONArray jA = jO.getJSONArray("posts");
                        ArrayList<String> postsList = new ArrayList<String>();
                        for (int j = 0; j < jA.length(); j++)
                        {
                            JSONObject r = jA.getJSONObject(j);
                            postsList.add("<a href=\"" + r.getString("link") + "\">" + r.getString("title") + "</a>");
                        }
                        mPostsLists.add(postsList);
                        Fragment fragment = new HomeFragment();
                        Bundle b = new Bundle();
                        b.putInt("index", mPostsLists.indexOf(postsList));
                        fragment.setArguments(b);
                        mFragmentList.add(fragment);
                    }
                    return "success";
                } catch (JSONException e) { e.printStackTrace();    }
                catch (Exception e) {   e.printStackTrace();    }
            }
            return "failed";
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            if (result.equals("success"))
            {
                updateFeedListAdapter(mTitle);
                Toast.makeText(getApplicationContext(), "Feed added successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Error: can't add feed. Is the URL correct?", Toast.LENGTH_SHORT).show();
        }
    }

    private void    updateFeedListAdapter(String newFeed)
    {
        this.mData.add(newFeed);
        this.mItemsAdapter.notifyDataSetChanged();
    }

    private class   DeleteFeed extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params)
        {
            if (mCurrentFeed != -1)
             {
                try
                {
                    if (mWebService.deleteFeed(mCurrentFeed) == -1)
                        return "failed";
                    else
                    {
                        mCurrentFeed = -1;
                        mMap.remove(mCurrentPosition);
                        mPostsLists.remove(mCurrentPosition);
                        mFragmentList.get(mCurrentPosition).onDestroy();
                        mFragmentList.remove(mCurrentPosition);
                        return "success";
                    }
                }
                catch (Exception e) {   e.printStackTrace();    }
            }
            return "failed";
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            if (result.equals("success")) {
                removeFromAdapter();
                Toast.makeText(getApplicationContext(), "Feed removed successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Error: no feed selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void    removeFromAdapter()
    {
        this.mData.remove(mCurrentPosition);
        this.mItemsAdapter.notifyDataSetChanged();
    }
}