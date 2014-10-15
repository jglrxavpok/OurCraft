package org.craft.client.render;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import org.craft.client.*;
import org.craft.resources.*;
import org.craft.utils.*;

public class TextureMap implements IconGenerator, ITextureObject, IDisposable
{

    private ResourceLoader              loader;
    private ResourceLocation            base;
    private ArrayList<TextureIcon>      registredIcons;
    private ArrayList<ResourceLocation> registredLocations;
    private Texture                     texture;
    private BufferedImage               nullImage;
    private BufferedImage               emptyImage;
    private boolean                     lenient;
    private Stitcher                    stitcher;

    /**
     * Creates TextureMap with given loader and base
     */
    public TextureMap(ResourceLoader loader, ResourceLocation base)
    {
        this(loader, base, false);
    }

    /**
     * Creates TextureMap with given loader and base
     */
    public TextureMap(ResourceLoader loader, ResourceLocation base, boolean lenientOnSizes)
    {
        this.lenient = lenientOnSizes;
        this.loader = loader;
        this.base = base;
        registredIcons = new ArrayList<TextureIcon>();
        registredLocations = new ArrayList<ResourceLocation>();

        initNullAndEmptyImages();
        stitcher = new Stitcher(emptyImage);
    }

    /**
     * Completes given ResourceLocation to get full ResourceLocation from base
     */
    public ResourceLocation completeLocation(ResourceLocation loc)
    {
        ResourceLocation newLoc = new ResourceLocation(base.getFullPath(), loc.getFullPath());
        return newLoc;
    }

    /**
     * Instantiates nullImage and emptyImage
     */
    private void initNullAndEmptyImages()
    {
        if(loader.doesResourceExists(completeLocation(new ResourceLocation("missigno.png"))))
        {
            try
            {
                nullImage = ImageUtils.loadImage(loader.getResource(completeLocation(new ResourceLocation("missigno.png"))));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            nullImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics g = nullImage.createGraphics();
            for(int x = 0; x < 16; x++ )
            {
                for(int y = 0; y < 16; y++ )
                {
                    int color = 0xFF000000;
                    if((x >= 8 && y >= 8) || (x < 8 && y < 8))
                        color = 0xFFFF00DC;
                    nullImage.setRGB(x, y, color);
                }
            }
            g.dispose();

        }

        if(loader.doesResourceExists(completeLocation(new ResourceLocation(".png"))))
        {
            try
            {
                emptyImage = ImageUtils.loadImage(loader.getResource(completeLocation(new ResourceLocation(".png"))));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            emptyImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics g = emptyImage.createGraphics();
            for(int x = 0; x < 16; x++ )
            {
                for(int y = 0; y < 16; y++ )
                {
                    if(x == 0 || y == 0)
                        emptyImage.setRGB(x, y, 0xFF4800FF);
                    else
                        emptyImage.setRGB(x, y, 0xFFFF00DC);
                }
            }
            g.dispose();
        }
    }

    @Override
    public TextureIcon generateIcon(ResourceLocation loc)
    {
        if(registredLocations.contains(loc))
            return registredIcons.get(registredLocations.indexOf(loc));
        TextureMapIcon icon = new TextureMapIcon(0, 0, 0, 0, 0, 0);
        registredIcons.add(icon);
        registredLocations.add(loc);
        return icon;
    }

    /**
     * Compiles the TextureMap to create icons from given images
     */
    public void compile() throws Exception
    {
        HashMap<Integer, TextureIcon> indexes = new HashMap<Integer, TextureIcon>();
        for(int i = 0; i < registredIcons.size(); i++ )
        {
            ResourceLocation loc = completeLocation(registredLocations.get(i));
            TextureIcon icon = registredIcons.get(i);
            try
            {
                AbstractResource res = loader.getResource(loc);
                BufferedImage img = ImageUtils.loadImage(res);
                indexes.put(stitcher.addImage(img, lenient), icon);
            }
            catch(Exception e)
            {
                Log.error("Unable to find icon: /" + loc.getFullPath());
                indexes.put(stitcher.addImage(nullImage, true), icon);
            }

        }

        BufferedImage stitchedImage = stitcher.stitch();
        Iterator<Integer> indexesIt = indexes.keySet().iterator();
        while(indexesIt.hasNext())
        {
            int index = indexesIt.next();
            TextureIcon icon = indexes.get(index);
            ((TextureMapIcon) icon).setMinU(stitcher.getMinU(index));
            ((TextureMapIcon) icon).setMinV(stitcher.getMinV(index));
            ((TextureMapIcon) icon).setMaxU(stitcher.getMaxU(index));
            ((TextureMapIcon) icon).setMaxV(stitcher.getMaxV(index));
            ((TextureMapIcon) icon).setWidth(stitcher.getWidth(index));
            ((TextureMapIcon) icon).setHeight(stitcher.getHeight(index));
        }

        if(false) // TODO: needs to know if we are in a debug mode
        {
            if(stitchedImage != null)
            {
                try
                {
                    ImageIO.write(stitchedImage, "png", new File(".", StringUtils.createCorrectedFileName(this.base.getFullPath()) + ".png"));
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        texture = OpenGLHelper.loadTexture(stitchedImage);
    }

    public TextureIcon get(String name)
    {
        return get(new ResourceLocation(name));
    }

    public TextureIcon get(ResourceLocation loc)
    {
        Log.message("Trying to get " + loc.getFullPath());
        if(registredLocations.indexOf(loc) > 0)
            return registredIcons.get(registredLocations.indexOf(loc));
        return new TextureMapIcon(0, 0, 1, 1, 16, 16);
    }

    public Texture getTexture()
    {
        return texture;
    }

    public void fixSize(int w, int h)
    {
        stitcher.setTileWidth(w);
        stitcher.setTileHeight(h);
    }

    private class TextureMapIcon implements TextureIcon
    {
        private float minu;
        private float maxu;
        private float minv;
        private float maxv;
        private int   width;
        private int   height;

        TextureMapIcon(float minu, float minv, float maxu, float maxv, int width, int height)
        {
            this.minu = minu;
            this.maxu = maxu;
            this.minv = minv;
            this.maxv = maxv;
            this.width = width;
            this.height = height;
        }

        @Override
        public float getWidth()
        {
            return width;
        }

        @Override
        public float getHeight()
        {
            return height;
        }

        @Override
        public float getMinU()
        {
            return minu;
        }

        @Override
        public float getMaxU()
        {
            return maxu;
        }

        @Override
        public float getMinV()
        {
            return minv;
        }

        @Override
        public float getMaxV()
        {
            return maxv;
        }

        public void setMinU(float minu)
        {
            this.minu = minu;
        }

        public void setMaxU(float maxu)
        {
            this.maxu = maxu;
        }

        public void setMinV(float minv)
        {
            this.minv = minv;
        }

        public void setMaxV(float maxv)
        {
            this.maxv = maxv;
        }

        public void setWidth(int width)
        {
            this.width = width;
        }

        public void setHeight(int height)
        {
            this.height = height;
        }
    }

    @Override
    public TextureIcon generateIcon(String loc)
    {
        return generateIcon(new ResourceLocation(loc));
    }

    @Override
    public void bind()
    {
        texture.bind();
    }

    @Override
    public void dispose()
    {
        texture.dispose();
    }

}
