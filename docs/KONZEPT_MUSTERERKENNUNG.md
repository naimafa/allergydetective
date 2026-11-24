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

## Stand 24.11.2026 (abends)

Umgesetzt (Branch `feat/day-lag-analysis`):

- Tagesrechnung mit gelernter Verzögerung 0–3 Tage, Vergleich "Tag nach X" gegen "Tag nach einem Tag ohne X"
- Beschwerden ab Stärke 2, alle zusammen; eine Beschwerde über mehrere Tage zählt nur am ersten Tag
- Spur erst ab 2 echten Beschwerdetagen; "gut vertragen" nur ohne jede Beschwerde danach
- Jede Zutat wird geprüft, auch ohne hinterlegten Auslöser
- Schnelle Reaktionen bis 2 h pro Mahlzeit; die klarere der beiden Sichten gilt
- "Fast täglich gegessen" ab 7 abgeschlossenen Tagen und ≥ 80 % der Tage, mit Hinweis zum Weglassen
- Tipp zum Weglassen in der Einführung und in "Muster entdecken"

## Nächste Schritte (aus den Analysen von ChatGPT und Gemini, mit dem User besprochen)

### A. Fass-Wert (kumulative Last)

Manche Reaktionen kommen erst, wenn sich etwas über Tage ansammelt (z. B. Histamin, Weizen):
eine kleine Portion geht, zwei Tage hintereinander nicht.

- Für jeden Kandidaten: Summe der Portionen (S 0,5 · M 1 · L 2) in den **3 Tagen vor** einem Tag.
- Vergleich: Last vor **Beschwerdetagen** gegen Last vor **beschwerdefreien Tagen**.
- Karte z. B.: "Viel Weizen über 2–3 Tage: danach öfter Beschwerden".
- Gleiche Regeln wie bei Spuren: mindestens 2 echte Beschwerdetage, keine Scheingenauigkeit.

### B. Weitere Störfaktoren im Tagesabschluss

Neben Stress, Sport, krank: **Medikamente** und **Periode** – beide senken oft die Toleranzschwelle.
Neue Lifestyle-Faktoren per Seed-Migration.

### C. Spurensuche-Zeitleiste

Tipp auf eine Karte → die Beschwerdetage mit den 72 h davor (Essen, Schlaf, Stress, Faktoren), Gemeinsamkeiten hervorgehoben.

### D. Experimente

Verdacht oder fast täglich Gegessenes 2 Wochen weglassen, dann wieder einführen; vorher/nachher vergleichen.

### Bewusst nicht übernommen

- **Gesamtwert "72 / 100"**: wirkt genau, ist es bei wenigen Tagen nicht, und liest sich wie eine Wahrscheinlichkeit (MDR).
- **Fest gewichtete Zeitfenster (1,0 / 0,7 / 0,4)**: willkürlich; die Verzögerung wird stattdessen aus den eigenen Daten gelernt und offen genannt.
- **Reaktionszeit je Beschwerde-Art**: erst, wenn genug Daten da sind.
