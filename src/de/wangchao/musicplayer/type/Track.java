
package de.wangchao.musicplayer.type;

import de.wangchao.musicplayer.util.Tools;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class Track implements Serializable {

    private static final long serialVersionUID = -8597901598018219041L;

    // private int id;
    private int songId = -1;
    private int kid = -1;
    private String trackName;
    private String albumName;
    private String artistName;
    private String lyricUrl;
    private String path;
    private String songImageUrl;
    private int length;

    private boolean isKMusic = false;

    public String getTrackName() {

        return trackName;
    }

    public void setTrackName(String trackName) {

        this.trackName = trackName;
    }

    public String getAlbumName() {

        return albumName;
    }

    public void setAlbumName(String albumName) {

        this.albumName = albumName;
    }

    public String getArtistName() {

        return artistName;
    }

    public void setArtistName(String artistName) {

        this.artistName = artistName;
    }

    public String getLyricUrl() {

        return lyricUrl;
    }

    public void setLyricUrl(String lyricUrl) {

        this.lyricUrl = lyricUrl;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = path;
    }

    public String getSongImageUrl() {

        return songImageUrl;
    }

    public void setSongImageUrl(String songImageUrl) {

        this.songImageUrl = songImageUrl;
    }

    public static Track parseMusic(Music music) {

        Track track = new Track();
        // track.setId(music.getSongId());
        track.setSongId(music.getSongId());
        track.setTrackName(music.getSongName());
        track.setArtistName(music.getSingerName());
        track.setLyricUrl(music.getLrcUrl());
        track.setAlbumName(music.getAlbum());
        track.setSongImageUrl(music.getPic());
        track.setPath(music.getWebFile());
        track.setLength(music.getLength());

        track.setKMusic(false);
        return track;
    }


    private String lrcname;

    public String getLyricName() {

        try {
            String path = (new URL(lyricUrl)).getFile();
            String[] files = path.split("/");
            lrcname = files[files.length - 1];

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Tools.debugLog("TrackParse", lrcname);
        return lrcname;

    }

    public int getSongId() {

        return songId;
    }

    public void setSongId(int songId) {

        this.songId = songId;
    }

    public int getKid() {

        return kid;
    }

    public void setKid(int kid) {

        this.kid = kid;
    }

    public int getLength() {

        return length;
    }

    public void setLength(int length) {

        this.length = length;
    }

    public boolean isKMusic() {

        return isKMusic;
    }

    public void setKMusic(boolean isKMusic) {

        this.isKMusic = isKMusic;
    }

    // public int getId() {
    //
    // return id;
    // }
    //
    // public void setId(int id) {
    //
    // this.id = id;
    // }

}
