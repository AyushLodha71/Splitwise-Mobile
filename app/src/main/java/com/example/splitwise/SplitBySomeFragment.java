package com.example.splitwise;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitBySomeFragment extends Fragment implements SplitFragment {
    private LinearLayout container;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private String gcode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b) {
        View view = inflater.inflate(R.layout.fragment_split_list, vg, false);
        container = view.findViewById(R.id.listContainer);
        gcode = getArguments().getString("GCODE");
        loadMembers();
        return view;
    }

    private void loadMembers() {
        new Thread(() -> {
            String[] members = ApiCaller.ApiCaller3("https://splitwise-backend-1-eg2d.onrender.com/GetSpecificData?val=name&table=SG_" + gcode);
            if (members != null) {
                getActivity().runOnUiThread(() -> {
                    for (String name : members) {
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(name);
                        cb.setChecked(true);
                        container.addView(cb);
                        checkBoxes.add(cb);
                    }
                });
            }
        }).start();
    }

    @Override
    public Map<String, Double> getSplitData(double totalAmount) {
        Map<String, Double> shares = new HashMap<>();
        List<String> selected = new ArrayList<>();
        for (CheckBox cb : checkBoxes) if (cb.isChecked()) selected.add(cb.getText().toString());

        double share = totalAmount / selected.size();
        for (String name : selected) shares.put(name, share);
        return shares;
    }

    @Override
    public boolean isValid(double totalAmount) {
        for (CheckBox cb : checkBoxes) if (cb.isChecked()) return true;
        return false;
    }
}