# Review: Schwachstellen, Optimierungen & Ideen

**Stand:** 14.11.2026 · **Basis:** PRD 2.1, TRD 1.3, UX/UI 1.2, Symptom-Katalog 1.0
**Kurzfazit:** Das Konzept ist stark bei Datenschutz, Low-Friction-UX und Monetarisierung. Die größten Schwachstellen liegen im Kern: im **Algorithmus**, weil häufig gegessene Lebensmittel immer verdächtig wirken, und im **Datenmodell**, weil die Zuordnung von Zutat zu Trigger fehlt. Dazu kommen **Sicherheit und Medizinprodukt-Risiko**. Diese drei Punkte sollten vor der ersten Zeile Code geklärt sein.

Prioritäten: 🔴 kritisch (vor dem MVP) · 🟠 wichtig (im MVP) · 🟢 Idee (später)

---

## 1. Die 7 wichtigsten Befunde auf einen Blick

| # | Befund | Prio |
| :--- | :--- | :--- |
| 1 | Score-Formel bevorzugt Lebensmittel, die oft gegessen werden (Brot, Kaffee, Milch). Ein Vergleich mit einer Basisrate fehlt. | 🔴 |
| 2 | Es fehlt die Tabelle **Zutat → Trigger-Eigenschaft**. Ohne sie lässt sich der `TimeFactor` nicht berechnen. | 🔴 |
| 3 | Es fehlt die Tabelle **Symptom → Reaktionstyp**. Damit ist "passt zum Reaktionstyp des Symptoms" nicht definiert. | 🔴 |
| 4 | Keine "Heute keine Beschwerden"-Eingabe. Ein Tag ohne Eintrag ist nicht dasselbe wie ein Tag ohne Symptom. Die Sicher-Liste wird dadurch unzuverlässig. | 🔴 |
| 5 | Notfall-Symptome (Atemnot, Schwellung) werden wie normale Einträge behandelt. Ein Hinweis auf den Notruf fehlt. | 🔴 |
| 6 | Wortwahl ("identifiziert Ursachen", "Wahrscheinlichkeit") kann die App zum Medizinprodukt machen. | 🔴 |
| 7 | 7 Tage reichen statistisch kaum für T3-Auslöser. Die Ergebnisdarstellung verspricht mehr, als die Daten tragen. | 🟠 |

---

## 2. Fachliche Logik (Zeitfenster & Trigger)

### 2.1 Zeitfenster prüfen 🟠
Einige Zuordnungen im TRD weichen von dem ab, was gängig beschrieben wird. **Bitte mit Fachliteratur gegenprüfen.** Die Werte sind Design-Annahmen, keine medizinischen Fakten.

| Trigger | TRD heute | Üblicher beschriebener Verlauf | Vorschlag |
| :--- | :--- | :--- | :--- |
| Histamin | T3 (6–48h) | meist Minuten bis wenige Stunden | **T1/T2** |
| Laktose / Fruktose | T2 (2–6h) | oft schon ab ca. 30 Min | **0,5–6h** |
| Gluten | T3 | Stunden bis Tage | T3 ✔ |
| Nüsse, Obst, Pollen-Kreuzreaktion | T1 | Minuten | T1 ✔ |

**Empfehlung:** Die Fenster dürfen sich **überlappen**. Jeder Trigger bekommt ein eigenes Fenster (`window_start_min`, `window_end_min`), keine starre Dreiteilung. Die Labels T1–T3 bleiben nur für die Anzeige.

### 2.2 Dosis fehlt 🟠
Laktose und Histamin wirken **dosisabhängig**. Bei Histamin sammelt sich die Last über den Tag an ("Fass-Modell"). Eine kleine Portion Käse ist etwas anderes als eine Käseplatte.
→ **Portionsgröße S/M/L** als optionaler Ein-Tap-Chip, Default M. Kostet kaum Zeit, verbessert die Analyse stark.

