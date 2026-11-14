# Technical Requirements Document (TRD): Allergie-Detektiv

**Version:** 1.3 (Finales technisches Master-Framework)
**Architektur:** Local-First, SQLite-basiert, Expert-System Logic.
**Ziel:** Präzise Korrelation von Ernährung, Symptomen und Lifestyle-Störfaktoren ohne Cloud-Kosten.

---

## 1. Daten-Architektur (Backend-less / SQLite)

### 1.1 Statische Expertentabellen (Read-Only für User)
* **`reaction_types`**: Definiert Zeitfenster für Reaktionen.
    * T1 (Sofort): 0–2h (Nüsse, Obst, Pollen-Kreuz)
    * T2 (Intermediär): 2–6h (Laktose, Fruktose, Magen-Darm)
    * T3 (Spät-Typ): 6–48h (Gluten, Histamin, Haut-Reaktionen)
* **`internal_properties`**: Chemische Trigger-Tags (ID 101: Histamin, 102: Gluten, 103: Lactose, etc.).
* **`trigger_timing_map`**: Verknüpft Eigenschaften mit Reaktionstypen (z.B. Gluten -> T3).

### 1.2 Benutzer-Stammdaten
* **`user_profile`**: Enthält `known_allergies` (ID-Referenz auf `internal_properties`) und Onboarding-Daten.
* **`custom_ingredients`**: Vom User manuell angelegte Lebensmittel, verknüpft mit einer `category_id`.

### 1.3 Logbuch-Daten (Transaction Data)
* **`meals_log`**: ID, Timestamp, Favorite_ID (falls genutzt), Liste der `ingredient_ids`.
* **`symptoms_log`**: ID, Timestamp, Symptom_Type, Intensity (1–10). Symptom-IDs: siehe [CONTENT_Symptome.md](CONTENT_Symptome.md)
* **`lifestyle_log`**: Date, Sleep_Hours, Sleep_Quality (1–5), Stress_Level (1–5), Exercise (Boolean).

---

## 2. Der Analyse-Algorithmus ("Detektiv-Logic")

Der Algorithmus arbeitet in drei Schritten, sobald ein Symptom ($s$) eingetragen wird:

### Schritt 1: Das Rolling Window (Zeit-Vorselektion)
Die App scannt rückwärts:
* **Kurzzeit-Fenster (0–2h):** Fokus auf T1-Trigger.
* **Langzeit-Fenster (2–48h):** Fokus auf T2 und T3-Trigger.

### Schritt 2: Die Gewichtungs-Formel (Score $S$)
Der Verdachts-Score für jede Zutat ($i$) wird berechnet:

$$S_{i} = (Intensity \times TimeFactor) \times NoiseFilter$$

* **TimeFactor:** 1.0 (wenn Zutat zum Reaktionstyp des Symptoms passt), 0.3 (wenn unplausibel).
* **NoiseFilter (Störfaktor-Bereinigung):**
    * Wenn `Stress > 3` oder `Sleep < 3`: Reduziere $S$ um 30% (Symptom könnte stressbedingt sein).
    * Wenn `Known_Allergy = True`: Erhöhe $S$ (Bestätigung eines bekannten Musters).

### Schritt 3: Die Ausschluss-Logik (Sicher-Liste)
Zutaten, die konsumiert wurden, ohne dass in den folgenden 48h ein Symptom auftrat (bei gleichzeitig guten Lifestyle-Werten), erhalten einen "Sicher-Bonus" und sinken im Verdachts-Ranking.

---

## 3. Funktionale Anforderungen (Features)

### 3.1 Onboarding & Setup
* Initialer Check bekannter Allergien zur Kalibrierung des Algorithmus.
* Einfache Kategorisierung neuer Zutaten durch den User (Zuweisung zu den 12 Basis-Kategorien).

### 3.2 Dateneingabe (UX-Fokus)
* **Favoriten-Auflösung:** Beim Loggen eines Favoriten ("Pizza") schreibt die App die Einzel-Zutaten-IDs in den `meals_log`, um die Analyse auf Inhaltsstoff-Ebene zu ermöglichen.
* **Voice-to-Text:** Nutzung lokaler System-APIs (iOS/Android) zur Texteingabe.

### 3.3 Datenschutz & Security
* Vollständige lokale Datenhaltung.
* Kein Tracking von Gesundheitsdaten.
* In-App-Purchase Validierung erfolgt lokal über Store-Receipts.

---

## 4. Technische Meilensteine (Development)
1. **Datenbank-Setup:** Implementierung der SQLite-Struktur und der statischen Experten-Maps.
2. **Logbuch-UI:** Entwicklung der schnellen Eingabemasken (Essen, Symptom, Lifestyle).
3. **Analyse-Engine:** Implementierung der mathematischen Korrelations-Formel.
4. **Premium-Layer:** Integration des Bezahlsystems für fortlaufende Analysen.
