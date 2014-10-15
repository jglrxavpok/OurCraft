package org.craft.client.render.blocks;

import org.craft.blocks.*;
import org.craft.client.render.*;
import org.craft.maths.*;
import org.craft.world.*;

public abstract class AbstractBlockRenderer
{

    /**
     * Renders given block at given coords
     */
    public abstract void render(RenderEngine engine, OffsettedOpenGLBuffer buffer, World w, Block b, int x, int y, int z);

    public void renderFace(OffsettedOpenGLBuffer buffer, World w, Block b, int x, int y, int z, TextureIcon icon, Vector3 startPos, Vector3 size)
    {
        renderFace(buffer, w, b, x, y, z, icon, startPos, size, false);
    }

    public void renderFace(OffsettedOpenGLBuffer buffer, World w, Block b, int x, int y, int z, TextureIcon icon, Vector3 startPos, Vector3 size, boolean flipZ)
    {
        renderFace(buffer, w, b, x, y, z, icon, startPos, size, flipZ, Vector2.NULL, Vector2.get(1, 1));
    }

    public void renderFace(OffsettedOpenGLBuffer buffer, World w, Block b, int x, int y, int z, TextureIcon icon, Vector3 startPos, Vector3 size, boolean flipZ, Vector2 minUV, Vector2 maxUV)
    {
        Chunk chunk = w.getChunk(x, y, z);
        if(chunk == null)
            return;
        float lightValue = chunk.getLightValue(w, x, y, z);
        float startX = startPos.getX();
        float startY = startPos.getY();
        float startZ = startPos.getZ();

        float width = size.getX();
        float height = size.getY();
        float depth = size.getZ();
        float deltaX = icon.getMaxU() - icon.getMinU();
        float deltaY = icon.getMaxV() - icon.getMinV();
        float minU = icon.getMinU() + minUV.getX() * deltaX;
        float minV = icon.getMinV() + minUV.getY() * deltaY;
        float maxU = icon.getMinU() + maxUV.getX() * deltaX;
        float maxV = icon.getMinV() + maxUV.getY() * deltaY;
        if(flipZ)
        {
            buffer.addVertex(Vertex.get(Vector3.get(x + startX, height + y + startY, z + startZ), Vector2.get(minU, minV), Vector3.get(lightValue, lightValue, lightValue))); // 2
            buffer.addVertex(Vertex.get(Vector3.get(x + startX, y + startY, depth + z + startZ), Vector2.get(minU, maxV), Vector3.get(lightValue, lightValue, lightValue))); // 0
            buffer.addVertex(Vertex.get(Vector3.get(width + x + startX, height + y + startY, depth + z + startZ), Vector2.get(maxU, maxV), Vector3.get(lightValue, lightValue, lightValue))); // 4
            buffer.addVertex(Vertex.get(Vector3.get(width + x + startX, y + startY, z + startZ), Vector2.get(maxU, minV), Vector3.get(lightValue, lightValue, lightValue))); // 6   
        }
        else
        {
            buffer.addVertex(Vertex.get(Vector3.get(x + startX, y + startY, z + startZ), Vector2.get(minU, maxV), Vector3.get(lightValue, lightValue, lightValue))); // 0
            buffer.addVertex(Vertex.get(Vector3.get(x + startX, height + y + startY, z + startZ), Vector2.get(minU, minV), Vector3.get(lightValue, lightValue, lightValue))); // 2
            buffer.addVertex(Vertex.get(Vector3.get(width + x + startX, height + y + startY, depth + z + startZ), Vector2.get(maxU, minV), Vector3.get(lightValue, lightValue, lightValue))); // 4
            buffer.addVertex(Vertex.get(Vector3.get(width + x + startX, y + startY, depth + z + startZ), Vector2.get(maxU, maxV), Vector3.get(lightValue, lightValue, lightValue))); // 6
        }
        buffer.addIndex(0);
        buffer.addIndex(1);
        buffer.addIndex(2);
        buffer.addIndex(2);
        buffer.addIndex(3);
        buffer.addIndex(0);
        buffer.setOffsetToEnd();
    }
}
