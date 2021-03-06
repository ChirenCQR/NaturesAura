package de.ellpeck.naturesaura.api.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class Matcher {

    private final IBlockState defaultState;
    private final ICheck check;

    public Matcher(IBlockState defaultState, ICheck check) {
        this.defaultState = defaultState;
        this.check = check;
    }

    public IBlockState getDefaultState() {
        return this.defaultState;
    }

    public ICheck getCheck() {
        return this.check;
    }

    public static Matcher wildcard() {
        return new Matcher(Blocks.AIR.getDefaultState(), null);
    }

    public static Matcher oreDict(Block defaultBlock, String name) {
        return new Matcher(defaultBlock.getDefaultState(), new ICheck() {
            private List<IBlockState> states;

            @Override
            public boolean matches(World world, BlockPos start, BlockPos offset, BlockPos pos, IBlockState state, char c) {
                if (this.states == null) {
                    this.states = new ArrayList<>();
                    for (ItemStack stack : OreDictionary.getOres(name)) {
                        Block block = Block.getBlockFromItem(stack.getItem());
                        if (block != null && block != Blocks.AIR) {
                            int damage = stack.getItemDamage();
                            if (damage == OreDictionary.WILDCARD_VALUE)
                                this.states.addAll(block.getBlockState().getValidStates());
                            else
                                this.states.add(block.getStateFromMeta(damage));
                        }
                    }
                }

                return this.states.isEmpty() || this.states.contains(state);
            }
        });
    }

    public interface ICheck {
        boolean matches(World world, BlockPos start, BlockPos offset, BlockPos pos, IBlockState state, char c);
    }
}
