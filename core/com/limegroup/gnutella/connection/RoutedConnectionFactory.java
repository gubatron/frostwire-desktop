package com.limegroup.gnutella.connection;

import java.net.Socket;


public interface RoutedConnectionFactory {

    public RoutedConnection createRoutedConnection(String host, int port);


    public RoutedConnection createRoutedConnection(Socket socket);

}