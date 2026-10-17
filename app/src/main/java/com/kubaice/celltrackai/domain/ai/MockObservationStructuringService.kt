package com.kubaice.celltrackai.domain.ai

import com.kubaice.celltrackai.domain.model.CellCulture
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockObservationStructuringService @Inject constructor() : ObservationStructuringService {

    override suspend fun structureObservation(
        rawText: String,
        cultureContext: CellCulture?
    ): StructuredObservationResult {
        val lower = rawText.lowercase()

        // Extract confluency percentage via Regex
        val confluencyRegex = Regex("(\\d{1,3})\\s*\\%|confluen\\w*\\s*(?:at|is|~|=|:)?\\s*(\\d{1,3})")
        val confluencyMatch = confluencyRegex.find(lower)
        val parsedConfluency = confluencyMatch?.groupValues?.filter { it.isNotBlank() }?.getOrNull(1)?.toFloatOrNull()
            ?: if (lower.contains("confluent") || lower.contains("full monolayer")) 90.0f else null

        // Extract passage number via Regex
        val passageRegex = Regex("(?:passage|p\\.?)\\s*(\\d{1,3})")
        val passageMatch = passageRegex.find(lower)
        val parsedPassage = passageMatch?.groupValues?.getOrNull(1)?.toIntOrNull()

        // Cell Type parsing
        val cellType = cultureContext?.cellType ?: when {
            lower.contains("hela") -> "HeLa"
            lower.contains("ipsc") || lower.contains("stem") -> "iPSC"
            lower.contains("vero") -> "Vero E6"
            lower.contains("hek") || lower.contains("293") -> "HEK293"
            lower.contains("msc") -> "MSC"
            else -> null
        }

        // Phenotypes
        val phenotypes = mutableListOf<String>()
        if (lower.contains("cobblestone")) phenotypes.add("Cobblestone morphology")
        if (lower.contains("detached") || lower.contains("floating")) phenotypes.add("Cell detachment observed")
        if (lower.contains("elongated") || lower.contains("spindle")) phenotypes.add("Spindle-like morphology")
        if (lower.contains("vacuole")) phenotypes.add("Intracellular vacuolation")
        if (lower.contains("clumping") || lower.contains("colonies")) phenotypes.add("Colony formation / clumping")
        if (phenotypes.isEmpty() && parsedConfluency != null) {
            phenotypes.add("Uniform monolayer at ${parsedConfluency.toInt()}% confluency")
        }

        // Contamination risk
        val contaminationRisk = when {
            lower.contains("cloudy") || lower.contains("turbid") || lower.contains("yellow") ->
                "HIGH: Rapid media acidification or turbidity indicative of bacterial contamination"
            lower.contains("debris") || lower.contains("black dots") || lower.contains("moving") ->
                "MEDIUM: Microscopic particulate debris observed; monitor for motility"
            lower.contains("fungal") || lower.contains("hyphae") || lower.contains("mold") ->
                "CRITICAL: Fungal hyphae structures detected"
            else -> "LOW: No visual cues of microbial contamination detected"
        }

        // Recommended actions
        val actions = mutableListOf<String>()
        if (parsedConfluency != null && parsedConfluency >= 80.0f) {
            actions.add("Passage/split culture at 1:3 to 1:6 ratio within 24 hours")
        } else if (parsedConfluency != null && parsedConfluency < 50.0f) {
            actions.add("Perform 50% media change and continue incubation")
        }
        if (lower.contains("cloudy") || lower.contains("fungal")) {
            actions.add("Isolate culture flask immediately to prevent cross-contamination")
            actions.add("Perform Mycoplasma PCR / sterility test")
        }

        // Missing fields
        val missing = mutableListOf<String>()
        if (parsedConfluency == null) missing.add("Confluency percentage not explicitly specified")
        if (parsedPassage == null) missing.add("Passage number missing from text note")

        val explanation = "Successfully parsed observation text. Extracted ${phenotypes.size} morphological traits, assessed contamination risk, and generated automated recommendations."

        return StructuredObservationResult(
            parsedCellType = cellType,
            parsedConfluencyPct = parsedConfluency,
            parsedPassageNumber = parsedPassage,
            observedPhenotypes = phenotypes,
            contaminationRisk = contaminationRisk,
            recommendedActions = actions,
            missingOrUncertainFields = missing,
            explanation = explanation
        )
    }
}
