package com.andrew2070.Empires.entities.Tools;

import com.andrew2070.Empires.API.commands.ChatManager;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.entities.Managers.ToolManager;
import com.andrew2070.Empires.entities.Misc.Tool;
import com.andrew2070.Empires.entities.Position.BlockPos;
import com.andrew2070.Empires.entities.Empire.Plot;
import com.andrew2070.Empires.entities.Empire.Citizen;
import com.andrew2070.Empires.entities.Empire.Empire;
import com.andrew2070.Empires.entities.Signs.SellSign;
import com.andrew2070.Empires.utils.EmpireUtils;

import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * A tool which places signs that, when right clicked, sells the plots to that person.
 */
public class PlotSellTool extends Tool {

    private int price;
    private boolean restricted = false;
    private Citizen owner;

    public PlotSellTool(Citizen owner, int price) {
        super(owner.getPlayer(), LocalManager.get("Empires.tool.name", LocalManager.get("Empires.tool.plot.sell.name")).getLegacyFormattedText()[0]);
        this.owner = owner;
        this.price = price;
    }

    @Override
    public void onItemUse(BlockPos bp, int face) {
        ForgeDirection direction = ForgeDirection.getOrientation(face);
        bp = new BlockPos(bp.getX() + direction.offsetX, bp.getY() + direction.offsetY, bp.getZ() + direction.offsetZ, bp.getDim());

        Empire empire = EmpireUtils.getEmpireAtPosition(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);
        if(!hasPermission(empire, bp)) {
            return;
        }
        new SellSign(bp, face, owner, price, restricted);
        ToolManager.instance.remove(this);
    }

    @Override
    protected String[] getDescription() {
        return LocalManager.get("Empires.tool.plot.sell.description", price, restricted).getLegacyFormattedText();
    }

    @Override
    public void onShiftRightClick() {
        this.restricted = !this.restricted;
        updateDescription();
        ChatManager.send(owner.getPlayer(), "Empires.tool.mode", LocalManager.get("Empires.tool.plot.sell.property"), restricted);
    }

    protected boolean hasPermission(Empire empire, BlockPos bp) {
        World world = MinecraftServer.getServer().worldServerForDimension(bp.getDim());

        if(world.getBlock(bp.getX(), bp.getY(), bp.getZ()) != Blocks.air) {
            return false;
        }

        if(empire == null) {
            ChatManager.send(owner.getPlayer(), "Empires.cmd.err.notInEmpire", owner.empiresContainer.getMainEmpire());
            return false;
        }

        Plot plot = empire.plotsContainer.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        if(plot == null) {
            ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.sell.notInPlot", empire);
            return false;
        }
        if(!plot.ownersContainer.contains(owner) && !plot.getEmpire().hasPermission(owner, "Empires.bypass.plot")) {
            ChatManager.send(owner.getPlayer(), "Empires.cmd.err.plot.noPermission");
            return false;
        }
        return true;
    }
}