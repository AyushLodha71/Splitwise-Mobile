package com.example.splitwise;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DeleteFragment extends Fragment {
    private String gcode, uname;
    private RecyclerView rvDeleteHistory;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    public DeleteFragment(String gcode, String uname) {
        this.gcode = gcode;
        this.uname = uname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete, container, false);
        rvDeleteHistory = view.findViewById(R.id.rvDeleteHistory);
        rvDeleteHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        loadHistory();
        return view;
    }

    private void loadHistory() {
        new Thread(() -> {
            try {
                // 1. Fetch data from DB
                String[][] historyData = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PH_" + gcode);

                if (historyData != null && historyData.length > 0) {
                    // 2. Reverse the array manually to get Newest on Top
                    int len = historyData.length;
                    String[][] reversedData = new String[len][];
                    for (int i = 0; i < len; i++) {
                        reversedData[i] = historyData[len - 1 - i];
                    }

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // 3. Set the adapter with the REVERSED data
                            rvDeleteHistory.setAdapter(new DeleteAdapter(reversedData));
                        });
                    }
                } else {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "No transactions found", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void processDeletion(String[] tInfo) {
        new Thread(() -> {
            try {
                // --- STEP 1: REPAY LOGIC (Reverse Balance Changes) ---
                String[][] tDetails = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=TD_" + gcode + "&tID=" + tInfo[5]);
                String[] uids = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Name&table=CAS_" + gcode);

                if (tDetails != null && tDetails.length > 0) {
                    String[] data = tDetails[0]; // The row of amounts from TD_

                    // Reverse Total Group Balance (Type 0 only)
                    if (tInfo[4].equals("0")) {
                        String[] totalAmt = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Amount&table=CAS_" + gcode + "&Name=Total");
                        double newTotal = Double.parseDouble(totalAmt[0]) - Double.parseDouble(tInfo[2]);
                        ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=CAS_" + gcode + "&where=Name='Total'&Amount=" + newTotal);
                    }

                    // Reverse Inter-Member (PA_) and Individual (CAS_) Balances
                    for (int i = 1; i < uids.length; i++) {
                        String memberName = uids[i];
                        String creator = data[1]; // Transaction creator is stored in index 1 of TD_ row

                        // Reverse PA_ (Debts)
                        String[] amt1 = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Amount&table=PA_" + gcode + "&Member1=" + memberName + "&Member2=" + creator);
                        String[] amt2 = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Amount&table=PA_" + gcode + "&Member2=" + memberName + "&Member1=" + creator);

                        if (amt1 != null && amt1.length > 0) {
                            double newVal = Double.parseDouble(amt1[0]) + Double.parseDouble(data[i + 2]);
                            ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member1='" + memberName + "'%20AND%20Member2='" + creator + "'&Amount=" + newVal);
                        } else if (amt2 != null && amt2.length > 0) {
                            double newVal = Double.parseDouble(amt2[0]) - Double.parseDouble(data[i + 2]);
                            ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member1='" + creator + "'%20AND%20Member2='" + memberName + "'&Amount=" + newVal);
                        }

                        // Reverse CAS_ (Individual Balances - Type 0 only)
                        if (tInfo[4].equals("0")) {
                            String[] currentMemberBal = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Amount&table=CAS_" + gcode + "&Name=" + memberName);
                            double share = Double.parseDouble(data[i + 2]);
                            double oldBal = Double.parseDouble(currentMemberBal[0]);
                            double updatedBal;

                            if (memberName.equalsIgnoreCase(creator)) {
                                updatedBal = oldBal - Double.parseDouble(tInfo[2]) - share;
                            } else {
                                updatedBal = oldBal - share;
                            }
                            ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=CAS_" + gcode + "&where=Name='" + memberName + "'&Amount=" + updatedBal);
                        }
                    }
                }

                // --- STEP 2: DELETE RECORDS ---
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteRowData?table=PH_" + gcode + "&tID=" + tInfo[5]);
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteRowData?table=TD_" + gcode + "&tID=" + tInfo[5]);

                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Transaction Deleted and Balances Reversed", Toast.LENGTH_LONG).show();
                        loadHistory(); // Refresh the list
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private class DeleteAdapter extends RecyclerView.Adapter<DeleteAdapter.ViewHolder> {
        String[][] history;
        DeleteAdapter(String[][] history) { this.history = history; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] item = history[position];
            String displayMsg;
            if (item[4].equals("0")) {
                displayMsg = item[1] + " added $" + item[2] + " for " + item[3];
            } else {
                displayMsg = item[1] + " paid $" + item[2] + " to " + item[3];
            }
            holder.tvReason.setText(displayMsg);
            holder.tvPayee.setText("TID: " + item[5]);
            holder.tvAmount.setText("");
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"));

            holder.itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to reverse this transaction?")
                        .setPositiveButton("Delete", (dialog, which) -> processDeletion(item))
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() { return history.length; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvReason, tvPayee, tvAmount;
            ViewHolder(View v) {
                super(v);
                tvReason = v.findViewById(R.id.tvReason);
                tvPayee = v.findViewById(R.id.tvPayee);
                tvAmount = v.findViewById(R.id.tvAmount);
            }
        }
    }
}