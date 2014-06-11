package com.example.cordit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
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

	private static String mFileName = null;

	private Button mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private EditText input;
	private AlertDialog.Builder alertSaveSong;
	private AlertDialog.Builder alertCancelSong;

	private MusicService musicServ;
	private Intent playIntent;

	private ArrayList<Song> songList;
	private ListView songView;

	private boolean musicBound = false;
	private boolean paused = false;
	private boolean playbackPaused = false;
	
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

		mRecordButton = (Button) findViewById(R.id.record_button);
		mRecordButton.setText("RECORD");
		mRecordButton.setTextColor(Color.YELLOW);
		mRecordButton.setOnClickListener(this);
		getWindow().getDecorView().setBackgroundColor(Color.DKGRAY);

		songView = (ListView) findViewById(R.id.song_list);

		//right = (ImageView)findViewById(R.id.right);

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

		// All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        utils = new Utilities();
        
        songProgressBar.setOnSeekBarChangeListener(this); 
        // right.setOnClickListener(this);
		//setController();

	}

	@Override
	public void onStart() {
		super.onStart();
		if (playIntent == null) {

			// start the intent
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	public void songPicked(View view) {
		musicServ.setSong(Integer.parseInt(view.getTag().toString()));
		musicServ.playSong();

		if (playbackPaused) {
			//setController();
			playbackPaused = false;
		}
		//controller.show(0);
	}

	private ServiceConnection musicConnection = new ServiceConnection() {

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

	public void getSongList() {
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
	public void onDestroy() {
		stopService(playIntent);
		musicServ = null;
		super.onDestroy();

	}

	/*
	 * @Override public void onListItemClick(ListView listView, View
	 * clickedView, int position, long id) { super.onListItemClick(listView,
	 * clickedView, position, id);
	 * 
	 * // TextView tv = (TextView)clickedView;
	 * 
	 * // String song = tv.getText().toString();
	 * 
	 * songPicked(clickedView);
	 * 
	 * }
	 */
	private void onRecord(boolean start) {
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
						// TODO Auto-generated method stub
						String value = input.getText().toString();
						// mFileName =
						// Environment.getExternalStorageDirectory().getAbsolutePath();
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

		case R.id.action_shuffle:
			musicServ.setShuffle();
			// musicServ.setShuffle();
			break;
		case R.id.action_end:
			stopService(playIntent);
			musicServ = null;
			// musicServ = null;
			System.exit(0);
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {

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

		case R.id.right:
			Toast.makeText(this, "Right Accessory " + v.getTag(),
					Toast.LENGTH_SHORT).show();
			break;
		}

	}

	// ---media player control methods START----
	/*
	@Override
	public void start() {
		musicServ.go();

	}

	@Override
	public void pause() {
		playbackPaused = true;
		musicServ.pausePlayer();

	}

	@Override
	public int getDuration() {
		if ((musicServ != null) && (musicBound) && (musicServ.isPng()))
			return musicServ.getDur();
		else
			return 0;
	}

	@Override
	public int getCurrentPosition() {

		if ((musicServ != null) && (musicBound) && (musicServ.isPng()))
			return musicServ.getPosn();
		else
			return 0;

	}

	@Override
	public void seekTo(int pos) {
		musicServ.seek(pos);

	}

	@Override
	public boolean isPlaying() {

		if ((musicServ != null) && (musicBound))
			return musicServ.isPng();
		return false;
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}
*/
	// ---media player control methods END----
/*
	private void setController() {
		controller = new MusicController(this);

		controller.setPrevNextListeners(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				playNext();
			}
		}, new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				playPrev();
			}
		});

		controller.setMediaPlayer(this);
		controller.setAnchorView(this.findViewById(R.id.record_button));
		controller.setEnabled(true);

	}
*/
	// play next
	private void playNext() {
		musicServ.playNext();
		if (playbackPaused) {
			//setController();
			playbackPaused = false;
		}
		//controller.show(0);
	}

	// play previous
	private void playPrev() {
		musicServ.playPrev();
		if (playbackPaused) {
		//	setController();
			playbackPaused = false;
		}
		//controller.show(0);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (paused) {
			//setController();
			paused = false;
		}
	}

	@Override
	public void onStop() {
	//	controller.hide();
		super.onStop();
	}

	public void updateProgressBar() {
       // mHandler.postDelayed(mUpdateTimeTask, 100);
    }   
 
    /**
     * Background Runnable thread
     * */
	
	/*
    private Runnable mUpdateTimeTask = new Runnable() {
           public void run() {
               long totalDuration = mp.getDuration();
               long currentDuration = mp.getCurrentPosition();
 
               // Displaying Total Duration time
               songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
               // Displaying time completed playing
               songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
 
               // Updating progress bar
               int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
               //Log.d("Progress", ""+progress);
               songProgressBar.setProgress(progress);
 
               // Running this thread after 100 milliseconds
               mHandler.postDelayed(this, 100);
           }
        };
     */   
	//-----seekbar change methods START-----
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	//	mHandler.removeCallbacks(mUpdateTimeTask);
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
		// mHandler.removeCallbacks(mUpdateTimeTask);
	      //  int totalDuration = mp.getDuration();
	      //  int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
	 
	        // forward or backward to certain seconds
	     //   mp.seekTo(currentPosition);
	 
	        // update timer progress again
	        updateProgressBar();
		
	}
//------seekbar change methods END---------

}
