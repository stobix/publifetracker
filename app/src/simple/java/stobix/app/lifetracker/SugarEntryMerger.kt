package stobix.app.lifetracker

fun List<SugarEntry>.getMergeables(candidates:List<SugarEntry>) : List<SugarEntry>{
    if(this.isEmpty())
        return candidates
    else if(candidates.isEmpty())
        return candidates

    val selfList = this.sortedBy { it.epochTimestamp }
    val mergeList = candidates.distinctBy { it.epochTimestamp }.sortedBy { it.epochTimestamp }

    var iSelfList=0
    var iMergeList=0
    val acc: MutableList<SugarEntry> = mutableListOf()

    var selfEntry = selfList[0]
    var selfTimestamp = selfEntry.epochTimestamp
    var mergeEntry = mergeList[iMergeList]
    var mergeTimestamp = mergeEntry.epochTimestamp

    loop@while(true) {
        when {
            selfTimestamp < mergeTimestamp -> {
                iSelfList++
                if (iSelfList >= selfList.size) {
                    // Add all elements with a later timestamp than the list
                    val remaining = mergeList.subList(iMergeList,mergeList.size)
                    fif@if(remaining.size>1) {
                        // we need to check timestamp consistency
                        var base = remaining[0]
                        // Add micro seconds to all remaining mergeList elements until all timestamps are distinct from eachother
                        consistencyCheck@ for (next in remaining.subList(1, remaining.size)) {
                            when {
                                base.epochTimestamp < next.epochTimestamp -> break@consistencyCheck
                                base.epochTimestamp == next.epochTimestamp -> {
                                    next.epochTimestamp++
                                    base = next
                                }
                                base.epochTimestamp > next.epochTimestamp -> continue@consistencyCheck
                            }
                        }
                    }
                    acc.addAll(remaining)
                    break@loop
                } else {
                    selfEntry = selfList[iSelfList]
                    selfTimestamp = selfEntry.epochTimestamp
                    continue@loop
                }
            }
            selfTimestamp == mergeTimestamp->
                if(selfEntry sameAs mergeEntry) {
                    // Discard elements that are the same
                    iMergeList++
                    if( iMergeList >= mergeList.size) {
                        break@loop
                    } else {
                        mergeEntry = mergeList[iMergeList]
                        mergeTimestamp = mergeEntry.epochTimestamp
                        continue@loop
                    }
                    // Make distinct elements that share the same timestamp
                    // This assumes that timestamps that occur at the same microsecond can
                    // be shifted forward a microsecond without breaking anything
                } else {
                    mergeEntry.epochTimestamp++
                    mergeTimestamp=mergeEntry.epochTimestamp
                    continue@loop
                }
            selfTimestamp > mergeTimestamp-> {
                // Add elements with an earlier timestamp than the list
                acc.plusAssign(mergeEntry)
                iMergeList++
                if( iMergeList >= mergeList.size) {
                    break@loop
                } else {
                    mergeEntry = mergeList[iMergeList]
                    mergeTimestamp = mergeEntry.epochTimestamp
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
