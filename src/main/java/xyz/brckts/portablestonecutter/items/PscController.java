package xyz.brckts.portablestonecutter.items;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xyz.brckts.portablestonecutter.PortableStonecutter;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PscController {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if(world.isRemote()) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        ItemStack is = event.getItemStack();

        if(!(is.getItem() instanceof PortableStonecutterItem || is.getItem() instanceof EnderPortableStonecutterItem)) {
            return;
        }

        CompoundNBT nbt = is.getTag();

        if(nbt == null || !nbt.contains("item") || !nbt.contains("recipeId")) {
            is.setTag(null);
            return;
        }

        int recipeId = nbt.getInt("recipeId");
        ResourceLocation inputItemRL = new ResourceLocation(nbt.getString("item"));

        if(!GameRegistry.findRegistry(Item.class).containsKey(inputItemRL)) {
            is.setTag(null);
            return;
        }

        Item inputItem = GameRegistry.findRegistry(Item.class).getValue(inputItemRL);
        IInventory inputInventory = new Inventory(1);
        inputInventory.setInventorySlotContents(0, new ItemStack(inputItem));

        if(!world.getBlockState(pos).getBlock().equals(Block.getBlockFromItem(inputItem))) {
            return;
        }

        List<StonecuttingRecipe> recipes = world.getRecipeManager().getRecipes(IRecipeType.STONECUTTING, inputInventory, world);
        StonecuttingRecipe recipe = recipes.get(recipeId);
        Block outputBlock = Block.getBlockFromItem(recipe.getRecipeOutput().getItem());
        int outputCnt = recipe.getRecipeOutput().getCount();

        if(recipeId >= recipes.size()) {
            is.setTag(null);
            return;
        }

        if(outputBlock == Blocks.AIR) {
            return;
        }

        BlockRayTraceResult blockRayTraceResult = rayTrace(world, player, RayTraceContext.FluidMode.ANY);

        ArrayList<BlockPos> toReplace = new ArrayList<>();

        toReplace.add(pos);

        if (is.getItem() instanceof EnderPortableStonecutterItem) {
            Direction.Axis axis = blockRayTraceResult.getFace().getAxis();

            switch (axis) {
                case X:
                    PortableStonecutter.LOGGER.debug("AXIS IS X");
                    toReplace.add(pos.up());
                    toReplace.add(pos.down());
                    toReplace.add(pos.north());
                    toReplace.add(pos.south());
                    toReplace.add(pos.north().up());
                    toReplace.add(pos.north().down());
                    toReplace.add((pos.south().up()));
                    toReplace.add((pos.south().down()));
                    break;
                case Y:
                    PortableStonecutter.LOGGER.debug("AXIS IS Y");
                    toReplace.add(pos.north());
                    toReplace.add(pos.south());
                    toReplace.add(pos.east());
                    toReplace.add(pos.west());
                    toReplace.add(pos.north().east());
                    toReplace.add(pos.north().west());
                    toReplace.add(pos.south().east());
                    toReplace.add(pos.south().west());
                    break;
                case Z:
                    PortableStonecutter.LOGGER.debug("AXIS IS Z");
                    toReplace.add(pos.up());
                    toReplace.add(pos.down());
                    toReplace.add(pos.east());
                    toReplace.add(pos.west());
                    toReplace.add(pos.east().up());
                    toReplace.add(pos.east().down());
                    toReplace.add((pos.west().up()));
                    toReplace.add((pos.west().down()));
                    break;
                default:
                    break;
            }
        }

        for (BlockPos blockPos : toReplace) {

            if(!world.getBlockState(blockPos).getBlock().equals(Block.getBlockFromItem(inputItem))) {
                continue;
            }

            world.setBlockState(blockPos, outputBlock.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, blockRayTraceResult))));
            world.addParticle(ParticleTypes.PORTAL, blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), 0, 0, 0);

            if (outputCnt > 1) {
                player.dropItem(new ItemStack(recipe.getRecipeOutput().getItem(), outputCnt - 1), true, true);
            }
        }
    }

    public static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        float f = player.rotationPitch;
        float f1 = player.rotationYaw;
        Vector3d vector3d = player.getEyePosition(1.0F);
        float f2 = MathHelper.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * ((float)Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();;
        Vector3d vector3d1 = vector3d.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
        return worldIn.rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.OUTLINE, fluidMode, player));
    }
    public static void clearTags(PlayerEntity player) {
        ItemStack stack = player.getHeldItemMainhand();
        stack.setTag(null);
    }
}
