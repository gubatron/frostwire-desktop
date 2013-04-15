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

package com.frostwire.gui.upnp.desktop;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.DefaultUpnpServiceConfiguration.ClingThreadFactory;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.message.IncomingDatagramMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.discovery.OutgoingSearchResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ProtocolFactoryImpl;
import org.fourthline.cling.protocol.ReceivingAsync;
import org.fourthline.cling.protocol.async.ReceivingSearch;
import org.limewire.concurrent.ThreadPoolExecutor;

import com.frostwire.gui.upnp.UPnPFWDevice;
import com.frostwire.gui.upnp.UPnPManager;
import com.frostwire.gui.upnp.UPnPRegistryListener;
import com.limegroup.gnutella.settings.LibrarySettings;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class UPnPService implements Runnable {

    private static final Logger LOG = Logger.getLogger(UPnPService.class.getName());

    final static String HACK_STREAM_HANDLER_SYSTEM_PROPERTY = "hackStreamHandlerProperty";

    private UpnpService service;
    private UPnPRegistryListener registryListener;

    private static LocalDevice localDevice;

    private boolean running;

    public UPnPService(UPnPRegistryListener registryListener) {
        this.registryListener = registryListener;
    }

    public UpnpService getService() {
        return service;
    }

    public static LocalDevice getLocalDevice() {
        if (localDevice == null) {
            localDevice = createLocalDevice();
        }
        return localDevice;
    }

    public void start() {
        if (service == null) {
            Thread t = new Thread(this);
            t.setDaemon(false);
            t.start();
        }
    }

    public void run() {
        try {

            running = true;

            // This is to disable the set of URL URLStreamHandlerFactory
            // inside StreamClientImpl. Now handled with new coded added to
            // azureus core.
            System.setProperty(HACK_STREAM_HANDLER_SYSTEM_PROPERTY, "alreadyWorkedAroundTheEvilJDK");

            service = new UpnpServiceImpl(new DefaultUpnpServiceConfiguration() {
                @Override
                protected ExecutorService createDefaultExecutorService() {
                    return UPnPService.this.createFrostWireExecutor();
                }
            }) {
                @Override
                protected ProtocolFactory createProtocolFactory() {

                    return new ProtocolFactoryImpl(this) {
                        @Override
                        protected ReceivingAsync createReceivingSearch(IncomingDatagramMessage<UpnpRequest> incomingRequest) {
                            return new ReceivingSearch(getUpnpService(), incomingRequest) {
                                @Override
                                protected List<OutgoingSearchResponse> createServiceTypeMessages(LocalDevice device, NetworkAddress activeStreamServer) {
                                    List<OutgoingSearchResponse> result = Collections.emptyList();
                                    try {
                                        result = super.createServiceTypeMessages(device, activeStreamServer);
                                    } catch (Throwable e) {
                                    }
                                    return result;
                                }
                            };
                        }
                    };
                }
            };

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    running = false;
                    service.shutdown();
                }
            });

            if (LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.getValue()) {
                this.service.getRegistry().addDevice(getLocalDevice());

                // refresh the list with all known devices
                for (Device<?, ?, ?> device : this.service.getRegistry().getDevices()) {
                    registryListener.deviceAdded(device);
                }

                // getting ready for future device advertisements
                this.service.getRegistry().addListener(registryListener);
            }

            while (running) {
                Thread.sleep(5000);

                if (LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.getValue()) {
                    this.service.getControlPoint().search();
                }
            }
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Exception occured with the UPnP framework", e);
        }
    }

    protected ExecutorService createFrostWireExecutor() {
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

    private static LocalDevice createLocalDevice() {
        try {
            UPnPFWDevice device = UPnPManager.instance().getUPnPLocalDevice();

            return new LocalDevice(device.getIdentity(), device.getType(), device.getDetails(), device.getIcon(), device.getServices());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