### 2.3 Auslöser außerhalb der Ernährung 🟠
Das Lifestyle-Log enthält `Exercise`, der Algorithmus nutzt den Wert aber nie. Häufige Störfaktoren fehlen ganz:
- **Alkohol** (wichtig bei Histamin)
- **Medikamente** (z.B. Schmerzmittel), nur als neutraler Tag ohne Wirkstoff-Datenbank
- **Zyklus / Menstruation**
- **Sport kurz nach dem Essen**
- **Pollenflug / Jahreszeit**

→ Als optionale **Tages-Tags** (Chips) im Lifestyle-Check. Sie gehen wie Lebensmittel als **Kandidaten** in die Analyse ein (siehe 3.3).

### 2.4 Symptom-Katalog ergänzen 🟠
| Neu | Warum |
| :--- | :--- |
| **Kribbeln Mund/Lippen** | Typisches Leitsymptom der Pollen-Kreuzallergie (T1) |
| **Augen jucken/tränen** | Atemwege/Allergie |
| **Herzrasen** | Häufig bei Histamin genannt |
| **Durchfall** und **Verstopfung** getrennt | "Stuhlveränderung" ist für die Analyse zu grob |
| **Sonstiges** (+ optionale Notiz) | Das Verbot von Freitext darf keine Daten verschlucken. Die Notiz fließt nicht in die Analyse ein, erscheint aber im PDF für den Arzt. |

Damit kommst du auch auf die versprochenen "Top 20" (aktuell 19).

### 2.5 Sicherheit: Notfall-Symptome 🔴
Bei **Atemnot (4004)** und **Schwellungen (2004)** im Gesicht, Mund oder Rachen darf die App nicht einfach "gespeichert ✓" anzeigen. Sie braucht einen deutlichen Hinweis:
> "Atemnot oder Schwellung im Mund/Rachen können ein Notfall sein. Ruf den Notruf (112)."

Das schützt Nutzer und dich (Haftung, Store-Review).

---

## 3. Algorithmus: Vorschlag für eine robustere "Detektiv-Logic"

### 3.1 Das Kernproblem der aktuellen Formel 🔴
`S = Intensity × TimeFactor × NoiseFilter` summiert **jedes** Vorkommen. Wer jeden Tag Brot isst, hat Weizen im Fenster jedes Symptoms, und Weizen landet immer auf Platz 1. Das ist ein **Basisraten-Fehler**. Die Formel zeigt dann, was man oft isst, nicht was schadet.

Weitere Probleme:
- **Überlappende Mahlzeiten:** Ein Symptom gibt allen 15 Zutaten der letzten 48h die volle Punktzahl.
- **Doppelzählung:** Wer "Blähbauch" dreimal in zwei Stunden einträgt, verdreifacht den Score.
- **Bekannte Allergie erhöht S:** Das ist ein Bestätigungsfehler. Bekannte Allergene gehören separat angezeigt ("Du hast X gegessen, das als bekannte Allergie hinterlegt ist"), nicht ins Ranking.
- **Stress-Abzug von pauschal 30 %:** Das ist willkürlich und verschleiert eine echte Erkenntnis ("Deine Kopfschmerzen hängen mit Schlaf zusammen, nicht mit Essen").

### 3.2 Neuer Ansatz: Vergleich "mit vs. ohne" (Lift) 🔴
Einheit ist die **Mahlzeit als Exposition**. Für jeden Kandidaten *c*, also eine Zutat oder eine Trigger-Eigenschaft:

1. **Exponierte Mahlzeiten:** alle Mahlzeiten, die *c* enthalten → davon *k₁* mit passendem Symptom im Fenster W(c), von insgesamt *n₁*
2. **Nicht exponierte Mahlzeiten:** alle anderen → *k₀* von *n₀*
3. Nur Mahlzeiten zählen, deren Fenster **abgeschlossen** ist und deren Tage als vollständig gelten (siehe 4.1).

$$Lift_c = \frac{(k_1 + 1)\,/\,(n_1 + 2)}{(k_0 + 1)\,/\,(n_0 + 2)}$$

