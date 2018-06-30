package stobix.utils.kotlin.strings

fun intersperse(a:String?, s: String, b:String?) =
        when {
            a.isNullOrEmpty() -> b
            b.isNullOrEmpty() -> a
            else -> "$a$s$b"
        }

infix fun String?.dot(s: String?) = intersperse(this,". ",s)
infix fun String?.comma(s: String?) = intersperse(this,", ",s)
infix fun String?.semicolon(s: String?) = intersperse(this,"; ",s)
infix fun String?.colon(s: String?) = intersperse(this,": ",s)
infix fun String?.exclamation(s: String?) = intersperse(this,"! ",s)
infix fun String?.questionmark(s: String?) = intersperse(this,"? ",s)
infix fun String?.interrobang(s: String?) = intersperse(this,"‽ ",s)
infix fun String?.bar(s: String?) = intersperse(this," | ",s)
infix fun String?.space(s: String?) = intersperse(this," ",s)
