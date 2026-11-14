# Product Requirements Document (PRD): Allergie-Detektiv App

**Projekt-Name:** Allergie-Detektiv (Phasen-Modell 1 & 2)
**Version:** 2.1 (Update: Fokus Vermarktung)
**Strategie:** Local-First (Datenschutz), Mathematische Korrelation (Keine KI-Kosten), Faktor-Bereinigung (Schlaf), Vertrauensbasiertes Wachstum.

---

## 1. Produkt-Vision
Eine hochdiskrete Tagebuch-App, die ohne Cloud-KI auskommt. Sie identifiziert versteckte Ursachen für Allergien und Unverträglichkeiten durch mathematische Verknüpfung von Ernährung, Symptomen und Schlafqualität. Der Fokus liegt auf einem schnellen "Aha-Erlebnis" für den Nutzer, um Vertrauen für ein langfristiges Abo zu schaffen.

---

## 2. Kernfunktionen (Features)

### 2.1 Intelligentes Ernährungs-Logbuch
* **Favoriten-System (Wort-Speicher):**
    * User speichern komplexe Mahlzeiten (z.B. "Pizza Margarita").
    * Hinterlegung von Einzelzutaten pro Favorit (z.B. Weißmehl, Käse, Tomate).
    * Ein-Klick-Loggen zerlegt die Mahlzeit automatisch in Einzelkomponenten für die Analyse.
* **Sprachsteuerung (Free Voice-In):** Nutzung der systemeigenen, kostenlosen Speech-to-Text-Funktion.
* **Automatischer Zeitstempel:** Präzise Erfassung der Verzehrzeit für die Zeitfenster-Analyse.

### 2.2 Symptom- & Intensitäts-Tracking
* **Top 20 Symptom-Datenbank:** Strukturierte Auswahl (Haut, Magen-Darm, Atemwege, Kopf). Details: [CONTENT_Symptome.md](CONTENT_Symptome.md)
* **Intensitätsskala:** Schieberegler von 1 bis 10.
* **Isoliertes Logging:** User trägt Symptome ein, wenn sie auftreten. Die App übernimmt die Korrelation zur Vergangenheit (keine Kausalitäts-Pflicht für den User).

### 2.3 Störfaktor-Bereinigung: Schlaf-Qualität
* **Morgendliche Abfrage:** Kurzer Check von Schlafdauer und -qualität (Smileys/Stunden).
* **Noise-Filtering:** Der Algorithmus erkennt Korrelationen zwischen Schlafmangel und Symptomen (z.B. Kopfschmerz) und senkt in diesen Fällen die "Schuld-Gewichtung" für Lebensmittel.

### 2.4 Die Korrelations-Engine (Berechnungs-Logik)
* **Mathematisches Modell:** Statistische Häufigkeitsanalyse (Rolling Window 0–48h).
* **Zeitfenster-Logik:**
    * *Soforttyp (0–2h):* Klassische Allergien.
    * *Spättyp (2–24h):* Unverträglichkeiten (Laktose, Histamin, etc.).
    * *Langzeittyp (bis 48h):* Entzündliche Reaktionen (z.B. Gluten/Haut).
* **Ausschluss-Verfahren:** Zutaten ohne negative Folgereaktionen werden als "Sicher" eingestuft.

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
