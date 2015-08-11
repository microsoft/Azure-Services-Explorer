/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.tooling.msservices.serviceexplorer;

import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;

import java.util.concurrent.Semaphore;

public class EventHelper {
    public interface EventStateHandle {
        boolean isEventTriggered();
    }

    public interface EventWaitHandle {
        void waitEvent(@NotNull Runnable callback) throws AzureCmdException;
    }

    public interface EventHandler {
        EventWaitHandle registerEvent()
                throws AzureCmdException;

        void unregisterEvent(@NotNull EventWaitHandle waitHandle)
                throws AzureCmdException;

        void interruptibleAction(@NotNull EventStateHandle eventState)
                throws AzureCmdException;

        void eventTriggeredAction() throws AzureCmdException;
    }

    private static class EventSyncInfo implements EventStateHandle {
        private final Object eventSync = new Object();
        Semaphore semaphore = new Semaphore(0);

        EventWaitHandle eventWaitHandle;
        boolean registeredEvent = false;
        boolean eventTriggered = false;
        AzureCmdException exception;

        public boolean isEventTriggered() {
            synchronized (eventSync) {
                return eventTriggered;
            }
        }
    }

    public static void runInterruptible(@NotNull final EventHandler eventHandler)
            throws AzureCmdException {
        final EventSyncInfo eventSyncInfo = new EventSyncInfo();

        eventSyncInfo.eventWaitHandle = eventHandler.registerEvent();
        eventSyncInfo.registeredEvent = true;

        DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventSyncInfo.eventWaitHandle.waitEvent(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (eventSyncInfo.eventSync) {
                                if (eventSyncInfo.registeredEvent) {
                                    eventSyncInfo.registeredEvent = false;
                                    eventSyncInfo.eventTriggered = true;
                                    eventSyncInfo.semaphore.release();
                                }
                            }
                        }
                    });
                } catch (AzureCmdException ignored) {
                }
            }
        });

        DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventHandler.interruptibleAction(eventSyncInfo);

                    synchronized (eventSyncInfo.eventSync) {
                        if (eventSyncInfo.registeredEvent) {
                            eventSyncInfo.registeredEvent = false;
                            eventSyncInfo.semaphore.release();
                        }
                    }
                } catch (AzureCmdException ex) {
                    synchronized (eventSyncInfo.eventSync) {
                        if (eventSyncInfo.registeredEvent) {
                            eventSyncInfo.registeredEvent = false;
                            eventSyncInfo.exception = ex;
                            eventSyncInfo.semaphore.release();
                        }
                    }
                }
            }
        });

        try {
            eventSyncInfo.semaphore.acquire();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } finally {
            eventHandler.unregisterEvent(eventSyncInfo.eventWaitHandle);
        }

        synchronized (eventSyncInfo.eventSync) {
            if (!eventSyncInfo.eventTriggered) {
                if (eventSyncInfo.exception != null) {
                    throw eventSyncInfo.exception;
                }

                eventHandler.eventTriggeredAction();
            }
        }
    }
}