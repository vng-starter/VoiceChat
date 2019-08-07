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
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author CPU11516-local
 */
public class VoiceProcess {

    public static DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
    public static TargetDataLine line;
    public static byte[] tempBuffer;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LineUnavailableException {
        line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        line.open(getAudioFormat());
        line.start();

        Thread speakerThread = new Speaker();
        speakerThread.start();

        while (AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
            try {
//                tempBuffer = new byte[line.getBufferSize()];
//                int read = line.read(tempBuffer, 0, tempBuffer.length);
                //speak(tempBuffer);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static AudioFormat getAudioFormat() { //you may change these parameters to fit you mic
        float sampleRate = 16000.0F;  //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;    //8,16
        int channels = 2;             //1,2
        boolean signed = true;        //true,false
        boolean bigEndian = false;    //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public static byte[] toByteArray(double[] doubleArray) {
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[doubleArray.length * times];
        for (int i = 0; i < doubleArray.length; i++) {
            ByteBuffer.wrap(bytes, i * times, times).putDouble(doubleArray[i]);
        }
        return bytes;
    }

    public static double[] toDoubleArray(byte[] byteArray) {
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
        }
        return doubles;
    }

}
