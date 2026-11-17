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
| 1 Daten-Herz | ca. 1:16 h | Room-Schema v1, Seed-Daten, Analyse-Engine, Gruppierung, Koin/Seeder, Repository |
| 2 Skelett | ca. 1:16 h | Alle Screens plus die offenen Punkte (Favoriten, Historie, Reminder, Spracheingabe …) |
| 3 Design | ca. 0:16 h | Theme hell/dunkel, Icons, Dashboard, Symptom-Kacheln, Ergebnis-Karten |
| 4 Self-Test (Vorbereitung) | ca. 0:11 h | JSON-Export, Anleitung, Installation aufs Handy; der 7-Tage-Test selbst läuft |
| Doku / Organisation | ca. 0:07 h | OFFENE_PUNKTE, PROGRESS, Zeitprotokoll (ab 16.11. in den Phasen enthalten) |
| **Gesamt (erfasst)** | **ca. 6:48 h** | ohne Konzeptphase |

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
| 16.11.2026 | 21:20–21:24 | 0:04 | 1 | Testfehler behoben (Long vs. Int im Test), alle Tests und Build grün | – |
| 16.11.2026 | 21:37–21:49 | 0:12 | 1 | Analyse-Engine test-first: 24 Tests mit synthetischen Daten, dann Engine (Episoden, Fenster, Lift, Evidenz, Sicher-Liste), `AnalysisConfig`; Übergabe per Git-Bundle | Kein Gradle-Build in Claudes Umgebung (kein Netz) → Tests mit Kotlin 2.0 und eigenem Mini-Runner geprüft, Gradle-Lauf lokal ausstehend; Rundungsfehler an Schwellen (5,999… < 6) gefunden und behoben |
| 16.11.2026 | 22:05–22:10 | 0:05 | 1 | Gruppierung nicht unterscheidbarer Kandidaten (TRD 2.7) test-first, Gradle-Tests grün | Typfehler im neuen Test (`Candidate.Ingredient` statt `Candidate`) |
| 16.11.2026 | 22:18–22:27 | 0:09 | 1 | Koin eingebunden, Seeder beim App-Start, im Emulator geprüft | – |
| 16.11.2026 | 22:27–22:40 | 0:13 | 1 | Repository Room → `AnalysisInput`, Emulator-Test mit 21-Tage-Test-Tagebuch in der echten Datenbank; Phase 1 fertig | Shell-Syntaxfehler beim Anlegen mehrerer Dateien → Dateien einzeln angelegt |
| 17.11.2026 | 19:24–19:35 | 0:11 | 2 | `develop` angelegt; App-Grundgerüst (Navigation, Theme, Uhr, Home, Texte EN/DE) | Keine GitHub-PRs möglich (`gh` fehlt, Token-Nutzung abgelehnt) → lokale Merges; Emulator neu gestartet |
| 17.11.2026 | 19:35–19:42 | 0:07 | 2 | Essen loggen (Suche mit Synonymen/Tippfehlern, Portionen, Rückgängig) | – |
| 17.11.2026 | 19:42–19:46 | 0:04 | 2 | Symptom melden mit Overlay, Schnell-Log und Notfall-Hinweis | Handy per USB dazugekommen → Prüfbefehle fest auf den Emulator |
| 17.11.2026 | 19:46–19:51 | 0:05 | 2 | Tages-Abschluss, Fortschritt auf Home | – |
| 17.11.2026 | 19:51–19:57 | 0:06 | 2 | Ergebnis-Screen, Doku; Phase 2 fertig | Emulator-DB beim Einspielen von Testdaten halb ersetzt (Pfad-Umschreibung der Git-Bash) → aus Backup wiederhergestellt |
| 17.11.2026 | 19:57–20:40 | 0:43 | 2 | Offene Punkte: Auto-Backup aus, freie Zeitwahl, Favoriten, eigene Zutaten, Onboarding, Tabs/Historie, Abend-Reminder, Vergleich im Ergebnis, Notruf je Region, Zahlenformat, Spracheingabe | WorkManager startet periodische Jobs nicht vor ihrer Zeit → kurzer Test-Build ohne Verzögerung; Spracherkennung im Emulator nicht verfügbar |
| 17.11.2026 | 20:40–20:56 | 0:16 | 3 | Design: Farben hell/dunkel, Icons, Dashboard mit Tagesspruch, Symptom-Symbole, Ergebnis-Karten; Phase 3 fertig | Schrift Inter offen (Font-Datei nötig) |
| 17.11.2026 | 20:56–21:07 | 0:11 | 4 | JSON-Export, SELFTEST.md, App aufs eigene Handy installiert | Handy war zuerst nicht verbunden |

---

## Erkenntnisse
- **Größter Zeitfresser bisher:** Werkzeug-Setup (Android Studio, Git-Auth, Gerät/USB), nicht der Code.
- **Room-Schema** ging schnell (ca. 21 min inkl. Fehlerbehebung), weil TRD und Coding-Richtlinien klar waren.
- **Analyse-Engine** in ca. 12 min, weil TRD Kap. 2 die Regeln fast vollständig vorgibt. Offene Fachfragen sind als Annahmen dokumentiert statt geblockt.
- **Phase 2 und 3 in ca. 1:30 h** (17.11.), weil Claude die Schritte ohne Rückfragen hintereinander gebaut, getestet und nach `develop` gemergt hat. Der meiste Aufwand steckte in der Prüfung im Emulator (Starten, Tippen, Datenbank auslesen), nicht im Code.
- **Zeiten ab dem 16.11. abends** stammen aus Git-Commits und dem Reflog; der Beginn eines Blocks ist daher der erste Git-Schritt, nicht die erste Nachricht.
