package com.suprogramuota_visata.vedlys.ui.components

import com.suprogramuota_visata.vedlys.AppLanguage

data class UIRuleDef(
    val id: Int, 
    val descriptionLt: String, 
    val descriptionEn: String, 
    val argNamesLt: List<String>, 
    val argNamesEn: List<String>
) {
    fun getDescription(language: AppLanguage) = if (language == AppLanguage.LT) descriptionLt else descriptionEn
    fun getArgNames(language: AppLanguage) = if (language == AppLanguage.LT) argNamesLt else argNamesEn
}

val ValidationUIRules = listOf(
    UIRuleDef(1, "Ar reikšmė nėra tuščia", "Is a value is not empty", emptyList(), emptyList()),
    UIRuleDef(2, "Reikšmė yra ne mažesnė ir ne didesnė nei", "Is the value no less and no greater than", listOf("Min", "Max"), listOf("Min", "Max")),
    UIRuleDef(3, "Reikšmė yra didesnė ar lygi 0", "Is the value greater than or equal to 0", emptyList(), emptyList()),
    UIRuleDef(4, "Reikšmė yra mažesnė ar lygi 0", "Is the value less than or equal to 0", emptyList(), emptyList()),
    UIRuleDef(5, "Ar įvesta reikšmė patenka į tarpą", "Does the entered value fall between", listOf("Pradžia", "Pabaiga"), listOf("Start", "End")),
    UIRuleDef(6, "Ar įvesta reikšmė sutampa su reikšmėmis sąraše (atskirtame kableliais)", "Whether the entered value falls between the values specified in the list, separated by a comma", listOf("Sąrašas"), listOf("List")),
    UIRuleDef(7, "Ar įvesta reikšmė atitinka minimalų ir maksimalų ilgį", "Whether the entered value meets the minimum and maximum length", listOf("Min ilgis", "Max ilgis"), listOf("Min length", "Max length")),
    UIRuleDef(8, "Ar reikšmė neviršija dydžio (baitais)", "Whether the value does not exceed the size (in bytes)", listOf("Max baitai"), listOf("Max bytes")),
    UIRuleDef(9, "Neviršija galiojimo", "Does Not Exceed Validity", listOf("Maksimali data (yyyy-MM-dd)"), listOf("Max Date (yyyy-MM-dd)")),
    UIRuleDef(10, "Reikšmė patenka į galiojimo intervalą", "The value falls within the valid range.", listOf("Pradžios data", "Pabaigos data"), listOf("Start Date", "End Date")),
    UIRuleDef(11, "Atitinka šabloną (Regex)", "Matches pattern/Regex", listOf("Šablonas (Regex)"), listOf("Regex pattern")),
    UIRuleDef(13, "Reikšmė privalo egzistuoti parinkto tipo sąraše", "Value must exist in the selected type list", emptyList(), emptyList()),
    UIRuleDef(14, "Validus IBAN numeris", "Valid IBAN number", emptyList(), emptyList()),
    UIRuleDef(15, "Validus SWIFT/BIC kodas", "Valid SWIFT/BIC code", emptyList(), emptyList()),
    UIRuleDef(16, "Reikšmė turi būti unikali šio tipo lentelėje", "Value must be unique across all records of this type", emptyList(), emptyList()),
    UIRuleDef(17, "Skenuoti Ciklopas įrengynyje", "Scan on Ciklopas device", emptyList(), emptyList())
)
