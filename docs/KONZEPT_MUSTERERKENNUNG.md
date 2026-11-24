# Konzept: Mustererkennung neu (Entwurf 24.11.2026)

## Ausgangslage

Die heutige Analyse prüft jede Mahlzeit mit einem festen Zeitfenster je Auslöser (z. B. Laktose 15 min – 6 h).
Probleme aus dem Self-Test:

- Reaktionen außerhalb des festen Fensters (später, oder erst nach Tagen) werden nicht gesehen.
- Lange Fenster (bis 2 Tage) überdecken viele Mahlzeiten: jedes Symptom passt zu fast allem.
- Was täglich gegessen wird, hat keine Vergleichsmahlzeiten "ohne".
- Kleine Zahlen + Glättung ergaben Spuren ohne Beschwerden (behoben am 24.11.: mindestens 2 echte Beschwerden).

## Idee

**Tage statt Mahlzeiten, und die Verzögerung wird aus den eigenen Daten gelernt.**

Für jedes Lebensmittel / jeden Auslöser fragt die App:
"Wie ging es dir am selben Tag, am Tag danach, zwei und drei Tage danach – an Tagen *mit* gegenüber Tagen *ohne*?"

### 1. Tagesbild

- Ein Tag ist ein Kalendertag (lokal), nur abgeschlossene Tage zählen.
- **Gegessen am Tag:** größte Portion des Kandidaten an diesem Tag (S 0,5 · M 1 · L 2).
- **Beschwerden am Tag:** stärkste Beschwerde des Tages (1–5). Ein Tag "mit Beschwerden" = mindestens eine Beschwerde ≥ 2.
- Krankheitstage zählen halb, Beschwerden "gehört zur Krankheit" zählen nicht (wie heute).

### 2. Verzögerung (Lag)

Vier Stufen: **gleicher Tag (0)**, **nächster Tag (1)**, **übernächster Tag (2)**, **3 Tage danach (3)**.

Für jede Stufe:

- **Mit:** Tage D, an denen der Kandidat gegessen wurde → Beschwerden an D + Lag?
- **Ohne (sauberer Vergleich):** Tage, an denen der Kandidat in den 3 Tagen davor *nicht* gegessen wurde → Beschwerden?

Die Stufe mit dem deutlichsten Unterschied ist die "typische Verzögerung" für diesen Kandidaten.

### 3. Wann ist es eine Spur?

- mindestens **3 Tage mit** dem Kandidaten und **3 saubere Tage ohne**
- mindestens **2 Tage mit Beschwerden** in der gewählten Stufe
- Beschwerden nach dem Kandidaten mindestens **doppelt so häufig** wie ohne
- Wer mehrere Stufen testet, findet leichter Zufälle: die beste Stufe muss deshalb etwas strenger bestehen (Lift ≥ 2 statt 1,5).
- Verdacht / starker Verdacht wie heute über mehr Tage und Stabilität (14 Tage).

### 4. Täglich gegessen

Hat ein Kandidat zu wenig saubere Tage ohne (z. B. Brot jeden Tag), zeigt die App:
"Brot isst du fast täglich – das lässt sich so nicht prüfen. Probier es 2 Wochen wegzulassen." (→ Experimente, später)

### 5. Schnelle Reaktionen bleiben

Sehr schnelle Reaktionen (bis 2 h, allergieähnlich) gehen in der Tagesrechnung unter.
Sie werden weiter je Mahlzeit geprüft (heutiges Verfahren, nur Fenster 0–2 h).

### 6. Anzeige

> **Spur: Milch** – Beschwerden meist **am nächsten Tag**
> Nach Milch: an 3 von 4 Tagen · ohne Milch: an 1 von 10 Tagen

Tipp auf die Karte → **Spurensuche**: die Tage mit Beschwerden als Zeitleiste der 72 h davor (Essen, Schlaf, Stress), Gemeinsamkeiten hervorgehoben.

### 7. Später

- **Experimente:** weglassen → wieder einführen, vorher/nachher vergleichen (einziger Weg bei Täglichem).
- **Tageslast:** Summe statt größte Portion für Histamin & Co.
- Getrennt nach Beschwerde-Art (Bauch, Haut, Kopf …), sobald genug Daten da sind.

## Offene Fragen

1. ~~Stärke~~ → **ab 2** (entschieden 24.11.2026)
2. ~~Zusammen oder getrennt~~ → **alle zusammen** (entschieden 24.11.2026)
3. ~~Längste Verzögerung~~ → **3 Tage** (entschieden 24.11.2026)

## Umsetzung (Vorschlag)

1. Tagesbild + Lag-Rechnung als neue Engine, test-first, alte Engine bleibt für schnelle Reaktionen
2. Karten in "Muster entdecken" mit typischer Verzögerung
3. Spurensuche-Zeitleiste
4. Hinweis "täglich gegessen"
5. Experimente (eigene Runde)
