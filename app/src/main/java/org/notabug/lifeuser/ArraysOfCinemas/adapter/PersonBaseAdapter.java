package org.notabug.lifeuser.ArraysOfCinemas.adapter;

import android.content.Intent;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.notabug.lifeuser.ArraysOfCinemas.R;
import org.notabug.lifeuser.ArraysOfCinemas.activity.CastActivity;

import java.util.ArrayList;


public class PersonBaseAdapter extends RecyclerView.Adapter<PersonBaseAdapter.PersonItemViewHolder> {

    private ArrayList<JSONObject> mPersonList;


    public PersonBaseAdapter(ArrayList<JSONObject> personList) {
        mPersonList = personList;
    }


    public static class PersonItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView personName;
        ImageView personImage;

        PersonItemViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            personName = (TextView) itemView.findViewById(R.id.personName);
            personImage = (ImageView) itemView.findViewById(R.id.personImage);
        }
    }

    @Override
    public int getItemCount() {
        return mPersonList.size();
    }

    @Override
    public PersonItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person_card, parent, false);
        PersonItemViewHolder personItemViewHolder = new PersonItemViewHolder(view);
        return personItemViewHolder;
    }

    @Override
    public void onBindViewHolder(PersonItemViewHolder holder, int position) {
        final JSONObject personData = mPersonList.get(position);

        Context context = holder.cardView.getContext();

        try {
            holder.personName.setText(personData.getString("name"));
            if(personData.getString("profile_path") == null) {
                holder.personImage.setImageDrawable
                        (context.getResources().getDrawable(R.drawable.image_broken_variant));
            } else {
                Picasso.with(context).load("https://image.tmdb.org/t/p/w154"
                        + personData.getString("profile_path"))
                        .into(holder.personImage);
            }
        } catch(JSONException je) {
            je.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CastActivity.class);
                intent.putExtra("actorObject", personData.toString());
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

