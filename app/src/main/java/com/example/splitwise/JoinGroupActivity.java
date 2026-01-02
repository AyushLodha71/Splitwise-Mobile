package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.splitwise.databinding.ActivityJoinGroupBinding;

public class JoinGroupActivity extends AppCompatActivity {

    private ActivityJoinGroupBinding binding;
    private String uname;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uname = getIntent().getStringExtra("KEY_USERNAME");

        // ONLY this listener for the submit button
        binding.btnSubmit.setOnClickListener(v -> {
            String code = binding.etJoinCode.getText().toString().trim();
            if (!code.isEmpty()) {
                startJoinProcess(code);
            } else {
                Toast.makeText(this, "Please enter a code", Toast.LENGTH_SHORT).show();
            }
        });

        // If you have a back button in your XML, use this:
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void startJoinProcess(String code) {
        // Show loading spinner, hide button
        binding.joinProgressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        new Thread(() -> {
            try {
                // 1. VALIDATION
                boolean exists = Exists.exist(BASE_URL + "/GetRowData?table=GroupNames_Splitwise&group_code=" + code);
                boolean alreadyMember = Exists.exist(BASE_URL + "/GetRowData?table=SG_" + code + "&name=" + uname);

                if (exists && !alreadyMember) {
                    String[][] members = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=SG_" + code);
                    for (String[] row : members) {
                        String existingMember = row[1];
                        ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=PA_" + code +
                                "&params=(Member1,Amount,Member2)&info=('" + existingMember + "',0,'" + uname + "')");
                    }

                    ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=SG_" + code + "&params=(name)&info=('" + uname + "')");

                    String[] gnameArray = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=group_name&table=GroupNames_Splitwise&group_code=" + code);
                    String gname = gnameArray.length > 0 ? gnameArray[0] : "Unknown Group";


                    ApiCaller.ApiCaller1(BASE_URL + "/AddColumn?table=TD_" + code + "&uname=" + uname);
                    ApiCaller.ApiCaller1(BASE_URL + "/UpdateData?table=TD_" + code + "&where=1=1&" + uname + "=0");

                    ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=PF_" + uname + "&params=(GroupID,GroupName)&info=('" + code + "','" + gname + "')");

                    ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=CAS_" + code + "&params=(Name,Amount)&info=('" + uname + "',0)");
                    ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + code + "&params=(payee,amount,reason,Ttype,tid)&info=('" + uname + "',0,'joined',3,'NA')");

                    // SUCCESS -> UI THREAD
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Success! Joined " + gname, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainPageActivity.class);
                        intent.putExtra("KEY_USERNAME", uname);
                        intent.putExtra("KEY_RCODE", code);
                        intent.putExtra("KEY_RNAME", gname);
                        startActivity(intent);
                        finish();
                    });

                } else {
                    runOnUiThread(() -> {
                        String msg = !exists ? "Wrong Code" : "Already a member";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        resetUI();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                    resetUI();
                });
            }
        }).start();

    }

    private void resetUI() {
        binding.joinProgressBar.setVisibility(View.GONE);
        binding.btnSubmit.setEnabled(true);
    }
}