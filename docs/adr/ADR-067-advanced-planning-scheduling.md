# ADR-067: Advanced Planning & Scheduling (APS)

**Status**: Accepted (Implementation Starting)  
**Date**: 2026-02-06  
**Deciders**: Architecture Team, Manufacturing Team, SCM Team  
**Priority**: P2 (Medium - Manufacturing Enhancement)  
**Tier**: Platform Enhancement  
**Tags**: manufacturing, aps, scheduling, mrp, capacity-planning, optimization

---

## Context

**Problem**: ChiroERP has **basic MRP (Material Requirements Planning)** but lacks **Advanced Planning & Scheduling (APS)** capabilities critical for manufacturing:

- **Current capability**: Simple MRP calculates material requirements
- **Missing**: Finite capacity scheduling, constraint-based optimization, what-if scenarios
- **Lost deals**: 35% of manufacturing prospects require APS (especially discrete manufacturing)
- **Manual workarounds**: Customers use Excel or external APS tools (SAP APO, Siemens Opcenter)
- **Production delays**: 20-30% due to suboptimal scheduling

**Manufacturing Pain Points**:

| Challenge | Current State | Impact |
|-----------|---------------|--------|
| **Capacity constraints** | Ignores machine/labor capacity | 25% overload → delays |
| **Material shortages** | No forward-looking visibility | 15% rush orders |
| **Changeover time** | No sequence optimization | 10% waste |
| **Multi-site coordination** | Manual phone calls/emails | 20% lead time variance |
| **Customer promises** | Cannot ATP (Available-to-Promise) | 30% delivery misses |
| **What-if scenarios** | No simulation capability | Reactive vs proactive |

**Competitive Reality**:

| ERP System | MRP | Finite Capacity | Constraint Optimization | What-If Scenarios | ATP/CTP |
|------------|-----|-----------------|------------------------|-------------------|---------|
| **SAP S/4HANA** | ✅ Advanced | ✅ PP/DS | ✅ Optimizer | ✅ Full | ✅ Global ATP |
| **Oracle Fusion** | ✅ Advanced | ✅ APS | ✅ Optimizer | ✅ Full | ✅ Global ATP |
| **Dynamics 365** | ✅ Good | ⚠️ Limited | ❌ No | ⚠️ Limited | ⚠️ Limited |
| **Infor CloudSuite** | ✅ Advanced | ✅ APS | ✅ Optimizer | ✅ Full | ✅ Full |
| **ChiroERP** | ⚠️ Basic MRP | ❌ **None** | ❌ **None** | ❌ **None** | ❌ **None** |

**Customer Quote** (VP Operations, Automotive Tier 2 Supplier):
> "We have 50 CNC machines with different capabilities, 3 shifts, setup times ranging from 15 minutes to 4 hours, and hard customer delivery dates. Your MRP tells us *what* to make, but not *when* or *where*. We need constraint-based scheduling that considers machine capacity, tooling availability, and operator skills."

**APS Market**:
- $3.5B market, 8.5% CAGR
- Standalone APS tools (Siemens Opcenter, Dassault DELMIA, Ortec): $50K-500K/year
- 60% of manufacturers with >$100M revenue use APS
- ChiroERP missing **$2M-5M ARR** opportunity (20-50 manufacturing customers)

---

## Decision

Build **Advanced Planning & Scheduling (APS)** module providing:

1. **Finite Capacity Scheduling**
   - Machine capacity (hours, throughput)
   - Labor capacity (skills, shifts)
   - Tooling/fixture availability
   - Queue time, setup time, run time

2. **Constraint-Based Optimization**
   - Minimize makespan (total production time)
   - Minimize tardiness (late deliveries)
   - Minimize changeover time
   - Maximize throughput
   - Balance workload

3. **Material & Capacity Planning**
   - Forward scheduling (ASAP)
   - Backward scheduling (from due date)
   - Pegging (demand → supply link)
   - Shortage resolution

4. **Available-to-Promise (ATP)**
   - Real-time availability check
   - Multi-level ATP (components + capacity)
   - Capable-to-Promise (CTP) with scheduling

5. **What-If Scenarios**
   - Simulate demand changes
   - Simulate capacity changes (new machine, breakdown)
   - Compare scenarios side-by-side

6. **Visual Scheduling Board**
   - Gantt chart (machine timeline)
   - Drag-and-drop rescheduling
   - Real-time constraint violations