Das `+1/+2` ist eine Laplace-Glättung. Sie verhindert, dass ein einziges Ereignis sofort 100 % ergibt.

- **Intensität** gewichtet das Ergebnis: Ein Treffer zählt `intensity / 5` statt 1.
- **Passendes Symptom** bedeutet, dass das Symptom laut `symptom_reaction_map` zum Reaktionstyp von *c* passt.
- **Mindestdaten:** Kandidaten mit *n₁* < 3 werden nicht gerankt, sondern als "zu wenig Daten" angezeigt.

### 3.3 Schlaf, Stress & Co. als eigene Verdächtige 🟠
Statt den Score pauschal zu kürzen, behandelst du "schlechter Schlaf", "hoher Stress", "Alkohol" usw. **genauso wie ein Lebensmittel**. Als Exposition zählt der Tag mit diesem Faktor. Dann kann die Analyse sagen:
> "Platz 1: Schlechter Schlaf (Kopfschmerzen). Platz 2: Rotwein."

Das ist ehrlicher und ein echter **Aha-Moment**, den Konkurrenten nicht bieten.

### 3.4 Nicht unterscheidbare Zutaten 🟠
Teig, Käse und Tomate kommen bei Pizza immer zusammen vor und lassen sich statistisch nicht trennen.
→ Treten zwei Kandidaten in ≥ 80 % der Expositionen gemeinsam auf, werden sie als **Gruppe** angezeigt ("Weizen + Käse – nicht unterscheidbar"). Die App schlägt dann ein **Experiment** vor (siehe 7.1).

### 3.5 Ergebnis ehrlich darstellen 🟠
"Wahrscheinlichkeitsbalken" suggerieren eine Diagnose. Besser ist eine **Evidenzstärke**:

| Stufe | Regel (Vorschlag) |
| :--- | :--- |
| 🔍 Spur | Lift ≥ 1,5 und n₁ ≥ 3 |
| 🔎 Verdacht | Lift ≥ 2 und k₁ ≥ 3 |
| 🕵️ Starker Verdacht | Lift ≥ 2,5, k₁ ≥ 4, n₁ ≥ 6 und über zwei Wochen stabil |

Nach 7 Tagen sind realistisch meist "Spuren" möglich. Das passt gut zum Abo: **"Woche 2 bestätigt oder entlastet deine Verdächtigen."**

### 3.6 Episoden statt Einzeleinträge 🟠
Gleiche Symptome derselben Gruppe innerhalb von 3h werden zu **einer Episode** zusammengefasst, mit maximaler Intensität und frühestem Startzeitpunkt. Das verhindert Doppelzählung.

### 3.7 Wann gerechnet wird 🟢
Nicht bei jedem Symptom-Eintrag, sondern **einmal täglich bzw. beim Öffnen des Ergebnis-Screens** (WorkManager). Das ist einfacher, gut testbar und spart Akku.

---

## 4. Datenmodell

### 4.1 Negative Daten erfassen 🔴
Der wichtigste fehlende Baustein. Der Abend-Reminder bekommt einen Ein-Tap-Button:
> **"✅ Heute alles notiert"** oder **"😊 Heute keine Beschwerden"**

Neue Tabelle `day_status (date, completed, no_symptoms)`. Erst damit werden die Sicher-Liste und die Kontrollgruppe im Lift (*n₀*) belastbar. Tage ohne Status zählen nicht mit.

