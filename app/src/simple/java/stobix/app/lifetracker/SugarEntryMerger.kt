package stobix.app.lifetracker


// Returns all elements from candidates such that they do not exist in the list and that no resulting element in candidates has a timestamp equal to any in the list.
fun List<SugarEntry>.getMergeables(candidates:List<SugarEntry>) : List<SugarEntry>{
    if(this.isEmpty())
        return candidates
    else if(candidates.isEmpty())
        return candidates

    val `as` = this.sortedBy { it.timestamp }
    // Todo See if there is anything left that will break if I remove distinctBy
    val bs = candidates.distinctBy { it.timestamp }.sortedBy { it.timestamp }

    var i=0
    var j=0
    val acc: MutableList<SugarEntry> = mutableListOf()

    var a = `as`[0]
    var b = bs[j]
    var tempIndex: Int? = null

    fun consistencyCheck(entries:List<SugarEntry>,base:SugarEntry){
        // Add micro seconds to all remaining elements until all timestamps are distinct from each other
        @Suppress("NAME_SHADOWING")
        var base = base
        check@ for (next in entries) {
            when {
                base.timestamp < next.timestamp -> break@check
                base.timestamp == next.timestamp -> {
                    next.timestamp++
                    base = next
                }
                base.timestamp > next.timestamp -> continue@check
            }
        }
    }

    loop@while(true) {
        when {
            a.timestamp < b.timestamp -> {
                i++
                if (i >= `as`.size) {
                    if (tempIndex != null) {
                        /* b is later than anything in a, and b has an updated timestamp.
                            Add b, but we need to check that there were no b's that had a
                            timestamp between b's original timestamp and its current timestamp
                            such that there were an a identical to it. Thus, we need to recur.
                            However, we also need to check all b's after this to see that
                            they do not end up having the same timestamp as the current b.
                            Thus, instead of doing a loop continue, we do a recursive call
                            followed by a consistency check.
                         */
                        acc+=b
                        j++
                        val remaining=this.subList(tempIndex,`as`.size).getMergeables(bs.subList(j,bs.size)).toMutableList()
                        if(remaining.size>0)
                            consistencyCheck(base=b,entries=remaining)
                        acc.addAll(remaining)
                        break@loop
                    }
                    else {
                        /* b is later than anything in a, and b has its original timestamp.
                        Thus, all later elements in b will also have a timestamp later than any a.
                        Add all b's with a later timestamp than the a's, while upgrading any
                         elements of b with the same timestamp */
                        val remaining = bs.subList(j, bs.size)
                        if (remaining.size > 1) {
                            // we need to check timestamp consistency
                            consistencyCheck(base=remaining[0],entries=remaining.drop(1))
                        }
                        acc.addAll(remaining)
                        break@loop
                    }
                } else {
                    a = `as`[i]
                    continue@loop
                }
            }
            a.timestamp == b.timestamp ->
                if(a sameAs b) {
                    // Discard elements that are the same in all fields
                    j++
                    if (j < bs.size) {
                        b = bs[j]
                        if (tempIndex != null) {
                            i = tempIndex
                            a = `as`[tempIndex]
                            tempIndex = null
                        }
                        continue@loop
                    } else {
                        // we're done if there are no more bees
                        break@loop
                    }
                } else {
                    /*
                    * a and b have the same timestamp, but are different;
                    * increase the timestamp of b and try again.
                    * Save the first a whose timestamp was the same as the original version of b -
                    * this is the a we start comparing the next b against.
                    */
                    tempIndex = tempIndex ?: i
                    b.timestamp++
                    continue@loop
                }
            a.timestamp > b.timestamp -> {
                // Add elements with an earlier timestamp than the list
                acc+=b
                j++
                if (j < bs.size) {
                    b = bs[j]
                    // New b; restore saved a's position, if any
                    if (tempIndex != null) {
                        i = tempIndex
                        a = `as`[tempIndex]
                        tempIndex = null
                    }
                    continue@loop
                } else {
                    break@loop
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
