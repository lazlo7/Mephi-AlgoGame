# Алгоритмы и струтктуры данных: проект
Алгоритмы для игры heroes.

## Сборка
Скопируйте зависимость `heroes_task_lib-1.0-SNAPSHOT.jar` в папку `libs`.  
Затем, соберите проект через gradle 8.5+:
```bash
./gradlew build
```
Собранный артефакт будет находиться в `build/libs/`.

## Алгоритмы
### 1. Generate Preset
Генерация армии компьютера. Мы должны собрать армию юнитов, 
в которую в первую очередь пойдут юниты с лучшим соотнешением атака и здоровья к стоимости. 
При этом, у нас есть ограничение по количеству каждого типа юнита и максимальной стоимости всей армии.  
Для решения этой задачи можно использовать жадный алгоритм: найдем лучших юнитов, 
из списка который нам дан, отсортировав список по соотношению атака/стоимость и здоровье/стоимость.
Затем будем постепенно брать максимальное количество юнитов, начиная с самых эффективных, пока
либо не закончатся доступные юниты, любо мы не потратим все очки.  
Таким образом, мы армию с максимально эффективными юнитами.  
  
Для начала напишем компаратор для сортировки типов юнитов по их эффективности:
```java
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
```
Затем определим основной метод.
```java
@Override
public Army generate(List<Unit> unitList, int maxPoints) {
    // Стандартная сортировка за O(n log n)
    var sortedUnits = getSortedUnits(unitList);
    var pickedUnits = new ArrayList<Unit>();
    // Проходимся по всем юнитам за n
    for (Unit unitType : sortedUnits.reversed()) {
        if (maxPoints == 0) {
            break;
        }
        // Пытаемся взять максимальное количество лучших юнитов.
        var unitsToTake = Math.min(UNIT_TYPE_LIMIT, maxPoints / unitType.getCost());
        // Добавляем до k юнитов в армию
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

    // Расставляем юнитов по случайным позициям на поле.
    // Это необязательно, но делает расстановку более интересной.
    
    // Randomize x/y positions of units.
    // Create an array of all possible position pairs (x, y)
    // Создаем все позиции за O(1), так как размеры поля фиксированы.
    var possiblePositions = new ArrayList<Map.Entry<Integer, Integer>>();
    for (int x = 0; x <= MAX_X_POSITION; ++x) {
        for (int y = 0; y <= MAX_Y_POSITION; ++y) {
            possiblePositions.add(new AbstractMap.SimpleEntry<>(x, y));
        }
    }

    // Shuffle the possible positions
    // Перемешиваем позиции за O(1), так как размер поля фиксирован.
    Collections.shuffle(possiblePositions, RANDOM_GENERATOR);

    // Assign positions to units
    // Назначаем позиции юнитам за O(n * k)
    for (int i = 0; i < pickedUnits.size(); ++i) {
        var position = possiblePositions.get(i);
        pickedUnits.get(i).setxCoordinate(position.getKey());
        pickedUnits.get(i).setyCoordinate(position.getValue());
    }

    return new Army(pickedUnits);
}
```
Сложность данного алгоритма равна O(n log n + nk + 1 + 1 + nk) = O(n log n + nk), где
- n - количество типов юнитов
- k - максимальное количество юнитов одного типа.

Если возьмем m = максимальное число юнитов в армии = n * k, 
то сложность алгоритма можно записать как O(n log n + m)
O(n log n + m) растет медленнее требуемого O(nm), значит алгоритм достаточно эффективен.

### 2. Simulate Battle
Необходимо смоделировать битву между двумя армиями.  
Предполагается, что армии ходят поочередно (в условие не сказано *как именно* могут ходить армии иначе).  
Первыми всегда должны ходить юниты с лучшей атакой. 
Заметим, что отсортировать юнитов можно один раз в начале, так как у нас не добавляются
новые юниты, а только удаляются погибшие. При удалении элемента из отсортированного массива, 
свойство сортировки сохраняется.  
Отсортируем юнитов и будем поочередно атаковать друг друга, пока одна из армий не останется без юнитов.  

