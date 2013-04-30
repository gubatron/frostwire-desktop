/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
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

package com.frostwire.gui.theme;

import java.awt.Color;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
final class SkinColors {

    private SkinColors() {
    }

    public static final Color PROGRESS_BAR_ENABLED_COLOR1 = Color.GRAY;
    public static final Color PROGRESS_BAR_ENABLED_COLOR2 = Color.BLUE;

    public static final Color[] PROGRESS_BAR_ENABLED_GRADIENT_COLORS = new Color[] { PROGRESS_BAR_ENABLED_COLOR1, PROGRESS_BAR_ENABLED_COLOR2 };

    public static final Color PROGRESS_BAR_DISABLED_COLOR1 = Color.GRAY;
    public static final Color PROGRESS_BAR_DISABLED_COLOR2 = Color.BLUE;

    public static final Color[] PROGRESS_BAR_DISABLED_GRADIENT_COLORS = new Color[] { PROGRESS_BAR_DISABLED_COLOR1, PROGRESS_BAR_DISABLED_COLOR2 };

    public static final Color PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR1 = Color.GRAY;
    public static final Color PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR2 = Color.BLUE;

    public static final Color[] PROGRESS_BAR_ENABLED_INDERTERMINATE_GRADIENT_COLORS = new Color[] { PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR1, PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR2 };

    public static final Color PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR1 = Color.GRAY;
    public static final Color PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR2 = Color.BLUE;

    public static final Color[] PROGRESS_BAR_DISABLED_INDERTERMINATE_GRADIENT_COLORS = new Color[] { PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR1, PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR2 };
}
