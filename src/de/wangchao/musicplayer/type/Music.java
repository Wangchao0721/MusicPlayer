
package de.wangchao.musicplayer.type;

import java.io.Serializable;

public class Music implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int songid;
    private String songname;
    //private int sid;
    //private int tid;
    private String album;
    private int length;
    private String webfile;
    private String lrcurl;
    //private String accurl = "";
    //private String addTime; // TODO can't parse date
    //private String updateTime;
    //private int uid;
    private String pic;
    //private int istop;
    //private int ishot;
    //private int views;
    //private int singcount;
    //private int ordernum;
    private String sname;

    //@SuppressWarnings("unused")
    //private int ext1;
    //@SuppressWarnings("unused")
    //private String ext2;

    public int getSongId() {

        return songid;
    }

    public void setSongId(int songId) {

        this.songid = songId;
    }

    public String getSongName() {

        return songname;
    }

    public void setSongName(String songName) {

        this.songname = songName;
    }

   /* public int getSingerId() {

        return sid;
    }

    public void setSingerId(int sid) {

        this.sid = sid;
    }

    public int getThemeId() {

        return tid;
    }

    public void setThemeId(int tid) {

        this.tid = tid;
    }*/

    public String getAlbum() {

        return album;
    }

    public void setAlbum(String album) {

        this.album = album;
    }

    public int getLength() {

        return length;
    }

    public void setLength(int length) {

        this.length = length;
    }

    public String getWebFile() {

        return webfile;
    }

    public void setWebFile(String webFile) {

        this.webfile = webFile;
    }

    public String getLrcUrl() {

        return lrcurl;
    }

    public void setLrcUrl(String lrcUrl) {

        this.lrcurl = lrcUrl;
    }

   /* public String getAccUrl() {

        return accurl;
    }

    public void setAccUrl(String accUrl) {

        this.accurl = accUrl;
    }

    public String getAddTime() {

        return addTime;
    }

    public void setAddTime(String addTime) {

        this.addTime = addTime;
    }

    public String getUpdateTime() {

        return updateTime;
    }

    public void setUpdateTime(String updateTime) {

        this.updateTime = updateTime;
    }*/

    public String getPic() {

        return pic;
    }

    public void setPic(String pic) {

        this.pic = pic;
    }

    /*public int getIsTop() {

        return istop;
    }

    public boolean isTop() {

        return 1 == istop;
    }

    public void setIsTop(int isTop) {

        this.istop = isTop;
    }

    public int getIsHot() {

        return ishot;
    }

    public boolean isHot() {

        return 1 == ishot;
    }

    public void setIsHot(int isHot) {

        this.ishot = isHot;
    }

    public int getListenTimes() {

        return views;
    }

    public void setListenTimes(int views) {

        this.views = views;
    }

    public int getSingTimes() {

        return singcount;
    }

    public void setSingTimes(int singCount) {

        this.singcount = singCount;
    }

    public int getOrderNum() {

        return ordernum;
    }

    public void setOrderNum(int orderNum) {

        this.ordernum = orderNum;
    }

    public int getUserId() {

        return uid;
    }

    public void setUserId(int uid) {

        this.uid = uid;
    }*/

    public String getSingerName() {

        return sname;
    }

    public void setSingerName(String sname) {

        this.sname = sname;
    }

    // private String lrcname;
    //
    // public String getLyricName() {
    //
    // try {
    // String path = (new URL(lrcurl)).getFile();
    // String[] files = path.split("/");
    // lrcname = files[files.length - 1];
    //
    // } catch (MalformedURLException e) {
    // e.printStackTrace();
    // }
    // Tools.debugLog("MusicParse", lrcname);
    // return lrcname;
    //
    // }
    //
    public static Music parseTrack(Track track) {

        Music music = new Music();
        music.setSongId(track.getSongId());
        music.setSongName(track.getTrackName());
        music.setSingerName(track.getArtistName());
        music.setLrcUrl(track.getLyricUrl());
        music.setAlbum(track.getAlbumName());
        music.setPic(track.getSongImageUrl());
        music.setWebFile(track.getPath());
        music.setLength(track.getLength());
        return music;
    }

    /*public static Music parseKMusic(KMusic kmusic) {

        Music music = new Music();
        music.setSongId(kmusic.getSongId());
        music.setSongName(kmusic.getKname());
        music.setSingerName(kmusic.getKuser());
        music.setLrcUrl(kmusic.getLrcurl());
        music.setPic(kmusic.getHeaderpic());
        music.setWebFile(kmusic.getUrl());
        music.setLength(kmusic.getLength());
        return music;
    }*/

}
