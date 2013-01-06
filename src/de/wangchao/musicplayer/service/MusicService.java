
package de.wangchao.musicplayer.service;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.activity.MediaPlayerActivity;
import de.wangchao.musicplayer.type.Music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

/**
 * Service that manage all music action , let clients bind to it
 * 
 * @author wangchao
 */
public class MusicService extends Service implements IMusicService {

    /************************************************************************/
    /* CONSTANTS */
    /************************************************************************/
    private static boolean DEBUG = true;
    private static String TAG = "com.service";
    private static String PREF = "ONLINEMUSIC";

    /************************************************************************/
    /* CONSTANTS Represent Music actions & state changes */
    /************************************************************************/
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;

    public static final String PREPARED_CHANGED = "de.wangchao.musicplayer.preparedchanged";
    public static final String PLAYSTATE_CHANGED = "de.wangchao.musicplayer.playstatechanged";
    public static final String CACHESTATE_CHANGED = "de.wangchao.musicplayer.cachechanged";
    public static final String META_CHANGED = "de.wangchao.musicplayer.metachanged";
    public static final String QUEUE_CHANGED = "de.wangchao.musicplayer.queuechanged";
    public static final String REPEATMODE_CHANGED = "com.repeatmodechanged";
    public static final String SHUFFLEMODE_CHANGED = "de.wangchao.musicplayer.shufflemodechanged";

    public static final String SERVICECMD = "de.wangchao.musicplayer.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String CMDCYCLEREPEAT = "cyclerepeat";
    public static final String CMDTOGGLESHUFFLE = "toggleshuffle";

    public static final String TOGGLEPAUSE_ACTION = "de.wangchao.musicplayer.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "de.wangchao.musicplayer.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "de.wangchao.musicplayer.musicservicecommand.previous";
    public static final String NEXT_ACTION = "de.wangchao.musicplayer.musicservicecommand.next";
    public static final String CYCLEREPEAT_ACTION = "de.wangchao.musicplayer.musicservicecommand.cyclerepeat";
    public static final String TOGGLESHUFFLE_ACTION = "de.wangchao.musicplayer.musicservicecommand.toggleshuffle";
    private static final String PLAYSTATUS_REQUEST = "de.wangchao.musicplayer.playstatusrequest";
    private static final String PLAYSTATUS_RESPONSE = "de.wangchao.musicplayer.playstatusresponse";

    private static final int TRACK_PREPARED = 0;
    private static final int TRACK_ENDED = 1;
    private static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FOCUSCHANGE = 4;
    private static final int FADEDOWN = 5;
    private static final int FADEUP = 6;

    /************************************************************************/
    /* Values about music play */
    /************************************************************************/
    private MultiPlayer mPlayer;
    private Music mTrackToPlay;
    private ArrayList<Music> mPlayList;
    private ArrayList<Integer> playedId;
    //private ArrayList<Track> mAutoShuffleList;
    private int mPlayListLen;
    private int mPlayPos;
    private int mShuffleMode = SHUFFLE_NONE;
    private Shuffler mRand = new Shuffler();
    private int mMediaMountedCount = 0;
    private BroadcastReceiver mUnmountReceiver = null;
    private BroadcastReceiver mA2dpReceiver = null;
    private WakeLock mWakeLock;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mIsPrepared = false;
    private AudioManager mAudioManager;
    // used to track current volume
    private float mCurrentVolume = 1.0f;
    private boolean mStartPlayback = false;
    private boolean mIsCacheDone = false;
    private static int CACHETIME = 3;

    // used to track what type of audio focus loss caused the playback to pause
    private boolean mPausedByTransientLossOfFocus = false;

    /************************************************************************/
    /* Values to control services */
    /************************************************************************/
    private SharedPreferences mPreferences;
    private final IBinder mBinder = new MusicBinder();
    private int mServiceStartId = -1;

