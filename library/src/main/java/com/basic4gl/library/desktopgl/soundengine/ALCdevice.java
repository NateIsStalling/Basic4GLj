package com.basic4gl.library.desktopgl.soundengine;


import java.util.HashMap;
import java.util.Iterator;

public final class ALCdevice {
    final long device;
    private boolean valid;
    private final HashMap<Long, ALCcontext> contexts = new HashMap();

    ALCdevice(long device) {
        this.device = device;
        this.valid = true;
    }

    public boolean equals(Object device) {
        if (device instanceof ALCdevice) {
            return ((ALCdevice)device).device == this.device;
        } else {
            return super.equals(device);
        }
    }

    void addContext(ALCcontext context) {
        synchronized(this.contexts) {
            this.contexts.put(context.context, context);
        }
    }

    void removeContext(ALCcontext context) {
        synchronized(this.contexts) {
            this.contexts.remove(context.context);
        }
    }

    void setInvalid() {
        this.valid = false;
        synchronized(this.contexts) {
            Iterator i$ = this.contexts.values().iterator();

            while(true) {
                if (!i$.hasNext()) {
                    break;
                }

                ALCcontext context = (ALCcontext)i$.next();
                context.setInvalid();
            }
        }

        this.contexts.clear();
    }

    public boolean isValid() {
        return this.valid;
    }
}
