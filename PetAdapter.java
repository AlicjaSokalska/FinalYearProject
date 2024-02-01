package com.example.testsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PetAdapter extends ArrayAdapter<Pet> {

    private Context context;
    private List<Pet> petList;

    public PetAdapter(Context context, int resource, List<Pet> petList) {
        super(context, resource, petList);
        this.context = context;
        this.petList = petList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.pet_list_item, parent, false);
        }

        Pet currentPet = petList.get(position);

        ImageView petImageView = listItemView.findViewById(R.id.petProfileImage);
        TextView petNameTextView = listItemView.findViewById(R.id.petNameTextView);


        Glide.with(context)
                .load(currentPet.getImageUrl())
                .placeholder(R.drawable.default_pet_image)
                .error(R.drawable.error_image)
                .into(petImageView);


        petNameTextView.setText(currentPet.getName());

        return listItemView;
    }
}


