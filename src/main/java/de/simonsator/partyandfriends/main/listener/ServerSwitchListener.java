package de.simonsator.partyandfriends.main.listener;

import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.api.party.PartyManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import de.simonsator.partyandfriends.main.Main;
import de.simonsator.partyandfriends.party.command.PartyCommand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

/**
 * The class with the ServerSwitchEvent
 *
 * @author Simonsator
 * @version 1.0.0
 */
public class ServerSwitchListener implements Listener {
	/**
	 * The list of the servers which the party will not join.
	 */
	private final List<String> notJoinServers;

	/**
	 * Initials the object
	 */
	public ServerSwitchListener() {
		notJoinServers = Main.getInstance().getConfig().getStringList("General.PartyDoNotJoinTheseServers");
	}

	/**
	 * Will be executed if a player switches the server
	 *
	 * @param pEvent The ServerSwitchEvent event
	 */
	@EventHandler
	public void onServerSwitch(final ServerSwitchEvent pEvent) {
		ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				moveParty(pEvent);
			}
		});
	}

	private void moveParty(ServerSwitchEvent pEvent) {
		ServerInfo server = pEvent.getPlayer().getServer().getInfo();
		if (notJoinServers.contains(server.getName()))
			return;
		OnlinePAFPlayer player = PAFPlayerManager.getInstance().getPlayer(pEvent.getPlayer());
		PlayerParty party = PartyManager.getInstance().getParty(player);
		if (party != null && party.isLeader(player)) {
			for (OnlinePAFPlayer p : party.getPlayers())
				p.connect(server);
			party.sendMessage((PartyCommand.getInstance().getPrefix()
					+ Main.getInstance().getMessages().getString("Party.Command.General.ServerSwitched")
					.replace("[SERVER]", server.getName())));
		}
	}
}
