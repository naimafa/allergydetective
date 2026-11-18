# Technical Requirements Document (TRD): Allergie-Detektiv

**Version:** 1.4 (Überarbeitung nach Review, siehe [REVIEW_Analyse.md](REVIEW_Analyse.md))
**Architektur:** Local-First, Kotlin Multiplatform (Ziel Android + iOS, MVP nur Android), SQLite (Room KMP), Expert-System Logic. Siehe [ARCHITEKTUR.md](ARCHITEKTUR.md).
**Ziel:** Muster zwischen Ernährung, Symptomen und Lifestyle-Faktoren in den eigenen Einträgen sichtbar machen, ohne Cloud-Kosten.

> **Änderungen gegenüber 1.3:** Zutaten-Datenbank mit Trigger-Zuordnung, Zeitfenster pro Trigger statt starrer T1–T3, Plausibilität Trigger ↔ Symptom, Tages-Abschluss (negative Daten), Portionsgröße, Lift-Algorithmus statt Summen-Score, Lifestyle-Faktoren als eigene Kandidaten, Episoden-Bildung, Notfall-Symptome, Intensität 1–5.

---

## 1. Daten-Architektur (Backend-less / Room)

### 1.1 Statische Expertentabellen (Read-Only, mit der App ausgeliefert)

| Tabelle | Felder | Zweck |
| :--- | :--- | :--- |
| `categories` | id, name | Die 13 Basis-Kategorien (siehe [CONTENT_Zutaten.md](CONTENT_Zutaten.md)) |
| `properties` | id, name, group | Trigger-Eigenschaften (Histamin, Gluten, Laktose, EU-14-Allergene …) |
| `property_windows` | property_id, start_min, end_min | Reaktions-Zeitfenster **pro Trigger** (dürfen sich überlappen) |
| `property_symptom_map` | property_id, symptom_group | Welche Symptomgruppen zu einem Trigger plausibel passen |
| `ingredients` | id, name, category_id, is_custom, base_ingredient_id | Vorbefüllte Zutatenliste (~100 Einträge zum Start) |
| `ingredient_properties` | ingredient_id, property_id | **Zutat → Trigger** (z.B. Spinat → Histamin) |
| `ingredient_synonyms` | ingredient_id, synonym | Für Voice- und Suchtreffer ("Semmel" = "Brötchen") |
| `symptoms` | id, name, group, is_red_flag | Symptom-Katalog (siehe [CONTENT_Symptome.md](CONTENT_Symptome.md)) |
| `lifestyle_factors` | id, name | Kandidaten außerhalb der Ernährung (Sport, Zyklus, Medikament). Schlechter Schlaf und Stress kommen aus `day_check`. Alkohol ist **kein** Faktor, sondern Trigger-Eigenschaft der Getränke. |

Die Anzeige-Labels T1/T2/T3 bleiben für die UI erhalten:
* **T1 Sofort:** Fenster endet ≤ 2h
* **T2 Intermediär:** Fenster endet ≤ 8h
* **T3 Spät:** Fenster endet > 8h

Die vorläufigen Fenster pro Trigger stehen in [CONTENT_Zutaten.md](CONTENT_Zutaten.md), Kapitel 2.

### 1.2 Benutzer-Stammdaten
* **`user_profile`**: Onboarding-Daten.
* **`user_known_allergies`** (property_id): Bekannte Allergien. **Nicht** Teil des Rankings (siehe 2.6).
* **Eigene Zutaten** landen in `ingredients` mit `is_custom = true`. Beim Anlegen wählt der User Kategorie und "ähnlich wie …" (`base_ingredient_id`). Die Trigger-Tags der Basis-Zutat werden vorgeschlagen und können angepasst werden.

### 1.3 Logbuch-Daten

| Tabelle | Felder |
| :--- | :--- |
| `meals` | id, eaten_at (UTC epoch), tz_offset_min, favorite_id (nullable) |
| `meal_ingredients` | meal_id, ingredient_id, portion (S/M/L) |
| `favorites` | id, name |
| `favorite_ingredients` | favorite_id, ingredient_id, default_portion |
| `symptom_entries` | id, symptom_id, started_at (UTC epoch), tz_offset_min, intensity (1–5), note (nullable) |
| `day_check` | date, wellbeing (1–5), sleep_hours, sleep_quality (1–5), stress (1–5), status |
| `day_factors` | date, factor_id |

