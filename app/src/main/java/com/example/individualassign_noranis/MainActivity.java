package com.example.individualassign_noranis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent; // REQUIRED for dropdown listener
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView; // REQUIRED for the dropdown field
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
// import android.widget.Spinner; // NO LONGER USED
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    AutoCompleteTextView monthDropdown; // Corrected type
    TextInputLayout tilUnit;
    EditText etUnit;

    RadioGroup radioGroupRebate;

    TextView tvTotalCharges, tvFinalCost;
    Button btnCalculateSave, btnViewHistory;

    private boolean isCalculated = false;

    private double currentUnitUsed = 0;
    private double currentRebatePercent = 0;
    private double currentTotalCharges = 0;
    private double currentFinalCost = 0;
    private String currentMonth = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDb = new DatabaseHelper(this);

        // Setup Toolbar and Menu
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        // --- Bind Views ---
        monthDropdown = findViewById(R.id.spinner_month);
        tilUnit = findViewById(R.id.til_unit);
        etUnit = findViewById(R.id.et_unit);

        // --- AutoCompleteTextView Adapter Setup (Initialization) ---
        // MUST happen before the listener is attached or the text is set.
        String[] months = getResources().getStringArray(R.array.months_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                months
        );

        monthDropdown.setAdapter(adapter);
        monthDropdown.setText(months[0], false); // Set initial default text ("January")
        // ----------------------------------------------------

        // FIX: OnTouchListener to force the dropdown to appear on tap
        // This stops the "appear and gone" bug by explicitly showing the list.
        monthDropdown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    monthDropdown.showDropDown();
                    return true;
                }
                return false;
            }
        });
        // ----------------------------------------------------


        // BINDING TO THE SINGLE, COMBINED RADIO GROUP
        radioGroupRebate = findViewById(R.id.radio_group_rebate);

        tvTotalCharges = findViewById(R.id.tv_total_charges);
        tvFinalCost = findViewById(R.id.tv_final_cost);
        btnCalculateSave = findViewById(R.id.btn_calculate_save);
        btnViewHistory = findViewById(R.id.btn_view_history);

        // Set initial button text
        btnCalculateSave.setText("CALCULATE");

        // --- RadioGroup Listener (Simplified to one group) ---
        // This group no longer needs mutual exclusion logic with another group.

        // --- Button Listeners ---
        btnCalculateSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCalculateSave();
            }
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });
    }

    // Menu Inflation (for About Page)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {

            // FIX: The Intent code for the About Page is now active
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- NEW HANDLER FOR CALCULATE/SAVE LOGIC ---
    private void handleCalculateSave() {
        if (!isCalculated) {
            // State 1: Perform Calculation
            performCalculation();
        } else {
            // State 2: Perform Save
            saveCalculatedBill();
        }
    }

    /**
     * Step 1: Performs validation and calculation, then updates the UI outputs.
     */
    private void performCalculation() {
        // --- 1. Input Validation ---
        String unitStr = etUnit.getText().toString().trim();
        if (unitStr.isEmpty()) {
            tilUnit.setError("Units Used field is required.");
            Toast.makeText(this, "Please enter the electricity units used.", Toast.LENGTH_SHORT).show();
            return;
        }

        double unitUsed;
        try {
            unitUsed = Double.parseDouble(unitStr);
            if (unitUsed < 0) {
                tilUnit.setError("Units cannot be negative.");
                Toast.makeText(this, "Units must be 0 or greater.", Toast.LENGTH_SHORT).show();
                return;
            }
            tilUnit.setError(null);
        } catch (NumberFormatException e) {
            tilUnit.setError("Invalid number format.");
            Toast.makeText(this, "Please enter a valid number for units.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get Month - Correct method for AutoCompleteTextView
        String month = monthDropdown.getText().toString();

        // --- 2. Get Rebate Percentage (from single RadioGroup) ---
        double rebatePercent = 0;
        int checkedId = radioGroupRebate.getCheckedRadioButtonId();

        if (checkedId != -1) {
            // Logic assumes RadioButtons are ordered 0%, 1%, 2%, 3%, 4%, 5% in XML
            View radioButton = radioGroupRebate.findViewById(checkedId);
            int idx = radioGroupRebate.indexOfChild(radioButton);
            rebatePercent = idx; // 0th index is 0%, 1st is 1%, etc.
        } else {
            Toast.makeText(this, "Please select a rebate percentage.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 3. Calculation ---
        // Assuming BillCalculator is an external class with a static calculate method
        double[] results = BillCalculator.calculate(unitUsed, rebatePercent);
        double totalCharges = results[0];
        double finalCost = results[1];

        // --- 4. Output Display ---
        tvTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
        tvFinalCost.setText(String.format("Final Cost (After %.0f%% Rebate): RM %.2f", rebatePercent, finalCost));

        // --- 5. Store results in member variables for later saving ---
        currentMonth = month;
        currentUnitUsed = unitUsed;
        currentRebatePercent = rebatePercent;
        currentTotalCharges = totalCharges;
        currentFinalCost = finalCost;

        // Update state and button text
        isCalculated = true;
        btnCalculateSave.setText("SAVE BILL");
        Toast.makeText(this, "Calculation complete. Click SAVE BILL to proceed.", Toast.LENGTH_LONG).show();
    }

    /**
     * Step 2: Saves the previously calculated bill to the database and resets the view.
     */
    private void saveCalculatedBill() {
        // --- 1. Quick Validation ---
        if (!isCalculated || currentTotalCharges == 0) {
            Toast.makeText(this, "Please calculate the bill first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 2. Store in Local Database ---
        long result = myDb.saveMonthlyBill(
                currentMonth,
                currentUnitUsed,
                currentRebatePercent,
                currentTotalCharges,
                currentFinalCost
        );

        if (result != -1) {
            Toast.makeText(MainActivity.this, "Bill saved successfully! Opening History...", Toast.LENGTH_LONG).show();

            // Reset state and button text
            isCalculated = false;
            btnCalculateSave.setText("CALCULATE");

            clearFieldsAndOutputs(); // Clear inputs AND outputs

            // YOUR REQUIREMENT: Go to Billing History after save
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));

        } else {
            Toast.makeText(MainActivity.this, "Error saving data. Check DatabaseHelper.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Clears input fields AND output TextViews after a successful save.
     */
    private void clearFieldsAndOutputs() {
        etUnit.setText("");
        radioGroupRebate.clearCheck();

        // Correct method to reset AutoCompleteTextView
        String[] months = getResources().getStringArray(R.array.months_array);
        monthDropdown.setText(months[0], false);

        // YOUR REQUIREMENT: Clear the outputs as well
        tvTotalCharges.setText("Total Charges: RM 0.00");
        tvFinalCost.setText("Final Cost (After 0% Rebate): RM 0.00");
    }
}