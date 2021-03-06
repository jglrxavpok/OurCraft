package org.craft.client.render.blocks;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.*;

import org.craft.blocks.*;
import org.craft.client.*;
import org.craft.client.models.*;
import org.craft.client.render.*;
import org.craft.maths.*;
import org.craft.utils.*;
import org.craft.world.*;

public class BlockModelRenderer extends AbstractBlockRenderer
{

    private HashMap<BlockVariant, HashMap<String, TextureIcon>> icons;
    private List<BlockVariant>                                  blockVariants;
    private static Quaternion                                   rotationQuaternion;

    /**
     * Creates a new renderer for given block variants
     */
    public BlockModelRenderer(List<BlockVariant> list)
    {
        this.blockVariants = list;
        icons = Maps.newHashMap();
        for(BlockVariant v : list)
            icons.put(v, new HashMap<String, TextureIcon>());
        if(rotationQuaternion == null)
            rotationQuaternion = new Quaternion();
    }

    @Override
    public void render(RenderEngine engine, OffsettedOpenGLBuffer buffer, World w, Block b, int x, int y, int z)
    {
        if(!b.shouldRender())
            return;
        Chunk chunk = w.getChunk(x, y, z);
        if(chunk == null)
            return;
        BlockVariant variant = getVariant(w, b, x, y, z);

        if(variant == null)
            return;
        BlockModel blockModel = variant.getModels().get(w.getRNG().nextInt(variant.getModels().size())); // TODO: random model ?
        float lightValue = chunk.getLightValue(w, x, y, z);
        for(int i = 0; i < blockModel.getElementsCount(); i++ )
        {
            BlockElement element = blockModel.getElement(i);
            if(element.hasRotation())
            {
                Vector3 axis = Vector3.xAxis;
                if(element.getRotationAxis() == null)
                    ;
                else if(element.getRotationAxis().equalsIgnoreCase("y"))
                    axis = Vector3.yAxis;
                else if(element.getRotationAxis().equalsIgnoreCase("z"))
                    axis = Vector3.zAxis;
                rotationQuaternion.init(axis, (float) Math.toRadians(element.getRotationAngle()));
            }
            Set<Entry<String, BlockFace>> entries = element.getFaces().entrySet();
            Vector3 startPos = element.getFrom();
            Vector3 size = element.getTo().sub(startPos);
            for(Entry<String, BlockFace> entry : entries)
            {
                Vector3 faceStart = Vector3.NULL;
                Vector3 faceSize = Vector3.NULL;
                TextureIcon icon = getTexture(blockModel, variant, entry.getValue().getTexture());
                boolean flip = false;
                EnumSide cullface = EnumSide.fromString(entry.getValue().getCullface());
                if(cullface != EnumSide.UNDEFINED)
                {
                    Block next = w.getBlockNextTo(x, y, z, cullface);
                    if(next.isSideOpaque(w, x, y, z, cullface.opposite()))
                    {
                        continue;
                    }
                }
                if(entry.getKey().equals("up"))
                {
                    faceStart = Vector3.get(startPos.getX(), startPos.getY() + size.getY(), startPos.getZ());
                    faceSize = Vector3.get(size.getX(), 0, size.getZ());
                    flip = true;
                }
                else if(entry.getKey().equals("down"))
                {
                    faceStart = Vector3.get(startPos.getX(), startPos.getY(), startPos.getZ());
                    faceSize = Vector3.get(size.getX(), 0, size.getZ());
                    flip = true;
                }

                else if(entry.getKey().equals("west"))
                {
                    faceStart = Vector3.get(startPos.getX(), startPos.getY(), startPos.getZ());
                    faceSize = Vector3.get(0, size.getY(), size.getZ());
                }
                else if(entry.getKey().equals("east"))
                {
                    faceStart = Vector3.get(startPos.getX() + size.getX(), startPos.getY(), startPos.getZ());
                    faceSize = Vector3.get(0, size.getY(), size.getZ());
                }

                else if(entry.getKey().equals("north"))
                {
                    faceStart = Vector3.get(startPos.getX(), startPos.getY(), startPos.getZ());
                    faceSize = Vector3.get(size.getX(), size.getY(), 0);
                }
                else if(entry.getKey().equals("south"))
                {
                    faceStart = Vector3.get(startPos.getX(), startPos.getY(), startPos.getZ() + size.getZ());
                    faceSize = Vector3.get(size.getX(), size.getY(), 0);
                }

                else
                {
                    continue;
                }
                renderFace(lightValue, buffer, w, b, x, y, z, icon, faceStart, faceSize, flip, entry.getValue().getMinUV(), entry.getValue().getMaxUV(), element.getRotationOrigin(), rotationQuaternion, element.shouldRescale());
                faceSize.dispose();
                faceStart.dispose();
            }
            size.dispose();
        }
    }

    /**
     * Returns most revelant variant depending on block states values at (x,y,z)
     */
    private BlockVariant getVariant(World w, Block b, int x, int y, int z)
    {
        BlockVariant variant = null;
        for(BlockVariant v : blockVariants)
        {
            if(v.getBlockState() == null && variant == null)
                variant = v;
            if(v.getBlockState() != null)
                if(w.getBlockState(x, y, z, v.getBlockState()) == v.getBlockStateValue())
                {
                    variant = v;
                }
        }
        return variant;
    }

    /**
     * Gets TextureIcon from texture variable found in json model file
     */
    private TextureIcon getTexture(BlockModel blockModel, BlockVariant variant, String texture)
    {
        if(texture == null)
            return null;
        if(!icons.get(variant).containsKey(texture))
        {
            if(texture.startsWith("#"))
            {
                TextureIcon icon = getTexture(blockModel, variant, blockModel.getTexturePath(texture.substring(1)));
                icons.get(variant).put(texture, icon);
            }
            else
            {
                TextureMap blockMap = (TextureMap) OurCraft.getOurCraft().getRenderEngine().getByLocation(RenderBlocks.blockMapLoc);
                icons.get(variant).put(texture, blockMap.get(texture + ".png"));
            }
        }
        return icons.get(variant).get(texture);
    }

    @Override
    public boolean shouldRenderInPass(EnumRenderPass currentPass, World w, Block b, int x, int y, int z)
    {
        BlockVariant variant = getVariant(w, b, x, y, z);
        if(variant == null)
            return false;
        return currentPass == variant.getPass();
    }
}
