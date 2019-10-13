/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.commands;

import com.scavettapps.noisebean.Info;
import com.scavettapps.noisebean.music.AudioInfo;
import com.scavettapps.noisebean.music.AudioPlayerSendHandler;
import com.scavettapps.noisebean.music.TrackManager;
import com.scavettapps.noisebean.MessageSender;
import com.scavettapps.noisebean.MessageUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;

import java.util.*;

import org.apache.commons.io.FileUtils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Command(name = "music")
public class MusicCommand extends AbstractCommand {

	private static final int PLAYLIST_LIMIT = 200;
	private static final AudioPlayerManager myManager = new DefaultAudioPlayerManager();
	private static final Map<String, Map.Entry<AudioPlayer, TrackManager>> players = new HashMap<>();

	private static final String CD = "\uD83D\uDCBF";
	private static final String DVD = "\uD83D\uDCC0";
	private static final String MIC = "\uD83C\uDFA4 **|>** ";
	
	private static final String QUEUE_INFO = "Info about the Queue: (Size - %d)";
	private static final String QUEUE_TITLE = "__%s has added %d new track%s to the Queue:__";
	private static final String QUEUE_DESCRIPTION = "%s **|>**  %s\n%s\n%s %s\n%s";
	private static final String ERROR = "Error while loading \"%s\"";

	public MusicCommand() {
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
			switch (args[0].toLowerCase()) {
			case "help":
			case "commands":
				sendHelpMessage(chat);
				break;

			case "now":
			case "current":
			case "nowplaying":
			case "info": // Display song info
				if (!hasPlayer(guild) || getPlayer(guild).getPlayingTrack() == null) { // No song is playing
					chat.sendMessage("No song is being played at the moment! *It's your time to shine..*");
				} else {
					AudioTrack track = getPlayer(guild).getPlayingTrack();
					chat.sendEmbed("Track Info", String.format(QUEUE_DESCRIPTION, CD, getOrNull(track.getInfo().title),
							"\n\u23F1 **|>** `[ " + getTimestamp(track.getPosition()) + " / "
									+ getTimestamp(track.getInfo().length) + " ]`",
							"\n" + MIC, getOrNull(track.getInfo().author), "\n\uD83C\uDFA7 **|>**  " + MessageUtil
									.userDiscrimSet(getTrackManager(guild).getTrackInfo(track).getAuthor().getUser())));
				}
				break;

			case "queue":
				if (!hasPlayer(guild) || getTrackManager(guild).getQueuedTracks().isEmpty()) {
					chat.sendMessage("The queue is empty! Load a song with **"
							+ MessageUtil.stripFormatting(Info.PREFIX) + "music play**!");
				} else {
					StringBuilder sb = new StringBuilder();
					Set<AudioInfo> queue = getTrackManager(guild).getQueuedTracks();
					queue.forEach(audioInfo -> sb.append(buildQueueMessage(audioInfo)));
					String embedTitle = String.format(QUEUE_INFO, queue.size());

					if (sb.length() <= 1960) {
						chat.sendEmbed(embedTitle, "**>** " + sb.toString());

					} else {
						e.getChannel().sendTyping().queue();
						File qFile = new File("queue.txt");
						try {
							FileUtils.write(qFile, sb.toString(), "UTF-8", false);
							e.getChannel().sendFile(qFile, qFile.getName(), null).queue();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						if (!qFile.delete()) { // Delete the queue file after we're done
							qFile.deleteOnExit();
						}
					}

				}
				break;

			case "skip":
				if (isIdle(chat, guild)) {
					return;
				}

				if (isCurrentDj(e.getMember())) {
					forceSkipTrack(guild, chat);
				} else {
					AudioInfo info = getTrackManager(guild).getTrackInfo(getPlayer(guild).getPlayingTrack());
					if (info.hasVoted(e.getAuthor())) {
						chat.sendMessage("\u26A0 You've already voted to skip this song!");
					} else {
						int votes = info.getSkips();
						if (votes >= 3) { // Skip on 4th vote
							getPlayer(guild).stopTrack();
							chat.sendMessage("\u23E9 Skipping current track.");
						} else {
							info.addSkip(e.getAuthor());
							tryToDelete(e.getMessage());
							chat.sendMessage("**" + MessageUtil.userDiscrimSet(e.getAuthor())
									+ "** has voted to skip this track! [" + (votes + 1) + "/4]");
						}
					}
				}
				break;

			case "forceskip":
				if (isIdle(chat, guild)) {
					return;
				}

				if (isCurrentDj(e.getMember()) || isDj(e.getMember())) {
					forceSkipTrack(guild, chat);
				} else {
					chat.sendMessage("You don't have permission to do that!\n" + "Use **"
							+ MessageUtil.stripFormatting(Info.PREFIX) + "music skip** to cast a vote!");
				}
				break;

			case "reset":
				if (!isDj(e.getMember())) {
					chat.sendMessage("You don't have the required permissions to do that! [DJ role]");
				} else {
					reset(guild);
					chat.sendMessage("\uD83D\uDD04 Resetting the music player..");
				}
				break;

			case "shuffle":
				if (isIdle(chat, guild)) {
					return;
				}

				if (isDj(e.getMember())) {
					getTrackManager(guild).shuffleQueue();
					chat.sendMessage("\u2705 Shuffled the queue!");
				} else {
					chat.sendMessage("\u26D4 You don't have the permission to do that!");
				}
				break;
			}

		default:
			String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			switch (args[0].toLowerCase()) {
			case "ytplay": // Query YouTube for a music video
				input = "ytsearch: " + input;
				// no break;

			case "play": // Play a track
				if (args.length <= 1) {
					chat.sendMessage("Please include a valid source.");
				} else {
					loadTrack(input, e.getMember(), e.getMessage(), chat);
				}
				break;
			}
			break;
		}
	}

