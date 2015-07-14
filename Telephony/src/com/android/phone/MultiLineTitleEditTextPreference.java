/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.phone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Ultra-simple subclass of EditTextPreference that allows the "title" to wrap
 * onto multiple lines.
 *
 * (By default, the title of an EditTextPreference is singleLine="true"; see
 * preference_holo.xml under frameworks/base.  But in the "Respond via SMS"
 * settings UI we want titles to be multi-line, since the customized messages
 * might be fairly long, and should be able to wrap.)
 *
 * TODO: This is pretty cumbersome; it would be nicer for the framework to
 * either allow modifying the title's attributes in XML, or at least provide
 * some way from Java (given an EditTextPreference) to reach inside and get a
 * handle to the "title" TextView.
 *
 * TODO: Also, it would reduce clutter if this could be an inner class in
 * RespondViaSmsManager.java, but then there would be no way to reference the
 * class from XML.  That's because
 *    <com.android.phone.RespondViaSmsManager$MultiLineTitleEditTextPreference ... />
 * isn't valid XML syntax due to the "$" character.  And Preference
 * elements don't have a "class" attribute, so you can't do something like
 * <view class="com.android.phone.Foo$Bar"> as you can with regular views.
 */
public class MultiLineTitleEditTextPreference extends EditTextPreference {
    public MultiLineTitleEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MultiLineTitleEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiLineTitleEditTextPreference(Context context) {
        super(context);
    }

    // The "title" TextView inside an EditTextPreference defaults to
    // singleLine="true" (see preference_holo.xml under frameworks/base.)
    // We override onBindView() purely to look up that TextView and call
    // setSingleLine(false) on it.
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView textView = (TextView) view.findViewById(com.android.internal.R.id.title);
        if (textView != null) {
            textView.setSingleLine(false);
        }
        updatePositiveButton();
    }

    //*********************************************MTK*********************************************
    /**
     * when activity recreate because of some action (ex: change language), update button.
     * ALPS01547093
     */
    private void updatePositiveButton() {
        AlertDialog dialog = ((AlertDialog)getDialog());
        if (dialog != null) {
            final Button button = dialog.getButton(Dialog.BUTTON_POSITIVE);
            if (button != null) {
                TextView textView2 = getEditText();
                if (textView2 != null) {
                    Editable editable = textView2.getEditableText();
                    String text = editable.toString();
                    button.setEnabled(!text.isEmpty());
                }
                textView2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();
                        Log.d("MultiLineTitleEditTextPreference", "updatePositiveButton... + text: " + text);
                        button.setEnabled(!text.trim().isEmpty());
                    }
                });
            }
        }
    }
}
