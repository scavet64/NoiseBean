/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting.games;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.scavettapps.noisebean.commands.AbstractCommand;
import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;
import com.scavettapps.noisebean.voting.Vote;
import com.scavettapps.noisebean.voting.VoteOptionDoesNotExistException;
import com.scavettapps.noisebean.voting.VoteSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Command(name = "gamevote", description = "Vote for a game that we wont play anyway")
@Slf4j
public class GameVoteCommand extends AbstractCommand {

   private final GameVoteService gameVoteService;

   private Map<Long, VoteSession> guildToVoteSession = new HashMap<>();
   
   @Autowired
   public GameVoteCommand(
       GameVoteService gameVoteService
   ) {
      this.gameVoteService = gameVoteService;
   }

   @Override
   public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length == 0) {
         sendHelpMessage(chat);
         return;
      }
      
      switch (args[0]) {
         case "start":
            startVote(args, event, chat);
            break;
         case "add":
            addOption(args, event, chat);
            break;
         case "remove":
            removeOption(args, event, chat);
            break;
         case "list":
            getOptionList(args, event, chat);
            break;
         default:
            break;
      }
   }

   @Override
   public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
      super.onMessageReactionRemove(event);
      if (!this.guildToVoteSession.containsKey(event.getGuild().getIdLong())) {
         // There is no voting session. Ignore reaction.
         return;
      }
      
      // remove the user from the list
      VoteSession vote = this.guildToVoteSession.get(event.getGuild().getIdLong());
      vote.getVoters().remove(event.getMember());
      log.info("Removed [{}] from the vote", event.getMember().getEffectiveName());
   }

   @Override
   public void onMessageReceived(MessageReceivedEvent e) {
      // Only process this if we have a vote going on, otherwise send to super class and move on
      if (!this.isVoteResponse(e)) {
         super.onMessageReceived(e);
         return;
      }
      
      // Is this a valid vote
      String voteMessage = e.getMessage().getContentRaw();
      try {
         int vote = Integer.parseInt(voteMessage);
         if(vote < 0 || vote > 3) {
            throw new NumberFormatException();
         }

         //Record the users vote
         VoteSession session = this.getVotingSession(e);
         boolean completedVotingOnOption = session.addVote(
             Vote.builder()
                 .userId(e.getAuthor().getIdLong())
                 .voteValue(vote)
                 .build()
         );
         
         if (completedVotingOnOption) {
            // Is the vote over?
            if (session.isElectionOver()) {
               sendElectionResults(session);
            } else {
               sendBallots(session);
            }
            
         }
         
      } catch (NumberFormatException nfe) {
         new MessageSender(e).sendMessage("Invalid Vote. Please type 0, 1, 2, or 3");
      }
   }
   
   @Override
   public void onMessageReactionAdd(MessageReactionAddEvent event) {
      super.onMessageReactionAdd(event); //To change body of generated methods, choose Tools | Templates.
      if (!this.guildToVoteSession.containsKey(event.getGuild().getIdLong())) {
         // There is no voting session. Ignore reaction.
         return;
      }
      
      // Get the person who reacted and add them to the voting session
      VoteSession vote = this.guildToVoteSession.get(event.getGuild().getIdLong());
      vote.getVoters().add(event.getMember());
      log.info("Added [{}] to the vote", event.getMember().getEffectiveName());
      
      new MessageSender(event).sendPrivateMessageToUser(
          "I will message you when the vote starts.", 
          event.getUser()
      );

      if (vote.getVoters().size() == vote.getNumberOfVotersForSession() && !vote.isRunning()) {
         // Start the first vote
         sendBallots(vote);
      }
   }
   
   private void sendHelpMessage(MessageSender chat) {
      chat.sendEmbed("NoiseBean Elections",
          MessageUtil.stripFormatting(this.prefix) + "gamevote\n"
          + "         -> start [number] - Start a vote with this number of players \n"
          + "         -> add [name]  - Add a new game to vote on\n"
          + "         -> remove [name]  - Remove an option to vote on\n");
   }
   
   private void startVote(String[] args, MessageReceivedEvent event, MessageSender chat) {
      
      if (this.guildToVoteSession.containsKey(event.getGuild().getIdLong())) {
         // Already a voting session
         chat.sendMessage("Already doing a vote!");
         return;
      }
      
      if (args.length < 2) {
         chat.sendMessage("Please supply a number of voters :^)");
         return;
      }
      
      int numberOfVoters;
      try {
         numberOfVoters = Integer.parseInt(args[1]);
      } catch (NumberFormatException nfe) {
         chat.sendMessage("Invalid number of voters.");
         return;
      }
      
      List<GameVoteOption> options = this.gameVoteService.getGameVoteOptions();
      if(options.isEmpty()) {
         chat.sendMessage("There are no options to vote on");
         return;
      }
      
      Message message = chat.sendMessage("React to this message to participate in the vote");
      VoteSession voteSession = VoteSession.builder()
          .initialMessage(message)
          .numberOfVotersForSession(numberOfVoters)
          .options(options)
          .build();
      guildToVoteSession.put(event.getGuild().getIdLong(), voteSession);
   }

   private void sendElectionResults(VoteSession session) {
      session.setRunning(false);
      
      MessageSender sender = new MessageSender(null);
      
      Message initialMessage = session.getInitialMessage();
      
      List<GameVoteOption> winners = session.getResults();
      
      StringBuilder builder = new StringBuilder();     
      for (int i = 0; i < winners.size(); i++) {
         builder.append("\t");
         builder.append(i + 1);
         builder.append(". ");
         builder.append(winners.get(i).getGameVoteName());
         builder.append("\n");
      }
      
      sender.sendEmbed(
          "Play These Games :^)" ,
          builder.toString(), 
          session.getInitialMessage().getChannel()
      );

      this.guildToVoteSession.remove(initialMessage.getGuild().getIdLong());
   }
   
   private void sendBallots(VoteSession session) {
      session.setRunning(true);
      
      MessageSender sender = new MessageSender(null);
      
      // Get the current game to vote on.
      GameVoteOption currentVote = session.getCurrentOption();
      
      // Send a DM to everyone so they can vote on the option.
      StringBuilder b = new StringBuilder();
      b.append("Vote for [");
      b.append(currentVote.getGameVoteName());
      b.append("] (0-3)");
      for (Member voter : session.getVoters()) {
         sender.sendPrivateMessageToUser(b.toString(), voter.getUser());
      }
   }
      
   private VoteSession getVotingSession(MessageReceivedEvent e) {
      if (e.isFromGuild()) {
         return this.getVotingSession(e.getGuild());
      } else {
         return getVotingSession(e.getAuthor());
      }
   }
   
   private VoteSession getVotingSession(Guild guild) {
      return this.guildToVoteSession.get(guild.getIdLong());
   }
   
   private VoteSession getVotingSession(User user) {
      for(Long guildId: this.guildToVoteSession.keySet()){
         VoteSession session = this.guildToVoteSession.get(guildId);
         if (session.hasVoter(user.getIdLong())){
            return session;
         }
      }
      return null;
   }
   
   private boolean isVoteResponse(MessageReceivedEvent event) {
      // Is this a DM
      if (event.isFromGuild()) {
         VoteSession session = this.guildToVoteSession.get(event.getGuild().getIdLong());
         return (session != null && isPartOfVote(session, event.getMember()));
      } else {
         return isPartOfVote(event.getAuthor());
      }
   }
   
   private boolean isPartOfVote(User id) {
      return getVotingSession(id) != null;
   }
   
   private boolean isPartOfVote(VoteSession session, Member member) {
      return session.getVoters().contains(member);
   }
   
   private void addOption(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length < 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      // Combine remaining text into a single string
      String[] remainingText = Arrays.copyOfRange(args, 1, args.length);
      String optionName = String.join(" ", remainingText);
      
      this.gameVoteService.saveGameVoteOption(optionName);
      chat.sendMessage("Added [" + optionName + "] as an option");
   }
   
   private void getOptionList(String[] args, MessageReceivedEvent event, MessageSender chat) {
      List<GameVoteOption> options = this.gameVoteService.getGameVoteOptionsOrdered();
      if(options.isEmpty()) {
         chat.sendMessage("There are no options to vote on");
         return;
      }
      
      StringBuilder builder = new StringBuilder();      
      for (int i = 0; i < options.size(); i++) {
         builder.append(options.get(i).getGameVoteName());
         builder.append("\n");
      }
      
      chat.sendEmbed("List of Games", builder.toString());
   }

   private void removeOption(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length < 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      // Combine remaining text into a single string
      String[] remainingText = Arrays.copyOfRange(args, 1, args.length);
      String optionName = String.join(" ", remainingText);
      try {
         this.gameVoteService.removeGameVoteOption(optionName);
         log.info("GameVoteOption removed: [{}]", optionName);
      } catch (VoteOptionDoesNotExistException ex) {
         log.warn("GameVoteOption did not exist: [{}]", optionName);
         chat.sendMessage("Could not find voting option with that name");
      }

      chat.sendMessage("Removed [" + optionName + "] as an option");
   }
}
