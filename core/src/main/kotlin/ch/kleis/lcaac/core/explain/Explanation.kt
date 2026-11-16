package ch.kleis.lcaac.core.explain

import ch.kleis.lcaac.core.lang.evaluator.derivation.ProcessDerivation
import ch.kleis.lcaac.core.lang.evaluator.derivation.VariableDerivation
import ch.kleis.lcaac.core.lang.value.*

/*
    How the impacts of a process are obtained, as plain data for a renderer:
    the results, then each process of the supply chain with how it was evaluated
    and how its products get their impacts per unit, checked against the solver.
    Amounts are in the reference unit of their product, substance or indicator.
 */

data class ExplainOptions(
    /** The short names of the indicators, substances or products to explain; all of them if empty. */
    val targets: Set<String> = emptySet(),
    /** Only explain the processes up to this depth in the supply chain, the entry point being at 0. */
    val maxDepth: Int? = null,
)

data class Explanation<Q>(
    val entryPoint: ProcessExplanation<Q>,
    /** What the impacts are measured in: indicators, and substances or products that nothing supplies. */
    val targets: List<MatrixColumnIndex<Q>>,
    /** The impacts of each reference product of the entry point. */
    val results: List<ProductResult<Q>>,
    /** The processes of the supply chain, numbered from the entry point, breadth first. */
    val processes: List<ProcessExplanation<Q>>,
    val substanceCharacterizations: List<SubstanceCharacterizationValue<Q>>,
    /** The global parameters and variables that the evaluation used. */
    val globals: List<VariableDerivation<Q>>,
)

data class ProductResult<Q>(
    val product: TechnoExchangeValue<Q>,
    val impacts: List<TargetImpact<Q>>,
)

data class TargetImpact<Q>(
    val target: MatrixColumnIndex<Q>,
    /** The impact of the amount of the product. */
    val amount: Double,
    /** The impact per reference unit of the product. */
    val perUnit: Double,
)

data class ProcessExplanation<Q>(
    val number: Int,
    val depth: Int,
    val process: ProcessValue<Q>,
    /** How the process was evaluated; null for a cached process, evaluated as a separate system. */
    val derivation: ProcessDerivation<Q>?,
    /** The numbers of the processes that use one of its products. */
    val usedBy: List<Int>,
    /** The numbers of the processes of the loop it belongs to, itself included; empty if it is not in a loop. */
    val loop: List<Int>,
    /** The supplier of each input, in the order of the inputs of [process]. */
    val suppliers: List<Supplier>,
    val contributions: List<ProductContributions<Q>>,
)

sealed interface Supplier {
    /** A process of the explanation. */
    data class Explained(val number: Int, val name: String) : Supplier
    /** A process deeper than the maximum depth. */
    data class NotExplained(val name: String) : Supplier
    /** No process supplies the product: it is an end point of the system. */
    data object EndPoint : Supplier
}

data class ProductContributions<Q>(
    val product: TechnoExchangeValue<Q>,
    /** The share of the process allocated to this product. */
    val share: Double,
    val tables: List<ContributionTable<Q>>,
    /** The targets that no exchange contributes to, as the solver also finds. */
    val unaffected: List<MatrixColumnIndex<Q>>,
)

/**
 * The impact per unit of a product on a [target]: the sum of the contributions of the exchanges
 * of its (allocated) process, divided by the amount of the product.
 */
data class ContributionTable<Q>(
    val target: MatrixColumnIndex<Q>,
    /** The exchanges that contribute; the others are only counted in [hidden]. */
    val rows: List<ContributionRow<Q>>,
    val hidden: Int,
    val total: Double,
    val productAmount: Double,
    val perUnit: Double,
    /** The impact per unit given by the solver, which [perUnit] should match. */
    val solverPerUnit: Double,
) {
    val matchesSolver: Boolean
        get() = kotlin.math.abs(perUnit - solverPerUnit) <=
            1e-6 * maxOf(kotlin.math.abs(perUnit), kotlin.math.abs(solverPerUnit)) + 1e-12
}

data class ContributionRow<Q>(
    val kind: ExchangeKind,
    /** The index of the exchange among the inputs, biosphere or impacts of the process. */
    val index: Int,
    /** The exchange of the allocated process: its amount is multiplied by the share of the product. */
    val exchange: ExchangeValue<Q>,
    val amount: Double,
    /** The impact per reference unit of the exchange, from the solver. */
    val factor: Double,
    val contribution: Double,
)

enum class ExchangeKind { INPUT, BIOSPHERE, IMPACT }
