package com.nss.oneiitp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.RelativeSizeSpan;
import android.graphics.Typeface;

public class MessMenuActivity extends AppCompatActivity {

    TextView menuText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_menu);

        menuText = findViewById(R.id.menuText);

        try {

            InputStream is = getResources().openRawResource(R.raw.mess_menu);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer);

            JSONArray jsonArray = new JSONArray(json);

            String result = "";

            for(int i = 0; i < jsonArray.length(); i++) {

                JSONObject obj = jsonArray.getJSONObject(i);

                result += "📅 " + obj.getString("day") + "\n\n";

                result += "🍳 Breakfast\n";
                result += obj.getString("breakfast") + "\n\n";

                result += "🍛 Lunch\n";
                result += obj.getString("lunch") + "\n\n";

                result += "🍟 Snacks\n";
                result += obj.getString("snacks") + "\n\n";

                result += "🍽 Dinner\n";
                result += obj.getString("dinner") + "\n\n";

                result += "--------------------------------\n\n";
            }

            SpannableString spannable = new SpannableString(result);

// Highlight Days (MONDAY etc.)
            String[] days = {"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY","EVERYDAY"};

            for(String day : days){
                int index = result.indexOf(day);
                if(index >= 0){
                    spannable.setSpan(new StyleSpan(Typeface.BOLD),
                            index, index + day.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    spannable.setSpan(new RelativeSizeSpan(1.4f),
                            index, index + day.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

// Highlight Breakfast/Lunch/Snacks/Dinner
            String[] meals = {"Breakfast","Lunch","Snacks","Dinner"};

            for(String meal : meals){
                int start = 0;
                while((start = result.indexOf(meal, start)) >= 0){
                    spannable.setSpan(new StyleSpan(Typeface.BOLD),
                            start, start + meal.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start += meal.length();
                }
            }

            menuText.setText(spannable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
