
package de.wangchao.musicplayer.activity;

import de.wangchao.musicplayer.lyric.LyricView;
import de.wangchao.musicplayer.lyric.PlayListItem;
import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.download.DownloadTask;
import de.wangchao.musicplayer.download.DownloadTaskListener;


import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;

import de.wangchao.musicplayer.util.ImageCache;
import de.wangchao.musicplayer.util.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.MalformedURLException;



public class MediaPlayerActivity extends Activity {
    /************************************************************************/
    /* CONSTANTS */
    /************************************************************************/
    private static boolean DEBUG = true;
    private static String TAG = "OnlineMusicTag";
    private static final String LRC_PATH = Environment.getExternalStorageDirectory()+"/onlinemusic/lrc/";

    /************************************************************************/
    /* CONSTANTS Represent Views */
    /************************************************************************/
    private TextView mStateTextView;
    private ImageButton mPrevButton;
    private ImageButton mPauseButton;
    private ImageButton mNextButton;
    private Button mShuffleButton;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private ProgressBar mProgress;
    private LyricView mLyricView;
    private TextView mTrackNameView;
    private TextView mSingerNameView;
    private ProgressDialog progressDialog;
    private ProgressDialog loadingDialog;

    private ImageView mSongImageView;

    private Drawable mShffleDrawableTop, mSequenceDrawableTop;

    /************************************************************************/
    /* CONSTANTS Represent Music related services , handler , values */
    /************************************************************************/
    MusicService mService;
    boolean mBound = false;

    PlaybackHandler mHandler = new PlaybackHandler();

    private long mPosOverride = -1;
    private boolean mFromTouch = false;
    private long mDuration;
    private boolean paused;
    private long mLastSeekEventTime;

    private static final int REFRESH_TIME = 1;
    private static final int REFRESH_LRC = 5;

    /************************************************************************/
    /* CONSTANTS deal with Lyric process */
    /************************************************************************/
    private boolean mIsLrcReady = false;
    LyricHandler mLyricHandler = new LyricHandler();
    private long mLrcTime = 0;
    private DownloadTask mDownloadTask;

    /************************************************************************/
    /* METHODS - core Service lifecycle methods */
    /************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        this.setContentView(R.layout.media_audio);
        
        mCurrentTime = (TextView) findViewById(R.id.currenttime);
        mTotalTime = (TextView) findViewById(R.id.totaltime);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        mTrackNameView = (TextView) findViewById(R.id.media_trackname);
        mSingerNameView = (TextView) findViewById(R.id.media_singer);

        mPrevButton = (ImageButton) findViewById(R.id.prev);
        mPauseButton = (ImageButton) findViewById(R.id.pause);
        mNextButton = (ImageButton) findViewById(R.id.next);

       
        mShuffleButton = (Button) findViewById(R.id.btn_shuffle);
        mShffleDrawableTop = getResources().getDrawable(R.drawable.btn_action_shuffle);
        mShffleDrawableTop.setBounds(0, 0, mShffleDrawableTop.getMinimumWidth(),
                mShffleDrawableTop.getMinimumHeight());
        mSequenceDrawableTop = getResources().getDrawable(R.drawable.btn_action_sequence);
        mSequenceDrawableTop.setBounds(0, 0, mSequenceDrawableTop.getMinimumWidth(),
                mSequenceDrawableTop.getMinimumHeight());

     
        mStateTextView = (TextView) this.findViewById(R.id.media_state);

        if (mProgress instanceof SeekBar) {
            SeekBar seeker = (SeekBar) mProgress;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
        mProgress.setMax(1000);

        mLyricView = (LyricView) findViewById(R.id.media_lrcview);

   
        mSongImageView = (ImageView) findViewById(R.id.media_song_image);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        
    }

    @Override
    protected void onStart() {

        super.onStart();
        //Tools.debugLog(TAG, "onStart");

        // Bind to music service
        Intent intent = new Intent(this, MusicService.class);
        this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        IntentFilter f = new IntentFilter();
        f.addAction(MusicService.PREPARED_CHANGED);
        f.addAction(MusicService.PLAYSTATE_CHANGED);
        f.addAction(MusicService.CACHESTATE_CHANGED);
        f.addAction(MusicService.META_CHANGED);
        f.addAction(MusicService.SHUFFLEMODE_CHANGED);
        registerReceiver(mStatusListener, new IntentFilter(f));

        updateTrackInfo();
        long next = refreshNow();
        queueNextRefresh(next);
    }

    @Override
    public void onResume() {

        super.onResume();
        //MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {

        super.onPause();
        //MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {

        super.onStop();
        /*
         * if (mBound) { this.unbindService(mConnection); mBound = false; }
         * mService = null;
         */