**Target**: Q3 2027 - Q1 2028 (6 months)

---

## Architecture

### 1. APS Module Structure

**New Module**: `manufacturing/advanced-planning-scheduling/`

```
manufacturing/
├── advanced-planning-scheduling/
│   ├── core/
│   │   ├── APSEngine.kt                  # Main scheduling engine
│   │   ├── ScheduleOptimizer.kt          # Optimization algorithms
│   │   ├── ConstraintValidator.kt        # Constraint checking
│   │   └── CapacityCalculator.kt         # Capacity availability
│   ├── scheduling/
│   │   ├── ForwardScheduler.kt           # ASAP scheduling
│   │   ├── BackwardScheduler.kt          # From due date
│   │   ├── FiniteCapacityScheduler.kt    # Constraint-aware
│   │   └── DragDropRescheduler.kt        # Manual adjustments
│   ├── optimization/
│   │   ├── GeneticAlgorithm.kt           # GA optimizer
│   │   ├── SimulatedAnnealing.kt         # SA optimizer
│   │   ├── ConstraintProgramming.kt      # CP-SAT solver
│   │   └── HeuristicScheduler.kt         # Fast heuristics
│   ├── atp/
│   │   ├── ATPService.kt                 # Available-to-Promise
│   │   ├── CTPService.kt                 # Capable-to-Promise
│   │   └── MultiLevelATP.kt              # Component + capacity
│   ├── scenarios/
│   │   ├── ScenarioManager.kt            # What-if scenarios
│   │   ├── ScenarioComparator.kt         # Compare scenarios
│   │   └── ScenarioSimulator.kt          # Simulate changes
│   ├── visualization/
│   │   ├── GanttChartService.kt          # Gantt timeline
│   │   ├── CapacityChartService.kt       # Capacity utilization
│   │   └── ScheduleBoardWebSocket.kt     # Real-time updates
│   └── models/
│       ├── WorkCenter.kt                 # Machine/resource
│       ├── Operation.kt                  # Manufacturing step
│       ├── ScheduledOperation.kt         # Scheduled step
│       └── CapacityBucket.kt             # Time bucket capacity
└── mrp/                                  # Existing MRP module
    └── MRPService.kt                     # Enhanced with APS integration
```

---

### 2. Core Scheduling Engine

**APS Engine** (Orchestrates Scheduling):

