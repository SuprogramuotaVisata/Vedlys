package com.suprogramuota_visata.vedlys.utils

import com.suprogramuota_visata.vedlys.AppLanguage
import com.suprogramuota_visata.api.domain.models.ExtAttributeDTO
import com.suprogramuota_visata.api.domain.models.AttributeValidationDTO
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

fun validateTypeConcept(type: String, value: String, language: AppLanguage): String? {
    if (value.isBlank()) return null
    return when (type) {
        "INTEGER" -> {
            if (value.toIntOrNull() == null) {
                if (language == AppLanguage.EN) "Must be a valid integer" else "Turi būti sveikasis skaičius"
            } else null
        }
        "LONG" -> {
            if (value.toLongOrNull() == null) {
                if (language == AppLanguage.EN) "Must be a valid integer" else "Turi būti sveikasis skaičius"
            } else null
        }
        "DECIMAL" -> {
            if (value.toBigDecimalOrNull() == null) {
                if (language == AppLanguage.EN) "Must be a valid decimal number" else "Turi būti skaičius"
            } else null
        }
        "DATE" -> {
            try {
                LocalDate.parse(value.trim())
                null
            } catch (e: Exception) {
                if (language == AppLanguage.EN) "Must be a valid date (YYYY-MM-DD)" else "Turi būti data (metai-mėnuo-diena)"
            }
        }
        "DATE_TIME" -> {
            try {
                LocalDateTime.parse(value.trim())
                null
            } catch (e: Exception) {
                if (language == AppLanguage.EN) "Must be a valid date & time (YYYY-MM-DDTHH:MM)" else "Turi būti data ir laikas (metai-mėnuo-dienaThh:mm)"
            }
        }
        "TIMESTAMP" -> {
            if (value.toLongOrNull() == null) {
                if (language == AppLanguage.EN) "Must be a valid timestamp (numeric)" else "Turi būti laiko žyma (skaičius)"
            } else null
        }
        "BOOL" -> {
            val lower = value.trim().lowercase()
            if (lower != "true" && lower != "false") {
                if (language == AppLanguage.EN) "Must be 'true' or 'false'" else "Turi būti 'true' arba 'false'"
            } else null
        }
        "Local_Type" -> {
            val parts = value.split(" - ")
            if (parts.isEmpty() || parts[0].trim().toIntOrNull() == null) {
                if (language == AppLanguage.EN) "Please select from the list" else "Pasirinkite iš sąrašo"
            } else null
        }
        else -> null
    }
}

