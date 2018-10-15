package de.ellpeck.naturesaura.packet;

import de.ellpeck.naturesaura.NaturesAura;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler {

    private static SimpleNetworkWrapper network;

    public static void init() {
        network = new SimpleNetworkWrapper(NaturesAura.MOD_ID);
        network.registerMessage(PacketParticles.Handler.class, PacketParticles.class, 0, Side.CLIENT);
    }

    public static void sendToAllLoaded(World world, BlockPos pos, IMessage message) {
        network.sendToAllTracking(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 0));
    }

}