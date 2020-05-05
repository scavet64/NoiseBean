/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.commands;

import com.scavettapps.noisebean.music.AudioInfo;
import com.scavettapps.noisebean.music.AudioPlayerSendHandler;
import com.scavettapps.noisebean.music.TrackManager;
import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;
import static com.scavettapps.noisebean.core.Unicode.CD;
import static com.scavettapps.noisebean.core.Unicode.HEADPHONE;
import static com.scavettapps.noisebean.core.Unicode.MIC;
import static com.scavettapps.noisebean.core.Unicode.NO_ENTRY;
import static com.scavettapps.noisebean.core.Unicode.POINTRIGHT;
import static com.scavettapps.noisebean.core.Unicode.RESET;
import static com.scavettapps.noisebean.core.Unicode.STOPWATCH;
import static com.scavettapps.noisebean.core.Unicode.WARNING_SIGN;
import static com.scavettapps.noisebean.core.Unicode.WHITE_HEAVY_CHECKMARK;
import static com.scavettapps.noisebean.music.AudioConstants.QUEUE_DESCRIPTION;
import static com.scavettapps.noisebean.music.AudioConstants.QUEUE_INFO;
import com.scavettapps.noisebean.music.ChatBasedAudioLoadResultHandlerImpl;
import com.scavettapps.noisebean.music.NoiseBeanAudioManager;
import com.scavettapps.noisebean.music.NoiseBeanAudioService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.*;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@Command(name = "music")
public class MusicCommand extends AbstractCommand {

   private final NoiseBeanAudioManager myManager;
   private final NoiseBeanAudioService noiseBeanAudioService;

   @Autowired
   public MusicCommand(
       NoiseBeanAudioManager myManager,
       NoiseBeanAudioService noiseBeanAudioService
   ) {
      this.myManager = myManager;
      this.noiseBeanAudioService = noiseBeanAudioService;
      AudioSourceManagers.registerRemoteSources(myManager);
   }

