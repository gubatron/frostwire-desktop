package com.frostwire.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * @author gubatron
 *
 */
public abstract class HostilesFileProcessor {
	public HostilesFileProcessor() {
	}

	public void loadDataFromFile(String file) throws Exception {
		File f = new File(file);
		if (!f.exists() || !f.isFile())
			throw new Exception("Could not load list, file not valid: " + file);

		// iterate through the file and start loading the list
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String strLine;

		while ((strLine = br.readLine()) != null) {
			// _oldList.add(strLine.trim());
			// System.out.print(".");
			doSomethingWithIP(strLine.trim());
		}
	}

	/** Generic callback invoked when a line on the file we're reading is met */
	public abstract void doSomethingWithIP(String ip);
}
