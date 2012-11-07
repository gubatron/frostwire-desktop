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

package com.frostwire.gui.upnp;

import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteDeviceIdentity;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;

import com.frostwire.gui.upnp.desktop.DesktopUPnPManager;
import com.frostwire.util.JsonUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public abstract class UPnPManager {

    private static final Logger LOG = Logger.getLogger(UPnPManager.class.getName());

    protected UPnPRegistryListener registryListener;
    private final ServiceId deviceInfoId;

    private static UPnPManager instance = new DesktopUPnPManager();

    public static UPnPManager instance() {
        return instance;
    }

    protected UPnPManager() {
        this.registryListener = new UPnPRegistryListener() {
            @Override
            protected void handleDevice(Device<?, ?, ?> device, boolean added) {
                UPnPManager.this.handleDevice(device, added);
            }
        };

        this.deviceInfoId = new UDAServiceId("UPnPFWDeviceInfo");
    }

    public abstract UpnpService getService();

    public abstract UPnPFWDevice getUPnPLocalDevice();

    public abstract PingInfo getLocalPingInfo();

    public abstract void refreshPing();

    protected abstract void handlePeerDevice(PingInfo p, InetAddress address, boolean added);

    private void handleDevice(Device<?, ?, ?> device, boolean added) {
        if (added) {
            Service<?, ?> deviceInfo;
            if ((deviceInfo = device.findService(deviceInfoId)) != null) {
                invokeGetPingInfo(getService(), deviceInfo, added);
                subscribeToDeviceInfo(getService(), deviceInfo);
            }
        } else {
            InetAddress address = getAddressFromDevice(device);
            handlePeerDevice(null, address, added);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void invokeSetPingInfo(UpnpService service, final Device<?, ?, ?> device) {
        Service<?, ?> deviceInfo = device.findService(deviceInfoId);

        ActionInvocation<?> actionInvocation = new ActionInvocation(deviceInfo.getAction("SetPingInfo"));

        service.getControlPoint().execute(new ActionCallback(actionInvocation) {
            @Override
            public void success(ActionInvocation invocation) {
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                LOG.info(defaultMsg);
            }
        });
    }

    private InetAddress getAddressFromDevice(Device<?, ?, ?> device) {
        InetAddress address = null;

        try {
            if (device instanceof RemoteDevice) {
                address = InetAddress.getByName(((RemoteDeviceIdentity) device.getIdentity()).getDescriptorURL().getHost());
            } else {
                address = InetAddress.getByName("0.0.0.0");
            }
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Unable to get ip address from device info", e);
        }

        return address;
    }

    private void onPingInfo(String json, Device<?, ?, ?> device, boolean added) {
        try {
            PingInfo p = JsonUtils.toObject(json, PingInfo.class);
            InetAddress address = getAddressFromDevice(device);

            handlePeerDevice(p, address, added);
        } catch (Throwable e) {
            LOG.log(Level.INFO, "Error processing ping info", e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void invokeGetPingInfo(UpnpService service, final Service<?, ?> deviceInfo, final boolean added) {
        Action<?> action = deviceInfo.getAction("GetPingInfo");
        if (action == null) {
            return;
        }
        ActionInvocation<?> actionInvocation = new ActionInvocation(action);

        service.getControlPoint().execute(new ActionCallback(actionInvocation) {
            @Override
            public void success(ActionInvocation invocation) {
                try {
                    String json = invocation.getOutput()[0].toString();
                    onPingInfo(json, deviceInfo.getDevice(), added);
                } catch (Throwable e) {
                    LOG.log(Level.INFO, "Error processing GetPingInfo return", e);
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                LOG.info(defaultMsg);
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void subscribeToDeviceInfo(UpnpService service, final Service<?, ?> deviceInfo) {
        // 1 min timeout
        service.getControlPoint().execute(new SubscriptionCallback(deviceInfo, 60) {
            @Override
            protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
                LOG.log(Level.INFO, "failed subscrition to device info");
            }

            @Override
            protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
                // ignore
            }

            @Override
            protected void eventReceived(GENASubscription subscription) {
                Map<String, StateVariableValue> stateValues = subscription.getCurrentValues();
                StateVariableValue stateValue = stateValues.get("PingInfo");

                Object value = stateValue.getValue();
                if (value instanceof String) {
                    String json = (String) value;
                    onPingInfo(json, deviceInfo.getDevice(), true);
                }
            }

            @Override
            protected void established(GENASubscription subscription) {
                LOG.log(Level.INFO, "Established subscrition to device info with id=" + subscription.getSubscriptionId());
            }

            @Override
            protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
            }
        });
    }
}
