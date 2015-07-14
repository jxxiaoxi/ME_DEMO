/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.mediatek.incallui.volte;

import com.android.incallui.BaseFragment;
import com.android.incallui.CallList;
import com.android.incallui.R;
import com.mediatek.incallui.volte.VoLteConfUiController.MemberChangeNotifier;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment for call control buttons
 */
public class VoLteConfManagerFragment
        extends BaseFragment<VoLteConfManagerPresenter,
        VoLteConfManagerPresenter.ConferenceManagerUi>
        implements VoLteConfManagerPresenter.ConferenceManagerUi {

    private View mButtonManageConferenceDone;
    private ViewGroup[] mConferenceCallList;
    private Chronometer mConferenceTime;

    @Override
    public
    VoLteConfManagerPresenter createPresenter() {
        // having a singleton instance.
        return new VoLteConfManagerPresenter();
    }

    @Override
    public
    VoLteConfManagerPresenter.ConferenceManagerUi getUi() {
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.mtk_volte_conference_manager_fragment, container,
                false);

        /// M: for ALPS01256945, avoid touch event pass to the layer under it. @{
        parent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // just return true to avoid the touch event pass to the layer under it.
                return true;
            }
        });
        /// @}

        // set up the Conference Call chronometer
        mConferenceTime = (Chronometer) parent.findViewById(R.id.manageConferencePanelHeader);
        mConferenceTime.setFormat(getActivity().getString(R.string.caller_manage_header));

        // Create list of conference call widgets
        mConferenceCallList = new ViewGroup[getPresenter().getMaxCallersInConference()];

        final int[] viewGroupIdList = { R.id.caller0, R.id.caller1, R.id.caller2,
                                        R.id.caller3, R.id.caller4 };
        for (int i = 0; i < getPresenter().getMaxCallersInConference(); i++) {
            mConferenceCallList[i] =
                    (ViewGroup) parent.findViewById(viewGroupIdList[i]);
        }

        mButtonManageConferenceDone = parent.findViewById(R.id.manage_done);
        mButtonManageConferenceDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().manageConferenceDoneClicked();
            }
        });

        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setVisible(boolean on) {
        if (on) {
            final CallList calls = CallList.getInstance();
            getPresenter().init(getActivity(), calls);
            getView().setVisibility(View.VISIBLE);

        } else {
            getView().setVisibility(View.GONE);
        }
    }

    @Override
    public boolean isFragmentVisible() {
        return isVisible();
    }

    @Override
    public void setRowVisible(int rowId, boolean on) {
        if (on) {
            mConferenceCallList[rowId].setVisibility(View.VISIBLE);
        } else {
            mConferenceCallList[rowId].setVisibility(View.GONE);
        }
    }

    /**
     * Helper function to fill out the Conference Call(er) information
     * for each item in the "Manage Conference Call" list.
     */
    @Override
    public final void displayCallerInfoForConferenceRow(int rowId, String callerName,
            String callerNumber, String callerNumberType) {

        final TextView nameTextView = (TextView) mConferenceCallList[rowId].findViewById(
                R.id.conferenceCallerName);
        final TextView numberTextView = (TextView) mConferenceCallList[rowId].findViewById(
                R.id.conferenceCallerNumber);
        final TextView numberTypeTextView = (TextView) mConferenceCallList[rowId].findViewById(
                R.id.conferenceCallerNumberType);

        // set the caller name
        nameTextView.setText(callerName);

        // set the caller number in subscript, or make the field disappear.
        if (TextUtils.isEmpty(callerNumber)) {
            numberTextView.setVisibility(View.GONE);
            numberTypeTextView.setVisibility(View.GONE);
        } else {
            numberTextView.setVisibility(View.VISIBLE);
            numberTextView.setText(callerNumber);
            numberTypeTextView.setVisibility(View.VISIBLE);
            numberTypeTextView.setText(callerNumberType);
        }
    }

    @Override
    public final void setupRemoveButtonForRow(final int rowId, boolean canRemove) {
        View removeButton = mConferenceCallList[rowId].findViewById(R.id.conferenceCallerDisconnect);
        if (canRemove) {
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPresenter().endConferenceConnection(rowId);
                }
            });
        } else {
            removeButton.setVisibility(View.GONE);
        }
    }

    @Override
    public final void setStatusForRow(final int rowId, boolean isOnLine) {
        final ImageView statusIcon = (ImageView) mConferenceCallList[rowId].findViewById(
                R.id.status);

        if (isOnLine) {
            statusIcon.setImageResource(R.drawable.mtk_ic_volte_conference_call_status_online);
        } else {
            statusIcon.setImageResource(R.drawable.mtk_ic_volte_conference_call_status_onhold);
        }
    }

    /**
     * Starts the "conference time" chronometer.
     */
    @Override
    public void startConferenceTime(long base) {
        if (mConferenceTime != null) {
            mConferenceTime.setBase(base);
            mConferenceTime.start();
        }
    }

    /**
     * Stops the "conference time" chronometer.
     */
    @Override
    public void stopConferenceTime() {
        if (mConferenceTime != null) {
            mConferenceTime.stop();
        }
    }

    @Override
    public void notifyMemberChange(int notifyType, String name) {
        MemberChangeNotifier.notifyMemberChange(getActivity(), notifyType, name);
    }

}
