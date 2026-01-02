package com.example.splitwise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class AmountSpentFragment extends Fragment {
    private String gcode, uname;
    private LinearLayout containerBalances;

    public AmountSpentFragment(String gcode, String uname) {
        this.gcode = gcode;
        this.uname = uname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amount_spent, container, false);
        containerBalances = view.findViewById(R.id.containerBalances);
        loadTable();
        return view;
    }

    private void loadTable() {
        new Thread(() -> {
            try {
                String[][] data = ApiCaller.ApiCaller1("https://splitwise-backend-1-eg2d.onrender.com/GetRowData?table=CAS_" + gcode);
                if (isAdded() && data != null) {
                    getActivity().runOnUiThread(() -> {
                        containerBalances.removeAllViews();
                        for (int i = 1; i < data.length; i++) {
                            View row = getLayoutInflater().inflate(R.layout.item_expenditure_row, null);
                            ((TextView)row.findViewById(R.id.tvMemberName)).setText(data[i][1]);
                            ((TextView)row.findViewById(R.id.tvMemberAmount)).setText("$" + data[i][2]);
                            containerBalances.addView(row);
                        }
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}