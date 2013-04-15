package com.frostwire.gui.upnp.desktop;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.limewire.concurrent.ThreadPoolExecutor;

public class DesktopUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

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
}
