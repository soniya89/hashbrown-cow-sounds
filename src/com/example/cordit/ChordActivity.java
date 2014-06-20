package com.example.cordit;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChordActivity extends Activity implements OnClickListener {

	private EditText chordTextView;
	private Button deleteSongButton;
	private Button saveNoteButton;
	private Button deleteNoteButton;
	private String songTitle;
	private TextView titleView;
	private AlertDialog.Builder alertSaveNote;
	private AlertDialog.Builder alertDeleteNote;
	private AlertDialog.Builder alertDeleteSong;
	
	
	@Override
	public void onCreate(Bundle data) {
		super.onCreate(data);

		setContentView(R.layout.activity_chord);
		getWindow().getDecorView().setBackgroundColor(Color.DKGRAY);
		
		Bundle bundle = getIntent().getExtras();
		songTitle = bundle.getString("song_title");
		
		titleView = (TextView)findViewById(R.id.song_title_note);
		titleView.setText(songTitle);
		
		chordTextView = (EditText)findViewById(R.id.chord_text);
		chordTextView.setBackgroundColor(Color.WHITE);
		chordTextView.setHint("Note about the song...");
		
		deleteSongButton = (Button)findViewById(R.id.delete_song_button);
		deleteNoteButton = (Button)findViewById(R.id.delete_note_button);
		saveNoteButton = (Button)findViewById(R.id.save_button);
		
		deleteSongButton.setOnClickListener(this);
		deleteNoteButton.setOnClickListener(this);
		saveNoteButton.setOnClickListener(this);
		
		alertSaveNote = new AlertDialog.Builder(this);
		alertSaveNote.setTitle("Save note?");

		alertDeleteNote = new AlertDialog.Builder(this);	
		alertDeleteNote.setTitle("Delete note?");
		
		alertDeleteSong = new AlertDialog.Builder(this);
		alertDeleteSong.setTitle("Delete song?");

		alertSaveNote
				.setMessage("Do you want to save this note?");

		alertSaveNote.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						
					}
				});
		
}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.delete_note_button:
			break;
		case R.id.delete_song_button:
			
			File dir = Environment.getExternalStorageDirectory();
			
			File file = new File(dir,
					"/Music/" + songTitle + ".mp3");
			
			boolean deleted = file.delete();
			if(deleted == true)
			{
				Toast.makeText(this, songTitle + " deleted!",
						 Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(this, "delete unsuccessful",
						 Toast.LENGTH_SHORT).show();
			}
			
			break;
		case R.id.save_button:
			break;
		}
		
	}
}
