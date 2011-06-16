package com.frostwire.bittorrent.websearch.extratorrent;

import java.util.List;

/**
{
 "title":"Extratorrent Search: shakira",
 "link":"http://extratorrent.com",
 "description":"Extratorrent Search: shakira",
 "total_results":224,
 "list":[
    {
    "title":"Shakira Waka Waka(This Time for Africa)Official 2010 FIFA 1080p ShoukaT",
    "category":"Music",
    "subcategory":"Music Videos",
    "link":"http://extratorrent.com/torrent/2246241/",
    "guid":"http://extratorrent.com/torrent/2246241/",
    "pubDate":"Wed, 09 Jun 2010 18:08:27 +0100",
    "torrentLink":"http://extratorrent.com/download/2246241/",
    "files":1,
    "comments":11,
    "hash":"3ec46f3dc2bb62caca4801bdeb6fbb2e7d3bcd5f",
    "peers":393,
    "seeds":388,
    "leechs":5,
    "size":101146107
    },
*/
public class ExtratorrentResponse {
    public List<ExtratorrentItem> list;
}
