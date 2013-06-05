package com.frostwire.gui.upnp.desktop;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.transport.impl.DatagramIOConfigurationImpl;
import org.fourthline.cling.transport.impl.DatagramIOImpl;
import org.fourthline.cling.transport.impl.DatagramProcessorImpl;
import org.fourthline.cling.transport.impl.MulticastReceiverConfigurationImpl;
import org.fourthline.cling.transport.impl.MulticastReceiverImpl;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.MulticastReceiver;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.limewire.concurrent.ThreadPoolExecutor;

public class DesktopUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    private static Logger LOG = Logger.getLogger(DesktopUpnpServiceConfiguration.class.getName());

    private static final int REGISTRY_MAINTENANCE_INTERVAL_MILLIS = 5000; // 5 seconds

    private long lastTimeIncomingSearchRequestParsed = -1;

    private final int INCOMING_SEARCH_REQUEST_PARSE_INTERVAL = 2500;

    private Map<String, Long> readResponseWindows = new LinkedHashMap<String, Long>();

    @Override
    public int getRegistryMaintenanceIntervalMillis() {
        return REGISTRY_MAINTENANCE_INTERVAL_MILLIS;
    }

    @Override
    protected ExecutorService createDefaultExecutorService() {
        return new ThreadPoolExecutor(0, 32, 30, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ClingThreadFactory()) {
            @Override
            public void execute(Runnable command) {
                try {
                    super.execute(command);
                } catch (Throwable e) {
                    //gubatron: we're catching a RejectedExecutionException until we figure out a solution.
                    //we're probably being too aggresive submitting tasks in the first place.
                }
            }
        };
    }

    @Override
    protected DatagramProcessor createDatagramProcessor() {
        return new DatagramProcessorImpl() {

            private final long WAIT_TIME = 8000;
            private final long WINDOW_SIZE = 1000;

            @Override
            protected IncomingDatagramMessage<?> readRequestMessage(InetAddress receivedOnAddress, DatagramPacket datagram, ByteArrayInputStream is, String requestMethod, String httpProtocol) throws Exception {
                //Throttle the parsing of incoming search messages.
                if (UpnpRequest.Method.getByHttpName(requestMethod).equals(UpnpRequest.Method.MSEARCH)) {
                    if (System.currentTimeMillis() - lastTimeIncomingSearchRequestParsed < INCOMING_SEARCH_REQUEST_PARSE_INTERVAL) {
                        return null;
                    } else {
                        lastTimeIncomingSearchRequestParsed = System.currentTimeMillis();
                    }
                }

                return super.readRequestMessage(receivedOnAddress, datagram, is, requestMethod, httpProtocol);
            }

            @Override
            protected IncomingDatagramMessage<?> readResponseMessage(InetAddress receivedOnAddress, DatagramPacket datagram, ByteArrayInputStream is, int statusCode, String statusMessage, String httpProtocol) throws Exception {

                IncomingDatagramMessage<?> response = null;
                String host = datagram.getAddress().getHostAddress();

                if (!readResponseWindows.containsKey(host)) {
                    response = super.readResponseMessage(receivedOnAddress, datagram, is, statusCode, statusMessage, httpProtocol);
                    readResponseWindows.put(host, System.currentTimeMillis());

                } else {
                    long windowStart = readResponseWindows.get(host);
                    long delta = System.currentTimeMillis() - windowStart;
                    if (delta >= 0 && delta < WINDOW_SIZE) {
                        response = super.readResponseMessage(receivedOnAddress, datagram, is, statusCode, statusMessage, httpProtocol);
                    } else if ((System.currentTimeMillis() - windowStart > (2 * WINDOW_SIZE) / 3)) {
                        readResponseWindows.put(host, System.currentTimeMillis() + WAIT_TIME);
                    } else {
                        //System.out.println("Come back later " + host + " !!!");
                    }
                }

                return response;
            }
        };
    }

    public DatagramIO<?> createDatagramIO(NetworkAddressFactory networkAddressFactory) {
        return new DatagramIOImpl(new DatagramIOConfigurationImpl()) {
            public void run() {
                while (true) {
                    try {
                        byte[] buf = new byte[getConfiguration().getMaxDatagramBytes()];
                        DatagramPacket datagram = new DatagramPacket(buf, buf.length);
                        socket.receive(datagram);
                        IncomingDatagramMessage<?> incomingDatagramMessage = datagramProcessor.read(localAddress.getAddress(), datagram);

                        if (incomingDatagramMessage != null) {
                            router.received(incomingDatagramMessage);
                        }

                    } catch (SocketException ex) {
                        LOG.fine("Socket closed");
                        break;
                    } catch (UnsupportedDataException ex) {
                        LOG.info("Could not read datagram: " + ex.getMessage());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                try {
                    if (!socket.isClosed()) {
                        LOG.fine("Closing unicast socket");
                        socket.close();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory networkAddressFactory) {
        return new MulticastReceiverImpl(new MulticastReceiverConfigurationImpl(networkAddressFactory.getMulticastGroup(), networkAddressFactory.getMulticastPort())) {
            public void run() {
                while (true) {
                    try {
                        byte[] buf = new byte[getConfiguration().getMaxDatagramBytes()];
                        DatagramPacket datagram = new DatagramPacket(buf, buf.length);

                        socket.receive(datagram);

                        InetAddress receivedOnLocalAddress = networkAddressFactory.getLocalAddress(multicastInterface, multicastAddress.getAddress() instanceof Inet6Address, datagram.getAddress());

                        IncomingDatagramMessage<?> incomingDatagramMessage = datagramProcessor.read(receivedOnLocalAddress, datagram);

                        if (incomingDatagramMessage != null) {
                            router.received(incomingDatagramMessage);
                        }

                    } catch (SocketException ex) {
                        LOG.info("Socket closed");
                        break;
                    } catch (UnsupportedDataException ex) {
                        LOG.info("Could not read datagram: " + ex.getMessage());
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                try {
                    if (!socket.isClosed()) {
                        LOG.info("Closing multicast socket");
                        socket.close();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }
}
