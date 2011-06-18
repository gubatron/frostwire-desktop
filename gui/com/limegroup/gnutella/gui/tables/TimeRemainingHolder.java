package com.limegroup.gnutella.gui.tables;

import org.limewire.util.CommonUtils;


/**
 * simple class to store the numeric value of time remaining (or ETA)
 * used so we can sort by a value, but display a human-readable time.
 * @author sberlin
 */
public final class TimeRemainingHolder implements Comparable<TimeRemainingHolder> {
	
	private long _timeRemaining;
	
	public TimeRemainingHolder(long intValue) 
	{
		_timeRemaining = intValue;
	}
	
	public int compareTo(TimeRemainingHolder o) {
	    return (int)(o._timeRemaining - _timeRemaining);
	}
	
    public String toString() {
        if (_timeRemaining < 0) {
            return "\u221E";
        } else {
            return _timeRemaining == 0 ? "" : CommonUtils.seconds2time(_timeRemaining);
        }
    }
}
