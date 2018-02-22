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
package com.arm.armsda.utils;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidUtils {

    public static void customToastWithTimer(Context context, String text, int color , int duration) {

        Toast toast = Toast.makeText(context, text, duration);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(color);
        toast.show();
    }

    public static void customToast(Context context, String text, int color) {

        customToastWithTimer(context, text, color, Toast.LENGTH_SHORT);
    }

}
