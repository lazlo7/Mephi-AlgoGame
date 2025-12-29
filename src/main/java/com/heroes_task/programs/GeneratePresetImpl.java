package com.heroes_task.programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {
    private static final int UNIT_TYPE_LIMIT = 11;
    private static final int MAX_X_POSITION = 2;
    private static final int MAX_Y_POSITION = 20;
    private static final Random RANDOM_GENERATOR = new Random();

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        var sortedUnits = getSortedUnits(unitList);
        var pickedUnits = new ArrayList<Unit>();
        for (Unit unitType : sortedUnits.reversed()) {
            if (maxPoints == 0) {
                break;
            }
            var unitsToTake = Math.min(UNIT_TYPE_LIMIT, maxPoints / unitType.getCost());
            for (int j = 0; j < unitsToTake; ++j) {
                pickedUnits.add(new Unit(
                        String.format("%s %d", unitType.getName(), j + 1),
                        unitType.getUnitType(),
                        unitType.getHealth(),
                        unitType.getBaseAttack(),
                        unitType.getCost(),
                        unitType.getAttackType(),
                        unitType.getAttackBonuses(),
                        unitType.getDefenceBonuses(),
                        0,
                        0
                ));
            }
            System.out.printf("Added %d units of type %s, attack: %d, health: %d, cost: %d%n",
                    unitsToTake, unitType.getName(), unitType.getBaseAttack(), unitType.getHealth(), unitType.getCost());
            maxPoints -= unitsToTake * unitType.getCost();
        }

        System.out.println("Remaining points: " + maxPoints);

        // Randomize x/y positions of units.
        // Create an array of all possible position pairs (x, y)
        var possiblePositions = new ArrayList<Map.Entry<Integer, Integer>>();
        for (int x = 0; x <= MAX_X_POSITION; ++x) {
            for (int y = 0; y <= MAX_Y_POSITION; ++y) {
                possiblePositions.add(new AbstractMap.SimpleEntry<>(x, y));
            }
        }

        // Shuffle the possible positions
        Collections.shuffle(possiblePositions, RANDOM_GENERATOR);

        // Assign positions to units
        for (int i = 0; i < pickedUnits.size(); ++i) {
            var position = possiblePositions.get(i);
            pickedUnits.get(i).setxCoordinate(position.getKey());
            pickedUnits.get(i).setyCoordinate(position.getValue());
        }

        return new Army(pickedUnits);
    }

    private static ArrayList<Unit> getSortedUnits(List<Unit> unitList) {
        var sortedUnits = new ArrayList<>(unitList);
        sortedUnits.sort((lhs, rhs) -> {
            var attackCostLHS = (double) lhs.getBaseAttack() / lhs.getCost();
            var attackCostRHS = (double) rhs.getBaseAttack() / rhs.getCost();
            var attackCompareResult = Double.compare(attackCostLHS, attackCostRHS);
            if (attackCompareResult != 0) {
                return attackCompareResult;
            }
            var healthCostLHS = (double) lhs.getHealth() / lhs.getCost();
            var healthCostRHS = (double) rhs.getHealth() / rhs.getCost();
            return Double.compare(healthCostLHS, healthCostRHS);
        });
        return sortedUnits;
    }
}