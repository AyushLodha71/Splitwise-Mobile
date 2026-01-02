package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.splitwise.databinding.ActivityGroupBinding;

/**
 * Group management hub - allows users to create, join, or enter groups
 */
public class GroupActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityGroupBinding binding;
    private String uname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uname = getIntent().getStringExtra("KEY_USERNAME");
        binding.tvWelcomeUser.setText("Welcome, " + uname + "!");


        // Set click listeners for all buttons
        binding.btnEnterGroup.setOnClickListener(this);
        binding.btnJoinGroup.setOnClickListener(this);
        binding.btnCreateGroup.setOnClickListener(this);
        binding.btnLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.btnEnterGroup) {
            Intent intent = new Intent(this, EnterGroupActivity.class);
            intent.putExtra("KEY_USERNAME", uname);
            startActivity(intent);

        } else if (id == R.id.btnJoinGroup) {
            Intent intent = new Intent(this, JoinGroupActivity.class);
            intent.putExtra("KEY_USERNAME", uname);
            startActivity(intent);

        } else if (id == R.id.btnCreateGroup) {
            Intent intent = new Intent(this, CreateGroupActivity.class);
            intent.putExtra("KEY_USERNAME", uname);
            startActivity(intent);

        } else if (id == R.id.btnLogout) {
            // Log out: Clear task and go to Welcome
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }

}