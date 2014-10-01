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

import com.frostwire.bittorrent.BTConfigurator;
import com.limegroup.gnutella.gui.*;
import org.limewire.i18n.I18nMarker;

import java.io.IOException;

public final class TorrentConnectionPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("BitTorrent Connection Settings");

    public final static String TEXT = I18n.tr("Adjust connection settings to make better use of your internet connection");

    public final static String MAX_ACTIVE_DOWNLOADS = I18n.tr("Maximum active downloads");

    public final static String MAX_GLOBAL_NUM_CONNECTIONS = I18n.tr("Global maximum number of connections");

    public final static String MAX_PEERS_PER_TORRENT = I18n.tr("Maximum number of connected peers per torrent");

    public final static String MAX_UPLOAD_SLOTS = I18n.tr("Number of upload slots per torrent");

    private WholeNumberField MAX_ACTIVE_DOWNLOADS_FIELD = new SizedWholeNumberField(4);

    private WholeNumberField MAX_GLOBAL_NUM_CONNECTIONS_FIELD = new SizedWholeNumberField(4);

    private WholeNumberField MAX_PEERS_PER_TORRENT_FIELD = new SizedWholeNumberField(4);

    private WholeNumberField MAX_UPLOAD_SLOTS_FIELD = new SizedWholeNumberField(4);

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
                I18nMarker.marktr(MAX_GLOBAL_NUM_CONNECTIONS),
                MAX_GLOBAL_NUM_CONNECTIONS_FIELD, LabeledComponent.LEFT_GLUE,
                LabeledComponent.LEFT);
        panel.add(comp.getComponent());
        panel.addVerticalComponentGap();

        comp = new LabeledComponent(
                I18nMarker.marktr(MAX_PEERS_PER_TORRENT),
                MAX_PEERS_PER_TORRENT_FIELD, LabeledComponent.LEFT_GLUE,
                LabeledComponent.LEFT);
        panel.add(comp.getComponent());
        panel.addVerticalComponentGap();

        comp = new LabeledComponent(
                I18nMarker.marktr(MAX_UPLOAD_SLOTS),
                MAX_UPLOAD_SLOTS_FIELD, LabeledComponent.LEFT_GLUE,
                LabeledComponent.LEFT);
        panel.add(comp.getComponent());
        panel.addVerticalComponentGap();

        add(panel);

    }

    @Override
    public boolean isDirty() {
        // TODO:BITTORRENT
        return true;
        //return (BittorrentSettings.TORRENT_MAX_ACTIVE_DOWNLOADS.getValue() != MAX_ACTIVE_DOWNLOADS_FIELD.getValue()) ||
        //	(COConfigurationManager.getIntParameter("Max.Peer.Connections.Total") != MAX_GLOBAL_NUM_CONNECTIONS_FIELD.getValue()) ||
        //	(COConfigurationManager.getIntParameter("Max.Peer.Connections.Per.Torrent") != MAX_PEERS_PER_TORRENT_FIELD.getValue()) ||
        //	(COConfigurationManager.getIntParameter("Max Uploads") != MAX_UPLOAD_SLOTS_FIELD.getValue());
    }

    @Override
    public void initOptions() {
        // TODO:BITTORRENT
        //MAX_GLOBAL_NUM_CONNECTIONS_FIELD.setValue(COConfigurationManager.getIntParameter("Max.Peer.Connections.Total"));
        //MAX_PEERS_PER_TORRENT_FIELD.setValue(COConfigurationManager.getIntParameter("Max.Peer.Connections.Per.Torrent"));
        MAX_ACTIVE_DOWNLOADS_FIELD.setValue(BTConfigurator.getMaxDownloads());
        MAX_UPLOAD_SLOTS_FIELD.setValue(BTConfigurator.getMaxUploads());
    }

    @Override
    public boolean applyOptions() throws IOException {
        // TODO:BITTORRENT
        //COConfigurationManager.setParameter("Max.Peer.Connections.Total",MAX_GLOBAL_NUM_CONNECTIONS_FIELD.getValue());
        //COConfigurationManager.setParameter("Max.Peer.Connections.Per.Torrent",MAX_PEERS_PER_TORRENT_FIELD.getValue());
        BTConfigurator.setMaxDownloads(MAX_ACTIVE_DOWNLOADS_FIELD.getValue());
        BTConfigurator.setMaxUploads(MAX_UPLOAD_SLOTS_FIELD.getValue());

        return false;
    }
}
