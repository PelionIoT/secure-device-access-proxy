// ----------------------------------------------------------------------------
//   The confidential and proprietary information contained in this file may
//   only be used by a person authorized under and to the extent permitted
//   by a subsisting licensing agreement from ARM Limited or its affiliates.
//
//          (C)COPYRIGHT 2018 ARM Limited or its affiliates.
//              ALL RIGHTS RESERVED
//
//   This entire notice must be reproduced on all copies of this file
//   and copies of this file may only be made by a person if such person is
//   permitted to do so under the terms of a subsisting license agreement
//   from ARM Limited or its affiliates.
// ----------------------------------------------------------------------------
package com.arm.armsda.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.arm.armsda.R;
import com.arm.armsda.data.IDataHandler;
import com.arm.armsda.data.LedConfiguration;
import com.arm.armsda.data.SharedPreferencesHandleData;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private Spinner mLedDropdown;
    private Spinner mColorDropdown;
    private LedConfiguration ledConfiguration = new LedConfiguration();

    private static final String sharedPreferencesKeyValue = "ledDetails";
    private static IDataHandler dataHandler = new SharedPreferencesHandleData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getDate();

        //get the spinner from the xml.
        mLedDropdown = findViewById(R.id.LedSpinner);
        //create a list of items for the spinner.
        ArrayList<String> ledItems = ledConfiguration.getLedTypes();
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> ledAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ledItems);
        //set the spinners adapter to the previously created one.
        mLedDropdown.setAdapter(ledAdapter);
        mLedDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                switch (position) {
                    case 0:
                        ledConfiguration.setLedType("POWER");
                        break;
                    case 1:
                        ledConfiguration.setLedType("WIFI");
                        break;
                    case 2:
                        ledConfiguration.setLedType("CLOUD");
                        break;
                    case 3:
                        ledConfiguration.setLedType("FWUP");
                        break;
                    case 4:
                        ledConfiguration.setLedType("LIGHT");
                        break;
                    case 5:
                        ledConfiguration.setLedType("TEMP");
                        break;
                    case 6:
                        ledConfiguration.setLedType("HUMIDITY");
                        break;
                    case 7:
                        ledConfiguration.setLedType("SOUND");
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        mLedDropdown.setSelection(ledConfiguration.getLedTypeIndex());

        //get the spinner from the xml.
        mColorDropdown = findViewById(R.id.ColorSpinner);
        //create a list of items for the spinner.
        ArrayList<String> colorItems = ledConfiguration.getLedColors();
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colorItems);
        //set the spinners adapter to the previously created one.
        mColorDropdown.setAdapter(colorAdapter);
        mColorDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                switch (position) {
                    case 0:
                        ledConfiguration.setLedColor("GREEN");
                        break;
                    case 1:
                        ledConfiguration.setLedColor("RED");
                        break;
                    case 2:
                        ledConfiguration.setLedColor("ORANGE");
                        break;
                    case 3:
                        ledConfiguration.setLedColor("PURPLE");
                        break;
                    case 4:
                        ledConfiguration.setLedColor("SNOW_YELLOW");
                        break;
                }
            }
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
        mColorDropdown.setSelection(ledConfiguration.getLedColorIndex());

    }

    private void saveData() {

        dataHandler.saveJsonStringData(
                sharedPreferencesKeyValue,
                ledConfiguration.toJsonObject(),
                SettingsActivity.this);
    }

    private void getDate() {
        //Retrieve login details from SharedRef
        JSONObject jsonStored =  dataHandler.getJsonStringData(
                sharedPreferencesKeyValue,
                SettingsActivity.this);

        //Set login details from SharedRef if exist
        if (null != jsonStored) {
            ledConfiguration = new LedConfiguration(jsonStored);
        }
    }

    @Override
    protected void onStop()
    {
        saveData();
        super.onStop();
    }

}
