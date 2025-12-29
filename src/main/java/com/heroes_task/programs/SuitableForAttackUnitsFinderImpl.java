package com.heroes_task.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.List;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {
    private static final int BOT_MIN_X_POSITION = 0;
    private static final int BOT_MAX_X_POSITION = 2;
    private static final int PLAYER_MIN_X_POSITION = 24;
    private static final int PLAYER_MAX_X_POSITION = 26;
    private static final int BOARD_HEIGHT = 21;

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        // Left army target -> player army is target
        if (isLeftArmyTarget) {
            return getTargets(unitsByRow, PLAYER_MIN_X_POSITION, PLAYER_MAX_X_POSITION);
        }
        // Otherwise -> bot army is target
        return getTargets(unitsByRow, BOT_MIN_X_POSITION, BOT_MAX_X_POSITION);
    }

    private List<Unit> getTargets(List<List<Unit>> unitsByRow, int minX, int maxX) {
        List<Unit> suitableUnits = new ArrayList<>();
        for (int y = 0; y < BOARD_HEIGHT; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                Unit unit = unitsByRow.get(y).get(x);
                if (unit != null) {
                    boolean isTopCellEmpty = (y == 0) || (unitsByRow.get(y - 1).get(x) == null);
                    boolean isBottomCellEmpty = (y == BOARD_HEIGHT - 1) || (unitsByRow.get(y + 1).get(x) == null);
                    if (isTopCellEmpty || isBottomCellEmpty) {
                        suitableUnits.add(unit);
                    }
                }
            }
        }
        return suitableUnits;
    }
}
