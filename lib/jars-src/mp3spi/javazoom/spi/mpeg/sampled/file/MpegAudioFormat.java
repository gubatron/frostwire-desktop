/*    */ package javazoom.spi.mpeg.sampled.file;
/*    */ 
/*    */ import java.util.Map;
/*    */ import javax.sound.sampled.AudioFormat;
/*    */ import org.tritonus.share.sampled.TAudioFormat;
/*    */ 
/*    */ public class MpegAudioFormat extends TAudioFormat
/*    */ {
/*    */   public MpegAudioFormat(AudioFormat.Encoding encoding, float nFrequency, int SampleSizeInBits, int nChannels, int FrameSize, float FrameRate, boolean isBigEndian, Map properties)
/*    */   {
/* 50 */     super(encoding, nFrequency, SampleSizeInBits, nChannels, FrameSize, FrameRate, isBigEndian, properties);
/*    */   }
/*    */ 
/*    */   public Map properties()
/*    */   {
/* 65 */     return super.properties();
/*    */   }
/*    */ }

