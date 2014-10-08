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

package com.limegroup.gnutella.gui.options.panes;

import com.frostwire.bittorrent.BTEngine2;
import com.limegroup.gnutella.gui.*;
import org.limewire.i18n.I18nMarker;

import java.io.IOException;

public final class TorrentConnectionPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("BitTorrent Connection Settings");

    public final static String TEXT = I18n.tr("Adjust connection settings to make better use of your internet connection");

    public final static String MAX_ACTIVE_DOWNLOADS = I18n.tr("Maximum active downloads");

    public final static String MAX_GLOBAL_NUM_CONNECTIONS = I18n.tr("Global maximum number of connections");

    public final static String MAX_PEERS = I18n.tr("Maximum number of peers");

    public final static String MAX_ACTIVE_SEEDS = I18n.tr("Maximum active seeds");

    private WholeNumberField MAX_ACTIVE_DOWNLOADS_FIELD = new SizedWholeNumberField(4);

    private WholeNumberField MAX_GLOBAL_NUM_CONNECTIONS_FIELD = new SizedWholeNumberField(4);

    private WholeNumberField MAX_PEERS_FIELD = new SizedWholeNumberField(4);

    private WholeNumberField MAX_ACTIVE_SEEDS_FIELD = new SizedWholeNumberField(4);

    public TorrentConnectionPaneItem() {
        super(TITLE, TEXT);

        BoxPanel panel = new BoxPanel();

        LabeledComponent comp = new LabeledComponent(
                I18nMarker.marktr(MAX_ACTIVE_DOWNLOADS),
                MAX_ACTIVE_DOWNLOADS_FIELD, LabeledComponent.LEFT_GLUE,
                LabeledComponent.LEFT);
        panel.add(comp.getComponent());
        panel.addVerticalComponentGap();

        comp = new LabeledComponent(
                I18nMarker.marktr(MAX_ACTIVE_SEEDS),
                MAX_ACTIVE_SEEDS_FIELD, LabeledComponent.LEFT_GLUE,
                LabeledComponent.LEFT);
        panel.add(comp.getComponent());
        panel.addVerticalComponentGap();

        comp = new LabeledComponent(
                I18nMarker.marktr(MAX_GLOBAL_NUM_CONNECTIONS),
                MAX_GLOBAL_NUM_CONNECTIONS_FIELD, LabeledComponent.LEFT_GLUE,
                LabeledComponent.LEFT);
        panel.add(comp.getComponent());
        panel.addVerticalComponentGap();

        comp = new LabeledComponent(
                I18nMarker.marktr(MAX_PEERS),
                MAX_PEERS_FIELD, LabeledComponent.LEFT_GLUE,
                LabeledComponent.LEFT);
        panel.add(comp.getComponent());
        panel.addVerticalComponentGap();

        add(panel);

    }

    @Override
    public boolean isDirty() {
        return (BTEngine2.getInstance().getMaxActiveDownloads() != MAX_ACTIVE_DOWNLOADS_FIELD.getValue()) ||
                (BTEngine2.getInstance().getMaxConnections() != MAX_GLOBAL_NUM_CONNECTIONS_FIELD.getValue()) ||
                (BTEngine2.getInstance().getMaxPeers() != MAX_PEERS_FIELD.getValue()) ||
                (BTEngine2.getInstance().getMaxActiveSeeds() != MAX_ACTIVE_SEEDS_FIELD.getValue());
    }

    @Override
    public void initOptions() {
        MAX_GLOBAL_NUM_CONNECTIONS_FIELD.setValue(BTEngine2.getInstance().getMaxConnections());
        MAX_PEERS_FIELD.setValue(BTEngine2.getInstance().getMaxPeers());
        MAX_ACTIVE_DOWNLOADS_FIELD.setValue(BTEngine2.getInstance().getMaxActiveDownloads());
        MAX_ACTIVE_SEEDS_FIELD.setValue(BTEngine2.getInstance().getMaxActiveSeeds());
    }

    @Override
    public boolean applyOptions() throws IOException {
        BTEngine2.getInstance().setMaxConnections(MAX_GLOBAL_NUM_CONNECTIONS_FIELD.getValue());
        BTEngine2.getInstance().setMaxPeers(MAX_PEERS_FIELD.getValue());
        BTEngine2.getInstance().setMaxActiveDownloads(MAX_ACTIVE_DOWNLOADS_FIELD.getValue());
        BTEngine2.getInstance().setMaxActiveSeeds(MAX_ACTIVE_SEEDS_FIELD.getValue());

        return false;
    }
}