        unregisterReceiver(mStatusListener);
        mHandler.removeCallbacksAndMessages(null);
        mLyricHandler.removeCallbacksAndMessages(null);
        this.finish();
        Tools.debugLog(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mBound) {
            this.unbindService(mConnection);
            mBound = false;
        }
    }

    /************************************************************************/
    /* BroadcastReceiver - response to state changes */
    /************************************************************************/
    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (mService != null) {
                if (action.equals(MusicService.META_CHANGED)) {
                    Tools.debugLog(TAG, "META_CHANGED");
                    setPauseButtonImage();

                    // Set Song Image
                    Tools.debugLog(TAG, mService.getSongImageUrl());
                    updateTrackInfo();
                    long next = refreshNow();
                    queueNextRefresh(next);

                    File lrcfile = new File(LRC_PATH + "/" + mService.getLrcName());
                    if (lrcfile.exists() && !lrcfile.isDirectory()) {
                        mIsLrcReady = true;
                        initLrcView();
                    } else {
                        try {
                            mIsLrcReady = false;
                            download(mService.getLrcUrl());
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                if (action.equals(MusicService.CACHESTATE_CHANGED)) {
                    if (mService.isCacheDone()) {
                        if (mService.isPlaying()) {
                            mStateTextView.setText(getString(R.string.playing));
                        } else {
                            mStateTextView.setText(getString(R.string.pause));
                        }

                    } else {
                        mStateTextView.setText(getString(R.string.loading));
                    }
                    setPauseButtonImage();

                    mProgress.setSecondaryProgress(10 * intent.getIntExtra("cachepercent", -1));
                }

                if (action.equals(MusicService.PREPARED_CHANGED)) {
                    Tools.debugLog(TAG, "PREPARED_CHANGED");
                    if (!mService.isPrepared()) {
                        mStateTextView.setText(getString(R.string.loading));
                    } else {
                        mStateTextView.setText(getString(R.string.ready));

                    }
                    setPauseButtonImage();
                }

                if (action.equals(MusicService.PLAYSTATE_CHANGED)) {
                    Tools.debugLog(TAG, "PLAYSTATE_CHANGED");
                    if (mService.isPlaying()) {
                        mStateTextView.setText(getString(R.string.playing));

                        updateTrackInfo();
                        long next = refreshNow();
                        queueNextRefresh(next);

                        // Refresh lyric
                        // In case that during pause,progress seek to other
                        // position
                        mLyricHandler.sendEmptyMessage(REFRESH_LRC);
                    } else {
                        mStateTextView.setText(getString(R.string.pause));

                        // Cancel next lyric refresh
                        mLyricHandler.removeMessages(REFRESH_LRC);
                    }
                    setPauseButtonImage();
                }

                if (action.equals(MusicService.SHUFFLEMODE_CHANGED)) {
                    Tools.debugLog(TAG, "SHUFFLEMODE_CHANGED");
                    setShuffleButtonImage();
                }

            }

        }
    };

    /************************************************************************/
    /* METHODS - Interact with user interface */
    /************************************************************************/

    public void onBackClick(View v) {

        super.onBackPressed();
    }
    
    public void onPlayListClick(View v){
    	/*Intent intent=new Intent(MediaPlayerActivity.this,LocalMusicListActivity.class);
    	intent.putExtra("id", "playlist");
    	startActivity(intent);*/
        Toast.makeText(getApplicationContext(), "undone", Toast.LENGTH_SHORT).show();
    }
    
    public void onFavClick(View v){
    	Toast.makeText(getApplicationContext(), "undone", Toast.LENGTH_SHORT).show();
    }
    
    public void onKmusicClick(View v){
    	Toast.makeText(getApplicationContext(), "undone", Toast.LENGTH_SHORT).show();
    }
    
    public void onPlayClick(View v) {

        if (mService != null & mService.isPrepared()) {
            if (mService.isPlaying()) {
                mService.pause();

            } else {
                mService.play();

            }
        }

    }

    public void onPrevClick(View v) {

        if (mService == null) {
            return;
        }
        mService.prev();
    }

    public void onNextClick(View v) {

        if (mService == null) {
            return;
        }
        mService.next();
    }

    public void setPauseButtonImage() {

        if (mService != null) {
            if (mService.isPrepared() || mService.isCacheDone()) {
                mPauseButton.setEnabled(true);
                if (mService.isPlaying()) {
                    mPauseButton.setImageResource(R.drawable.btn_pause);
                } else {
                    mPauseButton.setImageResource(R.drawable.btn_play);
                }
            } else {
                mPauseButton.setEnabled(false);
                mPauseButton.setImageResource(R.drawable.btn_playback_ic_loading);
            }

        }
    }

    private void setShuffleButtonImage() {

        if (mService == null) {
            return;
        }
        switch (mService.getShuffleMode()) {
            case MusicService.SHUFFLE_NONE:
                mShuffleButton.setCompoundDrawables(null, mShffleDrawableTop, null, null);
                mShuffleButton.setText(getString(R.string.shuffle));
                break;
            case MusicService.SHUFFLE_NORMAL:
                mShuffleButton.setCompoundDrawables(null, mSequenceDrawableTop, null, null);
                mShuffleButton.setText(getString(R.string.sequence));
                break;
            default:
                break;
        }
    }

    public void onShuffleClick(View v) {

        if (mService == null) {
            return;
        }

        int shuffle = mService.getShuffleMode();
        if (shuffle == MusicService.SHUFFLE_NONE) {
            mService.setShuffleMode(MusicService.SHUFFLE_NORMAL);

        } else if (shuffle == MusicService.SHUFFLE_NORMAL) {
            mService.setShuffleMode(MusicService.SHUFFLE_NONE);

        }

    }

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (!fromUser || (mService == null)) {
                return;
            }
            mLyricHandler.removeMessages(REFRESH_LRC);

            mPosOverride = mDuration * progress / 1000;
            mService.seek(mPosOverride);

            refreshNow();
            if (!mFromTouch) {
                refreshNow();
                mPosOverride = -1;
            }

            // Refresh Lrc
            mLrcTime = mPosOverride;
            mLyricHandler.sendEmptyMessage(REFRESH_LRC);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            mLastSeekEventTime = 0;
            mFromTouch = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(REFRESH_TIME);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            mPosOverride = -1;
            mFromTouch = false;
            // Ensure that progress is properly updated in the future,
            mHandler.sendEmptyMessage(REFRESH_TIME);
        }

    };

  
    /************************************************************************/
    /* ServiceConnection - deal with service connections */
    /************************************************************************/
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicBinder binder = (MusicBinder) service;
            mService = binder.getService();
            mBound = true;

            startPlayback();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mBound = false;

        }

    };

    /************************************************************************/
    /* METHODS - deal with music play actions */
    /************************************************************************/
    private class PlaybackHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case REFRESH_TIME: {
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;
                }
            }
        }
    };

    private void startPlayback() {

        if (mService == null) {
            return;
        }
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", -1);

        if (position != -1) {
            try {
                mService.stop();
                mService.open(mService.getOnlinePlayList(), position);

            } catch (Exception ex) {
                Tools.debugLog(TAG, "couldn't start playback: " + ex);
            }

        } else {
            if (mService.isInitialized()) {

            } else {
            	//miniplayer click mini_album 
                this.finish();
                Toast.makeText(this, getString(R.string.error_nochoosenmusic), Toast.LENGTH_LONG)
                        .show();
            }
        }
        Tools.debugLog(TAG, "onstartPlayback");
    }

    private void queueNextRefresh(long delay) {

        if (!paused && !mFromTouch) {
            Message msg = mHandler.obtainMessage(REFRESH_TIME);
            mHandler.removeMessages(REFRESH_TIME);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    private long refreshNow() {

        if (mService == null) {
            return 500;
        }
      //note: mPosOverride = mDuration * progress / 1000;
        long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
        mLrcTime = pos;
        long remaining = 1000 - (pos % 1000);//note:到下一秒还剩下的时间milliseconds，表示下一次更新的时刻
        if ((pos >= 0) && (mDuration > 0)) {
            mCurrentTime.setText(Tools.makeTimeString(this, pos / 1000));

            if (mService.isPlaying()) {
                mCurrentTime.setVisibility(View.VISIBLE);
            } else {
                // blink the counter
                // If the progress bar is still been dragged, then we do not
                // want to blink the
                // currentTime. It would cause flickering due to change in the
                // visibility.
                if (mFromTouch) { //note:手拉动进度条时也显示当前时间。
                    mCurrentTime.setVisibility(View.VISIBLE);
                } else {//如果暂停的时候，手不拉动进度条，则当前时间闪烁。
                    int vis = mCurrentTime.getVisibility();
                    mCurrentTime.setVisibility(vis == View.INVISIBLE ? View.VISIBLE
                            : View.INVISIBLE);
                }
                remaining = 500;
            }

            mProgress.setProgress((int) (1000 * pos / mDuration));
        } else {
            mCurrentTime.setText("--:--");
            mProgress.setProgress(-1);
        }
        // return the number of milliseconds until the next full second, so
        // the counter can be updated at just the right time
        return remaining;
    }

    private void updateTrackInfo() {

        if (mService == null) {
            return;
        }

        ImageCache.getInstance().getDrawable(mService.getSongImageUrl(), mSongImageView);

        mTrackNameView.setText(mService.getTrackName());
        mSingerNameView.setText(mService.getArtistName());
        mDuration = mService.duration();
        mTotalTime.setText(Tools.makeTimeString(this, mDuration / 1000));
    }

    /************************************************************************/
    /* METHODS - deal with music lrc update actions */
    /************************************************************************/
    private class LyricHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            if (mIsLrcReady) {
                switch (msg.what) {
                    case REFRESH_LRC:
                        Tools.debugLog(TAG, "On Lyric Refresh");
                        mUpdateResults.run();
                        queueNextLrc();
                        break;
                }
            }
        }
    };

    private void queueNextLrc() {

        if (mService == null) {
            return;
        }
        if (mService.isPlaying()) {
            Tools.debugLog(TAG, "Queue Next Lrc , CurrentTime:" + mLrcTime);
            long sleeptime = mLyricView.updateIndex(mLrcTime);//note:当前歌词那行需要的时间
            // mLrcTime += sleeptime;
            // long sleeptime = mLyricView.updateIndex(mPosOverride);
            Message msg = mLyricHandler.obtainMessage(REFRESH_LRC);
            mLyricHandler.removeMessages(REFRESH_LRC);
            mLyricHandler.sendMessageDelayed(msg, sleeptime);
            Tools.debugLog(TAG, "Next Lrc after " + sleeptime);
        }

    }

    Runnable mUpdateResults = new Runnable() {
        @Override
        public void run() {

            mLyricView.invalidate();
        }
    };

    private void initLrcView() {

        PlayListItem pli = new PlayListItem(mService.getTrackName() + "-"
                + mService.getArtistName(), mService.getPath(), 0L, true);
        mLyricView.init(pli, LRC_PATH + "/" + mService.getLrcName());
        Tools.debugLog(TAG, "Lrc file: " + LRC_PATH + "/" + mService.getLrcName());
        mLrcTime = 0;
    }

    private void download(String url) throws MalformedURLException {

        if (Tools.checkFolderAvailable(LRC_PATH)) {

            DownloadTaskListener taskListener = new DownloadTaskListener() {

                @Override
                public void updateProcess(DownloadTask task) {

                    // progressDialog.setProgress((int) task.getDownloadSize() /
                    // 1000);
                    // progressDialog.setMax((int) task.getTotalSize() / 1000);
                }

                @Override
                public void preDownload(DownloadTask task) {

                    // progressDialog.setTitle(R.string.progress_download_lrc);
                    // progressDialog.show();
                    // progressDialog.setOnCancelListener(downloadOnCancelListener);
                }

                @Override
                public void finishDownload(DownloadTask task) {

                    // progressDialog.dismiss();
                    mDownloadTask = null;

                    mIsLrcReady = true;
                    initLrcView();
                    mLyricHandler.sendEmptyMessage(REFRESH_LRC);

                }

                @Override
                public void errorDownload(DownloadTask task, int error) {

                    // progressDialog.dismiss();
                    mLyricView.setVisibility(View.GONE);
                    mDownloadTask = null;
                    mIsLrcReady = false;
                }
            };

            if (!TextUtils.isEmpty(url)) {
                mDownloadTask = new DownloadTask(MediaPlayerActivity.this, url, LRC_PATH,
                        taskListener);
                mDownloadTask.execute();
            }
        }
    }

    private OnCancelListener downloadOnCancelListener = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {

            if (mDownloadTask != null) {
                mDownloadTask.onCancelled();
            }
            finish();
        }
    };
}
