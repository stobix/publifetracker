//package stobix.view.containerview
//
//import android.arch.persistence.room.Entity
//import android.arch.persistence.room.ForeignKey
//import android.arch.persistence.room.PrimaryKey
//import android.arch.persistence.room.Relation
//import java.lang.Character.isDigit
//
//// For now, I define two sets of classes: One for active use and one to model the database.
//
//@Entity
//data class ESubmission(
//        @PrimaryKey var timestamp: Long,
//        @ForeignKey(
//                entity = ECollection::class,
//                parentColumns = arrayOf("id"),
//                childColumns = arrayOf("collection_id")
//        )
//        var collection_id : Int
//)
//
//@Entity
//data class ECollection(
//        @PrimaryKey(autoGenerate = true)
//        var id: Int?,
//        @Relation(parentColumn="id",entityColumn="collection_id")
//        var entities: List<EEntry>
//)
//
//@Entity
//data class EEntry(
//        @PrimaryKey(autoGenerate = true) var id: Int?,
//        @ForeignKey(
//                entity=ECollection::class,
//                parentColumns = arrayOf("id"),
//                childColumns = arrayOf("collection_id")
//        )
//        var collection_id: Int,
//        var postion: Int,
//        var measurement_content: Int? ,
//        var collection_content: Int?
//)
//
//@Entity
//data class EMeasurement(
//        @PrimaryKey(autoGenerate = true) var id: Int?,
//        var value: Int,
//        @ForeignKey(entity = Unit::class,
//                parentColumns = arrayOf("id"),
//                childColumns = arrayOf("unit_id")
//        )
//        var unit_id: Int
//)
//
//@Entity data class EEntryTag(
//        @ForeignKey(entity = EEntry::class,parentColumns = arrayOf("id"),childColumns = arrayOf("entry"))
//        var entry_id:Int,
//        @ForeignKey(entity = ETag::class,parentColumns = arrayOf("id"),childColumns = arrayOf("tag"))
//        var tag_id:Int
//)
//
//@Entity
//data class ETag(
//        @PrimaryKey(autoGenerate = true) var id: Int?,
//        var tag: String,
//        var description: String
//)
//
//@Entity
//data class EUnit(
//        @PrimaryKey(autoGenerate = true) var id: Int?,
//        var name: String,
//        var description: String
//)
//
//@Entity
//data class EUnitConversion(
//        @ForeignKey(entity = EUnit::class,parentColumns = arrayOf("id"),childColumns = arrayOf("to"))
//        var to:Int,
//        @ForeignKey(entity = EUnit::class,parentColumns = arrayOf("id"),childColumns = arrayOf("from"))
//        var from:Int
//){
//    companion object {
//        @JvmField val primaryKeys = arrayOf("to","from")
//    }
//}
//
//
////////////////////////////////////////////////////
//
//interface EntryContent
//
//data class Submission(
//        @PrimaryKey var timestamp: Long,
//        @ForeignKey(
//                entity = ECollection::class,
//                parentColumns = arrayOf("id"),
//                childColumns = arrayOf("collection")
//        )
//        var collection : Collection
//)
//
//
//data class Collection(
//        var id: Int?,
//        var tags: List<Tag>,
//        var entries: List<EntryContent>
//): EntryContent
//
//data class Measurement(
//        var value: Int,
//        var unit: MeasurementUnit,
//        var id: Int?=null,
//        var tags: List<Tag> = mutableListOf()
//): EntryContent
//
//fun <K,V> MutableMap<K,V>.modify(k:K,default:V,f:(V) -> V){
//    var foo = this[k]?:default
//    this[k]=f(foo)
//}
//
//fun <K,V> MutableMap<K,V>.apply(k:K,default:V,f:(V) -> Unit){
//    f(this.getOrPut(k,{default}))
//}
//
//data class TagStore(
//        val tags: MutableMap<String,Tag> = mutableMapOf(),
//        val connections: MutableMap<EntryContent,MutableSet<String>> = mutableMapOf()
//) {
//    fun listTags(entryContent: EntryContent) = connections.getOrDefault(entryContent, mutableSetOf()).toList()
//    fun appendEntryTag(entryContent: EntryContent,tag: Tag){
//
//        connections.getOrPut(entryContent,{mutableSetOf()}).add(tag.tag)
//
//        /*
//        var entryTags=connections.getOrDefault( entryContent, mutableSetOf() )
//        entryTags.add(tag.tag)
//        connections.put(entryContent,entryTags)
//        */
//
//        //val entryTags= connections[entryContent]?:mutableSetOf()
//
//        appendTag(tag)
//    }
//
//    fun appendTag(tag: Tag){
//        tags.put(tag.tag,tag)
//
//    }
//
//    fun appendTag(tag: String, description: String){
//        appendTag(Tag(tag=tag,description = description))
//
//    }
//}
//
//data class ConversionStore(
//    val conversions: MutableMap<Pair<MeasurementUnit,MeasurementUnit>,String> = mutableMapOf()
//) {
//    fun addConversion(u1: MeasurementUnit, u2: MeasurementUnit, formula: String){
//
//    }
//}
//
//
//interface Expression
//{
//    operator fun invoke(convertFrom:Number):Double
//}
//
//data class Add(val v1: Expression,val v2: Expression) : Expression
//{
//    override fun invoke(convertFrom:Number): Double = v2(convertFrom)+v1(convertFrom)
//    constructor(v1:Number,v2: Expression):this(Value(v1),v2)
//    constructor(v1:Expression,v2: Number):this(v1,Value(v2))
//    constructor(v1:Number,v2: Number):this(Value(v1),Value(v2))
//    override fun toString(): String = "($v1+$v2)"
//}
//data class Subtract(val v1: Expression,val v2: Expression) : Expression
//{
//    override fun invoke(convertFrom:Number): Double = v2(convertFrom)-v1(convertFrom)
//    constructor(v1:Number,v2: Expression):this(Value(v1),v2)
//    constructor(v1:Expression,v2: Number):this(v1,Value(v2))
//    constructor(v1:Number,v2: Number):this(Value(v1),Value(v2))
//    override fun toString(): String = "($v1-$v2)"
//}
//data class Multiply(val v1: Expression,val v2: Expression) : Expression
//{
//    override fun invoke(convertFrom:Number): Double = v2(convertFrom)*v1(convertFrom)
//    constructor(v1:Number,v2: Expression):this(Value(v1),v2)
//    constructor(v1:Expression,v2: Number):this(v1,Value(v2))
//    constructor(v1:Number,v2: Number):this(Value(v1),Value(v2))
//    override fun toString(): String = "($v1*$v2)"
//}
//data class Divide(val v1: Expression,val v2: Expression) : Expression
//{
//    constructor(v1:Number,v2: Expression):this(Value(v1),v2)
//    constructor(v1:Expression,v2: Number):this(v1,Value(v2))
//    constructor(v1:Number,v2: Number):this(Value(v1),Value(v2))
//    override fun invoke(convertFrom:Number): Double = v1(convertFrom)/v2(convertFrom)
//    override fun toString(): String = "($v1/$v2)"
//}
//data class Value(val v1: Number) : Expression {
//    override fun invoke(convertFrom: Number): Double = v1.toDouble()
//    override fun toString(): String = "$v1"
//}
//
//class From : Expression {
//    override fun invoke(convertFrom: Number): Double = convertFrom.toDouble()
//    override fun toString(): String = "⟪from⟫"
//}
//
//data class Tag(
//        var tag: String,
//        var description: String
//)
//
//data class MeasurementUnit(
//        var name: String,
//        var description: String,
//        var conversions: List<UnitConversion> = mutableListOf(),
//        var id: Int? = null
//)
//
//data class UnitConversion(
//        var to: MeasurementUnit,
//        var formula:String,
//        var description:String
//)
//
//val operators = listOf('+','-','*','/')
//
//typealias Parsing = Pair<String,Expression>
//
//// expr = parenExpr | opExpr | number | "from"
//// parenExpr = ( opExpr )
//// opExpr = expr op expr
//// number = 0..9+\.0..9+
//// op = * | + | / | -
//
//
///*
//fun parse(cs:String): Expression {
//    val c=cs[0]
//    if(c=='(')
//        parse(cs)
//    else if (cs.startsWith("from"))
//        From()
//    else if (c in operators)
//        error("premature operator: %c")
//    else if (c.isDigit()) {
//        val (cs,parsed)=parseDigit(cs)
//    }
//
//}
//
//fun parseNext(p:Parsing): Parsing {
//    val (cs,prevExpr)=p
//    val c = cs[0]
//    if (c.isDigit())
//        parseDigit(cs)
//    else if (c in operators)
//        Pair(cs.drop(1),)
//    }
//    for((i,c) in cs.withIndex()){
//        if
//        when(c){
//            if.isDigit() ->
//            in '0'..'9','.' -> {
//                parseNum
//                if (!wasNum) {
//                    wasNum = true
//                    firstIx = i
//                }
//            }
//            '+','-','*','/' -> {
//                if(wasNum){
//                    var first=Value(cs.substring(firstIx,i-1))
//                    var (second,rest) = parse(ix)
//                }
//            }
//            else -> {
//
//
//            }
//        }
//    }
//
//}
//
//*/
///*
//  Alt prop:
//
//
//
//    The table is built up of rows.
//    Each row contains a timestamp, and a number of entries.
//    Each entry contains some info about something.
//
//    a category is a description with zero or more entries
//    an entry belongs to a category, has position info and contains one category or one unit
//    a unit
//
//
//
//
//   5.3 mmol/l,4 NovoRapid,Breakfast: 1 Bread w. butter & ham, 1 cup of tea with milk, 88 kg new scale, 88 1/2 kg old scale
//
//   ->
//       0 () :: entry
//                5 {mmol/l} :: unit
//       1 () :: entry
//           Treatment :: tag
//               4 {NovoRapid} :: unit
//       2 () :: entry
//           Breakfast :: cat
//               0 1 :: entry
//                    Sandwich :: cat
//                        0 1 :: entry
//                            bread :: cat
//                                ()
//                        1 () :: entry
//                            butter :: cat
//                                ()
//                        2 2 :: entry
//                            ham :: cat
//                                ()
//               1 1 :: entry
//                    TeaWithMilk :: cat
//                        0 () :: entry
//                            Tea :: cat
//                                0 () :: entry
//                                    0.4 {l} :: unit
//                        1 () :: entry
//                            0.2l milk
//       3 () :: entry
//           Weight :: cat
//               0 () :: entry
//                    New scale :: cat
//                        0 () :: entry
//                            88 {kg}
//       4 () :: entry
//           Weight :: cat
//               Old scale :: cat
//                   88.5 {kg} :: unit
//
//   ->
//
//   Entry
//       id: E0
//       timestamp = T
//       container = C0
//   Container:
//       id = C0
//       cat = Measurement
//   Amount:
//       id = A0
//       parent = C0
//       unit = U0
//       value = 5.3 :: Float
//   Container:
//       id = C1
//       cat = Treatment
//   Amount:
//       id = A1
//       parent = C1
//       unit = U1
//
//
//
//   Unit:
//       id: U0
//       tag: "mmol/l"
//   Conversion:
//       id CV1
//       from: U0
//       to: <American format>
//       formula:: RPN as TEXT/BLOB
//   Amount:
//       id: A1
//
//
//
//
//*/
