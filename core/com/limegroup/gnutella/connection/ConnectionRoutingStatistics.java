package com.limegroup.gnutella.connection;


/**
 * Defines the interface to retrieve statistics about
 * {@link QueryRouteTable QueryRouteTables} that are sent and retrieved over a
 * {@link RoutedConnection}.
 */
public interface ConnectionRoutingStatistics {

    /**
     * Accessor for the last QueryRouteTable's percent full.
     */
    public double getQueryRouteTablePercentFull();

    /**
     * Accessor for the last QueryRouteTable's size.
     */
    public int getQueryRouteTableSize();

    /**
     * Accessor for the last QueryRouteTable's Empty Units.
     */
    public int getQueryRouteTableEmptyUnits();

    /**
     * Accessor for the last QueryRouteTable's Units In Use.
     */
    public int getQueryRouteTableUnitsInUse();

    /**
     * Returns the system time that we should next forward a query route table
     * along this connection. Only valid if isClientSupernodeConnection() is
     * true.
     */
    public long getNextQRPForwardTime();

    /**
     * Increments the next time we should forward query route tables for this
     * connection. This depends on whether or not this is a connection to a leaf
     * or to an Ultrapeer.
     * 
     * @param curTime the current time in milliseconds, used to calculate the
     *        next update time
     */
    public void incrementNextQRPForwardTime(long curTime);

}
