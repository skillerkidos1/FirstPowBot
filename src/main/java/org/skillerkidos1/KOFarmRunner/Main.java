package org.skillerkidos1.KOFarmRunner;

import com.google.common.eventbus.Subscribe;
import org.powbot.api.Condition;
import org.powbot.api.Tile;
import org.powbot.api.event.BreakEvent;
import org.powbot.api.event.MessageEvent;
import org.powbot.api.rt4.*;
import org.powbot.api.rt4.walking.model.Skill;
import org.powbot.api.script.AbstractScript;
import org.powbot.api.script.OptionType;
import org.powbot.api.script.ScriptConfiguration;
import org.powbot.api.script.ScriptManifest;
import org.powbot.api.script.paint.Paint;
import org.powbot.api.script.paint.PaintBuilder;
import org.powbot.api.script.paint.TrackSkillOption;
import org.powbot.mobile.service.ScriptUploader;

import java.util.concurrent.Callable;

import static org.powbot.api.Condition.sleep;
import static org.powbot.dax.shared.helpers.General.random;

@ScriptManifest(name = "KO Farm Runner", description = "Farming runs Herbs,Allotments,Flowers", version = "0.2")
@ScriptConfiguration.List({
        @ScriptConfiguration(
        name = "Allotment",
        description = "Allotment Seed to plant",
        optionType =  OptionType.STRING,
        allowedValues = {"Potato seed", "Onion seed","Cabbage seed","Tomato seed",
        "Sweetcorn seed","Watermelon seed","Snape grass seed"},enabled = true,visible = true
        ),

        @ScriptConfiguration(
                name="Flower",
                description ="Flower Seed to plant",
                optionType = OptionType.STRING,
                allowedValues = {"Marigold seed","Rosemary seed","Nasturtium seed",
                "Woad seed","Limpwurt seed"}
                ),

        @ScriptConfiguration(
                name="Herb",
                description ="Herb Seed to plant",
                optionType = OptionType.STRING,
                allowedValues = {"Guam seed","Marrentill seed","Tarromin seed",
                "Harralander seed","Ranarr seed","Toadflax seed","Irit seed","Avantoe seed",
                "Avantoe seed","Kwuarm seed","Snapdragon seed","Lantadyme seed","Dwarf weed seed",
                "Torstol seed"}
        ),

        @ScriptConfiguration(
                name="Catherby Teleport",
                description = "Teleport used for Catherby patch",
                optionType=OptionType.STRING,
                allowedValues = {"Tablet","Spell","Run"}
        ),
        @ScriptConfiguration(
                name="Falador Teleport",
                description = "Teleport used for Falador patch",
                optionType=OptionType.STRING,
                allowedValues = {"Tablet","Spell","Run"}
        ),
        @ScriptConfiguration(
                name="Ardougne Teleport",
                description = "Teleport used for Ardougne patch",
                optionType=OptionType.STRING,
                allowedValues = {"Tablet","Spell","Run"}
        )

        })

public class Main extends AbstractScript {

    //TODO
    //add other patches

    private long scriptStartTime;

    public String currently;

    //NPCs
    private final static String leprechaun = "Tool Leprechaun";

    //Seed names
    public String allotmentSeedSelected;
    public String flowerSeedSelected;
    public String herbSeedSelected;

    //Teleports
    public String faladorTeleportSelected;
    public String catherbyTeleportSelected;
    public String ardougneTeleportSelected;

    //Compost used
    private boolean compost1 = false;
    private boolean compost2 = false;
    private boolean compost3 = false;
    private boolean compost4 = false;

    //Tiles
    private final static Tile ARDOUGNE_TELEPORT_AREA = new Tile(2662, 3307, 0);
    private final static Tile ARDOUGNE_FARM_PATCH = new Tile(2665, 3376, 0);

    private final static Tile CATHERBY_TELEPORT_AREA = new Tile(2757, 3478, 0);
    private final static Tile CATHERBY_FARM_PATCH = new Tile(2807, 3464, 0);