```kotlin
/**
 * Advanced Planning & Scheduling Engine
 * Main orchestrator for constraint-based scheduling
 */
@Service
class APSEngine(
    private val workCenterRepository: WorkCenterRepository,
    private val productionOrderRepository: ProductionOrderRepository,
    private val finiteCapacityScheduler: FiniteCapacityScheduler,
    private val scheduleOptimizer: ScheduleOptimizer,
    private val constraintValidator: ConstraintValidator,
    private val capacityCalculator: CapacityCalculator,
    private val eventPublisher: EventPublisher
) {
    
    /**
     * Generate optimized production schedule
     */
    suspend fun generateSchedule(
        tenantId: UUID,
        planningHorizon: PlanningHorizon,
        schedulingStrategy: SchedulingStrategy,
        optimizationObjective: OptimizationObjective
    ): Schedule {
        logger.info("Generating APS schedule for tenant $tenantId")
        
        val startTime = System.currentTimeMillis()
        
        // Step 1: Load production orders (demand)
        val productionOrders = productionOrderRepository.findByHorizon(
            tenantId = tenantId,
            startDate = planningHorizon.startDate,
            endDate = planningHorizon.endDate,
            statuses = listOf(ProductionOrderStatus.RELEASED, ProductionOrderStatus.CONFIRMED)
        )
        
        logger.info("Loaded ${productionOrders.size} production orders")
        
        // Step 2: Load work centers (capacity)
        val workCenters = workCenterRepository.findByTenant(tenantId)
        
        logger.info("Loaded ${workCenters.size} work centers")
        
        // Step 3: Calculate available capacity
        val capacityBuckets = capacityCalculator.calculateCapacity(
            workCenters = workCenters,
            startDate = planningHorizon.startDate,
            endDate = planningHorizon.endDate,
            bucketSize = Duration.ofHours(1) // 1-hour buckets
        )
        
        // Step 4: Finite capacity scheduling
        val initialSchedule = finiteCapacityScheduler.schedule(
            productionOrders = productionOrders,
            workCenters = workCenters,
            capacityBuckets = capacityBuckets,
            strategy = schedulingStrategy
        )
        
        logger.info("Initial schedule created with ${initialSchedule.operations.size} operations")
        
        // Step 5: Validate constraints
        val violations = constraintValidator.validate(initialSchedule)
        
        if (violations.isNotEmpty()) {
            logger.warn("${violations.size} constraint violations detected")
        }
        
        // Step 6: Optimize schedule (if requested)
        val optimizedSchedule = if (optimizationObjective != OptimizationObjective.NONE) {
            scheduleOptimizer.optimize(
                schedule = initialSchedule,
                objective = optimizationObjective,
                maxIterations = 10000,
                timeLimit = Duration.ofMinutes(5)
            )
        } else {
            initialSchedule
        }
        
        // Step 7: Calculate KPIs
        val kpis = calculateScheduleKPIs(optimizedSchedule)
        
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.info("Schedule generated in ${elapsedTime}ms: makespan=${kpis.makespan}, tardiness=${kpis.totalTardiness}")
        
        // Step 8: Publish schedule created event
        eventPublisher.publish(
            ScheduleCreatedEvent(
                tenantId = tenantId,
                scheduleId = optimizedSchedule.id,
                operations = optimizedSchedule.operations.size,
                kpis = kpis,
                generatedAt = Instant.now()
            )
        )
        
        return optimizedSchedule
    }
    
    /**
     * Calculate schedule KPIs
     */
    private fun calculateScheduleKPIs(schedule: Schedule): ScheduleKPIs {
        val completionTimes = schedule.operations.map { it.endTime }
        val makespan = completionTimes.maxOrNull() ?: Instant.now()
        
        val tardiness = schedule.operations
            .filter { it.dueDate != null && it.endTime.isAfter(it.dueDate) }
            .sumOf { Duration.between(it.dueDate, it.endTime).toMinutes() }
        
        val setupTime = schedule.operations.sumOf { it.setupTime.toMinutes() }
        val runTime = schedule.operations.sumOf { it.runTime.toMinutes() }
        val totalTime = setupTime + runTime
        
        val utilizationByWorkCenter = schedule.operations
            .groupBy { it.workCenterId }
            .mapValues { (_, ops) ->
                val productive = ops.sumOf { it.setupTime.toMinutes() + it.runTime.toMinutes() }
                val available = Duration.between(schedule.startDate, schedule.endDate).toMinutes()
                (productive / available.toDouble() * 100).roundToInt()
            }
        
        return ScheduleKPIs(
            makespan = makespan,
            totalTardiness = tardiness,
            avgUtilization = utilizationByWorkCenter.values.average().toInt(),
            setupTime = setupTime,
            runTime = runTime,
            totalTime = totalTime,
            setupPercentage = (setupTime / totalTime.toDouble() * 100).roundToInt(),
            utilizationByWorkCenter = utilizationByWorkCenter
        )
    }
}

data class Schedule(
    val id: UUID,
    val tenantId: UUID,
    val startDate: Instant,
    val endDate: Instant,
    val operations: List<ScheduledOperation>,
    val kpis: ScheduleKPIs,
    val status: ScheduleStatus,
    val createdAt: Instant,
    val createdBy: UUID
)

data class ScheduledOperation(
    val id: UUID,
    val productionOrderId: UUID,
    val operationNumber: Int,
    val workCenterId: UUID,
    val startTime: Instant,
    val endTime: Instant,
    val setupTime: Duration,
    val runTime: Duration,
    val queueTime: Duration,
    val dueDate: Instant?,
    val priority: Int,
    val status: OperationStatus
)

enum class SchedulingStrategy {
    FORWARD,            // ASAP (as soon as possible)
    BACKWARD,           // From due date
    MIDDLE_OUT,         // Critical operations first
    PRIORITY_BASED      // By order priority
}

enum class OptimizationObjective {
    NONE,               // No optimization (heuristic only)
    MINIMIZE_MAKESPAN,  // Shortest total time
    MINIMIZE_TARDINESS, // Minimize late deliveries
    MINIMIZE_CHANGEOVER, // Reduce setup time
    MAXIMIZE_THROUGHPUT, // Maximum output
    BALANCE_WORKLOAD    // Even utilization
}

data class ScheduleKPIs(
    val makespan: Instant,
    val totalTardiness: Long,           // Minutes late
    val avgUtilization: Int,            // % capacity used
    val setupTime: Long,                // Minutes
    val runTime: Long,                  // Minutes
    val totalTime: Long,                // Minutes
    val setupPercentage: Int,           // % of total time
    val utilizationByWorkCenter: Map<UUID, Int>
)
```

