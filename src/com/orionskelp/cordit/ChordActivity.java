package com.orionskelp.cordit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.orionskelp.cordit.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
	private AlertDialog.Builder alertDeleteNote;
	private AlertDialog.Builder alertDeleteSong;

	@Override
	public void onCreate(Bundle data) {
		super.onCreate(data);

		setContentView(R.layout.activity_chord);
		getWindow().getDecorView().setBackgroundColor(Color.DKGRAY);

		Bundle bundle = getIntent().getExtras();
		songTitle = bundle.getString("song_title");

		titleView = (TextView) findViewById(R.id.song_title_note);
		titleView.setText(songTitle);

		chordTextView = (EditText) findViewById(R.id.chord_text);
		chordTextView.setBackgroundColor(Color.GRAY);
		chordTextView.setHint("Note about the song...");
		chordTextView.setHintTextColor(Color.WHITE);

		loadNote();

		deleteSongButton = (Button) findViewById(R.id.delete_song_button);
		deleteNoteButton = (Button) findViewById(R.id.delete_note_button);
		saveNoteButton = (Button) findViewById(R.id.save_button);

		deleteSongButton.setOnClickListener(this);
		deleteNoteButton.setOnClickListener(this);
		saveNoteButton.setOnClickListener(this);

		alertDeleteNote = new AlertDialog.Builder(this);
		alertDeleteNote.setTitle("Delete note?");

		alertDeleteSong = new AlertDialog.Builder(this);
		alertDeleteSong.setTitle("Delete song?");

		alertDeleteNote
				.setMessage("Do you want to delete this note permanently?");

		alertDeleteNote.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						deleteNote(false);

					}
				});
		alertDeleteNote.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

		alertDeleteSong
				.setMessage("Do you want to delete this song permanently?");

		alertDeleteSong.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						deleteSong();

					}
				});
		alertDeleteSong.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.delete_note_button:

			alertDeleteNote.show();

			break;
		case R.id.delete_song_button:

			alertDeleteSong.show();

			break;
		case R.id.save_button:

			saveNote();

			break;
		}

	}

	public void loadNote() {

		File dir = Environment.getExternalStorageDirectory();
		File noteFile = new File(dir, "/cordit/" + songTitle + ".txt");

		boolean noteExists = noteFile.exists();

		if (noteExists == true) {
			FileInputStream in;
			try {
				in = new FileInputStream(noteFile);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));

				String dataRow = "";
				String buffer = "";

				while ((dataRow = reader.readLine()) != null) {
					buffer = buffer + dataRow + "\n";
				}

				chordTextView.setText(buffer);

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void saveNote() {
		try {
			File dir = Environment.getExternalStorageDirectory();
			File file = new File(dir, "/cordit/");

			file.mkdirs();

			File noteFile = new File(file, songTitle + ".txt");

			FileOutputStream out = new FileOutputStream(noteFile);
			OutputStreamWriter writer = new OutputStreamWriter(out);

			writer.append(chordTextView.getText());
			writer.close();
			out.close();

			Toast.makeText(this, songTitle + " saved!!", Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			Toast.makeText(this, "File save unsuccessful", Toast.LENGTH_SHORT)
					.show();

		}
	}

	public void deleteNote(boolean fromDeleteSong) {
		// check if there is a note

		// delete note from sd card
		File dir = Environment.getExternalStorageDirectory();
		File file = new File(dir, "/cordit/" + songTitle + ".txt");

		if (file.exists()) {

			boolean deleted = file.delete();

			chordTextView.setText("");
			if (deleted == true) {

				Toast.makeText(this, songTitle + " note deleted!",
						Toast.LENGTH_SHORT).show();
				
			} else {
				Toast.makeText(this, "Delete note unsuccessful",
						Toast.LENGTH_SHORT).show();
				
			}
		}
		else
		{
			if(fromDeleteSong == false)
			{
			Toast.makeText(this, "No note to delete",
					Toast.LENGTH_SHORT).show();
			}
			
		}
	}

	public void deleteSong() {
		deleteNote(true);

		File dir = Environment.getExternalStorageDirectory();

		File file = new File(dir, "/Music/" + songTitle + ".mp3");

		String[] retCol = { MediaStore.Audio.Media._ID };
		Cursor cur = this.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, retCol,
				MediaStore.MediaColumns.DATA + "='" + file + "'", null, null);

		if (cur.getCount() == 0) {
			return;
		}

		cur.moveToFirst();
		int id = cur.getInt(cur.getColumnIndex(MediaStore.MediaColumns._ID));
		cur.close();

		Uri uri = ContentUris.withAppendedId(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
		this.getContentResolver().delete(uri, null, null);

		file.delete();
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   
		this.startActivity(intent);
		
		//System.exit(0);
		finish();

	}
	

}
