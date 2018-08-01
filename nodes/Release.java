package scripts.aiohunter.nodes;

import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;
import scripts.aiohunter.data.Constants;
import scripts.aiohunter.framework.Node;

import java.awt.event.KeyEvent;

public class Release extends Node {
    @Override
    public void execute() {
        System.out.println("_________________________________________________________________________________________");
        System.out.println("The Release Node has been Validated! Executing...");

        RSItem[] inventItems = Inventory.getAll();  // No null check since invent is full

        // Holds shift key
        Keyboard.sendPress(KeyEvent.CHAR_UNDEFINED, KeyEvent.VK_SHIFT);

        for (RSItem item : inventItems) {
            if (item.getID() != Constants.ROPE_ID && item.getID() != Constants.NET_ID){

                // Methods.shouldUndoUpText("Use Raw salmon ->", "Use Raw trout ->"); May be needed.
                item.click();
                General.sleep(100, 200);
            }
        }

        // Releases shift key
        Keyboard.sendRelease(KeyEvent.CHAR_UNDEFINED, KeyEvent.VK_SHIFT);
        System.out.println("_________________________________________________________________________________________");
    }

    @Override
    public boolean validate()
    {
        return Inventory.find(Constants.RED_SALLY_ID).length >= 16;
    }
}
