package com.example.individualassign_noranis;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper myDb;
    private ListView historyListView; // Existing member variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Setup action bar title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Billing History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        myDb = new DatabaseHelper(this);

        // Fix: Use the ID from your activity_history.xml
        historyListView = findViewById(R.id.list_view_history);

        loadHistoryData();
    }

    // Ensures data is refreshed every time the user comes back to this activity
    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData();
    }

    private void loadHistoryData() {
        Cursor cursor = myDb.getAllBillsCursor();

        // *** CRITICAL FIX: The check for 'cursor == null || cursor.getCount() == 0'
        // and the Toast/Return logic MUST BE REMOVED.
        // The code should proceed, passing the cursor (even if empty) to the adapter. ***

        if (cursor == null) {
            // Simple safety check if myDb returns null
            return;
        }

        String[] fromColumns = {
                DatabaseHelper.COLUMN_MONTH,
                DatabaseHelper.COLUMN_FINAL_COST
        };

        int[] toViews = {
                R.id.tv_list_month,
                R.id.tv_list_final_cost
        };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_bill,
                cursor, // Pass the cursor, even if empty
                fromColumns,
                toViews,
                0
        );

        // **********************************************
        // ********* VIEW BINDER LOGIC (Keep this) *********
        // **********************************************
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(DatabaseHelper.COLUMN_FINAL_COST)) {
                    if (view instanceof TextView) {
                        TextView textView = (TextView) view;
                        double finalCost = cursor.getDouble(columnIndex);
                        textView.setText(String.format("RM %.2f", finalCost));
                        return true;
                    }
                }
                return false;
            }
        });
        // **********************************************

        historyListView.setAdapter(adapter);

        // Set the ListView's empty view ID
        // Although the @android:id/empty should be picked up automatically,
        // it's good practice to set it manually if it fails sometimes.
        TextView emptyView = findViewById(android.R.id.empty);
        if (emptyView != null) {
            historyListView.setEmptyView(emptyView);
        }

        // Implement Clickable List Item (Opens DetailActivity)
        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("RECORD_ID", id);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}