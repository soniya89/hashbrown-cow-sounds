package com.orionskelp.cordit;

import java.util.ArrayList;

import com.orionskelp.cordit.R;
import com.orionskelp.cordit.Song;
import com.orionskelp.cordit.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
		ImageView right = (ImageView)songLay.findViewById(R.id.right);

		// get song using position
		Song currSong = songs.get(position);

		// get title and artist strings
		songView.setText(currSong.getTitle());

		// set position as tag
		songLay.setTag(position);
		right.setTag(position);
		right.setOnClickListener(this);

		return songLay;
	}

	@Override
	public void onClick(View v) {

		if(MainActivity.isMediaPlayerDisabled() == false)
		{
		Intent intent = new Intent(context, ChordActivity.class);

		int songPos = Integer.parseInt(v.getTag().toString());
		intent.putExtra("song_title", songs.get(songPos).getTitle());
		context.startActivity(intent);
		}
		
	}
	
	 public void updateAdapter(ArrayList<Song> arrylst) {
	        this.songs= arrylst;

	        //and call notifyDataSetChanged
	        notifyDataSetChanged();
	    }


}