```java
@Override
public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
    // Copy units from both armies and sort them by attack in descending order.
    // Сортируем юниты за O(n log n) времени.
    var playerUnits = new ArrayList<>(playerArmy.getUnits().stream()
            .sorted(Comparator.comparingInt(Unit::getBaseAttack).reversed())
            .toList());

    // Для простоты предположим, что размер армий одинаков, это не принципиально.
    // Сортируем юниты за O(n log n) времени.
    var computerUnits = new ArrayList<>(computerArmy.getUnits().stream()
            .sorted(Comparator.comparingInt(Unit::getBaseAttack).reversed())
            .toList());

    // Запоминаем текущие индексы юнитов.
    int currentPlayerUnitIdx = 0;
    int currentComputerUnitIdx = 0;
    // За каждый шаг цикла мы наносим урон одному юниту компьютера и одному юниту игрока.
    // Предположим, что здоровье юнита снижается на c, где c - константа (все статистики юнитов констатны).
    // Тогда можно предположить, что на каждом шаге цикла мы снижаем общее здоровье обеих армий на 2c.
    // Допустим самый длинный случай: обе армии бьются почти до самого последнего юнита.
    // Тогда будет сделано 2nL/c шагов цикла, где n - число юнитов в каждой армии, L - стоимость каждого шага.
    // L = O(4n) = O(n).
    // Таким образом, получаем, что цикл выполнится за O(n^2).
    while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
        var playerUnit = playerUnits.get(currentPlayerUnitIdx);
        var computerUnit = computerUnits.get(currentComputerUnitIdx);

        var attackedComputerUnit = playerUnit.getProgram().attack();
        // Этого никогда не должно случаться.
        if (attackedComputerUnit == null) {
            throw new IllegalStateException("Player unit did not return a target to attack.");
        }
        printBattleLog.printBattleLog(playerUnit, attackedComputerUnit);
        if (!attackedComputerUnit.isAlive()) {
            // Находим индекс - O(n)
            var deadComputerUnitIdx = computerUnits.indexOf(attackedComputerUnit);
            // Удаляем юнита - O(n)
            computerUnits.remove(attackedComputerUnit);
            if (currentComputerUnitIdx > deadComputerUnitIdx) {
                currentComputerUnitIdx--;
            }
            if (computerUnits.isEmpty()) {
                break;
            }
        }

        var attackedPlayerUnit = computerUnit.getProgram().attack();
        // Этого никогда не должно случаться.
        if (attackedPlayerUnit == null) {
            throw new IllegalStateException("Computer unit did not return a target to attack.");
        }
        printBattleLog.printBattleLog(computerUnit, attackedPlayerUnit);
        if (!attackedPlayerUnit.isAlive()) {
            // Находим индекс - O(n)
            var deadPlayerUnitIdx = playerUnits.indexOf(attackedPlayerUnit);
            // Удаляем юнита - O(n)
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
```
Сложность данного алгоритма равна O(n log n + n log n + n^2) = O(n^2), где n - количество юнитов в каждой армии.  
Это удовлетворяет требуемой сложности O(n^2 log n).

### 3. Suitable Units
Необходимо найти юнитов, которых можно атаковать.  
Исходя из условия, достаточно проверить, что юнит-цель имеет свободную ячейку над собой (если находится слева) 
или под собой (если находится справа).  

Определим функцию для проверки, что ячейка (x, y) пуста.
```java
public boolean isCellEmpty(List<List<Unit>> units, int x, int y) {
    // Проверяем все юниты в колонке x.
    // Цикл выполнится за O(H), где H - высота игрового поля.
    for (var unit : units.get(x)) {
        if (unit.getyCoordinate() == y && unit.isAlive()) {
            return false;
        }
    }
    return true;
}
```
Затем реализуем основной метод.
```java
@Override
public List<Unit> getSuitableUnits(List<List<Unit>> unitsByColumns, boolean isLeftArmyTarget) {
    List<Unit> suitableUnits = new ArrayList<>();
    // Left army target -> computer is attacked -> check for top cell.
    if (isLeftArmyTarget) {
        // O(C) шагов, где C - количество колонок для юнитов.
        for (int x = 0; x < unitsByColumns.size(); ++x) {
            // O(H) шагов, где H - высота игрового поля.
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
    // O(C) шагов, где C - количество колонок для юнитов.
    for (int x = 0; x < unitsByColumns.size(); ++x) {
        // O(H) шагов, где H - высота игрового поля.
        for (var unit : unitsByColumns.get(x)) {
            var y = unit.getyCoordinate();
            if (unit.isAlive() && (y == BOARD_HEIGHT - 1 || isCellEmpty(unitsByColumns, x, y + 1))) {
                suitableUnits.add(unit);
            }
        }
    }
    return suitableUnits;
}
```
Сложность данного алгоритма равна O(C * H + C * H) = O(C * H), где 
- C - количество колонок на игровом поле,
- H - высота игрового поля.

Это удовлетворяет требуемой сложности O(nm).

### 4. Get Target Path
Необходимо найти кратчайший путь от атакующего юнита к цели.  
Из условия (и предыдущих пунктов) не понятно, как к потенциальной цели можно подойти.  
Ведь, если брать условие для алгоритма *Suitable Units*, то цель может быть полностью огорожена
другими юнитами, но при этом все равно должна считаться достижимой, 
если есть свободная ячейка над или под ней.  
Например цель в следующей конфигурации *достижима по правилам*, 
но при этом к ней не понятно как надо добираться:  
X X X  
_ X X  
T X X  
X X X  
X - юнит, T - цель, _ - свободная ячейка. К данной цели нельзя построить вменяемый путь, 
но при этом к ней можно по правилам *подобраться*. 

Ввиду нечеткости и неоднозначности условий задачи, был реализован простой алгоритм 
поиска кратчайшего пути, который игнорирует препятствия (юниты). 
Данный алгоритм может двигаться по диагонали.
```java
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
```
За весь алгоритм мы не сделаем больше W * H шагов, так как всегда двигаемся к цели 
на как минимум одну единицу расстояния. Значит наш алгоритм работает за O(W * H) времени, где:
- W - ширина игрового поля,
- H - высота игрового поля.

Это удовлетворяет требуемой сложности.
