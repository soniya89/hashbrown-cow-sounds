package com.orionskelp.cordit;

import java.util.ArrayList;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.orionskelp.cordit.R;
import com.orionskelp.cordit.MainActivity;

import android.app.Notification;
import android.app.PendingIntent;

public class MusicService extends Service implements
		MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener {

	//for binding to the main activity
	private final IBinder musicBind = new MusicBinder();
	//for playing music
	private MediaPlayer player;
	
	//to control the songs
	private ArrayList<Song> songs;
	private int songPos;
	private String songTitle = "";
	
	private static final int NOTIFY_ID = 1;


	@Override
	public void onCreate() {
		super.onCreate();
		player = new MediaPlayer();
		songPos = 0;
		initMusicPlayer();
	
	}

	public void initMusicPlayer()
	// set player properties
	{
		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);

		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);

	}

	public void setList(ArrayList<Song> theSongs) {
		songs = theSongs;
	}

	// used to bind activity and service, sends service back to activity
	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}

	}

	@Override
	public IBinder onBind(Intent intent) {

		return musicBind;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (player.getCurrentPosition() > 0) {
			mp.reset();
			playNext();
		}

	}

	public void playSong() {
		player.reset();

		if(songs.size()!=0)
		{
		Song playSong = songs.get(songPos);

		songTitle = playSong.getTitle();
		
		long currSong = playSong.getID();

		// set uri, uri is a reference to data on the device, you have to
		// convert the string location to a uri object
		Uri trackUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				currSong);

		try {
			player.setDataSource(getApplicationContext(), trackUri);

		} catch (Exception e) {
			System.exit(0);
		}

		// call async so that it doesn't block
		player.prepareAsync();
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {

		mp.reset();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

		player.start();

		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);

		builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play)
				.setTicker(songTitle).setOngoing(true)
				.setContentTitle("Playing").setContentText(songTitle);
		Notification not = builder.build();

		startForeground(NOTIFY_ID, not);

	}

	public void setSong(int songIndex) {
		songPos = songIndex;
	}

	public int getPosn() {
		return player.getCurrentPosition();
	}

	public int getDur() {
		return player.getDuration();
	}

	public boolean isPng() {
		return player.isPlaying();
	}

	public void pausePlayer() {
		player.pause();
	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void go() {
		player.start();
	}

	public void playPrev() {
		songPos--;
		if (songPos < 0)
			songPos = songs.size() - 1;
		playSong();
	}

	public void playNext() {

			songPos++;
			if (songPos >= songs.size())
				songPos = 0;
		playSong();
	}
	
	public int getSongIndex()
	{
		return songPos;
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
	}



}
