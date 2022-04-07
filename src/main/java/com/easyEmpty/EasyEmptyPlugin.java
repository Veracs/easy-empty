/*
 * Copyright (c) 2022, Jacob Petersen <jakepetersen1221@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.easyEmpty;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "Easy Empty",
	description = "Swap essence pouch left click to Empty when near a runecrafting altar",
	tags = {"swap","swapper","menu","entry","menu entry swapper","runecrafting","pouch","essence"}
)
public class EasyEmptyPlugin extends Plugin
{
	int[] altars = {
		10571, // Earth
		10315, // Fire
		10827, // Water
		11339, // Air
		10059, // Body
		11083, // Mind
		8523,  // Cosmic
		9035,  // Chaos
		9803,  // Law
		9547,  // Nature
		8779,  // Death
		9291, // Wrath
		8508, // Astral
		12875 // Blood
	};

	private static final WorldArea zmi = new WorldArea(new WorldPoint(3050, 5573, 0), 20, 20);

	@Inject
	private Client client;

	@Inject
	private EasyEmptyConfig config;

	@Override
	protected void startUp()
	{
		log.info("Easy Empty  started!");
	}

	@Override
	protected void shutDown()
	{
		log.info("Easy Empty  stopped!");
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen() || client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		boolean atAltar = false;
		WorldPoint playerLoc = Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation();

		if (zmi.contains2D(playerLoc)) {
			atAltar = true;
		}
		else {
			for (int altarRegion : altars) {
				if (altarRegion == playerLoc.getRegionID()) {
					atAltar = true;
					break;
				}
			}
		}

		if (atAltar) {
			MenuEntry[] menuEntries = client.getMenuEntries();
			int emptyIdx = -1;
			int topIdx = menuEntries.length - 1;
			for (int i = 0; i < topIdx; i++) {

				if (Text.removeTags(menuEntries[i].getOption()).equals("Empty")) {
					emptyIdx = i;
					break;
				}
			}
			if (emptyIdx == -1) {
				return;
			}

			MenuEntry entry1 = menuEntries[emptyIdx];
			MenuEntry entry2 = menuEntries[topIdx];

			menuEntries[emptyIdx] = entry2;
			menuEntries[topIdx] = entry1;

			client.setMenuEntries(menuEntries);
		}
	}

	@Provides
	EasyEmptyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EasyEmptyConfig.class);
	}
}