package com.andrew2070.Empires.entities.Tools;



import com.andrew2070.Empires.Empires;
import com.andrew2070.Empires.API.commands.ChatManager;
import com.andrew2070.Empires.entities.Managers.ToolManager;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.entities.Position.BlockPos;
import com.andrew2070.Empires.entities.Misc.Tool;


import com.andrew2070.Empires.Config.Config;
import com.andrew2070.Empires.Thread.DelayedThread;
import com.andrew2070.Empires.Datasource.EmpiresUniverse;
import com.andrew2070.Empires.Handlers.VisualsHandler;
import com.andrew2070.Empires.entities.Empire.*;
import com.andrew2070.Empires.utils.EmpireUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Tool that selects two corners of a plot and creates it.
 */
public class PlotSelectionTool extends Tool {

    private Selection selectionFirst, selectionSecond;
    private String plotName;
    private boolean heightDependent = Config.instance.defaultPlotHightDependence.get();
    private Citizen owner;

    public PlotSelectionTool(Citizen owner, String plotName) {
        super(owner.getPlayer(), LocalManager.get("Empires.tool.name", LocalManager.get("Empires.tool.plot.selection.name")).getLegacyFormattedText()[0]);
        this.owner = owner;
        this.plotName = plotName;
    }

    @Override
    public void onItemUse(BlockPos bp, int face) {
        Empire empire = EmpireUtils.getEmpireAtPosition(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);

        if(!hasPermission(empire)) {
            resetSelection(true, 0);
            return;
        }

        if (selectionFirst != null && selectionFirst.dim != bp.getDim()) {
            ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.selection.otherDimension");
            return;
        }

        if (selectionFirst == null) {
            // selectionSecond = null;
            selectionFirst = new Selection(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
            // This is marked twice :P
            if(owner.getPlayer() instanceof EntityPlayerMP) {
                VisualsHandler.instance.markBlock(bp.getX(), bp.getY(), bp.getZ(), bp.getDim(), Blocks.redstone_block, (EntityPlayerMP) owner.getPlayer(), owner.getPlayer());
            }

        } else {
            selectionSecond = new Selection(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
            createPlotFromSelection();
        }
    }

    @Override
    protected String[] getDescription() {
        return LocalManager.get("Empires.tool.plot.selection.description", plotName, heightDependent).getLegacyFormattedText();
    }

    @Override
    public void onShiftRightClick() {
        heightDependent = !heightDependent;
        updateDescription();
        ChatManager.send(owner.getPlayer(), "Empires.tool.mode", LocalManager.get("Empires.tool.plot.selection.property"), heightDependent);
    }

    public void resetSelection(boolean resetBlocks, int delay) {
        this.selectionFirst = null;
        this.selectionSecond = null;

        if(resetBlocks && owner.getPlayer() instanceof EntityPlayerMP) {
            if(delay <= 0) {
                VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) owner.getPlayer(), owner.getPlayer());
            } else {
                try {
                    new DelayedThread(delay, VisualsHandler.class.getMethod("unmarkBlocks", EntityPlayerMP.class, Object.class), VisualsHandler.instance, owner.getPlayer(), owner.getPlayer()).start();
                } catch (Exception ex) {
                    Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
                }
            }
        }
    }

    protected boolean hasPermission(Empire empire) {
        if (empire == null || empire != owner.empiresContainer.getMainEmpire() && selectionFirst != null || selectionFirst != null && empire != selectionFirst.empire) {
            ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.selection.outside");
            return false;
        }
        if (!empire.plotsContainer.canCitizenMakePlot(owner)) {
            ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.limit", empire.plotsContainer.getMaxPlots());
            return false;
        }
        for(Plot plot : empire.plotsContainer) {
            if(plot.getName().equals(plotName)) {
                ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.name", plotName);
                return false;
            }
        }
        return true;
    }

