
package de.wangchao.musicplayer.widget;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.activity.MediaPlayerActivity;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.type.Track;
import de.wangchao.musicplayer.util.ImageCache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MiniPlayPannelWrapper {

    private static final String TAG = "MiniPlayPanelWrapper";

    private ImageView miniPlayImage;
    private TextView miniPlaySongText;
    private TextView miniPlaySingerText;
    private ImageButton miniPlayPlayButton;
    private ImageButton miniPlayStopButton;
    private ImageButton miniPlayPreButton;
    private ImageButton miniPlayNextButton;

    private View holder;

    public MiniPlayPannelWrapper(View view) {

        holder = view;
    }

    public ImageView getMiniPlayImage() {

        if (miniPlayImage == null) {
            miniPlayImage = (ImageView) holder.findViewById(R.id.mini_album);

        }
        miniPlayImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(holder.getContext(), MediaPlayerActivity.class);
                if (preTrack != null) {
                    if (preTrack.isKMusic()) {
                        intent.putExtra("isKSong", true);
                    }
                }
                v.getContext().startActivity(intent);
            }

        });
        return miniPlayImage;
    }

    public TextView getMiniPlaySongText() {

        if (miniPlaySongText == null) {
            miniPlaySongText = (TextView) holder.findViewById(R.id.txt_song);

        }
        return miniPlaySongText;
    }

    public TextView getMiniPlaySingerText() {

        if (miniPlaySingerText == null) {
            miniPlaySingerText = (TextView) holder.findViewById(R.id.txt_singer);
        }
        return miniPlaySingerText;
    }

    public ImageButton getMiniPlayPlayButton() {

        if (miniPlayPlayButton == null) {
            miniPlayPlayButton = (ImageButton) holder.findViewById(R.id.btn_play);
        }
        return miniPlayPlayButton;
    }

    public ImageButton getMiniPlayStopButton() {

        if (miniPlayStopButton == null) {
            miniPlayStopButton = (ImageButton) holder.findViewById(R.id.btn_stop);
        }
        return miniPlayStopButton;
    }

    public ImageButton getMiniPlayPreButton() {

        if (miniPlayPreButton == null) {
            miniPlayPreButton = (ImageButton) holder.findViewById(R.id.btn_pre);
        }
        return miniPlayPreButton;
    }

    public ImageButton getMiniPlayNextButton() {

        if (miniPlayNextButton == null) {
            miniPlayNextButton = (ImageButton) holder.findViewById(R.id.btn_next);
        }
        return miniPlayNextButton;
    }

    private Track preTrack = null;

    public void bindService(final MusicService service) {

        if (service == null) {
            clearView();
            return;
        }

        if (service.isPlaying() || !service.isPrepared()) {
            getMiniPlayStopButton().setVisibility(View.VISIBLE);
            getMiniPlayPlayButton().setVisibility(View.GONE);
        } else {
            getMiniPlayPlayButton().setVisibility(View.VISIBLE);
            getMiniPlayStopButton().setVisibility(View.GONE);
        }

        Track track = service.getTrackToPlay();
        if (track == null) {
            clearView();
            return;
        }
        if (preTrack == track) {
            return;
        }
        preTrack = track;

        getMiniPlaySongText().setText(track.getTrackName());
        getMiniPlaySingerText().setText(track.getArtistName());
        ImageCache.getInstance().getDrawable(track.getSongImageUrl(), getMiniPlayImage());
        getMiniPlayPlayButton().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                service.play();

                getMiniPlayStopButton().setVisibility(View.VISIBLE);
                getMiniPlayPlayButton().setVisibility(View.GONE);
            }
        });

        getMiniPlayStopButton().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                service.pause();

                getMiniPlayPlayButton().setVisibility(View.VISIBLE);
                getMiniPlayStopButton().setVisibility(View.GONE);
            }
        });

        getMiniPlayPreButton().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                service.prev(true);
                bindService(service);
            }
        });

        getMiniPlayNextButton().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                service.prev(false);
                bindService(service);
            }
        });

    }

    private void clearView() {

        getMiniPlayPlayButton().setVisibility(View.VISIBLE);
        getMiniPlayStopButton().setVisibility(View.GONE);

        getMiniPlaySongText().setText("");
        getMiniPlaySingerText().setText("");

        getMiniPlayImage().setImageResource(R.drawable.mini_default_album);
        getMiniPlayPlayButton().setOnClickListener(null);
        getMiniPlayStopButton().setOnClickListener(null);
        getMiniPlayPreButton().setOnClickListener(null);
        getMiniPlayNextButton().setOnClickListener(null);
    }

    private BroadcastReceiver mStatusListener;
    private Context mContext;

    public void registerBroadcastReceiver(Context context, final OnStatusChangedListener listener) {

        if (context == null) {
            return;
        }

        mContext = context;

        if (mStatusListener == null) {
            IntentFilter f = new IntentFilter();
            f.addAction(MusicService.PREPARED_CHANGED);
            f.addAction(MusicService.PLAYSTATE_CHANGED);
            // f.addAction(MusicService.CACHESTATE_CHANGED);
            f.addAction(MusicService.META_CHANGED);
            // f.addAction(MusicService.SHUFFLEMODE_CHANGED);

            mStatusListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (listener != null) {
                        listener.onStatusChanged();
                    }
                }
            };

            context.registerReceiver(mStatusListener, f);
        }
    }

    public void unregister() {

        if (mContext != null && mStatusListener != null) {
            mContext.unregisterReceiver(mStatusListener);
        }
    }

    public interface OnStatusChangedListener {
        public void onStatusChanged();
    }

}
