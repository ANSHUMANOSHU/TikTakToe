package com.media.tiktaktoe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.media.tiktaktoe.entity.Player;
import com.media.tiktaktoe.helper.RequestHelper;

import java.util.ArrayList;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Player> players = new ArrayList<>();

    public PlayersAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_layout,parent,false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.playerName.setText(players.get(position).name);
        holder.playerName.setSelected(true);

        holder.playerStats.setText(players.get(position).stats.wins+" / "+players.get(position).stats.losses);
        holder.playerStats.setSelected(true);

        holder.playerId.setText(players.get(position).id);
        holder.playerId.setSelected(true);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestHelper.openRequestDialog(context,players.get(position).name,players.get(position).id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView playerName, playerStats,playerId;
        private LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playerId = itemView.findViewById(R.id.playerID);
            playerName = itemView.findViewById(R.id.playerName);
            playerStats = itemView.findViewById(R.id.playerStats);
            linearLayout = itemView.findViewById(R.id.layout);
        }
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
        notifyDataSetChanged();
    }
}
