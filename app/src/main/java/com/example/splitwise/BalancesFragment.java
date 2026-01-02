package com.example.splitwise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BalancesFragment extends Fragment {
    private String gcode, uname;
    private RecyclerView rvBalances;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    public BalancesFragment(String gcode, String uname) {
        this.gcode = gcode;
        this.uname = uname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_balances, container, false);
        rvBalances = view.findViewById(R.id.rvBalances);
        rvBalances.setLayoutManager(new LinearLayoutManager(getContext()));

        loadBalances();
        return view;
    }

    private void loadBalances() {
        new Thread(() -> {
            try {
                // Fetch members from SG_ table
                String[] memberArray = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=name&table=SG_" + gcode);
                List<String> memberList = Arrays.asList(memberArray);

                // Fetch debts from PA_ table
                String[][] paData = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode);

                // Map each member to their balance list (logic from your Swing app)
                List<MemberBalance> processedBalances = new ArrayList<>();
                for (String name : memberList) {
                    MemberBalance mb = new MemberBalance(name);
                    if (paData != null) {
                        for (String[] row : paData) {
                            String m1 = row[1], m2 = row[3];
                            double amt = Double.parseDouble(row[2]);

                            if (name.equals(m1) || name.equals(m2)) {
                                if (amt > 0) {
                                    mb.details.add("• " + m2 + " owes " + amt + " to " + m1);
                                } else if (amt < 0) {
                                    mb.details.add("• " + m1 + " owes " + (-amt) + " to " + m2);
                                } else {
                                    mb.details.add("• Settled with " + (name.equals(m1) ? m2 : m1));
                                }
                            }
                        }
                    }
                    processedBalances.add(mb);
                }

                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        rvBalances.setAdapter(new BalanceAdapter(processedBalances));
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // Helper Data Class
    private static class MemberBalance {
        String name;
        List<String> details = new ArrayList<>();
        boolean isExpanded = false;
        MemberBalance(String name) { this.name = name; }
    }

    // Inner Adapter to avoid extra files
    private class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.ViewHolder> {
        List<MemberBalance> data;
        BalanceAdapter(List<MemberBalance> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_balance, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MemberBalance item = data.get(position);
            holder.tvName.setText(item.name);

            StringBuilder sb = new StringBuilder();
            for (String s : item.details) sb.append(s).append("\n");
            holder.tvDetails.setText(sb.toString().trim());

            // Handle Expand/Collapse Logic (Your toggle logic)
            holder.tvDetails.setVisibility(item.isExpanded ? View.VISIBLE : View.GONE);
            holder.ivChevron.setRotation(item.isExpanded ? 180 : 0);

            holder.itemView.setOnClickListener(v -> {
                item.isExpanded = !item.isExpanded;
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetails;
            ImageView ivChevron;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvMemberName);
                tvDetails = v.findViewById(R.id.tvBalanceDetails);
                ivChevron = v.findViewById(R.id.ivChevron);
            }
        }
    }
}