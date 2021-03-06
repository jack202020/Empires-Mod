package com.andrew2070.Empires.API.Chat;

import java.util.List;

import org.bukkit.Bukkit;

import com.andrew2070.Empires.API.commands.CommandsEMP;
import com.andrew2070.Empires.Datasource.EmpiresUniverse;
import com.andrew2070.Empires.entities.Empire.Citizen;
import com.andrew2070.Empires.entities.Empire.Empire;
import com.andrew2070.Empires.entities.Empire.Rank;
import com.andrew2070.Empires.entities.Managers.ToolManager;
import com.andrew2070.Empires.exceptions.EmpiresCommandException;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.ServerChatEvent;

public class ChatHandler {
	public static final ChatHandler instance = new ChatHandler();
	
	@SubscribeEvent
	
	public void onServerChatReceivedEvent(ServerChatEvent event) {
		
	EntityPlayer player = (EntityPlayer) event.player;

	
	 if (event.player!=null) {
		 
		 event.setCanceled(true);
		 
		 
		 List players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		 
		 
		 for (int i = 0; i < + players.size();i++) {
			 
			EntityPlayer target = (EntityPlayer) players.get(i);
			Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(target);
			Empire empire;
			Rank rank;
			
			if (CommandsEMP.getEmpireFromCitizen(res) == null)
			{
				String chat2 = player.getDisplayName() + ": " + event.message;
				target.addChatMessage(new ChatComponentTranslation(chat2));
			}

			empire = CommandsEMP.getEmpireFromCitizen(res);
			rank = CommandsEMP.getRankFromCitizen(res);
			// TODO: Abbreviate ranks to [L], [O], [M].
			// TODO: Add herochat support?
			 String chat = "[" + rank   +"]" + "§6[" + empire + "]" + "§f " + player.getDisplayName() + ": " + event.message;
			 target.addChatMessage(new ChatComponentTranslation(chat));
	
			}

			}

	 	}

	}
	
	



