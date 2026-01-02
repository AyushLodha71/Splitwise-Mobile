package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.splitwise.databinding.ActivityEnterGroupBinding;
import java.util.ArrayList;
import java.util.List;

public class EnterGroupActivity extends AppCompatActivity {

    private ActivityEnterGroupBinding binding;
    private String uname;
    private ArrayList<String[]> groupList = new ArrayList<>();
    private String selectedRCode = "";
    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnterGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uname = getIntent().getStringExtra("KEY_USERNAME");

        fetchUserGroups();

        binding.spinnerGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedRCode = groupList.get(position - 1)[0];
                    groupName = groupList.get(position - 1)[1];

                    binding.btnEnterGroup.setEnabled(true);
                    binding.btnEnterGroup.setText("Enter " + groupName);
                } else {
                    binding.btnEnterGroup.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });

        // 3. Enter Button Logic
        binding.btnEnterGroup.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainPageActivity.class);
            intent.putExtra("KEY_USERNAME", uname);
            intent.putExtra("KEY_RCODE", selectedRCode);
            intent.putExtra("KEY_RNAME", groupName);
            startActivity(intent);
            finish();
        });

        // 4. Back Button
        binding.btnEnterBack.setOnClickListener(v -> finish());
    }

    private void fetchUserGroups() {

        new Thread(() -> {
            try {
                // Using your exact PF_ logic
                String url = "https://splitwise-backend-1-eg2d.onrender.com/GetRowData?table=PF_" + uname;
                String[][] data = ApiCaller.ApiCaller1(url);

                runOnUiThread(() -> {
                    List<String> displayNames = new ArrayList<>();
                    displayNames.add(0,"Select Group");

                    for (String[] row : data) {
                        groupList.add(row);
                        displayNames.add(row[1] + " (" + row[0] + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, displayNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerGroups.setAdapter(adapter);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}