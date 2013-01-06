
package de.wangchao.musicplayer.widget;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.type.Music;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackAdapter extends MyArrayAdapter<Music> {

    TrackWrapper holder;
    Activity mActivity;
    ArrayList<Music> mlist = null;

    public TrackAdapter(Context context) {

        super(context);
    }

    @Override
    public long getItemId(int position) {

        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TrackWrapper wrapper;

        if (convertView == null) {
            convertView = super.mInflater.inflate(R.layout.online_list_item, null);

            wrapper = new TrackWrapper(convertView);
            convertView.setTag(wrapper);
        } else {
            wrapper = (TrackWrapper) convertView.getTag();
        }

        wrapper = new TrackWrapper(convertView);
        wrapper.populateFrom(getItem(position));

        return convertView;
    }

    private class TrackWrapper {
        private TextView songName = null;
        private TextView singerName = null;
        private ImageView pic = null;
        private View row = null;

        TrackWrapper(View row) {

            this.row = row;
        }

        public TextView getSongName() {

            if (songName == null) {
                songName = (TextView) row.findViewById(R.id.Song);
            }
            return songName;
        }

        public TextView getSingerName() {

            if (singerName == null) {
                singerName = (TextView) row.findViewById(R.id.Singer);
            }
            return singerName;
        }

        public ImageView getPic() {

            if (pic == null) {
                pic = (ImageView) row.findViewById(R.id.img_album);
            }
            return pic;
        }

        void populateFrom(Music track) {

            if (track == null) {
                return;
            }
            getSongName().setText(track.getSongName());
            getSingerName().setText(track.getSingerName());
            getPic().setTag(track.getPic());
        }
    }
}
