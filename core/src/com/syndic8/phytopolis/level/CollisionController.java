package com.syndic8.phytopolis.level;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import com.syndic8.phytopolis.GameplayMode;
import com.syndic8.phytopolis.InputController;
import com.syndic8.phytopolis.level.models.*;

import static com.syndic8.phytopolis.level.PlantController.PlantNode.LEAF_HEIGHT;

public class CollisionController implements ContactListener {

    private final Player player;
    private final UIController uiController;
    private final ResourceController resourceController;
    private final PlantController plantController;
    private final HazardController hazardController;
    private final InputController ic;
    private final GameplayMode worldController;
    protected ObjectSet<Fixture> sensorFixtures;
    private boolean addedWater;

    public CollisionController(GameplayMode c,
                               Player p,
                               UIController ui,
                               ResourceController rsrc,
                               PlantController plt,
                               HazardController hzd) {
        worldController = c;
        player = p;
        sensorFixtures = new ObjectSet<>();
        uiController = ui;
        resourceController = rsrc;
        plantController = plt;
        hazardController = hzd;
        ic = InputController.getInstance();

    }

    public boolean getAddedWater() {
        return addedWater;
    }

    public void setAddedWater(boolean value) {
        addedWater = value;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Model bd1 = (Model) body1.getUserData();
            Model bd2 = (Model) body2.getUserData();

            // See if we have landed on the ground.
            if ((player.getSensorName().equals(fd2) && player != bd1 &&
                    (bd1.getType() == Model.ModelType.LEAF ||
                            bd1.getType() == Model.ModelType.PLATFORM ||
                            bd1.getType() == Model.ModelType.TILE_FULL)) ||
                    (player.getSensorName().equals(fd1) && player != bd2) &&
                            (bd2.getType() == Model.ModelType.LEAF ||
                                    bd2.getType() == Model.ModelType.PLATFORM ||
                                    bd2.getType() ==
                                            Model.ModelType.TILE_FULL)) {
                player.setGrounded(true);
                sensorFixtures.add(player == bd1 ?
                                           fix2 :
                                           fix1); // Could have more than one ground
            }
            if ((player.getSensorName().equals(fd2) && player != bd1 &&
                    bd1.getType() == Model.ModelType.LEAF) ||
                    (player.getSensorName().equals(fd1) && player != bd2 &&
                            bd2.getType() == Model.ModelType.LEAF)) {
                Leaf l = (Leaf) (player == bd1 ? bd2 : bd1);
                if (l.getLeafType() == Leaf.leafType.BOUNCY) {
                    player.setBouncy(true);
                    l.setBouncy(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endContact(Contact contact) {
        contact.setEnabled(true);
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((player.getSensorName().equals(fd2) && player != bd1) ||
                (player.getSensorName().equals(fd1) && player != bd2)) {
            sensorFixtures.remove(player == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                player.setGrounded(false);
            }

            try {
                if (((Model) bd1).getType() == Model.ModelType.LEAF ||
                        ((Model) bd2).getType() == Model.ModelType.LEAF) {
                    Leaf l = (Leaf) (player == bd1 ? bd2 : bd1);
                    if (l.getLeafType() == Leaf.leafType.BOUNCY) {
                        player.setBouncy(false);
                        if (!ic.didJump()) {
                            l.setBouncy(false);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();
        boolean isCollisionBetweenPlayerAndBug =
                (fix1.getBody() == player.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.BUG) ||
                        (fix2.getBody() == player.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.BUG);
        boolean isCollisionBetweenPlayerAndLeaf =
                (fix1.getBody() == player.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.LEAF) ||
                        (fix2.getBody() == player.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.LEAF);
        boolean isCollisionBetweenPlayerAndNoTopTile =
                (fix1.getBody() == player.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_NOTOP) ||
                        (fix2.getBody() == player.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.TILE_NOTOP);
        boolean isCollisionBetweenPlayerAndWater =
                (fix1.getBody() == player.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.WATER) ||
                        (fix2.getBody() == player.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.WATER);
        boolean isCollisionBetweenPlayerAndSun =
                (fix1.getBody() == player.getBody() &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (fix2.getBody() == player.getBody() &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN);
        boolean isCollisionBetweenLeafAndSun =
                (((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.LEAF &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.LEAF && ((Model) fix1.getBody()
                                .getUserData()).getType() ==
                                Model.ModelType.SUN);
        boolean isCollisionBetweenPlatformAndSun =
                ((((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.PLATFORM) &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.PLATFORM &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN);
        boolean isCollisionBetweenWaterAndSun =
                ((((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.WATER) &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.WATER && ((Model) fix1.getBody()
                                .getUserData()).getType() ==
                                Model.ModelType.SUN);
        boolean isCollisionBetweenTileAndSun =
                (((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.TILE_NOTOP &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_NOTOP &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN) ||
                        (((Model) fix1.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_FULL &&
                                ((Model) fix2.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN) ||
                        (((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.TILE_FULL &&
                                ((Model) fix1.getBody()
                                        .getUserData()).getType() ==
                                        Model.ModelType.SUN);
        boolean isCollisionBetweenBugAndBouncy =
                (((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.LEAF &&
                        ((Model) fix2.getBody().getUserData()).getType() ==
                                Model.ModelType.BUG) ||
                        (((Model) fix1.getBody().getUserData()).getType() ==
                                Model.ModelType.BUG && ((Model) fix2.getBody()
                                .getUserData()).getType() ==
                                Model.ModelType.LEAF);

        if (isCollisionBetweenPlayerAndSun ||
                isCollisionBetweenPlatformAndSun ||
                isCollisionBetweenTileAndSun || isCollisionBetweenWaterAndSun) {
            contact.setEnabled(false);
        }
        if (isCollisionBetweenBugAndBouncy) {
            Bug b;
            Leaf l;
            if (((Model) fix1.getBody().getUserData()).getType() ==
                    Model.ModelType.BUG) {
                b = (Bug) fix1.getBody().getUserData();
                l = (Leaf) fix2.getBody().getUserData();
            } else {
                b = (Bug) fix2.getBody().getUserData();
                l = (Leaf) fix1.getBody().getUserData();
            }

            if (l.getLeafType() == Leaf.leafType.BOUNCY) {
                hazardController.removeHazard(b);
                plantController.removeHazardFromNodes(b);
            }
        }
        if (isCollisionBetweenLeafAndSun) {
            Sun s;
            Leaf l;
            if (((Model) fix1.getBody().getUserData()).getType() ==
                    Model.ModelType.SUN) {
                s = (Sun) fix1.getBody().getUserData();
                l = (Leaf) fix2.getBody().getUserData();
            } else {
                s = (Sun) fix2.getBody().getUserData();
                l = (Leaf) fix1.getBody().getUserData();
            }

            l.setSun(true);
            uiController.setFlash(true);
            uiController.setLabelSize(1.2f);
            contact.setEnabled(false);
            s.clear();
            worldController.addObject(new Indicator(s.getX(),
                                                    s.getY(),
                                                    worldController.getSunIndicatorTexture(),
                                                    worldController.getTilemap()));
            uiController.addTime();
        }
        if (isCollisionBetweenPlayerAndWater) {
            contact.setEnabled(false);
            Water w;
            if (((Model) fix1.getBody().getUserData()).getType() ==
                    Model.ModelType.WATER) {
                w = (Water) fix1.getBody().getUserData();
            } else {
                w = (Water) fix2.getBody().getUserData();
            }
            if (w.isFull() && !resourceController.fullWater()) {
                w.clear();
                resourceController.pickupWater();
                uiController.setWaterSize(1.2f);
                setAddedWater(true);
            }
        }
        // Some tolerance is necessary to prevent jittering
        boolean isPlayerGoingUp = player.getVY() > 0.1f;
        boolean isPlayerGoingDown = player.getVY() <= 0;
        boolean isPlayerBelow = false;
        if (fix1.getBody() == player.getBody()) {
            isPlayerBelow =
                    fix1.getBody().getPosition().y - player.getHeight() / 2f <
                            fix2.getBody().getPosition().y + LEAF_HEIGHT / 2f;
        } else if (fix2.getBody() == player.getBody()) {
            isPlayerBelow =
                    fix2.getBody().getPosition().y - player.getHeight() / 2f <
                            fix1.getBody().getPosition().y;
        }
        if (isCollisionBetweenPlayerAndLeaf &&
                (isPlayerGoingUp || isPlayerBelow || ic.isDropKeyDown())) {
            contact.setEnabled(false);
        }
        if (isCollisionBetweenPlayerAndNoTopTile) {
            contact.setEnabled(false);
        }

        if (isCollisionBetweenPlayerAndBug) {
            //            System.out.println("BUG");
            if (isPlayerGoingDown) {
                //                System.out.println("BUG DOWN");
                Bug b;
                if (((Model) fix1.getBody().getUserData()).getType() ==
                        Model.ModelType.BUG) {
                    b = (Bug) fix1.getBody().getUserData();
                } else {
                    b = (Bug) fix2.getBody().getUserData();
                }
                hazardController.removeHazard(b);
                plantController.removeHazardFromNodes(b);

            } else {
                contact.setEnabled(false);
            }
        }

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

}