fun validateFieldValue(
    fieldName: String,
    value: String,
    extAttr: ExtAttributeDTO?,
    language: AppLanguage
): String? {
    if (extAttr == null) return null
    if (!extAttr.validateRule) {
        return validateTypeConcept(extAttr.attributeType, value, language)
    }
    val validations = extAttr.validations
    for (v in validations) {
        val ruleId = v.ruleId
        val args = v.args.map { it.toString() }
        when (ruleId) {
            1 -> {
                if (value.isBlank()) {
                    return if (language == AppLanguage.EN) "Field cannot be empty" else "Laukas negali būti tuščias"
                }
            }
            2 -> {
                val num = value.toBigDecimalOrNull()
                if (num == null) {
                    return if (language == AppLanguage.EN) "Must be a number" else "Turi būti skaičius"
                }
                val minStr = args.getOrNull(0)
                val min = if (minStr.isNullOrBlank()) null else minStr.toBigDecimalOrNull()
                val maxStr = args.getOrNull(1)
                val max = if (maxStr.isNullOrBlank()) null else maxStr.toBigDecimalOrNull()
                if (min != null && num < min) {
                    return if (language == AppLanguage.EN) "Cannot be less than $min" else "Negali būti mažesnis už $min"
                }
                if (max != null && num > max) {
                    return if (language == AppLanguage.EN) "Cannot be greater than $max" else "Negali būti didesnis už $max"
                }
            }
            3 -> {
                val num = value.toBigDecimalOrNull()
                if (num == null) {
                    return if (language == AppLanguage.EN) "Must be a number" else "Turi būti skaičius"
                }
                if (num < BigDecimal.ZERO) {
                    return if (language == AppLanguage.EN) "Cannot be negative" else "Negali būti neigiamas"
                }
            }
            4 -> {
                val num = value.toBigDecimalOrNull()
                if (num == null) {
                    return if (language == AppLanguage.EN) "Must be a number" else "Turi būti skaičius"
                }
                if (num > BigDecimal.ZERO) {
                    return if (language == AppLanguage.EN) "Cannot be positive" else "Negali būti teigiamas"
                }
            }
            5 -> {
                val num = value.toBigDecimalOrNull()
                if (num == null) {
                    return if (language == AppLanguage.EN) "Must be a number" else "Turi būti skaičius"
                }
                val startStr = args.getOrNull(0)
                val start = if (startStr.isNullOrBlank()) null else startStr.toBigDecimalOrNull()
                val endStr = args.getOrNull(1)
                val end = if (endStr.isNullOrBlank()) null else endStr.toBigDecimalOrNull()
                if (start != null && end != null) {
                    if (num !in start..end) {
                        return if (language == AppLanguage.EN) "Must be between $start and $end" else "Turi būti tarp $start ir $end"
                    }
                }
            }
            6 -> {
                val list = args.getOrNull(0)?.split(",")?.map { it.trim() } ?: emptyList()
                if (!list.contains(value.trim())) {
                    return if (language == AppLanguage.EN) "Must be one of: ${args.getOrNull(0)}" else "Turi būti viena iš: ${args.getOrNull(0)}"
                }
            }
            7 -> {
                val minStr = args.getOrNull(0)
                val min = if (minStr.isNullOrBlank()) 0 else minStr.toIntOrNull() ?: 0
                val maxStr = args.getOrNull(1)
                val max = if (maxStr.isNullOrBlank()) Int.MAX_VALUE else maxStr.toIntOrNull() ?: Int.MAX_VALUE
                if (value.length < min) {
                    return if (language == AppLanguage.EN) "Length cannot be less than $min" else "Ilgis negali būti mažesnis nei $min"
                }
                if (value.length > max) {
                    return if (language == AppLanguage.EN) "Length cannot be greater than $max" else "Ilgis negali būti didesnis nei $max"
                }
            }
            8 -> {
                val maxBytesStr = args.getOrNull(0)
                val maxBytes = if (maxBytesStr.isNullOrBlank()) Int.MAX_VALUE else maxBytesStr.toIntOrNull() ?: Int.MAX_VALUE
                if (value.toByteArray().size > maxBytes) {
                    return if (language == AppLanguage.EN) "Exceeds $maxBytes bytes" else "Viršija $maxBytes baitų"
                }
            }
            9 -> {
                val maxDateStr = args.getOrNull(0)
                if (!maxDateStr.isNullOrBlank() && value > maxDateStr) {
                    return if (language == AppLanguage.EN) "Exceeds max date $maxDateStr" else "Viršija maksimalią datą $maxDateStr"
                }
            }
            10 -> {
                val start = args.getOrNull(0)
                val end = args.getOrNull(1)
                if (!start.isNullOrBlank() && value < start) {
                    return if (language == AppLanguage.EN) "Before start date $start" else "Ankstesnė nei pradžios data $start"
                }
                if (!end.isNullOrBlank() && value > end) {
                    return if (language == AppLanguage.EN) "After end date $end" else "Vėlesnė nei pabaigos data $end"
                }
            }
            11 -> {
                var pattern = args.getOrNull(0)
                if (!pattern.isNullOrBlank()) {
                    if (pattern.startsWith("\"\"\"") && pattern.endsWith("\"\"\"") && pattern.length >= 6) {
                        pattern = pattern.substring(3, pattern.length - 3)
                    } else if (pattern.startsWith("\"") && pattern.endsWith("\"") && pattern.length >= 2) {
                        pattern = pattern.substring(1, pattern.length - 1)
                    }
                    try {
                        val regex = Regex(pattern)
                        if (!regex.matches(value)) {
                            return if (language == AppLanguage.EN) "Invalid format" else "Neteisingas formatas"
                        }
                    } catch (e: Exception) {}
                }
            }
            13 -> {
                val firstPart = value.split(" - ").firstOrNull()?.trim()
                if (firstPart == null || firstPart.toIntOrNull() == null) {
                    return if (language == AppLanguage.EN) "Please select from the list" else "Pasirinkite iš sąrašo"
                }
            }
            14 -> {
                if (value.isNotBlank()) {
                    val clean = value.replace("\\s".toRegex(), "").uppercase()
                    if (clean.length < 15 || clean.length > 34) {
                        return if (language == AppLanguage.EN) "Invalid IBAN length (15 to 34 characters)" else "Neteisingas IBAN ilgis (turi būti 15-34)"
                    }
                    if (!clean.matches(Regex("^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$"))) {
                        return if (language == AppLanguage.EN) "Invalid IBAN format" else "Neteisingas IBAN formatas"
                    }
                    val rearranged = clean.substring(4) + clean.substring(0, 4)
                    var remainder = 0
                    for (char in rearranged) {
                        val digitVal = if (char.isDigit()) char - '0' else char - 'A' + 10
                        remainder = (remainder * (if (digitVal < 10) 10 else 100) + digitVal) % 97
                    }
                    if (remainder != 1) {
                        return if (language == AppLanguage.EN) "Invalid IBAN checksum" else "Neteisinga IBAN kontrolinė suma"
                    }
                }
            }
            15 -> {
                if (value.isNotBlank()) {
                    val clean = value.replace("\\s".toRegex(), "").uppercase()
                    if (!clean.matches(Regex("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$"))) {
                        return if (language == AppLanguage.EN) "Invalid SWIFT/BIC format" else "Neteisingas SWIFT/BIC formatas"
                    }
                }
            }
            16 -> {
                // Unikalumas tikrinamas serverio pusėje, tad čia vietinė validacija visada praeina sėkmingai (null)
            }
            17 -> {
                // Skenuoti Ciklopas įrengynyje - ši taisyklė yra tik žymė terminalui, tad čia vietinė validacija visada praeina sėkmingai (null)
            }
        }
    }
    return validateTypeConcept(extAttr.attributeType, value, language)
}
