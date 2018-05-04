package stobix.app.lifetracker

fun List<SugarEntry>.getMergeables(candidates:List<SugarEntry>) : List<SugarEntry>{
    val l1 = this.sortedBy { it.epochTimestamp }
    val l2 = candidates.distinctBy { it.epochTimestamp }.sortedBy { it.epochTimestamp }

    var i1=0
    var i2=0
    val acc: MutableList<SugarEntry> = mutableListOf()

    var e1 = l1[0]
    var t1 = e1.epochTimestamp
    var e2 = l2[i2]
    var t2 = e2.epochTimestamp

    loop@while(true) {
        when {
            t1 < t2 -> {
                i1++
                if (i1 >= l1.size) {
                    // Add all elements with a later timestamp than the list
                    val remaining = l2.subList(i2,l2.size)
                    fif@if(remaining.size>1) {
                        // we need to check timestamp consistency
                        var e1 = remaining[0]
                        // Add micro seconds to all remaining l2 elements until all timestamps are distinct from eachother
                        consistencyCheck@ for (e2 in remaining.subList(1, remaining.size)) {
                            when {
                                e1.epochTimestamp < e2.epochTimestamp -> break@consistencyCheck
                                e1.epochTimestamp == e2.epochTimestamp -> {
                                    e2.epochTimestamp++
                                    e1 = e2
                                }
                                e1.epochTimestamp > e2.epochTimestamp -> continue@consistencyCheck
                            }
                        }
                    }
                    acc.addAll(remaining)
                    break@loop
                } else {
                    e1 = l1[i1]
                    t1 = e1.epochTimestamp
                    continue@loop
                }
            }
            t1 == t2->
                if(e1 sameAs e2) {
                    // Discard elements that are the same
                    i2++
                    if( i2 >= l2.size) {
                        break@loop
                    } else {
                        e2 = l2[i2]
                        t2 = e2.epochTimestamp
                        continue@loop
                    }
                    // Make distinct elements that share the same timestamp
                    // This assumes that timestamps that occur at the same microsecond can
                    // be shifted forward a microsecond without breaking anything
                } else {
                    e2.epochTimestamp++
                    t2=e2.epochTimestamp
                    continue@loop
                }
            t1 > t2-> {
                // Add elements with an earlier timestamp than the list
                acc.plusAssign(e2)
                i2++
                if( i2 >= l2.size) {
                    break@loop
                } else {
                    e2 = l2[i2]
                    t2 = e2.epochTimestamp
                    continue@loop
                }
            }
        }
    }

    return acc
}
class SugarEntryMerger {
    companion object {
        @JvmStatic fun getMergeables(l1: List<SugarEntry>,l2: List<SugarEntry>) = l1.getMergeables(l2)
    }
}
