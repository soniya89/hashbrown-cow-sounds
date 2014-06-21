package com.example.cordit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
	private AlertDialog.Builder alertSaveNote;

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

		alertDeleteNote = new AlertDialog.Builder(this);	
		alertDeleteNote.setTitle("Delete note?");
		
		alertDeleteSong = new AlertDialog.Builder(this);
		alertDeleteSong.setTitle("Delete song?");
		
		alertSaveNote = new AlertDialog.Builder(this);
		alertSaveNote.setTitle("Save note?");

		alertDeleteNote
				.setMessage("Do you want to delete this note permanently?");

		alertDeleteNote.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						deleteNote();
						
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
		
		alertSaveNote
		.setMessage("Do you want to save your note?");
		
		alertSaveNote.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						saveNote();
					
					}
				});
		alertSaveNote.setNegativeButton("No",
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
		switch(v.getId())
		{
		case R.id.delete_note_button:
			
			alertDeleteNote.show();
			
			break;
		case R.id.delete_song_button:
			
			alertDeleteSong.show();
			
			break;
		case R.id.save_button:

			alertSaveNote.show();
		
			Toast.makeText(this, "Note saved!",
					 Toast.LENGTH_SHORT).show();
			
			break;
		}
		
	}
	
	public void loadNote() throws IOException
	{
		File dir = Environment.getExternalStorageDirectory();
		
		File noteFile = new File(dir,
				"/cordit/" + songTitle + ".txt");
		
		boolean noteExists = noteFile.exists();
		
		if(noteExists == true)
		{
			FileInputStream in = new FileInputStream(noteFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			String dataRow = "";
			String buffer = "";
			
			while((dataRow = reader.readLine()) != null)
			{
				buffer = buffer + dataRow + "\n";
			}
			
			chordTextView.setText(buffer);
			reader.close();
		}

	}
	public void saveNote()
	{
		try
		{
			File dir = Environment.getExternalStorageDirectory();
			
			File noteFile = new File(dir,
					"/cordit/" + songTitle + ".txt");
			noteFile.createNewFile();
			FileOutputStream out = new FileOutputStream(noteFile);
			OutputStreamWriter writer = new OutputStreamWriter(out);
			
			writer.append(chordTextView.getText());
			writer.close();
			out.close();
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void deleteNote()
	{
		//delete note from sd card
		File dir = Environment.getExternalStorageDirectory();
		
		File file = new File(dir,
				"/cordit/" + songTitle + ".txt");
		
		boolean deleted = file.delete();
		if(deleted == true)
		{
			/*
			Toast.makeText(this, songTitle + " deleted!",
					 Toast.LENGTH_SHORT).show();*/
		}
		else
		{
			/*Toast.makeText(this, "delete unsuccessful",
					 Toast.LENGTH_SHORT).show();*/
		}
	}
	
	public void deleteSong()
	{
		//delete note from sd card
		File dir = Environment.getExternalStorageDirectory();
		
		File file = new File(dir,
				"/Music/" + songTitle + ".mp3");
		
		boolean deleted = file.delete();
		
		DeleteRecursive(file);
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
		
		Intent intent = new Intent(this, ChordActivity.class);
		intent.putExtra("song_title", songTitle);
		this.startActivity(intent);
		
	}
	
	public static void DeleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory()) 
        {
            for (File child : fileOrDirectory.listFiles())
            {
                DeleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }
	
	@Override
    public void onBackPressed()
    {
        super.onBackPressed();
        
        //check if there is text
    }
}
