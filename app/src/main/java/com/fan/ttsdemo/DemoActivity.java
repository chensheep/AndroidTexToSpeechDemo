package com.fan.ttsdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fan.ttsdemo.utils.IntentUtils;

public class DemoActivity extends AppCompatActivity {

    Button button;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          Intent intent = IntentUtils.createExplicitFromImplicitIntent(getApplicationContext(), new Intent("com.fan.ttsdemo.tts"));
                                          if (intent != null) {
                                              intent.putExtra("play-message", ""+editText.getEditableText());
                                              startService(intent);
                                          }
                                      }
                                  });
        editText = findViewById(R.id.editText);
    }
}
