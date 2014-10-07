package org.craft.client.gui.widgets;

import static org.lwjgl.opengl.GL11.*;

import org.craft.client.gui.*;
import org.craft.client.render.*;
import org.craft.client.render.fonts.*;
import org.craft.resources.*;
import org.lwjgl.input.*;

public class GuiTextField extends GuiWidget
{

    private FontRenderer           font;
    private boolean                focused;
    private int                    cursorCounter;
    private int                    cursorPos;
    private String                 txt;
    private int                    secondCursorPos;
    public static ResourceLocation cursorLoc = new ResourceLocation("ourcraft", "textures/gui/cursor.png");

    public GuiTextField(int id, int x, int y, int w, int h, FontRenderer font)
    {
        super(id, x, y, w, h);
        cursorCounter = 0;
        this.font = font;
        this.txt = "TEST";
    }

    @Override
    public void render(int mx, int my, RenderEngine engine)
    {
        if(visible)
        {
            engine.bindLocation(Gui.widgetsTexture);
            Gui.drawTexturedRect(engine, getX(), getY(), getWidth(), getHeight(), 0, 20f / 256f, 100f / 256f, 40f / 256f);
            int color = 0xFFFFFF;
            String cursor = " ";
            int time = 54;
            float cursorOffset = font.getTextLength(txt.substring(0, cursorPos));
            if(secondCursorPos < 0)
                secondCursorPos = 0;
            if(secondCursorPos > txt.length())
                secondCursorPos = txt.length();
            float cursor2Offset = font.getTextLength(txt.substring(0, secondCursorPos));
            font.drawShadowedString(txt, color, (int) (getX() + 4), (int) (getY() + getHeight() / 2 - font.getCharHeight(' ') / 2), engine);
            int cx = (int) (getX() + 4 + cursorOffset);
            if(cursorCounter % time <= time / 2 && focused || cursorPos != secondCursorPos)
            {
                engine.bindLocation(cursorLoc);
                if(cursorPos != secondCursorPos)
                {
                    engine.enableGLCap(GL_COLOR_LOGIC_OP);
                    glLogicOp(GL_XOR);
                    Gui.drawTexturedRect(engine, cx, (int) (getY() + getHeight() / 2 - font.getCharHeight(' ') / 2), (int) (getX() + 4 + cursor2Offset) - cx, 16, 0, 0, 0.25f, 1);
                    engine.disableGLCap(GL_COLOR_LOGIC_OP);
                }
                else
                {
                    float minU = 0.5f;
                    float maxU = 0.75f;
                    if(cursorPos == txt.length())
                    {
                        minU += 0.25f;
                        maxU += 0.25f;
                    }
                    Gui.drawTexturedRect(engine, cx, (int) (getY() + getHeight() / 2 - font.getCharHeight(' ') / 2), 8, 16, minU, 0, maxU, 1);
                }
            }

        }
    }

    public boolean onButtonPressed(int x, int y, int button)
    {
        if(isMouseOver(x, y))
            focused = true;
        return true;
    }

    public boolean onButtonReleased(int x, int y, int button)
    {
        if(!isMouseOver(x, y))
            focused = false;
        else
        {
            int indexX = x - 4 - getX();
            int index = 0;
            for(; index < txt.length(); index++ )
            {
                String subtxt = txt.substring(0, index);
                if(indexX < font.getTextLength(subtxt))
                    break;
            }
            this.cursorPos = index;
        }
        return true;
    }

    public void updateCursorCounter()
    {
        cursorCounter++ ;
        if(cursorCounter > 1000)
            cursorCounter = 0;
    }

    public boolean keyPressed(int id, char c)
    {
        if(focused)
        {

        }
        return focused;
    }

    public boolean keyReleased(int id, char c)
    {
        if(focused)
        {
            if(id == Keyboard.KEY_RIGHT)
            {
                if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
                {
                    incrementSecondCursorPos();
                }
                else
                {
                    incrementCursorPos();
                    secondCursorPos = cursorPos;
                }
            }
            else if(id == Keyboard.KEY_LEFT)
            {
                if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
                {
                    decrementSecondCursorPos();
                }
                else
                {
                    decrementCursorPos();
                    secondCursorPos = cursorPos;
                }
            }
            else if(id == Keyboard.KEY_DELETE)
            {
                if(cursorPos == secondCursorPos)
                    deleteFromCursor(cursorPos, cursorPos + 1);
                else
                    deleteFromCursor(cursorPos, secondCursorPos);
            }
            else if(id == Keyboard.KEY_BACK)
            {
                if(cursorPos == secondCursorPos)
                    deleteFromCursor(cursorPos - 1, cursorPos);
                else
                    deleteFromCursor(cursorPos, secondCursorPos);
            }
            else if(isValid(c))
            {
                deleteFromCursor(cursorPos, secondCursorPos);
                insertFromCursorPos(cursorPos, cursorPos, Character.toString(c));
            }
        }
        return focused;
    }

    private boolean isValid(char c)
    {
        return c >= ' ' && c != 127;
    }

    private void insertFromCursorPos(int pos0, int pos1, String string)
    {
        deleteFromCursor(pos0, pos1);
        String newText = txt.substring(0, cursorPos) + string + txt.substring(cursorPos, txt.length());
        txt = newText;
        cursorPos += string.length();
        secondCursorPos = cursorPos;
    }

    private void deleteFromCursor(int pos0, int pos1)
    {
        int min = Math.min(pos0, pos1);
        int max = Math.max(pos0, pos1);
        if(min < 0)
            min = 0;
        if(max > txt.length())
            max = txt.length();
        String newText = txt.substring(0, min) + txt.substring(max, txt.length());
        txt = newText;
        cursorPos = min;
    }

    private void decrementCursorPos()
    {
        cursorPos-- ;
        if(cursorPos < 0)
        {
            cursorPos = 0;
        }
    }

    private void incrementCursorPos()
    {
        cursorPos++ ;
        if(cursorPos > txt.length())
        {
            cursorPos = txt.length();
        }
    }

    private void decrementSecondCursorPos()
    {
        secondCursorPos-- ;
        if(secondCursorPos < 0)
        {
            secondCursorPos = 0;
        }
    }

    private void incrementSecondCursorPos()
    {
        secondCursorPos++ ;
        if(secondCursorPos > txt.length())
        {
            secondCursorPos = txt.length();
        }
    }
}
