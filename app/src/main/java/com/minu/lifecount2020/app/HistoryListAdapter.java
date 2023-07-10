package com.minu.lifecount2020.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.minu.lifecount2020.app.databinding.HistoryEntryBinding;

import java.util.ArrayList;

/**
 * Created by Miro on 19/2/2016.
 */
public class HistoryListAdapter extends BaseAdapter {

    private ArrayList<GameSnapshot> mData;
    private static LayoutInflater mLayoutInflater;

    public HistoryListAdapter(Context context, ArrayList<GameSnapshot> data) {
        mData = data;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryEntryBinding binding = HistoryEntryBinding.inflate(mLayoutInflater, parent, false);

        GameSnapshot snapshot = mData.get(position);
        binding.leftLife.setText(snapshot.getLeftLife());
        binding.rightLife.setText(snapshot.getRightLife());

        addConditionalHistory(snapshot.getLeftPoison(), Constants.STARTING_POISON, binding.leftPoison, binding.poisonLeftIcon);
        addConditionalHistory(snapshot.getRightPoison(), Constants.STARTING_POISON, binding.rightPoison, binding.poisonRightIcon);
        addConditionalHistory(snapshot.getLeftEnergy(), Constants.STARTING_ENERGY, binding.energyLeft, binding.energyLeftIcon);
        addConditionalHistory(snapshot.getRightEnergy(), Constants.STARTING_ENERGY, binding.energyRight, binding.energyRightIcon);

        boolean showPipeAbove = getCount() > 1 && position > 0;
        boolean showPipeBelow = getCount() > 1 && position < getCount() - 1;

        binding.dividerPipeAbove.setVisibility(showPipeAbove ? View.VISIBLE : View.INVISIBLE);
        binding.dividerPipeBelow.setVisibility(showPipeBelow ? View.VISIBLE : View.INVISIBLE);

        return binding.getRoot();
    }

    private void addConditionalHistory(String historyEntry, String baseLine, TextView text, ImageView icon) {
        if (!historyEntry.equals(baseLine)) {
            text.setVisibility(View.VISIBLE);
            text.setText(historyEntry);
            icon.setVisibility(View.VISIBLE);
        }
    }

    public void setHistory(ArrayList<GameSnapshot> history) {
        mData = history;
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
    }
}
