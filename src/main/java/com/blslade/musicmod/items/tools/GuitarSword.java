package com.blslade.musicmod.items.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.Nullable;

import com.blslade.musicmod.sounds.ModSounds;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuitarSword extends Item
{
	public static final String name = "guitar_sword";
	
	public GuitarSword()
	{
		this.maxStackSize = 1;
		this.setCreativeTab(CreativeTabs.COMBAT);
		this.attackDamage = 6;
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		this.initProperties();
		
		ResourceLocation loc = new ResourceLocation("musicmod:sounds/jump_rope_blue_october.data.txt");
		try
		{
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			Gson gson = new Gson();
			JsonArray arr = gson.fromJson(reader, JsonArray.class);
			int len = arr.size();
			this.musicVolumes = new float[len];
			for (int q = 0; q < len; q++)
			{
				this.musicVolumes[q] = arr.get(q).getAsFloat();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			this.musicVolumes = new float[] { 0 };
		}
	}
	
	private float[] musicVolumes;
	
	private void initProperties()
	{
        this.addPropertyOverride(new ResourceLocation("volume"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
            	return GuitarSword.this.getCurrentMusicVolume();
            }
        });
	}

    private final float attackDamage;

    public float getDamageVsEntity()
    {
    	float volume = this.getCurrentMusicVolume();
        return this.attackDamage * ((volume * 2) + 0.5f);
    }

    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        Block block = state.getBlock();

        if (block == Blocks.WEB)
        {
            return 15.0F;
        }
        else
        {
            Material material = state.getMaterial();
            return material != Material.PLANTS && material != Material.VINE && material != Material.CORAL && material != Material.LEAVES && material != Material.GOURD ? 1.0F : 1.5F;
        }
    }
    
    public boolean canHarvestBlock(IBlockState blockIn)
    {
        return blockIn.getBlock() == Blocks.WEB;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean isFull3D()
    {
        return true;
    }

    public int getItemEnchantability()
    {
        return 22;
    }

    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot)
    {
        @SuppressWarnings("deprecation")
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.attackDamage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }

        return multimap;
    }
    
    public static long lastPlayTime = 0;
    private float getCurrentMusicVolume()
    {
    	long systemTime = Minecraft.getSystemTime();
    	int currentFrame = (int)Math.floor(((float)(systemTime - GuitarSword.lastPlayTime) / 1000) * 30);
    	if (currentFrame >= musicVolumes.length) return 0;
    	return musicVolumes[currentFrame];
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote)
		{
			ModSounds.playSoundFromServer(world, player.posX, player.posY, player.posZ, ModSounds.soundJumpRope, SoundCategory.MUSIC, 1.0f, 1.0f, false, 32.0f);
			GuitarSword.lastPlayTime = Minecraft.getSystemTime();
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
