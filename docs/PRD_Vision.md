# Product Requirements Document (PRD): Allergie-Detektiv App

**Projekt-Name:** Allergie-Detektiv (Phasen-Modell 1 & 2)
**Version:** 2.2 (Update nach Review: Zweckbestimmung, Tages-Abschluss, Zutaten-Datenbank, Notfall-Hinweis)
**Strategie:** Local-First (Datenschutz), Mathematische Korrelation (Keine KI-Kosten), Faktor-Bereinigung (Schlaf & Stress), Vertrauensbasiertes Wachstum.

---

## 1. Produkt-Vision
Eine hochdiskrete Tagebuch-App zur **Selbstbeobachtung**, die ohne Cloud-KI auskommt. Sie zeigt **Muster in den eigenen Einträgen**: welche Lebensmittel, Inhaltsstoffe oder Lifestyle-Faktoren (Schlaf, Stress) auffällig oft vor Beschwerden auftreten und welche gut vertragen werden. Der Fokus liegt auf einem schnellen "Aha-Erlebnis", um Vertrauen für ein langfristiges Abo zu schaffen.

### 1.1 Zweckbestimmung (verbindlich für alle Texte)
Die App ist ein **Tagebuch zur Selbstbeobachtung** und eine **Grundlage für das Gespräch mit Arzt oder Therapeut**. Sie stellt **keine Diagnose** und behauptet nicht, Ursachen oder Allergien zu "identifizieren".
* ✅ "zeigt Muster", "Spur", "Verdacht", "fiel auf", "gut vertragen"
* ❌ "Diagnose", "Ursache gefunden", "Allergie erkannt", "Wahrscheinlichkeit für Allergie"

Hintergrund: Je nach Zweckbestimmung kann Software in der EU unter die Medizinprodukte-Verordnung (MDR) fallen. Vor dem Launch fachlich prüfen lassen.

---

## 2. Kernfunktionen (Features)

### 2.1 Intelligentes Ernährungs-Logbuch
* **Favoriten-System (Wort-Speicher):**
    * User speichern komplexe Mahlzeiten (z.B. "Pizza Margarita").
    * Hinterlegung von Einzelzutaten pro Favorit (z.B. Weißmehl, Käse, Tomate).
    * Ein-Klick-Loggen zerlegt die Mahlzeit automatisch in Einzelkomponenten für die Analyse.
* **Vorbefüllte Zutaten-Datenbank:** ca. 100 Alltags-Zutaten mit hinterlegten Auslösern (Histamin, Gluten, Laktose, FODMAP, EU-14-Allergene …), offline mitgeliefert. Eigene Zutaten erben die Auslöser einer ähnlichen Zutat. Details: [CONTENT_Zutaten.md](CONTENT_Zutaten.md)
* **Portionsgröße S / M / L** (Default M), erklärt per Hand-Maß.
* **"Wie gestern?"**: zuletzt gegessene Mahlzeiten als Ein-Tap-Vorschläge.
* **Sprachsteuerung (Free Voice-In):** Nutzung der systemeigenen, kostenlosen Speech-to-Text-Funktion, mit unscharfer Suche.
* **Zeitstempel:** Automatisch "Jetzt", per Slider nachtragbar.

### 2.2 Symptom- & Intensitäts-Tracking
* **Symptom-Katalog (~24 Symptome):** Strukturierte Auswahl in vier Gruppen (Magen-Darm, Haut, Kopf/Kreislauf, Atemwege/Augen/Mund) + "Sonstiges". Details: [CONTENT_Symptome.md](CONTENT_Symptome.md)
* **Intensitätsskala:** 1 bis 5 (große Tap-Punkte, Default 3).
* **Isoliertes Logging:** User trägt Symptome ein, wenn sie auftreten. Die App übernimmt die Korrelation zur Vergangenheit (keine Kausalitäts-Pflicht für den User).
* **Notfall-Hinweis:** Bei Atemnot oder Schwellung zeigt die App sofort einen Hinweis mit Notruf-Button (112).

