package de.ellpeck.naturesaura.blocks.tiles;

import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.aura.chunk.IAuraChunk;
import de.ellpeck.naturesaura.api.aura.container.BasicAuraContainer;
import de.ellpeck.naturesaura.api.aura.container.IAuraContainer;
import de.ellpeck.naturesaura.packet.PacketHandler;
import de.ellpeck.naturesaura.packet.PacketParticles;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class TileEntityEndFlower extends TileEntityImpl implements ITickable {

    private final BasicAuraContainer container = new BasicAuraContainer(null, 500000) {
        {
            this.aura = this.maxAura;
        }

        @Override
        public int storeAura(int amountToStore, boolean simulate) {
            return 0;
        }

        @Override
        public int drainAura(int amountToDrain, boolean simulate) {
            int amount = super.drainAura(amountToDrain, simulate);
            if (amount > 0 && !simulate)
                TileEntityEndFlower.this.sendToClients();
            return amount;
        }

        @Override
        public int getAuraColor() {
            return 0x6a25dd;
        }
    };

    public boolean isDrainMode;

    @Override
    public void update() {
        if (!this.world.isRemote) {
            if (this.world.getTotalWorldTime() % 10 != 0)
                return;

            if (!this.isDrainMode) {
                List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class,
                        new AxisAlignedBB(this.pos).grow(1), EntitySelectors.IS_ALIVE);
                for (EntityItem item : items) {
                    if (item.cannotPickup())
                        continue;
                    ItemStack stack = item.getItem();
                    if (stack.getCount() != 1)
                        continue;
                    if (stack.getItem() != Items.ENDER_EYE)
                        continue;

                    this.isDrainMode = true;
                    item.setDead();

                    PacketHandler.sendToAllAround(this.world, this.pos, 32,
                            new PacketParticles((float) item.posX, (float) item.posY, (float) item.posZ, 21, this.container.getAuraColor()));
                    break;
                }
            } else {
                int toDrain = Math.min(5000, this.container.getStoredAura());
                this.container.drainAura(toDrain, false);

                while (toDrain > 0) {
                    BlockPos spot = IAuraChunk.getLowestSpot(this.world, this.pos, 30, this.pos);
                    toDrain -= IAuraChunk.getAuraChunk(this.world, spot).storeAura(spot, toDrain);
                }

                if (this.container.getStoredAura() <= 0) {
                    this.world.setBlockState(this.pos, Blocks.DEADBUSH.getDefaultState());
                    PacketHandler.sendToAllAround(this.world, this.pos, 32,
                            new PacketParticles(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 18, this.container.getAuraColor()));
                }
            }
        } else {
            if (this.isDrainMode && this.world.getTotalWorldTime() % 5 == 0)
                NaturesAuraAPI.instance().spawnMagicParticle(
                        this.pos.getX() + 0.25F + this.world.rand.nextFloat() * 0.5F,
                        this.pos.getY() + 0.25F + this.world.rand.nextFloat() * 0.5F,
                        this.pos.getZ() + 0.25F + this.world.rand.nextFloat() * 0.5F,
                        this.world.rand.nextGaussian() * 0.05F,
                        this.world.rand.nextFloat() * 0.1F,
                        this.world.rand.nextGaussian() * 0.05F,
                        this.container.getAuraColor(), this.world.rand.nextFloat() * 2F + 1F, 50, 0F, false, true);
        }
    }

    @Override
    public IAuraContainer getAuraContainer(EnumFacing facing) {
        return this.container;
    }

    @Override
    public void writeNBT(NBTTagCompound compound, SaveType type) {
        super.writeNBT(compound, type);
        if (type != SaveType.BLOCK) {
            this.container.writeNBT(compound);
            compound.setBoolean("drain_mode", this.isDrainMode);
        }
    }

    @Override
    public void readNBT(NBTTagCompound compound, SaveType type) {
        super.readNBT(compound, type);
        if (type != SaveType.BLOCK) {
            this.container.readNBT(compound);
            this.isDrainMode = compound.getBoolean("drain_mode");
        }
    }
}
