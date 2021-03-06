package com.andrew2070.Empires;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;

import com.andrew2070.Empires.API.Chat.ChatHandler;
import com.andrew2070.Empires.API.JSON.FlagsConfig;
import com.andrew2070.Empires.API.JSON.JsonConfig;
import com.andrew2070.Empires.API.JSON.RanksConfig;
import com.andrew2070.Empires.API.JSON.WildPermsConfig;
import com.andrew2070.Empires.API.commands.CommandManager;
import com.andrew2070.Empires.API.commands.CommandsEMP;
import com.andrew2070.Empires.API.commands.Local;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.API.permissions.PermissionManager;
import com.andrew2070.Empires.API.permissions.PermissionProxy;
import com.andrew2070.Empires.API.permissions.RankPermissionManager;
import com.andrew2070.Empires.API.permissions.Bridges.MyPermissionsBridge;
import cpw.mods.fml.common.event.*;
import com.andrew2070.Empires.Config.Config;
import com.andrew2070.Empires.Datasource.DatasourceCrashCallable;
import com.andrew2070.Empires.Datasource.EmpiresDatasource;
import com.andrew2070.Empires.Datasource.EmpiresUniverse;
import com.andrew2070.Empires.Handlers.Constants;
import com.andrew2070.Empires.Handlers.EmpiresLoadingCallback;
import com.andrew2070.Empires.Handlers.ExtraEventsHandler;
import com.andrew2070.Empires.Handlers.PlayerTracker;
import com.andrew2070.Empires.Handlers.SafemodeHandler;
import com.andrew2070.Empires.Handlers.Ticker;
import com.andrew2070.Empires.Handlers.VisualsHandler;
import com.andrew2070.Empires.Proxies.EconomyProxy;
import com.andrew2070.Empires.commands.Admin.CommandsAdmin;
import com.andrew2070.Empires.commands.Neutral.CommandsNeutral;
import com.andrew2070.Empires.commands.Neutral.PermCommands;
import com.andrew2070.Empires.commands.Neutral.PermCommands.MyPermissionManagerCommands;
import com.andrew2070.Empires.commands.Officer.CommandsOfficer;
import com.andrew2070.Empires.commands.Recruit.CommandsRecruit;
import com.andrew2070.Empires.entities.Empire.Citizen;
import com.andrew2070.Empires.entities.Managers.SignManager;
import com.andrew2070.Empires.entities.Managers.ToolManager;
import com.andrew2070.Empires.entities.Signs.SellSign;
import com.andrew2070.Empires.exceptions.ConfigException;
import com.andrew2070.Empires.protection.ProtectionHandlers;
import com.andrew2070.Empires.protection.ProtectionManager;
import com.andrew2070.Empires.protection.JSON.ProtectionParser;
import com.andrew2070.Empires.utils.StringUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)

public class Empires {
//@Instance
	@Mod.Instance(Constants.MODID)

	public static Empires instance;
    public Local LOCAL;
    public Logger LOG;
    public EmpiresDatasource datasource;
    private final List<JsonConfig> jsonConfigs =  new ArrayList<JsonConfig>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		//ITEMS & BLOCK INIT + REGISTERING STUFF
		MinecraftServer server = MinecraftServer.getServer();
        LOG = ev.getModLog();
        
        Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/Empires/";
        Constants.DATABASE_FOLDER = ev.getModConfigurationDirectory().getParent() + "/databases/";
        // Load Configs
        
        Config.instance.init(Constants.CONFIG_FOLDER + "/Empires.cfg", "Empires Mod");
        
        // REF: The localization can simply take the whole config instance to get the localization needed.
        
        LOCAL = new Local(Constants.CONFIG_FOLDER + "/Localization/", Config.instance.localization.get(), "/Empires/Localization/", Empires.class);
        LocalManager.register(LOCAL, "Empires");

        // Register handlers/trackers
        FMLCommonHandler.instance().bus().register(PlayerTracker.instance);
        MinecraftForge.EVENT_BUS.register(PlayerTracker.instance);
        
        FMLCommonHandler.instance().bus().register(ChatHandler.instance);
        MinecraftForge.EVENT_BUS.register(ChatHandler.instance);
        
        
        FMLCommonHandler.instance().bus().register(Ticker.instance);
        MinecraftForge.EVENT_BUS.register(Ticker.instance);
        
        FMLCommonHandler.instance().bus().register(ToolManager.instance);
        MinecraftForge.EVENT_BUS.register(ToolManager.instance);

        FMLCommonHandler.instance().bus().register(SignManager.instance);
        MinecraftForge.EVENT_BUS.register(SignManager.instance);

        registerHandlers();
        
