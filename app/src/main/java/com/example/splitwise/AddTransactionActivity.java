package com.example.splitwise;

import android.os.Bundle;
import android.view.View; // IMPORTANT: Ensure this is android.view.View
import android.widget.AdapterView; // IMPORTANT: Ensure this is android.widget.AdapterView
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for adding new expenses with different split methods
 * Supports equal, partial equal, unequal, and percentage-based splits
 */
public class AddTransactionActivity extends AppCompatActivity {
    private String uname, gcode;
    private EditText etAmount, etReason;
    private Spinner spinner;
    private Fragment currentFragment;
    private final String BASE_URL = "https://splitwise-backend-1-eg2d.onrender.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Get data passed from MainPageActivity
        uname = getIntent().getStringExtra("KEY_USERNAME");
        gcode = getIntent().getStringExtra("KEY_RCODE");

        // Initialize Views
        etAmount = findViewById(R.id.etAmount);
        etReason = findViewById(R.id.etReason);
        spinner = findViewById(R.id.spinnerSplitType);

        setupSpinner();

        // Submit Button logic
        findViewById(R.id.btnSubmitExpense).setOnClickListener(v -> submitData());
    }

    /**
     * Sets up the split type spinner with different split options
     */
    private void setupSpinner() {
        final String[] types = {"All Equally", "Equally By Some", "Unequally", "By Percentages"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switchFragment(types[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not used but must be implemented
            }
        });
    }

    /**
     * Switches the fragment based on selected split type
     */
    private void switchFragment(String type) {
        // Prepare data to pass to fragments
        Bundle bundle = new Bundle();
        bundle.putString("GCODE", gcode);

        if (type.equals("All Equally")) {
            currentFragment = null;
            // Remove any existing fragment
            Fragment existing = getSupportFragmentManager().findFragmentById(R.id.splitFragmentContainer);
            if (existing != null) {
                getSupportFragmentManager().beginTransaction().remove(existing).commit();
            }
            return;
        }

        if (type.equals("Equally By Some")) {
            currentFragment = new SplitBySomeFragment();
        } else if (type.equals("Unequally")) {
            currentFragment = new SplitUnequalFragment();
        } else {
            currentFragment = new SplitPercentageFragment();
        }

        currentFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.splitFragmentContainer, currentFragment)
                .commit();
    }

    /**
     * Validates and submits transaction data to backend
     */
    private void submitData() {
        String amtStr = etAmount.getText().toString();
        String reason = etReason.getText().toString();

        if (amtStr.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please enter amount and reason", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = Double.parseDouble(amtStr);

        new Thread(() -> {
            try {
                // Get all members first to calculate shares
                String[] members = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=name&table=SG_" + gcode);
                if (members == null) return;

                Map<String, Double> shares = new HashMap<>();

                // Handle "All Equally" vs Fragments
                if (currentFragment == null) {
                    double shareValue = total / members.length;
                    for (String m : members) shares.put(m, shareValue);
                } else {
                    SplitFragment sf = (SplitFragment) currentFragment;
                    // Validate on UI thread
                    runOnUiThread(() -> {
                        if (!sf.isValid(total)) {
                            Toast.makeText(this, "Split values are invalid!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    shares.putAll(sf.getSplitData(total));
                }

                if (shares.isEmpty()) return;

                // DATABASE UPDATE LOGIC
                String tID = UUID.randomUUID().toString().substring(0, 7);

                // 1. Insert Public History (PH)
                ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=PH_" + gcode +
                        "&params=(payee,amount,reason,Ttype,tid)&info=('" + uname + "'," + total + ",'" + reason + "',0,'" + tID + "')");

                // 2. Loop through members to update balances
                StringBuilder tdParams = new StringBuilder("(Creator,tID");
                StringBuilder tdInfo = new StringBuilder("('" + uname + "','" + tID + "'");

                // ... inside the member loop in your Thread ...
                for (String member : members) {
                    double share = shares.getOrDefault(member, 0.0);

                    // 1. Update CAS (Same as before, but manual calculation for safety)
                    String[] casResult = ApiCaller.ApiCaller3(BASE_URL + "/GetSpecificData?val=Amount&table=CAS_" + gcode + "&Name=" + member);
                    if (casResult != null && casResult.length > 0) {
                        double currentCAS = Double.parseDouble(casResult[0]);
                        double newCAS = currentCAS + share;
                        ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=CAS_" + gcode + "&where=Name='" + member + "'&Amount=" + newCAS);
                    }

                    // 2. Update PA (The Relationship Table) - MANUAL CALCULATION
                    if (!member.equals(uname)) {
                        // Find how much 'member' currently owes 'uname' (Member1 = uname)
                        // Note: Your API returns a 2D array, so we use ApiCaller1 logic
                        String[][] paData1 = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member1=" + uname + "&Member2=" + member);
                        if (paData1 != null && paData1.length > 0) {
                            double currentPA = Double.parseDouble(paData1[0][2]); // Index 2 is usually the Amount column
                            double newPA = currentPA + share;
                            ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member1='" + uname + "' AND Member2='" + member + "'&Amount=" + newPA);
                        }

                        // Mirror update (Member2 = uname)
                        String[][] paData2 = ApiCaller.ApiCaller1(BASE_URL + "/GetRowData?table=PA_" + gcode + "&Member2=" + uname + "&Member1=" + member);
                        if (paData2 != null && paData2.length > 0) {
                            double currentPA = Double.parseDouble(paData2[0][2]);
                            double newPA = currentPA - share;
                            ApiCaller.ApiCaller2(BASE_URL + "/UpdateData?table=PA_" + gcode + "&where=Member2='" + uname + "' AND Member1='" + member + "'&Amount=" + newPA);
                        }
                    }

                    // 3. Build TD (Transaction Detail) Strings
                    tdParams.append(",").append(member);
                    double tdVal = member.equals(uname) ? (share - total) : share;
                    tdInfo.append(",").append(tdVal);
                }

                // 3. Finalize Transaction Detail (TD)
                ApiCaller.ApiCaller2(BASE_URL + "/InsertData?table=TD_" + gcode + "&params=" + tdParams + ")&info=" + tdInfo + ")");

                runOnUiThread(() -> {
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to MainPage
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}