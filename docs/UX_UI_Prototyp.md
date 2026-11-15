# UX/UI Design Document: Allergie-Detektiv

**Version:** 1.3 (nach Review: Tages-Abschluss, schnelleres Essen-Loggen, Kontrast, Ergebnis-Screen)
**Leitprinzip:** "5-Sekunden-Regel" – Jede Eingabe muss blitzschnell und ohne Hürden funktionieren.
**Design-Stil:** Clean, modern, beruhigend (Pastell-Töne), weg vom medizinischen "Krankenhaus-Look".

---

## 1. Design-Sprache & Atmosphäre

### 1.1 Farbpalette
Pastellfarben für Flächen, dunklere Varianten für Text und Icons (WCAG-Kontrast ≥ 4,5 : 1 auf Weiß).

| Rolle | Fläche | Text / Icon | Kontrast Text auf Weiß |
| :--- | :--- | :--- | :--- |
| **Primär** – Soft Petrol (Ruhe, Vertrauen) | `#3A7D8E` | `#2E6371` | 6,7 : 1 |
| **Sekundär** – Salbeigrün ("gut vertragen", Erfolg) | `#84A59D` | `#4F6F67` | 5,5 : 1 |
| **Akzent** – Sanftes Koralle (Symptome) | `#F28482` | `#B5453F` | 5,4 : 1 |

* Weißer Text auf Petrol `#3A7D8E` ist ok (4,7 : 1). Auf Salbei und Koralle **dunklen Text** verwenden.
* **Dark Mode** von Anfang an mitdenken (Material 3).

### 1.2 Typografie & Icons
* Moderne Sans-Serif (z.B. *Inter*). Große Klickflächen (min. 48 dp).
* Abgerundete, freundliche Line-Icons.

---

## 2. Dashboard (Home Screen)

* **Header – Daily Quote:** Rotierender Satz aus einem lokalen Pool (ca. 50 Sätze), ermutigend, ohne Heilsversprechen (z.B. *"Jeder Eintrag ist ein Puzzleteil zu deinem Wohlbefinden."*).
* **Detektiv-Fortschritt:** Kreisförmiger Balken *"Tag 4 von 7 – Wir sammeln Spuren"*. Gezählt werden **bestätigte Tage** (Tages-Abschluss). Ein verpasster Tag verlängert die Sammelphase, statt sie zurückzusetzen.
* **Quick-Action-Kacheln:**
    1. 🍎 **Essen loggen**
    2. ⚡ **Symptom melden**
    3. 🌙 **Tages-Abschluss** (Schlaf, Stress, Wohlbefinden, "Heute keine Beschwerden")

---

## 3. User Flows

### 3.1 Essen loggen (die wichtigste Hürde)
```text
+---------------------------+
| ESSEN LOGGEN    [🎤]      |
| Wann? [ Jetzt ▾ ] -------|  <-- Zeit-Slider / Chips: Jetzt, -1h, -2h, Frühstück…
+---------------------------+
| WIE GESTERN?              |
| [Müsli] [Kaffee] [Brot]   |  <-- zuletzt gegessen, nach Tageszeit sortiert
| FAVORITEN                 |
| [Pizza] [Döner] [Salat]   |
| 🔍 Zutat suchen …         |
+---------------------------+
| Ausgewählt:               |
| Kaffee      (S)[M](L)     |  <-- Portion, Default M
| Brot        (S)[M](L)     |
|      [ SPEICHERN ]        |
+---------------------------+
```
* **Portion S / M / L** mit Hand-Maß-Erklärung beim ersten Mal (Info-Icon):
    * **S** = ein paar Bissen / halbe Handvoll / ½ Glas
    * **M** = eine Faust bzw. Handfläche / 1 Glas oder Tasse
    * **L** = zwei Fäuste / Nachschlag / 2 Gläser
    * Werte siehe [TRD](TRD_Technik.md), Kapitel 1.4
* **Neue Mahlzeit:** Name + 3–5 Zutaten-Chips aus der [Zutatenliste](CONTENT_Zutaten.md), optional "Als Favorit speichern". Keine Pflichtfelder außer einer Zutat.
* **Bekannte Allergie:** Enthält die Mahlzeit eine hinterlegte Allergie, erscheint ein dezenter Hinweis (kein Blockieren).

### 3.2 Voice-In (lokales Wort-Matching)
1. Mikrofon-Icon, z.B. *"Spinat Pizza und Kaffee"*.
2. System-Spracherkennung (bevorzugt offline) wandelt das in Text um.
3. Die App sucht Favoriten → Synonyme → Zutaten, auch unscharf (Tippfehler, "Spinatpizza").
4. **Ergebnis:** Treffer landen als Chips in "Ausgewählt" und lassen sich prüfen und korrigieren. Ohne Treffer: *"'Spinat Pizza' neu anlegen?"*.
5. Funktioniert die Offline-Erkennung auf dem Gerät nicht, wird das Mikrofon ausgeblendet. Suche per Tastatur funktioniert immer.

