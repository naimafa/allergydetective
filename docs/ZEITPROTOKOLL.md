# Zeitprotokoll: Allergie-Detektiv

Ziel: Timeline und Produktivität nachvollziehen. Pro Arbeitsblock: Datum, Zeitraum, Dauer, Phase, Ergebnis.

**Quelle der Zeiten:** Nachrichten-Zeitstempel im Chat und Änderungszeiten der Dateien bzw. Git-Commits.
Die Dauer ist **verstrichene Zeit** (inkl. kurzer Pausen), keine reine Arbeitszeit. Abweichungen bitte von Hand korrigieren.

---

## Übersicht

| Phase | Dauer (bisher) | Bemerkung |
| :--- | ---: | :--- |
| 0 Konzept & Doku | ? | Vor dem 14.11., 18:30 (Smartphone-Chats). Bitte grob nachtragen. |
| 0.5 Setup | ca. 3:42 h | davon ca. 2:15 h Android Studio / KMP-Einrichtung, 30 min Git-Push mit Auth-Problem, 24 min App aufs Handy |
| 1 Daten-Herz | ca. 0:33 h | Room-Schema v1, Seed-Daten |
| Doku / Organisation | ca. 0:07 h | OFFENE_PUNKTE, PROGRESS, Zeitprotokoll |
| **Gesamt (erfasst)** | **ca. 4:22 h** | ohne Konzeptphase |

---

## Einträge

| Datum | Von–Bis | Dauer | Phase | Was | Hindernisse |
| :--- | :--- | ---: | :--- | :--- | :--- |
| 14.11.2026 | bis 18:34 | ? | 0 | Konzeptdokumente sortiert, Review, Architektur- und IDE-Entscheidung | Zeit nicht erfasst |
| 14.11.2026 | 18:32–18:34 | 0:02 | 0.5 | `git init`, erster Commit (Doku) | – |
| 14.11.2026 | 18:34–20:51 | ca. 2:15 | 0.5 | Android Studio, KMP-Plugin, Testgerät eingerichtet, KMP-Projekt angelegt | Genaue Aufteilung unbekannt |
| 14.11.2026 | 20:51–21:16 | 0:25 | 0.5 | Erster Build, Hello World auf Android, PROGRESS aktualisiert | – |
| 15.11.2026 | 19:02–19:05 | 0:03 | 0.5 | Stand geprüft: Projektcode nicht committet, kein Remote | – |
| 15.11.2026 | 19:05–19:08 | 0:03 | Doku | Launch-Checkliste (Rechtliches) in OFFENE_PUNKTE.md | – |
| 15.11.2026 | 19:08–19:38 | 0:30 | 0.5 | Commit, GitHub-Repo angelegt, Push | `Authentication failed` (Passwort statt Browser-Login/Token) |
| 15.11.2026 | 19:38–19:54 | 0:16 | 1 | Room 3 + KSP eingebunden, 18 Tabellen, erster Build (Schema `1.json` erzeugt) | Lint-Fehler „implicit dependency“ |
| 15.11.2026 | 19:54–19:59 | 0:05 | 1 | Lint/KSP-Workaround, Build grün, gepusht | – |
| 15.11.2026 | 19:59–20:03 | 0:04 | Doku | PROGRESS aktualisiert, Zeitprotokoll angelegt | – |
| 15.11.2026 | 20:03–20:06 | 0:03 | 0.5 | Entscheidung minSdk 26 / targetSdk 37, umgestellt | Projekt-Assistent hatte minSdk 37 gesetzt (Testgerät Android 13 wäre ausgeschlossen) |
| 15.11.2026 | 20:06–20:30 | 0:24 | 0.5 | App erstmals auf eigenem Handy (Android 13) installiert und gestartet | USB-Debugging einrichten; `INSTALL_FAILED_USER_RESTRICTED` (Schalter „Über USB installieren“) |
| 16.11.2026 | 20:56–21:08 | 0:12 | 1 | Seed-Daten (JSON, EN/DE-Strings), Seeder-Code, Tests; Übergabe per Git-Bundle | Claude kam nicht direkt an die tief verschachtelten Code-Dateien → Repo aus `.git` gespiegelt |

---

## Erkenntnisse
- **Größter Zeitfresser bisher:** Werkzeug-Setup (Android Studio, Git-Auth, Gerät/USB), nicht der Code.
- **Room-Schema** ging schnell (ca. 21 min inkl. Fehlerbehebung), weil TRD und Coding-Richtlinien klar waren.
