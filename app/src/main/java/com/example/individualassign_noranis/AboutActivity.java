package com.example.individualassign_noranis;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 1. Toolbar Setup
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About Student");
        }

        // Inside your onCreate method in AboutActivity.java:
        TextView tvWebsite = findViewById(R.id.tv_website_url);
        if (tvWebsite != null) {
            tvWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Updated to your specific URL
                    String url = "https://github.com/2023239548-source/ElectricityBillApp";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
    }

    // 3. Handle Toolbar Back Arrow Click
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes AboutActivity and returns to MainActivity
        return true;
    }
}