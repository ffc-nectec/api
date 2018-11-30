package ffc.airsync.api.services.search

interface Extractor<T> {

    fun extractFrom(query: String): Query<T>?
}

class ElderExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        if (query.containSome("สูงอายุ", "คนแก่", "60 ปี")) {
            return Query("age", 60, Operator.MORE_THAN)
        }
        return null
    }
}

class DmExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("เบาหวาน", "dm", "น้ำตาลสูง")) {
            return Query("dm", true)
        }
        return null
    }
}

class HtExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ความดัน", "ความดันสูง", "ความดันสูงโลหิตสูง", "ht")) {
            return Query("ht", true)
        }
        return null
    }
}
