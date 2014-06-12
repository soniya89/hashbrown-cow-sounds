package com.example.cordit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.example.cordit.MusicService.MusicBinder;

public class MainActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener {

	private static final String LOG_TAG = "AudioRecordTest";

	// recording
	private static String mFileName = null;
	private Button mRecordButton = null;
	private MediaRecorder mRecorder = null;

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

	private boolean musicBound = false;
	private boolean paused = false;
	private boolean playbackPaused = false;

	// media player
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private SeekBar songProgressBar;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;

	private Handler mHandler = new Handler();
	private Utilities utils;

	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds

	@Override
	public void onCreate(Bundle data) {
		super.onCreate(data);

		setContentView(R.layout.activity_main);

		// default storage location for audio file
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/Music/cordit/audiorecordtest.3gp";

		// set up record button
		mRecordButton = (Button) findViewById(R.id.record_button);
		mRecordButton.setText("RECORD");
		mRecordButton.setTextColor(Color.YELLOW);
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
		SongAdapter songAdt = new SongAdapter(this, songList);
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

		utils = new Utilities();

		songProgressBar.setOnSeekBarChangeListener(this);
		// right.setOnClickListener(this);
		// setController();

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
		musicServ.setSong(Integer.parseInt(view.getTag().toString()));
		 
		musicServ.playSong();

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
				songList.add(new Song(thisId, thisTitle, thisArtist));
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
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Prepare() failed");
		}

		mRecorder.start();

	}

	private void stopRecording() {
		// prompt user for song filename
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

						String value = input.getText().toString();
						mFileName = "/Music/cordit/" + value + ".3gp";

						// check if name exists
						// titleExists(mFileName);

						// Toast.makeText(this, mFileName,
						// Toast.LENGTH_LONG).show();

						// rename audio file
						File sdcard = Environment.getExternalStorageDirectory();
						File from = new File(sdcard,
								"Music/cordit/audiorecordtest.3gp");
						File to = new File(sdcard, mFileName);
						from.renameTo(to);

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
								"Music/cordit/audiorecordtest.3gp");
						boolean deleted = file.delete();

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

		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
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
			if (mRecorder != null) {
				mRecorder.release();
			}
			mRecorder = null;
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

			boolean mStartRecording = true;

			onRecord(mStartRecording);
			if (mStartRecording) {
				mRecordButton.setText("Stop recording");
			} else {
				mRecordButton.setText("RECORD");
			}

			mStartRecording = !mStartRecording;

			break;

		case R.id.btnForward:
			if (musicServ != null) {
				currentPosition = getCurrentPosition();
				if (currentPosition + seekForwardTime <= getDuration()) {
					musicServ.seek(currentPosition + seekForwardTime);
				} else {
					musicServ.seek(getDuration());
				}
			}
			break;

		case R.id.btnBackward:
			if (musicServ != null) {
				currentPosition = getCurrentPosition();
				if (currentPosition - seekBackwardTime >= 0) {
					musicServ.seek(currentPosition - seekBackwardTime);
				} else {
					musicServ.seek(0);
				}
			}

			break;

		case R.id.btnNext:
			playNext();
			break;

		case R.id.btnPrevious:
			playPrev();
			break;

		case R.id.btnPlay:
			// if the music is already playing, pause player
			if (isPlaying()) {
				if (musicServ != null) {
					musicServ.pausePlayer();
					btnPlay.setImageResource(R.drawable.btn_play);
					playbackPaused = true;
				}
			} else// if the music is not playing
			{
				if (musicServ != null) {
					
						musicServ.go();
						btnPlay.setImageResource(R.drawable.btn_pause);
						playbackPaused = false;
					
				}

			}

		}

	}

	// play next
	private void playNext() {
		musicServ.playNext();
		if (playbackPaused) {

			playbackPaused = false;
		}
	}

	// play previous
	private void playPrev() {
		musicServ.playPrev();
		if (playbackPaused) {

			playbackPaused = false;
		}

	}

	@Override
	public void onResume() {
		super.onResume();

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

	public void updateProgressBar() {
		// run the runnable on the handler after 100ms
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		// runs in the background to update the progress bar
		public void run() {
			
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

				songProgressBar.setProgress(progress);

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, 100);
			
		}
	};
	
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
}
