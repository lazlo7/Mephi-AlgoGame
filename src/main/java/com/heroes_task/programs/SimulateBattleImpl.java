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

        int currentPlayerUnitIdx = 0;
        int currentComputerUnitIdx = 0;
        while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
            var playerUnit = playerUnits.get(currentPlayerUnitIdx);
            var computerUnit = computerUnits.get(currentComputerUnitIdx);

            var attackedComputerUnit = playerUnit.getProgram().attack();
            if (attackedComputerUnit == null) {
                throw new IllegalStateException("Player unit did not return a target to attack.");
            }
            printBattleLog.printBattleLog(playerUnit, attackedComputerUnit);
            if (!attackedComputerUnit.isAlive()) {
                var deadComputerUnitIdx = computerUnits.indexOf(attackedComputerUnit);
                computerUnits.remove(attackedComputerUnit);
                if (currentComputerUnitIdx > deadComputerUnitIdx) {
                    currentComputerUnitIdx--;
                }
                if (computerUnits.isEmpty()) {
                    break;
                }
            }

            var attackedPlayerUnit = computerUnit.getProgram().attack();
            if (attackedPlayerUnit == null) {
                throw new IllegalStateException("Computer unit did not return a target to attack.");
            }
            printBattleLog.printBattleLog(computerUnit, attackedPlayerUnit);
            if (!attackedPlayerUnit.isAlive()) {
                var deadPlayerUnitIdx = playerUnits.indexOf(attackedPlayerUnit);
                playerUnits.remove(attackedPlayerUnit);
                if (currentPlayerUnitIdx > deadPlayerUnitIdx) {
                    currentPlayerUnitIdx--;
                }
                if (playerUnits.isEmpty()) {
                    break;
                }
            }

            currentPlayerUnitIdx = (currentPlayerUnitIdx + 1) % playerUnits.size();
            currentComputerUnitIdx = (currentComputerUnitIdx + 1) % computerUnits.size();
        }
    }
}