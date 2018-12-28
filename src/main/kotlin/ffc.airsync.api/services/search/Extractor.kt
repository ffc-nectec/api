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

class AgeExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        val age = Regex("""อายุ ?(\d+)""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.EQAUL)
        }
        return null
    }
}

class AgeMoreExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        val age = Regex("""อายุมากกว่า ?(\d+)""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.MORE_THAN)
        }
        return null
    }
}

class AgeLessExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        val age = Regex("""อายุน้อยกว่า ?(\d+)""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.MORE_THAN)
        }
        return null
    }
}

class NcdsExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ncd")) {
            return Query("ncd", true)
        }
        return null
    }
}

class MaleExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ผู้ชาย")) {
            return Query("male", true)
        }
        return null
    }
}

class FemaleExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ผู้หญิง")) {
            return Query("female", true)
        }
        return null
    }
}
