package de.simonsator.partyandfriends.api.pafplayers;

import de.simonsator.partyandfriends.api.events.DisplayNameProviderChangedEvent;
import de.simonsator.partyandfriends.utilities.StandardDisplayNameProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.protocol.packet.Chat;

import java.util.List;
import java.util.Random;

public abstract class PAFPlayerClass implements PAFPlayer {
	private static DisplayNameProvider displayNameProvider = new StandardDisplayNameProvider();
	private Random randomGenerator = new Random();

	public static DisplayNameProvider getDisplayNameProvider() {
		return displayNameProvider;
	}

	public static void setDisplayNameProvider(DisplayNameProvider pDisplayNameProvider) {
		displayNameProvider = pDisplayNameProvider;
		ProxyServer.getInstance().getPluginManager().callEvent(new DisplayNameProviderChangedEvent(pDisplayNameProvider));
	}

	@Override
	public void sendMessage(TextComponent pTextComponent) {
		/* If the player is offline no message needs to be send. This method should be overwritten
		 * by a class which extends this class and implements OnlinePAFPlayer*/
	}

	@Override
	public void sendMessage(String pText) {
		String[] spited = pText.split("LINE_BREAK");
		for (String split : spited)
			sendMessage(new TextComponent(split));
	}


	@Override
	public void sendPacket(Chat chat) {
		/* If the player is offline no message needs to be send. This method should be overwritten
		 * by a class which extends this class and implements OnlinePAFPlayer*/
	}

	@Override
	public int hashCode() {
		return getUniqueId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof PAFPlayer && ((PAFPlayer) obj).getUniqueId().equals(this.getUniqueId());
	}

	@Override
	public String toString() {
		return "{Name:\"" + getName() + "\", DisplayName:\"" + getDisplayName() + "\"}";
	}

	@Override
	public String getDisplayName() {
		return displayNameProvider.getDisplayName(this);
	}

	@Override
	public void sendMessage(Object pMessage) {
		if (pMessage instanceof List) {
			sendMessage((List<String>) pMessage);
			return;
		}
		if (pMessage instanceof String)
			sendMessage((String) pMessage);
	}

	@Override
	public void sendMessage(List<String> pMessages) {
		sendMessage(pMessages.get(randomGenerator.nextInt(pMessages.size())));
	}

	@Override
	public boolean isOnline() {
		return false;
	}
}
