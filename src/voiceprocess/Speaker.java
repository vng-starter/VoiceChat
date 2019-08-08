/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceprocess;

import java.nio.ByteBuffer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author TBN
 */
public class Speaker extends Thread {

    static byte[] tempBuffer;

    @Override
    public void run() {
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, VoiceProcess.getAudioFormat());
        while (true) {
            //if (tempBuffer != null) {
            try {
                SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(VoiceProcess.getAudioFormat());
                sourceDataLine.start();
                tempBuffer = new byte[VoiceProcess.line.getBufferSize()];
                VoiceProcess.line.read(tempBuffer, 0, tempBuffer.length);

                AutocorrellatedVoiceActivityDetector avd = new AutocorrellatedVoiceActivityDetector();
                byte[] voiceOut = avd.removeSilence(tempBuffer, VoiceProcess.SAMPLE_RATE);
                sourceDataLine.write(voiceOut, 0, voiceOut.length);
                sourceDataLine.drain();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            //}
        }

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
