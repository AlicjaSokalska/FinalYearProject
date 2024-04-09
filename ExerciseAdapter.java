package com.example.testsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ExerciseAdapter extends ArrayAdapter<ExerciseData> {

    public ExerciseAdapter(Context context, List<ExerciseData> exerciseDataList) {
        super(context, 0, exerciseDataList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExerciseData exerciseData = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_exercise, parent, false);
        }


        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView distanceTextView = convertView.findViewById(R.id.distanceTextView);
        TextView stepCountTextView = convertView.findViewById(R.id.stepCountTextView);

        dateTextView.setText("Date: " + exerciseData.getDate());
        distanceTextView.setText("Distance: " + exerciseData.getDistance() + " km");
        stepCountTextView.setText("Step Count: " + exerciseData.getStepCount());

        return convertView;
    }
}
