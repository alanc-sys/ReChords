package com.misacordes.application.controller.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TunerWebSocketHandler extends BinaryWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int SAMPLE_RATE = 44100;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> hello = new HashMap<>();
        hello.put("type", "hello");
        hello.put("message", "Tuner WS connected");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(hello)));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();

        int samples = payload.remaining() / 2; // 16-bit
        float[] buffer = new float[samples];
        for (int i = 0; i < samples; i++) {
            short s = (short) ((payload.get() & 0xFF) | (payload.get() << 8));
            buffer[i] = s / 32768f;
        }

        double frequency = detectPitchHz(buffer, SAMPLE_RATE);

        Map<String, Object> result = new HashMap<>();
        result.put("type", "pitch");
        result.put("frequency", frequency);
        result.put("note", getNoteName(frequency));
        result.put("cents", getCentsFromA4(frequency));

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Map<String, Object> err = new HashMap<>();
        err.put("type", "error");
        err.put("message", exception.getMessage());
        safeSend(session, err);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    }

    private void safeSend(WebSocketSession session, Map<String, Object> payload) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (IOException ignored) {}
    }

    private static double detectPitchHz(float[] audioBuffer, int sampleRate) {
        int size = audioBuffer.length;
        if (size < 1024) return 0;

        double rms = 0;
        for (float sample : audioBuffer) {
            rms += sample * sample;
        }
        rms = Math.sqrt(rms / size);

        if (rms < 0.01) return 0;

        int minLag = sampleRate / 1200; // ~37 samples para 44100 Hz
        int maxLag = sampleRate / 82;   // ~537 samples para 44100 Hz
        
        if (maxLag >= size / 2) maxLag = size / 2 - 1;
        if (minLag < 1) minLag = 1;

        double[] acf = new double[maxLag + 1];
        double[] nsdf = new double[maxLag + 1];
        
        // Calcular autocorrelación
        for (int lag = minLag; lag <= maxLag; lag++) {
            double sum = 0;
            for (int i = 0; i < size - lag; i++) {
                sum += audioBuffer[i] * audioBuffer[i + lag];
            }
            acf[lag] = sum;
        }

        double maxAcf = 0;
        for (int lag = minLag; lag <= maxLag; lag++) {
            if (acf[lag] > maxAcf) maxAcf = acf[lag];
        }
        
        if (maxAcf == 0) return 0;
        
        for (int lag = minLag; lag <= maxLag; lag++) {
            nsdf[lag] = acf[lag] / maxAcf;
        }

        int bestLag = -1;
        double bestValue = 0;
        
        for (int lag = minLag + 1; lag < maxLag - 1; lag++) {
            if (nsdf[lag] > nsdf[lag - 1] && 
                nsdf[lag] > nsdf[lag + 1] && 
                nsdf[lag] > 0.3) {  // Umbral de claridad
                
                if (nsdf[lag] > bestValue) {
                    bestValue = nsdf[lag];
                    bestLag = lag;
                }
            }
        }

        if (bestLag <= 0 || bestValue < 0.3) return 0;

        double refinedLag = parabolicInterpolation(nsdf, bestLag);
        
        return (double) sampleRate / refinedLag;
    }

    private static double parabolicInterpolation(double[] array, int x) {
        if (x < 1 || x >= array.length - 1) return x;
        
        double s0 = array[x - 1];
        double s1 = array[x];
        double s2 = array[x + 1];
        
        double adjustment = 0.5 * (s0 - s2) / (s0 - 2 * s1 + s2);
        return x + adjustment;
    }

    private static String getNoteName(double freq) {
        if (freq <= 0 || freq < 20 || freq > 4200) return "--";
        double n = 12 * (Math.log(freq / 440.0) / Math.log(2));
        int midi = (int) Math.round(n) + 69;
        String[] names = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int noteIndex = Math.floorMod(midi, 12);
        int octave = (midi / 12) - 1;
        return names[noteIndex] + octave;
    }

    private static double getCentsFromA4(double freq) {
        if (freq <= 0 || freq < 20 || freq > 4200) return 0;

        double semitonesFromA4 = 12 * (Math.log(freq / 440.0) / Math.log(2));

        int nearestSemitone = (int) Math.round(semitonesFromA4);

        double cents = (semitonesFromA4 - nearestSemitone) * 100;
        
        return cents;
    }
}


