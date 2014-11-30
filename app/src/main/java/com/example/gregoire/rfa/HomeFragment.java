package com.example.gregoire.rfa;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment
{
    ArrayAdapter    mAdapter;
    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ListView list = (ListView) rootView.findViewById(R.id.postListView);
        ArrayList<String> postList = HomeActivity.mPostsList.get(HomeActivity.mPostsList.size() - 1);
        this.mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, postList);
        list.setAdapter(this.mAdapter);
        return rootView;
    }
}