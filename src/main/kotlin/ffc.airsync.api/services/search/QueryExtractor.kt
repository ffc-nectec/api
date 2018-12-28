package ffc.airsync.api.services.search

class QueryExtractor {

    val extractor = listOf<Extractor<*>>(
        ElderExtractor(),
        DmExtractor(),
        HtExtractor(),
        AgeExtractor(),
        AgeMoreExtractor(),
        AgeLessExtractor(),
        NcdsExtractor(),
        MaleExtractor(),
        FemaleExtractor(),
        ActiveLowExtractor(),
        ActiveMidExtractor()
    )

    val queryMap = mutableMapOf<String, Query<Any>>()

    fun extract(query: String?): Map<String, Query<Any>> {
        if (query == null) return queryMap
        extractor.forEach { ext ->
            val q = ext.extractFrom(query)?.let {
                queryMap.put(it.key, it as Query<Any>)
            }
        }
        return queryMap
    }
}

data class Query<T>(
    val key: String,
    val value: T,
    val operator: Operator = Operator.EQAUL,
    val inclusive: Boolean = true
)

fun <T : Comparable<T>> Query<T>.compareTo(other: () -> T): Boolean {
    return when (operator) {
        Operator.EQAUL -> other() == value
        Operator.MORE_THAN -> if (inclusive) other() >= value else other() > value
        Operator.LESS_THEN -> if (inclusive) other() <= value else other() < value
    }
}

enum class Operator {
    EQAUL, MORE_THAN, LESS_THEN
}
