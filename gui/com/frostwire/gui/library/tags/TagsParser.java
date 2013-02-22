package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;

interface TagsParser {

    public TagsData parse();

    public BufferedImage getArtwork();
}
