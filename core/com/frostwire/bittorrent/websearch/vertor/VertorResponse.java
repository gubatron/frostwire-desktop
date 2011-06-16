package com.frostwire.bittorrent.websearch.vertor;

import java.util.List;

/**
{ "results":[
   {"name":"...",
   "cdate":"8 Jun 11",
   "seeds":"733",
   "leechers":"287",
   "size":"105166808",
   "url":"...",
   "download":"...",
   "category":"Music"},
*/
public class VertorResponse {
    public List<VertorItem> results;
}