	@Override
	public List<String> getAlias() {
		return Collections.singletonList("music");
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		if (!players.containsKey(event.getGuild().getId())) {
			return; // Guild doesn't have a music player
		}
		TrackManager manager = getTrackManager(event.getGuild());
		manager.getQueuedTracks().stream()
				.filter(info -> !info.getTrack().equals(getPlayer(event.getGuild()).getPlayingTrack())
						&& info.getAuthor().getUser().equals(event.getMember().getUser()))
				.forEach(manager::remove);
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		reset(event.getGuild());
	}

	private void tryToDelete(Message m) {
		if (m.getGuild().getSelfMember().hasPermission(m.getTextChannel(), Permission.MESSAGE_MANAGE)) {
			m.delete().queue();
		}
	}

	private boolean hasPlayer(Guild guild) {
		return players.containsKey(guild.getId());
	}

	private AudioPlayer getPlayer(Guild guild) {
		AudioPlayer p;
		if (hasPlayer(guild)) {
			p = players.get(guild.getId()).getKey();
		} else {
			p = createPlayer(guild);
		}
		return p;
	}

	private TrackManager getTrackManager(Guild guild) {
		return players.get(guild.getId()).getValue();
	}

	private AudioPlayer createPlayer(Guild guild) {
		AudioPlayer nPlayer = myManager.createPlayer();
		TrackManager manager = new TrackManager(nPlayer);
		nPlayer.addListener(manager);
		guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(nPlayer));
		players.put(guild.getId(), new AbstractMap.SimpleEntry<>(nPlayer, manager));
		return nPlayer;
	}

	private void reset(Guild guild) {
		players.remove(guild.getId());
		getPlayer(guild).destroy();
		getTrackManager(guild).purgeQueue();
		guild.getAudioManager().closeAudioConnection();
	}

	private void loadTrack(String identifier, Member author, Message msg, MessageSender chat) {
		if (author.getVoiceState().getChannel() == null) {
			chat.sendMessage("You are not in a Voice Channel!");
			return;
		}

		Guild guild = author.getGuild();
		getPlayer(guild); // Make sure this guild has a player.

		msg.getTextChannel().sendTyping().queue();
		myManager.loadItemOrdered(guild, identifier, new Class(chat, identifier, guild, author));
		tryToDelete(msg);
	}

	private boolean isDj(Member member) {
		return member.getRoles().stream().anyMatch(r -> r.getName().equals("DJ"));
	}

	private boolean isCurrentDj(Member member) {
		return getTrackManager(member.getGuild()).getTrackInfo(getPlayer(member.getGuild()).getPlayingTrack())
				.getAuthor().equals(member);
	}

	private boolean isIdle(MessageSender chat, Guild guild) {
		if (!hasPlayer(guild) || getPlayer(guild).getPlayingTrack() == null) {
			chat.sendMessage("No music is being played at the moment!");
			return true;
		}
		return false;
	}

	private void forceSkipTrack(Guild guild, MessageSender chat) {
		getPlayer(guild).stopTrack();
		chat.sendMessage("\u23E9 Skipping track!");
	}

	private void sendHelpMessage(MessageSender chat) {
		chat.sendEmbed("DJNoiseBeans",
			MessageUtil.stripFormatting(Info.PREFIX) 
				+ "music\n"
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
	
	private final class Class implements AudioLoadResultHandler {
		private final MessageSender chat;
		private final String identifier;
		private final Guild guild;
		private final Member author;

		private Class(MessageSender chat, String identifier, Guild guild, Member author) {
			this.chat = chat;
			this.identifier = identifier;
			this.guild = guild;
			this.author = author;
		}

		@Override
		public void trackLoaded(AudioTrack track) {
			chat.sendEmbed(String.format(QUEUE_TITLE, MessageUtil.userDiscrimSet(author.getUser()), 1, ""),
					String.format(QUEUE_DESCRIPTION, CD, getOrNull(track.getInfo().title), "", MIC,
							getOrNull(track.getInfo().author), ""));
			getTrackManager(guild).queue(track, author);
		}

		@Override
		public void playlistLoaded(AudioPlaylist playlist) {
			if (playlist.getSelectedTrack() != null) {
				trackLoaded(playlist.getSelectedTrack());
			} else if (playlist.isSearchResult()) {
				trackLoaded(playlist.getTracks().get(0));
			} else {
				chat.sendEmbed(
						String.format(QUEUE_TITLE, MessageUtil.userDiscrimSet(author.getUser()),
								Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT), "s"),
						String.format(QUEUE_DESCRIPTION, DVD, getOrNull(playlist.getName()), "", "", "", ""));
				for (int i = 0; i < Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT); i++) {
					getTrackManager(guild).queue(playlist.getTracks().get(i), author);
				}
			}
		}

		@Override
		public void noMatches() {
			chat.sendEmbed(String.format(ERROR, identifier), "\u26A0 No playable tracks were found.");
		}

		@Override
		public void loadFailed(FriendlyException exception) {
			chat.sendEmbed(String.format(ERROR, identifier), "\u26D4 " + exception.getLocalizedMessage());
		}
	}
}
