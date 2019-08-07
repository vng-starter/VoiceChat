/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceprocess;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import static voiceprocess.VoiceProcess.getAudioFormat;

/**
 *
 * @author TBN
 */
public class Speaker extends Thread {
    @Override
    public void run() {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, VoiceProcess.getAudioFormat());            
            while (true) {
                //if (VoiceProcess.tempBuffer != null) {
                try {
                    SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    sourceDataLine.open(getAudioFormat());
                    sourceDataLine.start();
                    VoiceProcess.tempBuffer = new byte[VoiceProcess.line.getBufferSize()];
                    VoiceProcess.line.read(VoiceProcess.tempBuffer, 0, VoiceProcess.tempBuffer.length);
                    System.out.println(VoiceProcess.tempBuffer[0]);
                    sourceDataLine.write(VoiceProcess.tempBuffer, 0, VoiceProcess.tempBuffer.length);
                    sourceDataLine.drain();

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                //}
            }
        
    }

}
