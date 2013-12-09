package com.frostwire.torrent;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Date;

import org.gudy.azureus2.core3.util.SystemTime;

final class Debug {

    public static void printStackTrace(Throwable e) {
        e.printStackTrace();
    }

    public static String getNestedExceptionMessage(Throwable e) {
        String last_message = "";

        while (e != null) {

            String this_message;

            if (e instanceof UnknownHostException) {

                this_message = "Unknown host " + e.getMessage();

            } else if (e instanceof FileNotFoundException) {

                this_message = "File not found: " + e.getMessage();

            } else {

                this_message = e.getMessage();
            }

            // if no exception message then pick up class name. if we have a deliberate
            // zero length string then we assume that the exception can be ignored for
            // logging purposes as it is just delegating

            if (this_message == null) {

                this_message = e.getClass().getName();

                int pos = this_message.lastIndexOf(".");

                this_message = this_message.substring(pos + 1).trim();
            }

            if (this_message.length() > 0 && last_message.indexOf(this_message) == -1) {

                last_message += (last_message.length() == 0 ? "" : ", ") + this_message;
            }

            e = e.getCause();
        }

        return (last_message);
    }

    public static void out(final Throwable _exception) {
        out("", _exception);
    }

    /**
     * Prints out the given debug message to System.out,
     * prefixed by the calling class name, method and
     * line number, appending the stacktrace of the given exception.
     */
    public static void out(final String _debug_msg, final Throwable _exception) {
        if ((_exception instanceof ConnectException) && _exception.getMessage().startsWith("No route to host")) {
            return;
        }
        if ((_exception instanceof UnknownHostException)) {
            return;
        }
        String header = "DEBUG::";
        header = header + new Date(SystemTime.getCurrentTime()).toString() + "::";
        String className;
        String methodName;
        int lineNumber;
        String trace_trace_tail = null;

        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] st = e.getStackTrace();

            StackTraceElement first_line = st[2];
            className = first_line.getClassName() + "::";
            methodName = first_line.getMethodName() + "::";
            lineNumber = first_line.getLineNumber();

            trace_trace_tail = getCompressedStackTrace(e, 3, 200, false);
        }

        System.err.println(header + className + (methodName) + lineNumber + ":");
        if (_debug_msg.length() > 0) {
            System.err.println("  " + _debug_msg);
        }
        if (trace_trace_tail != null) {
            System.err.println("    " + trace_trace_tail);
        }
        if (_exception != null) {
            _exception.printStackTrace();
        }
    }

    public static void out(String _debug_message) {
        out(_debug_message, null);
    }

    public static String getNestedExceptionMessageAndStack(Throwable e) {
        return (getNestedExceptionMessage(e) + ", " + getCompressedStackTrace(e, 0));
    }

    private static String getCompressedStackTrace(Throwable t, int frames_to_skip) {
        return getCompressedStackTrace(t, frames_to_skip, 200);
    }

    public static String getCompressedStackTrace(Throwable t, int frames_to_skip, int iMaxLines) {
        return getCompressedStackTrace(t, frames_to_skip, iMaxLines, true);
    }

    public static String getCompressedStackTrace(Throwable t, int frames_to_skip, int iMaxLines, boolean showErrString) {
        StringBuffer sbStackTrace = new StringBuffer(showErrString ? (t.toString() + "; ") : "");
        StackTraceElement[] st = t.getStackTrace();

        if (iMaxLines < 0) {
            iMaxLines = st.length + iMaxLines;
            if (iMaxLines < 0) {
                iMaxLines = 1;
            }
        }
        int iMax = Math.min(st.length, iMaxLines + frames_to_skip);
        for (int i = frames_to_skip; i < iMax; i++) {

            if (i > frames_to_skip) {
                sbStackTrace.append(", ");
            }

            String classname = st[i].getClassName();
            String cnShort = classname.substring(classname.lastIndexOf(".") + 1);

            sbStackTrace.append(cnShort);
            sbStackTrace.append("::");
            sbStackTrace.append(st[i].getMethodName());
            sbStackTrace.append("::");
            sbStackTrace.append(st[i].getLineNumber());
        }

        Throwable cause = t.getCause();

        if (cause != null) {
            sbStackTrace.append("\n\tCaused By: ");
            sbStackTrace.append(getCompressedStackTrace(cause, 0));
        }

        return sbStackTrace.toString();
    }
}
