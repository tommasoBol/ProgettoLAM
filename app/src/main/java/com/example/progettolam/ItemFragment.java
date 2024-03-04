package com.example.progettolam;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

public class ItemFragment extends Fragment {
    
    private MapViewModel mvvm;

    public ItemFragment() {
    }

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        
        mvvm = ViewModelProvider.AndroidViewModelFactory.getInstance(
                this.getActivity().getApplication()).create(MapViewModel.class);
        
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        MyItemRecyclerViewAdapter adapter = new MyItemRecyclerViewAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        mvvm.getLiveMeasurements().observe(getViewLifecycleOwner(), (List<Measurement> list) -> adapter.updateList(list));
        
        
        return view;
    }
}