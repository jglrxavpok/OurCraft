package org.craft.world;

import java.util.*;

import com.google.common.base.Optional;

import org.craft.blocks.*;
import org.craft.blocks.states.*;
import org.craft.entity.*;
import org.craft.maths.*;
import org.craft.spongeimpl.block.*;
import org.craft.utils.*;
import org.craft.utils.CollisionInfos.CollisionType;
import org.spongepowered.api.math.*;
import org.spongepowered.api.world.biome.*;

public class World implements org.spongepowered.api.world.World
{

    private LinkedList<Entity> entities;
    private ArrayList<Entity>  spawingQueue;
    private ChunkProvider      chunkProvider;
    private WorldGenerator     generator;
    private String             name;
    private WorldLoader        worldLoader;
    public boolean             isRemote;
    private Random             rng;

    public World(String name, ChunkProvider prov, WorldGenerator generator, WorldLoader worldLoader)
    {
        this.rng = new Random(generator.getSeed());
        this.worldLoader = worldLoader;
        this.name = name;
        this.generator = generator;
        this.chunkProvider = prov;
        spawingQueue = new ArrayList<Entity>();
        entities = new LinkedList<Entity>();
    }

    public void update(double delta)
    {
        while(!spawingQueue.isEmpty())
            entities.add(spawingQueue.remove(0));
        ArrayList<Entity> deadEntities = new ArrayList<Entity>();
        for(Entity e : entities)
        {
            Chunk c = getChunk((int) e.getX(), (int) e.getY(), (int) e.getZ());
            if(c != null)
                c.update();
            e.update();

            if(e.isDead())
            {
                deadEntities.add(e);
            }
        }
        entities.removeAll(deadEntities);
    }

    /**
     * Returns chunk from given coords in world space
     */
    public Chunk getChunk(int x, int y, int z)
    {
        return chunkProvider.get(this, (int) Math.floor((float) x / 16f), (int) Math.floor((float) y / 16f), (int) Math.floor((float) z / 16f));
    }

    public void addChunk(Chunk c)
    {
        chunkProvider.addChunk(this, c);
    }

    /**
     * Returns block next to given coords and given side
     */
    public Block getBlockNextTo(int x, int y, int z, EnumSide side)
    {
        return getBlockAt(x + side.getTranslationX(), y + side.getTranslationY(), z + side.getTranslationZ());
    }

    /**
     * Returns block at given coords
     */
    public Block getBlockAt(int x, int y, int z)
    {
        Chunk c = getChunk(x, y, z);
        if(c == null)
            return Blocks.air;
        return c.getBlock(this, x, y, z);
    }

    public IBlockStateValue getBlockState(int x, int y, int z, BlockState state)
    {
        Chunk c = getChunk(x, y, z);
        if(c == null)
            return null;
        return c.getBlockState(x, y, z, state);
    }

    /**
     * Sets block state at given coords
     */
    public void setBlockState(int x, int y, int z, BlockState state, IBlockStateValue value)
    {
        setBlockState(x, y, z, state, value, true);
    }

    public void setBlockState(int x, int y, int z, BlockState state, IBlockStateValue value, boolean notify)
    {
        Chunk c = getChunk(x, y, z);
        if(c == null)
            return;
        c.setBlockState(x, y, z, state, value);
        if(notify)
            updateBlockAndNeighbors(x, y, z);
    }

    /**
     * Sets block at given coords
     */
    public void setBlock(int x, int y, int z, Block block)
    {
        Chunk c = getChunk(x, y, z);
        if(c == null)
        {
            return;
        }
        c.setBlock(this, x, y, z, block);
        updateBlockAndNeighbors(x, y, z);
        c.markDirty();
    }

    /**
     * Spawns a new entity in world
     */
    public void spawn(Entity e)
    {
        Log.message("added " + e.getClass());
        this.spawingQueue.add(e);
    }

