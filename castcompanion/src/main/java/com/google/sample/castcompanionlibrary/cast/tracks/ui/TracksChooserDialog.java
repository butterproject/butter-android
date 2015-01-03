/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
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

package com.google.sample.castcompanionlibrary.cast.tracks.ui;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaTrack;
import com.google.sample.castcompanionlibrary.R;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog to show the available tracks (Text, Audio and Video) to allow selection of tracks.
 */
@SuppressLint("ValidFragment")
public class TracksChooserDialog extends DialogFragment {

    private static final String TAG = "TracksChooserDialog";
    private VideoCastManager mCastManager;
    private long[] mActiveTracks = null;
    private OnTracksSelectedListener mListener;
    private MediaInfo mMediaInfo;
    private TracksListAdapter mTextAdapter;
    private TracksListAdapter mAudioVideoAdapter;
    private List<MediaTrack> mTextTracks = new ArrayList<MediaTrack>();
    private List<MediaTrack> mAudioVideoTracks = new ArrayList<MediaTrack>();
    private static final long TEXT_TRACK_NONE_ID = -1;
    private static final MediaTrack TEXT_TRACK_NONE = new MediaTrack.Builder(TEXT_TRACK_NONE_ID,
            MediaTrack.TYPE_TEXT)
            .setName("None")
            .setSubtype(MediaTrack.SUBTYPE_CAPTIONS)
            .setContentId("").build();
    private int mSelectedTextPosition = 0;
    private int mSelectedAudioPosition = -1;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_tracks_dialog_layout, null);
        setupView(view);

        builder.setView(view)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        if (mListener == null) {
                            return;
                        }
                        List<MediaTrack> selectedTracks = new ArrayList<MediaTrack>();
                        MediaTrack textTrack = mTextAdapter.getSelectedTrack();
                        if (textTrack.getId() != TEXT_TRACK_NONE_ID) {
                            selectedTracks.add(textTrack);
                        }
                        MediaTrack audioVideoTrack = mAudioVideoAdapter.getSelectedTrack();
                        if (null != audioVideoTrack) {
                            selectedTracks.add(audioVideoTrack);
                        }
                        mListener.onTracksSelected(selectedTracks);
                        mListener = null;
                        TracksChooserDialog.this.getDialog().cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener = null;
                        TracksChooserDialog.this.getDialog().cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mListener = null;
                        TracksChooserDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * This is to get around the following bug:
     * https://code.google.com/p/android/issues/detail?id=17423
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void setupView(View view) {
        ListView listView1 = (ListView) view.findViewById(R.id.listview1);
        ListView listView2 = (ListView) view.findViewById(R.id.listview2);
        TextView textEmptyMessageView = (TextView) view.findViewById(R.id.text_empty_message);
        TextView audioEmptyMessageView = (TextView) view.findViewById(R.id.audio_empty_message);
        partitionTracks();

        mTextAdapter = new TracksListAdapter(getActivity(), R.layout.tracks_row_layout,
                mTextTracks, mSelectedTextPosition);
        mAudioVideoAdapter = new TracksListAdapter(getActivity(), R.layout.tracks_row_layout,
                mAudioVideoTracks, mSelectedAudioPosition);

        listView1.setAdapter(mTextAdapter);
        listView2.setAdapter(mAudioVideoAdapter);

        TabHost tabs = (TabHost) view.findViewById(R.id.tabhost);
        tabs.setup();

        // create tab 1
        TabHost.TabSpec tab1 = tabs.newTabSpec("tab1");
        if (mTextTracks == null || mTextTracks.isEmpty()) {
            listView1.setVisibility(View.INVISIBLE);
            tab1.setContent(R.id.text_empty_message);
        } else {
            textEmptyMessageView.setVisibility(View.INVISIBLE);
            tab1.setContent(R.id.listview1);
        }
        tab1.setIndicator(getString(R.string.caption_subtitles));
        tabs.addTab(tab1);

        // create tab 2
        TabHost.TabSpec tab2 = tabs.newTabSpec("tab2");
        if (mAudioVideoTracks == null || mAudioVideoTracks.isEmpty()) {
            listView2.setVisibility(View.INVISIBLE);
            tab2.setContent(R.id.audio_empty_message);
        } else {
            audioEmptyMessageView.setVisibility(View.INVISIBLE);
            tab2.setContent(R.id.listview2);
        }
        tab2.setIndicator(getString(R.string.caption_audio));
        tabs.addTab(tab2);
    }

    private void partitionTracks() {
        List<MediaTrack> allTracks = mMediaInfo.getMediaTracks();
        mAudioVideoTracks.clear();
        mTextTracks.clear();
        mTextTracks.add(TEXT_TRACK_NONE);
        mSelectedTextPosition = 0;
        mSelectedAudioPosition = -1;
        if (allTracks != null) {
            int textPosition = 1; /* start from 1 since we have a NONE selection at the beginning */
            int audioPosition = 0;
            for (MediaTrack track : allTracks) {
                switch (track.getType()) {
                    case MediaTrack.TYPE_TEXT:
                        mTextTracks.add(track);
                        if (mActiveTracks != null) {
                            for(int i=0; i < mActiveTracks.length; i++) {
                                if (mActiveTracks[i] == track.getId()) {
                                    mSelectedTextPosition = textPosition;
                                }
                            }
                        }
                        textPosition++;
                        break;
                    case MediaTrack.TYPE_AUDIO:
                    case MediaTrack.TYPE_VIDEO:
                        mAudioVideoTracks.add(track);
                        if (mActiveTracks != null) {
                            for(int i=0; i < mActiveTracks.length; i++) {
                                if (mActiveTracks[i] == track.getId()) {
                                    mSelectedAudioPosition = audioPosition;
                                }
                            }
                        }
                        audioPosition++;
                        break;
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        List<MediaTrack> allTracks = mMediaInfo.getMediaTracks();
        if (allTracks == null || allTracks.isEmpty()) {
            Utils.showToast(activity, R.string.caption_no_tracks_available);
            dismiss();
        }
    }

    public TracksChooserDialog(MediaInfo mediaInfo, OnTracksSelectedListener listener) {
        mMediaInfo = mediaInfo;
        mListener = listener;
        try {
            mCastManager = VideoCastManager.getInstance();
            mActiveTracks = mCastManager.getActiveTrackIds();
        } catch (CastException e) {
            LOGE(TAG, "Failed to get an instance of VideoCatManager", e);
        }
    }

    /**
     * An interface that would be used to inform
     * {@link com.google.sample.castcompanionlibrary.cast.tracks.ui.TracksChooserDialog.OnTracksSelectedListener}
     * listeners when user is changing the active tracks for a media.
     */
    public interface OnTracksSelectedListener {

        /**
         * Called to inform the listeners of the new set of active tracks, set by the user.
         *
         * @param tracks A Non-<code>null</code> list of MediaTracks.
         */
        public void onTracksSelected(List<MediaTrack> tracks);
    }
}
