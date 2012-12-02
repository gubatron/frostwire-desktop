#include "ToggleFullScreen.h"

#include <stdio.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>

Window getWindowParent(Display* display, Window window) {

    Window win;
    Window parent;
    Window *children;
    unsigned int numChildren;

    Status status = XQueryTree(display, window, &win, &parent, &children, &numChildren);
    if (status == 0 ) {
        fprintf(stderr, "ERROR: failed inside getWindowParent in call to XQueryTree.\n");
        return 0;
    }

    if (children != NULL) {
        XFree(children);
    }

    return parent;
}


bool toggleFullScreen(unsigned long winID) {
    
    Atom MOTIF_ATOM;
    Display *display;
    int screen;

    static bool isFullScreen = false;
    static Hints originalHints = {0,0,0,0,0};
    static Hints fullScreenHints = {2,0,0,0,0};
    static int orig_x, orig_y, orig_width, orig_height;
    static Window origParent;

    display = XOpenDisplay(NULL);
    screen = XDefaultScreen(display);

    MOTIF_ATOM = XInternAtom(display,"_MOTIF_WM_HINTS", true);
    
    if (!isFullScreen) {
        fprintf(stdout, "GOING FULLSCREEN\n");

        // store original dimensions
        XWindowAttributes curAttribs;
        XGetWindowAttributes(display, winID, &curAttribs);
        orig_x = curAttribs.x;
        orig_y = curAttribs.y;
        orig_width = curAttribs.width;
        orig_height = curAttribs.height;
       
        // Set window decorations
        int status;
        Atom actual_type;
        int  actual_format;
        unsigned long nitems;
        unsigned long bytes;
        unsigned long *data;

        status = XGetWindowProperty( display, winID, MOTIF_ATOM, 0, (~0L), False, AnyPropertyType,
                &actual_type, &actual_format, &nitems, &bytes, (unsigned char**)&data);

        if (status == Success && nitems == 5) {
           originalHints = {data[0], data[1], data[2], data[3], data[4]};
        }
        
        XChangeProperty(display,winID,MOTIF_ATOM,MOTIF_ATOM,32,PropModeReplace,(unsigned char*)&fullScreenHints,5);
        XSync(display,True);

        // set window dimensions 
        int screen = DefaultScreen(display);
        int width = 0.8 * DisplayWidth( display, screen );
        int height = 0.8 * DisplayHeight( display, screen );

        fprintf(stdout, "screen dimensions - width:%d height%d\n", width, height);

        XResizeWindow( display, winID, width, height );
    
    } else {
        
        fprintf(stdout, "GOING WINDOWED\n");

        // set window decorations
        XChangeProperty(display,winID,MOTIF_ATOM,MOTIF_ATOM,32,PropModeReplace,(unsigned char*)&originalHints,5);
        XFlush(display,True);

        // set window position
        XResizeWindow( display, winID, orig_width, orig_height );

    }

    XCloseDisplay(display);
    
    isFullScreen = !isFullScreen;
    return true; 
}

