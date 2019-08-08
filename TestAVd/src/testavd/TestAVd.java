/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testavd;

/**
 *
 * @author CPU11516-local
 */
import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestAVd {

    private static final int RAW_SAMPLE_RATE = 48000;
    private static final int CHANNEL = 1;
    private static final int FRAME_SIZE = 480;
    private static final int SAMPLE_RATE = 48000;
    static long id = 0;
    static int sample_size = RAW_SAMPLE_RATE * 2 * CHANNEL / 10;

    public static void main(String[] args) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(RAW_SAMPLE_RATE, 16, CHANNEL, true, true);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("not supported");
        }
        TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
        // Obtain and open the line.
        microphone.open(format);
        // Begin audio capture.
        microphone.start();

        SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
        speaker.open(format);
        speaker.start();

        IntBuffer error = IntBuffer.allocate(4);
//        PointerByReference opusDecoder = Opus.INSTANCE.opus_decoder_create(SAMPLE_RATE, CHANNEL, error);
//        PointerByReference opusEncoder = Opus.INSTANCE.opus_encoder_create(SAMPLE_RATE, CHANNEL, Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY, error);
        int delayCnt = 0;
        while (true) {
            ShortBuffer dataFromMic = recordFromMicrophone(microphone);
            short[] tmp = new short[dataFromMic.remaining()];
            dataFromMic.duplicate().get(tmp);
            ShortBuffer dataFromMicEdit = ShortBuffer.allocate(tmp.length);
            dataFromMicEdit.put(tmp);
            dataFromMicEdit.flip();
            boolean isSil = isSilence(tmp, -100);
            if (!isSil || delayCnt > 0) {
                id += 1;
                delayCnt = isSil ? 0 : delayCnt - 1;
                System.out.println("read["+id+"-"+System.nanoTime()+"]:"+Arrays.toString(tmp));
                long start = System.nanoTime();
//	            List<ByteBuffer> packets = encode(opusEncoder, dataFromMic);
                // packets go over network
//	            ShortBuffer decodedFromNetwork = decode(opusDecoder, packets);
                long stop = System.nanoTime();
                //System.out.println((stop - start) / 1000000D + "ms");
//	            playBack(speaker, decodedFromNetwork);
//	            playBack(speaker, dataFromMicEdit);
            } else {
                playSilence(speaker);
            }

        }

//      Opus.INSTANCE.opus_decoder_destroy(opusDecoder);
//      Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
    }