    /**
     * Performs a raycast from {@code sender} to get object in front of it. Results are saved into {@code infos}
     */
    public void performRayCast(Entity sender, CollisionInfos infos, float maxDist)
    {
        float maxReachedDist = maxDist;
        float size = 0.45f;
        Vector3 origin = Vector3.get(sender.posX, sender.posY + sender.getEyeOffset() - size / 2f, sender.posZ);
        Vector3 pos = origin;
        Vector3 ray = sender.getQuaternionRotation().getForward();

        int x = 0;
        int y = 0;
        int z = 0;
        float step = 0.005f;
        AABB rayBB = new AABB(Vector3.get(0, 0, 0), Vector3.get(size, size, size));
        Vector3 blockPos = Vector3.get(x, y, z);
        for(float dist = 0f; dist <= maxDist + step; dist += step)
        {
            x = (int) Math.round(pos.getX());
            y = (int) Math.round(pos.getY());
            z = (int) Math.round(pos.getZ());
            Block b = getBlockAt(x, y, z);
            if(b != null)
            {
                AABB blockBB = b.getSelectionBox(this, x, y, z);
                if(blockBB != null)
                {
                    if(blockBB.intersectAABB(rayBB.translate(pos)))
                    {
                        infos.x = x;
                        infos.y = y;
                        infos.z = z;
                        infos.type = CollisionType.BLOCK;
                        infos.side = EnumSide.BOTTOM;
                        infos.value = b;
                        blockPos = Vector3.get(x + 0.5f, y + 0.5f, z + 0.5f);

                        Vector3 diff = blockPos.sub(pos.add(0.5f));

                        float absx = Math.abs(diff.getX());
                        float absy = Math.abs(diff.getY());
                        float absz = Math.abs(diff.getZ());

                        if(absx > absy && absx > absz)
                        {
                            if(diff.getX() > 0)
                                infos.side = EnumSide.WEST;
                            else
                                infos.side = EnumSide.EAST;
                        }
                        if(absy > absx && absy > absz)
                        {
                            if(diff.getY() > 0)
                                infos.side = EnumSide.BOTTOM;
                            else
                                infos.side = EnumSide.TOP;
                        }
                        if(absz > absy && absz > absx)
                        {
                            if(diff.getZ() > 0)
                                infos.side = EnumSide.NORTH;
                            else
                                infos.side = EnumSide.SOUTH;
                        }
                    }
                }
            }
            pos = origin.add(ray.mul((maxDist + step) - dist));
            for(Entity e : entities)
            {
                if(e.getBoundingBox().intersectAABB(rayBB))
                {
                    infos.type = CollisionType.ENTITY;
                    infos.value = e;
                    infos.x = e.posX;
                    infos.y = e.posY;
                    infos.z = e.posZ;
                    infos.distance = maxReachedDist;
                }
            }
        }
        origin.dispose();
        pos.dispose();
        ray.dispose();
        rayBB.dispose();
    }

    public WorldGenerator getGenerator()
    {
        return generator;
    }

    public ChunkProvider getChunkProvider()
    {
        return chunkProvider;
    }

    /**
     * Returns true if given block can see the sky
     */
    public boolean canBlockSeeSky(int x, int y, int z)
    {
        for(int y1 = y + 1; y1 < 256; y1++ )
        {
            if(!getBlockAt(x, y1, z).letLightGoThrough())
            {
                return false;
            }
        }
        return true;
    }

    public List<Entity> getEntitiesList()
    {
        return entities;
    }

    public void clearStates(int x, int y, int z)
    {
        Chunk c = getChunk(x, y, z);
        if(c == null)
        {
            return;
        }
        c.clearStates(x, y, z);
    }

    /**
     * Returns whether a chunk exists at chunk coordinates x, y, z
     */
    public boolean doesChunkExists(int x, int y, int z)
    {
        return this.chunkProvider.doesChunkExists(this, x, y, z);
    }

