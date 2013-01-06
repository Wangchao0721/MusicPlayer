
package de.wangchao.musicplayer.type;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class Music implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int songid;
    private String songname;
    private String album;
    private int length;
    private String webfile;
    private String lrcurl;
    private String pic;
    private String sname;
    
    private boolean fromNet=true;
    
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

    public String getPic() {

        return pic;
    }

    public void setPic(String pic) {

        this.pic = pic;
    }

    public String getSingerName() {

        return sname;
    }

    public void setSingerName(String sname) {

        this.sname = sname;
    }
    
   
    public String getLyricName() {
    	String lrcname;
        try {
            String path = (new URL(lrcurl)).getFile();
            String[] files = path.split("/");
            lrcname = files[files.length - 1];

        } catch (MalformedURLException e) {
        	lrcname=null;
            e.printStackTrace();
        }
        return lrcname;

    }
    
    public void setFormNet(boolean fromNet){
    	this.fromNet=fromNet;
    }
    public boolean getFormNet(){
    	return fromNet;
    }
}
