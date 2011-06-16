package com.frostwire.bittorrent.websearch.btjunkie;

/**
{ "results": [
 {
  "title": "...",
   "date": "Sat, 10 Oct 2009 00:00:00 +0000",
   "peers": 131,
   "seeds": 630,
   "category": "Audio",
   "cdp": "...",
   "comments": 83,
   "size": 99614720,
   "votes": 999,
   "download": "...",
   "hash": "..."
 },
*/
public class BTJunkieItem {
    public String title;
    public String date;
    public int peers;
    public int seeds;
    public String category;
    public String cdp;
    public int comments;
    public long size;
    public int votes;
    public String download;
    public String hash;
}
