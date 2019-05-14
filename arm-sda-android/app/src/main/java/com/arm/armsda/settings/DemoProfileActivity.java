package com.arm.armsda.settings;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.arm.armsda.R;
import com.arm.armsda.data.ApplicationData;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.SharedPreferencesHandleData;
import com.arm.armsda.utils.AndroidUtils;

import org.json.simple.JSONObject;

public class DemoProfileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String appDataSharedPreferencesKeyValue = "appDataDetails";
    private ApplicationData applicationData;
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_profile);

        ListView listview = (ListView) findViewById(R.id.demoListView);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("DemoProfileActivity", "You clicked Item: " + id + " at position:" + position);

        String msg;
        switch (position) {
            case 0:
                setDemoProfile(ApplicationData.HANNOVER_MESSE);
                msg = "Hannover Messe" + " " +  getString(R.string.settings_set_demo);
                AndroidUtils.customToast(
                        getApplicationContext(),
                        msg,
                        Color.GREEN
                );
                finish();
                break;
            case 1:
                setDemoProfile(ApplicationData.MWC);
                msg = "Mwc" + " " +  getString(R.string.settings_set_demo);
                AndroidUtils.customToast(
                        getApplicationContext(),
                        msg,
                        Color.GREEN
                );
                finish();
                break;
        }
    }

    private void setDemoProfile(String profile) {

        JSONObject jsonStored =  dataHandler.getJsonStringData(
                appDataSharedPreferencesKeyValue,
                DemoProfileActivity.this);

        if (null != jsonStored) {
            applicationData = new ApplicationData(jsonStored);

            //For safety we save the configuration again. Demo wont be empty in the next activity
            applicationData = new ApplicationData(
                    profile,
                    applicationData.getAccountId(),
                    applicationData.getCloudUrl());

            dataHandler.saveJsonStringData(
                    appDataSharedPreferencesKeyValue,
                    applicationData.toJsonObject(),
                    DemoProfileActivity.this);

        } else {
            applicationData = new ApplicationData(
                    profile,
                    getString(R.string.account_id),
                    getString(R.string.env_url));

            dataHandler.saveJsonStringData(
                    appDataSharedPreferencesKeyValue,
                    applicationData.toJsonObject(),
                    DemoProfileActivity.this);
        }
    }

}
