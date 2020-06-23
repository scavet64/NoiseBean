/**
 * Copyright 2020 Vincent Scavetta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.scavettapps.noisebean.music;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerOptions;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.remote.RemoteNodeRegistry;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.TrackStateListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
public class NoiseBeanAudioManager extends DefaultAudioPlayerManager {

   @Override
   public void setHttpBuilderConfigurator(Consumer<HttpClientBuilder> configurator) {
      super.setHttpBuilderConfigurator(configurator); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void setHttpRequestConfigurator(Function<RequestConfig, RequestConfig> configurator) {
      super.setHttpRequestConfigurator(configurator); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public RemoteNodeRegistry getRemoteNodeRegistry() {
      return super.getRemoteNodeRegistry(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   protected AudioPlayer constructPlayer() {
      return super.constructPlayer(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public AudioPlayer createPlayer() {
      return super.createPlayer(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ExecutorService getExecutor() {
      return super.getExecutor(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void setItemLoaderThreadPoolSize(int poolSize) {
      super.setItemLoaderThreadPoolSize(poolSize); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void setPlayerCleanupThreshold(long cleanupThreshold) {
      super.setPlayerCleanupThreshold(cleanupThreshold); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public long getTrackStuckThresholdNanos() {
      return super.getTrackStuckThresholdNanos(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void setTrackStuckThreshold(long trackStuckThreshold) {
      super.setTrackStuckThreshold(trackStuckThreshold); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void setFrameBufferDuration(int frameBufferDuration) {
      super.setFrameBufferDuration(frameBufferDuration); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getFrameBufferDuration() {
      return super.getFrameBufferDuration(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void setUseSeekGhosting(boolean useSeekGhosting) {
      super.setUseSeekGhosting(useSeekGhosting); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean isUsingSeekGhosting() {
      return super.isUsingSeekGhosting(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public AudioConfiguration getConfiguration() {
      return super.getConfiguration(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void executeTrack(TrackStateListener listener, InternalAudioTrack track, AudioConfiguration configuration, AudioPlayerOptions playerOptions) {
      super.executeTrack(listener, track, configuration, playerOptions); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public AudioTrack decodeTrackDetails(AudioTrackInfo trackInfo, byte[] buffer) {
      return super.decodeTrackDetails(trackInfo, buffer); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public byte[] encodeTrackDetails(AudioTrack track) {
      return super.encodeTrackDetails(track); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public DecodedTrackHolder decodeTrack(MessageInput stream) throws IOException {
      return super.decodeTrack(stream); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void encodeTrack(MessageOutput stream, AudioTrack track) throws IOException {
      super.encodeTrack(stream, track); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Future<Void> loadItemOrdered(Object orderingKey, String identifier, AudioLoadResultHandler resultHandler) {
      return super.loadItemOrdered(orderingKey, identifier, resultHandler); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Future<Void> loadItem(String identifier, AudioLoadResultHandler resultHandler) {
      return super.loadItem(identifier, resultHandler); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public <T extends AudioSourceManager> T source(Class<T> klass) {
      return super.source(klass); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void registerSourceManager(AudioSourceManager sourceManager) {
      super.registerSourceManager(sourceManager); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void enableGcMonitoring() {
      super.enableGcMonitoring(); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void useRemoteNodes(String... nodeAddresses) {
      super.useRemoteNodes(nodeAddresses); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void shutdown() {
      super.shutdown(); //To change body of generated methods, choose Tools | Templates.
   }
   
}
