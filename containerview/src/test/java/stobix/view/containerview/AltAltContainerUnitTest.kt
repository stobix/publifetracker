package stobix.view.containerview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import stobix.utils.DateHandler

/**
 * Created by stobix on 2018-03-28.
 */
class AltAltContainerUnitTest {
    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
    private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)
    @Test
    fun buildSubmission() {
        System.out.println(
                CSubmission(
                        timestamp = DateHandler().timestamp,
                        contentId = 0,
                        collection = CCollection(
                                myCollId = 0,
                                tags = listOf(),
                                contents = listOf(
                                        CCollection(
                                                myCollId = 1,
                                                tags= listOf(),
                                                contents = listOf()
                                        ),
                                        CMeasurement(
                                                measurementId = 0,
                                                tags = listOf(
                                                        CTag(
                                                                tagId = 0,
                                                                tagName = "vikt",
                                                                description = "hur mycket jag väger"
                                                        )
                                                ),
                                                mesUnit = CMesUnit(
                                                        unitId = 0,
                                                        shortForm = "kg",
                                                        description = "lol",
                                                        conversions = listOf()
                                                )
                                        ),
                                        CMeasurement(
                                                measurementId = 1,
                                                tags = listOf(
                                                        CTag(
                                                                tagId = 0,
                                                                tagName = "blodsocker",
                                                                description = "blodsockernivå"
                                                        )
                                                ),
                                                mesUnit = CMesUnit(
                                                        unitId = 0,
                                                        shortForm = "mmol/l",
                                                        description = "standardformat för blodsocker i delar av Europa",
                                                        conversions = listOf(
                                                                /*
                                                                CConversion(
                                                                        fromId = 0,
                                                                        toId = ??,
                                                                        formula = " x / 18 "
                                                                )
                                                                */
                                                        )
                                                )
                                        )
                                )
                        )
                )
       )
       /*
       listOf(
               CMesUnit(
                       unitId = 0,
                       shortForm = "kg",
                       description = "lol",
                       conversions = listOf()
               ),
               CMesUnit(
                       unitId = 1,
                       shortForm = "mmol/l",
                       description = "standardformat för blodsocker i delar av Europa",
                       conversions = listOf(
                               CConversion(
                                       fromId = 1,
                                       toId = 2,
                                       formula = " x / 18 "
                               )
                       )
               ),
               CMesUnit(
                       unitId = 2,
                       shortForm = "mg/dl",
                       description = "standardformat för blodsocker USA kanske",
                       conversions = listOf(
                               CConversion(
                                       fromId = 2,
                                       toId = 1,
                                       formula = " 18 * x "
                               )
                       )
               )
               */
   }
}