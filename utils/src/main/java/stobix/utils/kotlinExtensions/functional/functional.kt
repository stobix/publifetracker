package stobix.utils.kotlinExtensions.functional

fun <A> id(a: A) = a
fun <A,B> const(b: B) = { _:A ->b}
fun <A,B> A.const(b: B?):(A)->B? = {_ -> b}

