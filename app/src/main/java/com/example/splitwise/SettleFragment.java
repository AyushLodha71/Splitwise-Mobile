package com.example.splitwise;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SettleFragment extends Fragment {
    private String gcode, uname;
    private RecyclerView rvSettleList;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    public SettleFragment(String gcode, String uname) {
        this.gcode = gcode;
        this.uname = uname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settle, container, false);
        rvSettleList = view.findViewById(R.id.rvSettleList);
        rvSettleList.setLayoutManager(new LinearLayoutManager(getContext()));

        loadPendingDebts();
        return view;
    }

    private void loadPendingDebts() {
        new Thread(() -> {
            try {
                String[][] m1Records = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member1=" + uname);
                String[][] m2Records = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member2=" + uname);

                List<String[]> combined = new ArrayList<>();

                if (m1Records != null){

                    combined.addAll(Arrays.asList(m1Records));

                }
                if (m2Records != null){

                    combined.addAll(Arrays.asList(m2Records));

                }

                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (combined.isEmpty()) {
                            Toast.makeText(getContext(), "No transactions yet", Toast.LENGTH_SHORT).show();
                        } else {
                            rvSettleList.setAdapter(new SettleAdapter(combined));
                        }
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private class SettleAdapter extends RecyclerView.Adapter<SettleAdapter.ViewHolder> {
        List<String[]> data;
        SettleAdapter(List<String[]> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] record = data.get(position);
            String m1 = record[1], m2 = record[3];
            double amount = Double.parseDouble(record[2]);
            String displayMsg;
            int color;

            // Logic from your Swing SettlePayment constructor
            if (m1.equals(uname)) {
                if (amount > 0) {
                    displayMsg = m2 + " needs to give you $" + amount;
                    color = Color.parseColor("#2E7D32");
                } else {
                    displayMsg = "You need to give $" + Math.abs(amount) + " to " + m2;
                    color = Color.RED;
                }
            } else {
                if (amount > 0) {
                    displayMsg = "You need to give $" + amount + " to " + m1;
                    color = Color.RED;
                } else {
                    displayMsg = m1 + " needs to give you $" + Math.abs(amount);
                    color = Color.parseColor("#2E7D32");
                }
            }

            holder.tvReason.setText(displayMsg);
            holder.tvPayee.setText("Tap to Settle");
            holder.tvAmount.setVisibility(View.GONE);
            holder.tvReason.setTextColor(color);

            holder.itemView.setOnClickListener(v -> showSettleDialog(record));
        }

        @Override
        public int getItemCount() { return data.size(); }

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

    // This handles the "AmountSettled" part of your logic
    private void showSettleDialog(String[] SIInfo) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settle_amount, null);
        dialog.setContentView(dialogView);

        TextView tvPrompt = dialogView.findViewById(R.id.tvSettlePrompt);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Button btnFinish = dialogView.findViewById(R.id.btnFinishSettle);

        // Determine Prompt and Type (from your AmountSettled logic)
        String prompt;
        double defaultAmt;
        int settleType = 0; // 0 = Receiving, 1 = Paying

        if (Double.parseDouble(SIInfo[2]) > 0) {
            if (SIInfo[1].equals(uname)) {
                prompt = "Amount " + SIInfo[3] + " gave you:";
                defaultAmt = Double.parseDouble(SIInfo[2]);
            } else {
                prompt = "Amount you gave to " + SIInfo[1] + ":";
                defaultAmt = Double.parseDouble(SIInfo[2]);
                settleType = 1;
            }
        } else {
            if (SIInfo[3].equals(uname)) {
                prompt = "Amount " + SIInfo[1] + " gave you:";
                defaultAmt = Math.abs(Double.parseDouble(SIInfo[2]));
            } else {
                prompt = "Amount you gave to " + SIInfo[1] + ":";
                defaultAmt = Math.abs(Double.parseDouble(SIInfo[2]));
                settleType = 1;
            }
        }

        tvPrompt.setText(prompt);
        etAmount.setText(String.valueOf(defaultAmt));

        int finalSettleType = settleType;
        btnFinish.setOnClickListener(v -> {
            recordSettlement(etAmount.getText().toString(), SIInfo, finalSettleType);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void recordSettlement(String amount, String[] SII, int type) {
        new Thread(() -> {
            try {
                // Generate tID (from your createCode logic)
                String tID = "";
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                Random r = new Random();
                for (int i = 0; i < 7; i++) tID += chars.charAt(r.nextInt(chars.length()));

                // Logic from your Finish() method
                if (SII[1].equals(uname)) {
                    if (type == 0) {
                        ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member1='" + uname + "'%20AND%20Member2='" + SII[3] + "'&Amount=" + (Double.parseDouble(SII[2]) - Double.parseDouble(amount)));
                        ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + gcode + "&params=(payee,amount,reason,Ttype,tid)&info=('" + SII[3] + "'," + amount + ",'" + uname + "',1,'" + tID + "')");
                    } else {
                        ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member1='" + uname + "'%20AND%20Member2='" + SII[3] + "'&Amount=" + (Double.parseDouble(SII[2]) + Double.parseDouble(amount)));
                        ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + gcode + "&params=(payee,amount,reason,Ttype,tid)&info=('" + uname + "'," + amount + ",'" + SII[3] + "',1,'" + tID + "')");
                    }
                } else {
                    if (type == 1) {
                        ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member2='" + uname + "'%20AND%20Member1='" + SII[1] + "'&Amount=" + (Double.parseDouble(SII[2]) - Double.parseDouble(amount)));
                        ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + gcode + "&params=(payee,amount,reason,Ttype,tid)&info=('" + uname + "'," + amount + ",'" + SII[1] + "',1,'" + tID + "')");
                    } else {
                        ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member2='" + uname + "'%20AND%20Member1='" + SII[1] + "'&Amount=" + (Double.parseDouble(SII[2]) + Double.parseDouble(amount)));
                        ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + gcode + "&params=(payee,amount,reason,Ttype,tid)&info=('" + SII[1] + "'," + amount + ",'" + uname + "',1,'" + tID + "')");
                    }
                }

                String[][] people = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=CAS_" + gcode);

                if (people != null) {
                    StringBuilder params = new StringBuilder("&params=(Creator,tID,");
                    StringBuilder info = new StringBuilder("&info=('" + uname + "','" + tID + "',");

                    // Start from index 1 to skip headers
                    for (int i = 1; i < people.length; i++) {
                        String memberName = people[i][1];
                        params.append(memberName).append(",");

                        if (memberName.equals(uname)) {
                            // User impact
                            double userAmt = (type == 1) ? -1.0 * Double.parseDouble(amount) : Double.parseDouble(amount);
                            info.append(userAmt).append(",");
                        } else if (memberName.equals(SII[1]) || memberName.equals(SII[3])) {
                            // Other party impact (Opposite of user)
                            double otherAmt = (type == 0) ? -1.0 * Double.parseDouble(amount) : Double.parseDouble(amount);
                            info.append(otherAmt).append(",");
                        } else {
                            // Uninvolved members
                            info.append("0,");
                        }
                    }

                    String finalParams = params.substring(0, params.length() - 1) + ")";
                    String finalInfo = info.substring(0, info.length() - 1) + ")";

                    ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=TD_" + gcode + finalParams + finalInfo);
                }

                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Amount Settled Successfully", Toast.LENGTH_SHORT).show();
                        loadPendingDebts();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}