    private final static Tile FALADOR_FARM_PATCH = new Tile(3056, 3310, 0);
    private final static Tile FALADOR_TELEPORT_AREA = new Tile(2965, 3381, 0);

    private final Tile f_allotment1_tile = new Tile(3050, 3312, 0);//within 5
    private final Tile f_allotment2_tile = new Tile(3059, 3303, 0);//within 5
    private final Tile f_flower_tile = new Tile(3054, 3307, 0);//within 5
    private final Tile f_herb_tile = new Tile(3059, 3312, 0);//within 5
    private final int f_herbPatchEmptyID = 8132;

    private final Tile a_allotment1_tile = new Tile(2666, 3381, 0);//within 5
    private final Tile a_allotment2_tile = new Tile(2666, 3368, 0);//within 5
    private final Tile a_flower_tile = new Tile(2667, 3375, 0);//within 5
    private final Tile a_herb_tile = new Tile(2671, 3375, 0);//within 5
    private final int a_herbPatchEmptyID = 8132;

    private final Tile c_allotment1_tile = new Tile(2809, 3469, 0);//within 5
    private final Tile c_allotment2_tile = new Tile(2809, 3458, 0);//within 5
    private final Tile c_flower_tile = new Tile(2809, 3464, 0);//within 5
    private final Tile c_herb_tile = new Tile(2814, 3463, 0);//within 5
    private final int c_herbPatchEmptyID = 8135;

    private static final int allotmentItemID = 1982;
    private static final int allotmentNotedID = 1983;
    private static final int herbItemID = 199;
    private static final int flowerItemID = 225;

    //Patches completed
    private boolean faladorCompleted = false;
    private boolean catherbyCompleted = false;
    private boolean ardougneCompleted = false;

    //Patch number
    private int patchNumber;

    //GUI setup
    private boolean setup = false;

    //onStart
    @Override
    public void onStart() {
        scriptStartTime = System.currentTimeMillis();
        allotmentSeedSelected = getOption("Allotment");
        flowerSeedSelected = getOption("Flower");
        herbSeedSelected = getOption("Herb");
        faladorTeleportSelected = getOption("Falador Teleport");
        catherbyTeleportSelected = getOption("Catherby Teleport");
        ardougneTeleportSelected = getOption("Ardougne Teleport");
        addPaint(getPaintBuilder());
    }

