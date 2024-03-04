package com.example.progettolam;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.progettolam.databinding.FragmentItemBinding;

import java.util.List;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    
    private List<Measurement> mValues;
    
    public MyItemRecyclerViewAdapter(List<Measurement> items) {
        mValues = items;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        
        return new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        
    }
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.textType.setText(String.valueOf(mValues.get(position).getId()));
        holder.textInfo.setText(mValues.get(position).toString());
    }
    
    @Override
    public int getItemCount() {
        return mValues.size();
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textInfo;
        public final TextView textType;
        
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            textInfo = binding.textInfo;
            textType = binding.textType;
        }
        
        @Override
        public String toString() {
            return super.toString() + " '" + textInfo.getText() + "'";
        }
    }
    
    public void updateList(List<Measurement> m) {
        if (mValues != null) {
            mValues.clear();
            mValues.addAll(m);
            notifyDataSetChanged();
        } else
            mValues = m;
    }
}