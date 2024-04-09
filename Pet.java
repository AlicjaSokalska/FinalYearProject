package com.example.testsample;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.Serializable;

public class Pet implements Serializable {
    private String name;
    private String breed;
    private String dob;
    private String description;
    private String imageUrl;
    private String type;
    private PetLocation location;

    public Pet() {
        // Required empty constructor for Firebase
    }

    public Pet(String name,  String dob,String type,String breed, String description, String imageUrl, PetLocation location) {
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.dob = dob;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;

    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getDob() {
        return dob;
    }


    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public PetLocation getLocation() {
        return location;
    }


    public void setLocation(PetLocation location) {
        this.location = location;
    }



    public void loadPetImage(ImageView imageView, Context context) {
        if (imageUrl != null) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageView);
        }
    }
}
