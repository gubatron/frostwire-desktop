package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.settings.SearchSettings;

public class MediaTypeFilter implements TableLineFilter<SearchResultDataLine> {
    
    private final boolean allowAll;
    
    public MediaTypeFilter() {
        this.allowAll = isAllSelected();
    }

    @Override
    public boolean allow(SearchResultDataLine node) {
        if (allowAll) {
            return true;
        }
        
        NamedMediaType nmt = node.getNamedMediaType();
        if (nmt != null) {
            return SearchSettings.LAST_MEDIA_TYPES_USED.contains(nmt.getMediaType().getMimeType());
        }
        
        return false;
    }
    
    private boolean isAllSelected() {
        boolean selected = true;
        
        selected &= SearchSettings.LAST_MEDIA_TYPES_USED.contains(MediaType.getAudioMediaType().getMimeType());
        selected &= SearchSettings.LAST_MEDIA_TYPES_USED.contains(MediaType.getVideoMediaType().getMimeType());
        selected &= SearchSettings.LAST_MEDIA_TYPES_USED.contains(MediaType.getImageMediaType().getMimeType());
        selected &= SearchSettings.LAST_MEDIA_TYPES_USED.contains(MediaType.getDocumentMediaType().getMimeType());
        selected &= SearchSettings.LAST_MEDIA_TYPES_USED.contains(MediaType.getProgramMediaType().getMimeType());
        selected &= SearchSettings.LAST_MEDIA_TYPES_USED.contains(MediaType.getTorrentMediaType().getMimeType());
        
        return selected;
    }
}
