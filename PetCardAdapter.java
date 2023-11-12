package com.example.testsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import com.squareup.picasso.Picasso;

import java.util.List;
public class PetCardAdapter extends RecyclerView.Adapter<PetCardAdapter.PetViewHolder> {
    private List<Pet> petList;
    private Context context;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PetCardAdapter(List<Pet> petList, Context context) {
        this.petList = petList;
        this.context = context;
    }


    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pet_card_view, parent, false);
        return new PetViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        final Pet pet = petList.get(position);
        holder.petNameTextView.setText(pet.getName());
        holder.petAgeTextView.setText(pet.getAge());
        holder.petBreedTextView.setText(pet.getBreed());
        holder.petDescriptionTextView.setText(pet.getDescription());

        // image Picasso
        Picasso.get().load(pet.getImageUrl()).into(holder.petImageView);
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public Pet getPet(int position) {
        if (position >= 0 && position < petList.size()) {
            return petList.get(position);
        }
        return null;
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView petNameTextView;
        TextView petAgeTextView;
        TextView petBreedTextView;
        TextView petDescriptionTextView;
        ImageView petImageView;

        public PetViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            petNameTextView = itemView.findViewById(R.id.petNameTextView);
            petAgeTextView = itemView.findViewById(R.id.petAgeTextView);
            petBreedTextView = itemView.findViewById(R.id.petBreedTextView);
            petDescriptionTextView = itemView.findViewById(R.id.petDescriptionTextView);
            petImageView = itemView.findViewById(R.id.petImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
