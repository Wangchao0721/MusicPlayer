
package de.wangchao.musicplayer.widget;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.type.Music;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicsAdapter extends MyArrayAdapter<Music> {

    public MusicsAdapter(Context context) {

        super(context);
    }

    @Override
    public long getItemId(int position) {

        Music music = getItem(position);
        if (music == null) {
            return -1;
        }
        return music.getSongId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MusicWrapper wrapper;

        if (convertView == null) {
            convertView = super.mInflater.inflate(R.layout.online_list_item, null);

            wrapper = new MusicWrapper(convertView);
            convertView.setTag(wrapper);
        } else {
            wrapper = (MusicWrapper) convertView.getTag();
        }

        wrapper = new MusicWrapper(convertView);
        wrapper.populateFrom(getItem(position));

        return convertView;
    }

    private class MusicWrapper {

        private TextView songName = null;
        private TextView singerName = null;
        private ImageView pic = null;
        private View row = null;

        MusicWrapper(View row) {

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

        void populateFrom(Music music) {

            if (music == null) {
                return;
            }
            getSongName().setText(music.getSongName());
            getSingerName().setText(music.getSingerName());
            getPic().setTag(music.getPic());
        }
    }
}
