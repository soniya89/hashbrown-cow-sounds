package com.example.cordit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.example.cordit.MusicService.MusicBinder;
import com.example.cordit.RecMicToMp3;

public class MainActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener{
	private static final String LOG_TAG = "AudioRecordTest";

	// recording
	private static String mFileName = null;
	private Button mRecordButton = null;
	private RecMicToMp3 mRecMicToMp3;

	// saving song
	private EditText input;
	private AlertDialog.Builder alertSaveSong;
	private AlertDialog.Builder alertCancelSong;

	// for music playback
	private MusicService musicServ;
	private Intent playIntent;
	
	// song list
	private ArrayList<Song> songList;
	private ListView songView;
	SongAdapter songAdt;

	private boolean musicBound = false;
	private boolean paused = false;
	private boolean playbackPaused = false;
	private boolean disableMediaPlayer = false;
	private boolean mStartRecording = true;
	private boolean myMusicSelected = false;

	// media player
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private SeekBar songProgressBar;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	private TextView theTitle;

	private Handler mHandler = new Handler();
	private Utilities utils;
	
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	
	private boolean startedPlayingOnce = false;

	@Override
	public void onCreate(Bundle data) {
		super.onCreate(data);

		setContentView(R.layout.activity_main);
		
		Toast.makeText(this, "oncreate",
				 Toast.LENGTH_SHORT).show();
		/*
		Bundle bundle = getIntent().getExtras();
		
		if(bundle.isEmpty()==false)
		{
		String deletedSongTitle = bundle.getString("song_title");
		
		Toast.makeText(this, deletedSongTitle,
				 Toast.LENGTH_SHORT).show();
		}
	*/
		// set up record button
		mRecordButton = (Button) findViewById(R.id.record_button);
		mRecordButton.setText("RECORD");
		mRecordButton.setOnClickListener(this);
		getWindow().getDecorView().setBackgroundColor(Color.DKGRAY);

		// set up song list
		songView = (ListView) findViewById(R.id.song_list);
		songList = new ArrayList<Song>();
		getSongList();
		// sort songs alphabetically
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});
		songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);

		// set music player buttons, seekbar, time
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(this);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnForward.setOnClickListener(this);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnBackward.setOnClickListener(this);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(this);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPrevious.setOnClickListener(this);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		theTitle = (TextView)findViewById(R.id.title);

		utils = new Utilities();

		songProgressBar.setOnSeekBarChangeListener(this);
		
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		int sb2value = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sb2value, 0);
		
		/*
		mRecMicToMp3.setHandle(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case RecMicToMp3.MSG_REC_STARTED:
				
					break;
				case RecMicToMp3.MSG_REC_STOPPED:
					
					break;
				case RecMicToMp3.MSG_ERROR_GET_MIN_BUFFERSIZE:
				
					
					break;
				case RecMicToMp3.MSG_ERROR_CREATE_FILE:
					
					break;
				case RecMicToMp3.MSG_ERROR_REC_START:
					
					break;
				case RecMicToMp3.MSG_ERROR_AUDIO_RECORD:
					
					break;
				case RecMicToMp3.MSG_ERROR_AUDIO_ENCODE:
				
					break;
				case RecMicToMp3.MSG_ERROR_WRITE_FILE:
					
					break;
				case RecMicToMp3.MSG_ERROR_CLOSE_FILE:
					
					break;
				default:
					break;
				}
			}
		});
		*/
	
	}

	@Override
	public void onStart()
	// typically avoided, but needed here to start music service before activity
	// comes to the screen
	{
		super.onStart();
		
		if (playIntent == null) {

			// start the intent
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	public void songPicked(View view)
	// song is picked from the list view
	{
		if(disableMediaPlayer == false)
		{
		startedPlayingOnce = true;
		musicServ.setSong(Integer.parseInt(view.getTag().toString()));
		 
		musicServ.playSong();

		String songTitle = songList.get(Integer.parseInt(view.getTag().toString())).getTitle();
        theTitle.setText(songTitle);
        
		// if playback was previously paused, unpause it
		if (playbackPaused) {

			playbackPaused = false;
		}
		
		// because music is playing, set the play button to display pause
		btnPlay.setImageResource(R.drawable.btn_pause);

		// reset the progress bar
		songProgressBar.setProgress(0);
		songProgressBar.setMax(100);

		// start the thread which continually updates the progress bar
		updateProgressBar();
		}
		else
		{
			 Toast.makeText(this, "Audio recording in progress!",
			 Toast.LENGTH_SHORT).show();
		}

	}

	private ServiceConnection musicConnection = new ServiceConnection()
	// used to monitor the state of the service
	{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder) service;
			musicServ = binder.getService();
			musicServ.setList(songList);
			musicBound = true;

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			musicBound = false;
		}
	};

	public void getSongList()
	// retrieves songs from media store, adds them to song list
	{
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null,
				null);

		if (musicCursor != null && musicCursor.moveToFirst()) {
			// get column indexes for data items
			int titleColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);

			do {
				// create a new song object
				long thisId = musicCursor.getLong(idColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				if(thisArtist.equals("my_cordit_recordings"))
				{
				songList.add(new Song(thisId, thisTitle, thisArtist));
				}
			} while (musicCursor.moveToNext());

		}

	}

	@Override
	public void onDestroy()
	// shutdown all background services before app is destroyed
	{
		stopService(playIntent);
		musicServ = null;
		super.onDestroy();

	}

	private void onRecord(boolean start)
	// record button is pressed
	{
		if (start) {
			disableMediaPlayer = true;
			if (musicServ != null) {
				if(musicServ.isPng())
				{
				musicServ.pausePlayer();
				}
				btnPlay.setImageResource(R.drawable.btn_play);
				playbackPaused = true;
			}
			startRecording();
		} else {
			stopRecording();
			disableMediaPlayer = false;
		}
	}

	
	
	private void startRecording() {
		mRecMicToMp3 = new RecMicToMp3(
				Environment.getExternalStorageDirectory() + "/Music/testing.mp3", 44100);
		
		mRecMicToMp3.start();

	}

	private void stopRecording() {
		// prompt user for song filename
		
		mRecMicToMp3.stop();
		
		input = new EditText(this);
		alertSaveSong = new AlertDialog.Builder(this);

		alertSaveSong.setTitle("Song Name");

		alertCancelSong = new AlertDialog.Builder(this);

		// alert user that un-named song will be deleted
		alertCancelSong.setTitle("Cancel Saving Song");
		alertCancelSong
				.setMessage("If you do not name a song it will be deleted, are you sure you want to delete this song?");

		alertSaveSong.setView(input).setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						ContentResolver cResolver = getContentResolver();
						ContentValues values = new ContentValues();
						
						// rename audio file
						File sdcard = Environment.getExternalStorageDirectory();
						File from = new File(sdcard,
								"/Music/testing.mp3");
						
						
						String value = input.getText().toString();
						
						/*
						// check if name exists
						 //check if name is null
						boolean titleExist = titleExists(value);
						
						if(titleExist == false)
						{*/
						mFileName = "/Music/" + value + ".mp3";
						
						File to = new File(sdcard, mFileName);
						from.renameTo(to);
						
						values.put(MediaStore.MediaColumns.DATA, to.getAbsolutePath());
						values.put(MediaStore.MediaColumns.TITLE, value);
						values.put(MediaStore.Audio.Media.ARTIST, "my_cordit_recordings");
						values.put(MediaStore.Audio.Media.IS_MUSIC, true);
						
						Uri uri = MediaStore.Audio.Media.getContentUriForPath(to.getAbsolutePath());
						Uri uri2= cResolver.insert(uri, values);
						
						disableMediaPlayer = false;
						updateProgressBar();
						refreshSongList();
						
						/*
						}
						else
						{
							
						}*/
						
					}
				});

		alertSaveSong.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						alertCancelSong.show();

					}
				});

		alertCancelSong.setPositiveButton("Yes, Delete Song",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						File dir = Environment.getExternalStorageDirectory();
						
						File file = new File(dir,
								"/Music/testing.mp3");
						
						boolean deleted = file.delete();
					
						disableMediaPlayer = false;
						
						updateProgressBar();

					}
				});

		alertCancelSong.setNegativeButton("No, Save Song",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						((ViewGroup) input.getParent()).removeView(input);
						alertSaveSong.show();
					}
				});

		alertSaveSong.show();
	}

	@Override
	public void onPause() {
		super.onPause();

		if(mRecMicToMp3!=null)
		{
		mRecMicToMp3.stop();
		}

		paused = true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_end:
			stopService(playIntent);
			musicServ = null;
			if(mRecMicToMp3!=null)
			{
			mRecMicToMp3.stop();
			}
			System.exit(0);
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {

		int currentPosition = 0;

		switch (v.getId()) {
		case R.id.record_button:

			//stop playing any music
			
			onRecord(mStartRecording);
			if (mStartRecording) {
				mRecordButton.setText("Stop recording");
			} else {
				mRecordButton.setText("RECORD");
			}

			mStartRecording = !mStartRecording;

			break;

		case R.id.btnForward:
			if(disableMediaPlayer == false)
			{
			if (musicServ != null) {
				
				 if(startedPlayingOnce == false)
			        {
			        	String songTitle = songList.get(musicServ.getSongIndex()).getTitle();
				        theTitle.setText(songTitle);
				        
			        	songProgressBar.setProgress(0);
						songProgressBar.setMax(100);

						// start the thread which continually updates the progress bar
						updateProgressBar();
						startedPlayingOnce = true;
						btnPlay.setImageResource(R.drawable.btn_pause);
			        }
				currentPosition = getCurrentPosition();
				if (currentPosition + seekForwardTime <= getDuration()) {
					musicServ.seek(currentPosition + seekForwardTime);
				} else {
					musicServ.seek(getDuration());
				}
			}
			}
			else
			{
				Toast.makeText(this, "Audio recording in progress!",
						 Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btnBackward:
			if(disableMediaPlayer == false)
			{
			if (musicServ != null) {
			
		        if(startedPlayingOnce == false)
		        {
		        	String songTitle = songList.get(musicServ.getSongIndex()).getTitle();
			        theTitle.setText(songTitle);
			        
		        	songProgressBar.setProgress(0);
					songProgressBar.setMax(100);

					// start the thread which continually updates the progress bar
					updateProgressBar();
					startedPlayingOnce = true;
					btnPlay.setImageResource(R.drawable.btn_pause);
		        }
				currentPosition = getCurrentPosition();
				if (currentPosition - seekBackwardTime >= 0) {
					musicServ.seek(currentPosition - seekBackwardTime);
				} else {
					musicServ.seek(0);
				}
			}
			}
			else
			{
				Toast.makeText(this, "Audio recording in progress!",
						 Toast.LENGTH_SHORT).show();
			}

			break;

		case R.id.btnNext:
			if(disableMediaPlayer == false)
			{
			playNext();
				
			}
			else
			{
				Toast.makeText(this, "Audio recording in progress!",
						 Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btnPrevious:
			if(disableMediaPlayer == false)
			{
				
			
			playPrev();
			}
			else
			{
				Toast.makeText(this, "Audio recording in progress!",
						 Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btnPlay:
			if(disableMediaPlayer == false)
			{
			// if the music is already playing, pause player
			if (isPlaying()) {
				if (musicServ != null) {
					musicServ.pausePlayer();
					btnPlay.setImageResource(R.drawable.btn_play);
					playbackPaused = true;
				}
			} else// if the music is not playing
			{
				if (musicServ != null && musicBound) {
					if(startedPlayingOnce == false)
					{
						startedPlayingOnce = true;
						musicServ.setSong(0);
						 
						musicServ.playSong();
						
						// because music is playing, set the play button to display pause
						btnPlay.setImageResource(R.drawable.btn_pause);

						String songTitle = songList.get(musicServ.getPosn()).getTitle();
				        theTitle.setText(songTitle);
						// reset the progress bar
						songProgressBar.setProgress(0);
						songProgressBar.setMax(100);

						// start the thread which continually updates the progress bar
						updateProgressBar();
						
						playbackPaused = false;
			
					}
					else
					{

						musicServ.go();
						btnPlay.setImageResource(R.drawable.btn_pause);
						playbackPaused = false;
			
					}
					
				}

			}

		}
		
		else
		{
			Toast.makeText(this, "Audio recording in progress!",
					 Toast.LENGTH_SHORT).show();
		}
			break;
		}

	}

	// play next
	private void playNext() {
		musicServ.playNext();
		String songTitle = songList.get(musicServ.getSongIndex()).getTitle();
        theTitle.setText(songTitle);
        if(startedPlayingOnce == false)
        {
        	songProgressBar.setProgress(0);
			songProgressBar.setMax(100);

			// start the thread which continually updates the progress bar
			updateProgressBar();
			startedPlayingOnce = true;
			btnPlay.setImageResource(R.drawable.btn_pause);
        }

		if (playbackPaused) {

			playbackPaused = false;
		}
	}

	// play previous
	private void playPrev() {
		musicServ.playPrev();
		String songTitle = songList.get(musicServ.getSongIndex()).getTitle();
        theTitle.setText(songTitle);
        
        if(startedPlayingOnce == false)
        {
        songProgressBar.setProgress(0);
		songProgressBar.setMax(100);

		// start the thread which continually updates the progress bar
		updateProgressBar();
		startedPlayingOnce = true;
		btnPlay.setImageResource(R.drawable.btn_pause);
        }
		if (playbackPaused) {

			playbackPaused = false;
		}

	}

	@Override
	public void onResume() {

		super.onResume();

		refreshSongList();
		if (paused) {

			paused = false;
		}
	}

	@Override
	public void onStop() {

		super.onStop();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

		// reset the progress bar accordingly
		mHandler.removeCallbacks(mUpdateTimeTask);

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		if(disableMediaPlayer == false)
		{
		mHandler.removeCallbacks(mUpdateTimeTask);
		if (musicServ != null) {
			int totalDuration = getDuration();
			int currentPosition = utils.progressToTimer(seekBar.getProgress(),
					totalDuration);

			// forward or backward to certain seconds
			musicServ.seek(currentPosition);

			// update timer progress again
			updateProgressBar();
		}
		}
		else
		{
			Toast.makeText(this, "Audio recording in progress!",
					 Toast.LENGTH_SHORT).show();
		}

	}

	public void updateProgressBar() {
		// run the runnable on the handler after 100ms
		if(disableMediaPlayer == false)
		{
		mHandler.postDelayed(mUpdateTimeTask, 100);
		}
		else
		{
			Toast.makeText(this, "Audio recording in progress!",
					 Toast.LENGTH_SHORT).show();
		}
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		// runs in the background to update the progress bar
		public void run() {
			
			if(playbackPaused == false)
			{
				long totalDuration = getDuration();
				long currentDuration = getCurrentPosition();
				
				// Displaying Total Duration time
				songTotalDurationLabel.setText(""
						+ utils.milliSecondsToTimer(totalDuration));
				// Displaying time completed playing
				songCurrentDurationLabel.setText(""
						+ utils.milliSecondsToTimer(currentDuration));

				// Updating progress bar
				int progress = (int) (utils.getProgressPercentage(
						currentDuration, totalDuration));
				
				if(progress == 0)
				{
					String songTitle = songList.get(musicServ.getSongIndex()).getTitle();
			        theTitle.setText(songTitle);
				}
				

				songProgressBar.setProgress(progress);

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, 100);
			
			}
			else
			{
				long totalDuration = getDurationWhenPaused();
				long currentDuration = getCurrentPositionWhenPaused();
				
				// Displaying Total Duration time
				songTotalDurationLabel.setText(""
						+ utils.milliSecondsToTimer(totalDuration));
				// Displaying time completed playing
				songCurrentDurationLabel.setText(""
						+ utils.milliSecondsToTimer(currentDuration));

				// Updating progress bar
				int progress = (int) (utils.getProgressPercentage(
						currentDuration, totalDuration));

				songProgressBar.setProgress(progress);
				
				if(progress == 0)
				{
					String songTitle = songList.get(musicServ.getSongIndex()).getTitle();
			        theTitle.setText(songTitle);
				}

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, 100);
			
			}

				
		}
	};
	
	public int getCurrentPositionWhenPaused()
	{
		if(musicServ!=null && musicBound)
		{
			return musicServ.getPosn();
		}

		else
		{
			return 0;
		}
	}
	public int getCurrentPosition()
	{
		if(musicServ!=null && musicBound && musicServ.isPng())
		{
			return musicServ.getPosn();
		}

		else
		{
			return 0;
		}
	}
	
	public int getDurationWhenPaused()
	{
		if(musicServ!=null && musicBound)
		{
			return musicServ.getDur();
		}

		else
		{
			return 0;
		}
	}
	public int getDuration()
	{
		if(musicServ!=null && musicBound && musicServ.isPng())
		{
			return musicServ.getDur();
		}

		else
		{
			return 0;
		}
	}
	
	public boolean isPlaying()
	{
		if(musicServ!=null && musicBound)
		{
			return musicServ.isPng();
		}
		return false;
	}
	
	public void refreshSongList()
	{
		songList.clear();
		getSongList();
		// sort songs alphabetically
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});
	
		songAdt.updateAdapter(songList);
	}


}