### 2.3 Tages-Abschluss & Störfaktoren
* **Ein täglicher Check (abends, ca. 10 Sek.):** Wohlbefinden (1–5), Schlaf der letzten Nacht (Stunden + Qualität), Stress (1–5), optionale Tages-Faktoren (Alkohol, Sport, Medikament, Periode).
* **Bestätigung:** *"Keine Beschwerden"* oder *"Alles eingetragen"*. Erst bestätigte Tage zählen für die Analyse. Dadurch weiß die App, wann wirklich nichts war.
* **Störfaktoren als eigene Verdächtige:** Schlechter Schlaf, Stress usw. erscheinen im selben Ranking wie Lebensmittel (z.B. *"Kopfschmerzen hängen eher mit kurzen Nächten zusammen"*).

### 2.4 Die Korrelations-Engine (Berechnungs-Logik)
* **Mathematisches Modell:** Vergleich "mit vs. ohne". Wie oft folgen Beschwerden auf Mahlzeiten mit einem Auslöser, verglichen mit Mahlzeiten ohne ihn? Häufig gegessene Lebensmittel werden so nicht automatisch verdächtig.
* **Zeitfenster pro Auslöser:** z.B. Allergene 0–2h, Laktose bis ca. 6h, Gluten bis 48h. Nur zum Auslöser passende Symptome zählen. Details: [TRD_Technik.md](TRD_Technik.md)
* **Evidenzstufen statt Prozent:** Spur → Verdacht → Starker Verdacht.
* **"Gut vertragen"-Liste:** Zutaten, die mehrfach ohne passende Beschwerden gegessen wurden.
* **Bekannte Allergien** werden nicht analysiert, sondern beim Loggen als Hinweis angezeigt.

---

## 3. Monetarisierung (Vertrauens-Modell)

* **Gratis-Einstieg:** Die erste vollständige Analyse (nach ca. 7 Tagen Daten-Sammlung) ist inklusive aller Namen der verdächtigen Inhaltsstoffe kostenlos.
* **Premium-Abo (Detektiv-Pro):**
    * Fortlaufende Echtzeit-Analyse nach der ersten Woche.
    * Identifikation komplexer Kreuz-Korrelationen.
    * Export-Funktion: Professioneller PDF-Report für den Arztbesuch.
* **Preisstrategie:** Monatliches Kleinst-Abo (Low-Price), da nahezu keine Server-Kosten anfallen.

---

## 4. Technische Rahmenbedingungen
* **Datenschutz:** "Offline-First" – Alle Gesundheitsdaten verbleiben auf dem Gerät des Users.
* **Technologie:** SQLite für lokale Daten; System-APIs für Sprache und In-App-Käufe.
* **Sicherheit:** Keine Weitergabe von Daten an Dritte; keine Cloud-Speicherung ohne expliziten Wunsch.

Details: [TRD_Technik.md](TRD_Technik.md)

---

## 5. Roadmap (Strategischer Phasenplan)

### Phase 1: MVP (Technisches Fundament)
* Entwicklung der Core-App (Tagebuch, Favoriten, Schlaf-Log).
* Programmierung der mathematischen Analyse-Engine.
* Fokus: Stabilität und intuitive Bedienung.

Detaillierter Bau-Fahrplan: [ROADMAP_Entwicklung.md](ROADMAP_Entwicklung.md)

### Phase 2: Vermarktung & User-Acquisition (NEU)
* **App Store Optimization (ASO):** Optimierung auf Keywords wie "Bauchschmerzen Ursache", "Histamin-Check", "Migräne-Trigger".
* **Social Media Content:** Aufklärungsvideos über "Versteckte Allergie-Auslöser" (TikTok/Reels).
* **Review-Mining:** Automatisi… *(Text im Original abgeschnitten – bitte ergänzen)*

Store-Text: [marketing/STORE_Beschreibung.md](marketing/STORE_Beschreibung.md)
