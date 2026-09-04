package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.domain.models.ExtTemplateDTO

@Composable
fun JsonSampleDialog(
    template: ExtTemplateDTO?,
    isTransaction: Boolean,
    format: String = "JSON",
    typeName: String = "ProductSv",
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    val sampleText = remember(template, format, isTransaction, typeName) {
        generateSampleContent(format, template, isTransaction, typeName)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${format.uppercase()} importo pavyzdys")
                if (isCopied) {
                    Text("Nukopijuota!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (template != null) {
                        "Pavyzdys suformatuotas pagal „${format.uppercase()}“ formatą ir šabloną: „${template.name}“"
                    } else {
                        "Pavyzdys suformatuotas pagal „${format.uppercase()}“ formatą be papildomų atributų."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = sampleText,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(sampleText))
                        isCopied = true
                    }
                ) {
                    Text("Kopijuoti")
                }
                Button(onClick = onDismiss) {
                    Text("Uždaryti")
                }
            }
        }
    )
}

private fun generateSampleContent(
    format: String,
    template: ExtTemplateDTO?,
    isTransaction: Boolean,
    typeName: String = "ProductSv"
): String {
    val upperFormat = format.uppercase().trim()
    val attributes = template?.attributes ?: emptyList()
    val isPartner = typeName in listOf("PartnerSv", "Partner", "ClientSv", "Client", "OwnerSv", "SupplierSv", "CustomerSv")
    val isService = typeName in listOf("ServiceSv", "Service", "Paslauga")
    val isWarehouse = typeName in listOf("WarehouseSv", "Warehouse", "Sandėlis")
    val isDivision = typeName in listOf("DivisionSv", "Division", "Padalinys")

    return when (upperFormat) {
        "EIP" -> {
            if (isTransaction) {
                val customDetailTags = if (attributes.isNotEmpty()) {
                    val sb = StringBuilder()
                    sb.append("        <!-- Šablono „${template?.name}“ papildomi laukai -->\n")
                    attributes.forEachIndexed { idx, attr ->
                        val tag = if (!attr.tag.isNullOrBlank()) attr.tag!! else "PAP_${idx + 1}"
                        sb.append("        <$tag>Pavyzdinė reikšmė</$tag> <!-- SV: ${attr.name} -->\n")
                    }
                    sb.toString().trimEnd()
                } else ""

                """<I06>
    <I06_DOK_NR>SF-2026-0001</I06_DOK_NR>
    <I06_OP_DATA>2026-09-01</I06_OP_DATA>
    <I06_KODAS_KS>KL-0001</I06_KODAS_KS>
    <I06_PAV>UAB Pavyzdinė Įmonė</I06_PAV>
    <I06_SUMA>100.00</I06_SUMA>
    <I06_SUMA_PVM>21.00</I06_SUMA_PVM>
    <I06_SUMA_PLIUS>121.00</I06_SUMA_PLIUS>
    <I06_PASTABOS>Apmokėti per 14 dienų</I06_PASTABOS>
    <I07>
        <I07_KODAS_PS>PR-0001</I07_KODAS_PS>
        <I07_PAV>Pavyzdinė Prekė</I07_PAV>
        <I07_KIEKIS>5</I07_KIEKIS>
        <I07_KAINA>20.00</I07_KAINA>
        <I07_SUMA>100.00</I07_SUMA>
        <I07_MOKESTIS>21</I07_MOKESTIS>
        <I07_SUMA_PVM>21.00</I07_SUMA_PVM>
        <I07_SUMA_PLIUS>121.00</I07_SUMA_PLIUS>
${if (customDetailTags.isNotEmpty()) "$customDetailTags\n" else ""}    </I07>
</I06>"""
            } else {
                val eipAttributes = if (attributes.isNotEmpty()) {
                    val sb = StringBuilder()
                    sb.append("    <!-- Šablono „${template?.name}“ papildomi laukai (PAP_1, PAP_2...) -->\n")
                    attributes.forEachIndexed { idx, attr ->
                        val tagName = if (!attr.tag.isNullOrBlank()) attr.tag!! else "PAP_${idx + 1}"
                        val sampleVal = when {
                            attr.name.contains("metai", ignoreCase = true) -> "2026"
                            attr.name.contains("modelis", ignoreCase = true) -> "Pavyzdinis modelis"
                            attr.name.contains("spalv", ignoreCase = true) -> "Pavyzdinė spalva"
                            attr.name.contains("pavadin", ignoreCase = true) -> "Pavyzdinis pavadinimas"
                            attr.name.contains("kodas", ignoreCase = true) -> "KOD-001"
                            attr.name.contains("barkod", ignoreCase = true) || attr.name.contains("brūkšn", ignoreCase = true) -> "4770000000000"
                            attr.name.contains("vienet", ignoreCase = true) || attr.name.contains("vnt", ignoreCase = true) -> "vnt."
                            else -> "Reikšmė (${attr.attributeType})"
                        }
                        sb.append("    <$tagName>$sampleVal</$tagName> <!-- SV: ${attr.name} -->\n")
                    }
                    sb.toString().trimEnd()
                } else ""

                val extraAttrBlock = if (eipAttributes.isNotBlank()) "\n$eipAttributes" else ""

                when {
                    isPartner -> {
                        """<N08>
    <N08_KODAS_KS>KL-0001</N08_KODAS_KS>
    <N08_RUSIS>3</N08_RUSIS>
    <N08_PAV>UAB Pavyzdinė Įmonė</N08_PAV>
    <N08_IM_KODAS>123456789</N08_IM_KODAS>
    <N08_PVM_KODAS>LT123456789012</N08_PVM_KODAS>
    <N08_ADR>Pavyzdžio g. 1, Vilnius</N08_ADR>
    <N08_PASTAS>LT-00001</N08_PASTAS>
    <N08_TEL>+37050000000</N08_TEL>
    <N08_MOB_TEL>+37060000000</N08_MOB_TEL>
    <N08_E_MAIL>info@example.com</N08_E_MAIL>
    <N08_ATSTOVAS>Vardenis Pavardenis</N08_ATSTOVAS>$extraAttrBlock
</N08>"""
                    }
                    isService -> {
                        """<N17>
    <N17_KODAS_PS>PASL-0001</N17_KODAS_PS>
    <N17_TIPAS>2</N17_TIPAS>
    <N17_PAV>Pavyzdinė Paslauga</N17_PAV>
    <N17_KODAS_US>VNT</N17_KODAS_US>
    <N17_KODAS_DS>PR001</N17_KODAS_DS>
    <N17_MOKESTIS>1</N17_MOKESTIS>
    <N17_TAX>1</N17_TAX>$extraAttrBlock
</N17>"""
                    }
                    isWarehouse -> {
                        """<N06>
    <N06_KODAS_SS>SAND-001</N06_KODAS_SS>
    <N06_PAV>Pavyzdinis Sandėlis</N06_PAV>$extraAttrBlock
</N06>"""
                    }
                    isDivision -> {
                        """<N07>
    <N07_KODAS_IS>PAD-001</N07_KODAS_IS>
    <N07_PAV>Pavyzdinis Padalinys</N07_PAV>$extraAttrBlock
</N07>"""
                    }
                    else -> { // Default Product (N17)
                        """<N17>
    <N17_KODAS_PS>PR-0001</N17_KODAS_PS>
    <N17_TIPAS>1</N17_TIPAS>
    <N17_PAV>Pavyzdinė Prekė</N17_PAV>
    <N17_BAR_KODAS>4770000000000</N17_BAR_KODAS>
    <N17_KODAS_US>VNT</N17_KODAS_US>
    <N17_KODAS_KS>KL-0001</N17_KODAS_KS>
    <N17_KODAS_DS>PR001</N17_KODAS_DS>
    <N17_SAVIKAINA>10.00</N17_SAVIKAINA>
    <N17_KAINA1>15.00</N17_KAINA1>
    <N17_MOKESTIS>1</N17_MOKESTIS>
    <N17_TAX>1</N17_TAX>$extraAttrBlock
</N17>"""
                    }
                }
            }
        }
        "CSV" -> {
            if (isTransaction) {
                """Dokumento_Nr,Data,Partneris,Prekės_Kodas,Pavadinimas,Kiekis,Kaina,PVM_Proc,Suma
SF-2026-0001,2026-09-01,"UAB Pavyzdinė Įmonė",PR-0001,"Pavyzdinė Prekė",5,20.00,21,121.00
SF-2026-0002,2026-09-01,"UAB Kita Įmonė",PR-0002,"Kita Prekė",2,15.50,21,37.51"""
            } else if (isPartner) {
                val headerAttr = if (attributes.isNotEmpty()) {
                    "," + attributes.joinToString(",") { "\"${it.name}\"" }
                } else ""

                val row1Attr = if (attributes.isNotEmpty()) {
                    "," + attributes.joinToString(",") { "\"Reikšmė\"" }
                } else ""

                """Kodas,Pavadinimas,Imones_Kodas,PVM_Kodas,Adresas,Telefonas,Email$headerAttr
KL-0001,"UAB Pavyzdinė Įmonė",123456789,LT123456789012,"Pavyzdžio g. 1, Vilnius",+37050000000,info@example.com$row1Attr"""
            } else {
                val headerAttr = if (attributes.isNotEmpty()) {
                    "," + attributes.joinToString(",") { "\"${it.name}\"" }
                } else ""

                val row1Attr = if (attributes.isNotEmpty()) {
                    "," + attributes.joinToString(",") { attr ->
                        when {
                            attr.name.contains("metai", ignoreCase = true) -> "2026"
                            attr.name.contains("modelis", ignoreCase = true) -> "\"Pavyzdinis Modelis\""
                            else -> "\"Pavyzdys\""
                        }
                    }
                } else ""

                val row2Attr = if (attributes.isNotEmpty()) {
                    "," + attributes.joinToString(",") { attr ->
                        when {
                            attr.name.contains("metai", ignoreCase = true) -> "2025"
                            attr.name.contains("modelis", ignoreCase = true) -> "\"Kitas Modelis\""
                            else -> "\"Pavyzdys 2\""
                        }
                    }
                } else ""

                """Kodas,Pavadinimas,Barkodas,Matavimo_Vnt,Savikaina,Kaina$headerAttr
PR-0001,"Pavyzdinė Prekė",4770000000000,vnt.,10.00,15.00$row1Attr
PR-0002,"Kita Prekė",4770000000001,vnt.,18.00,25.50$row2Attr"""
            }
        }
        "XML" -> {
            if (isTransaction) {
                """<?xml version="1.0" encoding="UTF-8"?>
<Invoices>
    <Invoice>
        <DocumentNumber>SF-2026-0001</DocumentNumber>
        <Date>2026-09-01</Date>
        <Partner>UAB Pavyzdinė Įmonė</Partner>
        <Details>
            <Item>
                <Code>PR-0001</Code>
                <Description>Pavyzdinė Prekė</Description>
                <Quantity>5.0</Quantity>
                <Price>20.00</Price>
                <VatRate>21.0</VatRate>
                <TotalAmount>121.00</TotalAmount>
            </Item>
        </Details>
    </Invoice>
</Invoices>"""
            } else if (isPartner) {
                val xmlAttributes = if (attributes.isNotEmpty()) {
                    val sb = StringBuilder("        <Attributes>\n")
                    attributes.forEach { attr ->
                        sb.append("            <Attribute name=\"${attr.name}\">Reikšmė (${attr.attributeType})</Attribute>\n")
                    }
                    sb.append("        </Attributes>")
                    sb.toString()
                } else ""

                """<?xml version="1.0" encoding="UTF-8"?>
<Partners>
    <Partner>
        <Code>KL-0001</Code>
        <Name>UAB Pavyzdinė Įmonė</Name>
        <CompanyCode>123456789</CompanyCode>
        <VatCode>LT123456789012</VatCode>
        <Address>Pavyzdžio g. 1, Vilnius</Address>
        <Phone>+37050000000</Phone>
        <Email>info@example.com</Email>
$xmlAttributes
    </Partner>
</Partners>"""
            } else {
                val xmlAttributes = if (attributes.isNotEmpty()) {
                    val sb = StringBuilder("        <Attributes>\n")
                    attributes.forEach { attr ->
                        val sampleVal = when {
                            attr.name.contains("metai", ignoreCase = true) -> "2026"
                            attr.name.contains("modelis", ignoreCase = true) -> "Pavyzdinis modelis"
                            else -> "Reikšmė (${attr.attributeType})"
                        }
                        sb.append("            <Attribute name=\"${attr.name}\">$sampleVal</Attribute>\n")
                    }
                    sb.append("        </Attributes>")
                    sb.toString()
                } else ""

                """<?xml version="1.0" encoding="UTF-8"?>
<Products>
    <Product>
        <Code>PR-0001</Code>
        <Name>Pavyzdinė Prekė</Name>
        <Barcode>4770000000000</Barcode>
        <Unit>vnt.</Unit>
        <Price>15.00</Price>
$xmlAttributes
    </Product>
</Products>"""
            }
        }
        "XLSX" -> {
            if (isTransaction) {
                """Stulpelių struktūra Excel lentelėje (Sheet1):

[A] Dokumento Nr  : SF-2026-0001
[B] Data          : 2026-09-01
[C] Partneris     : UAB Pavyzdinė Įmonė
[D] Prekės Kodas  : PR-0001
[E] Pavadinimas   : Pavyzdinė Prekė
[F] Kiekis        : 5
[G] Kaina         : 20.00
[H] PVM %         : 21
[I] Suma su PVM   : 121.00

Pavyzdinė eilutė:
SF-2026-0001 | 2026-09-01 | UAB Pavyzdinė Įmonė | PR-0001 | Pavyzdinė Prekė | 5 | 20.00 | 21 | 121.00"""
            } else if (isPartner) {
                val attrColumns = if (attributes.isNotEmpty()) {
                    attributes.mapIndexed { idx, it -> "[${('H' + idx)}] ${it.name}" }.joinToString(" | ")
                } else ""

                val attrValues = if (attributes.isNotEmpty()) {
                    " | " + attributes.joinToString(" | ") { "Pavyzdys" }
                } else ""

                """Stulpelių struktūra Excel lentelėje (Sheet1):

[A] Kodas | [B] Pavadinimas | [C] Įmonės kodas | [D] PVM kodas | [E] Adresas | [F] Telefonas | [G] El. paštas ${if (attrColumns.isNotEmpty()) "| $attrColumns" else ""}

Pavyzdinė eilutė:
KL-0001 | UAB Pavyzdinė Įmonė | 123456789 | LT123456789012 | Pavyzdžio g. 1, Vilnius | +37050000000 | info@example.com$attrValues"""
            } else {
                val attrColumns = if (attributes.isNotEmpty()) {
                    attributes.mapIndexed { idx, it -> "[${('G' + idx)}] ${it.name}" }.joinToString(" | ")
                } else ""

                val attrValues = if (attributes.isNotEmpty()) {
                    " | " + attributes.joinToString(" | ") { attr ->
                        when {
                            attr.name.contains("metai", ignoreCase = true) -> "2026"
                            attr.name.contains("modelis", ignoreCase = true) -> "Pavyzdinis modelis"
                            else -> "Pavyzdys"
                        }
                    }
                } else ""

                """Stulpelių struktūra Excel lentelėje (Sheet1):

[A] Kodas | [B] Pavadinimas | [C] Barkodas | [D] Matavimo Vnt | [E] Savikaina | [F] Kaina ${if (attrColumns.isNotEmpty()) "| $attrColumns" else ""}

Pavyzdinė eilutė:
PR-0001 | Pavyzdinė Prekė | 4770000000000 | vnt. | 10.00 | 15.00$attrValues"""
            }
        }
        "TXT", "WORD", "PDF" -> {
            if (isTransaction) {
                """SĄSKAITA FAKTŪRA
Serija ir numeris: SF-2026-0001
Data: 2026-09-01
Pardavėjas / Partneris: UAB Pavyzdinė Įmonė

Prekės / Paslaugos:
1. Prekė: Pavyzdinė Prekė (Kodas: PR-0001)
   Kiekis: 5 vnt.
   Kaina: 20.00 EUR
   PVM: 21%
   Bendra suma su PVM: 121.00 EUR"""
            } else if (isPartner) {
                val txtAttr = if (attributes.isNotEmpty()) {
                    "\nPapildomi šablono atributai:\n" + attributes.joinToString("\n") { attr ->
                        "- ${attr.name}: Pavyzdinė reikšmė"
                    }
                } else ""

                """Partnerio kortelė:
Kodas: KL-0001
Pavadinimas: UAB Pavyzdinė Įmonė
Įmonės kodas: 123456789
PVM kodas: LT123456789012
Adresas: Pavyzdžio g. 1, Vilnius
Telefonas: +37050000000
El. paštas: info@example.com$txtAttr"""
            } else {
                val txtAttr = if (attributes.isNotEmpty()) {
                    "\nPapildomi šablono atributai:\n" + attributes.joinToString("\n") { attr ->
                        val sampleVal = when {
                            attr.name.contains("metai", ignoreCase = true) -> "2026"
                            attr.name.contains("modelis", ignoreCase = true) -> "Pavyzdinis modelis"
                            else -> "Pavyzdinė reikšmė"
                        }
                        "- ${attr.name}: $sampleVal"
                    }
                } else ""

                """Prekės kortelė:
Kodas: PR-0001
Pavadinimas: Pavyzdinė Prekė
Barkodas: 4770000000000
Matavimo vienetas: vnt.
Kaina: 15.00 EUR$txtAttr"""
            }
        }
        else -> { // Default JSON
            val attributesString = attributes.joinToString(",\n      ") {
                val tagJson = if (!it.tag.isNullOrBlank()) ",\n        \"tag\": \"${it.tag}\"" else ""
                "{\n        \"name\": \"${it.name}\",\n        \"value\": \"Reikšmė (${it.attributeType})\"$tagJson\n      }"
            }

            val attributesField = if (attributesString.isNotEmpty()) {
                ",\n    \"attributes\": [\n      $attributesString\n    ]"
            } else ""

            if (isTransaction) {
                """[
  {
    "documentNumber": "SF-2026-0001",
    "documentDate": "2026-09-01",
    "partnerName": "UAB Pavyzdinė Įmonė",
    "details": [
      {
        "description": "Prekės ar paslaugos aprašymas",
        "quantity": 5.0,
        "price": 20.0,
        "vatRate": 21.0,
        "vatAmount": 21.0,
        "totalAmount": 121.0
      }
    ]$attributesField
  }
]"""
            } else if (isPartner) {
                """[
  {
    "name": "UAB Pavyzdinė Įmonė",
    "code": "KL-0001",
    "companyCode": "123456789",
    "vatCode": "LT123456789012",
    "address": "Pavyzdžio g. 1, Vilnius",
    "phone": "+37050000000",
    "email": "info@example.com"$attributesField
  }
]"""
            } else {
                """[
  {
    "name": "Pavyzdinė Prekė",
    "code": "PR-0001",
    "barcode": "4770000000000",
    "baseUnit": "vnt.",
    "conversionFactor": 1.0,
    "price": 15.00$attributesField
  }
]"""
            }
        }
    }
}
