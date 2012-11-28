#include "ToggleFullScreen.h"

#include <stdio.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>

bool toggleFullScreen(unsigned long winID) {
    
    Atom MOTIF_ATOM;
    Display *display;
   
    static bool isFullScreen = false;
    static Hints originalHints = {0,0,0,0,0};
    static Hints fullScreenHints = {2,0,0,0,0};
    static int orig_x, orig_y, orig_width, orig_height;

    display = XOpenDisplay(NULL);
    MOTIF_ATOM = XInternAtom(display,"_MOTIF_WM_HINTS", true);
    
    // ------------------------
    // set window decorations
    // ------------------------
    if (!isFullScreen) {


    } else {
    }
    
    if (!isFullScreen) {
        
        // Set window decorations
        int status;
        Atom actual_type;
        int  actual_format;
        unsigned long nitems;
        unsigned long bytes;
        unsigned long *data;

        status = XGetWindowProperty( display, winID, MOTIF_ATOM, 0, (~0L), False, AnyPropertyType,
                &actual_type, &actual_format, &nitems, &bytes, (unsigned char**)&data);

        if (status != Success) {
           fprintf(stderr, "failed in call to XGetWindowProperty() status = %d\n", status);
        } else if (nitems == 5) {
           originalHints = {data[0], data[1], data[2], data[3], data[4]};
        }
        
        XChangeProperty(display,winID,MOTIF_ATOM,MOTIF_ATOM,32,PropModeReplace,(unsigned char*)&fullScreenHints,5);

        // set window position
        XWindowAttributes curAttribs;
        XGetWindowAttributes(display, winID, &curAttribs);
        orig_x = curAttribs.x;
        orig_y = curAttribs.y;
        orig_width = curAttribs.width;
        orig_height = curAttribs.height;

        fprintf(stdout, "going fullscreen\n");
        fprintf(stdout, "orig_x: %d\n", orig_x);
        fprintf(stdout, "orig_y: %d\n", orig_y);
        fprintf(stdout, "orig_width: %d\n", orig_width);
        fprintf(stdout, "orig_height: %d\n", orig_height);

        int screen = DefaultScreen(display);
        XMoveResizeWindow( display, winID, 0, 0, DisplayWidth( display, screen), DisplayHeight( display, screen ) );

    } else {
        
        // set window position
        fprintf(stdout, "now restoring to windowed\n");
        fprintf(stdout, "orig_x: %d\n", orig_x);
        fprintf(stdout, "orig_y: %d\n", orig_y);
        fprintf(stdout, "orig_width: %d\n", orig_width);
        fprintf(stdout, "orig_height: %d\n", orig_height);
        XMoveResizeWindow( display, winID, orig_x, orig_y, orig_width, orig_height );
        
        // set window decorations back to original
        XChangeProperty(display,winID,MOTIF_ATOM,MOTIF_ATOM,32,PropModeReplace,(unsigned char*)&originalHints,5);
    }

    XCloseDisplay(display);
    
    isFullScreen = !isFullScreen;
    return true; 
}

