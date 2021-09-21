package converter

import kotlin.collections.ArrayList
import kotlin.io.*

enum class UnitType {
    LENGTH,
    WEIGHT,
    TEMPERATURE,
    WRONG_TYPE
}

enum class ConvertedUnits (var  names : ArrayList<String>, val unitType:UnitType, val ratio: Double) {
    METER(arrayListOf("m", "meter", "meters"), UnitType.LENGTH, 1.0),
    KILOMETER(arrayListOf("km", "kilometer", "kilometers"), UnitType.LENGTH, 1000.0),
    CENTIMETER(arrayListOf("cm", "centimeter", "centimeters"), UnitType.LENGTH, .01),
    MILLIMETER(arrayListOf("mm", "millimeter", "millimeters"), UnitType.LENGTH, .001),
    MILE(arrayListOf("mi", "mile", "miles"), UnitType.LENGTH, 1609.35),
    YARD(arrayListOf("yd", "yard", "yards"), UnitType.LENGTH, 0.9144),
    FOOT(arrayListOf("ft", "foot", "feet"), UnitType.LENGTH, 0.3048),
    INCH(arrayListOf("in", "inch", "inches"), UnitType.LENGTH, 0.0254),
    GRAM(arrayListOf("g", "gram", "grams"), UnitType.WEIGHT, 1.0),
    KILOGRAM(arrayListOf("kg", "kilogram", "kilograms"), UnitType.WEIGHT, 1000.0),
    MILLIGRAM(arrayListOf("mg", "milligram", "milligrams"), UnitType.WEIGHT, .001),
    POUND(arrayListOf("lb", "pound", "pounds"), UnitType.WEIGHT, 453.5920),
    OUNCE(arrayListOf("oz", "ounce", "ounces"), UnitType.WEIGHT, 28.3495),
    CELSIUS(
        arrayListOf(
            "celsius", "degree Celsius", "degrees Celsius",
            "degree celsius", "degrees celsius", "dc", "c"
        ), UnitType.TEMPERATURE, 1.0
    ),
    FAHRENHEIT(
        arrayListOf(
            "fahrenheit", "degree Fahrenheit", "degrees Fahrenheit",
            "degree fahrenheit", "degrees fahrenheit", "df", "f"
        ), UnitType.TEMPERATURE, 2.0
    ),
    KELVIN(arrayListOf("k", "kelvin", "kelvins"), UnitType.TEMPERATURE, 3.0);

    companion object {

        fun getType(unitName: String): UnitType {
            for (currentUnit in values()) {
                if (currentUnit.names.contains(unitName))
                    return currentUnit.unitType
            }
            return UnitType.WRONG_TYPE
        }

        private fun getRatio(unit: String): Double {
            for (currentUnit in values()) {
                if (currentUnit.names.contains(unit))
                    return currentUnit.ratio
            }
            throw Exception("Wrong Unit")
        }

        fun isSameType(unit1: String, unit2: String) = getType(unit1) == getType(unit2)

        fun getUnitName(unit: String, isSingle: Boolean): String =
            if (isSingle) getSingularName(unit) else getPluralName(unit)

        private fun getSingularName(unit: String): String {
            for (currentUnit in values()) {
                if (currentUnit.names.contains(unit))
                    return currentUnit.names[1]
            }
            return "???"
        }

        private fun getPluralName(unit: String): String {
            for (currentUnit in values()) {
                if (currentUnit.names.contains(unit))
                    return currentUnit.names[2]
            }
            return "???"
        }

        fun convertToNewUnit(value: Double, unit1: String, unit2: String): Double {
            if (getType(unit1) == UnitType.TEMPERATURE)
                return Temperature.getFormula(unit1,unit2).invoke(value)

            return value * getRatio(unit1) / getRatio(unit2)
        }

    }

    object Temperature {
        private val temperatureFormula = arrayOf(
            { c: Double -> c * (9.0 / 5.0) + 32 },      //Celsius to Fahrenheit
            { f: Double -> (f - 32) * (5.0 / 9.0) },    //Fahrenheit to Celsius
            { c: Double -> c + 273.15 },                //Celsius to kelvins
            { k: Double -> k - 273.15 },                //kelvins to Celsius
            { f: Double -> (f + 459.67) * (5.0 / 9.0) },//Fahrenheit to kelvins
            { k: Double -> k * (9.0 / 5.0) - 459.67 },  //kelvins to Fahrenheit
            { t: Double -> t }                          //the same unit
        )

        fun getFormula(unit1: String, unit2: String): (Double) -> Double =
            when {
                getRatio(unit1) == 1.0 && getRatio(unit2) == 2.0 -> temperatureFormula[0]
                getRatio(unit1) == 2.0 && getRatio(unit2) == 1.0 -> temperatureFormula[1]
                getRatio(unit1) == 1.0 && getRatio(unit2) == 3.0 -> temperatureFormula[2]
                getRatio(unit1) == 3.0 && getRatio(unit2) == 1.0 -> temperatureFormula[3]
                getRatio(unit1) == 2.0 && getRatio(unit2) == 3.0 -> temperatureFormula[4]
                getRatio(unit1) == 3.0 && getRatio(unit2) == 2.0 -> temperatureFormula[5]
                else -> temperatureFormula[6]
            }
    }

}

private fun readDate(): Array<Any> {
    var indexInput = 0
    print("Enter what you want to convert (or exit): ")
    val nm = readLine()!!.split(" ")
    val firstValue = nm[indexInput++].trim()
    if (firstValue == "exit") return arrayOf("exit")
    else if (firstValue.toDoubleOrNull() == null) {
        return arrayOf("Parse error")
    }
    val value = firstValue.toDouble()
    var unit1 = nm[indexInput++].trim().toLowerCase()
    if ((unit1 == "degree") or (unit1 == "degrees"))
        unit1 += " " + nm[indexInput++].trim().toLowerCase()
    var unit2 = nm[++indexInput].trim().toLowerCase()
    if ((unit2 == "degree") or (unit2 == "degrees"))
        unit2 += " " + nm[++indexInput].trim().toLowerCase()

    return arrayOf(value, unit1, unit2)
}

private fun printDate(value: Double, unit1: String, unit2: String){
    if (ConvertedUnits.isSameType(unit1, unit2) && ConvertedUnits.getType(unit1) != UnitType.WRONG_TYPE) {
        when {
            (ConvertedUnits.getType(unit1) == UnitType.LENGTH) and (value < 0) -> println("Length shouldn't be negative.")

            (ConvertedUnits.getType(unit1) == UnitType.WEIGHT) and (value < 0) ->
                println("Weight shouldn't be negative.")

            else -> {
                val result = ConvertedUnits.convertToNewUnit(value, unit1, unit2)
                println(
                    "$value ${
                        ConvertedUnits.getUnitName(
                            unit1,
                            value == 1.0
                        )
                    } is $result ${ConvertedUnits.getUnitName(unit2, result == 1.0)}"
                )
            }
        }
    } else {
        println(
            "Conversion from ${ConvertedUnits.getUnitName(unit1, false)} to ${ConvertedUnits.getUnitName(
                unit2,
                false
            )} is impossible"
        )
    }
}

fun main(args: Array<String>) {
    while (true) {
        val value: Double
        val unit1: String
        val unit2: String

        val inputs = readDate()

        if (inputs[0] == "exit") break
        else if (inputs[0] == "Parse error") {
            println("Parse error")
            continue
        } else {
            value = inputs[0] as Double
            unit1 = inputs[1] as String
            unit2 = inputs[2] as String
            printDate(value, unit1, unit2)
        }
    }
}