* **Favoriten werden beim Loggen als Snapshot kopiert:** Die Zutaten wandern nach `meal_ingredients`. Spätere Rezept-Änderungen verändern die Vergangenheit nicht.
* **`day_check.status`**:
    * `NO_SYMPTOMS`: Tag bestätigt, keine Beschwerden
    * `COMPLETE`: Tag bestätigt, alle Beschwerden eingetragen
    * `OPEN`: nicht bestätigt (Default)
* Die Notiz in `symptom_entries` fließt **nicht** in die Analyse ein, erscheint aber im PDF-Export.

### 1.4 Portionsgröße
Die Portion wirkt in der Analyse als **relativer Faktor**. Gramm-Angaben dienen nur der Orientierung im UI.

| Portion | Faktor | Orientierung (Hand-Maß) | ca. Gramm (feste Speisen) | Getränke |
| :--- | :--- | :--- | :--- | :--- |
| **S** – klein | 0,5 | ein paar Bissen / eine halbe Handvoll / daumengroßes Stück (Käse, Schokolade) | ≈ 30–80 g | Schluck bis ½ Glas (≈ 100 ml) |
| **M** – normal (Default) | 1,0 | eine Faust bzw. eine Handfläche (Fleisch, Fisch) | ≈ 100–200 g | 1 Glas / 1 Tasse (≈ 200–250 ml) |
| **L** – groß | 2,0 | zwei Fäuste / Nachschlag / ganzer Teller | ≈ 250 g und mehr | 2 Gläser / großes Bier (≥ 0,5 l) |

---

## 2. Der Analyse-Algorithmus ("Detektiv-Logic")

### 2.1 Zeitpunkt der Berechnung
Einmal täglich (WorkManager, nachts) und beim Öffnen des Ergebnis-Screens. **Nicht** bei jedem Eintrag.

### 2.2 Vorverarbeitung
1. **Gültige Tage:** Nur Tage mit `day_check.status ∈ {NO_SYMPTOMS, COMPLETE}` gehen in die Analyse ein.
2. **Episoden bilden:** Einträge mit demselben Symptom **oder** derselben Symptomgruppe innerhalb von 3h werden zu **einer Episode** zusammengefasst: frühester Startzeitpunkt, maximale Intensität.
3. **Abgeschlossene Fenster:** Eine Mahlzeit zählt für einen Trigger erst, wenn dessen Fenster vollständig vergangen ist und alle betroffenen Tage gültig sind.

### 2.3 Treffer-Definition
Eine Mahlzeit *m* ist für einen Kandidaten *c* ein **Treffer**, wenn:
* eine Episode im Fenster `[eaten_at + start_min, eaten_at + end_min]` von *c* beginnt **und**
* die Symptomgruppe der Episode laut `property_symptom_map` zu *c* passt.

Für eine Zutat gilt das Fenster und die Plausibilität ihrer Trigger, also die Vereinigung aller ihrer Eigenschaften.

Gewicht eines Treffers: `w = intensity / 5`

### 2.4 Vergleich "mit vs. ohne" (Lift)
Für jeden Kandidaten *c* (Zutat **oder** Trigger-Eigenschaft):

* *n₁* = gewichtete Anzahl Mahlzeiten **mit** *c*. Jede Mahlzeit zählt mit ihrem Portionsfaktor (0,5 / 1 / 2).
* *k₁* = Summe der Treffer-Gewichte dieser Mahlzeiten, multipliziert mit dem Portionsfaktor.
* *n₀*, *k₀* = dasselbe für alle gültigen Mahlzeiten **ohne** *c*, geprüft mit dem Fenster von *c*.

$$Lift_c = \frac{(k_1 + 1)\,/\,(n_1 + 2)}{(k_0 + 1)\,/\,(n_0 + 2)}$$

Laplace-Glättung (+1/+2): Ein einzelnes Ereignis erzeugt keinen Extremwert.

### 2.5 Lifestyle-Faktoren als eigene Kandidaten
"Schlechter Schlaf" (`sleep_quality ≤ 2` **oder** `sleep_hours < 6`), "hoher Stress" (`stress ≥ 4`) und jeder Faktor aus `day_factors` werden **wie ein Kandidat** behandelt:
* **Exposition:** ein Tag mit diesem Faktor (Schlaf: der Tag nach der Nacht).
* **Treffer:** eine Episode an diesem Tag.
* **Lift:** Vergleich mit gültigen Tagen ohne den Faktor.

Lebensmittel und Lifestyle-Faktoren erscheinen **gemeinsam** im Ranking. Der pauschale Abzug von 30 % aus Version 1.3 entfällt.

