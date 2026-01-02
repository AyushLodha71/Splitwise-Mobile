package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.splitwise.databinding.ActivityCreateGroupBinding;
import java.util.Random;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    private String uname, generatedCode,gName;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uname = getIntent().getStringExtra("KEY_USERNAME");

        binding.btnSubmit.setOnClickListener(v -> {
            gName = binding.etGroupName.getText().toString().trim();
            if (!gName.isEmpty()) {
                startCreationProcess(gName);
            }
        });

        binding.btnEnterNewGroup.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainPageActivity.class);
            intent.putExtra("KEY_USERNAME", uname);
            intent.putExtra("KEY_RCODE", generatedCode);
            intent.putExtra("KEY_RNAME", gName);
            startActivity(intent);
            finish();
        });

        binding.btnCreateBack.setOnClickListener(v -> finish());
    }

    private void startCreationProcess(String gName) {
        binding.createProgressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        new Thread(() -> {
            try {

                generatedCode = generateUniqueCode();

                ApiCaller.ApiCaller1(BASE_URL + "/CreateTable?table=SG_" + generatedCode + "&columns=id%20INT%20AUTO_INCREMENT%20PRIMARY%20KEY,%20name%20VARCHAR(100)%20NOT%20NULL");
                ApiCaller.ApiCaller1(BASE_URL + "/CreateTable?table=PH_" + generatedCode + "&columns=id%20INT%20AUTO_INCREMENT%20PRIMARY%20KEY%2C%20Payee%20VARCHAR(100)%20NOT%20NULL%2C%20Amount%20DECIMAL(10%2C2)%20NOT%20NULL%2C%20Reason%20VARCHAR(100)%20NOT%20NULL%2C%20TType%20INT%20NOT%20NULL%2C%20tID%20VARCHAR(100)%20COLLATE%20utf8mb4_bin%20NOT%20NULL");
                ApiCaller.ApiCaller1(BASE_URL + "/CreateTable?table=PA_" + generatedCode + "&columns=id%20INT%20AUTO_INCREMENT%20PRIMARY%20KEY,%20Member1%20VARCHAR(100)%20NOT%20NULL,%20Amount%20DECIMAL(10%2C2)%20NOT%20NULL,%20Member2%20VARCHAR(100)%20NOT%20NULL");
                ApiCaller.ApiCaller1(BASE_URL + "/CreateTable?table=CAS_" + generatedCode + "&columns=id%20INT%20AUTO_INCREMENT%20PRIMARY%20KEY,%20Name%20VARCHAR(100)%20NOT%20NULL,%20Amount%20DECIMAL(10%2C2)%20NOT%20NULL");
                ApiCaller.ApiCaller1(BASE_URL + "/CreateTable?table=TD_" + generatedCode + "&columns=id%20INT%20AUTO_INCREMENT%20PRIMARY%20KEY,%20Creator%20VARCHAR(100)%20,%20tID%20VARCHAR(100)%20COLLATE%20utf8mb4_bin," + uname + "%20DECIMAL(10%2C2)%20NOT%20NULL");


                ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=GroupNames_Splitwise&params=(group_name,group_code)&info=('" + gName + "','" + generatedCode + "')");
                ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=SG_" + generatedCode + "&params=(name)&info=('" + uname + "')");
                ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=PF_" + uname + "&params=(GroupID,GroupName)&info=('" + generatedCode + "','" + gName + "')");
                ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=CAS_" + generatedCode + "&params=(Name,Amount)&info=('Total',0)");
                ApiCaller.ApiCaller1(BASE_URL + "/InsertData?table=CAS_" + generatedCode + "&params=(Name,Amount)&info=('" + uname + "',0)");
                ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + generatedCode + "&params=(payee,amount,reason,Ttype,tid)&info=('" + uname + "',0,'created',4,'NA')");
                ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + generatedCode + "&params=(payee,amount,reason,Ttype,tid)&info=('" + uname + "',0,'joined',3,'NA')");

                // UI UPDATE
                runOnUiThread(() -> {
                    binding.createProgressBar.setVisibility(View.GONE);
                    binding.tvGeneratedCode.setText("Group Code: " + generatedCode);
                    binding.layoutSuccess.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Group Created Successfully!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to create group", Toast.LENGTH_LONG).show();
                    binding.createProgressBar.setVisibility(View.GONE);
                    binding.btnSubmit.setEnabled(true);
                });
            }
        }).start();
    }

    private String generateUniqueCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        String newCode;
        boolean exists;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            newCode = sb.toString();
            exists = Exists.exist(BASE_URL + "/GetRowData?table=GroupNames_Splitwise&group_code=" + newCode);
        } while (exists);

        return newCode;
    }
}