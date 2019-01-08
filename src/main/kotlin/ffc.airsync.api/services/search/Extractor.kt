package ffc.airsync.api.services.search

interface Extractor<T> {

    fun extractFrom(query: String): Query<T>?
}

class ElderExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        if (query.containSome("สูงอายุ", "คนแก่", "60 ปี", "ผู้ชายแก่", "ผู้หญิงแก่")) {
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
        val age = Regex("""^.*อายุ(เท่ากับ)? ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.EQAUL)
        }
        return null
    }
}

class AgeMoreExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        var age = Regex(""".*อายุมากกว่า ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age == null)
            age = Regex(""".*อายุ ?(\d+) ?ปีขึ้นไป.*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age == null)
            age = Regex(""".*อายุ ?(\d+) ?ขึ้นไป.*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age == null)
            age = Regex(""".*อายุสูงกว่า ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age == null)
            age = Regex(""".* (\d+) ?ปีขึ้นไป.*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.MORE_THAN)
        }
        return null
    }
}

class AgeLessExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        var age = Regex(""".*อายุน้อยกว่า ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age == null)
            age = Regex(""".*อายุต่ำกว่า ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age == null)
            age = Regex(""".*อายุ ?(\d+) ?ปีลงไป.*""").matchEntire(query)?.groupValues?.get(1)?.toIntOrNull()

        if (age == null)
            age = Regex(""".*อายุ ?(\d+) ?ลงไป.*""").matchEntire(query)?.groupValues?.get(1)?.toIntOrNull()

        if (age == null)
            age = Regex(""".* (\d+) ?ปีลงไป.*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.LESS_THEN)
        }
        return null
    }
}

class AgeBetweenExtractor : Extractor<List<Int>> {
    override fun extractFrom(query: String): Query<List<Int>>? {
        var ageBetween = Regex(""".*อายุระหว่าง ?(\d+) ?(ปี)? ?ถึง ?(\d+).*""").matchEntire(query)?.groupValues
        if (ageBetween == null) {
            ageBetween = Regex(""".*อายุระหว่าง ?(\d+) ?- ?(\d+).*""").matchEntire(query)?.groupValues
        }
        if (ageBetween == null) {
            ageBetween = Regex(""".*อายุ ?(\d+) ?(ปี)? ?ถึง ?(\d+).*""").matchEntire(query)?.groupValues
        }
        if (ageBetween == null) {
            ageBetween = Regex(""".*อายุ ?(\d+) ?- ?(\d+).*""").matchEntire(query)?.groupValues
        }
        val ageStart = ageBetween?.get(1)?.toIntOrNull()
        val ageEnd = ageBetween?.lastOrNull()?.toIntOrNull()

        if (ageStart != null && ageEnd != null) {
            return Query("agebetween", listOf(ageStart, ageEnd), Operator.EQAUL)
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

class ActivitiesVeryHiExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ติดเตียง", "ภาวะพึ่งพิง")) {
            return Query("activitiesvhi", true)
        }
        return null
    }
}

class ActivitiesMidExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ติดบ้าน", "ภาวะพึ่งพิง")) {
            return Query("activitiesmid", true)
        }
        return null
    }
}

class CataractExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ต้อกระจก")) {
            return Query("cataract", true)
        }
        return null
    }
}

class FarsightedExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("สายตายาว")) {
            return Query("farsighted", true)
        }
        return null
    }
}

class GlaucomaExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ต้อหิน")) {
            return Query("glaucoma", true)
        }
        return null
    }
}

class AmdExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("จอประสาทตาเสื่อม")) {
            return Query("amd", true)
        }
        return null
    }
}

class DepressiveExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ซึมเศร้า")) {
            return Query("depressive", true)
        }
        return null
    }
}

class NearsightedExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("สายตาสั้น")) {
            return Query("nearsighted", true)
        }
        return null
    }
}

class CvdExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("โรคหัวใจ")) {
            return Query("cvd", true)
        }
        return null
    }
}

class OaKneeExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ข้อเข่าเสื่อม")) {
            return Query("oaknee", true)
        }
        return null
    }
}
