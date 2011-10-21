package org.limewire.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Fix an issue with Linux and IPv6
 */
public class LinuxDnsContextFactory implements InitialContextFactory {

    private InitialContextFactory delegate;

    public LinuxDnsContextFactory() {
        try {
            delegate = (InitialContextFactory) Class.forName("com.sun.jndi.dns.DnsContextFactory").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert delegate != null;
    }

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Context context = delegate.getInitialContext(environment);
        fixContext(context);
        return context;
    }

    private void fixContext(Context context) {
        try {
            Class<?> clazz = context.getClass();
            if (clazz.getName().equals("com.sun.jndi.dns.DnsContext")) {
                Field serversField = clazz.getDeclaredField("servers");
                serversField.setAccessible(true);
                String[] servers = (String[]) serversField.get(context);
                servers = filterServers(servers);
                serversField.set(context, servers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] filterServers(String[] servers) {
        List<String> result = new ArrayList<String>(servers.length);
        for (String s : servers) {
            try {
                int index = s.indexOf(":");
                if (index != -1) {
                    s = s.substring(0, index);
                }
                if (NetworkUtils.isDottedIPV4(s)) {
                    result.add(s);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return result.toArray(new String[0]);
    }
}
