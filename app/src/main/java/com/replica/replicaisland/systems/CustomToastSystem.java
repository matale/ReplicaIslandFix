/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.replica.replicaisland.systems;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.replica.replicaisland.misc.BaseObject;
import com.replica.replicaisland.R;

public class CustomToastSystem extends BaseObject {
    private final View mView;
    private final TextView mText;
    private final Toast mToast;

    public CustomToastSystem(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.custom_toast, null);

        mText = mView.findViewById(R.id.text);
        mToast = new Toast(context);
        mToast.setView(mView);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    public void toast(String text, int length) {
        mText.setText(text);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(length);
        mToast.show();
    }
}
