package com.example.splitwise;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitUnequalFragment extends Fragment implements SplitFragment {
    private LinearLayout container;
    private Map<String, EditText> inputMap = new HashMap<>();
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
                        EditText et = new EditText(getContext());
                        et.setHint(name + " ($)");
                        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        container.addView(et);
                        inputMap.put(name, et);
                    }
                });
            }
        }).start();
    }

    @Override
    public Map<String, Double> getSplitData(double totalAmount) {
        Map<String, Double> shares = new HashMap<>();
        for (Map.Entry<String, EditText> entry : inputMap.entrySet()) {
            String val = entry.getValue().getText().toString();
            shares.put(entry.getKey(), val.isEmpty() ? 0.0 : Double.parseDouble(val));
        }
        return shares;
    }

    @Override
    public boolean isValid(double totalAmount) {
        double sum = 0;
        for (EditText et : inputMap.values()) {
            String val = et.getText().toString();
            sum += val.isEmpty() ? 0 : Double.parseDouble(val);
        }
        return Math.abs(totalAmount - sum) < 0.01; // Ensure sum equals total
    }
}