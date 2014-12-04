package com.example.gregoire.rfa;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeFragment extends Fragment
{
    MySimpleArrayAdapter    mAdapter;

    public HomeFragment(){}

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ArrayList<String> posts = HomeActivity.mPostsLists.get(getArguments().getInt("index"));
        this.mAdapter = new MySimpleArrayAdapter(getActivity(), posts);

        ListView list = (ListView) rootView.findViewById(R.id.postListView);
        list.setAdapter(this.mAdapter);
        return rootView;
    }
}

class MySimpleArrayAdapter extends ArrayAdapter<String>
{
    private final Context context;
    private final ArrayList<String> values;

    public MySimpleArrayAdapter(Context context, ArrayList<String> values)
    {
        super(context, 0, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
       values.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        // Populate the data into the template view using the data object
        tvName.setText(Html.fromHtml(values.get(position)));
        tvName.setMovementMethod(LinkMovementMethod.getInstance());
        // Return the completed view to render on screen
        return convertView;
    }
}