---

### 3. Finite Capacity Scheduler

**Constraint-Aware Scheduling**:

```kotlin
/**
 * Finite Capacity Scheduler
 * Schedules operations considering capacity constraints
 */
@Service
class FiniteCapacityScheduler(
    private val workCenterRepository: WorkCenterRepository,
    private val routingService: RoutingService,
    private val capacityCalculator: CapacityCalculator
) {
    
    /**
     * Schedule production orders with finite capacity
     */
    suspend fun schedule(
        productionOrders: List<ProductionOrder>,
        workCenters: List<WorkCenter>,
        capacityBuckets: Map<UUID, List<CapacityBucket>>,
        strategy: SchedulingStrategy
    ): Schedule {
        logger.info("Finite capacity scheduling ${productionOrders.size} orders")
        
        // Step 1: Sort orders by strategy
        val sortedOrders = when (strategy) {
            SchedulingStrategy.FORWARD -> productionOrders.sortedBy { it.startDate }
            SchedulingStrategy.BACKWARD -> productionOrders.sortedBy { it.dueDate }
            SchedulingStrategy.PRIORITY_BASED -> productionOrders.sortedByDescending { it.priority }
            SchedulingStrategy.MIDDLE_OUT -> {
                // Critical ratio = (due date - today) / remaining lead time
                productionOrders.sortedBy { order ->
                    val remainingTime = Duration.between(Instant.now(), order.dueDate).toHours()
                    val leadTime = order.estimatedLeadTime.toHours()
                    remainingTime.toDouble() / leadTime
                }
            }
        }
        
        // Step 2: Initialize schedule
        val scheduledOperations = mutableListOf<ScheduledOperation>()
        val workCenterSchedules = mutableMapOf<UUID, MutableList<TimeSlot>>()
        
        workCenters.forEach { wc ->
            workCenterSchedules[wc.id] = mutableListOf()
        }
        
        // Step 3: Schedule each order
        for (order in sortedOrders) {
            // Get routing (sequence of operations)
            val routing = routingService.getRouting(order.itemId)
            
            var previousEndTime = when (strategy) {
                SchedulingStrategy.FORWARD -> order.startDate
                SchedulingStrategy.BACKWARD -> order.dueDate
                else -> Instant.now()
            }
            
            // Schedule each operation in routing
            for (operation in routing.operations) {
                val workCenter = workCenters.find { it.id == operation.workCenterId }
                    ?: continue
                
                // Calculate operation times
                val setupTime = calculateSetupTime(
                    workCenter = workCenter,
                    previousOperation = scheduledOperations.lastOrNull { it.workCenterId == workCenter.id },
                    currentOperation = operation
                )
                
                val runTime = calculateRunTime(
                    operation = operation,
                    quantity = order.quantity,
                    workCenter = workCenter
                )
                
                val totalTime = setupTime + runTime
                
                // Find available time slot
                val timeSlot = when (strategy) {
                    SchedulingStrategy.BACKWARD -> {
                        findBackwardTimeSlot(
                            workCenterId = workCenter.id,
                            duration = totalTime,
                            endBefore = previousEndTime,
                            workCenterSchedule = workCenterSchedules[workCenter.id]!!,
                            capacityBuckets = capacityBuckets[workCenter.id]!!
                        )
                    }
                    else -> {
                        findForwardTimeSlot(
                            workCenterId = workCenter.id,
                            duration = totalTime,
                            startAfter = previousEndTime,
                            workCenterSchedule = workCenterSchedules[workCenter.id]!!,
                            capacityBuckets = capacityBuckets[workCenter.id]!!
                        )
                    }
                }
                
                // Create scheduled operation
                val scheduledOp = ScheduledOperation(
                    id = UUID.randomUUID(),
                    productionOrderId = order.id,
                    operationNumber = operation.operationNumber,
                    workCenterId = workCenter.id,
                    startTime = timeSlot.start,
                    endTime = timeSlot.end,
                    setupTime = setupTime,
                    runTime = runTime,
                    queueTime = Duration.ZERO,
                    dueDate = order.dueDate,
                    priority = order.priority,
                    status = OperationStatus.SCHEDULED
                )
                
                scheduledOperations.add(scheduledOp)
                workCenterSchedules[workCenter.id]!!.add(timeSlot)
                
                previousEndTime = timeSlot.end
            }
        }
        
        return Schedule(
            id = UUID.randomUUID(),
            tenantId = productionOrders.first().tenantId,
            startDate = scheduledOperations.minOf { it.startTime },
            endDate = scheduledOperations.maxOf { it.endTime },
            operations = scheduledOperations,
            kpis = ScheduleKPIs(
                makespan = scheduledOperations.maxOf { it.endTime },
                totalTardiness = 0,
                avgUtilization = 0,
                setupTime = 0,
                runTime = 0,
                totalTime = 0,
                setupPercentage = 0,
                utilizationByWorkCenter = emptyMap()
            ),
            status = ScheduleStatus.DRAFT,
            createdAt = Instant.now(),
            createdBy = UUID.randomUUID()
        )
    }
    
    /**
     * Find forward time slot (ASAP scheduling)
     */
    private fun findForwardTimeSlot(
        workCenterId: UUID,
        duration: Duration,
        startAfter: Instant,
        workCenterSchedule: List<TimeSlot>,
        capacityBuckets: List<CapacityBucket>
    ): TimeSlot {
        var candidateStart = startAfter
        
        while (true) {
            val candidateEnd = candidateStart.plus(duration)
            
            // Check if slot overlaps with existing operations
            val hasOverlap = workCenterSchedule.any { existing ->
                candidateStart.isBefore(existing.end) && candidateEnd.isAfter(existing.start)
            }
            
            if (!hasOverlap) {
                // Check if capacity is available
                val hasCapacity = hasCapacityInTimeRange(
                    capacityBuckets = capacityBuckets,
                    start = candidateStart,
                    end = candidateEnd
                )
                
                if (hasCapacity) {
                    return TimeSlot(candidateStart, candidateEnd)
                }
            }
            
            // Move to next available slot
            candidateStart = candidateStart.plus(Duration.ofHours(1))
        }
    }
    
    /**
     * Calculate setup time (sequence-dependent)
     */
    private fun calculateSetupTime(
        workCenter: WorkCenter,
        previousOperation: ScheduledOperation?,
        currentOperation: RoutingOperation
    ): Duration {
        // If same product family, reduced setup
        if (previousOperation != null && 
            isSameProductFamily(previousOperation.productionOrderId, currentOperation.itemId)) {
            return Duration.ofMinutes(workCenter.minSetupTime)
        }
        
        // Full setup required
        return Duration.ofMinutes(workCenter.avgSetupTime)
    }
    
    /**
     * Calculate run time
     */
    private fun calculateRunTime(
        operation: RoutingOperation,
        quantity: BigDecimal,
        workCenter: WorkCenter
    ): Duration {
        val cycleTime = operation.cycleTime // seconds per unit
        val totalSeconds = (cycleTime.toLong() * quantity.toLong()) / workCenter.efficiency
        
        return Duration.ofSeconds(totalSeconds)
    }
}

data class WorkCenter(
    val id: UUID,
    val tenantId: UUID,
    val code: String,
    val name: String,
    val type: WorkCenterType,
    val capacity: BigDecimal,          // Units per hour
    val efficiency: Int,               // % efficiency (100 = 100%)
    val minSetupTime: Long,            // Minutes
    val avgSetupTime: Long,            // Minutes
    val maxSetupTime: Long,            // Minutes
    val operatorsRequired: Int,
    val shifts: List<Shift>,
    val status: WorkCenterStatus
)

enum class WorkCenterType {
    MACHINE,
    ASSEMBLY_LINE,
    WORK_CELL,
    INSPECTION,
    PACKAGING
}

data class Shift(
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val daysOfWeek: List<DayOfWeek>
)

data class CapacityBucket(
    val workCenterId: UUID,
    val startTime: Instant,
    val endTime: Instant,
    val availableCapacity: BigDecimal,  // Hours
    val usedCapacity: BigDecimal = BigDecimal.ZERO
) {
    val remainingCapacity: BigDecimal
        get() = availableCapacity - usedCapacity
}

data class TimeSlot(
    val start: Instant,
    val end: Instant
)
```

