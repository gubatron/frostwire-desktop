/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frostwire.mp4;

//import com.googlecode.mp4parser.AbstractBox;
import com.frostwire.mp4.boxes.Box;

//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
//import java.net.URL;
//import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Property file based BoxFactory
 */
public class PropertyBoxParserImpl extends AbstractBoxParser {
    Properties mapping;

    public PropertyBoxParserImpl(String... customProperties) {
        /*
        InputStream is = new BufferedInputStream(getClass().getResourceAsStream("/isoparser-default.properties"));
        try {
            mapping = new Properties();
            try {
                mapping.load(is);
                Enumeration<URL> enumeration = Thread.currentThread().getContextClassLoader().getResources("isoparser-custom.properties");

                while (enumeration.hasMoreElements()) {
                    URL url = enumeration.nextElement();
                    InputStream customIS = new BufferedInputStream(url.openStream());
                    try {
                        mapping.load(customIS);
                    } finally {
                        customIS.close();
                    }
                }
                for (String customProperty : customProperties) {
                    mapping.load(new BufferedInputStream(getClass().getResourceAsStream(customProperty)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                // ignore - I can't help
            }
        }
        */
        mapping = new Properties();
        
//        hint=com.coremedia.iso.boxes.TrackReferenceTypeBox(type)
//        cdsc=com.coremedia.iso.boxes.TrackReferenceTypeBox(type)
//        meta-ilst=com.coremedia.iso.boxes.apple.AppleItemListBox()
//        -----name=com.coremedia.iso.boxes.apple.AppleNameBox()
//        -----mean=com.coremedia.iso.boxes.apple.AppleMeanBox()
//        -----data=com.coremedia.iso.boxes.apple.AppleDataBox()
//        rmra=com.coremedia.iso.boxes.apple.AppleReferenceMovieBox()
//        rmda=com.coremedia.iso.boxes.apple.AppleReferenceMovieDescriptorBox()
//        rmdr=com.coremedia.iso.boxes.apple.AppleDataRateBox()
//        rdrf=com.coremedia.iso.boxes.apple.AppleDataReferenceBox()
//        ilst-cprt=com.coremedia.iso.boxes.apple.AppleCopyrightBox()
//        ilst-\u00A9cmt=com.coremedia.iso.boxes.apple.AppleCommentBox()
//        ilst-desc=com.coremedia.iso.boxes.apple.AppleDescriptionBox()
//        ilst-covr=com.coremedia.iso.boxes.apple.AppleCoverBox()
//        ilst-\u00A9alb=com.coremedia.iso.boxes.apple.AppleAlbumBox()
//        ilst-\u00A9gen=com.coremedia.iso.boxes.apple.AppleCustomGenreBox()
//        ilst-\u00A9grp=com.coremedia.iso.boxes.apple.AppleGroupingBox()
//        ilst-\u00A9wrt=com.coremedia.iso.boxes.apple.AppleTrackAuthorBox()
//        ilst-aART=com.coremedia.iso.boxes.apple.AppleAlbumArtistBox()
//        ilst-tvsh=com.coremedia.iso.boxes.apple.AppleShowBox()
//        ilst-stik=com.coremedia.iso.boxes.apple.AppleMediaTypeBox()
//        ilst-pgap=com.coremedia.iso.boxes.apple.AppleGaplessPlaybackBox()
//        ilst-tmpo=com.coremedia.iso.boxes.apple.AppleTempBox()
//        ilst-\u00A9nam=com.coremedia.iso.boxes.apple.AppleTrackTitleBox()
//        ilst-ldes=com.coremedia.iso.boxes.apple.AppleSynopsisBox()
//        ilst-\u00A9ART=com.coremedia.iso.boxes.apple.AppleArtistBox()
//        ilst-name=com.coremedia.iso.boxes.apple.AppleNameBox()
//        ilst-cpil=com.coremedia.iso.boxes.apple.AppleCompilationBox()
//        ilst-purd=com.coremedia.iso.boxes.apple.ApplePurchaseDateBox()
//        ilst-\u00A9too=com.coremedia.iso.boxes.apple.AppleEncoderBox()
//        ilst-sfID=com.coremedia.iso.boxes.apple.AppleStoreCountryCodeBox()
//        ilst-gnre=com.coremedia.iso.boxes.apple.AppleStandardGenreBox()
//        ilst-tves=com.coremedia.iso.boxes.apple.AppleTvEpisodeBox()
//        ilst-ilst=com.coremedia.iso.boxes.apple.AppleItemListBox()
//        ilst-data=com.coremedia.iso.boxes.apple.AppleDataBox()
//        ilst-tvsn=com.coremedia.iso.boxes.apple.AppleTvSeasonBox()
//        ilst-soal=com.coremedia.iso.boxes.apple.AppleSortAlbumBox()
//        ilst-tven=com.coremedia.iso.boxes.apple.AppleTvEpisodeNumberBox()
//        ilst-trkn=com.coremedia.iso.boxes.apple.AppleTrackNumberBox()
//        ilst-\u00A9day=com.coremedia.iso.boxes.apple.AppleRecordingYearBox()
//        ilst-----=com.coremedia.iso.boxes.apple.AppleGenericBox()
//        ilst-akID=com.coremedia.iso.boxes.apple.AppleStoreAccountTypeBox()
//        ilst-rtng=com.coremedia.iso.boxes.apple.AppleRatingBox()
//        ilst-tvnn=com.coremedia.iso.boxes.apple.AppleNetworkBox()
//        ilst-apID=com.coremedia.iso.boxes.apple.AppleIdBox()
//        wave=com.coremedia.iso.boxes.apple.AppleWaveBox()
//
//        udta-ccid=com.coremedia.iso.boxes.odf.OmaDrmContentIdBox()
//        udta-yrrc=com.coremedia.iso.boxes.RecordingYearBox()
//        udta-titl=com.coremedia.iso.boxes.TitleBox()
//        udta-dscp=com.coremedia.iso.boxes.DescriptionBox()
//        udta-icnu=com.coremedia.iso.boxes.odf.OmaDrmIconUriBox()
//        udta-infu=com.coremedia.iso.boxes.odf.OmaDrmInfoUrlBox()
//        udta-albm=com.coremedia.iso.boxes.AlbumBox()
//        udta-cprt=com.coremedia.iso.boxes.CopyrightBox()
//        udta-gnre=com.coremedia.iso.boxes.GenreBox()
//        udta-perf=com.coremedia.iso.boxes.PerformerBox()
//        udta-auth=com.coremedia.iso.boxes.AuthorBox()
//        udta-kywd=com.coremedia.iso.boxes.KeywordsBox()
//        udta-loci=com.coremedia.iso.boxes.threegpp26244.LocationInformationBox()
//        udta-rtng=com.coremedia.iso.boxes.RatingBox()
//        udta-clsf=com.coremedia.iso.boxes.ClassificationBox()
//        udta-cdis=com.coremedia.iso.boxes.vodafone.ContentDistributorIdBox()
//        udta-albr=com.coremedia.iso.boxes.vodafone.AlbumArtistBox()
//        udta-cvru=com.coremedia.iso.boxes.odf.OmaDrmCoverUriBox()
//        udta-lrcu=com.coremedia.iso.boxes.odf.OmaDrmLyricsUriBox()
//
//
//
//
//        stsd-tx3g=com.coremedia.iso.boxes.sampleentry.TextSampleEntry(type)
//        stsd-enct=com.coremedia.iso.boxes.sampleentry.TextSampleEntry(type)
        put(mapping, "stsd-samr", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-sawb", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-mp4a", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-drms", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-alac", "sampleentry.AudioSampleEntry(type)");
//        stsd-mp4s=com.coremedia.iso.boxes.sampleentry.MpegSampleEntry(type)
        put(mapping, "stsd-owma", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-ac-3", "sampleentry.AudioSampleEntry(type)");
//        dac3=com.googlecode.mp4parser.boxes.AC3SpecificBox()
        put(mapping, "stsd-ec-3", "sampleentry.AudioSampleEntry(type)");
//        dec3=com.googlecode.mp4parser.boxes.EC3SpecificBox()
        put(mapping, "stsd-lpcm", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-dtsc", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-dtsh", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-dtsl", "sampleentry.AudioSampleEntry(type)");
//        ddts=com.googlecode.mp4parser.boxes.DTSSpecificBox()
        put(mapping, "stsd-dtse", "sampleentry.AudioSampleEntry(type)");
        put(mapping, "stsd-mlpa", "sampleentry.AudioSampleEntry(type)");
//        dmlp=com.googlecode.mp4parser.boxes.MLPSpecificBox()
        put(mapping, "stsd-enca", "sampleentry.AudioSampleEntry(type)");
//        stsd-encv=com.coremedia.iso.boxes.sampleentry.VisualSampleEntry(type)
//        stsd-mp4v=com.coremedia.iso.boxes.sampleentry.VisualSampleEntry(type)
//        stsd-s263=com.coremedia.iso.boxes.sampleentry.VisualSampleEntry(type)
//        stsd-avc1=com.coremedia.iso.boxes.sampleentry.VisualSampleEntry(type)
//        stsd-ovc1=com.coremedia.iso.boxes.sampleentry.Ovc1VisualSampleEntryImpl()
//        stsd-stpp=com.coremedia.iso.boxes.sampleentry.SubtitleSampleEntry(type)
//        avcC=com.coremedia.iso.boxes.h264.AvcConfigurationBox()
//        alac=com.coremedia.iso.boxes.apple.AppleLosslessSpecificBox()
//        btrt=com.coremedia.iso.boxes.BitRateBox()
        put(mapping, "ftyp", "FileTypeBox()");
        put(mapping, "mdat", "mdat.MediaDataBox()");
        put(mapping, "moov", "MovieBox()");
        put(mapping, "mvhd", "MovieHeaderBox()");
        put(mapping, "trak", "TrackBox()");
        put(mapping, "tkhd", "TrackHeaderBox()");
        put(mapping, "edts", "EditBox()");
        put(mapping, "elst", "EditListBox()");
        put(mapping, "mdia", "MediaBox()");
        put(mapping, "mdhd", "MediaHeaderBox()");
        put(mapping, "hdlr", "HandlerBox()");
        put(mapping, "minf", "MediaInformationBox()");
        put(mapping, "vmhd", "VideoMediaHeaderBox()");
        put(mapping, "smhd", "SoundMediaHeaderBox()");
        put(mapping, "sthd", "SubtitleMediaHeaderBox()");
        put(mapping, "hmhd", "HintMediaHeaderBox()");
        put(mapping, "dinf", "DataInformationBox()");
        put(mapping, "dref", "DataReferenceBox()");
        put(mapping, "url ", "DataEntryUrlBox()");
//        urn\ =com.coremedia.iso.boxes.DataEntryUrnBox()
        put(mapping, "stbl", "SampleTableBox()");
        put(mapping, "ctts", "CompositionTimeToSample()");
        put(mapping, "stsd", "SampleDescriptionBox()");
        put(mapping, "stts", "TimeToSampleBox()");
//        stss=com.coremedia.iso.boxes.SyncSampleBox()
        put(mapping, "stsc", "SampleToChunkBox()");
        put(mapping, "stsz", "SampleSizeBox()");
        put(mapping, "stco", "StaticChunkOffsetBox()");
//        subs=com.coremedia.iso.boxes.SubSampleInformationBox()
//        sbgp=com.coremedia.iso.boxes.SampleToGroupBox()
//        udta=com.coremedia.iso.boxes.UserDataBox()
//        skip=com.coremedia.iso.boxes.FreeSpaceBox()
//        tref=com.coremedia.iso.boxes.TrackReferenceBox()
//        iloc=com.coremedia.iso.boxes.ItemLocationBox()
//        idat=com.coremedia.iso.boxes.ItemDataBox()
//        saio=com.coremedia.iso.boxes.SampleAuxiliaryInformationOffsetsBox()
//        saiz=com.coremedia.iso.boxes.SampleAuxiliaryInformationSizesBox()
//        damr=com.coremedia.iso.boxes.sampleentry.AmrSpecificBox()
//        meta=com.coremedia.iso.boxes.MetaBox()
//        ipro=com.coremedia.iso.boxes.ItemProtectionBox()
//        sinf=com.coremedia.iso.boxes.ProtectionSchemeInformationBox()
//        frma=com.coremedia.iso.boxes.OriginalFormatBox()
//        schi=com.coremedia.iso.boxes.SchemeInformationBox()
//        odkm=com.coremedia.iso.boxes.odf.OmaDrmKeyManagenentSystemBox()
//        odaf=com.coremedia.iso.boxes.OmaDrmAccessUnitFormatBox()
//        schm=com.coremedia.iso.boxes.SchemeTypeBox()
        put(mapping, "uuid", "UserBox(userType)");
//        free=com.coremedia.iso.boxes.FreeBox()
        put(mapping, "mvex", "fragment.MovieExtendsBox()");
//        mehd=com.coremedia.iso.boxes.fragment.MovieExtendsHeaderBox()
        put(mapping, "trex", "fragment.TrackExtendsBox()");
//
        put(mapping, "moof", "fragment.MovieFragmentBox()");
//        mfhd=com.coremedia.iso.boxes.fragment.MovieFragmentHeaderBox()
        put(mapping, "traf", "fragment.TrackFragmentBox()");
        put(mapping, "tfhd", "fragment.TrackFragmentHeaderBox()");
        put(mapping, "trun", "fragment.TrackRunBox()");
        put(mapping, "sdtp", "SampleDependencyTypeBox()");
//        mfra=com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessBox()
//        tfra=com.coremedia.iso.boxes.fragment.TrackFragmentRandomAccessBox()
//        mfro=com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessOffsetBox()
//        tfdt=com.coremedia.iso.boxes.fragment.TrackFragmentBaseMediaDecodeTimeBox()
//        nmhd=com.coremedia.iso.boxes.NullMediaHeaderBox()
//        gmhd=com.coremedia.iso.boxes.GenericMediaHeaderBoxImpl()
//        cslg=com.coremedia.iso.boxes.CompositionShiftLeastGreatestAtom()
//        pdin=com.coremedia.iso.boxes.ProgressiveDownloadInformationBox()
//        bloc=com.googlecode.mp4parser.boxes.ultraviolet.BaseLocationBox()
//        ftab=com.googlecode.mp4parser.boxes.threegpp26245.FontTableBox()
//        co64=com.coremedia.iso.boxes.ChunkOffset64BitBox()
//        xml\ =com.coremedia.iso.boxes.XmlBox()
//        avcn=com.googlecode.mp4parser.boxes.basemediaformat.AvcNalUnitStorageBox()
//        ainf=com.googlecode.mp4parser.boxes.ultraviolet.AssetInformationBox()
//
//        trik=com.coremedia.iso.boxes.dece.TrickPlayBox()
//        uuid[A2394F525A9B4F14A2446C427C648DF4]=com.googlecode.mp4parser.boxes.piff.PiffSampleEncryptionBox()
//        uuid[8974DBCE7BE74C5184F97148F9882554]=com.googlecode.mp4parser.boxes.piff.PiffTrackEncryptionBox()
//        uuid[D4807EF2CA3946958E5426CB9E46A79F]=com.googlecode.mp4parser.boxes.piff.TfrfBox()
//        uuid[6D1D9B0542D544E680E2141DAFF757B2]=com.googlecode.mp4parser.boxes.piff.TfxdBox()
//        uuid[D08A4F1810F34A82B6C832D8ABA183D3]=com.googlecode.mp4parser.boxes.piff.UuidBasedProtectionSystemSpecificHeaderBox()
//        senc=com.googlecode.mp4parser.boxes.basemediaformat.SampleEncryptionBox()
//        tenc=com.googlecode.mp4parser.boxes.basemediaformat.TrackEncryptionBox()
//        amf0=com.googlecode.mp4parser.boxes.adobe.ActionMessageFormat0SampleEntryBox()
//
//        #iods=com.googlecode.mp4parser.boxes.mp4.ObjectDescriptorBox()
        put(mapping, "esds", "mp4.ESDescriptorBox()");
//
//        tmcd=com.googlecode.mp4parser.boxes.apple.TimeCodeBox()
//
        put(mapping, "default", "UnknownBox(type)");
//
//
//
//        #stsd-rtp\ =com.coremedia.iso.boxes.rtp.RtpHintSampleEntry(type)
//        #udta-hnti=com.coremedia.iso.boxes.rtp.HintInformationBox()
//        #udta-hinf=com.coremedia.iso.boxes.rtp.HintStatisticsBox()
//        #hnti-sdp\ =com.coremedia.iso.boxes.rtp.RtpTrackSdpHintInformationBox()
//        #hnti-rtp\ =com.coremedia.iso.boxes.rtp.RtpMovieHintInformationBox()
//        #hinf-pmax=com.coremedia.iso.boxes.rtp.LargestHintPacketBox()
//        #hinf-payt=com.coremedia.iso.boxes.rtp.PayloadTypeBox()
//        #hinf-tmin=com.coremedia.iso.boxes.rtp.SmallestRelativeTransmissionTimeBox()
//        #hinf-tmax=com.coremedia.iso.boxes.rtp.LargestRelativeTransmissionTimeBox()
//        #hinf-maxr=com.coremedia.iso.boxes.rtp.MaximumDataRateBox()
//        #hinf-dmax=com.coremedia.iso.boxes.rtp.LargestHintPacketDurationBox()
//        #hinf-hnti=com.coremedia.iso.boxes.rtp.HintInformationBox()
//        #hinf-tims=com.coremedia.iso.boxes.rtp.TimeScaleEntry()
//
//        #hinf-nump=com.coremedia.iso.boxes.rtp.HintPacketsSentBox(type)
//        #hinf-npck=com.coremedia.iso.boxes.rtp.HintPacketsSentBox(type)
//
//        #hinf-trpy=com.coremedia.iso.boxes.rtp.HintStatisticBoxes(type)
//        #hinf-totl=com.coremedia.iso.boxes.rtp.HintStatisticBoxes(type)
//        #hinf-tpyl=com.coremedia.iso.boxes.rtp.HintStatisticBoxes(type)
//        #hinf-tpay=com.coremedia.iso.boxes.rtp.HintStatisticBoxes(type)
//        #hinf-dmed=com.coremedia.iso.boxes.rtp.HintStatisticBoxes(type)
//        #hinf-dimm=com.coremedia.iso.boxes.rtp.HintStatisticBoxes(type)
//        #hinf-drep=com.coremedia.iso.boxes.rtp.HintStatisticBoxes(type)
//        #tims=com.coremedia.iso.boxes.rtp.TimeScaleEntry()
//
//        #odrm=com.coremedia.iso.boxes.odf.OmaDrmContainerBox()
//        #mdri=com.coremedia.iso.boxes.odf.MutableDrmInformationBox()
//        #odtt=com.coremedia.iso.boxes.odf.OmaDrmTransactionTrackingBox()
//        #odrb=com.coremedia.iso.boxes.odf.OmaDrmRightsObjectBox()
//        #odhe=com.coremedia.iso.boxes.odf.OmaDrmDiscreteHeadersBox()
//        #odda=com.coremedia.iso.boxes.odf.OmaDrmContentObjectBox()
//        #ohdr=com.coremedia.iso.boxes.odf.OmaDrmCommonHeadersBox()
//        #grpi=com.coremedia.iso.boxes.odf.OmaDrmGroupIdBox()
    }

    public PropertyBoxParserImpl(Properties mapping) {
        this.mapping = mapping;
    }
    
    private void put(Properties mapping, String key, String value) {
        mapping.put(key, "com.frostwire.mp4.boxes." + value);
    }

    Pattern p = Pattern.compile("(.*)\\((.*?)\\)");

    @SuppressWarnings("unchecked")
    public Class<? extends Box> getClassForFourCc(String type, byte[] userType, String parent) {
        FourCcToBox fourCcToBox = new FourCcToBox(type, userType, parent).invoke();
        try {
            return (Class<? extends Box>) Class.forName(fourCcToBox.clazzName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Box createBox(String type, byte[] userType, String parent) {

        FourCcToBox fourCcToBox = new FourCcToBox(type, userType, parent).invoke();
        String[] param = fourCcToBox.getParam();
        String clazzName = fourCcToBox.getClazzName();
        try {
            if (param[0].trim().length() == 0) {
                param = new String[]{};
            }
            Class clazz = Class.forName(clazzName);

            Class[] constructorArgsClazz = new Class[param.length];
            Object[] constructorArgs = new Object[param.length];
            for (int i = 0; i < param.length; i++) {

                if ("userType".equals(param[i])) {
                    constructorArgs[i] = userType;
                    constructorArgsClazz[i] = byte[].class;
                } else if ("type".equals(param[i])) {
                    constructorArgs[i] = type;
                    constructorArgsClazz[i] = String.class;
                } else if ("parent".equals(param[i])) {
                    constructorArgs[i] = parent;
                    constructorArgsClazz[i] = String.class;
                } else {
                    throw new InternalError("No such param: " + param[i]);
                }


            }
            Constructor<AbstractBox> constructorObject;
            try {
                if (param.length > 0) {
                    constructorObject = clazz.getConstructor(constructorArgsClazz);
                } else {
                    constructorObject = clazz.getConstructor();
                }

                return constructorObject.newInstance(constructorArgs);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private class FourCcToBox {
        private String type;
        private byte[] userType;
        private String parent;
        private String clazzName;
        private String[] param;

        public FourCcToBox(String type, byte[] userType, String parent) {
            this.type = type;
            this.parent = parent;
            this.userType = userType;
        }

        public String getClazzName() {
            return clazzName;
        }

        public String[] getParam() {
            return param;
        }

        public FourCcToBox invoke() {
            String constructor;
            if (userType != null) {
                if (!"uuid".equals((type))) {
                    throw new RuntimeException("we have a userType but no uuid box type. Something's wrong");
                }
                constructor = mapping.getProperty((parent) + "-uuid[" + Hex.encodeHex(userType).toUpperCase() + "]");
                if (constructor == null) {
                    constructor = mapping.getProperty("uuid[" + Hex.encodeHex(userType).toUpperCase() + "]");
                }
                if (constructor == null) {
                    constructor = mapping.getProperty("uuid");
                }
            } else {
                constructor = mapping.getProperty((parent) + "-" + (type));
                if (constructor == null) {
                    constructor = mapping.getProperty((type));
                }
            }
            if (constructor == null) {
                constructor = mapping.getProperty("default");
            }
            if (constructor == null) {
                throw new RuntimeException("No box object found for " + type);
            }
            Matcher m = p.matcher(constructor);
            boolean matches = m.matches();
            if (!matches) {
                throw new RuntimeException("Cannot work with that constructor: " + constructor);
            }
            clazzName = m.group(1);
            param = m.group(2).split(",");
            return this;
        }
    }
}
