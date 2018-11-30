package ffc.airsync.api.services.search

class QueryExtractor {

    val extractor = listOf<Extractor<*>>(
        ElderExtractor(),
        DmExtractor(),
        HtExtractor()
    )

    val queryMap = mutableMapOf<String, Query<Any>>()
    val querys = listOf<Query<Any>>()

    fun extract(query: String): Map<String, Query<Any>> {
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
) {

    enum class Operator {
        EQAUL, MORE_THAN, LESS_THEN
    }
}
