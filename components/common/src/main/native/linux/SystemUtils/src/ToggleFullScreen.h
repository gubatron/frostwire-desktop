#include <X11/Xlib.h>

struct Hints
{
    unsigned long flags;
    unsigned long functions;
    unsigned long decorations;
    long input_mode;
    unsigned long status;
};

bool toggleFullScreen(unsigned long winID);
