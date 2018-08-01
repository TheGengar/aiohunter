package scripts.aiohunter.nodes;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.aiohunter.data.Constants;
import scripts.aiohunter.data.Methods;
import scripts.aiohunter.data.Vars;
import scripts.aiohunter.framework.Node;
import scripts.aiohunter.utilities.Antiban;

public class HuntSallys extends Node{

    // Hunting statistics; used in ABC2
    private long lastHuntingWaitTime;
    private long averageHuntingWaitTime = 3000;
    private long totalHuntingWaitTime = 0;
    private long totalHuntingInstances = 0;

    private void updateHuntingStatistics(long waitTime)
    {
        lastHuntingWaitTime = waitTime;
        totalHuntingWaitTime += lastHuntingWaitTime;
        totalHuntingInstances++;
        averageHuntingWaitTime = totalHuntingWaitTime / totalHuntingInstances;

        General.println("Average Wait Time is: " + averageHuntingWaitTime);
    }

    @Override
    public void execute()
    {
        System.out.println("_________________________________________________________________________________________");
        System.out.println("The HuntSallys Node has been Validated! Executing...");
        if (isNetsSet())
        {
            resetPosition();
            // Nets are set so now we are about to AFK.
            doAntiban();
        }
        else  if (isEquipmentOnGround())
        {
            lootEquipment();
            // Equipment has been looted so it is time to fix the net.
            fixNet();
        }
        else if (isSallyCaught())
        {
            catchSally();
        }
        else if (isNetNotSet())
        {
            fixNet();
        }
    }

    // Checks to see if all nets are set and ready to go.
    private boolean isNetsSet()
    {
        RSObject[] setNets = Objects.find(10, Vars.setTreeID);

        return (setNets.length == 3);
    }

    private void doAntiban()
    {
        // Prior to idle antiban procedure
        long startingHuntingTime = System.currentTimeMillis();
        Antiban.get().generateTrackers((int) averageHuntingWaitTime);

        while (isNetsSet())
        {
            System.out.print("doAntiBan ~~ Commencing idle procedures.");
            General.sleep(200);
            Antiban.get().timedActions();
        }

        updateHuntingStatistics(System.currentTimeMillis() - startingHuntingTime);
        Antiban.get().sleepReactionTime((int) averageHuntingWaitTime);
    }

    // Checks to see if equipment are on the ground
    private boolean isEquipmentOnGround()
    {
        RSGroundItem[] items = GroundItems.find(Constants.NET_ID, Constants.ROPE_ID);

        return (items.length > 0);
    }

    private void lootEquipment()
    {
        RSGroundItem[] items = GroundItems.find(Constants.NET_ID, Constants.ROPE_ID);



        if (items.length > 0)
        {
            // Loot one of the items via right click. Wait for person to get rope, after wait get net.
            // Rope is above net when dropped.

            // Wait till Player gets to item coordinate
            RSTile itemPosition = items[0].getPosition();
            int currentAmount = Inventory.getCount(items[0].getID());

            Methods.safeClick("Take " + items[0].getDefinition().getName());
            if (Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100);
                    return Player.getPosition().equals(itemPosition);
                }
            }, General.random(3000, 3150)))
            {
                // Wait till Player gets item.
                if (Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        General.sleep(100);
                        return false;
                    }
                }, General.random(600, 700)))
                {
                    // Player has just got rope.

                    // The net is still on the ground
                }
            }
            // Wait till Player gets item.



            items[0].hover();
            RSTile hoverTile = items[0].getPosition();

            System.out.print("lootEquipment ~~ " + items[0].getDefinition().getName() + " found on the ground. Looting...");

            // Gets specifically the items on the current tile, not any other traps that may have fallen.
            while (GroundItems.getAt(hoverTile).length > 0)
            {
                RSGroundItem[] groundItems = GroundItems.getAt(hoverTile);
                // Shits on the ground, loot it.
                groundItems[0].hover();
                Methods.safeClick("Take " + groundItems[0].getDefinition().getName());

                General.sleep(350);
            }
        }
    }

    private boolean isSallyCaught()
    {
        RSObject[] sallyTree = Objects.find(8, Vars.caughtTreeID);

        return sallyTree.length > 0;
    }

    private void catchSally()
    {
        RSObject[] sallyTree = Objects.find(8, Vars.caughtTreeID);

        if (sallyTree.length > 0)
        {
            Methods.clickObject(sallyTree[0], Constants.CATCH_SALLY_ANIMATION, Constants.CATCH_TRAP_UPTEXT);
            fixNet();
        }
    }

    private boolean isNetNotSet()
    {
        RSObject[] openTrees = Objects.find(5, Vars.emptyTreeID);
        return (openTrees.length > 0);
    }

    // Re-sets the trap
    private void fixNet()
    {
        RSObject[] openTrees = Objects.find(5, Vars.emptyTreeID);

        if (openTrees.length > 0)
        {
            // There's an empty tree. Set a trap there.
            Methods.clickObject(openTrees[0], Constants.SET_TRAP_ANIMATION, Constants.SET_TRAP_UPTEXT);
        }
    }

    private void turnToTree(RSObject tree){
        if (!tree.isOnScreen())
        {
            Camera.turnToTile(tree.getPosition());
        }
    }

    // Goes back to AFK position
    private void resetPosition()
    {
        if (!Player.getPosition().equals(Vars.afkTile))
        {
            WebWalking.walkTo(Constants.AFK_RED_TILE);
            Methods.waitToStop(1000, 1100);
        }
    }

    @Override
    public boolean validate() {
        return Inventory.find(Constants.RED_SALLY_ID).length < 16;

    }
}
