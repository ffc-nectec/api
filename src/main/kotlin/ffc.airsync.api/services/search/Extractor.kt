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
        val age = Regex("""^.*อายุ ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.EQAUL)
        }
        return null
    }
}

class AgeMoreExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        val age = Regex(""".*อายุมากกว่า ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.MORE_THAN)
        }
        return null
    }
}

class AgeLessExtractor : Extractor<Int> {
    override fun extractFrom(query: String): Query<Int>? {
        val age = Regex(""".*อายุน้อยกว่า ?(\d+).*""").matchEntire(query)?.groupValues?.lastOrNull()?.toIntOrNull()

        if (age != null) {
            return Query("age", age, Operator.LESS_THEN)
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
        if (query.containSome("ติดเตียง")) {
            return Query("activitiesvhi", true)
        }
        return null
    }
}

class ActivitiesMidExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ติดบ้าน")) {
            return Query("activitiesmid", true)
        }
        return null
    }
}

/*
 "CATARACT": {
                "haveIssue": false,
                "type": "HealthChecked",
                "issue": "CATARACT",
                "date": "2018-01-19"
            }
 */
class CataractExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ต้อกระจก")) {
            return Query("cataract", true)
        }
        return null
    }
}

/*
"FARSIGHTED": {
                "haveIssue": false,
                "type": "HealthChecked",
                "issue": "FARSIGHTED",
                "date": "2018-01-19"
            }
 */
class FarsightedExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("สายตายาว")) {
            return Query("farsighted", true)
        }
        return null
    }
}

/*

            "GLAUCOMA": {
                "haveIssue": false,
                "type": "HealthChecked",
                "issue": "GLAUCOMA",
                "date": "2018-01-19"
            }
 */
class GlaucomaExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ต้อหิน")) {
            return Query("glaucoma", true)
        }
        return null
    }
}

/*
"AMD": {
                "haveIssue": false,
                "type": "HealthChecked",
                "issue": "AMD",
                "date": "2018-01-19"
            },
 */
class AmdExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("จอประสาทตาเสื่อม")) {
            return Query("amd", true)
        }
        return null
    }
}

/*
"NEARSIGHTED": {
                "haveIssue": false,
                "type": "HealthChecked",
                "issue": "NEARSIGHTED",
                "date": "2018-01-19"
            },
 */
class NearsightedExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("สายตาสั้น")) {
            return Query("nearsighted", true)
        }
        return null
    }
}

/*
"CVD": {
                "severity": "OK",
                "type": "HealthProblem",
                "issue": "CVD",
                "date": "2018-01-19"
            },
 */
class CvdExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("โรคหัวใจ")) {
            return Query("cvd", true)
        }
        return null
    }
}

/*

            "OA_KNEE": {
                "haveIssue": false,
                "type": "HealthChecked",
                "issue": "OA_KNEE",
                "date": "2018-01-19"
            },
 */
class OaKneeExtractor : Extractor<Boolean> {

    override fun extractFrom(query: String): Query<Boolean>? {
        if (query.containSome("ข้อเข่าเสื่อม")) {
            return Query("oaknee", true)
        }
        return null
    }
}
