/*
 * LifeCompanion AAC and its sub projects
 *
 * Copyright (C) 2014 to 2019 Mathieu THEBAUD
 * Copyright (C) 2020 to 2023 CMRRF KERPAPE (Lorient, France) and CoWork'HIT (Lorient, France)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lifecompanion.plugin.caaai.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import javafx.beans.property.*;
import org.lifecompanion.util.IOUtils;
import org.lifecompanion.util.javafx.FXThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpeechToTextService {
    private static final int SILENCE_DETECTION_INTERVAL = 4; // every 4 requests
    private static final long SILENCE_DURATION = 1_000; // 5 second of continuous silence to stop
    private static final long SILENCE_DETECTION_OFFSET = 2_000; // 1 second before detecting silences
    private static final double SILENCE_THRESHOLD = 127.0 * 0.15; // 10% of max volume
    private static final long MAX_DURATION = 2 * 60 * 1_000; // speech detection can last 2 minutes
    private static final boolean DEBUG_AUDIO_FILE = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeechToTextService.class);

    private final static AudioFormat[] AUDIO_FORMATS = {
            new AudioFormat(32000, 16, 1, true, false),
            new AudioFormat(16000, 16, 1, true, false)};

    private Consumer<String> onSentenceDetected;
    private Consumer<List<String>> onSpeechFinished;

    private final BooleanProperty recording;
    private final String jsonCredentialsContent;
    private SpeechToTextThread currentSpeechToTextThread;

    private final DoubleProperty currentRecordedVolume;

    public SpeechToTextService(String jsonCredentialsContent) {
        this.recording = new SimpleBooleanProperty();
        this.currentRecordedVolume = new SimpleDoubleProperty();
        this.jsonCredentialsContent = jsonCredentialsContent;
    }

    public ReadOnlyBooleanProperty recordingProperty() {
        return this.recording;
    }

    public ReadOnlyDoubleProperty currentRecordedVolumeProperty() {
        return currentRecordedVolume;
    }

    public void dispose() {
        if (recording.get()) {
            stopRecording();
        }
        this.onSpeechFinished = null;
        this.onSentenceDetected = null;
    }

    public void stopRecording() {
        if (this.currentSpeechToTextThread != null) {
            recording.set(false);
            currentSpeechToTextThread.stopRecording();
            updateCurrentRecordedVolume(0.0);
            this.currentSpeechToTextThread = null;
        }
    }

    public void startRecording(Consumer<String> onSentenceDetected, Consumer<List<String>> onSpeechFinished) {
        if (recording.get()) {
            throw new IllegalStateException("A recording is already active");
        }
        this.recording.set(true);
        this.onSentenceDetected = onSentenceDetected;
        this.onSpeechFinished = onSpeechFinished;
        this.currentSpeechToTextThread = new SpeechToTextThread();
        this.currentSpeechToTextThread.start();
    }

    private void updateCurrentRecordedVolume(double vol) {
        FXThreadUtils.runOnFXThread(() -> currentRecordedVolume.set(Math.min(1.0, Math.max(0.0, vol * 1.2))));
    }

    public class SpeechToTextThread extends Thread implements ResponseObserver<StreamingRecognizeResponse> {
        private TargetDataLine line;
        private AudioFormat format;
        private final List<String> accumulatedSentences;
        private long recordingStarted;

        public SpeechToTextThread() {
            super("CAA-AI-SpeechToTextThread");
            accumulatedSentences = new ArrayList<>();
        }

        public void stopRecording() {
            line.stop();
            line.close();
        }

        @Override
        public void run() {
            try {
                // Init the line
                DataLine.Info info = createBestDatalineFormat();

                LOGGER.info("Selected format : {} - {}", format, info);
                line = (TargetDataLine) AudioSystem.getLine(info);

                // Open and start
                line.open(format);
                line.start();
                recordingStarted = System.currentTimeMillis();
                AudioInputStream audioInputStream = new AudioInputStream(line);

                try (SpeechClient client = SpeechClient.create(getSettings())) {
                    ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable().splitCall(this);
                    // Init request
                    RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setLanguageCode("fr-FR")
                            .setModel("latest_long")
                            .setSampleRateHertz((int) format.getSampleRate())
                            // .setEnableAutomaticPunctuation(true) not working...
                            .build();
                    clientStream.send(StreamingRecognizeRequest.newBuilder()
                            .setStreamingConfig(StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build())
                            .build());

                    // Stream line to request
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    long lastPeak = System.currentTimeMillis();
                    long i = 0;
                    while (recording.get() && System.currentTimeMillis() - recordingStarted < MAX_DURATION) {
                        // Read buffer
                        byte[] buff = new byte[4096];
                        int read = audioInputStream.read(buff);
                        if (DEBUG_AUDIO_FILE) {
                            baos.write(buff, 0, read);
                        }

                        // Detect silence to stop if needed
                        if ((i++) % SILENCE_DETECTION_INTERVAL == 0) {
                            lastPeak = detectSilence(buff, lastPeak);
                        }

                        // Send request
                        clientStream.send(StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(ByteString.copyFrom(buff))
                                .build());
                    }
                    updateCurrentRecordedVolume(0);
                    LOGGER.info("Recording finished");
                    clientStream.closeSend();
                    this.onComplete();
                    if (DEBUG_AUDIO_FILE) {
                        File debugFile = IOUtils.getTempFile("speech-to-text-wav", ".wav");
                        LOGGER.info("Audio debug file will be saved to {}", debugFile);
                        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(baos.toByteArray()), format, baos.size()), AudioFileFormat.Type.WAVE, debugFile);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Problem when running speech to text service", e);
            }
        }

        private long detectSilence(byte[] buff, long lastPeak) {
            double rms = computeRmsAndUpdateVolume(buff);
            if (rms < SILENCE_THRESHOLD) {
                if (System.currentTimeMillis() - recordingStarted > SILENCE_DETECTION_OFFSET && System.currentTimeMillis() - lastPeak > SILENCE_DURATION) {
                    LOGGER.info("Silence detected, will stop recording");
                    SpeechToTextService.this.stopRecording();
                }
            } else {
                lastPeak = System.currentTimeMillis();
            }
            return lastPeak;
        }

        private DataLine.Info createBestDatalineFormat() {
            DataLine.Info info = null;
            for (AudioFormat formatToTest : AUDIO_FORMATS) {
                info = new DataLine.Info(TargetDataLine.class, formatToTest);
                if (AudioSystem.isLineSupported(info)) {
                    format = formatToTest;
                    break;
                }
            }
            return info;
        }

        private SpeechSettings getSettings() throws IOException {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(jsonCredentialsContent.getBytes(StandardCharsets.UTF_8)));
            return SpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build();
        }

        private double computeRmsAndUpdateVolume(byte[] buff) {
            double sum = 0d;
            for (byte value : buff) {
                sum += value;
            }
            double average = sum / buff.length;
            double sumMeanSquare = 0d;
            for (byte b : buff) {
                sumMeanSquare += Math.pow(b - average, 2d);
            }
            double rms = Math.sqrt(sumMeanSquare / buff.length);
            updateCurrentRecordedVolume(rms / 127.0);
            return rms;
        }

        @Override
        public void onStart(StreamController streamController) {

        }

        @Override
        public void onResponse(StreamingRecognizeResponse response) {
            for (StreamingRecognitionResult streamingRecognitionResult : response.getResultsList()) {
                for (SpeechRecognitionAlternative speechRecognitionAlternative : streamingRecognitionResult.getAlternativesList()) {
                    String sentence = speechRecognitionAlternative.getTranscript();
                    accumulatedSentences.add(sentence);
                    if (onSentenceDetected != null) onSentenceDetected.accept(sentence);
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            LOGGER.error("Error while using speech to text", throwable);
        }

        @Override
        public void onComplete() {
            if (onSpeechFinished != null) {
                onSpeechFinished.accept(accumulatedSentences);
            }
        }
    }
}
