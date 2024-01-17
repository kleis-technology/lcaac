package ch.kleis.lcaac.core.testing

data class TestResult<S>(
    val source: S,
    val name: String,
    val results: List<AssertionResult>,
)
