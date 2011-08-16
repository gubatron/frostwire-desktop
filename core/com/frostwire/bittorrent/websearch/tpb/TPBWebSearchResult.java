package com.frostwire.bittorrent.websearch.tpb;

import java.util.regex.Matcher;

import com.frostwire.bittorrent.websearch.WebSearchResult;

public class TPBWebSearchResult implements WebSearchResult {

	private String fileName;
	private String torrentDetailsURI;
	private String torrentURI;
	private String infoHash;
	private long size;
	private long creationTime;
	private int seeds;

	public TPBWebSearchResult(Matcher matcher) {
		/*
		 * Matcher groups cheatsheet
		 * 1 -> Category (useless)
		 * 2 -> Torrent Details Page
	     * 3 -> Title/Name
	     * 4 -> .torrent URL
         * 5 -> infoHash
         * 6 -> MM-DD&nbsp;YYYY or Today&nbsp;HH:MM or Y-day&nbsp;HH:MM 
         * 7 -> SIZE&nbsp;(B|KiB|MiBGiB)
         * 8 -> seeds
		 */
		torrentDetailsURI = matcher.group(2);
		fileName = matcher.group(3);
		torrentURI = matcher.group(4);
		System.out.println("Hash: " + matcher.group(5));
		infoHash = matcher.group(5);
		creationTime = parseCreationTime(matcher.group(6));
		size = parseSize(matcher.group(7));
		seeds = parseSeeds(matcher.group(8));
		
		
	}
	
	
	private long parseSize(String group) {
		System.out.println("Size: " + group);
		return 0;
	}


	private int parseSeeds(String group) {
		System.out.println("Seeds: " + group);
		return 0;
	}


	private long parseCreationTime(String group) {
		System.out.println("Creation Time: " + group);
		return 0;
	}


	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getVendor() {
		return "TPB";
	}

	@Override
	public String getFilenameNoExtension() {
		return fileName;
	}

	@Override
	public String getHash() {
		return infoHash;
	}

	@Override
	public String getTorrentURI() {
		return torrentURI;
	}

	@Override
	public int getSeeds() {
		return seeds;
	}

	@Override
	public String getTorrentDetailsURL() {
		return torrentDetailsURI;
	}

}