### 4.2 Fehlende Tabellen 🔴
```text
ingredients            (id, name, category_id, is_custom)
ingredient_properties  (ingredient_id, property_id)        ← Zutat → Trigger
symptom_reaction_map   (symptom_id, reaction_type_id)      ← Symptom → T1/T2/T3
property_windows       (property_id, start_min, end_min)   ← ersetzt starre T-Fenster
meal_ingredients       (meal_id, ingredient_id, portion)   ← statt Liste in meals_log
favorite_ingredients   (favorite_id, ingredient_id)
day_tags               (date, tag_id)                      ← Alkohol, Zyklus, Medikament …
day_status             (date, completed, no_symptoms)
```
- Eine **Liste von IDs in einer Spalte** (`meals_log.ingredient_ids`) bricht die Normalisierung. Das wird in Room mühsam zu abfragen, deshalb die Verknüpfungstabelle `meal_ingredients`.
- **Favoriten als Snapshot** loggen: Ändert der User später das Rezept, darf sich die Vergangenheit nicht ändern. Deshalb beim Loggen die Zutaten nach `meal_ingredients` kopieren. Das TRD beschreibt das schon richtig, jetzt ist es auch im Schema verankert.
- **Zeit:** UTC-Epoch plus Zeitzonen-Offset speichern, wichtig bei Reisen und Zeitumstellung.

### 4.3 Vorbefüllte Zutaten-Datenbank 🔴
Wenn der User jede Zutat selbst anlegt und kategorisiert, weiß die App nicht, dass "Spinat" histaminreich ist. Die Experten-Logik läuft dann ins Leere.
→ Eine **kuratierte Liste mit ca. 200–400 Alltags-Zutaten** mit Trigger-Tags mitliefern, offline. Eigene Zutaten wählen beim Anlegen eine ähnliche Basis-Zutat ("ähnlich wie: Hartkäse") und erben deren Tags.
**Achtung Lizenz:** Externe Datenbanken (z.B. Bundeslebensmittelschlüssel, Open Food Facts/ODbL) haben eigene Lizenzbedingungen. Für den MVP ist eine eigene, kleine Liste am sichersten.

### 4.4 Entscheidungen zu den Widersprüchen (Vorschlag)
| Thema | Vorschlag | Begründung |
| :--- | :--- | :--- |
| Intensität | **1–5**, Default 3 | Passt zur 5-Sekunden-Regel, schnelles Tippen statt Slider |
| Stress | **1–5**, Slider rastet ein | Konsistent zum Datenmodell |
| `Sleep < 3` | → `Sleep_Quality ≤ 2` **oder** `Sleep_Hours < 6` | Eindeutig |
| Plattform MVP | **nur Android** | Passt zu deinem Workflow |
| Schlaf-Abfrage | Im **Abend-Reminder** für die letzte Nacht | Kein zusätzlicher Push nötig |

---

## 5. Datenschutz & Technik

### 5.1 Versprechen technisch einlösen 🔴
- **"Verschlüsselt"** (Store-Text) → **SQLCipher für Room** umsetzen oder das Wort streichen.
- **Android Auto-Backup:** Standardmäßig sichert Android App-Daten in Google Drive. Das widerspricht "keine Cloud". → `android:allowBackup="false"` bzw. `dataExtractionRules` setzen.
- **Datenverlust-Risiko:** Ohne Cloud sind bei Handyverlust alle Daten weg. → **Verschlüsselter Export/Import** als Datei mit Passwort, vom User gesteuert. Das ist Datenschutz und Komfort zugleich.
- Optional: **App-Sperre** per Biometrie.

### 5.2 Spracheingabe realistisch planen 🟠
- Offline-Erkennung ist **geräteabhängig**. Das Sprachpaket muss installiert sein (`EXTRA_PREFER_OFFLINE`). Ein Fallback auf Tippen muss immer möglich sein.
- **Fuzzy-Matching** einplanen: "Spinatpizza" vs. "Spinat Pizza", Tippfehler, Mehrzahl. Ein einfacher Normalisierer (Kleinschreibung, Komposita splitten, Levenshtein ≤ 2) plus **Synonymliste** ("Brötchen" = "Semmel" = "Weckle").

### 5.3 In-App-Kauf 🟢
Lokale Receipt-Prüfung ist leicht zu umgehen. Für ein Kleinst-Abo ist das ein vertretbares Risiko. Es sollte aber eine bewusste Entscheidung sein. Die Google Play Billing Library liefert den Kaufstatus auch ohne eigenen Server.

---

## 6. UX/UI

