package com.frostwire.bittorrent.websearch.btjunkie;

import java.util.List;

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
public class BTJunkieResponse {
    public List<BTJunkieItem> results;
}
