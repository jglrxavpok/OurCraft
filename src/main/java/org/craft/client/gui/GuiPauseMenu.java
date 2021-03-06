package org.craft.client.gui;

import org.craft.client.*;
import org.craft.client.gui.widgets.*;
import org.lwjgl.input.*;

public class GuiPauseMenu extends Gui
{

    public GuiPauseMenu(OurCraft game)
    {
        super(game);
    }

    @Override
    public boolean requiresMouse()
    {
        return true;
    }

    @Override
    public void init()
    {
        addWidget(new GuiButton(0, oc.getDisplayWidth() / 2 - 150, oc.getDisplayHeight() / 2, 300, 40, I18n.format("main.play.return"), getFontRenderer()));
        addWidget(new GuiButton(1, oc.getDisplayWidth() / 2 - 150, oc.getDisplayHeight() / 2 + 60, 300, 40, I18n.format("main.play.quitToMainScreen"), getFontRenderer()));
    }

    public void actionPerformed(GuiWidget widget)
    {
        if(widget.getID() == 0)
        {
            oc.openMenu(new GuiIngame(oc));
        }
        else if(widget.getID() == 1)
        {
            oc.quitToMainScreen();
        }
    }

    @Override
    public void update()
    {
        super.update();
    }

    public void keyReleased(int id, char c)
    {
        super.keyReleased(id, c);
        if(id == Keyboard.KEY_ESCAPE)
        {
            oc.openMenu(new GuiIngame(oc));
        }
    }

    public boolean pausesGame()
    {
        return true;
    }

}