---

### 4. Schedule Optimizer

**Genetic Algorithm Optimizer**:

```kotlin
/**
 * Schedule Optimizer
 * Uses genetic algorithm to optimize schedules
 */
@Service
class ScheduleOptimizer {
    
    /**
     * Optimize schedule using genetic algorithm
     */
    fun optimize(
        schedule: Schedule,
        objective: OptimizationObjective,
        maxIterations: Int,
        timeLimit: Duration
    ): Schedule {
        logger.info("Optimizing schedule with objective: $objective")
        
        val startTime = System.currentTimeMillis()
        
        // Step 1: Initialize population (random variations)
        val populationSize = 100
        var population = generateInitialPopulation(schedule, populationSize)
        
        // Step 2: Evolve population
        var generation = 0
        var bestSchedule = population.minByOrNull { evaluateFitness(it, objective) }!!
        
        while (generation < maxIterations && 
               Duration.ofMillis(System.currentTimeMillis() - startTime) < timeLimit) {
            
            // Selection (tournament)
            val parents = selectParents(population, objective)
            
            // Crossover
            val offspring = crossover(parents)
            
            // Mutation
            val mutated = mutate(offspring)
            
            // Evaluate fitness
            population = (population + mutated)
                .sortedBy { evaluateFitness(it, objective) }
                .take(populationSize)
            
            // Update best
            val generationBest = population.first()
            if (evaluateFitness(generationBest, objective) < evaluateFitness(bestSchedule, objective)) {
                bestSchedule = generationBest
                logger.debug("Generation $generation: improved fitness = ${evaluateFitness(bestSchedule, objective)}")
            }
            
            generation++
        }
        
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.info("Optimization complete: $generation generations in ${elapsedTime}ms")
        
        return bestSchedule
    }
    
    /**
     * Evaluate fitness function
     */
    private fun evaluateFitness(schedule: Schedule, objective: OptimizationObjective): Double {
        return when (objective) {
            OptimizationObjective.MINIMIZE_MAKESPAN -> {
                val makespan = schedule.operations.maxOf { it.endTime }.toEpochMilli().toDouble()
                makespan
            }
            OptimizationObjective.MINIMIZE_TARDINESS -> {
                schedule.operations
                    .filter { it.dueDate != null && it.endTime.isAfter(it.dueDate) }
                    .sumOf { Duration.between(it.dueDate, it.endTime).toMinutes().toDouble() }
            }
            OptimizationObjective.MINIMIZE_CHANGEOVER -> {
                schedule.operations.sumOf { it.setupTime.toMinutes().toDouble() }
            }
            OptimizationObjective.MAXIMIZE_THROUGHPUT -> {
                -schedule.operations.size.toDouble() // Negative because we minimize fitness
            }
            OptimizationObjective.BALANCE_WORKLOAD -> {
                val utilizations = schedule.kpis.utilizationByWorkCenter.values
                val mean = utilizations.average()
                val variance = utilizations.map { (it - mean).pow(2) }.average()
                variance
            }
            OptimizationObjective.NONE -> 0.0
        }
    }
    
    /**
     * Crossover (combine two parent schedules)
     */
    private fun crossover(parents: List<Schedule>): List<Schedule> {
        val offspring = mutableListOf<Schedule>()
        
        for (i in parents.indices step 2) {
            if (i + 1 < parents.size) {
                val parent1 = parents[i]
                val parent2 = parents[i + 1]
                
                // Single-point crossover
                val crossoverPoint = parent1.operations.size / 2
                
                val child1Operations = parent1.operations.take(crossoverPoint) +
                                       parent2.operations.drop(crossoverPoint)
                
                val child2Operations = parent2.operations.take(crossoverPoint) +
                                       parent1.operations.drop(crossoverPoint)
                
                offspring.add(parent1.copy(id = UUID.randomUUID(), operations = child1Operations))
                offspring.add(parent2.copy(id = UUID.randomUUID(), operations = child2Operations))
            }
        }
        
        return offspring
    }
    
    /**
     * Mutation (random changes)
     */
    private fun mutate(schedules: List<Schedule>): List<Schedule> {
        val mutationRate = 0.1 // 10% mutation rate
        
        return schedules.map { schedule ->
            if (Random.nextDouble() < mutationRate) {
                // Swap two operations
                val ops = schedule.operations.toMutableList()
                val i = Random.nextInt(ops.size)
                val j = Random.nextInt(ops.size)
                
                val temp = ops[i]
                ops[i] = ops[j]
                ops[j] = temp
                
                schedule.copy(id = UUID.randomUUID(), operations = ops)
            } else {
                schedule
            }
        }
    }
}
```

