package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.settings.SearchSettings;

public class MediaTypeFilter implements TableLineFilter<SearchResultDataLine> {


    public MediaTypeFilter() {
    }

    @Override
    public boolean allow(SearchResultDataLine node) {
        try {
            // hard coding disable youtube extension
            if (node.getExtension().equals("youtube")) {
                return false;
            }
        } catch (Throwable e) {
            // ignore
        }

        NamedMediaType nmt = node.getNamedMediaType();
        if (nmt != null) {
            return SearchSettings.LAST_MEDIA_TYPE_USED.getValue().equals(nmt.getMediaType().getMimeType());
        }

        return false;
    }   
}
