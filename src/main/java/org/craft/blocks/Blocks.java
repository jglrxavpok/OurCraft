package org.craft.blocks;

import java.util.*;
import java.util.Map.Entry;

public final class Blocks
{

    public static Block                        dirt;
    public static Block                        grass;
    public static Block                        air;
    public static Block                        bedrock;
    public static Block                        stone;
    public static Block                        log;
    public static Block                        leaves;
    public static Block                        glass;
    public static final HashMap<String, Block> BLOCK_REGISTRY = new HashMap<String, Block>();
    private static ArrayList<Block> blockByID; 
    public static void init()
    {
        blockByID = new ArrayList<Block>();
        register(air = new BlockAir());
        register(dirt = new Block("dirt"));
        register(grass = new BlockGrass("grass"));
        register(bedrock = new Block("bedrock"));
        register(stone = new Block("stone"));
        register(log = new BlockLog("log"));
        register(leaves = new BlockTransparent("leaves"));
        register(glass = new BlockTransparent("glass"));
        
        for(short i = 0; i < blockByID.size(); i++)
        {
            Block b = blockByID.get(i);
            if(b !=null) b.setUniqueID(i);
        }
    }

    /**
     * Registers a block into the BLOCK_REGISTRY field
     */
    public static void register(Block block)
    {
        if(BLOCK_REGISTRY.containsKey(block.getID()))
        {
            throw new IllegalArgumentException("Id " + block.getID() + " is already used by " + BLOCK_REGISTRY.get(block.getID()) + " when trying to add " + block);
        }
        BLOCK_REGISTRY.put(block.getID(), block);
        blockByID.add(block);
    }

    /**
     * Returns the block in BLOCK_REGISTRY with given id
     */
    public static Block get(String string)
    {
        if(string == null)
            return air;
        return BLOCK_REGISTRY.get(string);
    }
    
    public static Block getByID(int id)
    {
        Block b = blockByID.get(id);
        if(b == null) b = air;
        return b;
    }
}
