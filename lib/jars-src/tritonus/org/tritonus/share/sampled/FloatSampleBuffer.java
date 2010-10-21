/*     */ package org.tritonus.share.sampled;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Random;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ 
/*     */ public class FloatSampleBuffer
/*     */ {
/*     */   private static final boolean LAZY_DEFAULT = true;
/* 188 */   private ArrayList channels = new ArrayList();
/* 189 */   private int sampleCount = 0;
/* 190 */   private int channelCount = 0;
/* 191 */   private float sampleRate = 0.0F;
/* 192 */   private int originalFormatType = 0;
/*     */   public static final int DITHER_MODE_AUTOMATIC = 0;
/*     */   public static final int DITHER_MODE_ON = 1;
/*     */   public static final int DITHER_MODE_OFF = 2;
/* 201 */   private static Random random = null;
/* 202 */   private float ditherBits = 0.8F;
/* 203 */   private boolean doDither = false;
/*     */ 
/* 205 */   private int ditherMode = 0;
/*     */   private static final int F_8 = 1;
/*     */   private static final int F_16 = 2;
/*     */   private static final int F_24 = 3;
/*     */   private static final int F_32 = 4;
/*     */   private static final int F_SAMPLE_WIDTH_MASK = 7;
/*     */   private static final int F_SIGNED = 8;
/*     */   private static final int F_BIGENDIAN = 16;
/*     */   private static final int CT_8S = 9;
/*     */   private static final int CT_8U = 1;
/*     */   private static final int CT_16SB = 26;
/*     */   private static final int CT_16SL = 10;
/*     */   private static final int CT_24SB = 27;
/*     */   private static final int CT_24SL = 11;
/*     */   private static final int CT_32SB = 28;
/*     */   private static final int CT_32SL = 12;
/*     */   private static final float twoPower7 = 128.0F;
/*     */   private static final float twoPower15 = 32768.0F;
/*     */   private static final float twoPower23 = 8388608.0F;
/*     */   private static final float twoPower31 = 2.147484E+09F;
/*     */   private static final float invTwoPower7 = 0.007813F;
/*     */   private static final float invTwoPower15 = 3.051758E-05F;
/*     */   private static final float invTwoPower23 = 1.192093E-07F;
/*     */   private static final float invTwoPower31 = 4.656613E-10F;
/*     */ 
/*     */   public FloatSampleBuffer()
/*     */   {
/* 230 */     this(0, 0, 1.0F);
/*     */   }
/*     */ 
/*     */   public FloatSampleBuffer(int channelCount, int sampleCount, float sampleRate) {
/* 234 */     init(channelCount, sampleCount, sampleRate, true);
/*     */   }
/*     */ 
/*     */   public FloatSampleBuffer(byte[] buffer, int offset, int byteCount, AudioFormat format)
/*     */   {
/* 239 */     this(format.getChannels(), byteCount / (format.getSampleSizeInBits() / 8 * format.getChannels()), format.getSampleRate());
/*     */ 
/* 242 */     initFromByteArray(buffer, offset, byteCount, format);
/*     */   }
/*     */ 
/*     */   protected void init(int channelCount, int sampleCount, float sampleRate) {
/* 246 */     init(channelCount, sampleCount, sampleRate, true);
/*     */   }
/*     */ 
/*     */   protected void init(int channelCount, int sampleCount, float sampleRate, boolean lazy) {
/* 250 */     if ((channelCount < 0) || (sampleCount < 0)) {
/* 251 */       throw new IllegalArgumentException("Invalid parameters in initialization of FloatSampleBuffer.");
/*     */     }
/*     */ 
/* 254 */     setSampleRate(sampleRate);
/* 255 */     if ((getSampleCount() != sampleCount) || (getChannelCount() != channelCount))
/* 256 */       createChannels(channelCount, sampleCount, lazy);
/*     */   }
/*     */ 
/*     */   private void createChannels(int channelCount, int sampleCount, boolean lazy)
/*     */   {
/* 261 */     this.sampleCount = sampleCount;
/*     */ 
/* 263 */     this.channelCount = 0;
/* 264 */     for (int ch = 0; ch < channelCount; ++ch) {
/* 265 */       insertChannel(ch, false, lazy);
/*     */     }
/* 267 */     while ((!lazy) && 
/* 269 */       (this.channels.size() > channelCount))
/* 270 */       this.channels.remove(this.channels.size() - 1);
/*     */   }
/*     */ 
/*     */   public void initFromByteArray(byte[] buffer, int offset, int byteCount, AudioFormat format)
/*     */   {
/* 278 */     initFromByteArray(buffer, offset, byteCount, format, true);
/*     */   }
/*     */ 
/*     */   public void initFromByteArray(byte[] buffer, int offset, int byteCount, AudioFormat format, boolean lazy)
/*     */   {
/* 283 */     if (offset + byteCount > buffer.length) {
/* 284 */       throw new IllegalArgumentException("FloatSampleBuffer.initFromByteArray: buffer too small.");
/*     */     }
/*     */ 
/* 287 */     boolean signed = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
/* 288 */     if ((!signed) && (!format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)))
/*     */     {
/* 290 */       throw new IllegalArgumentException("FloatSampleBuffer: only PCM samples are possible.");
/*     */     }
/*     */ 
/* 293 */     int bytesPerSample = format.getSampleSizeInBits() / 8;
/* 294 */     int bytesPerFrame = bytesPerSample * format.getChannels();
/* 295 */     int thisSampleCount = byteCount / bytesPerFrame;
/* 296 */     init(format.getChannels(), thisSampleCount, format.getSampleRate(), lazy);
/* 297 */     int formatType = getFormatType(format.getSampleSizeInBits(), signed, format.isBigEndian());
/*     */ 
/* 300 */     this.originalFormatType = formatType;
/* 301 */     for (int ch = 0; ch < format.getChannels(); ++ch) {
/* 302 */       convertByteToFloat(buffer, offset, bytesPerFrame, formatType, getChannel(ch), 0, this.sampleCount);
/*     */ 
/* 304 */       offset += bytesPerSample;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initFromFloatSampleBuffer(FloatSampleBuffer source) {
/* 309 */     init(source.getChannelCount(), source.getSampleCount(), source.getSampleRate());
/* 310 */     for (int ch = 0; ch < getChannelCount(); ++ch)
/* 311 */       System.arraycopy(source.getChannel(ch), 0, getChannel(ch), 0, this.sampleCount);
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 320 */     init(0, 0, 1.0F, false);
/*     */   }
/*     */ 
/*     */   public void reset(int channels, int sampleCount, float sampleRate)
/*     */   {
/* 328 */     init(channels, sampleCount, sampleRate, false);
/*     */   }
/*     */ 
/*     */   public int getByteArrayBufferSize(AudioFormat format)
/*     */   {
/* 338 */     if ((!format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) && (!format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)))
/*     */     {
/* 340 */       throw new IllegalArgumentException("FloatSampleBuffer: only PCM samples are possible.");
/*     */     }
/*     */ 
/* 343 */     int bytesPerSample = format.getSampleSizeInBits() / 8;
/* 344 */     int bytesPerFrame = bytesPerSample * format.getChannels();
/* 345 */     return bytesPerFrame * getSampleCount();
/*     */   }
/*     */ 
/*     */   public int convertToByteArray(byte[] buffer, int offset, AudioFormat format)
/*     */   {
/* 353 */     int byteCount = getByteArrayBufferSize(format);
/* 354 */     if (offset + byteCount > buffer.length) {
/* 355 */       throw new IllegalArgumentException("FloatSampleBuffer.convertToByteArray: buffer too small.");
/*     */     }
/*     */ 
/* 358 */     boolean signed = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
/* 359 */     if ((!signed) && (!format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)))
/*     */     {
/* 361 */       throw new IllegalArgumentException("FloatSampleBuffer.convertToByteArray: only PCM samples are allowed.");
/*     */     }
/*     */ 
/* 364 */     if (format.getSampleRate() != getSampleRate()) {
/* 365 */       throw new IllegalArgumentException("FloatSampleBuffer.convertToByteArray: different samplerates.");
/*     */     }
/*     */ 
/* 368 */     if (format.getChannels() != getChannelCount()) {
/* 369 */       throw new IllegalArgumentException("FloatSampleBuffer.convertToByteArray: different channel count.");
/*     */     }
/*     */ 
/* 372 */     int bytesPerSample = format.getSampleSizeInBits() / 8;
/* 373 */     int bytesPerFrame = bytesPerSample * format.getChannels();
/* 374 */     int formatType = getFormatType(format.getSampleSizeInBits(), signed, format.isBigEndian());
/*     */ 
/* 376 */     for (int ch = 0; ch < format.getChannels(); ++ch) {
/* 377 */       convertFloatToByte(getChannel(ch), this.sampleCount, buffer, offset, bytesPerFrame, formatType);
/*     */ 
/* 380 */       offset += bytesPerSample;
/*     */     }
/* 382 */     return getSampleCount() * bytesPerFrame;
/*     */   }
/*     */ 
/*     */   public byte[] convertToByteArray(AudioFormat format)
/*     */   {
/* 394 */     byte[] res = new byte[getByteArrayBufferSize(format)];
/* 395 */     convertToByteArray(res, 0, format);
/* 396 */     return res;
/*     */   }
/*     */ 
/*     */   public void changeSampleCount(int newSampleCount, boolean keepOldSamples)
/*     */   {
/* 409 */     int oldSampleCount = getSampleCount();
/* 410 */     if (oldSampleCount == newSampleCount) {
/* 411 */       return;
/*     */     }
/* 413 */     Object[] oldChannels = null;
/* 414 */     if (keepOldSamples) {
/* 415 */       oldChannels = getAllChannels();
/*     */     }
/* 417 */     init(getChannelCount(), newSampleCount, getSampleRate());
/* 418 */     if (!keepOldSamples)
/*     */       return;
/* 420 */     int copyCount = (newSampleCount < oldSampleCount) ? newSampleCount : oldSampleCount;
/*     */ 
/* 422 */     for (int ch = 0; ch < getChannelCount(); ++ch) {
/* 423 */       float[] oldSamples = (float[])oldChannels[ch];
/* 424 */       float[] newSamples = (float[])getChannel(ch);
/* 425 */       if (oldSamples != newSamples)
/*     */       {
/* 427 */         System.arraycopy(oldSamples, 0, newSamples, 0, copyCount);
/*     */       }
/* 429 */       if (oldSampleCount >= newSampleCount)
/*     */         continue;
/* 431 */       for (int i = oldSampleCount; i < newSampleCount; ++i)
/* 432 */         newSamples[i] = 0.0F;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void makeSilence()
/*     */   {
/* 441 */     if (getChannelCount() > 0) {
/* 442 */       makeSilence(0);
/* 443 */       for (int ch = 1; ch < getChannelCount(); ++ch)
/* 444 */         copyChannel(0, ch);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void makeSilence(int channel)
/*     */   {
/* 450 */     float[] samples = getChannel(0);
/* 451 */     for (int i = 0; i < getSampleCount(); ++i)
/* 452 */       samples[i] = 0.0F;
/*     */   }
/*     */ 
/*     */   public void addChannel(boolean silent)
/*     */   {
/* 458 */     insertChannel(getChannelCount(), silent);
/*     */   }
/*     */ 
/*     */   public void insertChannel(int index, boolean silent)
/*     */   {
/* 465 */     insertChannel(index, silent, true);
/*     */   }
/*     */ 
/*     */   public void insertChannel(int index, boolean silent, boolean lazy)
/*     */   {
/* 479 */     int physSize = this.channels.size();
/* 480 */     int virtSize = getChannelCount();
/* 481 */     float[] newChannel = null;
/* 482 */     if (physSize > virtSize)
/*     */     {
/* 484 */       for (int ch = virtSize; ch < physSize; ++ch) {
/* 485 */         float[] thisChannel = (float[])this.channels.get(ch);
/* 486 */         if ((((!lazy) || (thisChannel.length < getSampleCount()))) && (((lazy) || (thisChannel.length != getSampleCount())))) {
/*     */           continue;
/*     */         }
/* 489 */         newChannel = thisChannel;
/* 490 */         this.channels.remove(ch);
/* 491 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 495 */     if (newChannel == null) {
/* 496 */       newChannel = new float[getSampleCount()];
/*     */     }
/* 498 */     this.channels.add(index, newChannel);
/* 499 */     this.channelCount += 1;
/* 500 */     if (silent)
/* 501 */       makeSilence(index);
/*     */   }
/*     */ 
/*     */   public void removeChannel(int channel)
/*     */   {
/* 507 */     removeChannel(channel, true);
/*     */   }
/*     */ 
/*     */   public void removeChannel(int channel, boolean lazy)
/*     */   {
/* 518 */     if (!lazy)
/* 519 */       this.channels.remove(channel);
/* 520 */     else if (channel < getChannelCount() - 1)
/*     */     {
/* 522 */       this.channels.add(this.channels.remove(channel));
/*     */     }
/* 524 */     this.channelCount -= 1;
/*     */   }
/*     */ 
/*     */   public void copyChannel(int sourceChannel, int targetChannel)
/*     */   {
/* 532 */     float[] source = getChannel(sourceChannel);
/* 533 */     float[] target = getChannel(targetChannel);
/* 534 */     System.arraycopy(source, 0, target, 0, getSampleCount());
/*     */   }
/*     */ 
/*     */   public void copy(int sourceIndex, int destIndex, int length)
/*     */   {
/* 542 */     for (int i = 0; i < getChannelCount(); ++i)
/* 543 */       copy(i, sourceIndex, destIndex, length);
/*     */   }
/*     */ 
/*     */   public void copy(int channel, int sourceIndex, int destIndex, int length)
/*     */   {
/* 552 */     float[] data = getChannel(channel);
/* 553 */     int bufferCount = getSampleCount();
/* 554 */     if ((sourceIndex + length > bufferCount) || (destIndex + length > bufferCount) || (sourceIndex < 0) || (destIndex < 0) || (length < 0))
/*     */     {
/* 556 */       throw new IndexOutOfBoundsException("parameters exceed buffer size");
/*     */     }
/* 558 */     System.arraycopy(data, sourceIndex, data, destIndex, length);
/*     */   }
/*     */ 
/*     */   public void expandChannel(int targetChannelCount)
/*     */   {
/* 572 */     if (getChannelCount() != 1) {
/* 573 */       throw new IllegalArgumentException("FloatSampleBuffer: can only expand channels for mono signals.");
/*     */     }
/*     */ 
/* 576 */     for (int ch = 1; ch < targetChannelCount; ++ch) {
/* 577 */       addChannel(false);
/* 578 */       copyChannel(0, ch);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mixDownChannels()
/*     */   {
/* 590 */     float[] firstChannel = getChannel(0);
/* 591 */     int sampleCount = getSampleCount();
/* 592 */     int channelCount = getChannelCount();
/* 593 */     for (int ch = channelCount - 1; ch > 0; --ch) {
/* 594 */       float[] thisChannel = getChannel(ch);
/* 595 */       for (int i = 0; i < sampleCount; ++i) {
/* 596 */         firstChannel[i] += thisChannel[i];
/*     */       }
/* 598 */       removeChannel(ch);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setSamplesFromBytes(byte[] srcBuffer, int srcOffset, AudioFormat format, int destOffset, int lengthInSamples)
/*     */   {
/* 604 */     int bytesPerSample = (format.getSampleSizeInBits() + 7) / 8;
/* 605 */     int bytesPerFrame = bytesPerSample * format.getChannels();
/*     */ 
/* 607 */     if (srcOffset + lengthInSamples * bytesPerFrame > srcBuffer.length) {
/* 608 */       throw new IllegalArgumentException("FloatSampleBuffer.setSamplesFromBytes: srcBuffer too small.");
/*     */     }
/*     */ 
/* 611 */     if (destOffset + lengthInSamples > getSampleCount()) {
/* 612 */       throw new IllegalArgumentException("FloatSampleBuffer.setSamplesFromBytes: destBuffer too small.");
/*     */     }
/*     */ 
/* 615 */     boolean signed = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
/* 616 */     boolean unsigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED);
/* 617 */     if ((!signed) && (!unsigned)) {
/* 618 */       throw new IllegalArgumentException("FloatSampleBuffer: only PCM samples are possible.");
/*     */     }
/*     */ 
/* 621 */     int formatType = getFormatType(format.getSampleSizeInBits(), signed, format.isBigEndian());
/*     */ 
/* 624 */     for (int ch = 0; ch < format.getChannels(); ++ch) {
/* 625 */       convertByteToFloat(srcBuffer, srcOffset, bytesPerFrame, formatType, getChannel(ch), destOffset, lengthInSamples);
/*     */ 
/* 627 */       srcOffset += bytesPerSample;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getChannelCount()
/*     */   {
/* 634 */     return this.channelCount;
/*     */   }
/*     */ 
/*     */   public int getSampleCount() {
/* 638 */     return this.sampleCount;
/*     */   }
/*     */ 
/*     */   public float getSampleRate() {
/* 642 */     return this.sampleRate;
/*     */   }
/*     */ 
/*     */   public void setSampleRate(float sampleRate)
/*     */   {
/* 650 */     if (sampleRate <= 0.0F) {
/* 651 */       throw new IllegalArgumentException("Invalid samplerate for FloatSampleBuffer.");
/*     */     }
/*     */ 
/* 654 */     this.sampleRate = sampleRate;
/*     */   }
/*     */ 
/*     */   public float[] getChannel(int channel)
/*     */   {
/* 662 */     if ((channel < 0) || (channel >= getChannelCount())) {
/* 663 */       throw new IllegalArgumentException("FloatSampleBuffer: invalid channel number.");
/*     */     }
/*     */ 
/* 666 */     return (float[])this.channels.get(channel);
/*     */   }
/*     */ 
/*     */   public Object[] getAllChannels() {
/* 670 */     Object[] res = new Object[getChannelCount()];
/* 671 */     for (int ch = 0; ch < getChannelCount(); ++ch) {
/* 672 */       res[ch] = getChannel(ch);
/*     */     }
/* 674 */     return res;
/*     */   }
/*     */ 
/*     */   public void setDitherBits(float ditherBits)
/*     */   {
/* 683 */     if (ditherBits <= 0.0F) {
/* 684 */       throw new IllegalArgumentException("DitherBits must be greater than 0");
/*     */     }
/* 686 */     this.ditherBits = ditherBits;
/*     */   }
/*     */ 
/*     */   public float getDitherBits() {
/* 690 */     return this.ditherBits;
/*     */   }
/*     */ 
/*     */   public void setDitherMode(int mode)
/*     */   {
/* 704 */     if ((mode != 0) && (mode != 1) && (mode != 2))
/*     */     {
/* 707 */       throw new IllegalArgumentException("Illegal DitherMode");
/*     */     }
/* 709 */     this.ditherMode = mode;
/*     */   }
/*     */ 
/*     */   public int getDitherMode() {
/* 713 */     return this.ditherMode;
/*     */   }
/*     */ 
/*     */   public int getFormatType(int ssib, boolean signed, boolean bigEndian)
/*     */   {
/* 720 */     int bytesPerSample = ssib / 8;
/* 721 */     int res = 0;
/* 722 */     if (ssib == 8)
/* 723 */       res = 1;
/* 724 */     else if (ssib == 16)
/* 725 */       res = 2;
/* 726 */     else if (ssib == 24)
/* 727 */       res = 3;
/* 728 */     else if (ssib == 32) {
/* 729 */       res = 4;
/*     */     }
/* 731 */     if (res == 0) {
/* 732 */       throw new IllegalArgumentException("FloatSampleBuffer: unsupported sample size of " + ssib + " bits per sample.");
/*     */     }
/*     */ 
/* 736 */     if ((!signed) && (bytesPerSample > 1)) {
/* 737 */       throw new IllegalArgumentException("FloatSampleBuffer: unsigned samples larger than 8 bit are not supported");
/*     */     }
/*     */ 
/* 741 */     if (signed) {
/* 742 */       res |= 8;
/*     */     }
/* 744 */     if ((bigEndian) && (ssib != 8)) {
/* 745 */       res |= 16;
/*     */     }
/* 747 */     return res;
/*     */   }
/*     */ 
/*     */   private static void convertByteToFloat(byte[] input, int inputOffset, int bytesPerFrame, int formatType, float[] output, int outputOffset, int sampleCount)
/*     */   {
/* 769 */     int endCount = outputOffset + sampleCount;
/* 770 */     for (int sample = outputOffset; sample < endCount; ++sample)
/*     */     {
/* 772 */       switch (formatType)
/*     */       {
/*     */       case 9:
/* 774 */         output[sample] = (input[inputOffset] * 0.007813F);
/*     */ 
/* 776 */         break;
/*     */       case 1:
/* 778 */         output[sample] = (((input[inputOffset] & 0xFF) - 128) * 0.007813F);
/*     */ 
/* 780 */         break;
/*     */       case 26:
/* 782 */         output[sample] = ((input[inputOffset] << 8 | input[(inputOffset + 1)] & 0xFF) * 3.051758E-05F);
/*     */ 
/* 785 */         break;
/*     */       case 10:
/* 787 */         output[sample] = ((input[(inputOffset + 1)] << 8 | input[inputOffset] & 0xFF) * 3.051758E-05F);
/*     */ 
/* 790 */         break;
/*     */       case 27:
/* 792 */         output[sample] = ((input[inputOffset] << 16 | (input[(inputOffset + 1)] & 0xFF) << 8 | input[(inputOffset + 2)] & 0xFF) * 1.192093E-07F);
/*     */ 
/* 796 */         break;
/*     */       case 11:
/* 798 */         output[sample] = ((input[(inputOffset + 2)] << 16 | (input[(inputOffset + 1)] & 0xFF) << 8 | input[inputOffset] & 0xFF) * 1.192093E-07F);
/*     */ 
/* 802 */         break;
/*     */       case 28:
/* 804 */         output[sample] = ((input[inputOffset] << 24 | (input[(inputOffset + 1)] & 0xFF) << 16 | (input[(inputOffset + 2)] & 0xFF) << 8 | input[(inputOffset + 3)] & 0xFF) * 4.656613E-10F);
/*     */ 
/* 809 */         break;
/*     */       case 12:
/* 811 */         output[sample] = ((input[(inputOffset + 3)] << 24 | (input[(inputOffset + 2)] & 0xFF) << 16 | (input[(inputOffset + 1)] & 0xFF) << 8 | input[inputOffset] & 0xFF) * 4.656613E-10F);
/*     */ 
/* 816 */         break;
/*     */       case 2:
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/*     */       case 6:
/*     */       case 7:
/*     */       case 8:
/*     */       case 13:
/*     */       case 14:
/*     */       case 15:
/*     */       case 16:
/*     */       case 17:
/*     */       case 18:
/*     */       case 19:
/*     */       case 20:
/*     */       case 21:
/*     */       case 22:
/*     */       case 23:
/*     */       case 24:
/*     */       case 25:
/*     */       default:
/* 818 */         throw new IllegalArgumentException("Unsupported formatType=" + formatType);
/*     */       }
/*     */ 
/* 821 */       inputOffset += bytesPerFrame;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected byte quantize8(float sample) {
/* 826 */     if (this.doDither) {
/* 827 */       sample += random.nextFloat() * this.ditherBits;
/*     */     }
/* 829 */     if (sample >= 127.0F)
/* 830 */       return 127;
/* 831 */     if (sample <= -128.0F) {
/* 832 */       return -128;
/*     */     }
/* 834 */     return (byte)(int)((sample < 0.0F) ? sample - 0.5F : sample + 0.5F);
/*     */   }
/*     */ 
/*     */   protected int quantize16(float sample)
/*     */   {
/* 839 */     if (this.doDither) {
/* 840 */       sample += random.nextFloat() * this.ditherBits;
/*     */     }
/* 842 */     if (sample >= 32767.0F)
/* 843 */       return 32767;
/* 844 */     if (sample <= -32768.0F) {
/* 845 */       return -32768;
/*     */     }
/* 847 */     return (int)((sample < 0.0F) ? sample - 0.5F : sample + 0.5F);
/*     */   }
/*     */ 
/*     */   protected int quantize24(float sample)
/*     */   {
/* 852 */     if (this.doDither) {
/* 853 */       sample += random.nextFloat() * this.ditherBits;
/*     */     }
/* 855 */     if (sample >= 8388607.0F)
/* 856 */       return 8388607;
/* 857 */     if (sample <= -8388608.0F) {
/* 858 */       return -8388608;
/*     */     }
/* 860 */     return (int)((sample < 0.0F) ? sample - 0.5F : sample + 0.5F);
/*     */   }
/*     */ 
/*     */   protected int quantize32(float sample)
/*     */   {
/* 865 */     if (this.doDither) {
/* 866 */       sample += random.nextFloat() * this.ditherBits;
/*     */     }
/* 868 */     if (sample >= 2.147484E+09F)
/* 869 */       return 2147483647;
/* 870 */     if (sample <= -2.147484E+09F) {
/* 871 */       return -2147483648;
/*     */     }
/* 873 */     return (int)((sample < 0.0F) ? sample - 0.5F : sample + 0.5F);
/*     */   }
/*     */ 
/*     */   private void convertFloatToByte(float[] input, int sampleCount, byte[] output, int offset, int bytesPerFrame, int formatType)
/*     */   {
/* 887 */     switch (this.ditherMode)
/*     */     {
/*     */     case 0:
/* 889 */       this.doDither = ((this.originalFormatType & 0x7) > (formatType & 0x7));
/*     */ 
/* 891 */       break;
/*     */     case 1:
/* 893 */       this.doDither = true;
/* 894 */       break;
/*     */     case 2:
/* 896 */       this.doDither = false;
/*     */     }
/*     */ 
/* 899 */     if ((this.doDither) && (random == null))
/*     */     {
/* 901 */       random = new Random();
/*     */     }

	int iSample;
/*     */ 
/* 905 */     for (int inIndex = 0; inIndex < sampleCount; ++inIndex)
/*     */     {
/* 907 */       switch (formatType)
/*     */       {
/*     */       case 9:
/* 909 */         output[offset] = quantize8(input[inIndex] * 128.0F);
/* 910 */         break;
/*     */       case 1:
/* 912 */         output[offset] = (byte)(quantize8(input[inIndex] * 128.0F) + 128);
/* 913 */         break;
/*     */       case 26:
/* 915 */         iSample = quantize16(input[inIndex] * 32768.0F);
/* 916 */         output[offset] = (byte)(iSample >> 8);
/* 917 */         output[(offset + 1)] = (byte)(iSample & 0xFF);
/* 918 */         break;
/*     */       case 10:
/* 920 */         iSample = quantize16(input[inIndex] * 32768.0F);
/* 921 */         output[(offset + 1)] = (byte)(iSample >> 8);
/* 922 */         output[offset] = (byte)(iSample & 0xFF);
/* 923 */         break;
/*     */       case 27:
/* 925 */         iSample = quantize24(input[inIndex] * 8388608.0F);
/* 926 */         output[offset] = (byte)(iSample >> 16);
/* 927 */         output[(offset + 1)] = (byte)(iSample >>> 8 & 0xFF);
/* 928 */         output[(offset + 2)] = (byte)(iSample & 0xFF);
/* 929 */         break;
/*     */       case 11:
/* 931 */         iSample = quantize24(input[inIndex] * 8388608.0F);
/* 932 */         output[(offset + 2)] = (byte)(iSample >> 16);
/* 933 */         output[(offset + 1)] = (byte)(iSample >>> 8 & 0xFF);
/* 934 */         output[offset] = (byte)(iSample & 0xFF);
/* 935 */         break;
/*     */       case 28:
/* 937 */         iSample = quantize32(input[inIndex] * 2.147484E+09F);
/* 938 */         output[offset] = (byte)(iSample >> 24);
/* 939 */         output[(offset + 1)] = (byte)(iSample >>> 16 & 0xFF);
/* 940 */         output[(offset + 2)] = (byte)(iSample >>> 8 & 0xFF);
/* 941 */         output[(offset + 3)] = (byte)(iSample & 0xFF);
/* 942 */         break;
/*     */       case 12:
/* 944 */         iSample = quantize32(input[inIndex] * 2.147484E+09F);
/* 945 */         output[(offset + 3)] = (byte)(iSample >> 24);
/* 946 */         output[(offset + 2)] = (byte)(iSample >>> 16 & 0xFF);
/* 947 */         output[(offset + 1)] = (byte)(iSample >>> 8 & 0xFF);
/* 948 */         output[offset] = (byte)(iSample & 0xFF);
/* 949 */         break;
/*     */       case 2:
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/*     */       case 6:
/*     */       case 7:
/*     */       case 8:
/*     */       case 13:
/*     */       case 14:
/*     */       case 15:
/*     */       case 16:
/*     */       case 17:
/*     */       case 18:
/*     */       case 19:
/*     */       case 20:
/*     */       case 21:
/*     */       case 22:
/*     */       case 23:
/*     */       case 24:
/*     */       case 25:
/*     */       default:
/* 951 */         throw new IllegalArgumentException("Unsupported formatType=" + formatType);
/*     */       }
/*     */ 
/* 954 */       offset += bytesPerFrame;
/*     */     }
/*     */   }
/*     */ 
/*     */   private static String formatType2Str(int formatType)
/*     */   {
/* 965 */     String res = "" + formatType + ": ";
/* 966 */     switch (formatType & 0x7)
/*     */     {
/*     */     case 1:
/* 968 */       res = res + "8bit";
/* 969 */       break;
/*     */     case 2:
/* 971 */       res = res + "16bit";
/* 972 */       break;
/*     */     case 3:
/* 974 */       res = res + "24bit";
/* 975 */       break;
/*     */     case 4:
/* 977 */       res = res + "32bit";
/*     */     }
/*     */ 
/* 980 */     res = res + (((formatType & 0x8) == 8) ? " signed" : " unsigned");
/* 981 */     if ((formatType & 0x7) != 1) {
/* 982 */       res = res + (((formatType & 0x10) == 16) ? " big endian" : " little endian");
/*     */     }
/*     */ 
/* 985 */     return res;
/*     */   }
/*     */ }
