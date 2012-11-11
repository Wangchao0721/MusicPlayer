package de.wangchao.musicplayer.download;

public interface DownloadTaskListener {
	
	public void updateProcess(DownloadTask task);

	public void finishDownload(DownloadTask task);

	public void preDownload(DownloadTask task);

	public void errorDownload(DownloadTask task,int error);
}