---

### 5. Available-to-Promise (ATP)

**Real-Time Availability Check**:

```kotlin
/**
 * Available-to-Promise Service
 * Real-time availability calculation
 */
@Service
class ATPService(
    private val inventoryService: InventoryService,
    private val productionOrderRepository: ProductionOrderRepository,
    private val apsEngine: APSEngine
) {
    
    /**
     * Check ATP (Available-to-Promise)
     */
    suspend fun checkATP(
        tenantId: UUID,
        itemId: UUID,
        quantity: BigDecimal,
        requestedDate: LocalDate
    ): ATPResult {
        logger.info("Checking ATP for item $itemId, qty $quantity, date $requestedDate")
        
        // Step 1: Check on-hand inventory
        val onHand = inventoryService.getOnHand(tenantId, itemId)
        
        // Step 2: Check committed quantity (sales orders)
        val committed = inventoryService.getCommitted(tenantId, itemId)
        
        // Step 3: Check planned receipts (production orders, purchase orders)
        val plannedReceipts = getPlannedReceipts(tenantId, itemId, requestedDate)
        
        // Step 4: Calculate ATP
        val availableNow = onHand - committed
        val availableByDate = availableNow + plannedReceipts
        
        val isAvailable = availableByDate >= quantity
        
        // Step 5: If not available, calculate CTP (Capable-to-Promise)
        val ctpDate = if (!isAvailable) {
            calculateCTP(tenantId, itemId, quantity, requestedDate)
        } else {
            null
        }
        
        return ATPResult(
            itemId = itemId,
            requestedQuantity = quantity,
            requestedDate = requestedDate,
            onHand = onHand,
            committed = committed,
            plannedReceipts = plannedReceipts,
            available = availableByDate,
            isAvailable = isAvailable,
            alternativeDate = ctpDate
        )
    }
    
    /**
     * Calculate CTP (Capable-to-Promise) with scheduling
     */
    private suspend fun calculateCTP(
        tenantId: UUID,
        itemId: UUID,
        quantity: BigDecimal,
        requestedDate: LocalDate
    ): LocalDate? {
        // Create hypothetical production order
        val hypotheticalOrder = ProductionOrder(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            itemId = itemId,
            quantity = quantity,
            startDate = Instant.now(),
            dueDate = requestedDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            priority = 1,
            status = ProductionOrderStatus.PLANNED,
            estimatedLeadTime = Duration.ofDays(7)
        )
        
        // Run APS to schedule
        val schedule = apsEngine.generateSchedule(
            tenantId = tenantId,
            planningHorizon = PlanningHorizon(
                startDate = Instant.now(),
                endDate = requestedDate.plusMonths(3).atStartOfDay().toInstant(ZoneOffset.UTC)
            ),
            schedulingStrategy = SchedulingStrategy.FORWARD,
            optimizationObjective = OptimizationObjective.NONE
        )
        
        // Find completion date for hypothetical order
        val completionDate = schedule.operations
            .filter { it.productionOrderId == hypotheticalOrder.id }
            .maxOfOrNull { it.endTime }
            ?.atZone(ZoneOffset.UTC)
            ?.toLocalDate()
        
        return completionDate
    }
}

data class ATPResult(
    val itemId: UUID,
    val requestedQuantity: BigDecimal,
    val requestedDate: LocalDate,
    val onHand: BigDecimal,
    val committed: BigDecimal,
    val plannedReceipts: BigDecimal,
    val available: BigDecimal,
    val isAvailable: Boolean,
    val alternativeDate: LocalDate?
)
```

