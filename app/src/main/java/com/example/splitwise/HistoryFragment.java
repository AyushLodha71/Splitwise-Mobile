package com.example.splitwise;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {
    private String gcode, uname;
    private RecyclerView rvTransactions;

    public HistoryFragment(String gcode, String uname) {
        this.gcode = gcode;
        this.uname = uname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        loadHistoryData();

        return view;
    }

    public void loadHistoryData() {

        new Thread(() -> {
            try {
                String[][] data = ApiCaller.ApiCaller1("https://splitwise-backend-1-eg2d.onrender.com/GetRowData?table=PH_" + gcode);
                List<TransactionModel> list = new ArrayList<>();
                if (data != null) {
                    for (String[] row : data) {
                        list.add(new TransactionModel(row[1], row[2], row[3], row[4]));
                    }
                    Collections.reverse(list);
                }

                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        rvTransactions.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
                            @NonNull
                            @Override
                            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
                                return new ViewHolder(v);
                            }

                            @Override
                            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                                TransactionModel item = list.get(position);

                                // Reset defaults (important for RecyclerView recycling)
                                holder.tvAmount.setVisibility(View.VISIBLE);
                                holder.ivTypeIcon.setColorFilter(Color.parseColor("#2E7D32")); // Default Green

                                if ("0".equals(item.type)) {
                                    // TYPE 0: NEW EXPENSE
                                    holder.tvReason.setText(item.reason);
                                    holder.tvPayee.setText(item.payee + " added an expense");
                                    holder.tvAmount.setText("$" + item.amount);

                                    // Green if you paid, Red if you owe
                                    int color = item.payee.equalsIgnoreCase(uname) ? Color.parseColor("#2E7D32") : Color.RED;
                                    holder.tvAmount.setTextColor(color);
                                    holder.ivTypeIcon.setImageResource(R.drawable.ic_add_transaction);

                                } else if ("1".equals(item.type)) {
                                    // TYPE 1: SETTLE PAYMENT
                                    holder.tvReason.setText("Settled Payment");
                                    holder.tvPayee.setText(item.payee + " paid " + item.reason); // reason field contains recipient
                                    holder.tvAmount.setText("$" + item.amount);
                                    holder.tvAmount.setTextColor(Color.parseColor("#1976D2")); // Blue for settlements

                                    // Use your new vector or a reliable system one
                                    holder.ivTypeIcon.setImageResource(R.drawable.ic_settle);
                                    holder.ivTypeIcon.setColorFilter(Color.parseColor("#1976D2"));

                                } else if ("2".equals(item.type)) {
                                    // TYPE 2: EXIT GROUP
                                    holder.tvReason.setText("Member Update");
                                    holder.tvPayee.setText(item.payee + " has left the group");
                                    holder.tvAmount.setVisibility(View.GONE); // No money involved

                                    // Use your new vector or a reliable system one
                                    holder.ivTypeIcon.setImageResource(android.R.drawable.ic_delete);
                                    holder.ivTypeIcon.setColorFilter(Color.GRAY);
                                } else if ("3".equals(item.type)) {
                                    // TYPE 2: EXIT GROUP
                                    holder.tvReason.setText("Member Update");
                                    holder.tvPayee.setText(item.payee + " has joined the group");
                                    holder.tvAmount.setVisibility(View.GONE); // No money involved

                                    // Use your new vector or a reliable system one
                                    holder.ivTypeIcon.setImageResource(android.R.drawable.ic_menu_add);
                                    holder.ivTypeIcon.setColorFilter(Color.GRAY);
                                } else if ("4".equals(item.type)) {
                                    // TYPE 2: EXIT GROUP
                                    holder.tvReason.setText("Group Update");
                                    holder.tvPayee.setText(item.payee + " created the group");
                                    holder.tvAmount.setVisibility(View.GONE); // No money involved

                                    // Use your new vector or a reliable system one
                                    holder.ivTypeIcon.setImageResource(android.R.drawable.ic_menu_add);
                                    holder.ivTypeIcon.setColorFilter(Color.GRAY);
                                }

                                holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"));

                            }

                            @Override
                            public int getItemCount() { return list.size(); }
                        });
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReason, tvPayee, tvAmount;
        android.widget.ImageView ivTypeIcon; // Add this

        public ViewHolder(View itemView) {
            super(itemView);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvPayee = itemView.findViewById(R.id.tvPayee);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon); // And this
        }
    }

}