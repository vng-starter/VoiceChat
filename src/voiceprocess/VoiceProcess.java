/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceprocess;

import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author CPU11516-local
 */
public class VoiceProcess {

    public static DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
    public static TargetDataLine line;
    public static final float SAMPLE_RATE = 16000.0F;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LineUnavailableException {
        line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        line.open(getAudioFormat());
        line.start();

        Thread speakerThread = new Speaker();
        speakerThread.start();

    }

    public static AudioFormat getAudioFormat() { //you may change these parameters to fit you mic
        float sampleRate = SAMPLE_RATE;  //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;    //8,16
        int channels = 2;             //1,2
        boolean signed = true;        //true,false
        boolean bigEndian = false;    //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }


}