    private static final Class[] mStartForegroundSignature = new Class[] {
            int.class, Notification.class
    };
    private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class
    };

    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    public static final int REPEAT_ONE=0;
    public static final int REPEAT_ALL=1;
    public static final int PLAY_ONE=2;
    public static final int PLAY_ALL=3;
    private int repeat_mode=REPEAT_ALL;

    private Handler mMediaplayerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            //Tools.debugLog(TAG, "mMediaplayerHandler.handleMessage " + msg.what);
            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += .01f;
                    if (mCurrentVolume < 1.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (mIsSupposedToBePlaying) {
                        next();
                    } else {
                        // the server died when we were idle, so just
                        // reopen the same song (it will start again
                        // from the beginning though when the user
                        // restarts)
                        openCurrent();
                    }
                    break;
                case TRACK_ENDED:
                    /*
                     * if (mRepeatMode == REPEAT_CURRENT) { seek(0); play(); }
                     * else { next(false); }
                     */
                    next();
                    //Tools.debugLog(TAG, "Track ended");
                    break;
                case RELEASE_WAKELOCK:
                    mWakeLock.release();
                    break;
                case FOCUSCHANGE:
                    // This code is here so we can better synchronize it with
                    // the code that
                    // handles fade-in
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            //Tools.debugLog(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                            if (isPlaying()) {
                                mPausedByTransientLossOfFocus = false;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            mMediaplayerHandler.removeMessages(FADEUP);
                            mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                           // Tools.debugLog(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                            mPausedByTransientLossOfFocus = true;
                            pause(); // don't move pause out because we
                                     // have ducking
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            //Tools.debugLog(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                            if (isPlaying() || mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                play(); // also queues a fade-in
                            } else {
                                mMediaplayerHandler.removeMessages(FADEDOWN);
                                mMediaplayerHandler.sendEmptyMessage(FADEUP);
                            }
                            break;
                        default:
                            //Tools.debugLog(TAG, "Unknown audio focus change code");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /************************************************************************/
    /* METHODS - core Service lifecycle methods */
    /************************************************************************/
    @Override
    public void onCreate() {

        super.onCreate();

        if (DEBUG) {
            Log.v(TAG, "Service OnCreate()");
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // mAudioManager.registerMediaButtonEventReceiver(new
        // ComponentName(getPackageName(),
        // MediaButtonIntentReceiver.class.getName()));

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }

        mPreferences = getSharedPreferences(PREF, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);

        registerExternalStorageListener();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // Needs to be done in this thread, since otherwise
        // ApplicationContext.getPowerManager() crashes.
        mPlayer = new MultiPlayer();
        mPlayer.setHandler(mMediaplayerHandler);

    }

    @Override
    public IBinder onBind(Intent intent) {

        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {

        mServiceInUse = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        //Tools.debugLog(TAG, "onUnbind");
        mServiceInUse = false;

        if (isPlaying()) {

        } else {
            // No active playlist, OK to stop the service right now
            stopSelf(mServiceStartId);
        }

        return true;
    }

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MusicBinder extends Binder {
        public MusicService getService() {

            // Return this instance of LocalService so clients can call public
            // methods
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {

       // Tools.debugLog(TAG, "onDestroy");
        if (isPlaying()) {
            //Tools.debugLog(TAG, "Service being destroyed while still playing.");
        }

        mPlayer.release();
        mPlayer = null;

        // Make sure our notification is gone.
        // stopForegroundCompat(R.string.foreground_service_started);

        mAudioManager.abandonAudioFocus(mAudioFocusListener);

        // TODO:Deal with telephone state
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        mMediaplayerHandler.removeCallbacksAndMessages(null);

        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        mWakeLock.release();

        super.onDestroy();
    }

    /************************************************************************/
    /* Listeners to handle mobile state changes , like phone calls and so on */
    /************************************************************************/
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            // TODO:Create Media player to handler Focuschange event
            mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //Tools.debugLog(TAG, "PhoneState: received CALL_STATE_RINGING");
                    if (isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                        pause();
                    }
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Tools.debugLog(TAG, "PhoneState: received CALL_STATE_OFFHOOK");
                    mPausedByTransientLossOfFocus = false;
                    if (isPlaying()) {
                        pause();
                    }
                    break;
            }
        }
    };

    /**
     * Registers an intent to listen for ACTION_MEDIA_EJECT notifications. The
     * intent will call closeExternalStorageFiles() if the external media is
     * going to be ejected, so applications can clean up any files they have
     * open.
     */
    public void registerExternalStorageListener() {

        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        // saveQueue(true);
                        // mQueueIsSaveable = false;
                        // closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        // mMediaMountedCount++;
                        // mCardId =
                        // MusicUtils.getCardId(MediaPlaybackService.this);
                        // reloadQueue();
                        // mQueueIsSaveable = true;
                        // notifyChange(QUEUE_CHANGED);
                        // notifyChange(META_CHANGED);
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    /**
     * Notify the change-receivers that something has changed. The intent that
     * is sent contains the following data for the currently playing track: "id"
     * - Integer: the database row ID "artist" - String: the name of the artist
     * "album_artist" - String: the name of the album artist "album" - String:
     * the name of the album "track" - String: the name of the track The intent
     * has an action that is one of "com.android.music.metachanged"
     * "com.android.music.queuechanged", "com.android.music.playbackcomplete"
     * "com.android.music.playstatechanged" respectively indicating that a new
     * track has started playing, that the playback queue has changed, that
     * playback has stopped because the last file in the list has been played,
     * or that the play-state changed (paused/resumed).
     */
    private void notifyChange(String what) {

        Intent i = new Intent(what);
        // i.putExtra("id", Long.valueOf(getAudioId()));
        i.putExtra("artist", getArtistName());
        // i.putExtra("album_artist", getAlbumartistName());
        i.putExtra("album", getAlbumName());
        i.putExtra("track", getTrackName());
        i.putExtra("playing", isPlaying());
        // i.putExtra("songid", getAudioId());
        // i.putExtra("albumid", getAlbumId());
        // i.putExtra("duration", duration());
        // i.putExtra("position", position());
        if (mPlayList != null) {
            i.putExtra("ListSize", Long.valueOf(mPlayList.size()));
        } else {
            i.putExtra("ListSize", Long.valueOf(mPlayListLen));
        }

        if (what.equals(CACHESTATE_CHANGED)) {
            i.putExtra("cachepercent", mPlayer.cachePercent());
        }

        sendStickyBroadcast(i);

        /*
         * if (what.equals(QUEUE_CHANGED)) { saveQueue(true); } else {
         * saveQueue(false); }
         */

    }

    /************************************************************************/
    /* METHODS - to handle all music player actions */
    /************************************************************************/

    /**
     * Opens the specified file online or local and readies it for playback.
     * 
     * @see com.media.service.IMusicService#openFile(de.wangchao.musicplayer.type.Track)
     */
    @Override
    public void openFile(Music track) {

        if (null == track) {
            return;
        }
        mPlayer.setDataSource(mTrackToPlay.getWebFile());
    }

    private void openCurrent() {
    	if(!playedId.contains(mPlayPos))
            playedId.add(mPlayPos);
        mTrackToPlay = mPlayList.get(mPlayPos);
        openFile(mTrackToPlay);
    }

    /**
     * Replaces the current playlist with a new list, and prepares for starting
     * playback at the specified position in the list, or a random position if
     * the specified position is 0.
     * 
     * @param list The new list of tracks.
     */
    @Override
    public void open(ArrayList<Music> list, int position) {

        synchronized (this) {
            // TODO Deal with shuffle mode

            // determine whether the list is new or not
            int listlength = list.size();
            boolean newlist = true;

            if (list.size() != 0) {
                mPlayList = list;
                mPlayListLen = mPlayList.size();
                mPlayPos = position;
                if(!playedId.contains(position))
                    playedId.add(position);
                mTrackToPlay = mPlayList.get(mPlayPos);
                openFile(mTrackToPlay);
                
            }
        }
    }

    @Override
    public void setOnlinePlayList(ArrayList<Music> list) {

        playedId=new ArrayList<Integer>();
        if (list.size() != 0) {
        	mPlayList = list;
            mPlayListLen = list.size();
        }
    }

    @Override
    public ArrayList<Music> getOnlinePlayList() {

        if (null == mPlayList) {
            return null;
        }
        return mPlayList;
    }

    @Override
    public Music getTrackToPlay() {

        return mTrackToPlay;
    }

    @Override
    public boolean isPlaying() {

        return mIsSupposedToBePlaying;
    }

    @Override
    public boolean isInitialized()
    {
		return mPlayer.isInitialized();	
    }
    @Override
    public boolean isPrepared() {

        return mIsPrepared;
    }

    @Override
    public boolean isCacheDone() {

        return mIsCacheDone;
    }

    @Override        
    public void stop() {

        if (mPlayer.isInitialized()) {
            mIsSupposedToBePlaying = false;
            mPlayer.stop();
        }

    }

    @Override
    public void pause() {

        synchronized (this) {
            if (isPlaying()) {
                mPlayer.pause();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
            }
        }

    }

    /**
     * Starts playback of a previously opened file.
     * 
     * @see com.media.service.IMusicService#play()
     */
    @Override
    public void play() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            return;
        }

        // Managing Audio Focus
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        if (mPlayer.isInitialized()) {
            mPlayer.start();

            // assign the song name to songName
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
                    getApplicationContext(), MediaPlayerActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification();
            notification.tickerText = getString(R.string.app_name);
            notification.icon = R.drawable.mini_default_album;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name),
                    getString(R.string.playing) + mTrackToPlay.getSongName(), pi);
            startForegroundCompat(R.string.foreground_service_started, notification);

            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
    }

    /**
     * Returns the duration of the file in milliseconds. Currently this method
     * returns -1 for the duration of MIDI files.
     */
    @Override
    public long duration() {

        if (mPlayer.mIsInitialized & mIsPrepared) {
            return mPlayer.duration();
        }
        return -1;
    }

    /**
     * Returns the current playback position in milliseconds
     */
    @Override
    public long position() {

        if (mPlayer.mIsInitialized) {
            return mPlayer.position();
        }
        return -1;
    }

    @Override
    public long seek(long pos) {

        if (mPlayer.mIsInitialized & mIsPrepared) {
            if (pos < 0) {
                pos = 0;
            }
            if (pos > mPlayer.duration()) {
                pos = mPlayer.duration();
            }

            return mPlayer.seek(pos);
        }
        return -1;
    }

    @Override
    public void prev(boolean prev) {

        synchronized (this) {
            if (mPlayListLen <= 0) {
                //Tools.debugLog(TAG, "No play queue");
            }

            if (mShuffleMode == SHUFFLE_NORMAL) {
                int currentPos = mPlayPos;
                mPlayPos = mRand.nextInt(mPlayListLen);

                // Make sure shuffle to song that not current song
                while (mPlayPos == currentPos) {
                	if(mPlayListLen==1)
                		break;
                    mPlayPos = mRand.nextInt(mPlayListLen);
                }

                //Tools.debugLog(TAG, "Shuffle to " + mPlayPos);
            } else {
            	if(prev){//prev
	                if (mPlayPos > 0) {
	                    mPlayPos--;
	                } else {
	                   //Tools.debugLog(TAG, "This is first one");
	                    mPlayPos = mPlayListLen - 1;
	                }
            	}
            	else{//next
            		 if (mPlayPos < mPlayListLen - 1) {
                      	mPlayPos++;
                      } else if (mPlayPos >= mPlayListLen - 1) {
                        //  Tools.debugLog(TAG, "This is Last one");
                          mPlayPos = 0;
                      }
            	}
            }

            stop();
            openCurrent();
            notifyChange(META_CHANGED);
            playedId.clear();
        }

    }

    @Override
    public void next() {

        synchronized (this) {
            if (mPlayListLen <= 0) {
                //Tools.debugLog(TAG, "No play queue");
                return;
            }
           // Toast.makeText(getApplicationContext(), playedId.toString(), Toast.LENGTH_SHORT).show();
            if(repeat_mode==REPEAT_ONE){
        		//repeat current song
        	}
            if(repeat_mode==PLAY_ONE){
            	//stop music
            	stop();
            	return;
            }
            if(repeat_mode==REPEAT_ALL){
            	if(playedId.size()==mPlayListLen)
                    playedId.clear();
            	
            	if (mShuffleMode == SHUFFLE_NORMAL) {
                    // Tools.debugLog(TAG, "Current is " + mPlayPos);
                     int currentPos = mPlayPos;
                     mPlayPos = mRand.nextInt(mPlayListLen);
                     // Make sure shuffle to song that not current song
                     while (playedId.contains(mPlayPos)) {
                      	if(mPlayListLen==1)
                      		 break;
                          mPlayPos = mRand.nextInt(mPlayListLen);
                      }
                      
                 } else {
                     if (mPlayPos < mPlayListLen - 1) {
                     	mPlayPos++;
                     } else if (mPlayPos >= mPlayListLen - 1) {
                       //  Tools.debugLog(TAG, "This is Last one");
                         mPlayPos = 0;
                     }
                 }
            }
            if(repeat_mode==PLAY_ALL){
            	
            	if(playedId.size()==mPlayListLen){
            	 stop();
               	 return;
                }
            	if (mShuffleMode == SHUFFLE_NORMAL) {
                    // Tools.debugLog(TAG, "Current is " + mPlayPos);
                     int currentPos = mPlayPos;
                     mPlayPos = mRand.nextInt(mPlayListLen);

                     // Make sure shuffle to song that not current song
                     while (playedId.contains(mPlayPos)) {
                     	if(mPlayListLen==1)
                     		 break;
                         mPlayPos = mRand.nextInt(mPlayListLen);
                     }
                 } else {
                     if (mPlayPos < mPlayListLen - 1) {
                     	mPlayPos++;
                     } else if (mPlayPos >= mPlayListLen - 1) {
                       //  Tools.debugLog(TAG, "This is Last one");
                    	 mPlayPos=0;
                     }
                 }
            }
            
            stop();
            openCurrent();
            notifyChange(META_CHANGED);
        }

    }
    
    @Override
    public void setRepeatMode(int mode){
    	repeat_mode=mode;
    	 notifyChange(REPEATMODE_CHANGED);
    }
    
    @Override
    public int getRepeatMode(){
    	return repeat_mode;
    }

    @Override
    public String getTrackName() {

        if (mTrackToPlay == null) {
            return null;
        }
        return mTrackToPlay.getSongName();

    }

    @Override
    public String getAlbumName() {

        if (mTrackToPlay == null) {
            return null;
        }
        return mTrackToPlay.getAlbum();
    }

    @Override
    public String getArtistName() {

        if (mTrackToPlay == null) {
            return null;
        }
        return mTrackToPlay.getSingerName();
    }

    @Override
    public String getPath() {

        if (mTrackToPlay == null) {
            return null;
        }
        return mTrackToPlay.getWebFile();
    }

    @Override
    public String getLrcUrl() {

        if (mTrackToPlay == null) {
            return null;
        }
        return mTrackToPlay.getLrcUrl();
    }

    @Override
    public String getLrcName() {

        if (mTrackToPlay == null) {
            return null;
        }
        return mTrackToPlay.getLyricName();
    }

    public String getSongImageUrl() {

        if (mTrackToPlay == null) {
            return null;
        }

        return mTrackToPlay.getPic();
    }

    @Override
    public int removeTrack(long id) {

        // TODO Auto-generated method stub
        return 0;
    }

    /************************************************************************/
    /* Method to deal with Shuffle music */
    /************************************************************************/
    @Override
    public void setShuffleMode(int shufflemode) {

        synchronized (this) {
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }

            mShuffleMode = shufflemode;
            //mAutoShuffleList = (ArrayList<Track>) mPlayList.clone();
            notifyChange(SHUFFLEMODE_CHANGED);
        }

    }

    @Override
    public int getShuffleMode() {

        return mShuffleMode;
    }

    // A simple variation of Random that makes sure that the
    // value it returns is not equal to the value it returned
    // previously, unless the interval is 1.
    private static class Shuffler {
        private int mPrevious;
        private Random mRandom = new Random();

        public int nextInt(int interval) {

            int ret;
            do {
                ret = mRandom.nextInt(interval);
            } while (ret == mPrevious && interval > 1);
            mPrevious = ret;
            return ret;
        }
    };

    /************************************************************************/
    /* Method to start a foreground service compat with all api level */
    /************************************************************************/

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {

        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }

        // Fall back on the old API.
        setForeground(true);
        mNM.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {

        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API. Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        setForeground(false);
    }

    /**
     * Provides a unified interface for dealing with midi files and other media
     * files.
     * 
     * @author wangchao
     */
    private class MultiPlayer {
        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Handler mHandler;
        private boolean mIsInitialized = false;
        private int mCachePercent = -1;

        public MultiPlayer() {

            mMediaPlayer.setWakeMode(MusicService.this, PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setDataSource(String path) {

            try {
                mMediaPlayer.reset();

                mMediaPlayer.setOnPreparedListener(onprelistener);
                if (path.startsWith("content://")) {
                    mMediaPlayer.setDataSource(MusicService.this, Uri.parse(path));
                } else {
                    mMediaPlayer.setDataSource(path);
                }
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                mIsPrepared = false;
                notifyChange(PREPARED_CHANGED);

                try {
                    mMediaPlayer.prepareAsync();
                } catch (IllegalStateException e) {
                    mMediaPlayer.reset();
                    mMediaPlayer.setOnPreparedListener(onprelistener);
                    if (path.startsWith("content://")) {
                        mMediaPlayer.setDataSource(MusicService.this, Uri.parse(path));
                    } else {
                        mMediaPlayer.setDataSource(path);
                    }
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    mIsPrepared = false;
                    notifyChange(PREPARED_CHANGED);
                    mMediaPlayer.prepareAsync();
                }

                mIsCacheDone = false;
                mCachePercent = -1;
                notifyChange(CACHESTATE_CHANGED);

            } catch (IOException ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            } catch (IllegalArgumentException ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            }
            mMediaPlayer.setOnCompletionListener(oncomlistener);
            mMediaPlayer.setOnErrorListener(errorListener);
            mMediaPlayer.setOnBufferingUpdateListener(bufferlistener);
            // TODO:This is for API Level 9+
            /*
             * Intent i = new
             * Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
             * i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
             * i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
             * sendBroadcast(i);
             */
            mIsInitialized = true;
        }

        public boolean isInitialized() {

            return mIsInitialized;
        }

        public void start() {

            mMediaPlayer.start();
        }

        public void stop() {

            mMediaPlayer.reset();
            mIsInitialized = false;
        }

        /**
         * You CANNOT use this player anymore after calling release()
         */
        public void release() {

            stop();
            mMediaPlayer.release();
        }

        public void pause() {

            mMediaPlayer.pause();
        }

        public void setHandler(Handler handler) {

            mHandler = handler;
        }

        /**
         * Called to update status in buffering a media stream received through
         * progressive HTTP download. The received buffering percentage
         * indicates how much of the content has been buffered or played.
         */
        MediaPlayer.OnBufferingUpdateListener bufferlistener = new MediaPlayer.OnBufferingUpdateListener() {

            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

                if (mIsPrepared) {
                    if (DEBUG) {
                        if (duration() > 0) {
                            Log.v(TAG, "NowPos: " + 100 * position() / duration()
                                    + "onBufferingUpdate: " + percent);
                        }
                    }
                    // TODO:Handle buffer of http download & show it to user
                    mCachePercent = percent;

                    if (percent <= 100 * position() / (duration() + CACHETIME)) {
                        mIsCacheDone = false;
                    } else {
                        mIsCacheDone = true;
                    }

                    notifyChange(CACHESTATE_CHANGED);
                }

            }
        };

        MediaPlayer.OnPreparedListener onprelistener = new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                // mHandler.sendEmptyMessage(TRACK_PREPARED);
                if (DEBUG) {
                    Log.v(TAG, "OnPrepared");
                }
                mIsPrepared = true;
                notifyChange(PREPARED_CHANGED);
                play();
            }
        };

        MediaPlayer.OnCompletionListener oncomlistener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if (DEBUG) {
                    Log.v(TAG, "onCompletion");
                }
                // Acquire a temporary wakelock, since when we return from
                // this callback the MediaPlayer will release its wakelock
                // and allow the device to go to sleep.
                // This temporary wakelock is released when the RELEASE_WAKELOCK
                // message is processed, but just in case, put a timeout on it.
                mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        };

        MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                if (DEBUG) {
                    Log.v(TAG, "onError");
                }
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        mIsInitialized = false;
                        mMediaPlayer.release();
                        // Creating a new MediaPlayer and settings its wakemode
                        // does not
                        // require the media service, so it's OK to do this now,
                        // while the
                        // service is still being restarted
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setWakeMode(MusicService.this, PowerManager.PARTIAL_WAKE_LOCK);
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                        break;
                    case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                       //Tools.debugLog(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: "
                       //        + what + "," + extra);
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                       ///Tools.debugLog(TAG, "MEDIA_ERROR_UNKNOWN: " + what + "," + extra);
                        break;
                    default:
                        Log.d(TAG, "Error: " + what + "," + extra);
                        break;
                }
                return false;
            }
        };

        public int cachePercent() {

            return mCachePercent;
        }

        public long duration() {

            return mMediaPlayer.getDuration();
        }

        public long position() {

            return mMediaPlayer.getCurrentPosition();
        }

        public long seek(long whereto) {

            mMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        public void setVolume(float vol) {

            mMediaPlayer.setVolume(vol, vol);
            mCurrentVolume = vol;
        }

    }

}
