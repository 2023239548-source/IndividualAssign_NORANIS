package com.example.individualassign_noranis;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent; // <--- CRITICAL FIX: Add this line
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    private DatabaseHelper myDb;
    private long recordId = -1; // To store the ID passed from HistoryActivity

    private TextView tvMonth, tvCharges, tvFinalCost;
    private EditText etUnit, etRebate;
    private Button btnUpdate;
    private ImageButton btnDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Setup action bar title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bill Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        myDb = new DatabaseHelper(this);

        // Bind Views
        tvMonth = findViewById(R.id.tv_detail_month);
        tvCharges = findViewById(R.id.tv_detail_charges);
        tvFinalCost = findViewById(R.id.tv_detail_final_cost);
        etUnit = findViewById(R.id.et_detail_unit);
        etRebate = findViewById(R.id.et_detail_rebate);
        btnUpdate = findViewById(R.id.btn_detail_update);
        btnDelete = findViewById(R.id.btn_detail_delete);

        // Get the ID passed from HistoryActivity
        Intent intent = getIntent(); // This line now works
        recordId = intent.getLongExtra("RECORD_ID", -1);

        if (recordId != -1) {
            loadData();
        } else {
            Toast.makeText(this, "Error: No record ID found.", Toast.LENGTH_LONG).show();
            finish();
        }

        // --- Listeners ---
        btnUpdate.setOnClickListener(v -> updateRecord());
        btnDelete.setOnClickListener(v -> deleteRecord());
    }

    private void loadData() {
        Cursor cursor = myDb.getBillById(recordId);

        if (cursor != null && cursor.moveToFirst()) {

            // Get data from Cursor
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH));
            double unit = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNIT));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_CHARGES));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FINAL_COST));

            // Display data
            tvMonth.setText("Bill Details for " + month);

            // FIX 1: Format Units Used (kWh) as a simple String (often still shows .0)
            // It's best to use String.format or casting for clean integer display.

            // If you want units to show 350, not 350.0:
            etUnit.setText(String.format("%.0f", unit));


            // FIX 2: CRITICAL CHANGE FOR REBATE PERCENTAGE
            // Use String.format("%.0f", rebate) to convert the double (e.g., 5.0)
            // into a string without the decimal (e.g., "5").
            etRebate.setText(String.format("%.0f", rebate));


            // The rest of the display remains the same:
            tvCharges.setText(String.format("Total Charges: RM %.2f", total));
            tvFinalCost.setText(String.format("Final Cost: RM %.2f", finalCost));

            cursor.close();
        } else {
            Toast.makeText(this, "Record not found.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateRecord() {
        // Clear previous errors first
        etUnit.setError(null);
        etRebate.setError(null);

        // 1. Get updated input
        String unitStr = etUnit.getText().toString();
        String rebateStr = etRebate.getText().toString();
        String month = tvMonth.getText().toString().replace("Bill Details for ", "").trim();

        if (unitStr.isEmpty() || rebateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double unit = Double.parseDouble(unitStr);
            double rebate = Double.parseDouble(rebateStr);

            boolean hasError = false;

            // --- VALIDATION FOR WHOLE NUMBER REBATE (0 to 5) ---

            // 1. Check if the value is a whole number (no decimals)
            if (rebate != (int) rebate) {
                etRebate.setError("Rebate must be a whole number (0, 1, 2, 3, 4, or 5).");
                hasError = true;
            }

            // 2. Check the range (0 to 5)
            else if (rebate < 0 || rebate > 5) {
                etRebate.setError("Rebate must be between 0 and 5.");
                hasError = true;
            }

            // Additional check for Units Used
            if (unit < 0) {
                etUnit.setError("Units Used cannot be negative.");
                hasError = true;
            }

            // If any error occurred, stop the update process and return
            if (hasError) {
                return;
            }
            // ---------------------------------------------------

            // 2. Recalculate based on new inputs
            double[] results = BillCalculator.calculate(unit, rebate);
            double totalCharges = results[0];
            double finalCost = results[1];

            // 3. Update database
            boolean success = myDb.updateBill(recordId, month, unit, rebate, totalCharges, finalCost);

            if (success) {
                Toast.makeText(this, "Record updated successfully!", Toast.LENGTH_LONG).show();
                loadData();
            } else {
                Toast.makeText(this, "Error updating record.", Toast.LENGTH_LONG).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format for Unit or Rebate.", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteRecord() {
        boolean success = myDb.deleteBill(recordId);

        if (success) {
            Toast.makeText(this, "Record deleted successfully!", Toast.LENGTH_LONG).show();
            // Close the detail activity and return to HistoryActivity
            finish();
        } else {
            Toast.makeText(this, "Error deleting record.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}