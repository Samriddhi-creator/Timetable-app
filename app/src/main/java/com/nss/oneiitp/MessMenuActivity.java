package com.nss.oneiitp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.RelativeSizeSpan;
import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

public class MessMenuActivity extends AppCompatActivity {

    private TextView menuText;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_menu);

        menuText = findViewById(R.id.menuText);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadMessMenu();
    }

    private void loadMessMenu() {
        try {
            InputStream is = getResources().openRawResource(R.raw.mess_menu);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                builder.append("📅 ").append(obj.getString("day").toUpperCase()).append("\n\n");

                builder.append("🍳 Breakfast\n");
                builder.append(obj.optString("breakfast", "N/A")).append("\n\n");

                builder.append("🍛 Lunch\n");
                builder.append(obj.optString("lunch", "N/A")).append("\n\n");

                builder.append("🍟 Snacks\n");
                builder.append(obj.optString("snacks", "N/A")).append("\n\n");

                builder.append("🍽 Dinner\n");
                builder.append(obj.optString("dinner", "N/A")).append("\n\n");

                builder.append("------------------------------------------\n\n");
            }

            String result = builder.toString();
            SpannableString spannable = new SpannableString(result);

            // Highlight Days
            String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY", "EVERYDAY"};
            for (String day : days) {
                int index = result.indexOf(day);
                while (index >= 0) {
                    spannable.setSpan(new StyleSpan(Typeface.BOLD), index, index + day.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new RelativeSizeSpan(1.5f), index, index + day.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#1976D2")), index, index + day.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    index = result.indexOf(day, index + day.length());
                }
            }

            // Highlight Meal Types
            String[] meals = {"Breakfast", "Lunch", "Snacks", "Dinner"};
            for (String meal : meals) {
                int start = 0;
                while ((start = result.indexOf(meal, start)) >= 0) {
                    spannable.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, start + meal.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#388E3C")), start, start + meal.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start += meal.length();
                }
            }

            menuText.setText(spannable);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
