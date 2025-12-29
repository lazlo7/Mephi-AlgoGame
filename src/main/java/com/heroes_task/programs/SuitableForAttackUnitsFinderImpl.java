package com.heroes_task.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.List;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {
    private static final int BOARD_HEIGHT = 21;

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByColumns, boolean isLeftArmyTarget) {
        List<Unit> suitableUnits = new ArrayList<>();
        // Left army target -> computer is attacked -> check for top cell.
        if (isLeftArmyTarget) {
            for (int x = 0; x < unitsByColumns.size(); ++x) {
                for (var unit : unitsByColumns.get(x)) {
                    var y = unit.getyCoordinate();
                    if (unit.isAlive() && (y == 0 || isCellEmpty(unitsByColumns, x, y - 1))) {
                        suitableUnits.add(unit);
                    }
                }
            }
            return suitableUnits;
        }
        // Otherwise, right army target -> player is attacked -> check for bottom cell.
        for (int x = 0; x < unitsByColumns.size(); ++x) {
            for (var unit : unitsByColumns.get(x)) {
                var y = unit.getyCoordinate();
                if (unit.isAlive() && (y == BOARD_HEIGHT - 1 || isCellEmpty(unitsByColumns, x, y + 1))) {
                    suitableUnits.add(unit);
                }
            }
        }
        return suitableUnits;
    }

    public boolean isCellEmpty(List<List<Unit>> units, int x, int y) {
        for (var unit : units.get(x)) {
            if (unit.getyCoordinate() == y && unit.isAlive()) {
                return false;
            }
        }
        return true;
    }
}
