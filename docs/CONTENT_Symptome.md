# Symptom-Katalog & Eingabe-Logik: Allergie-Detektiv

**Version:** 1.0
**Ziel:** Standardisierte Datenerfassung für präzise mathematische Korrelation (T1, T2, T3).

---

## 1. Die Symptom-Kategorien (Grid-Auswahl)

Um die Auswahl zu beschleunigen, werden die Symptome in vier visuelle Gruppen unterteilt. Jedes Symptom hat eine feste interne ID für den Algorithmus.

### Gruppe A: Magen & Darm (Fokus T2/T3)
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 1001 | 🤢 | Übelkeit |
| 1002 | 🎈 | Blähbauch |
| 1003 | 🔥 | Sodbrennen |
| 1004 | 📉 | Bauchkrämpfe |
| 1005 | 🚽 | Stuhlveränderung |

### Gruppe B: Haut & Äußeres (Fokus T1/T3)
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 2001 | 🔴 | Flush (Rötung) |
| 2002 | 🐜 | Juckreiz |
| 2003 | 🖐️ | Quaddeln |
| 2004 | 💧 | Schwellungen |
| 2005 | 🌵 | Trockene Stellen |

### Gruppe C: Kopf & Energie (Fokus Histamin/Stress)
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 3001 | 🤯 | Kopfschmerzen |
| 3002 | 🌀 | Schwindel |
| 3003 | ⚡ | Migräne |
| 3004 | 💤 | Müdigkeit |
| 3005 | 🧠 | Brain Fog |

### Gruppe D: Atemwege (Fokus T1 Sofortreaktion)
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 4001 | 🤧 | Schnupfen |
| 4002 | 👃 | Verstopfte Nase |
| 4003 | 😤 | Reizhusten |
| 4004 | 🌬️ | Atemnot |

---

## 2. Der Symptom-Detail-Flow (Overlay)

Sobald eine Kachel ausgewählt wurde, öffnet sich dieses Overlay zur Präzisierung. Ohne diese Daten kann die Analyse-Engine ([TRD](TRD_Technik.md)) nicht rechnen.

### Wireframe: Symptom-Präzisierung
```text
+---------------------------+
| [Icon] SYMPTOM-NAME       |
+---------------------------+
|                           |
| WIE STARK WAR ES?         |
| [ 1 - 2 - 3 - 4 - 5 ]     |  <-- Schnellauswahl (Punkte)
|                           |
| WANN GING ES LOS?         |
| Uhrzeit: [ 16:45 ]        |
| [--|---------------]      |  <-- Zeit-Slider (Default: Jetzt)
|                           |
|      [ SPEICHERN ]        |
+---------------------------+
```

---

## 3. Technische Verknüpfung (TRD-Schnittstelle)

Jeder Symptom-Eintrag wird in der lokalen SQLite-Tabelle `symptoms_log` mit folgendem Mapping gespeichert:

| Feld | Logik |
| :--- | :--- |
| **Symptom_ID** | Referenz aus obiger Liste. |
| **Intensity** | Wert 1–5 (wird für Score-Gewichtung genutzt). |
| **Timestamp** | Exakter Zeitpunkt (wichtig für Fenster-Analyse). |
| **Context** | Automatischer Check: War `Stress_Level` zum Zeitpunkt > 3? |

---

## 4. UX-Prinzipien für Symptome

1. **Kein Freitext:** Um "Daten-Müll" zu vermeiden, gibt es kein Freitextfeld. Nutzer müssen aus der Liste wählen.
2. **Favoriten-Symptome:** Die drei am häufigsten genutzten Symptome rutschen im Auswahl-Grid nach ganz oben.
3. **One-Tap-Logging:** Wenn die Intensität egal ist, reicht ein Doppelklick auf die Kachel, um es mit Standardwerten (Intensität 3, Zeit: Jetzt) sofort zu loggen.
