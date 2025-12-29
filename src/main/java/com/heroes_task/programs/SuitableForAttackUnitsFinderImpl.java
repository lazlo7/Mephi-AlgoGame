package com.heroes_task.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.List;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {
    private static final int BOARD_HEIGHT = 21;

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> suitableUnits = new ArrayList<>();
        // Left army target -> computer is attacked -> check for top cell.
        if (isLeftArmyTarget) {
            for (int y = 0; y < BOARD_HEIGHT; ++y) {
                for (int x = 0; x < 3; ++x) {
                    Unit unit = unitsByRow.get(y).get(x);
                    if (unit != null && unit.isAlive() && (y == 0 || unitsByRow.get(y - 1).get(x) == null)) {
                        suitableUnits.add(unit);
                    }
                }
            }
            return suitableUnits;
        }
        // Otherwise, right army target -> player is attacked -> check for bottom cell.
        for (int y = 0; y < BOARD_HEIGHT; ++y) {
            for (int x = 0; x < 3; ++x) {
                Unit unit = unitsByRow.get(y).get(x);
                if (unit != null && unit.isAlive() && (y == BOARD_HEIGHT - 1 || unitsByRow.get(y + 1).get(x) == null)) {
                    suitableUnits.add(unit);
                }
            }
        }
        return suitableUnits;
    }
}
