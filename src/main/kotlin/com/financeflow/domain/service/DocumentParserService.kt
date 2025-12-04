package com.financeflow.domain.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DocumentParserService {
    
    private val amountPatterns = listOf(
        Regex("""\$\s*([\d,]+\.?\d*)"""),
        Regex("""Total[:\s]*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE),
        Regex("""Monto[:\s]*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE),
        Regex("""Amount[:\s]*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
    )
    
    private val datePatterns = listOf(
        Regex("""\d{2}/\d{2}/\d{4}"""),
        Regex("""\d{4}-\d{2}-\d{2}"""),
        Regex("""\d{2}-\d{2}-\d{4}""")
    )
    
    fun parseDocument(file: File, mimeType: String): DocumentMetadata {
        if (!mimeType.contains("pdf")) {
            return DocumentMetadata(null, null)
        }
        
        return try {
            val document = Loader.loadPDF(file)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            
            val amount = extractAmount(text)
            val date = extractDate(text)
            
            DocumentMetadata(amount, date)
        } catch (e: Exception) {
            DocumentMetadata(null, null)
        }
    }
    
    private fun extractAmount(text: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val value = match.groupValues[1].replace(",", "")
                return value.toDoubleOrNull()
            }
        }
        return null
    }
    
    private fun extractDate(text: String): LocalDate? {
        for (pattern in datePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return parseDate(match.value)
            }
        }
        return null
    }
    
    private fun parseDate(dateStr: String): LocalDate? {
        val formats = listOf("dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy")
        for (format in formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format))
            } catch (e: Exception) { }
        }
        return null
    }
}

data class DocumentMetadata(val amount: Double?, val date: LocalDate?)
