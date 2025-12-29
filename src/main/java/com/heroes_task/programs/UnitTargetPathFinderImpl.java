package com.heroes_task.programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.ArrayList;
import java.util.List;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        var path = new ArrayList<Edge>();
        var attackerX = attackUnit.getxCoordinate();
        var attackerY = attackUnit.getyCoordinate();
        path.add(new Edge(attackerX, attackerY));

        // Try to equalize both X and Y coordinates - move diagonally.
        while (attackerX != targetUnit.getxCoordinate() && attackerY != targetUnit.getyCoordinate()) {
            attackerX += Integer.signum(targetUnit.getxCoordinate() - attackerX);
            attackerY += Integer.signum(targetUnit.getyCoordinate() - attackerY);
            path.add(new Edge(attackerX, attackerY));
        }

        // Move in X direction if needed.
        while (attackerX != targetUnit.getxCoordinate()) {
            attackerX += Integer.signum(targetUnit.getxCoordinate() - attackerX);
            path.add(new Edge(attackerX, attackerY));
        }

        // Move in Y direction if needed.
        while (attackerY != targetUnit.getyCoordinate()) {
            attackerY += Integer.signum(targetUnit.getyCoordinate() - attackerY);
            path.add(new Edge(attackerX, attackerY));
        }

        return path;
    }
}