    //Builds paint
    private Paint getPaintBuilder() {
        return PaintBuilder.newBuilder()
                .x(10)
                .y(10)
                .trackSkill(Skill.Farming, "Farming XP Gained", TrackSkillOption.Exp)
                .addString("Currently: ", new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return getCurrently();
                    }
                })
                .build();
    }

    //Sets currently for paint
    private String getCurrently() {
        return currently;
    }

    //loop
    @Override
    public void poll() {
        if (allotmentSeedSelected == null || flowerSeedSelected == null || herbSeedSelected == null ||
                faladorTeleportSelected == null || ardougneTeleportSelected == null || catherbyTeleportSelected == null) {
            getLog().info("Setting up");
            currently = "setting up";
            allotmentSeedSelected = getOption("Allotment");
            flowerSeedSelected = getOption("Flower");
            herbSeedSelected = getOption("Herb");
            faladorTeleportSelected = getOption("Falador Teleport");
            catherbyTeleportSelected = getOption("Catherby Teleport");
            ardougneTeleportSelected = getOption("Ardougne Teleport");
            sleep(5000);
        } else if (allPatchesCompleted()) {
            currently = "Waiting for break to start";
            getLog().info("Waiting for break to start");
            sleep(5000);
        } else {
            if (atFaladorTeleport()) {
                currently = "Walk to Falador farm patch";
                resetCompost();
                getWalkingToFaladorFarmingPatch();
            } else if (atFaladorFarmingPatch()) {
                getLog().info("At Falador Patch");
                getFaladorFarmHandler();
            } else if (atCamelotTeleportArea()) {
                currently = "Walk to Camelot farm patch";
                resetCompost();
                getWalkingToCatherbyFarmingPatch();
            } else if (atCatherbyFarmingPatch()) {
                getLog().info("At Catherby Patch");
                getCatherbyFarmHandler();
            } else if (atArdougneTeleportArea()) {
                currently = "Walk to Ardougne farm patch";
                resetCompost();
                getWalkingToArdougneFarmingPatch();
            } else if (atArdougneFarmingPatch()) {
                getArdougneFarmHandler();
            } else {
                getLog().info("We are lost");
                sleep(1000);
                //TODO
                //add something idk yet
            }
        }
    }

    //Break event
    @Subscribe
    public void onBreakEvent(BreakEvent evt) {
        if (!allPatchesCompleted()) {
            evt.delay(30000);
        } else {
            getLog().info("Accepted break");
            evt.accept();
        }
    }

    //Message event
    @Subscribe
    public void onMessage(MessageEvent event) {
        if (event.getMessage().contains("You treat the") ||
                event.getMessage().contains("has already been treated")) {
            if (patchNumber == 1) {
                getLog().info("Compost 1 done");
                compost1 = true;
            } else if (patchNumber == 2) {
                getLog().info("Compost 2 done");
                compost2 = true;
            } else if (patchNumber == 3) {
                getLog().info("Compost 3 done");
                compost3 = true;
            } else if (patchNumber == 4) {
                getLog().info("Compost 4 done");
                compost4 = true;
            }
        }
    }

    //At any teleport area
    private boolean atTeleportArea() {
        return atFaladorTeleport() || atArdougneTeleportArea() || atCamelotTeleportArea();
    }

    //all patches completed
    private boolean allPatchesCompleted() {
        return faladorCompleted && catherbyCompleted && ardougneCompleted;
    }

    private void resetCompost() {
        compost1 = false;
        compost2 = false;
        compost3 = false;
        compost4 = false;
    }

    private boolean getCompost(int num) {
        if (num == 1) {
            return compost1;
        } else if (num == 2) {
            return compost2;
        } else if (num == 3) {
            return compost3;
        } else if (num == 4) {
            return compost4;
        } else {
            return false;
        }
    }


    private void getFaladorFarmHandler() {
        getLog().info("Falador farm handler");
        if (!isPatchPlanted(allotmentSeedSelected, f_allotment1_tile).valid()) {
            testFarmHandler(allotmentSeedSelected, "Allotment", f_allotment1_tile, getCompost(1), 1);
        } else if (!isPatchPlanted(allotmentSeedSelected, f_allotment2_tile).valid()) {
            testFarmHandler(allotmentSeedSelected, "Allotment", f_allotment2_tile, getCompost(2), 2);
        } else if (!isPatchPlanted(flowerSeedSelected, f_flower_tile).valid()) {
            testFarmHandler(flowerSeedSelected, "Flower", f_flower_tile, getCompost(3), 3);
        } else if (!isPatchPlanted("Herb", f_herb_tile).valid()) {
            testFarmHandler(herbSeedSelected, "Herb", f_herb_tile, getCompost(4), 4);
        } else {
            getLog().info("Nothing to do");
            faladorCompleted = true;
            getTeleportHandler("Camelot", catherbyTeleportSelected, CATHERBY_FARM_PATCH);
        }
    }

    private void getArdougneFarmHandler() {
        if (!isPatchPlanted(allotmentSeedSelected, a_allotment1_tile).valid()) {
            testFarmHandler(allotmentSeedSelected, "Allotment", a_allotment1_tile, getCompost(1), 1);
        } else if (!isPatchPlanted(allotmentSeedSelected, a_allotment2_tile).valid()) {
            testFarmHandler(allotmentSeedSelected, "Allotment", a_allotment2_tile, getCompost(2), 2);
        } else if (!isPatchPlanted(flowerSeedSelected, a_flower_tile).valid()) {
            testFarmHandler(flowerSeedSelected, "Flower", a_flower_tile, getCompost(3), 3);
        } else if (!isPatchPlanted("Herb", a_herb_tile).valid()) {
            testFarmHandler(herbSeedSelected, "Herb", a_herb_tile, getCompost(4), 4);
        } else {
            getLog().info("Nothing to do");
            ardougneCompleted = true;
            getTeleportHandler("Falador", faladorTeleportSelected, FALADOR_FARM_PATCH);
        }
    }

    private void getCatherbyFarmHandler() {
        getLog().info("Catherby farm handler");
        if (!isPatchPlanted(allotmentSeedSelected, c_allotment1_tile).valid()) {
            testFarmHandler(allotmentSeedSelected, "Allotment", c_allotment1_tile, getCompost(1), 1);
        } else if (!isPatchPlanted(allotmentSeedSelected, c_allotment2_tile).valid()) {
            testFarmHandler(allotmentSeedSelected, "Allotment", c_allotment2_tile, getCompost(2), 2);
        } else if (!isPatchPlanted(flowerSeedSelected, c_flower_tile).valid()) {
            testFarmHandler(flowerSeedSelected, "Flower", c_flower_tile, getCompost(3), 3);
        } else if (!isPatchPlanted("Herb", c_herb_tile).valid()) {
            testFarmHandler(herbSeedSelected, "Herb", c_herb_tile, getCompost(4), 4);
        } else {
            getLog().info("Nothing to do");
            catherbyCompleted = true;
            getTeleportHandler("Ardougne", ardougneTeleportSelected, ARDOUGNE_FARM_PATCH);
        }
    }

    private void testFarmHandler(String seedName, String patchName, Tile tile, boolean compostNumber, int tPatchNumber) {
        if (Inventory.isFull()) {
            currently = "Full Inventory Handler";
            getLog().info("Inventory is full");
            if (Game.tab(Game.Tab.INVENTORY)) {
                getFullInventoryHandler();
            }
        } else if (getDeadPlantsGameObject().valid()) {
            currently = "Clear dead patch";
            Game.closeOpenTab();
            moveCameraTo(getDeadPlantsGameObject());
            getClearDeadPlants();
        } else if (getReadyPatchGameObject(seedName, patchName, tile).valid()) {
            currently = "Farm ready patch";
            Game.closeOpenTab();
            moveCameraTo(getReadyPatchGameObject(seedName, patchName, tile));
            getHarvest(seedName, patchName, tile);
        } else if (getWeedPatchGameObject(patchName, tile).valid()) {
            currently = "Rake weed patch";
            Game.closeOpenTab();
            moveCameraTo(getWeedPatchGameObject(patchName, tile));
            getRakePatch(getWeedPatchGameObject(patchName, tile));
        } else if (getEmptyPatchGameObject(patchName, tile).valid()) {
            moveCameraTo(getEmptyPatchGameObject(patchName, tile));
            Game.tab(Game.Tab.INVENTORY);
            if (!compostNumber) {
                currently = "Use Compost in empty patch";
                patchNumber = tPatchNumber;
                getCompostHandler(getEmptyPatchGameObject(patchName, tile), compostNumber);
            } else {
                currently = "Plant seeds in empty patch";
                getPlantSeed(seedName, getEmptyPatchGameObject(patchName, tile));
            }
        }
    }

    private GameObject isPatchPlanted(String seedName, Tile tile) {
        String harvestName = seedName.replace(" seed", "").trim();
        GameObject patch;
        if (seedName.equals("Herb")) {
            patch = Objects.stream()
                    .filter(a -> a.getName().contains(harvestName) &&
                            !a.actions().contains("Rake") &&
                            !a.actions().contains("Pick") &&
                            a.getId() != c_herbPatchEmptyID &&
                            a.getId() != f_herbPatchEmptyID &&
                            a.getId() != a_herbPatchEmptyID &&
                            a.getTile().distanceTo(tile) <= 5)
                    .nearest().first();
        } else {
            patch = Objects.stream()
                    .filter(a -> a.getName().contains(harvestName) &&
                            !a.actions().contains("Rake") &&
                            !a.actions().contains("Pick") &&
                            !a.actions().contains("Harvest") &&
                            a.getTile().distanceTo(tile) <= 5)
                    .nearest().first();
        }
        return patch;
    }

    private void getCompostHandler(GameObject patchName, boolean compostNumber) {
        getLog().info("Compost Handler");
        Item compost = Inventory.stream().filter(a ->
                a.name().contains("compost")).first();
        if (compost.valid()) {
            compost.interact("Use");
            if (patchName.valid()) {
                patchName.interact("Use");
                if (Condition.wait(() ->
                        isAnimating() && compostNumber, 75, 10)) {
                    Condition.wait(() ->
                            !isAnimating() && compostNumber, 75, 10);
                }
            }
        }
    }

    private void getTeleportHandler(String destination, String method, Tile tile) {
        getLog().info("Teleport handler");
        Item teleTab = Inventory.stream().filter(a ->
                a.name().contains(destination)).first();
        if (destination.equals("Falador")) {
            if (method.equals("Tablet")) {
                Game.tab(Game.Tab.INVENTORY);
                if (teleTab.valid()) {
                    teleTab.interact("Break");
                    Condition.wait(() ->
                            atTeleportArea(), 300, 30);
                }
            } else if (method.equals("Spell")) {
                Game.tab(Game.Tab.MAGIC);
                Magic.Spell.FALADOR_TELEPORT.cast("Cast");
            } else if (method.equals("Run")) {
                Movement.moveTo(tile);
            }
        } else if (destination.equals("Camelot")) {
            if (method.equals("Tablet")) {
                Game.tab(Game.Tab.INVENTORY);
                if (teleTab.valid()) {
                    teleTab.interact("Break");
                    Condition.wait(() ->
                            atTeleportArea(), 300, 30);
                }
            } else if (method.equals("Spell")) {
                Game.tab(Game.Tab.MAGIC);
                Magic.Spell.CAMELOT_TELEPORT.cast("Cast");
            } else if (method.equals("Run")) {
                Movement.moveTo(tile);
            }
        } else if (destination.equals("Ardougne")) {
            if (method.equals("Tablet")) {
                Game.tab(Game.Tab.INVENTORY);
                if (teleTab.valid()) {
                    teleTab.interact("Break");
                    Condition.wait(() ->
                            atTeleportArea(), 300, 30);
                }
            } else if (method.equals("Spell")) {
                Game.tab(Game.Tab.MAGIC);
                Magic.Spell.ARDOUGNE_TELEPORT.cast("Cast");
            } else if (method.equals("Run")) {
                Movement.moveTo(tile);
            }
        }
    }

    private void getClearDeadPlants() {
        getLog().info("Clear dead plants");
        if (getDeadPlantsGameObject().valid()) {
            getDeadPlantsGameObject().interact("Clear");
            Condition.wait(() ->
                    !getDeadPlantsGameObject().valid() ||
                            Inventory.isFull(), 300, 50);
        }
    }

    private GameObject getDeadPlantsGameObject() {
        GameObject patch = Objects.stream().filter(a ->
                        !a.actions().contains("Rake") &&
                                !a.actions().contains("Harvest") &&
                                !a.actions().contains("Pick") &&
                                a.actions().contains("Clear"))
                .nearest().first();
        return patch;
    }

    private void getHarvest(String seedName, String patchName, Tile tile) {
        String harvestName = seedName.replace(" seed", "").trim();
        getLog().info("Harvest:" + harvestName);
        moveCameraTo(getReadyPatchGameObject(harvestName, patchName, tile));
        if (getReadyPatchGameObject(harvestName, patchName, tile).interact("Harvest")) {
            Condition.wait(() ->
                    !getReadyPatchGameObject(harvestName, patchName, tile).valid() ||
                            Inventory.isFull(), 300, 50);
        }
        if (getReadyPatchGameObject(harvestName, patchName, tile).interact("Pick")) {
            Condition.wait(() ->
                    !getReadyPatchGameObject(harvestName, patchName, tile).valid() ||
                            Inventory.isFull(), 300, 50);
        }
    }

    private void getPlantSeed(String seedName, GameObject patchGameObject) {
        getLog().info("Plant seeds");
        Item seeds = Inventory.stream().name(seedName).first();
        if (seeds.valid()) {
            seeds.useOn(patchGameObject);
            if (Condition.wait(() ->
                    isAnimating(), 75, 50)) {
                Condition.wait(() ->
                        !patchGameObject.valid() && !isAnimating(), 75, 50);
            }
        }
    }

    private GameObject getWeedPatchGameObject(String patchName, Tile tile) {
        return Objects.stream()
                .filter(a -> a.getName().contains(patchName) &&
                        a.actions().contains("Rake") &&
                        a.getTile().distanceTo(tile) <= 5)
                .nearest().first();
    }

    private GameObject getReadyPatchGameObject(String seedName, String patchName, Tile tile) {
        String harvestName = seedName.replace(" seed", "").trim();
        GameObject patch;
        if (patchName.contains("Herb")) {
            patch = Objects.stream()
                    .filter(a -> a.getName().contains("Herb") &&
                            !a.actions().contains("Rake") &&
                            (a.actions().contains("Harvest") || a.actions().contains("Pick")) &&
                            !a.actions().contains("Clear") &&
                            a.getTile().distanceTo(tile) <= 5)
                    .nearest().first();
        } else {
            patch = Objects.stream()
                    .filter(a -> a.getName().contains(harvestName) &&
                            !a.actions().contains("Rake") &&
                            (a.actions().contains("Harvest") || a.actions().contains("Pick")) &&
                            !a.actions().contains("Clear") &&
                            a.getTile().distanceTo(tile) <= 5)
                    .nearest().first();
        }
        return patch;
    }

    private GameObject getEmptyPatchGameObject(String patchName, Tile tile) {
        return Objects.stream()
                .filter(a -> a.getName().contains(patchName) &&
                        !a.actions().contains("Rake") &&
                        !a.actions().contains("Harvest") &&
                        !a.actions().contains("Clear") &&
                        !a.actions().contains("Pick") &&
                        a.getTile().distanceTo(tile) <= 5)
                .nearest().first();
    }

    private void getFullInventoryHandler() {
        Item weeds = Inventory.stream().name("Weeds").first();
        Item inventoryItemToNote1 = Inventory.stream().id(allotmentItemID).first();
        Item inventoryItemToNote2 = Inventory.stream().id(herbItemID).first();
        Item inventoryItemToNote3 = Inventory.stream().id(flowerItemID).first();
        if (weeds.valid()) {
            getDropAllWeeds();
        }
        if (inventoryItemToNote1.valid()) {
            useItemOnLeprechaun();
        }
        if (inventoryItemToNote2.valid()) {
            useItemOnLeprechaun();
        }
        if (inventoryItemToNote3.valid()) {
            useItemOnLeprechaun();
        }
    }

    private void moveCameraTo(GameObject a) {
        if (a.valid()) {
            Movement.step(a);
            if (Condition.wait(() -> Players.local().inMotion(), 50, 15)) {
                Condition.wait(() -> !Players.local().inMotion(), 150, 25);
            }
            if (!a.inViewport()) {
                Camera.angleToLocatable(a);
                sleep(random(100, 200));
                Camera.turnTo(a);
                sleep(random(100, 200));
            }
        }
    }

    private void moveCameraTo(Npc a) {
        if (a.valid()) {
            Movement.step(a);
            if (Condition.wait(() -> Players.local().inMotion(), 50, 15)) {
                Condition.wait(() -> !Players.local().inMotion(), 150, 25);
            }
            if (!a.inViewport()) {
                sleep(random(100, 200));
                Camera.angleToLocatable(a);
                sleep(random(100, 200));
                Camera.turnTo(a);
                sleep(random(100, 200));
            }
        }
    }

    private void useItemOnLeprechaun() {
        getLog().info("Use item on Leprechaun");
        Npc lep = Npcs.stream().name(leprechaun).nearest().first();
        Item farmItem1 = Inventory.stream().id(allotmentItemID).first();
        Item farmItem2 = Inventory.stream().id(herbItemID).first();
        Item farmItem3 = Inventory.stream().id(flowerItemID).first();
        if (lep.valid()) {
            moveCameraTo(lep);
            if (lep.distanceTo(Players.local().tile()) > 5) {
                Movement.step(lep);
                if (Condition.wait(() -> Players.local().inMotion(), 50, 15)) {
                    Condition.wait(() -> !Players.local().inMotion(), 150, 25);
                }
            } else {
                if (farmItem1.valid()) {
                    farmItem1.interact("Use");
                    if (Inventory.selectedItem().valid()) {
                        lep.interact("Use");
                        Condition.wait(() ->
                                !farmItem1.valid(), 75, 50);
                    }
                }
                if (farmItem2.valid()) {
                    farmItem2.interact("Use");
                    if (Inventory.selectedItem().valid()) {
                        lep.interact("Use");
                        Condition.wait(() ->
                                !farmItem2.valid(), 75, 50);
                    }
                }
                if (farmItem3.valid()) {
                    farmItem3.interact("Use");
                    if (Inventory.selectedItem().valid()) {
                        lep.interact("Use");
                        Condition.wait(() ->
                                !farmItem3.valid(), 75, 50);
                    }
                }
            }
        }
    }

    private boolean shouldTryAction() {
        return !isAnimating() && !Players.local().inMotion();
    }

    private void getDropAllWeeds() {
        getLog().info("Drop all weeds");
        if (Game.tab(Game.Tab.INVENTORY)) {
            for (Item i : Inventory.items()) {
                if (i.valid()) {
                    if (i.name().contains("Weeds")) {
                        i.interact("Drop");
                    }
                }
            }
        }
    }

    private void getRakePatch(GameObject patchGameObject) {
        if (shouldTryAction()) {
            if (patchGameObject.valid()) {
                if (patchGameObject.inViewport()) {
                    patchGameObject.interact("Rake");
                    if (Condition.wait(() ->
                            !patchGameObject.valid() && shouldTryAction(), 300, 50)) {
                        Condition.wait(() ->
                                !patchGameObject.valid() && shouldTryAction(), 300, 50);
                    }
                }
            }
        }
    }

    private boolean atArdougneTeleportArea() {
        return ARDOUGNE_TELEPORT_AREA.distanceTo(Players.local().tile()) <= 20;
    }

    private void getWalkingToArdougneFarmingPatch() {
        getLog().info("Walk to ardougne patch");
        Movement.moveTo(ARDOUGNE_FARM_PATCH);
    }

    private boolean atArdougneFarmingPatch() {
        return ARDOUGNE_FARM_PATCH.distanceTo(Players.local().tile()) <= 15;
    }

    private boolean atCamelotTeleportArea() {
        return CATHERBY_TELEPORT_AREA.distanceTo(Players.local().tile()) <= 15;
    }

    private void getWalkingToCatherbyFarmingPatch() {
        getLog().info("Walk to catherby patch");
        Movement.moveTo(CATHERBY_FARM_PATCH);
    }

    private boolean atCatherbyFarmingPatch() {
        return CATHERBY_FARM_PATCH.distanceTo(Players.local().tile()) <= 15;
    }

    private boolean atFaladorFarmingPatch() {
        return FALADOR_FARM_PATCH.distanceTo(Players.local().tile()) <= 15;
    }

    private void getWalkingToFaladorFarmingPatch() {
        getLog().info("Walk to falador patch");
        Movement.moveTo(FALADOR_FARM_PATCH);
    }

    private boolean atFaladorTeleport() {
        return FALADOR_TELEPORT_AREA.distanceTo(Players.local().tile()) <= 15;
    }

    private boolean isAnimating() {
        return Players.local().animation() != -1;
    }

    public static void main(String[] args) {
        // Start your script with this function. Make sure your device is connected via ADB, and only one is connected
        new ScriptUploader().uploadAndStart("KO Farm Runner", "",
                "127.0.0.1:5815", true, false);
    }
}