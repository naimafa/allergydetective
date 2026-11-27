package com.nfar.allergydetective.domain.diary

import kotlin.test.Test
import kotlin.test.assertEquals

class PackageIngredientsTest {

    @Test
    fun takesOnlyTheIngredientListAndDropsPercentagesAndTheNutritionTable() {
        val text = "Müsli 500 g\nZutaten: Hafer-\nflocken (62 %), Rosinen 12,5%, Mandeln; Salz.\nKann Spuren von Erdnüssen enthalten.\nNährwerte pro 100 g"

        assertEquals("Haferflocken, Rosinen, Mandeln, Salz", PackageIngredients.clean(text))
    }

    @Test
    fun theListEndsAtItsFullStopEvenWhenTheTracesLineIsNotRecognised() {
        val text = "DE-ÖKO-001\nZutaten: Dinkelmehl¹ (eine Weizenart) 69 %, Rohrohr-\nzucker¹, Backpulver (Back-\ntriebmittel (Natriumcarbonate)), Meersalz, gemahlene Bourbon-Vanille¹.\n¹aus kontrolliert biologischem Anbau.\nKann Spuren von Milch, Ei, Soja.\nBackmischungen und Teige sind nicht zum Rohverzehr bestimmt."

        assertEquals("Dinkelmehl, rohrzucker, Backpulver, Meersalz, Bourbon-Vanille", PackageIngredients.clean(text))
    }

    @Test
    fun aMarketingTextWithTheWordIngredientsBeforeTheListIsSkipped() {
        val text = "durch wertvolle Zutaten. Für deinen bewussten\nLebensstil und puren Bio-Genuss.\nErfahre mehr über deine REWE Bio Produkte:\nwww.rewe.de/bio\nDE-ÖKO-001\nZutaten: Dinkelmehl¹ (eine Weizenart) 69 %, Maisstärke¹, Meersalz, gemahlene Bourbon-Vanille¹.\nKann Spuren von Milch enthalten."

        assertEquals("Dinkelmehl, Maisstärke, Meersalz, Bourbon-Vanille", PackageIngredients.clean(text))
    }

    @Test
    fun theFrontOfAPackageWithoutAListGivesNothing() {
        assertEquals("", PackageIngredients.clean("REWE Bio\nDinkel Kaiserschmarrn\nBackmischung mit Grieß\nFür deinen bewussten Lebensstil"))
    }

    @Test
    fun aRecipeKeepsOnlyItsIngredientsNotTheTitleLabelsOrSteps() {
        val text = "Caprese-Salat|H- 2+ Portionen|Glutenfrei|Zutaten|• 200 g Tomaten|Vegetarisch|• 125 g Mozzarela|•1 Bund Basilikum|• 2 EL Olivenöl|Zubereitung|Tomaten und Mozzarella in Scheiben|schneiden.|O 10 Min.".split('|').joinToString("\n")

        assertEquals("Tomaten, Mozzarela, Basilikum, Olivenöl", PackageIngredients.clean(text))
    }

    @Test
    fun worksWithoutAMarkerAndInEnglish() {
        assertEquals("milk, sugar, cocoa", PackageIngredients.clean("Ingredients: milk, sugar (20%), cocoa. May contain nuts."))
        assertEquals("Milch, Zucker", PackageIngredients.clean("Milch, Zucker"))
    }
}
