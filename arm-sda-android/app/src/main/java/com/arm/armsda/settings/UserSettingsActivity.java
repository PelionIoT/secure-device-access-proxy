package com.arm.armsda.settings;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.arm.armsda.R;
import com.arm.armsda.data.ApplicationData;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.utils.AndroidUtils;

import org.json.simple.JSONObject;

public class UserSettingsActivity extends AppCompatActivity {

    private static final String appDataSharedPreferencesKeyValue = "appDataDetails";
    private ApplicationData applicationData;
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        EditText accountIdInput = findViewById(R.id.accountIdEditText);
        EditText cloudUrlInput = findViewById(R.id.cloudUrlEditText);
        Button okButton = findViewById(R.id.userSettingsOkButton);

        JSONObject jsonStored =  dataHandler.getJsonStringData(
                appDataSharedPreferencesKeyValue,
                UserSettingsActivity.this);

        if (null != jsonStored) {
            applicationData = new ApplicationData(jsonStored);
            accountIdInput.setText(applicationData.getAccountId());
            cloudUrlInput.setText(applicationData.getCloudUrl());
        } else {
            Log.d("UserSettingsActivity", "oops something bad happened");
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject jsonStored =  dataHandler.getJsonStringData(
                        appDataSharedPreferencesKeyValue,
                        UserSettingsActivity.this);

                if (null != jsonStored) {
                    applicationData = new ApplicationData(jsonStored);

                    //For safety we save the configuration again. Demo wont be empty in the next activity
                    applicationData = new ApplicationData(
                            applicationData.getDemoMode(),
                            accountIdInput.getText().toString(),
                            cloudUrlInput.getText().toString());

                    dataHandler.saveJsonStringData(
                            appDataSharedPreferencesKeyValue,
                            applicationData.toJsonObject(),
                            UserSettingsActivity.this);
                }
                AndroidUtils.customToast(
                        getApplicationContext(),
                        getString(R.string.settings_saved),
                        Color.GREEN
                );
                finish();
            }
        });

    }
}
