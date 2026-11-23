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
| 3 Design | ca. 5:42 h | 17.11.: Theme hell/dunkel, Icons, Dashboard, Symptom-Kacheln, Ergebnis-Karten (0:16). 19.11.: Startseite feinjustiert, Dark Mode (1:10). 20.11.: App-Icon, HTML-Prototyp (0:39). 21.11.: Startseite nach Design-Bild, hell und dunkel (3:37) |
| 4 Self-Test | ca. 8:14 h | 17.11.: Vorbereitung (0:11). 18.11.: Rückmeldungen aus dem Self-Test umgesetzt, inkl. 0:07 Absturz-Reparatur (2:56). 19.11.: Design-Feedback, Historie, Krankheitstage, Nachtragen (3:14). 20.11.: Getränk, Dauer, Arzt-PDF, Verschlüsselung, Feedback-Runde (1:53); der 7-Tage-Test selbst läuft |
| Doku / Organisation | ca. 0:50 h | OFFENE_PUNKTE, PROGRESS, Zeitprotokoll, 18.11. auch GitHub-PR (sonst meist in den Phasen enthalten) |
| **Gesamt (erfasst)** | **ca. 32:00 h** | ohne Konzeptphase; 21.11. Nachmittag bis 23.11. (ca. 11:00 h, meist Design und Testnotizen) aus den Einträgen unten nachgerechnet |

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
| 18.11.2026 | ca. 19:25–19:33 | ca. 0:08 | 4 | Self-Test-Notizen (14 Punkte) mit dem Code abgeglichen, in Bugs / Daten / Vereinfachungen / Neues sortiert | – |
| 18.11.2026 | 19:33–19:53 | 0:20 | 4 | 4 Bugs: Speichern-Button, JSON-Export als Datei, Tages-Abschluss wählt heute, Dinkelvollkornbrot (Migration 1 → 2); Upgrade im Emulator geprüft | Emulator-Fenster nur zu einem Drittel sichtbar (noch beim Booten); Handy zusätzlich per USB → Befehle fest auf den Emulator |
| 18.11.2026 | 19:53–20:00 | 0:07 | 4 | Absturz des PCs: Git-Metadaten und zwei Doku-Dateien mit Nullen überschrieben → `.git` gesichert, HEAD/Index repariert, Doku neu geschrieben | Repo zeitweise nicht mehr als Git-Repo erkannt |
| 18.11.2026 | 20:00–20:30 | 0:30 | 4 | Build und Tests nach dem Absturz, Installation aufs Handy, Rückmeldung (3 Fixes bestätigt) | – |
| 18.11.2026 | 20:30–20:59 | 0:29 | 4 | Übrige Punkte: Öle & Oliven (Migration 2 → 3), Mahlzeit/Tageszeit statt Uhrzeit, kranke Tage (Migration 3 → 4), Gerichte aus Zutaten, Einführung; Emulator-Check hell/dunkel, Installation | Uhrzeiten für Tageszeiten selbst festgelegt (ENTSCHEIDUNGEN) |
| 18.11.2026 | 20:59–21:10 | 0:11 | 4 | Butter verschwand hinter der Tastatur, doppeltes Dinkelvollkornbrot unter „Zuletzt gegessen“ | Ursache: eigene Zutat von vor dem Seed-Eintrag |
| 18.11.2026 | 21:10–21:35 | 0:25 | 4 | Roh/gekocht als Pflicht (Migration 4 → 5), Zutat im Gericht anlegen, Doppelt-Prüfung beim Anlegen; Upgrade v4 → v5 im Emulator | Testskript tippte bei offener Tastatur ins Leere (alte Version) |
| 18.11.2026 | 21:35–21:48 | 0:13 | 4 | Doppelte Zutaten beim Start zusammenführen (vorher Handy-DB gesichert und im Emulator getestet), Gerichte bearbeiten | Kein sqlite3 im Emulator → Prüfung über die Oberfläche |
| 18.11.2026 | 21:48–21:57 | 0:09 | 4 | „Einführung ansehen“ im Profil (Einführung kam nur beim ersten Start) | – |
| 18.11.2026 | 21:57–22:21 | 0:24 | 4 | Krankheitszeitraum (Migration 5 → 6), Historie aufgeräumt, Kachel-Design Start und Symptome; Installation | – |
| 18.11.2026 | 22:21–22:33 | 0:12 | Doku | APK für andere Handys bereitgelegt, `gh` angemeldet, PR #5 `develop` → `main` gemergt | `gh auth login` hing im Terminal (Sprachausgabe-Modus) → Anmeldung per Gerätecode |
| 18.11.2026 | 22:33–22:40 | 0:07 | Doku | Zeitprotokoll und PROGRESS nachgetragen | – |
| 19.11.2026 | 19:22–19:47 | 0:25 | 4 / 3 | Design-Feedback: Waldgrün-Palette, Startseite neu (Kacheln nicht mehr abgeschnitten), Essen in drei Schritten ohne vorgewählte Mahlzeit, Mahlzeiten aus der Historie bearbeiten und ergänzen, Historie mit Filtern und Krankheitszeiten, Tagesabschluss in Karten mit sofortigem Speichern (PRs #7–#12) | – |
| 19.11.2026 | 19:47–20:07 | 0:20 | 4 | Krankheitstage schwächer gewichtet, je Symptom „gehört zur Krankheit?“ (Migration 6 → 7, PR #13); PRs #7–#13 nach `develop` gemergt | – |
| 19.11.2026 | 20:07–20:44 | 0:37 | 4 | Ermutigender Text im Fortschritt, klarere Kachelnamen, ein Stil für Status-Marken, Favoriten als Karten, Gerichte suchen, gegessene Mahlzeit als Gericht speichern, ruhigere Startseite (PRs #14–#17) | – |
| 19.11.2026 | 20:44–21:37 | 0:53 | 3 | Startseite nach Handy-Test feinjustiert: gleiche Schriftgröße auf allen Kacheln, schmalerer Kopf, kürzere Kacheltitel, niedrigere Kacheln | Bei Schriftgröße 125 % schrumpften die Kacheltexte unterschiedlich → mehrere Runden bis zu einer gemeinsamen Größe |
| 19.11.2026 | 21:37–21:54 | 0:17 | 3 | Dark Mode im Profil (System / Hell / Dunkel), automatisch abends und nachts (PR #18); kompakte Karte „Dein heutiger Tag“ (PR #19) | Karte drückte die Kacheln aus dem Bild → kompakter gemacht |
| 19.11.2026 | 21:54–22:17 | 0:23 | 4 | Historie: Datum bleibt sichtbar, Krankheitstage unter „Abschlüsse“, jedes Löschen mit Rückfrage, Krankheitszeitraum als ein Band (PR #20) | – |
| 19.11.2026 | 22:17–23:00 | 0:43 | 4 / 3 | Historie als ruhige Zeilen mit Detail-Blatt, Filter in einer wischbaren Zeile (PR #21); ausgewählte Chips deutlich sichtbar in Waldgrün mit Häkchen (PR #22) | Ausgewählte Chips waren kaum vom Dialog-Hintergrund zu unterscheiden |
| 19.11.2026 | 23:00–23:17 | 0:17 | 4 | Skalen 1–5 in einer Reihe gleich breiter Chips; Beschwerden korrigieren, Essen und Beschwerden für frühere Tage nachtragen (PR #23) | Bei großer Schrift rutschte die „5“ in eine zweite Zeile |
| 19.11.2026 | 23:17–23:46 | 0:29 | 4 / 3 | Gleiche Tageszeile „Heute · Gestern · Datum“ in jedem Formular, fünf Tage zurück (PR #24); ruhigerer Dark Mode (PR #25); kompakte Symptom-Kacheln, Namen in einer Zeile (PR #26) | Im Symptom-Fenster war „Datum“ abgeschnitten → drei Korrektur-Runden |
| 20.11.2026 | 20:47–20:52 | 0:05 | 4 | Neue Punkte gesichtet (Icon, Arzt-Export, Getränk, Verschlüsselung), Ursache „Getränk wird Mahlzeit“ gefunden, Stand Verschlüsselung geprüft | – |
| 20.11.2026 | 20:52–20:58 | 0:06 | 4 | Profil: Einstellungen oben, bekannte Allergien unten (PR #27) | – |
| 20.11.2026 | 20:58–21:07 | 0:09 | 4 | Getränk als eigene Art, Art wird gespeichert und bleibt bei geänderter Uhrzeit (Migration 7 → 8, PR #28) | Kein Python auf dem PC → Änderungen einzeln; SQLite-Test der Migration auf dem PC nicht möglich (native Bibliothek fehlt) |
| 20.11.2026 | 21:07–21:10 | 0:03 | 3 | App-Icon als Vektor nachgebaut, Vorschau per Edge (PR #29) | Veralteter Build-Cache meldete fehlende Ressource |
| 20.11.2026 | 21:10–21:15 | 0:05 | 4 | Dauer beim Symptom, optional (Migration 8 → 9, PR #30) | – |
| 20.11.2026 | 21:15–21:20 | 0:05 | 4 | Bericht für den Arzt als PDF (14/30/90 Tage, PR #31) | – |
| 20.11.2026 | 21:20–22:15 | 0:55 | 4 / 3 | Verschlüsselung recherchiert und gebaut: eigener Room-3-Treiber auf SQLCipher, Schlüssel im Android Keystore, einmalige Umstellung alter Daten (PR #32); Icon aus dem Bild zugeschnitten (PR #33) | Kein offizieller SQLCipher-Treiber für Room 3; Sitzung brach mitten im Build ab → beschädigte Gradle-Sperrdateien und KSP-Datei bereinigt |
| 20.11.2026 | 22:15–22:27 | 0:12 | 4 | Installation aufs Handy: Umstellung auf Verschlüsselung stürzte ab (ATTACH ohne CREATE), behoben, zweiter Versuch erfolgreich (DB verschlüsselt, Migration 7 → 9) | Alte Daten blieben beim Absturz wie geplant unverändert |
| 20.11.2026 | 22:27–22:35 | 0:08 | 3 | Flaches Icon aus dem neuen Bild, zentriert für alle Masken; Name „Allergy Detective“ mit Leerzeichen | – |
| 20.11.2026 | 22:35–22:37 | 0:02 | 4 | Bekannte Allergien im Profil zuklappbar (PR #34) | – |
| 20.11.2026 | 22:37–22:44 | 0:07 | 4 | Design-Feedback: Legenden für Schlafqualität und Stress, Stärke in Worten, „Schlaf letzte Nacht“, Krankheit als Knopf (PR #35) | – |
| 20.11.2026 | 22:44–23:12 | 0:28 | 3 | Klickbarer HTML-Prototyp, nach Rückmeldung nach Code und Handy-Screenshots neu gebaut, Screen für Screen abgeglichen (PR #36) | Erste Version frei gestaltet, sah nicht aus wie die App; automatische Screenshot-Serie lieferte nur den Startbildschirm |
| 20.11.2026 | 23:12–23:19 | 0:07 | 4 | Aus dem Abgleich: „Datum“ abgeschnitten (PR #37), Sterne der Schlafqualität abgeschnitten (PR #38), Symptom-Fenster breiter (PR #39); Dialog „Neues Gericht“ im Prototyp | – |
| 20.11.2026 | 23:19–23:35 | 0:16 | Doku | Zeitprotokoll und PROGRESS für den 19. und 20.11. nachgetragen (19.11. aus der Git-Historie) | – |
| 21.11.2026 | 11:10–11:24 | 0:14 | 3 | Startseite nach dem neuen Design-Bild: Begrüßung nach Tageszeit, Tageskarte mit drei Zahlen, Kacheln mit Verlauf (PR #41) | Branchname `feature/home-redesign` schon vergeben |
| 21.11.2026 | 11:24–11:55 | 0:31 | 3 | Handy-Test bei Schrift 125 %: Titel und „Jetzt abschließen“ abgeschnitten, Kacheln wirkten einfarbig; Farben aus dem Bild gemessen, Logo, Zweig, Glocke, eigene Icons, neue Leiste | Handy nicht erkannt (adb-Server neu gestartet), Handy gesperrt |
| 21.11.2026 | 11:55–12:21 | 0:26 | 3 | Alles auf einen Bildschirm ohne Scrollen, Zweig neu, Farben aus dem ChatGPT-Style-Guide, Schriften Playfair Display und Inter (OFL) eingebunden | PC-Absturz mitten im Build, Änderungen blieben erhalten |
| 21.11.2026 | 12:21–12:51 | 0:30 | 3 | Zweites Design-Bild: Pfeile im Kreis, kein Button, „Verlauf“; Farbliste der Nutzerin; Bild und Handy nebeneinander verglichen, Größen und Kacheln angepasst | – |
| 21.11.2026 | 12:51–13:22 | 0:31 | 3 | Fortschrittskarte nachgemessen, Design-Review von ChatGPT geprüft (Teile übernommen, Widersprüche zum Bild begründet) | Lesemodus des Handys verfälschte die Farben (rosa, körnig) |
| 21.11.2026 | 13:22–13:54 | 0:32 | 3 | Einheitliches Creme, Zweig weich gezeichnet, dann eigenes Blätter-Bild als Hintergrund (35 KB) und Logo als Bild, zuletzt per Code gezeichnet; Schriftzug fett in Inter | WebP ohne Python/Node: per Edge gerendert, Transparenz aus zwei Hintergründen berechnet |
| 21.11.2026 | 13:54–14:13 | 0:19 | 3 | Vorgeschlagenen Zweig-Code gerendert und verglichen (verworfen), PR #41 abgeschlossen | – |
| 21.11.2026 | 14:13–14:47 | 0:34 | 3 | Dark Mode der Startseite (PR #42): Kacheln klar statt milchig, Waldgrün-Hintergrund, derselbe Zweig wie hell neu eingefärbt, drei Varianten zur Wahl, unscharfe Blätter zurückgenommen | Mehrere Runden, bis Zweig, Grün und Lichthof zum hellen Modus passten |
| 21.11.2026 | 14:47–14:55 | 0:08 | Doku | Zeitprotokoll und PROGRESS für den 21.11. | – |
| 21.11.2026 | 14:55–15:38 | 0:43 | 3 | App-Icon aus dem Lupen-Bild der Nutzerin, vereinfacht: grüne Ecke bis zum Lupenring, Gabel und Herz statt Blitz, vergrößert (PR #44) | Launcher zeigt neue Icons erst nach Neustart des Handys |
| 21.11.2026 | 15:38–16:20 | 0:22 | 4 | Essen-Maske: Suche nicht verdeckt, Speichern über der Tastatur, Gerichte ab zwei Buchstaben, „enthält Tomate“ als Grund (PR #45) | Pause 15:49–16:14 |
| 21.11.2026 | 20:14–20:54 | 0:40 | 4 | Testnotizen Teil 2: Synonyme in der Suche, Favoriten scrollen zum Gericht, „als Gericht speichern“, Verlauf neueste zuerst, eine klare Favoriten-Aktion, „gut vertragen“ erklärt, Profil in Karten, krank → Symptome, Einführung zum Wischen, Datenschutz als eigene Seite (PRs #46–#54) | `adb input tap` nicht erlaubt → Nutzerin öffnet die Masken selbst |
| 22.11.2026 | 19:10–19:16 | 0:06 | 4 | Gewürze als eigene Kategorie inkl. Kurkuma (Migration 9 → 10, PR #55) | Neue Zutat erst nach Installation auf dem Handy sichtbar |
| 22.11.2026 | 19:16–19:23 | 0:07 | 4 | Gerichte per Finger sortieren (Migration 10 → 11, PR #56) | – |
| 22.11.2026 | 19:23–19:35 | 0:12 | 4 | Bericht und Export als PDF oder Excel, nur Analyse oder mit allen Einträgen; JSON entfernt (PR #57) | Excel ohne Bibliothek als xlsx selbst geschrieben |
| 22.11.2026 | 19:35–19:55 | 0:20 | 4 | Sprachen im Profil: Deutsch, Englisch, Französisch, Arabisch (rechts nach links), rund 600 Texte je neue Sprache, Test auf vollständige Übersetzungen (PR #58) | Startbildschirm des Handys statt App im Vordergrund → Screenshot ungesehen gelöscht |
| 22.11.2026 | 19:55–20:00 | 0:05 | Doku | Zeitprotokoll und PROGRESS für den 21. und 22.11. | – |
| 22.11.2026 | 20:00–20:50 | 0:50 | 3 | Neue Masken-Bilder der Nutzerin: Blätter-Hintergrund und große Playfair-Titel auf allen Masken, Essen-Maske in Schritt-Karten („Welche Mahlzeit?“, runde Suche, Stern und „+“ an den Chips, „Jetzt“-Pille oben rechts), Pfeil am Ende der Favoriten- und Schnellauswahl-Reihen | Bei Schrift 125 % wurde „Abendessen“ abgeschnitten → drei Runden (gleiche Schrift für alle, eigene schmale Pillen statt Chips) |
| 22.11.2026 | 20:50–21:25 | 0:35 | 3 | Verlauf als Karten mit Uhrzeit-Kästchen, Favoriten-Karten, Profil in apricot und salbeigrün, 14/30/90 Tage, PDF/Excel, Darstellung und Erinnerung je in einer Zeile, eine Schriftskala für alle Masken, Prototyp angeglichen (PR #60); danach kein reines Weiß mehr, Profilkarten bis unten im Wechsel | Blatt schien durch Filter, Suchfeld und Datumszeile → deckende cremefarbene Füllung |
| 22.11.2026 | 21:25–21:40 | 0:15 | Doku | Zeitprotokoll und PROGRESS nachgetragen (Stil-PR #60) | – |

| 23.11.2026 | 17:20–18:45 | 1:25 | 3 | Essen-Maske nach dem neuen Design-Bild (PR #61): Mahlzeit-Chips, Zutaten in Schritt 2 mit S/M/L und Papierkorb, Kategorie-Bilder (Ölflasche gezeichnet, Gemüse-Mix), Box „Ausgewählte Gerichte“ mit zusammengeführten Zutaten („aus: …“), eigene Karte „Als Gericht speichern“, Haken nur noch über Farbe; Prototyp angeglichen | Bei Schrift 125 % passten Icons nur in einer Reihe (jetzt alle oder keine); zwei Gerichte zeigten die Zutaten des ersten ohne Kopf → Zutaten merken sich ihr Gericht |
| 23.11.2026 | 18:45–19:35 | 0:50 | 4 | Testnotizen Teil 1 (PRs #62–#66): Mahlzeit abwählbar, Uhrzeit direkt im Fenster und in Schritt 1, Suche zeigt Zutat vor Gericht, vorhandenes Mittagessen wird geöffnet, Suchfeld im Dunkelmodus, schwebender „Neues Gericht“-Button, Gerichte im Editor suchbar, Kräuter als eigene Kategorie (Migration 11 → 12), Tagesabschluss in Schritten | Handy war zuerst nicht verbunden; Abwählen störte interne Aufrufe → eigene Funktion für den Tipp |
| 23.11.2026 | 19:35–20:00 | 0:25 | 4 | Ziel im Profil (14/21/eigene Tage, Fokus, neuer Abschnitt; PR #67), Tage im Verlauf getrennt (#68), Glocke führt zum offenen Tag, Sprache markiert (#69), Symptome in Schritten wie Essen (#70), Muster erklärt mit Maßnahmen und „Als Fokus setzen“ (#71) | Widerspruch in der Auswertung bemerkt (Tee/Olivenöl „nicht unterscheidbar“ und „gut vertragen“), noch offen |
| 23.11.2026 | 20:00–21:15 | 1:15 | 3 | Profil-Karten nach Bildern (#72), Bericht ruhiger mit Kalender für den Zeitraum (#73), Einführung lebendiger, Allergien vor Datenschutz (#74), Bezahlseite als versteckte Testansicht (#75), Symbol vor jedem Profil-Titel (#76) | Langes Drücken traf zuerst nur den Titel; ein Screenshot zeigte die App-Übersicht mit anderen Apps → gelöscht |
| 23.11.2026 | 21:15–22:15 | 1:00 | 3 | Profil in fünf Gruppen nach Video-Vorschlag der Nutzerin (#77): Sprache & Darstellung, Meine Gesundheit, App-Info als Kästchen und Zeilen, Allergien auf eigener Seite, Speichern erst nach Änderung | Video ohne ffmpeg: Einzelbilder per Edge aus einer HTML-Seite gezogen |
| 23.11.2026 | 22:15–22:25 | 0:10 | Doku | Zeitprotokoll und PROGRESS für den 23.11. | – |
---

## Erkenntnisse
- **Größter Zeitfresser bisher:** Werkzeug-Setup (Android Studio, Git-Auth, Gerät/USB), nicht der Code.
- **Room-Schema** ging schnell (ca. 21 min inkl. Fehlerbehebung), weil TRD und Coding-Richtlinien klar waren.
- **Analyse-Engine** in ca. 12 min, weil TRD Kap. 2 die Regeln fast vollständig vorgibt. Offene Fachfragen sind als Annahmen dokumentiert statt geblockt.
- **Phase 2 und 3 in ca. 1:30 h** (17.11.), weil Claude die Schritte ohne Rückfragen hintereinander gebaut, getestet und nach `develop` gemergt hat. Der meiste Aufwand steckte in der Prüfung im Emulator (Starten, Tippen, Datenbank auslesen), nicht im Code.
- **Zeiten ab dem 16.11. abends** stammen aus Git-Commits und dem Reflog; der Beginn eines Blocks ist daher der erste Git-Schritt, nicht die erste Nachricht.
- **18.11. (Self-Test-Rückmeldungen) in ca. 2:56 h:** 14 Notizen plus 6 Nachträge aus dem Testen umgesetzt, 5 Datenbank-Migrationen, jede mit Upgrade-Test im Emulator. Kurze Runden "bauen → im Emulator prüfen → aufs Handy" haben Fehler früh gezeigt (z.B. Butter hinter der Tastatur).
- **Absturz kostete nur ca. 7 min**, weil Git-Objekte heil blieben und nur die zuletzt geschriebenen Dateien betroffen waren. Lehre: vor riskanten Schritten an echten Daten immer zuerst sichern (so beim Zusammenführen der doppelten Zutaten gemacht).
- **19.11. in ca. 4:24 h, 20 PRs:** fast nur Design-Rückmeldungen aus dem Handy-Test. Viel Zeit ging in Feinschliff bei Schriftgröße 125 % (Kacheltexte, Chips, Tageszeile), weil jede Runde erst auf dem Handy sichtbar wurde. Die Zeiten sind nachträglich aus Git-Commits und Reflog rekonstruiert, die Hindernisse aus Commit-Texten und Code-Kommentaren.
- **20.11. in ca. 2:48 h, 14 PRs:** Die Verschlüsselung war der größte Block (ca. 1:07 h inkl. Handy-Test), weil es für Room 3 keinen fertigen SQLCipher-Treiber gibt. Der Fehler bei der Umstellung zeigte sich erst auf dem echten Gerät; die Regel „alte Datei erst nach geprüfter Kopie löschen“ hat die Daten geschützt.
- **Handy-Screenshots schlagen Annahmen:** Der Abgleich Screen für Screen hat zwei echte Fehler bei Schriftgröße 125 % gefunden (abgeschnittenes „Datum“, fehlende Sterne), die in Tests und Code-Review nicht auffallen.
- **21.11. in ca. 3:45 h, 2 PRs (#41, #42) mit über 30 Commits:** fast nur Startseite. Am Anfang kosteten geschätzte Farben und Größen viele Runden; schneller wurde es erst durch Messen im Bild und Bild-neben-Handy-Vergleiche. Eigene Bilder der Nutzerin (Hintergrund) waren am Ende besser als gezeichnete Blätter. Der Lesemodus des Handys hatte Farbvergleiche verfälscht.
- **21.11. Nachmittag/Abend und 22.11. in ca. 4:25 h, 16 PRs (#44–#60):** Kleine, klar beschriebene Testnotizen gingen schnell (oft 3–5 min pro PR). Länger dauerten das Icon (viele Feinrunden nach Augenmaß) und die Sprachen (Übersetzung von rund 600 Texten). Französisch und Arabisch brauchen noch eine Durchsicht durch Muttersprachler.
- **Der Stil-PR (#60) kostete am 22.11. abends ca. 1:25 h:** Bilder der Nutzerin als Vorlage, Handy-Screenshots nach jeder Runde. Am meisten Zeit gingen in kleine Dinge bei Schrift 125 % (Mahlzeiten-Pillen, durchscheinende Blätter). Eine zentrale Schriftskala und gemeinsame Auswahl-Pillen machen die nächsten Masken einheitlich, ohne jede einzeln anzufassen.
- **23.11. in ca. 5:05 h, 17 PRs (#61–#77):** fast nur Design nach Bildern und einem Video der Nutzerin. Schnell ging es, wenn auf jede Änderung sofort ein Handy-Screenshot folgte. Mehrere Runden kosteten Dinge, die nur bei Schrift 125 % sichtbar sind (Icons in Chips, Datum im Verlauf, Datums-Chip). Die Nutzerin hat zwischendurch #42–#72 nach develop gemergt.