### 3.3 Symptom melden
Siehe [CONTENT_Symptome.md](CONTENT_Symptome.md): Grid → Overlay mit 5 Tap-Punkten und Zeit-Slider → Speichern mit Snackbar *"Gespeichert · Rückgängig"*. Long-Press auf eine Kachel = Schnell-Log. Bei Notfall-Symptomen erscheint vorher der Notruf-Hinweis.

### 3.4 Tages-Abschluss (ca. 10 Sekunden)
Ein einziger Screen, geöffnet über den Abend-Reminder oder die Kachel:

```text
+---------------------------+
| DEIN TAG, 14. NOV         |
+---------------------------+
| Wie ging es dir heute?    |
|  😣  🙁  😐  🙂  😊        |  <-- Wohlbefinden 1–5
|                           |
| Letzte Nacht geschlafen:  |
|  [ 7 h ▾ ]  ★★★★☆         |  <-- Stunden + Qualität 1–5
|                           |
| Stress heute:             |
|  [●──●──●──○──○]          |  <-- 1–5, rastet ein, Grün → Orange
|                           |
| Besonderes? (optional)    |
| [🍷 Alkohol] [🏃 Sport]   |
| [💊 Medikament] [🩸 Periode]|
|                           |
| Beschwerden heute?        |
| [ ✅ Keine Beschwerden ]  |  <-- nur wenn heute nichts eingetragen
| [ ✅ Alles eingetragen ]  |
| [ ✍️ Noch nachtragen ]    |
+---------------------------+
```
* Der Tag zählt für die Analyse erst, wenn **"Keine Beschwerden"** oder **"Alles eingetragen"** gewählt wurde. Wohlbefinden allein reicht nicht: Ein guter Tag kann trotzdem einen kurzen Blähbauch enthalten.
* Nicht bestätigte Tage werden am nächsten Tag angeboten, bis zu 2 Tage rückwirkend.

---

## 4. Benachrichtigungen

* **Abend-Reminder (ca. 20:00, Uhrzeit einstellbar):** *"Detektiv-Stunde: Wie war dein Tag? 🕵️‍♂️"* → öffnet direkt den Tages-Abschluss.
* **Kein Follow-Up** nach dem Essen.
* **Kein weiterer Push.** Die Schlaf-Abfrage ist Teil des Tages-Abschlusses.
* **Meilensteine statt Streak-Druck:** *"7 Tage Spuren gesammelt – deine erste Auswertung ist bereit!"*

---

## 5. Die Analyse-Präsentation (Aha-Moment)

Nach 7 bestätigten Tagen:

```text
+---------------------------+
| DEINE SPUREN              |
+---------------------------+
| 🔎 Verdacht               |
| Schlechter Schlaf         |
| → Kopfschmerzen           |
| 4 von 5 Tagen nach kurzer |
| Nacht, sonst 1 von 9      |
|  [ Mehr ]                 |
+---------------------------+
| 🔍 Spur                   |
| Laktose (Milch, Quark)    |
| → Blähbauch, 1–3 h danach |
+---------------------------+
| 🟢 GUT VERTRAGEN (12)     |
| Reis, Kartoffel, Huhn …   |
+---------------------------+
| 🔒 100 % lokal berechnet  |
+---------------------------+
```
* **Evidenzstufen statt Prozent:** – zu wenig Daten / 🔍 Spur / 🔎 Verdacht / 🕵️ Starker Verdacht (Regeln siehe TRD 2.8).
* **Lebensmittel und Lifestyle-Faktoren** im selben Ranking.
* **Begründung in einem Satz** mit echten Zahlen aus den eigenen Einträgen.
* **"Gut vertragen"-Liste** als positives Ergebnis.
* **Nicht unterscheidbar:** *"Weizen + Käse kamen immer zusammen vor."*
* **Nächster Schritt:** "Mit Arzt besprechen (PDF)" (Premium).
* Formulierungen bleiben bei **Muster in deinen Einträgen**, nie "Diagnose" oder "Ursache gefunden".

---

## 6. UI-Spezifikationen für Entwickler

* **Navigation:** Tab-Bar unten (Home, Historie, Favoriten, Profil).
* **Feedback:** Nach dem Speichern eine kurze Snackbar mit "Rückgängig" statt Bestätigungsdialog. Die Erfolgsanimation aus dem Original ist abgeschnitten, Details folgen.
* **Eingaben:** 5er-Tap-Punkte statt Slider für Intensität. Der Stress-Slider rastet auf 5 Stufen ein.
* **Haptik:** Kurzes Vibrieren beim Speichern.
