package de.simonsator.partyandfriends.friends.subcommands;

import de.simonsator.partyandfriends.api.TextReplacer;
import de.simonsator.partyandfriends.api.friends.abstractcommands.FriendSubCommand;
import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.api.pagesmanager.PageAsListContainer;
import de.simonsator.partyandfriends.api.pagesmanager.PageCreator;
import de.simonsator.partyandfriends.api.pagesmanager.PageEntriesAsTextContainer;
import de.simonsator.partyandfriends.main.Main;
import de.simonsator.partyandfriends.utilities.PatterCollection;
import de.simonsator.partyandfriends.utilities.PlayerListElement;
import net.md_5.bungee.api.config.ServerInfo;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

/***
 * The command list
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class FriendList extends FriendSubCommand implements PageCreator<PlayerListElement> {
	private final String LAST_ONLINE_COLOR = Main.getInstance().getMessages().getString("Friends.Command.List.TimeColor");
	private final int ENTRIES_PER_PAGE = Main.getInstance().getConfig().getInt("Commands.Friends.SubCommands.List.EntriesPerPage");
	private boolean sortElements;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(Main.getInstance().getConfig().getString("General.Time.Format"),
			Locale.forLanguageTag(Main.getInstance().getConfig().getString("General.Time.LanguageTag")));
	private List<TextReplacer> replacerList = new ArrayList<>();

	public FriendList(List<String> pCommands, int pPriority, String pHelp, String pPermission) {
		super(pCommands, pPriority, pHelp, pPermission);
		dateFormat.setTimeZone(TimeZone.getTimeZone(Main.getInstance().getConfig().getString("General.Time.TimeZone")));
		sortElements = Main.getInstance().getConfig().getBoolean("Commands.Friends.SubCommands.List.SortElements");
	}

	@Override
	public void onCommand(OnlinePAFPlayer pPlayer, String[] args) {
		List<PAFPlayer> friends = pPlayer.getFriends();
		if (!hasFriends(pPlayer, friends))
			return;
		PageEntriesAsTextContainer friendsCombined = getFriendsCombined(friends, args);
		if (friendsCombined == null) {
			pPlayer.sendMessage(PREFIX + Main.getInstance().getMessages().getString("Friends.Command.List.PageDoesNotExist"));
			return;
		}
		pPlayer.sendMessage(PREFIX
				+ Main.getInstance().getMessages().getString("Friends.Command.List.FriendsList")
				+ friendsCombined.getLimitedTextList());
		if (friendsCombined.doesFurtherItemsExist())
			pPlayer.sendMessage(PREFIX + PatterCollection.PAGE_PATTERN.matcher(Main.getInstance().getMessages().getString("Friends.Command.List.NextPage")).replaceFirst("" + (friendsCombined.getPage() + 1)));
	}

	private PageEntriesAsTextContainer getFriendsCombined(List<PAFPlayer> pFriends, String[] args) {
		StringBuilder friendsCombined = new StringBuilder();
		List<PlayerListElement> playerListElements = toList(pFriends);
		if (sortElements)
			Collections.sort(playerListElements);
		PageAsListContainer<PlayerListElement> playerListElementsContainer = createPage(playerListElements, args, ENTRIES_PER_PAGE);
		if (playerListElementsContainer == null)
			return null;
		playerListElements = playerListElementsContainer.getLimitedList();
		for (int i = 0; i < playerListElements.size(); i++) {
			StringBuilder builder = new StringBuilder();
			String additive;
			String color;
			if (!playerListElements.get(i).isOnline()) {
				additive = PatterCollection.LAST_ONLINE_PATTERN.matcher(
						Main.getInstance().getMessages().getString("Friends.Command.List.OfflineTitle")).replaceAll(Matcher.quoteReplacement(
						setLastOnlineColor(dateFormat.format(playerListElements.get(i).getLastOnline()))));
				color = Main.getInstance().getMessages().getString("Friends.Command.List.OfflineColor");
			} else {
				ServerInfo server = playerListElements.get(i).getServer();
				String serverName;
				if (server == null)
					serverName = "?";
				else
					serverName = server.getName();
				additive = PatterCollection.SERVER_ON.matcher(Main.getInstance().getMessages().getString("Friends.Command.List.OnlineTitle")).
						replaceAll(Matcher.quoteReplacement(serverName));
				color = Main.getInstance().getMessages().getString("Friends.Command.List.OnlineColor");
			}
			if (i > 0)
				builder.append(Main.getInstance().getMessages().getString("Friends.Command.List.PlayerSplit"));
			builder.append(color);
			builder.append(playerListElements.get(i).getDisplayName());
			builder.append(additive);
			String processed = process(playerListElements.get(i).getPlayer(), builder.toString());
			friendsCombined.append(processed);
		}
		return new PageEntriesAsTextContainer(playerListElementsContainer.doesFurtherItemsExist(), friendsCombined.toString(), playerListElementsContainer.getPage());
	}

	private boolean hasFriends(OnlinePAFPlayer pPlayer, List<PAFPlayer> pFriends) {
		if (pFriends.isEmpty()) {
			pPlayer.sendMessage((PREFIX
					+ Main.getInstance().getMessages().getString("Friends.Command.List.NoFriendsAdded")));
			return false;
		}
		return true;
	}

	private String setLastOnlineColor(String pLastOnline) {
		StringBuilder stringBuilder = new StringBuilder();
		for (char args : pLastOnline.toCharArray()) {
			stringBuilder.append(LAST_ONLINE_COLOR);
			stringBuilder.append(args);
		}
		return stringBuilder.toString();
	}

	private List<PlayerListElement> toList(List<PAFPlayer> pPlayers) {
		List<PlayerListElement> playerListElements = new ArrayList<>(pPlayers.size());
		for (PAFPlayer player : pPlayers)
			playerListElements.add(new PlayerListElement(player));
		return playerListElements;
	}

	private String process(PAFPlayer pPlayer, String pMessage) {
		String message = pMessage;
		for (TextReplacer replacer : replacerList)
			message = replacer.onProecess(pPlayer, pMessage);
		return message;
	}

	public void registerTextReplacer(TextReplacer pTextReplacer) {
		replacerList.add(pTextReplacer);
	}

	public void unregisterTextReplacer(TextReplacer pTextReplacer) {
		replacerList.remove(pTextReplacer);
	}
}
