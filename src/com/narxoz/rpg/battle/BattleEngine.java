package com.narxoz.rpg.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class BattleEngine {
    private static BattleEngine instance;
    private Random random = new Random(1L);

    private BattleEngine() {
    }

    public static BattleEngine getInstance() {
        if (instance == null) {
            instance = new BattleEngine();
        }
        return instance;
    }

    public BattleEngine setRandomSeed(long seed) {
        this.random = new Random(seed);
        return this;
    }

    public void reset() {
        this.random = new Random(1L);
    }

    public EncounterResult runEncounter(List<Combatant> teamA, List<Combatant> teamB) {
        Objects.requireNonNull(teamA, "teamA");
        Objects.requireNonNull(teamB, "teamB");

        List<Combatant> a = new ArrayList<>();
        for (Combatant c : teamA) if (c != null && c.isAlive()) a.add(c);

        List<Combatant> b = new ArrayList<>();
        for (Combatant c : teamB) if (c != null && c.isAlive()) b.add(c);

        EncounterResult result = new EncounterResult();

        if (a.isEmpty() && b.isEmpty()) {
            result.setWinner("Draw");
            result.setRounds(0);
            result.addLog("Both teams have no living combatants.");
            return result;
        }

        if (a.isEmpty()) {
            result.setWinner("Team B");
            result.setRounds(0);
            result.addLog("Team A has no living combatants.");
            return result;
        }

        if (b.isEmpty()) {
            result.setWinner("Team A");
            result.setRounds(0);
            result.addLog("Team B has no living combatants.");
            return result;
        }

        int rounds = 0;

        while (!a.isEmpty() && !b.isEmpty()) {
            rounds++;
            result.addLog("---- Round " + rounds + " ----");

            teamAttacks(a, b, "Team A", "Team B", result);
            if (b.isEmpty()) break;

            teamAttacks(b, a, "Team B", "Team A", result);
        }

        result.setRounds(rounds);

        if (!a.isEmpty() && b.isEmpty()) result.setWinner("Team A");
        else if (!b.isEmpty() && a.isEmpty()) result.setWinner("Team B");
        else result.setWinner("Draw");

        result.addLog("Battle ended. Winner: " + result.getWinner());
        return result;
    }

    private void teamAttacks(List<Combatant> attackers, List<Combatant> defenders, String atkName, String defName, EncounterResult result) {
        for (int i = 0; i < attackers.size(); i++) {
            if (defenders.isEmpty()) return;

            Combatant attacker = attackers.get(i);
            if (attacker == null || !attacker.isAlive()) continue;

            int targetIndex = firstAliveIndex(defenders);
            if (targetIndex == -1) {
                defenders.clear();
                return;
            }

            Combatant target = defenders.get(targetIndex);

            int dmg = Math.max(0, attacker.getAttackPower());
            boolean crit = random.nextInt(100) < 10;
            if (crit) dmg *= 2;

            target.takeDamage(dmg);

            String line = atkName + ": " + attacker.getName() + " hits " + defName + ": " + target.getName() + " for " + dmg;
            if (crit) line += " (CRIT)";
            result.addLog(line);

            if (!target.isAlive()) {
                result.addLog(defName + ": " + target.getName() + " is defeated.");
                defenders.remove(targetIndex);
            }
        }
    }

    private int firstAliveIndex(List<Combatant> team) {
        for (int i = 0; i < team.size(); i++) {
            Combatant c = team.get(i);
            if (c != null && c.isAlive()) return i;
        }
        return -1;
    }
}