//    private static ShortBuffer decode(PointerByReference opusDecoder, List<ByteBuffer> packets) {
//        ShortBuffer shortBuffer = ShortBuffer.allocate(packets.size() * FRAME_SIZE * CHANNEL *2);
//        for (ByteBuffer dataBuffer : packets) {
//            byte[] transferedBytes = new byte[dataBuffer.remaining()];
//            dataBuffer.get(transferedBytes);
//            int decoded = Opus.INSTANCE.opus_decode(opusDecoder, transferedBytes, transferedBytes.length, shortBuffer, FRAME_SIZE, 0);
//            shortBuffer.position(shortBuffer.position() + decoded);
//        }
//        shortBuffer.flip();
//        return shortBuffer;
//    }
//    private static List<ByteBuffer> encode(PointerByReference opusEncoder, ShortBuffer shortBuffer) {
//        int read = 0;
//        List<ByteBuffer> list = new ArrayList<>();
//        while (shortBuffer.hasRemaining()) {
//            ByteBuffer dataBuffer = ByteBuffer.allocate(CHANNEL*8*1024);
//            int toRead = Math.min(shortBuffer.remaining(), dataBuffer.remaining());
//            read = Opus.INSTANCE.opus_encode(opusEncoder, shortBuffer, FRAME_SIZE, dataBuffer, toRead);
//            dataBuffer.position(dataBuffer.position() + read);
//            dataBuffer.flip();
//            list.add(dataBuffer);
//            shortBuffer.position(shortBuffer.position() + FRAME_SIZE);
//        }
//
//        // used for debugging
//        shortBuffer.flip();
//        return list;
//    }
    private static byte[] audioSilence = null;

    private static synchronized void playSilence(SourceDataLine speaker) throws LineUnavailableException {
        if (audioSilence == null) {
            audioSilence = new byte[sample_size];
            for (int i = 0; i < audioSilence.length; i++) {
                audioSilence[i] = 0;
            }
        }
        speaker.write(audioSilence, 0, audioSilence.length);
    }

    private static void playBack(SourceDataLine speaker, ShortBuffer shortBuffer) throws LineUnavailableException {
        short[] shortAudioBuffer = new short[shortBuffer.remaining()];
        shortBuffer.get(shortAudioBuffer);
        byte[] audio = ShortToByte_Twiddle_Method(shortAudioBuffer);
        System.out.println("write["+id+"-"+System.nanoTime()+"]:"+Arrays.toString(shortAudioBuffer));
        speaker.write(audio, 0, audio.length);
    }

    private static ShortBuffer recordFromMicrophone(TargetDataLine microphone) throws LineUnavailableException {
        // Assume that the TargetDataLine, line, has already been obtained and
        // opened.

        byte[] data = new byte[sample_size];
        // probably way too big
        // Here, stopped is a global boolean set by another thread.
        int numBytesRead;
        numBytesRead = microphone.read(data, 0, data.length);
        //System.out.println("rawEnc:"+StringUtil.toHexString(data));
        ShortBuffer shortBuffer = ShortBuffer.allocate(numBytesRead * 2);
        shortBuffer.put(ByteBuffer.wrap(data).asShortBuffer());
        shortBuffer.flip();
        return shortBuffer;
    }

    private static byte[] ShortToByte_Twiddle_Method(final short[] input) {
        final int len = input.length;
        final byte[] buffer = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            buffer[(i * 2) + 1] = (byte) (input[i]);
            buffer[(i * 2)] = (byte) (input[i] >> 8);
        }
        return buffer;
    }

    /**
     * Calculates the local (linear) energy of an audio buffer.
     *
     * @param buffer The audio buffer.
     * @return The local (linear) energy of an audio buffer.
     */
    private static double localEnergy(final float[] buffer) {
        double power = 0.0D;
        for (float element : buffer) {
            power += element * element;
        }
        return power;
    }

    /**
     * Returns the dBSPL for a buffer.
     *
     * @param buffer The buffer with audio information.
     * @return The dBSPL level for the buffer.
     */
    private static double soundPressureLevel(final float[] buffer) {
        double value = Math.pow(localEnergy(buffer), 0.5);
        value = value / buffer.length;
        return linearToDecibel(value);
    }

    /**
     * Converts a linear to a dB value.
     *
     * @param value The value to convert.
     * @return The converted value.
     */
    private static double linearToDecibel(final double value) {
        //System.out.println(value + " \t " + Math.log10(value));
        return 20.0 * Math.log10(value);
    }

    /**
     * Checks if the dBSPL level in the buffer falls below a certain threshold.
     *
     * @param buffer The buffer with audio information.
     * @param silenceThreshold The threshold in dBSPL
     * @return True if the audio information in buffer corresponds with silence,
     * false otherwise.
     */
    public static boolean isSilence(final float[] buffer, final double silenceThreshold) {
        double currentSPL = soundPressureLevel(buffer);
        return currentSPL < silenceThreshold;
    }

    public static boolean isSilence(short[] data, double dBSilence) {
        boolean bKq = true;
        float[] dataf = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            float f = data[i] / 32768.0f;
            if (f > 1) {
                f = 1;
            } else if (f < -1) {
                f = -1;
            }
            dataf[i] = f;
        }
        bKq = isSilence(dataf, dBSilence);
        return bKq;
    }
}
