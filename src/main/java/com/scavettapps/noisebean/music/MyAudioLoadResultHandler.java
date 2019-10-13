//package com.scavettapps.noisebean.music;
//
//import com.scavettapps.noisebean.MessageSender;
//import com.scavettapps.noisebean.MessageUtil;
//import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
//import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
//import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
//import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
//
//import net.dv8tion.jda.api.entities.Guild;
//import net.dv8tion.jda.api.entities.Member;
//
//public final class MyAudioLoadResultHandler implements AudioLoadResultHandler {
//	
//	private static final int PLAYLIST_LIMIT = 200;
//	private static final String CD = "\uD83D\uDCBF";
//	private static final String DVD = "\uD83D\uDCC0";
//	private static final String MIC = "\uD83C\uDFA4 **|>** ";
//	
//	private static final String QUEUE_INFO = "Info about the Queue: (Size - %d)";
//	private static final String QUEUE_TITLE = "__%s has added %d new track%s to the Queue:__";
//	private static final String QUEUE_DESCRIPTION = "%s **|>**  %s\n%s\n%s %s\n%s";
//	private static final String ERROR = "Error while loading \"%s\"";
//	
//	private final MessageSender chat;
//	private final String identifier;
//	private final Guild guild;
//	private final Member author;
//
//	private MyAudioLoadResultHandler(MessageSender chat, String identifier, Guild guild, Member author) {
//		this.chat = chat;
//		this.identifier = identifier;
//		this.guild = guild;
//		this.author = author;
//	}
//
//	@Override
//	public void trackLoaded(AudioTrack track) {
//		chat.sendEmbed(String.format(QUEUE_TITLE, MessageUtil.userDiscrimSet(author.getUser()), 1, ""),
//				String.format(QUEUE_DESCRIPTION, CD, getOrNull(track.getInfo().title), "", MIC,
//						getOrNull(track.getInfo().author), ""));
//		getTrackManager(guild).queue(track, author);
//	}
//
//	@Override
//	public void playlistLoaded(AudioPlaylist playlist) {
//		if (playlist.getSelectedTrack() != null) {
//			trackLoaded(playlist.getSelectedTrack());
//		} else if (playlist.isSearchResult()) {
//			trackLoaded(playlist.getTracks().get(0));
//		} else {
//			chat.sendEmbed(
//					String.format(QUEUE_TITLE, MessageUtil.userDiscrimSet(author.getUser()),
//							Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT), "s"),
//					String.format(QUEUE_DESCRIPTION, DVD, getOrNull(playlist.getName()), "", "", "", ""));
//			for (int i = 0; i < Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT); i++) {
//				getTrackManager(guild).queue(playlist.getTracks().get(i), author);
//			}
//		}
//	}
//
//	@Override
//	public void noMatches() {
//		chat.sendEmbed(String.format(ERROR, identifier), "\u26A0 No playable tracks were found.");
//	}
//
//	@Override
//	public void loadFailed(FriendlyException exception) {
//		chat.sendEmbed(String.format(ERROR, identifier), "\u26D4 " + exception.getLocalizedMessage());
//	}
//	
//	private String getOrNull(String s) {
//		return s.isEmpty() ? "N/A" : s;
//	}
//}