### 6.1 Die eigentliche Hürde ist das Essen-Loggen 🔴
Symptome sind schnell erfasst. **Mahlzeiten** sind die Stelle, an der Nutzer abspringen. Ideen:
- **"Wie gestern?"** – zuletzt gegessene Mahlzeiten als Top-Chips
- **Tageszeit-Vorschlag:** Morgens erscheinen Frühstücks-Favoriten zuerst
- **Zeit-Slider auch für Mahlzeiten**, nicht nur für Symptome. Nachtragen ist die Regel, nicht die Ausnahme.
- **Schnell-Zutaten:** Neue Mahlzeit = Name + 3–5 Zutaten-Chips, keine Pflichtfelder

### 6.2 Farben & Barrierefreiheit 🟠
Kontrast auf Weiß (WCAG verlangt ≥ 4,5 : 1 für normalen Text):

| Farbe | Kontrast | Bewertung | Text-Variante |
| :--- | :--- | :--- | :--- |
| Petrol `#3A7D8E` | 4,66 | ✔ knapp | `#2E6371` (6,69) für kleine Schrift |
| Salbei `#84A59D` | 2,68 | ✘ nur für Flächen | `#4F6F67` (5,52) |
| Koralle `#F28482` | 2,50 | ✘ nur für Flächen | `#B5453F` (5,40) |

→ Pastellfarben für Hintergründe und Kacheln, die dunklen Varianten für Text und Icons. **Dark Mode** von Anfang an mitdenken (Material 3 Dynamic Color optional).

### 6.3 Interaktion 🟠
- **Vertikaler Slider** im Overlay kollidiert mit Scroll-Gesten. Besser sind **5 große Tap-Punkte**, wie im Symptom-Wireframe.
- **Doppelklick für One-Tap-Logging** ist auf Mobilgeräten schwer zu entdecken. Besser **Long-Press = Schnell-Log** mit kurzer Snackbar "Gespeichert · Rückgängig".
- **Rückgängig statt Bestätigungsdialog** bei jeder Speicherung.

### 6.4 Motivation ohne Druck 🟢
- Ein **Belohnung nach 5 Tagen** und eine **Analyse nach 7 Tagen** sind zwei Ziele für denselben Zeitraum. Ein einziger Fortschritt ist klarer: Die Analyse ist das Ziel, Zwischenschritte heißen "Spuren gesammelt".
- **Kein Streak-Verlust bestrafen.** Ein verpasster Tag verlängert die Sammelphase, statt sie zurückzusetzen.
- **Daily Quotes:** Die 50 Sätze sollten nicht esoterisch oder heilsversprechend klingen (Store-Review, Glaubwürdigkeit).

### 6.5 Ergebnis-Screen 🟠
Jeder Verdächtige bekommt:
1. Evidenzstärke (siehe 3.5)
2. Begründung in einem Satz ("4 von 5 Mal nach Käse Blähbauch, sonst 1 von 10")
3. **Nächster Schritt:** "Experiment starten" oder "Mit Arzt besprechen (PDF)"

---

## 7. Ideen für Mehrwert & Premium

### 7.1 🟢⭐ Detektiv-Experimente (stärkstes Premium-Feature)
Korrelation ist nur ein Verdacht. Die App kann aber einen **strukturierten Selbsttest** begleiten:
1. **Weglassen:** 2 Wochen ohne Verdächtigen X, die App trackt weiter
2. **Wiedereinführen:** gezielt wieder essen, die App beobachtet das passende Fenster
3. **Ergebnis:** "Bestätigt / entlastet"

Damit wird aus "Tagebuch" ein **Werkzeug mit klarem Ergebnis**. Das ist ein überzeugender Grund für das Abo. Hinweis in der App: bei bekannten oder schweren Allergien nur nach ärztlicher Rücksprache, und nie bei Notfall-Symptomen in der Vorgeschichte.

### 7.2 🟢 Arzt-PDF als Kernfeature
Zeitstrahl (Mahlzeiten, Symptome, Schlaf), Top-Verdächtige mit Zahlen, Notizen. Ärzte schätzen strukturierte Protokolle. Das ist auch gutes Marketing ("von Patienten mitgebracht").

