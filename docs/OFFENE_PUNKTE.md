# Offene Punkte & Widersprüche zwischen den Dokumenten

Beim Zusammenführen der Dokumente aufgefallen. Vor dem Programmieren entscheiden, sonst baut die KI je nach Dokument unterschiedliche Logik.

## Widersprüche
- [ ] **Intensitäts-Skala:** PRD, TRD und UX/UI sagen **1–10** (Slider), der Symptom-Katalog sagt **1–5** (Punkte-Schnellauswahl, Default 3). → Eine Skala festlegen.
- [ ] **Zeitfenster T2/T3:** TRD: T2 = 2–6h, T3 = 6–48h. PRD: Spättyp = 2–24h, Langzeittyp = bis 48h. → TRD als Referenz nehmen und PRD angleichen?
- [ ] **Stress-Eingabe:** UX/UI beschreibt einen stufenlosen Farb-Slider, TRD speichert `Stress_Level` 1–5. → Slider auf 5 Stufen rastern?
- [ ] **NoiseFilter `Sleep < 3`:** Unklar, ob `Sleep_Hours` (dann wären 3 Stunden sehr wenig) oder `Sleep_Quality` (1–5) gemeint ist.
- [ ] **Verschlüsselung:** Store-Text verspricht "verschlüsselt auf deinem Gerät", TRD sieht keine Verschlüsselung vor (z.B. SQLCipher). → Entweder umsetzen oder Store-Text anpassen.
- [ ] **Schlaf-Abfrage:** PRD: "morgendliche Abfrage", UX/UI: kein Follow-up, nur ein Abend-Reminder um 20:00. → Klären, ob es einen Morgen-Prompt gibt.

## Unvollständig
- [ ] **Known_Allergy:** "Erhöhe S" – um welchen Faktor?
- [ ] **Sicher-Bonus:** Höhe des Bonus und Definition "gute Lifestyle-Werte" fehlen.
- [ ] **12 Basis-Kategorien** für Zutaten (TRD 3.1) sind nirgends aufgelistet.
- [ ] **`internal_properties`:** Nur 101–103 definiert, Rest ("etc.") fehlt.
- [ ] **Symptom-Anzahl:** PRD spricht von "Top 20", Katalog enthält 19 Symptome.
- [ ] **Motivations-Sprüche:** Pool mit 30–50 Sätzen existiert noch nicht.
- [ ] **Abgeschnittene Texte:** PRD Phase 2 ("Review-Mining: Automatisi…") und UX/UI Kapitel 6 ("…mit einer kurzen Animatio…") sind im Original abgebrochen.
- [ ] **Plattform:** TRD erwähnt iOS/Android, Workflow ist Android Studio + Kotlin/Compose. → Für MVP nur Android?
