package com.example.splitwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main dashboard for a group - displays transactions, balances, and settlements
 * Provides bottom navigation to different features
 */
public class MainPageActivity extends AppCompatActivity {
    String uname, gcode, gname;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        uname = getIntent().getStringExtra("KEY_USERNAME");
        gcode = getIntent().getStringExtra("KEY_RCODE");
        gname = getIntent().getStringExtra("KEY_RNAME");

        TextView tvName = findViewById(R.id.tvGroupName);
        TextView tvCode = findViewById(R.id.tvGroupCode);
        tvName.setText(gname != null ? gname : "Group Dashboard");
        tvCode.setText("Code: " + gcode);

        findViewById(R.id.btnMainBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnMainExit).setOnClickListener(v -> confirmExit());

        // FAB to add new expense
        findViewById(R.id.fabAddExpense).setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, AddTransactionActivity.class);
            intent.putExtra("KEY_USERNAME", uname);
            intent.putExtra("KEY_RCODE", gcode);
            intent.putExtra("KEY_RName", gname);
            startActivity(intent);

        });

        // Bottom navigation setup
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            loadBalances();

            if (id == R.id.nav_home) {
                selectedFragment = new HistoryFragment(gcode, uname);
            } else if (id == R.id.nav_balances) {
                selectedFragment = new BalancesFragment(gcode, uname);
            } else if (id == R.id.nav_settle) {
                selectedFragment = new SettleFragment(gcode, uname);
            } else if (id == R.id.nav_delete) {
                selectedFragment = new DeleteFragment(gcode, uname);
            } else if (id == R.id.nav_amountspent) {
                selectedFragment = new AmountSpentFragment(gcode, uname);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });

        // Set Default Tab
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HistoryFragment(gcode, uname)).commit();
    }

    /**
     * Refreshes data when activity resumes
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Force the Header Balances to update
        loadBalances();

        // Force the current Fragment to reload its data
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof HistoryFragment) {
            ((HistoryFragment) currentFragment).loadHistoryData();
            loadBalances();
        } else if (currentFragment instanceof BalancesFragment) {
            // Add similar reload methods for other fragments if they exist
        }
    }

    /**
     * Fetches and displays group balances from backend
     */
    public void loadBalances() {
        new Thread(() -> {
            try {
                // 1. Fetch where you are Member 1
                String[][] side1 = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member1=" + uname);
                // 2. Fetch where you are Member 2
                String[][] side2 = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member2=" + uname);

                double totalOwedToYou = 0;
                double totalYouOwe = 0;

                // Process Side 1: Member1 is YOU
                if (side1 != null) {
                    for (String[] row : side1) {
                        double amt = Double.parseDouble(row[2]);
                        if (amt > 0) totalOwedToYou += amt;      // Positive: They owe you
                        else if (amt < 0) totalYouOwe += Math.abs(amt); // Negative: You owe them
                    }
                }

                // Process Side 2: Member2 is YOU (Reverse Logic)
                if (side2 != null) {
                    for (String[] row : side2) {
                        double amt = Double.parseDouble(row[2]);
                        if (amt < 0) totalOwedToYou += Math.abs(amt); // Negative here means Member1 owes Member2 (You)
                        else if (amt > 0) totalYouOwe += amt;         // Positive here means Member2 (You) owes Member1
                    }
                }

                final double finalOwed = totalOwedToYou;
                final double finalOwe = totalYouOwe;

                runOnUiThread(() -> {
                    TextView tvOwed = findViewById(R.id.tvYouAreOwed);
                    TextView tvOwe = findViewById(R.id.tvYouOwe);
                    if (tvOwed != null) tvOwed.setText(String.format("$%.2f", finalOwed));
                    if (tvOwe != null) tvOwe.setText(String.format("$%.2f", finalOwe));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group?")
                .setMessage("To leave, your balance with all members must be $0.00.")
                .setPositiveButton("Verify & Exit", (d, w) -> checkEligibilityAndExit())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkEligibilityAndExit() {
        new Thread(() -> {
            try {
                // 1. Check Eligibility
                String[][] rec1 = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member1=" + uname);
                String[][] rec2 = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member2=" + uname);

                boolean canExit = true;
                if (rec1 != null) for (String[] row : rec1) if (Double.parseDouble(row[2]) != 0) canExit = false;
                if (rec2 != null) for (String[] row : rec2) if (Double.parseDouble(row[2]) != 0) canExit = false;

                if (!canExit) {
                    runOnUiThread(() -> Toast.makeText(this, "Settle balances first!", Toast.LENGTH_LONG).show());
                    return;
                }

                // 2. 9-Step Delete Sequence
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteRowData?table=PA_" + gcode + "&Member1=" + uname);
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteRowData?table=PA_" + gcode + "&Member2=" + uname);

                String[] userBal = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Amount&table=CAS_" + gcode + "&Name=" + uname);
                String[] totalBal = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Amount&table=CAS_" + gcode + "&Name=Total");
                double newTotal = Double.parseDouble(totalBal[0]) - Double.parseDouble(userBal[0]);

                ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=CAS_" + gcode + "&where=Name='Total'&Amount=" + newTotal);
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteRowData?table=CAS_" + gcode + "&Name=" + uname);
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteRowData?table=SG_" + gcode + "&name=" + uname);
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteRowData?table=PF_" + uname + "&GroupID=" + gcode);
                ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + gcode + "&params=(payee,amount,reason,Ttype,tid)&info=('" + uname + "',0,'left',2,'NA')");
                ApiCaller.ApiCaller2(BASE_URL + "/DeleteColumn?table=TD_" + gcode + "&uname=" + uname);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Group Exited", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}