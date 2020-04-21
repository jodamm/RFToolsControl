package mcjty.rftoolscontrol.modules.processor.network;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetLog {

    protected BlockPos pos;
    protected DimensionType type;
    protected TypedMap params;
    private boolean fromTablet;

    public PacketGetLog(PacketBuffer buf) {
        pos = buf.readBlockPos();
        type = DimensionType.getById(buf.readInt());
        params = TypedMapTools.readArguments(buf);
        fromTablet = buf.readBoolean();
    }

    public PacketGetLog(DimensionType type, BlockPos pos, boolean fromTablet) {
        this.pos = pos;
        this.type = type;
        this.params = TypedMap.EMPTY;
        this.fromTablet = fromTablet;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(type.getId());
        TypedMapTools.writeArguments(buf, params);
        buf.writeBoolean(fromTablet);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerWorld world = WorldTools.getWorld(ctx.getSender().getEntityWorld(), type);
            TileEntity te = world.getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<String> list = commandHandler.executeWithResultList(ProcessorTileEntity.CMD_GETLOG, params, Type.STRING);
            if (fromTablet) {
                // We don't have a good position for our tile entity as it might not exist client-side
                RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(null, ProcessorTileEntity.CLIENTCMD_GETLOG, list), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            } else {
                RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(pos, ProcessorTileEntity.CLIENTCMD_GETLOG, list), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        ctx.setPacketHandled(true);
    }
}