   @Override
   public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
      Guild guild = e.getGuild();
      switch (args.length) {
         case 0: // Show help message
            sendHelpMessage(chat);
            break;
         case 1:
            oneArgumentSubcommands(args, e, chat, guild);
            break;
         case 2:
            twoArgumentSubcommands(args, e, chat, guild);
            break;
         default:
            break;
      }
   }

   private void oneArgumentSubcommands(String[] args, MessageReceivedEvent e, MessageSender chat, Guild guild) {
      String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
      switch (args[0].toLowerCase()) {
         case "help":
         case "commands":
            sendHelpMessage(chat);
            break;

         case "now":
         case "current":
         case "nowplaying":
         case "info": // Display song info
            infoSubcommand(chat, guild);
            break;

         case "queue":
            queueSubcommand(e, chat, guild);
            break;

         case "skip":
            if (isIdle(chat, guild)) {
               return;
            }
            skipSubcommand(e, chat, guild);
            break;

         case "forceskip":
            if (isIdle(chat, guild)) {
               return;
            }
            forceSkipSubcommand(e, chat, guild);
            break;

         case "reset":
            resetSubcommand(e, chat, guild);
            break;

         case "shuffle":
            if (isIdle(chat, guild)) {
               return;
            }
            shuffleSubcommand(e, chat, guild);
            break;
         case "loop":
            loopSubcommand(e, chat, guild);
            break;

         case "ytplay": // Query YouTube for a music video
            input = "ytsearch: " + input;

         case "play": // Play a track
            if (args.length <= 1) {
               chat.sendMessage("Please include a valid source.");
            } else {
               loadTrack(input, e.getMember(), e.getMessage(), chat);
            }
            break;
         default:
            break;
      }
   }

   private void twoArgumentSubcommands(String[] args, MessageReceivedEvent e, MessageSender chat, Guild guild) {
      String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
      switch (args[0].toLowerCase()) {
         case "play": // Play a track
            loadTrack(input, e.getMember(), e.getMessage(), chat);
            break;
         default:
            break;
      }
   }

   private void loopSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      TrackManager manager = this.noiseBeanAudioService.getTrackManager(guild);
      if (manager == null) {
         chat.sendMessage(WARNING_SIGN + " Nothing to loop!");
         return;
      }

      boolean toggleState = manager.toggleLoop();
      if (toggleState) {
         chat.sendMessage(WHITE_HEAVY_CHECKMARK + " Loop has been enabled!");
      } else {
         chat.sendMessage(WHITE_HEAVY_CHECKMARK + " Loop has been disabled!");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void shuffleSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (isDj(e.getMember())) {
         this.noiseBeanAudioService.getTrackManager(guild).shuffleQueue();
         chat.sendMessage(WHITE_HEAVY_CHECKMARK + " Shuffled the queue!");
      } else {
         chat.sendMessage(NO_ENTRY + " You don't have the permission to do that!");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void resetSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (!isDj(e.getMember())) {
         chat.sendMessage("You don't have the required permissions to do that! [DJ role]");
      } else {
         this.noiseBeanAudioService.reset(guild);
         chat.sendMessage(RESET + " Resetting the music player..");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void forceSkipSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (isCurrentDj(e.getMember()) || isDj(e.getMember())) {
         forceSkipTrack(guild, chat);
      } else {
         chat.sendMessage("You don't have permission to do that!\n" + "Use **"
             + MessageUtil.stripFormatting(this.prefix) + "music skip** to cast a vote!");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void skipSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (isCurrentDj(e.getMember())) {
         forceSkipTrack(guild, chat);
      } else {
         AudioInfo info = this.noiseBeanAudioService.getTrackManager(guild).getTrackInfo(
             this.noiseBeanAudioService.getPlayer(guild).getPlayingTrack()
         );
         if (info.hasVoted(e.getAuthor())) {
            chat.sendMessage(WARNING_SIGN + " You've already voted to skip this song!");
         } else {
            int votes = info.getSkips();
            if (votes >= 3) { // Skip on 4th vote
               this.noiseBeanAudioService.getPlayer(guild).stopTrack();
               chat.sendMessage(POINTRIGHT + " Skipping current track.");
            } else {
               info.addSkip(e.getAuthor());
               tryToDelete(e.getMessage());
               chat.sendMessage("**" + MessageUtil.userDiscrimSet(e.getAuthor())
                   + "** has voted to skip this track! [" + (votes + 1) + "/4]");
            }
         }
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void queueSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (!this.noiseBeanAudioService.hasPlayer(guild) || this.noiseBeanAudioService.getTrackManager(guild).getQueuedTracks().isEmpty()) {
         chat.sendMessage("The queue is empty! Load a song with **" + MessageUtil.stripFormatting(this.prefix)
             + "music play**!");
      } else {
         StringBuilder sb = new StringBuilder();
         Set<AudioInfo> queue = this.noiseBeanAudioService.getTrackManager(guild).getQueuedTracks();
         queue.forEach(audioInfo -> sb.append(buildQueueMessage(audioInfo)));
         String embedTitle = String.format(QUEUE_INFO, queue.size());
         chat.sendEmbed(embedTitle, "**>** " + sb.toString());
      }
   }

   /**
    * @param chat
    * @param guild
    */
   private void infoSubcommand(MessageSender chat, Guild guild) {
      if (!this.noiseBeanAudioService.hasPlayer(guild) || this.noiseBeanAudioService.getPlayer(guild).getPlayingTrack() == null) { // No song is playing
         chat.sendMessage("No song is being played at the moment! *It's your time to shine..*");
      } else {
         AudioTrack track = this.noiseBeanAudioService.getPlayer(guild).getPlayingTrack();
         StringBuilder trackInfoBuilder = new StringBuilder();

         String trackInfo = trackInfoBuilder
             .append("\n")
             .append(STOPWATCH)
             .append(" **|>** `[ ")
             .append(getTimestamp(track.getPosition()))
             .append(" / ")
             .append(getTimestamp(track.getInfo().length)).append(" ]`").toString();

         StringBuilder authorInfoBuilder = new StringBuilder();
         String authorInfo = authorInfoBuilder
             .append("\n")
             .append(MIC)
             .append(getOrNull(track.getInfo().author))
             .toString();

         StringBuilder requestingUserBuilder = new StringBuilder();
         String reqUser = requestingUserBuilder
             .append("\n")
             .append(HEADPHONE)
             .append(" **|>**  ")
             .append(MessageUtil.userDiscrimSet(
                 this.noiseBeanAudioService.getTrackManager(guild).getTrackInfo(track).getAuthor().getUser()))
             .toString();

         chat.sendEmbed("Track Info", String.format(
             QUEUE_DESCRIPTION,
             CD,
             getOrNull(track.getInfo().title),
             trackInfo,
             authorInfo,
             reqUser));
      }
   }

   @Override
   public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
      AudioPlayer player = this.noiseBeanAudioService.getPlayer(event.getGuild());

      if (player == null) {
         return; // Guild doesn't have a music player
      }
      TrackManager manager = this.noiseBeanAudioService.getTrackManager(event.getGuild());
      manager.getQueuedTracks().stream()
          .filter(info -> !info.getTrack().equals(this.noiseBeanAudioService.getPlayer(event.getGuild()).getPlayingTrack())
          && info.getAuthor().getUser().equals(event.getMember().getUser()))
          .forEach(manager::remove);
   }

   @Override
   public void onGuildLeave(GuildLeaveEvent event) {
      this.noiseBeanAudioService.reset(event.getGuild());
   }

   private void tryToDelete(Message m) {
      if (m.getGuild().getSelfMember().hasPermission(m.getTextChannel(), Permission.MESSAGE_MANAGE)) {
         m.delete().queue();
      }
   }

   private void loadTrack(String identifier, Member author, Message msg, MessageSender chat) {
      if (author.getVoiceState().getChannel() == null) {
         chat.sendMessage("You are not in a Voice Channel!");
         return;
      }

      Guild guild = author.getGuild();
      this.noiseBeanAudioService.getPlayer(guild); // Make sure this guild has a player.

      msg.getTextChannel().sendTyping().queue();
      myManager.loadItemOrdered(guild, 
          identifier, 
          new ChatBasedAudioLoadResultHandlerImpl(chat, identifier, guild, author, noiseBeanAudioService.getTrackManager(guild))
      );
      tryToDelete(msg);
   }

   private boolean isDj(Member member) {
      return member.getRoles().stream().anyMatch(r -> r.getName().equals("DJ"));
   }

   private boolean isCurrentDj(Member member) {
      return this.noiseBeanAudioService.getTrackManager(member.getGuild()).getTrackInfo(this.noiseBeanAudioService.getPlayer(member.getGuild()).getPlayingTrack())
          .getAuthor().equals(member);
   }

   private boolean isIdle(MessageSender chat, Guild guild) {
      if (!this.noiseBeanAudioService.hasPlayer(guild) || this.noiseBeanAudioService.getPlayer(guild).getPlayingTrack() == null) {
         chat.sendMessage("No music is being played at the moment!");
         return true;
      }
      return false;
   }

   private void forceSkipTrack(Guild guild, MessageSender chat) {
      this.noiseBeanAudioService.getPlayer(guild).stopTrack();
      chat.sendMessage(POINTRIGHT + " Skipping track!");
   }

   private void sendHelpMessage(MessageSender chat) {
      chat.sendEmbed("DJNoiseBeans",
          MessageUtil.stripFormatting(this.prefix) + "music\n"
          + "         -> play [url]           - Load a song or a playlist\n"
          + "         -> ytplay [query]  - Search YouTube for a video and load it\n"
          + "         -> queue                 - View the current queue\n"
          + "         -> skip                     - Cast a vote to skip the current track\n"
          + "         -> current               - Display info related to the current track\n"
          + "         -> forceskip**\\***          - Force a skip\n"
          + "         -> shuffle**\\***              - Shuffle the queue\n"
          + "         -> reset**\\***                 - Reset the music player\n\n"
          + "Commands with an asterisk**\\*** require the __DJ Role__");
   }

   private String buildQueueMessage(AudioInfo info) {
      AudioTrackInfo trackInfo = info.getTrack().getInfo();
      String title = trackInfo.title;
      long length = trackInfo.length;
      return "`[ " + getTimestamp(length) + " ]` " + title + "\n";
   }

   private String getTimestamp(long milis) {
      long seconds = milis / 1000;
      long hours = Math.floorDiv(seconds, 3600);
      seconds = seconds - (hours * 3600);
      long mins = Math.floorDiv(seconds, 60);
      seconds = seconds - (mins * 60);
      return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
   }

   private String getOrNull(String s) {
      return s.isEmpty() ? "N/A" : s;
   }
}
