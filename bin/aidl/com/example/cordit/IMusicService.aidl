package com.example.cordit;

interface IMusicService
{
	void stop();
    void play();
	void setDataSource(in long id);
	String getSongTitle();
	boolean isPlaying();
}