/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.message;

/**
 * This adapter sends log output to SLF4J. SLF4J supports multiple
 * implementations such as Logback, Log4j, Jakarta Commons Logging (JCL), JDK
 * 1.4 logging, x4juli, and Simple Log. To use SLF4J, you need to add the
 * required jar files to the classpath, and set the trace level to 4 when opening
 * a database:
 *
 * <pre>
 * jdbc:h2:&tilde;/test;TRACE_LEVEL_FILE=4
 * </pre>
 *
 * The logger name is 'h2database'.
 */
public class TraceWriterAdapter implements TraceWriter {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled(int level) {
	return false;
    }

    public void write(int level, String module, String s, Throwable t) {
    }

}