    private boolean expandVertically() {
        if(selectionFirst == null || selectionSecond == null)
            return false;

        selectionFirst.y = 0;
        selectionSecond.y = MinecraftServer.getServer().worldServerForDimension(selectionSecond.dim).getActualHeight() - 1;

        if(owner.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) owner.getPlayer(), owner.getPlayer());

        if(owner.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.markPlotBorders(selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z, selectionFirst.dim, (EntityPlayerMP) owner.getPlayer(), owner.getPlayer());

        return true;
    }

    private void createPlotFromSelection() {
        normalizeSelection();
        if(!isProperSize()) {
            resetSelection(true, 0);
            return;
        }

        int lastX = 1000000, lastZ = 1000000;
        for (int i = selectionFirst.x; i <= selectionSecond.x; i++) {
            for (int j = selectionFirst.z; j <= selectionSecond.z; j++) {

                // Verifying if it's in empire
                if (i >> 4 != lastX || j >> 4 != lastZ) {
                    lastX = i >> 4;
                    lastZ = j >> 4;
                    EmpireBlock block = EmpiresUniverse.instance.blocks.get(selectionFirst.dim, lastX, lastZ);
                    if (block == null || block.getEmpire() != selectionFirst.empire) {
                        ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.outside");
                        resetSelection(true, 0);
                        return;
                    }
                }

                // Verifying if it's inside another plot
                for (int k = selectionFirst.y; k <= selectionSecond.y; k++) {
                    Plot plot = selectionFirst.empire.plotsContainer.get(selectionFirst.dim, i, k, j);
                    if (plot != null) {
                        ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.insideOther", plot);
                        resetSelection(true, 0);
                        return;
                    }
                }
            }
        }

        Plot plot = EmpiresUniverse.instance.newPlot(plotName, selectionFirst.empire, selectionFirst.dim, selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z);
        resetSelection(true, 5);

        Empires.instance.datasource.savePlot(plot);
        Empires.instance.datasource.linkCitizenToPlot(owner, plot, true);
        ChatManager.send(owner.getPlayer(), "Empires.notification.plot.created");
        ToolManager.instance.remove(this);
    }

    private void normalizeSelection() {
        if(!heightDependent) {
            expandVertically();
        } else {
            if(owner.getPlayer() instanceof EntityPlayerMP)
                VisualsHandler.instance.markCorners(selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z, selectionFirst.dim, (EntityPlayerMP) owner.getPlayer());
        }

        if (selectionSecond.x < selectionFirst.x) {
            int aux = selectionFirst.x;
            selectionFirst.x = selectionSecond.x;
            selectionSecond.x = aux;
        }
        if (selectionSecond.y < selectionFirst.y) {
            int aux = selectionFirst.y;
            selectionFirst.y = selectionSecond.y;
            selectionSecond.y = aux;
        }
        if (selectionSecond.z < selectionFirst.z) {
            int aux = selectionFirst.z;
            selectionFirst.z = selectionSecond.z;
            selectionSecond.z = aux;
        }
    }

    private boolean isProperSize() {
        if(!(selectionFirst.empire instanceof AdminEmpire)) {
            if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) < Config.instance.minPlotsArea.get()
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 < Config.instance.minPlotsHeight.get()) {
                ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.tooSmall", Config.instance.minPlotsArea.get(), Config.instance.minPlotsHeight.get());
                return false;
            } else if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) > Config.instance.maxPlotsArea.get()
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 > Config.instance.maxPlotsHeight.get()) {
                ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.tooLarge", Config.instance.maxPlotsArea.get(), Config.instance.maxPlotsHeight.get());
                return false;
            }
        }
        return true;
    }

    private class Selection {
        private int x, y, z, dim;
        private Empire empire;

        public Selection(int dim, int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            // Not checking for null since this should not be created if the empire is null.
            this.empire = EmpiresUniverse.instance.blocks.get(dim, x >> 4, z >> 4).getEmpire();
        }
    }
}