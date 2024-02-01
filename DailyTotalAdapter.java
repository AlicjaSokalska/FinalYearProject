package com.example.testsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DailyTotalAdapter extends ArrayAdapter<DailyTotal> {

    private Context context;
    private List<DailyTotal> dailyTotals;

    public DailyTotalAdapter(@NonNull Context context, @NonNull List<DailyTotal> dailyTotals) {
        super(context, 0, dailyTotals);
        this.context = context;
        this.dailyTotals = dailyTotals;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.daily_total_item, parent, false);
        }

        DailyTotal currentTotal = dailyTotals.get(position);

        TextView dateTextView = listItemView.findViewById(R.id.dateTextView);
        TextView distanceTextView = listItemView.findViewById(R.id.distanceTextView);
        TextView stepCountTextView = listItemView.findViewById(R.id.stepCountTextView);

        dateTextView.setText("Date: " + currentTotal.getDate());
        distanceTextView.setText("Distance: " + currentTotal.getDistance());
        stepCountTextView.setText("Step Count: " + currentTotal.getStepCount());

        return listItemView;
    }
}
