package com.example.cordit;

import java.util.ArrayList;

import com.example.cordit.R;
import com.example.cordit.Song;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter implements OnClickListener{

	private ArrayList<Song> songs;
	private LayoutInflater songInf;
	private Context context;

	public SongAdapter(Context c, ArrayList<Song> theSongs) {
		songs = theSongs;
		songInf = LayoutInflater.from(c);
		this.context = c;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return songs.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// map to song layout
		RelativeLayout songLay = (RelativeLayout) songInf.inflate(R.layout.song,
				parent, false);

		// get title and artist views
		TextView songView = (TextView) songLay.findViewById(R.id.song_title);
		TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
		ImageView right = (ImageView)songLay.findViewById(R.id.right);

		// get song using position
		Song currSong = songs.get(position);

		// get title and artist strings
		songView.setText(currSong.getTitle());
		artistView.setText(currSong.getArtist());

		// set position as tag
		songLay.setTag(position);
		right.setTag(position);
		right.setOnClickListener(this);

		return songLay;
	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent(context, ChordActivity.class);

		context.startActivity(intent);
		
	}


}
