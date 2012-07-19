/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.bittorrent.websearch.soundcloud;

/*
{
   "id":17768842,
   "uid":"OJ5kG8UAXgDg",
   "user":{
      "username":"Rosine",
      "permalink":"rosine"
   },
   "uri":"/rosine/shakira-ft-pitbull-rabiosa",
   "duration":175713,
   "token":"gK6xL",
   "name":"shakira-ft-pitbull-rabiosa",
   "title":"Shakira ft. Pitbull - Rabiosa",
   "commentable":true,
   "revealComments":true,
   "commentUri":"/rosine/shakira-ft-pitbull-rabiosa/comments/",
   "streamUrl":"http://media.soundcloud.com/stream/OJ5kG8UAXgDg?stream_token=gK6xL",
   "waveformUrl":"http://w1.sndcdn.com/OJ5kG8UAXgDg_m.png",
   "propertiesUri":"/rosine/shakira-ft-pitbull-rabiosa/properties/",
   "statusUri":"/transcodings/OJ5kG8UAXgDg",
   "replacingUid":null,
   "preprocessingReady":true,
   "renderingFailed":false,
   "isPublic":true,
   "geo":[

   ],
   "commentableByUser":true,
   "favorite":false,
   "followingTrackOwner":false
}
 */
/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SoundcloudItem {

    public int id;
    public String uid;
    public String uri;
    public int duration;
    public String token;
    public String name;
    public String title;
    public String streamUrl;
}
