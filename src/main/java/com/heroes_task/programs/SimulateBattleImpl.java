package com.heroes_task.programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.Comparator;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        // Copy units from both armies and sort them by attack in descending order.
        var playerUnits = new ArrayList<>(playerArmy.getUnits().stream()
                .sorted(Comparator.comparingInt(Unit::getBaseAttack).reversed())
                .toList());

        var computerUnits = new ArrayList<>(computerArmy.getUnits().stream()
                .sorted(Comparator.comparingInt(Unit::getBaseAttack).reversed())
                .toList());

        var playerUnitIterator = playerUnits.iterator();
        var computerUnitIterator = computerUnits.iterator();
        while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
            if (!playerUnitIterator.hasNext()) {
                playerUnitIterator = playerUnits.iterator();
            }

            if (!computerUnitIterator.hasNext()) {
                computerUnitIterator = computerUnits.iterator();
            }

            var playerUnit = playerUnitIterator.next();
            var computerUnit = computerUnitIterator.next();

            var attackedComputerUnit = playerUnit.getProgram().attack();
            if (attackedComputerUnit == null) {
                throw new IllegalStateException("Player unit did not return a target to attack.");
            }
            printBattleLog.printBattleLog(playerUnit, attackedComputerUnit);
            if (!attackedComputerUnit.isAlive()) {
                computerUnits.remove(attackedComputerUnit);
            }

            var attackedPlayerUnit = computerUnit.getProgram().attack();
            if (attackedPlayerUnit == null) {
                throw new IllegalStateException("Computer unit did not return a target to attack.");
            }
            printBattleLog.printBattleLog(computerUnit, attackedPlayerUnit);
            if (!attackedPlayerUnit.isAlive()) {
                playerUnits.remove(attackedPlayerUnit);
            }
        }
    }
}