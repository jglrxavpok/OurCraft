package org.craft.client.render;

import static org.lwjgl.opengl.GL20.*;

import java.nio.*;
import java.util.*;

import org.craft.maths.*;
import org.craft.utils.*;
import org.lwjgl.*;

public class Shader implements IDisposable
{

    private static Shader            current = null;
    private int                      program;
    private HashMap<String, Integer> locsMap;

    public Shader(String vert, String frag)
    {
        locsMap = new HashMap<String, Integer>();
        program = glCreateProgram();
        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(vertexId, vert);
        glShaderSource(fragmentId, frag);

        glCompileShader(vertexId);
        if(glGetShaderi(vertexId, GL_COMPILE_STATUS) == 0)
        {
            Log.error("Compilation of vertex shader failed: ");
            Log.error(glGetShaderInfoLog(vertexId, 1024));
            return;
        }
        glCompileShader(fragmentId);
        if(glGetShaderi(fragmentId, GL_COMPILE_STATUS) == 0)
        {
            Log.error("Compilation of fragment shader failed: ");
            Log.error(glGetShaderInfoLog(fragmentId, 1024));
            return;
        }

        glAttachShader(program, vertexId);
        glAttachShader(program, fragmentId);

        glLinkProgram(program);
        if(glGetProgrami(program, GL_LINK_STATUS) == 0)
        {
            Log.error("Linking of program failed: ");
            Log.error(glGetProgramInfoLog(program, 1024));
            return;
        }
    }

    /**
     * Binds this program to OpenGL
     */
    public void bind()
    {
        current = this;
        glUseProgram(program);
    }

    public static Shader getCurrentlyBound()
    {
        return current;
    }

    /**
     * Sets the given uniform to the value of the given vec2
     */
    public Shader setUniform(String uniform, Vector2 v)
    {
        int l = getLocation(uniform);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 2);
        v.write(buffer);
        buffer.flip();
        glUniform2(l, buffer);
        return this;
    }

    /**
     * Sets the given uniform to the value of the given vec3
     */
    public Shader setUniform(String uniform, Vector3 v)
    {
        int l = getLocation(uniform);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 3);
        v.write(buffer);
        buffer.flip();
        glUniform3(l, buffer);
        return this;
    }

    /**
     * Sets the given uniform to the value of the given matrix
     */
    public Shader setUniform(String uniform, Matrix4 m)
    {
        int l = getLocation(uniform);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 4); // TODO: Pooling
        m.write(buffer);
        buffer.flip();
        glUniformMatrix4(l, true, buffer);
        return this;
    }

    private int getLocation(String uniform)
    {
        if(!locsMap.containsKey(uniform))
        {
            int loc = glGetUniformLocation(program, uniform);
            if(loc == -1)
            {
                Log.error("Uniform named " + uniform + " not found in shader");
            }
            locsMap.put(uniform, loc);
        }
        return locsMap.get(uniform);
    }

    public void dispose()
    {
        glDeleteProgram(program);
    }
}
