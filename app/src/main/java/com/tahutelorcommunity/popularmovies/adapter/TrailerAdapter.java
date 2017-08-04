package com.tahutelorcommunity.popularmovies.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tahutelorcommunity.popularmovies.R;
import com.tahutelorcommunity.popularmovies.activity.MainActivity;
import com.tahutelorcommunity.popularmovies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Fikry-PC on 7/2/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder>{
    private JSONArray trailerData;
    public TrailerAdapter(){

    }

    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachParentImmediately = false;

        View view = inflater.inflate(R.layout.trailer_list_item, parent, shouldAttachParentImmediately);
        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapterViewHolder holder, int position) {
        JSONObject trailer;
        String trailerName = null;
        try{
            trailer = trailerData.getJSONObject(position);
            trailerName = trailer.getString("name");
        } catch (Exception e){
            e.printStackTrace();
        }
        holder.trailerName.setText(trailerName);
    }

    @Override
    public int getItemCount() {
        if(trailerData==null){
            return 0;
        }
        else{
            return trailerData.length();
        }
    }

    public void setTrailerData(JSONArray data){
        this.trailerData = data;
        notifyDataSetChanged();
    }

    class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final TextView trailerName;
        public Context mContext;
        public String trailerUrl;
        public Activity mActivity;
        public TrailerAdapterViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
            itemView.setTag(trailerUrl);
            trailerName = (TextView)itemView.findViewById(R.id.trailer_name);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            JSONObject data;
            Uri youtubeUri=null;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            try{
                data = trailerData.getJSONObject(clickedPosition);
                String key = data.getString("key");
                youtubeUri = Uri.parse(NetworkUtils.buildGetYoutubeUrl(key).toString());
            } catch (Exception e){
                Toast.makeText(mContext, mContext.getString(R.string.sorry_not_available), Toast.LENGTH_SHORT).show();
            }
            intent.setData(youtubeUri);
            if(intent.resolveActivity(mContext.getPackageManager()) !=null ){
                mContext.startActivity(intent);
            }
        }
    }
}