### 7.3 🟢 Wochen-Rückblick
Jeden Sonntag eine kurze Zusammenfassung in der App (kein Push): neue Spuren, entlastete Lebensmittel, wachsende Sicher-Liste. Das hält Nutzer im Abo.

### 7.4 🟢 Sicher-Liste als positives Feature
"Diese 23 Lebensmittel verträgst du" ist für viele Betroffene mindestens so wertvoll wie die Verdächtigen und motiviert stärker.

---

## 8. Produkt, Recht & Positionierung

### 8.1 Medizinprodukt-Risiko 🔴
*(Keine Rechtsberatung, aber für das Design relevant.)* Software, die Gesundheitsdaten auswertet, um **Ursachen von Beschwerden zu identifizieren**, kann in der EU unter die **Medizinprodukte-Verordnung (MDR)** fallen. Entscheidend ist die **Zweckbestimmung**, also wie du die App beschreibst. Ein Disclaimer allein reicht nicht.
- Kritisch im PRD: *"identifiziert versteckte Ursachen für Allergien"*, *"Wahrscheinlichkeitsbalken"*, *"Top-Verdächtige"* im Sinne einer Diagnose
- Weniger kritisch: *"Tagebuch zur Selbstbeobachtung"*, *"zeigt Muster in deinen eigenen Einträgen"*, *"Grundlage für das Arztgespräch"*

Der Store-Text ist hier schon vorsichtiger formuliert als das PRD. → **PRD-Vision und In-App-Texte an den Store-Text angleichen.** Vor dem Launch kurz fachlich prüfen lassen.

### 8.2 Name 🟢
"Allergie" verspricht Allergie-Erkennung, und echte Allergien gehören zum Arzt. Die meisten Nutzer haben eher **Unverträglichkeiten oder Bauchbeschwerden**. Gute ASO-Keywords dafür hast du ja schon: "Bauchschmerzen Ursache", "Histamin-Check". Mögliche Richtungen: **"Bauch-Detektiv"**, **"Spurensucher – Ernährungstagebuch"**, oder "Detektiv" als Marke mit dem Untertitel "Ernährungs- & Symptomtagebuch". Kein Muss, aber jetzt ist der günstigste Zeitpunkt für eine Änderung.

### 8.3 Lücken im PRD 🟠
- **Zielgruppe/Persona** fehlt (z.B. "Frau, 25–45, Reizdarm-Verdacht, war schon beim Arzt ohne klares Ergebnis")
- **Erfolgskennzahlen:** z.B. % der Nutzer, die 7 Tage durchhalten (wichtigste Kennzahl!), Abo-Conversion nach der ersten Analyse
- **Nicht-Ziele / MVP-Grenzen:** was bewusst *nicht* gebaut wird (iOS, Barcode-Scan, Cloud-Sync …)
- **Akzeptanzkriterien** pro Feature, damit Gemini prüfbare Vorgaben bekommt

---

## 9. Empfohlene Reihenfolge

1. 🔴 Entscheidungen aus 4.4 treffen, dann `OFFENE_PUNKTE.md` abhaken
2. 🔴 TRD überarbeiten: neue Tabellen (4.2), Fenster pro Trigger (2.1), Lift-Algorithmus (3.2), `day_status` (4.1)
3. 🔴 Zutaten-Startliste mit Trigger-Tags erstellen (4.3)
4. 🔴 PRD-Wortwahl an die Zweckbestimmung "Selbstbeobachtung" anpassen (8.1), Notfall-Hinweis aufnehmen (2.5)
5. 🟠 UX anpassen: Essen-Loggen (6.1), Farben (6.2), 1–5-Tap-Punkte (6.3)
6. Dann erst: Phase 1 der Roadmap (Room-Entities + Engine mit Testdaten)
7. 🟢 Nach dem Self-Test: Experimente (7.1) als Premium-Kern
