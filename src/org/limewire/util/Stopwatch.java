/*
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

package org.limewire.util;

import java.util.concurrent.TimeUnit;

import com.frostwire.logging.Logger;

public class Stopwatch {

    private final Logger log;
    private long start = System.nanoTime();

    public Stopwatch(final Logger log) {
        this.log = log;
    }

    /**
     * Resets and returns elapsed time in milliseconds.
     */
    public long reset() {
        long now = System.nanoTime();
        long elapsed = now - start;
        start = now;
        return TimeUnit.NANOSECONDS.toMillis(elapsed);
    }

    /**
     * Resets and logs elapsed time in milliseconds.
     */
    public void resetAndLog(String label) {
        log.info(label + ": " + reset() + "ms");
    }
}