### 2.6 Bekannte Allergien
Bekannte Allergien sind nicht Teil des Rankings. Isst der User eine Zutat mit bekannter Allergie, zeigt die App einen separaten Hinweis: *"Du hast X gegessen, das als bekannte Allergie hinterlegt ist."*

### 2.7 Nicht unterscheidbare Kandidaten
Treten zwei Kandidaten in ≥ 80 % ihrer Expositionen gemeinsam auf, werden sie als **Gruppe** angezeigt (*"Weizen + Käse – bisher nicht unterscheidbar"*).

### 2.8 Evidenzstufen (statt Wahrscheinlichkeit)

| Stufe | Regel |
| :--- | :--- |
| – zu wenig Daten | n₁ < 3 |
| 🔍 Spur | Lift ≥ 1,5 und n₁ ≥ 3 |
| 🔎 Verdacht | Lift ≥ 2 und k₁ ≥ 3 |
| 🕵️ Starker Verdacht | Lift ≥ 2,5, k₁ ≥ 4, n₁ ≥ 6 und Verdacht seit ≥ 14 Tagen stabil |

### 2.9 Sicher-Liste
Eine Zutat gilt als **"gut vertragen"**, wenn n₁ ≥ 3 (ungewichtet) gilt und keine plausible Episode in ihren Fenstern auftrat (k₁ = 0). Gezählt werden nur gültige Tage.

> Alle Schwellenwerte (3h, 80 %, Lift-Grenzen) sind Startwerte und werden mit Testdaten und im Self-Test kalibriert.

---

## 3. Funktionale Anforderungen

### 3.1 Onboarding & Setup
* Abfrage bekannter Allergien, als Auswahl aus `properties`.
* Hinweis auf die Zweckbestimmung: Selbstbeobachtung, keine Diagnose.

### 3.2 Dateneingabe
* **Favoriten-Auflösung:** Ein Favorit wird als Snapshot in `meal_ingredients` geschrieben.
* **Voice-to-Text:** System-SpeechRecognizer mit `EXTRA_PREFER_OFFLINE`. Fallback ist immer die Tastatur.
* **Matching:** Text normalisieren (Kleinschreibung, Umlaute, Plural, zusammengesetzte Wörter wie "Spinatpizza" → "spinat" + "pizza"), Suche in Favoriten → `ingredient_synonyms` → `ingredients`, Fuzzy-Match mit Levenshtein-Distanz ≤ 2.
* **Zeit nachtragen:** Für Mahlzeiten **und** Symptome per Zeit-Slider.

### 3.3 Tages-Abschluss
Täglicher Check (Abend-Reminder ca. 20:00) mit Wohlbefinden, Schlaf der letzten Nacht, Stress, optionalen Tages-Faktoren und dem Status (siehe 1.3). Nicht bestätigte Tage werden am nächsten Tag zum Nachtragen angeboten, bis zu 2 Tage rückwirkend.

### 3.4 Notfall-Symptome
Symptome mit `is_red_flag = true` (Atemnot, Schwellung) zeigen **vor** dem Speichern einen Hinweis-Screen mit Notruf-Button (112). Speichern bleibt möglich.

### 3.5 Datenschutz & Security
* Vollständige lokale Datenhaltung, kein Tracking von Gesundheitsdaten.
* Android Auto-Backup deaktivieren (`allowBackup="false"` / `dataExtractionRules`), damit keine Daten in die Google-Cloud gelangen.
* In-App-Purchase über Google Play Billing, Prüfung lokal.
* **Verschlüsselung (SQLCipher), verschlüsselter Export/Import und App-Sperre:** Entscheidung später (siehe [OFFENE_PUNKTE.md](OFFENE_PUNKTE.md)).

---

## 4. Technische Meilensteine
1. **Datenbank-Setup:** Room-Schema, statische Tabellen und Import der Zutaten-Startliste.
2. **Analyse-Engine:** Episoden, Fenster, Lift, Evidenzstufen. Unit-Tests mit synthetischen Testdaten, z.B. "Laktose verursacht Blähbauch nach 1–3h" muss erkannt werden, "täglich Brot ohne Beschwerden" darf nicht verdächtig werden.
3. **Logbuch-UI:** Essen, Symptom, Tages-Abschluss.
4. **Ergebnis-Screen & PDF-Export.**
5. **Premium-Layer:** Bezahlsystem für fortlaufende Analysen.
