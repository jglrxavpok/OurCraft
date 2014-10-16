package org.craft.client.render;

import static org.lwjgl.opengl.GL15.*;
import gnu.trove.*;

import java.nio.*;
import java.util.*;

import org.craft.maths.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

public class OpenGLBuffer
{

    private int               vboID;
    private int               iboID;
    private ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    private TIntArrayList     indices  = new TIntArrayList();
    private int               indicesLength;
    private int               verticesLength;

    /**
     * Creates an empty OpenGLBuffer instance
     */
    public OpenGLBuffer()
    {
        vboID = glGenBuffers();
        iboID = glGenBuffers();
    }

    /**
     * Creates an OpenGLBuffer instance with given vertices and indices
     */
    public OpenGLBuffer(List<Vertex> vertices, List<Integer> indices)
    {
        this();
        this.setVertices(vertices);
        this.setIndices(indices);
    }

    /**
     * Uploads data to OpenGL
     */
    public void upload()
    {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(verticesLength * Vertex.SIZE_IN_FLOATS);
        for(int i = 0; i < verticesLength; i++ )
        {
            Vertex vertex = vertices.get(i);
            verticesBuffer.put(vertex.getPos().getX());
            verticesBuffer.put(vertex.getPos().getY());
            verticesBuffer.put(vertex.getPos().getZ());

            verticesBuffer.put(vertex.getTexCoords().getX());
            verticesBuffer.put(vertex.getTexCoords().getY());

            verticesBuffer.put(vertex.getColor().getX());
            verticesBuffer.put(vertex.getColor().getY());
            verticesBuffer.put(vertex.getColor().getZ());
        }
        verticesBuffer.flip();
        GL15.glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboID);
        indicesLength = indices.size();
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesLength);
        for(int i = 0; i < indicesLength; i++ )
        {
            int indice = indices.get(i);
            indicesBuffer.put(indice);
        }
        indicesBuffer.flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void setIndices(List<Integer> newIndices)
    {
        indices.clear();
        for(int index : newIndices)
            addIndex(index);
    }

    public void setVertices(List<Vertex> newVertices)
    {
        setVertices(newVertices, true);
    }

    public void setVertices(List<Vertex> newVertices, boolean disposePrevious)
    {
        if(disposePrevious)
            clearAndDisposeVertices();
        else
        {
            verticesLength = 0;
        }
        for(Vertex v : newVertices)
            addVertex(v);
    }

    /**
     * Adds a new vertex at the end of vertices' list
     */
    public void addVertex(Vertex v)
    {
        if(vertices.size() <= verticesLength)
        {
            vertices.add(v);
        }
        else
        {
            vertices.set(verticesLength, v);
        }
        verticesLength++ ;
    }

    /**
     * Adds a new vertex at the end of indices' list
     */
    public void addIndex(int i)
    {
        indices.add(i);
    }

    public int getVboID()
    {
        return vboID;
    }

    public int getIboID()
    {
        return iboID;
    }

    public int getIndicesCount()
    {
        return indices.size();
    }

    /**
     * Clear indices and vertices lists and update OpenGL Data
     */
    public void clear()
    {
        indices.clear();
        clearAndDisposeVertices();
        upload();
    }

    public void dispose()
    {
        clearAndDisposeVertices();
        glDeleteBuffers(iboID);
        glDeleteBuffers(vboID);
    }

    /**
     * Disposes and clear vertices list.<br/>
     * Usage: Buffers than require to be uploaded only one time and rendered multiple times.
     */
    public void clearAndDisposeVertices()
    {
        verticesLength = 0;
        for(Vertex v : vertices)
        {
            v.dispose();
        }
    }

    public void setToCube()
    {
        clearAndDisposeVertices();
        indices.clear();
        int index = 0;
        addVertex(Vertex.get(Vector3.get(0, 0, 0), Vector2.get(0, 0)));
        addVertex(Vertex.get(Vector3.get(1, 0, 0), Vector2.get(1, 0)));
        addVertex(Vertex.get(Vector3.get(1, 1, 0), Vector2.get(1, 1)));
        addVertex(Vertex.get(Vector3.get(0, 1, 0), Vector2.get(0, 0)));
        addIndex(index + 0);
        addIndex(index + 1);
        addIndex(index + 2);
        addIndex(index + 2);
        addIndex(index + 3);
        addIndex(index + 0);
        index += 4;

        addVertex(Vertex.get(Vector3.get(0, 0, 1), Vector2.get(0, 0)));
        addVertex(Vertex.get(Vector3.get(1, 0, 1), Vector2.get(1, 0)));
        addVertex(Vertex.get(Vector3.get(1, 1, 1), Vector2.get(1, 1)));
        addVertex(Vertex.get(Vector3.get(0, 1, 1), Vector2.get(0, 0)));
        addIndex(index + 0);
        addIndex(index + 1);
        addIndex(index + 2);
        addIndex(index + 2);
        addIndex(index + 3);
        addIndex(index + 0);
        index += 4;

        addVertex(Vertex.get(Vector3.get(0, 1, 0), Vector2.get(1, 0)));
        addVertex(Vertex.get(Vector3.get(1, 1, 0), Vector2.get(0, 0)));
        addVertex(Vertex.get(Vector3.get(1, 1, 1), Vector2.get(0, 1)));
        addVertex(Vertex.get(Vector3.get(0, 1, 1), Vector2.get(1, 0)));
        addIndex(index + 0);
        addIndex(index + 1);
        addIndex(index + 2);
        addIndex(index + 2);
        addIndex(index + 3);
        addIndex(index + 0);
        index += 4;

        upload();
        clearAndDisposeVertices();
    }

    public Vertex getVertex(int vertexIndex)
    {
        return vertices.get(vertexIndex);
    }

    public int getIndex(int indexIndex)
    {
        return indices.get(indexIndex);
    }
}