        FMLCommonHandler.instance().registerCrashCallable(new DatasourceCrashCallable());
      
        
	}

    public void loadConfig() {
        Config.instance.reload();
        PermissionProxy.init();
        LOCAL.load();
    }

	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Proxy, TileEntity, entity, GUI, and Packet Register
	    System.out.println("Empires Mod: By Andrew2070");
	    System.out.println("Empires Mod: Now Initializing...");
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		//Put hooks for other Mods here
		SellSign.SellSignType.instance.register();
	    System.out.println("Empires Mod: Initialization Finished.");

	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent ev) {
	    EconomyProxy.init();
        checkConfig();
        registerCommands();
        CommandsEMP.populateCompletionMap();
	    System.out.println("Empires Mod: Server Detected, Registering Commands...");
	    loadConfig();
        CommandManager.registerCommands(PermCommands.class, null, Empires.instance.LOCAL, null);
        if(PermissionProxy.getPermissionManager() instanceof MyPermissionsBridge) {	
       CommandManager.registerCommands(PermissionManager.class, "Empires.cmd", Empires.instance.LOCAL, null);
	}
        
        jsonConfigs.add(new WildPermsConfig(Constants.CONFIG_FOLDER + "/WildPerms.json"));
        jsonConfigs.add(new FlagsConfig(Constants.CONFIG_FOLDER + "/DefaultFlags.json"));
        jsonConfigs.add(new RanksConfig(Constants.CONFIG_FOLDER + "/DefaultEmpireRanks.json"));
        for (JsonConfig jsonConfig : jsonConfigs) {
            jsonConfig.init();
        }

        ProtectionParser.start();
        //SafemodeHandler.setSafemode(!DatasourceProxy.start(config));
        datasource = new EmpiresDatasource();
        LOG.info("Started");
   

}
	 @Mod.EventHandler
	    public void serverStopping(FMLServerStoppingEvent ev) {
	        datasource.deleteAllBlockOwners();
	        ProtectionManager.saveBlockOwnersToDB();
	        datasource.stop();
    
	 }	
	 
	 
	    private void registerCommands() {
	        RankPermissionManager bridge = new RankPermissionManager();
	        CommandManager.registerCommands(CommandsRecruit.class, null, LOCAL, bridge);
	        CommandManager.registerCommands(CommandsOfficer.class, "Empires.cmd", LOCAL, bridge);
	        if (Config.instance.modifiableRanks.get())
	        CommandManager.registerCommands(CommandsOfficer.ModifyRanks.class, "Empires.cmd", LOCAL, bridge);
	        CommandManager.registerCommands(CommandsAdmin.class, null, LOCAL, null);
	        if(Config.instance.enablePlots.get()) {
	            CommandManager.registerCommands(CommandsRecruit.Plots.class, "Empires.cmd", LOCAL, null);
	            CommandManager.registerCommands(CommandsOfficer.Plots.class, "Empires.cmd", LOCAL, bridge);
	            CommandManager.registerCommands(CommandsAdmin.Plots.class, "Empires.adm.cmd", LOCAL, null);
	        }
	        CommandManager.registerCommands(CommandsNeutral.class, "Empires.cmd", LOCAL, null);
	    }
	 
	 
	    public WildPermsConfig getWildConfig() {
	        for(JsonConfig jsonConfig : jsonConfigs) {
	            if(jsonConfig instanceof WildPermsConfig)
	                return (WildPermsConfig)jsonConfig;
	        }
	        return null;
	    }
	 
	    public RanksConfig getRanksConfig() {
	        for(JsonConfig jsonConfig : jsonConfigs) {
	            if(jsonConfig instanceof RanksConfig)
	                return (RanksConfig)jsonConfig;
	        }
	        return null;
	    }
	    
	    public FlagsConfig getFlagsConfig() {
	        for(JsonConfig jsonConfig : jsonConfigs) {
	            if(jsonConfig instanceof FlagsConfig)
	                return (FlagsConfig)jsonConfig;
	        }
	        return null;
	    }
	    
	    private void registerHandlers() {

	        FMLCommonHandler.instance().bus().register(new SafemodeHandler());
	        Ticker playerTracker = new Ticker();

	        FMLCommonHandler.instance().bus().register(playerTracker);
	        MinecraftForge.EVENT_BUS.register(playerTracker);

	        FMLCommonHandler.instance().bus().register(VisualsHandler.instance);

	        FMLCommonHandler.instance().bus().register(ProtectionHandlers.instance);
	        MinecraftForge.EVENT_BUS.register(ProtectionHandlers.instance);

	        MinecraftForge.EVENT_BUS.register(ExtraEventsHandler.getInstance());

	        ForgeChunkManager.setForcedChunkLoadingCallback(this, new EmpiresLoadingCallback());
	    }
	 
	    
	    
	    public void loadConfigs() {
	        Config.instance.reload();

	        EconomyProxy.init();
	        checkConfig();

	        LOCAL.load();

	        for (JsonConfig jsonConfig : jsonConfigs) {
	            jsonConfig.init();
	        }

	        ProtectionParser.start();
	    }
	    
	    private void checkConfig() {
	        // Checking cost item
	        if(EconomyProxy.isItemEconomy()) {
	            String[] split = Config.instance.costItemName.get().split(":");
	            if (split.length < 2 || split.length > 3) {
	                throw new ConfigException("Field costItem has an invalid value. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
	            }

	            if (GameRegistry.findItem(split[0], split[1]) == null) {
	                throw new ConfigException("Field costItem has an invalid modid or unique name of the item. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
	            }

	            if (split.length > 2 && (!StringUtils.tryParseInt(split[2]) || Integer.parseInt(split[2]) < 0)) {
	                throw new ConfigException("Field costItem has an invalid metadata. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
	            }
	        }
	    }

	    
	    
	    
	 
}