---

## Implementation Roadmap

### Phase 1: Core Scheduling (Q3 2027)

**Deliverables**:
- [ ] Work center model (capacity, shifts, efficiency)
- [ ] Finite capacity scheduler (forward/backward)
- [ ] Capacity calculator (buckets, availability)
- [ ] Gantt chart visualization

**Timeline**: 10 weeks (July-September 2027)
**Resources**: 2 backend engineers, 1 frontend engineer

### Phase 2: Optimization & ATP (Q4 2027)

**Deliverables**:
- [ ] Genetic algorithm optimizer
- [ ] Constraint programming (CP-SAT)
- [ ] ATP/CTP service
- [ ] Drag-and-drop rescheduling

**Timeline**: 10 weeks (October-December 2027)
**Resources**: 2 backend engineers, 1 optimization specialist

### Phase 3: Scenarios & Advanced Features (Q1 2028)

**Deliverables**:
- [ ] What-if scenarios (simulation)
- [ ] Scenario comparison
- [ ] Multi-site coordination
- [ ] Real-time schedule updates (WebSocket)

**Timeline**: 8 weeks (January-February 2028)
**Resources**: 2 backend engineers, 1 frontend engineer

---

## Cost Estimate

### Development Costs

| Phase | Timeline | Cost | Team |
|-------|----------|------|------|
| **Phase 1**: Core Scheduling | 10 weeks | $230K-300K | 2 BE + 1 FE |
| **Phase 2**: Optimization & ATP | 10 weeks | $280K-360K | 2 BE + 1 specialist |
| **Phase 3**: Scenarios & Advanced | 8 weeks | $190K-250K | 2 BE + 1 FE |
| **Total Development** | **28 weeks** | **$700K-910K** | |

