package org.notabug.lifeuser.ArraysOfCinemas.adapter;

import android.content.Intent;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.notabug.lifeuser.ArraysOfCinemas.R;
import org.notabug.lifeuser.ArraysOfCinemas.activity.CastActivity;

import java.util.ArrayList;


public class CastBaseAdapter extends RecyclerView.Adapter<CastBaseAdapter.CastItemViewHolder> {

	ArrayList<JSONObject> castList;
	public Context context;

	public CastBaseAdapter(ArrayList<JSONObject> castList, Context context) {
		this.castList = castList;
		this.context = context;
	}

	public static class CastItemViewHolder extends RecyclerView.ViewHolder {
		CardView cardView;
		TextView castName;
		TextView characterName;
		ImageView castImage;

		CastItemViewHolder(View itemView) {
			super(itemView);
			cardView = (CardView) itemView.findViewById(R.id.cardView);
			castName = (TextView) itemView.findViewById(R.id.castName);
			characterName = (TextView) itemView.findViewById(R.id.characterName);
			castImage = (ImageView) itemView.findViewById(R.id.castImage);
		}
	}

	@Override
	public int getItemCount() {
		return castList.size();
	}

	@Override
	public CastItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.cast_card, parent, false);
		CastItemViewHolder castItemViewHolder = new CastItemViewHolder(view);
		return castItemViewHolder;
	}

	@Override
	public void onBindViewHolder(CastItemViewHolder holder, int position) {
		final JSONObject actorData = castList.get(position);

		try {
			holder.castName.setText(actorData.getString("name"));
			holder.characterName.setText(actorData.getString("character"));

			if(actorData.getString("profile_path").equals("null")) {
				holder.castImage.setImageDrawable
						(context.getResources().getDrawable(R.drawable.image_broken_variant));
			} else {
				Picasso.with(context).load("https://image.tmdb.org/t/p/w154" +
						actorData.getString("profile_path"))
						.into(holder.castImage);
			}

			Animation animation = AnimationUtils.loadAnimation(context,
					R.anim.fade_in_fast);
			holder.castImage.startAnimation(animation);
		} catch(JSONException je) {
			je.printStackTrace();
		}

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(view.getContext(), CastActivity.class);
				intent.putExtra("actorObject", actorData.toString());
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
