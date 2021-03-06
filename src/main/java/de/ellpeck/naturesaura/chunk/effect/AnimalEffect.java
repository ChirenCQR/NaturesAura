package de.ellpeck.naturesaura.chunk.effect;

import de.ellpeck.naturesaura.ModConfig;
import de.ellpeck.naturesaura.NaturesAura;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.aura.chunk.IAuraChunk;
import de.ellpeck.naturesaura.api.aura.chunk.IDrainSpotEffect;
import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AnimalEffect implements IDrainSpotEffect {

    public static final ResourceLocation NAME = new ResourceLocation(NaturesAura.MOD_ID, "animal");

    private int chance;
    private AxisAlignedBB bb;

    private boolean calcValues(World world, BlockPos pos, Integer spot) {
        if (spot <= 0)
            return false;
        int aura = IAuraChunk.getAuraInArea(world, pos, 30);
        if (aura < 1500000)
            return false;
        this.chance = Math.min(50, MathHelper.ceil(Math.abs(aura) / 500000F / IAuraChunk.getSpotAmountInArea(world, pos, 30)));
        if (this.chance <= 0)
            return false;
        int dist = MathHelper.clamp(Math.abs(aura) / 150000, 5, 35);
        this.bb = new AxisAlignedBB(pos).grow(dist);
        return true;
    }

    @Override
    public int isActiveHere(EntityPlayer player, Chunk chunk, IAuraChunk auraChunk, BlockPos pos, Integer spot) {
        if (!this.calcValues(player.world, pos, spot))
            return -1;
        if (!this.bb.contains(player.getPositionVector()))
            return -1;
        if (!NaturesAuraAPI.instance().isEffectPowderActive(player.world, player.getPosition(), NAME))
            return 0;
        return 1;
    }

    @Override
    public ItemStack getDisplayIcon() {
        return new ItemStack(Items.EGG);
    }

    @Override
    public void update(World world, Chunk chunk, IAuraChunk auraChunk, BlockPos pos, Integer spot) {
        if (!this.calcValues(world, pos, spot))
            return;

        List<EntityAnimal> animals = world.getEntitiesWithinAABB(EntityAnimal.class, this.bb);
        if (animals.size() >= 200)
            return;

        if (world.getTotalWorldTime() % 200 == 0) {
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, this.bb);
            for (EntityItem item : items) {
                if (item.isDead)
                    continue;
                if (!NaturesAuraAPI.instance().isEffectPowderActive(world, item.getPosition(), NAME))
                    continue;

                ItemStack stack = item.getItem();
                if (!(stack.getItem() instanceof ItemEgg))
                    continue;
                // The getAge() method is private for absolutely no reason but I want it so I don't care
                int age = ReflectionHelper.getPrivateValue(EntityItem.class, item, "field_70292_b", "age");
                if (age < item.lifespan / 2)
                    continue;

                if (stack.getCount() <= 1)
                    item.setDead();
                else {
                    stack.shrink(1);
                    item.setItem(stack);
                }

                EntityChicken chicken = new EntityChicken(world);
                chicken.setGrowingAge(-24000);
                chicken.setPosition(item.posX, item.posY, item.posZ);
                world.spawnEntity(chicken);

                BlockPos closestSpot = IAuraChunk.getHighestSpot(world, item.getPosition(), 35, pos);
                IAuraChunk.getAuraChunk(world, closestSpot).drainAura(closestSpot, 2000);
            }
        }

        if (world.rand.nextInt(200) <= this.chance) {
            if (animals.size() < 2)
                return;
            EntityAnimal first = animals.get(world.rand.nextInt(animals.size()));
            if (first.isChild() || first.isInLove())
                return;
            if (!NaturesAuraAPI.instance().isEffectPowderActive(world, first.getPosition(), NAME))
                return;

            Optional<EntityAnimal> secondOptional = animals.stream()
                    .filter(e -> e != first && !e.isInLove() && !e.isChild())
                    .min(Comparator.comparingDouble(e -> e.getDistanceSq(first)));
            if (!secondOptional.isPresent())
                return;
            EntityAnimal second = secondOptional.get();
            if (second.getDistanceSq(first) > 5 * 5)
                return;

            this.setInLove(first);
            this.setInLove(second);

            BlockPos closestSpot = IAuraChunk.getHighestSpot(world, first.getPosition(), 35, pos);
            IAuraChunk.getAuraChunk(world, closestSpot).drainAura(closestSpot, 3500);
        }
    }

    private void setInLove(EntityAnimal animal) {
        animal.setInLove(null);
        for (int j = 0; j < 7; j++)
            animal.world.spawnParticle(EnumParticleTypes.HEART,
                    (animal.posX + (double) (animal.world.rand.nextFloat() * animal.width * 2.0F)) - animal.width,
                    animal.posY + 0.5D + (double) (animal.world.rand.nextFloat() * animal.height),
                    (animal.posZ + (double) (animal.world.rand.nextFloat() * animal.width * 2.0F)) - animal.width,
                    animal.world.rand.nextGaussian() * 0.02D,
                    animal.world.rand.nextGaussian() * 0.02D,
                    animal.world.rand.nextGaussian() * 0.02D);
    }

    @Override
    public boolean appliesHere(Chunk chunk, IAuraChunk auraChunk, IAuraType type) {
        return ModConfig.enabledFeatures.animalEffect;
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }
}