### Infrastructure & Services

| Item | Cost/Year | Notes |
|------|-----------|-------|
| **OR-Tools (CP-SAT)** | Free | Google open-source |
| **Compute (optimization)** | $10K-20K | Extra compute for GA/SA |
| **WebSocket infrastructure** | $5K-10K | Real-time updates |
| **Total Infrastructure** | **$15K-30K** | |

### Total Investment: **$715K-940K** (first year)

---

## Success Metrics

### Business KPIs
- ✅ **Manufacturing customers with APS**: 30+ by end 2028
- ✅ **Incremental revenue**: $2M+ ARR (APS upsell)
- ✅ **Average deal size**: +25% ($50K → $62.5K)
- ✅ **Customer retention**: +15% (APS increases stickiness)

### Technical KPIs
- ✅ **Schedule generation**: <5 minutes (1000 operations)
- ✅ **Optimization improvement**: 15-30% (vs heuristic)
- ✅ **ATP response time**: <500ms
- ✅ **Schedule accuracy**: >85% (actual vs planned)

### Operational KPIs
- ✅ **On-time delivery**: +20% (60% → 80%)
- ✅ **Capacity utilization**: +15% (65% → 80%)
- ✅ **Setup time reduction**: 10-15%
- ✅ **Rush orders**: -30% (better planning)

---

## Integration with Other ADRs

- **ADR-009**: Financial GL (production costs)
- **ADR-040**: Manufacturing Execution (work order dispatch)
- **ADR-041**: Inventory Management (ATP, on-hand)
- **ADR-058**: SOC 2 (audit trails)

---

## Consequences

### Positive ✅
- **Competitive parity**: SAP/Oracle level scheduling
- **Revenue growth**: $2M+ ARR from APS upsell
- **Customer satisfaction**: On-time delivery +20%
- **Operational efficiency**: Capacity utilization +15%

### Negative ⚠️
- **Complexity**: Scheduling algorithms are complex
- **Performance**: Large schedules (10K+ operations) may be slow
- **Data quality**: APS requires accurate routing/capacity data
- **Change management**: Users must trust automated schedules

### Risks 🚨
- **Optimization time**: May exceed 5-minute limit for large problems
  - Mitigation: Incremental scheduling, heuristics for large problems
- **Schedule instability**: Frequent rescheduling confuses shop floor
  - Mitigation: Frozen zones (don't reschedule started operations)
- **Data accuracy**: Garbage in, garbage out
  - Mitigation: Data validation, capacity calibration
- **User adoption**: Users may override schedules
  - Mitigation: Training, show KPI improvements

---

## References

### Academic Sources
- **Job Shop Scheduling**: Pinedo, M. (2016). Scheduling: Theory, Algorithms, and Systems
- **Genetic Algorithms**: Goldberg, D. E. (1989). Genetic Algorithms in Search, Optimization, and Machine Learning
- **Constraint Programming**: Rossi, F., van Beek, P., & Walsh, T. (2006). Handbook of Constraint Programming

### Industry Standards
- **APICS CPIM**: Certified in Production and Inventory Management
- **ISA-95**: Enterprise-Control System Integration

### Related ADRs
- ADR-009: Financial Accounting Domain
- ADR-040: Manufacturing Execution System (MES)
- ADR-041: Inventory Management
- ADR-058: SOC 2 Compliance Framework

---

*Document Owner*: Head of Manufacturing  
*Review Frequency*: Quarterly  
*Next Review*: May 2027  
*Status*: **APPROVED - IMPLEMENTATION STARTING Q3 2027**
