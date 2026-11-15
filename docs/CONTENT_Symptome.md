# Symptom-Katalog & Eingabe-Logik: Allergie-Detektiv

**Version:** 1.1 (nach Review: ergänzte Symptome, Intensität 1–5, Notfall-Symptome)
**Ziel:** Standardisierte Datenerfassung für die Analyse (siehe [TRD](TRD_Technik.md)).

---

## 1. Die Symptom-Kategorien (Grid-Auswahl)

Vier visuelle Gruppen. Jedes Symptom hat eine feste interne ID. Die Gruppe (A–D) bestimmt, welche Trigger als plausibel gelten (siehe [CONTENT_Zutaten.md](CONTENT_Zutaten.md), Kapitel 2).

### Gruppe A: Magen & Darm
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 1001 | 🤢 | Übelkeit |
| 1002 | 🎈 | Blähbauch |
| 1003 | 🔥 | Sodbrennen |
| 1004 | 📉 | Bauchkrämpfe |
| 1005 | 💩 | Durchfall |
| 1006 | 🧱 | Verstopfung |

### Gruppe B: Haut
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 2001 | 🔴 | Flush (Rötung) |
| 2002 | 🐜 | Juckreiz |
| 2003 | 🖐️ | Quaddeln |
| 2004 | 💧 | Schwellungen ⚠️ |
| 2005 | 🌵 | Trockene Stellen / Ekzem |

### Gruppe C: Kopf, Energie & Kreislauf
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 3001 | 🤯 | Kopfschmerzen |
| 3002 | 🌀 | Schwindel |
| 3003 | ⚡ | Migräne |
| 3004 | 💤 | Müdigkeit |
| 3005 | 🧠 | Brain Fog |
| 3006 | 💓 | Herzrasen |

### Gruppe D: Atemwege, Augen & Mund
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 4001 | 🤧 | Schnupfen |
| 4002 | 👃 | Verstopfte Nase |
| 4003 | 😤 | Reizhusten |
| 4004 | 🌬️ | Atemnot ⚠️ |
| 4005 | 👄 | Kribbeln Mund / Lippen |
| 4006 | 👁️ | Augen jucken / tränen |

### Sonstiges
| ID | Icon | Symptom |
| :--- | :--- | :--- |
| 9001 | ❓ | Sonstiges (mit optionaler Notiz) |

⚠️ = **Notfall-Symptom** (`is_red_flag`), siehe Kapitel 3.

> "Sonstiges" fließt nicht in die Analyse ein, erscheint aber im PDF für den Arzt.

---

## 2. Der Symptom-Detail-Flow (Overlay)

### Wireframe: Symptom-Präzisierung
```text
+---------------------------+
| [Icon] SYMPTOM-NAME       |
+---------------------------+
|                           |
| WIE STARK?                |
|  (1) (2) (3) (4) (5)      |  <-- 5 große Tap-Punkte, Default 3
|  leicht         stark     |
|                           |
| WANN GING ES LOS?         |
| Uhrzeit: [ 16:45 ]        |
| [--|---------------]      |  <-- Zeit-Slider (Default: Jetzt)
|                           |
| + Notiz (optional)        |
|                           |
|      [ SPEICHERN ]        |
+---------------------------+
```

---

## 3. Notfall-Symptome

Bei **Schwellungen (2004)** und **Atemnot (4004)** erscheint **vor** dem Overlay ein Hinweis:

```text
+---------------------------+
| ⚠️  WICHTIG               |
|                           |
| Atemnot oder Schwellung   |
| an Mund, Zunge oder Hals  |
| können ein Notfall sein.  |
|                           |
|  [ 📞 Notruf 112 ]        |
|  [ Weiter eintragen ]     |
+---------------------------+
```

---

## 4. Technische Verknüpfung

Gespeichert in `symptom_entries` (siehe [TRD](TRD_Technik.md), Kapitel 1.3):

| Feld | Logik |
| :--- | :--- |
| **symptom_id** | Referenz aus obiger Liste |
| **intensity** | 1–5, gewichtet Treffer mit `intensity / 5` |
| **started_at** | Zeitpunkt des Beginns (UTC + Zeitzone) |
| **note** | Optional, nicht in der Analyse |

Gleiche Symptome bzw. dieselbe Gruppe innerhalb von 3h werden zu **einer Episode** zusammengefasst (TRD 2.2).

---

## 5. UX-Prinzipien für Symptome

1. **Feste Auswahl statt Freitext:** "Sonstiges" + Notiz als Ventil.
2. **Favoriten-Symptome:** Die drei am häufigsten genutzten Symptome stehen im Grid ganz oben.
3. **Schnell-Log per Long-Press:** Long-Press auf eine Kachel speichert sofort (Intensität 3, Zeit: Jetzt). Danach erscheint eine Snackbar *"Gespeichert · Rückgängig"*. Notfall-Symptome zeigen trotzdem den Hinweis.
