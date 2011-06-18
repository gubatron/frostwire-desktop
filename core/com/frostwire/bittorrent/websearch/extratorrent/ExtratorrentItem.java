package com.frostwire.bittorrent.websearch.extratorrent;

/**
{
 "title":"Extratorrent Search: ...",
 "link":"http://extratorrent.com",
 "description":"Extratorrent Search: ...",
 "total_results":224,
 "list":[
    {
    "title":"...",
    "category":"Music",
    "subcategory":"Music Videos",
    "link":"...",
    "guid":"...",
    "pubDate":"Wed, 09 Jun 2010 18:08:27 +0100",
    "torrentLink":"...",
    "files":1,
    "comments":11,
    "hash":"...",
    "peers":393,
    "seeds":388,
    "leechs":5,
    "size":101146107
    },
*/
public class ExtratorrentItem {
    public String title;
    public String category;
    public String subcategory;
    public String link;
    public String guid;
    public String pubDate;
    public String torrentLink;
    public int files;
    public int comments;
    public String hash;
    public int peers;
    public int seeds;
    public int leechs;
    public long size;
    
}