    public void createChunk(final int x, final int y, final int z)
    {
        final World w = this;
        if(doesChunkExists(x, y, z))
            Log.error("Cannot generate a chunk on a chunk on " + x + ", " + y + ", " + z);
        else
        {
            chunkProvider.create(w, x, y, z);
        }
    }

    public BlockStatesObject getBlockStates(int x, int y, int z)
    {
        Chunk c = getChunk(x, y, z);
        if(c != null)
        {
            return c.getBlockStates(x, y, z);
        }
        return null;
    }

    public boolean updateBlock(int x, int y, int z)
    {
        Block b = getBlockAt(x, y, z);
        if(b != null)
        {
            b.onBlockUpdate(this, x, y, z);
            return true;
        }
        return false;
    }

    protected boolean updateBlockFromNeighbor(int x, int y, int z)
    {
        Block b = getBlockAt(x, y, z);
        if(b != null)
        {
            b.onBlockUpdateFromNeighbor(this, x, y, z);
            return true;
        }
        return false;
    }

    public void updateBlockAndNeighbors(int x, int y, int z)
    {
        updateBlock(x, y, z);
        updateBlockFromNeighbor(x, y, z + 1);
        updateBlockFromNeighbor(x, y, z - 1);
        updateBlockFromNeighbor(x, y + 1, z);
        updateBlockFromNeighbor(x, y - 1, z);
        updateBlockFromNeighbor(x + 1, y, z);
        updateBlockFromNeighbor(x - 1, y, z);
    }

    public int getDirectElectricPowerAt(int x, int y, int z)
    {
        Chunk c = getChunk(x, y, z);
        if(c == null)
            return 0;
        int maxPower = 0;
        for(EnumSide side : EnumSide.values())
        {
            IBlockStateValue value = getBlockState(x + side.getTranslationX(), y + side.getTranslationY(), z + side.getTranslationZ(), BlockStates.electricPower);
            if(value != null && value instanceof EnumPowerStates)
            {
                EnumPowerStates power = (EnumPowerStates) value;
                if(power.powerValue() == 15)
                    return 15;
                else if(power.powerValue() > maxPower)
                    maxPower = power.powerValue();
            }
        }
        return maxPower;
    }

    // -------------------------------------------------
    // START OF SPONGE IMPLEMENTATION
    // -------------------------------------------------

    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUID getUniqueID()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public org.spongepowered.api.world.Chunk getChunk(Vector2i position)
    {
        return getChunk(position.getX(), 0, position.getY());
    }

    @Override
    public org.spongepowered.api.world.Chunk loadChunk(Vector2i position, boolean shouldGenerate)
    {
        if(shouldGenerate)
            return chunkProvider.getOrCreate(this, position.getX(), 0, position.getY());
        else
            return getChunk(position);
    }

    @Override
    public org.spongepowered.api.world.Chunk loadChunk(Vector2i position)
    {
        return loadChunk(position, false);
    }

    @Override
    public org.spongepowered.api.block.Block getBlock(Vector3d position)
    {
        return new SpongeBlock(getBlockAt((int) position.getX(), (int) position.getY(), (int) position.getZ()), (int) position.getX(), (int) position.getY(), (int) position.getZ(), this);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(org.spongepowered.api.entity.EntityType type, Vector3d position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(org.spongepowered.api.entity.EntitySnapshot snapshot, Vector3d position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Biome getBiome(Vector3d position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public org.spongepowered.api.block.Block getBlock(int x, int y, int z)
    {
        return new SpongeBlock(getBlockAt(x, y, z), x, y, z, this);
    }

    public void setSeed(long seed)
    {
        this.getGenerator().setSeed(seed);
    }

    public long getSeed()
    {
        return getGenerator().getSeed();
    }

    public WorldLoader getLoader()
    {
        return worldLoader;
    }

    public Random getRNG()
    {
        return rng;
    }
}
