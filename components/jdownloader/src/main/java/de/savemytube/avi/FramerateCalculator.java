/**
 * JToothpaste - Copyright (C) 2007 Matthias
 * Schuhmann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package de.savemytube.avi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.log.Category;
import de.savemytube.flv.FLV;

public class FramerateCalculator {

    static Category log = Category.getInstance(FramerateCalculator.class); 
    public static Framerate calculateTrueFrameRate(List _videoTimeStamps) {
        Framerate framerate = new Framerate();
        HashMap deltaCount = new HashMap();
        int i, threshold;
        int minDelta;
        Integer delta = null;
        Integer count = null;
        // Calculate the distance between the timestamps, count how many times each delta appears
        for (i = 1; i < _videoTimeStamps.size(); i++) {
            int p1 = (int) ((Long)_videoTimeStamps.get(i)).longValue();
            int p2 = (int) ((Long)_videoTimeStamps.get(i-1)).longValue();
            int deltaS = p1 - p2;

            if (deltaS < 0) continue;
            delta = new Integer(deltaS);
            
            Integer value = (Integer)deltaCount.get(delta);
            if (value != null) {
                deltaCount.put(delta, new Integer(value.intValue()+1));
            }
            else {
                deltaCount.put(delta, new Integer(1));
            }
        }

        threshold = _videoTimeStamps.size() / 10;
        minDelta = Integer.MAX_VALUE;

        // Find the smallest delta that made up at least 10% of the frames (grouping in delta+1
        // because of rounding, e.g. a NTSC video will have deltas of 33 and 34 ms)
        Iterator it = deltaCount.keySet().iterator();
        while(it.hasNext()) {
            delta = (Integer)it.next();            
            count = (Integer) deltaCount.get(delta);
            
            Integer value = (Integer)deltaCount.get(new Integer(delta.intValue()+1));
            if (value != null) {
                count = new Integer(count.intValue()+value.intValue());
            }

            if ((count.intValue() >= threshold) && (delta.intValue() < minDelta)) {
                minDelta = delta.intValue();
            }
        }

        // Calculate the frame rate based on the smallest delta, and delta+1 if present
        if (minDelta != Integer.MAX_VALUE) {
            int totalTime, totalFrames;
            
            count = (Integer)deltaCount.get(new Integer(minDelta));
            totalTime = minDelta * count.intValue();
            totalFrames = count.intValue();

            Integer value = (Integer)deltaCount.get(new Integer(minDelta+1));
            if (value != null) {
                count = value;
                totalTime += (minDelta + 1) * count.intValue();
                totalFrames += count.intValue();
            }

            if (totalTime != 0) {
                framerate.N = totalFrames * 1000;
                framerate.D = totalTime;
                framerate.Reduce();
                log.debug(framerate.toString());
                return framerate;
            }
        }

        // Default if the frame rate couldn't be calculated
        framerate.N = 25;
        framerate.D = 1;
        log.debug(framerate.toString());
        return framerate;
    }
}
