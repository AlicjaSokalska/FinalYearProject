package com.example.testsample;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeekExercise extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_exercise);


        List<ExerciseData> exerciseDataList = retrieveExerciseData();


        ExerciseAdapter adapter = new ExerciseAdapter(this, exerciseDataList);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    private List<ExerciseData> retrieveExerciseData() {
        List<ExerciseData> exerciseDataList = new ArrayList<>();


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            String dayOfWeek = dayFormat.format(calendar.getTime());


            String formattedDate = dateFormat.format(calendar.getTime());
           ExerciseData exerciseData = getExerciseDataForDay(formattedDate + "_" + dayOfWeek);
            if (exerciseData == null) {
                exerciseDataList.add(new ExerciseData(formattedDate + "_" + dayOfWeek, 0.0, 0));
            } else {
                exerciseDataList.add(exerciseData);
            }

            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        return exerciseDataList;
    }

    private ExerciseData getExerciseDataForDay(String dayKey) {
       if ("20220127_Thursday".equals(dayKey)) {
            return new ExerciseData(dayKey, 18.24, 24);
        } else {
            return null;
        }
    }
}
