package de.ellpeck.naturesaura.compat.crafttweaker;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.utils.BaseMapAddition;
import com.blamejared.mtlib.utils.BaseMapRemoval;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import de.ellpeck.naturesaura.Helper;
import de.ellpeck.naturesaura.NaturesAura;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.recipes.AltarRecipe;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ZenRegister
@ZenClass("mods." + NaturesAura.MOD_ID + ".Altar")
public final class AltarTweaker {

    @ZenMethod
    public static void addRecipe(String name, IItemStack input, IItemStack output, IItemStack catalyst, int aura, int time) {
        CraftTweakerCompat.SCHEDULED_ACTIONS.add(() -> {
            Block block = Block.getBlockFromItem(InputHelper.toStack(catalyst).getItem());
            ResourceLocation res = new ResourceLocation(name);
            return new Add(Collections.singletonMap(res, new AltarRecipe(res,
                    InputHelper.toStack(input),
                    InputHelper.toStack(output),
                    block == Blocks.AIR ? null : block,
                    aura, time)));
        });
    }

    @ZenMethod
    public static void removeRecipe(IItemStack output) {
        CraftTweakerCompat.SCHEDULED_ACTIONS.add(() -> {
            Map<ResourceLocation, AltarRecipe> recipes = new HashMap<>();
            for (AltarRecipe recipe : NaturesAuraAPI.ALTAR_RECIPES.values())
                if (Helper.areItemsEqual(recipe.output, InputHelper.toStack(output), true))
                    recipes.put(recipe.name, recipe);
            return new Remove(recipes);
        });
    }

    private static class Add extends BaseMapAddition<ResourceLocation, AltarRecipe> {

        protected Add(Map<ResourceLocation, AltarRecipe> map) {
            super("Natural Altar", NaturesAuraAPI.ALTAR_RECIPES, map);
        }

        @Override
        protected String getRecipeInfo(Map.Entry<ResourceLocation, AltarRecipe> recipe) {
            return LogHelper.getStackDescription(recipe.getValue().output);
        }
    }

    private static class Remove extends BaseMapRemoval<ResourceLocation, AltarRecipe> {

        protected Remove(Map<ResourceLocation, AltarRecipe> map) {
            super("Natural Altar", NaturesAuraAPI.ALTAR_RECIPES, map);
        }

        @Override
        protected String getRecipeInfo(Map.Entry<ResourceLocation, AltarRecipe> recipe) {
            return LogHelper.getStackDescription(recipe.getValue().output);
        }
    }
}
