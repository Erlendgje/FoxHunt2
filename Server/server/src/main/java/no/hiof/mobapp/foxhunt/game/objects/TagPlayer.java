package no.hiof.mobapp.foxhunt.game.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.IHunter;
import no.hiof.mobapp.foxhunt.game.IStamina;
import no.hiof.mobapp.foxhunt.game.ITagHunter;
import no.hiof.mobapp.foxhunt.game.LatLongCoord;
import no.hiof.mobapp.foxhunt.game.World;

/**
 * Foxes try to flock with foxes of the same group and avoid foxes of other groups, predators, and obstacles.
 *
 * @author      joink
 */
public class TagPlayer extends GameObject implements ITagHunter, IStamina
{
        final static int DEFAULT_GROUPID = 1;
        final static double DEFAULT_FOX_SPEED = 5; // m/s
    final static int DEFAULT_FOX_THETA = 35; // Can turn 90 degrees in one second

        final static int CATCH_RANGE = 9; // Distance in meters

        public int catchrange = CATCH_RANGE;

        private GameObject lastCaughtBy;
    boolean isHunter = false;
    long hunterFromTime;

    double minSpeed, maxSpeed;

    static PreparedStatement dbIsHunter;

        public TagPlayer()
        {
                super(DEFAULT_GROUPID);
        setSpeed( DEFAULT_FOX_SPEED );
        setMaxTurnTheta( DEFAULT_FOX_THETA );
        }

        public TagPlayer(double lon, double lat, int theta, int groupID) {
                super(lon, lat, theta, groupID);
                // TODO Auto-generated constructor stub
        }

        public TagPlayer(int groupID) {
                super(groupID);
                // TODO Auto-generated constructor stub
        }

        @Override
        public void initFromDatabase(Connection con) {
                // TODO Auto-generated method stub
                config.put("behavior", "TagPlayerAndHunter");
                config.put("speed", "3.3");
                config.put("minSpeed", "0.7");
                config.put("maxSpeed", "2.0");
                config.put("turnTheta", "40");

                config.put("detectionRange", "60.0");
                config.put("separationRange", "20.0");

                config.put("staminaMax", "20");
                config.put("staminaRebuildAlpha", "1");

                super.initFromDatabase(con);

            // The range at which game objects can detect the inherited instances like foxes, food, predators, and obstacles

                if (config.containsKey("catchRadius"))
                {
                        catchrange = Integer.parseInt(config.get("catchRadius"));
                }

                if (config.containsKey("isHunter"))
                {
                        setHunter(Boolean.parseBoolean(config.get("isHunter")));
                }

                if (config.containsKey("minSpeed"))
                {
                        minSpeed = Double.parseDouble(config.get("minSpeed"));
                }

                if (config.containsKey("maxSpeed"))
                {
                        maxSpeed = Double.parseDouble(config.get("maxSpeed"));
                }

                if (config.containsKey("staminaMax"))
                {
                        staminaMax = Double.parseDouble(config.get("staminaMax"));
                        staminaCurrent = staminaMax;
                }

                if (config.containsKey("staminaRebuildAlpha"))
                {
                        staminaRebuildAlpha = Double.parseDouble(config.get("staminaRebuildAlpha"));
                }
        }

        public double getKillRange() {
                // TODO Auto-generated method stub
                return catchrange;
        }

        public void randomizePlacement()
        {

                double newlon = getWorld().getMap().c1.lon + Math.random()*(getWorld().getMap().c2.lon - getWorld().getMap().c1.lon);
                double newlat = getWorld().getMap().c1.lat + Math.random()*(getWorld().getMap().c2.lat -getWorld().getMap().c1.lat);

                getLocation().lat = newlat;
                getLocation().lon = newlon;
        }
        /**
         * @return the caught
         */
        public boolean isHunter() {
                return isHunter;
        }

        /**
         * @param caught the caught to set
         */
        public void setHunter(boolean isHunter) {
                if (this.isHunter != isHunter) {
                        //System.out.println("Object: " + config.get("name") + "'s hunter state is now: " + Boolean.toString(isHunter));
                        hunterFromTime = System.currentTimeMillis();
                        this.isHunter = isHunter;

                        if (World.doLogging == true) {
                                updateLog(hunterFromTime, this.dbSimulationID, dbObjectID, "ishunter", Boolean.toString(this.isHunter));
                        }
                }
        }

        /* Methods related to stamina */
        double staminaMax;
        double staminaRebuildAlpha;
        double staminaCurrent;

        public double getMaxStamina() {
                // TODO Auto-generated method stub
                return staminaMax;
        }

        public double getRemaingStamina() {
                return staminaCurrent;
        }

        public double getStaminaRebuildRate() {
                return staminaRebuildAlpha;
        }

        public void setCurrentStamina(double curStam) {
            staminaCurrent = curStam;
    }

    public void setMaxStamina(double maxStam) {
            staminaMax = maxStam;
    }

    public void setStaminaRebuildRate(double alpha) {
            staminaRebuildAlpha = alpha;
    }

    // ITagHunter
    public boolean isLastCaughtBy(GameObject go) {
            // TODO Auto-generated method stub
            return lastCaughtBy == go;
    }

    public void setLastCaughtBy(GameObject go) {
            lastCaughtBy = go;

    }

    public void rebuildStamina(double dT) {
            if (isHunter() == true) {
                    staminaCurrent += dT * staminaRebuildAlpha * 1.2;
            } else {
                    staminaCurrent += dT * staminaRebuildAlpha;
            }
            if (staminaCurrent > staminaMax) {
                    staminaCurrent = staminaMax;
            }

    }
    public double getStaminaSpeed(double factor, double dT) {
            double stamSpeed = maxSpeed * factor;
            double cost = (stamSpeed-minSpeed)*dT;

            if (cost < 0)
            {
                    cost = 0;
            }

            // The current stamina is too low to accomplish the requested speed. Return the maximum available
            if (staminaCurrent <= cost) {

                    stamSpeed = (staminaCurrent)/dT;
                    cost = staminaCurrent;

                    return minSpeed;
            }

            // Decrease stamina-pool
            staminaCurrent -= cost;

            if (stamSpeed < minSpeed) {
                    return minSpeed;
            } else {
                    return stamSpeed;
            }
    }

}
