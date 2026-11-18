# Offene Punkte & Entscheidungen

## ✅ Entschieden (Stand 14.11.2026)
- [x] **Score-Formel:** Lift-Vergleich "mit vs. ohne" statt Summen-Score (TRD 2.4)
- [x] **Zutaten-Datenbank** mit Trigger-Zuordnung, 12 Basis-Kategorien definiert ([CONTENT_Zutaten.md](CONTENT_Zutaten.md))
- [x] **Tages-Abschluss** mit Wohlbefinden + Bestätigung "Keine Beschwerden / Alles eingetragen" (UX 3.4, TRD 3.3)
- [x] **Notfall-Hinweis** bei Atemnot / Schwellung (Symptome Kap. 3)
- [x] **Zweckbestimmung "Selbstbeobachtung"**, Wortwahl festgelegt (PRD 1.1)
- [x] **Zeitfenster pro Trigger**, vorläufig nach eigenem Wissen (Zutaten Kap. 2)
- [x] **Portionsgröße S/M/L** mit Faktor 0,5 / 1 / 2 und Hand-Maß (TRD 1.4)
- [x] **Schlaf & Stress als eigene Kandidaten** statt 30-%-Abzug (TRD 2.5)
- [x] **Farben:** Text-Varianten mit ausreichendem Kontrast (UX 1.1)
- [x] **Essen-Loggen vereinfacht:** "Wie gestern?", Zeit-Slider, Portion-Chips (UX 3.1)
- [x] **Symptom-Katalog erweitert** (Durchfall/Verstopfung getrennt, Mund, Augen, Herzrasen, Sonstiges)
- [x] **Schlaf-Abfrage** im abendlichen Tages-Abschluss, kein Morgen-Push
- [x] **Intensität 1–5** (bestätigt)
- [x] **Stress 1–5** (Slider rastet ein)
- [x] **Plattform:** Ziel ist Android + iOS aus einer Codebasis (Kotlin Multiplatform). Jetzt wird nur unter Windows entwickelt und für Android getestet ([ARCHITEKTUR.md](ARCHITEKTUR.md)).
- [x] **Alkohol nur als Trigger** (Getränke), nicht als Lifestyle-Faktor (16.11.2026)
- [x] **minSdk 26 (Android 8.0), targetSdk 37 (Android 17):** große Reichweite, auch ältere Geräte (wichtig für FR/AR-Zielgruppe). Eigenes Testgerät: Android 13.

## ⏳ Später entscheiden
- [ ] **Verschlüsselung** (SQLCipher), verschlüsselter Export/Import, App-Sperre: beim Thema Datenschutz besprechen. Bis dahin das Wort "verschlüsselt" **nicht** im Store-Text verwenden.
- [ ] **App-Name** ("Allergie" vs. breiter, z.B. Bauch-/Spurensucher)
- [ ] **Package / App-ID** (aktuell `com.nfar.allergydetective`): spätestens vor dem ersten Upload in die Play Console entscheiden. Die `applicationId` lässt sich unabhängig von den Kotlin-Packages ändern.
- [ ] **Premium-Idee "Detektiv-Experimente"** (Weglassen und Wiedereinführen begleiten)

## 🧩 Phase 2: offene Punkte (Stand 17.11.2026)
Erledigt am 17.11.2026, je ein Branch nach `develop`:
- [x] **Auto-Backup aus** (`allowBackup="false"`, `dataExtractionRules`), TRD 3.5
- [x] **Zeit frei wählen:** "Andere Zeit …" mit Tag (bis vorgestern) und Uhrzeit, nie in der Zukunft
- [x] **Favoriten:** als Favorit speichern, Favoriten-Zeile im Essen-Loggen, `favoriteId` als Herkunft der Mahlzeit
- [x] **Eigene Zutaten:** Name, Kategorie, "ähnlich wie …" mit übernommenen Triggern; IDs ab 100000
- [x] **Onboarding:** Zweckbestimmung + bekannte Allergien beim ersten Start
- [x] **Tab-Bar** (Start, Historie, Favoriten, Profil); Historie der letzten 14 Tage mit Löschen; Profil mit Allergien
- [x] **Abend-Reminder:** WorkManager, Opt-in im Profil (19–22 Uhr), entfällt bei bestätigtem Tag, Tap öffnet den Tages-Abschluss
- [x] **Begründung im Ergebnis** mit Vergleich ("sonst X von Y"), neue ungewichtete Zähler in der Engine
- [x] **Notrufnummer** je Region (112 / 911 / 000), **Dezimalkomma** und Datum je Sprache
- [x] **Spracheingabe:** Mikrofon im Essen-Loggen (System-Erkennung, offline bevorzugt), Abgleich Favoriten → Synonyme → Zutaten, zusammengesetzte Wörter, Tippfehler

Noch offen bzw. Annahmen:
- [ ] **Spracheingabe auf echtem Gerät testen.** Im Emulator öffnet sich die Google-Erkennung, meldet aber "Voice search isn't available". Der Abgleich ist per Unit-Test abgedeckt.
- [ ] **Notrufnummer Marokko** (und weitere Zielländer) fachlich klären; bis dahin gilt dort 112.
- [ ] **Historie:** Einträge lassen sich nur löschen, nicht bearbeiten (Löschen + neu eintragen).
- [ ] **Reminder-Zeit** nur als Chips 19–22 Uhr; freie Uhrzeit folgt mit dem Design.
- [ ] **iOS:** Reminder-Planung, Benachrichtigungs-Erlaubnis und Spracheingabe sind nur Platzhalter (kein Mac).
- [ ] **Onboarding** fragt den Reminder nicht ab; er ist aus, bis man ihn im Profil einschaltet.
- [x] **GitHub-PRs:** `gh` ist eingerichtet (18.11.2026); Änderungen gehen per Pull Request nach `main` (PR #5)

## 🧪 Self-Test-Feedback (Stand 18.11.2026)
Bugs, erledigt am 18.11.2026 (je ein Branch nach `develop`):
- [x] **Speichern-Button im Essen-Loggen** war halb von der System-Navigationsleiste verdeckt (Edge-to-Edge)
- [x] **JSON-Export** schlug bei WhatsApp fehl ("Teilen fehlgeschlagen"): wird jetzt als Datei-Anhang geteilt (FileProvider)
- [x] **Tages-Abschluss bestätigte den falschen Tag:** Er wählte den ältesten offenen Tag vor (15.11. statt 17.11.). Jetzt ist immer "Heute" vorgewählt, das Datum steht groß da, offene Vortage werden nur als Hinweis genannt
- [x] **"Dinkel Vollkornbrot"** wurde zu Dinkelbrot + Vollkornbrot (Weizen): neue Zutat Dinkelvollkornbrot (Migration DB 1 → 2, im Emulator v1 → v2 geprüft), Spracheingabe prüft getrennte Wörter auch als ein Kompositum

Noch offen (Entscheidung bzw. Konzept nötig):
- [x] **Kategorie Öle, Fette & Oliven** (ID 13) mit 9 Zutaten, Migration DB 2 → 3
- [x] **Gerichte aus Zutaten:** "Neue Zutat" fragt nur noch "Was ist drin?" (Zutaten aus der Liste), Kategorie und Trigger übernimmt die App. Kategorie nur, wenn nichts passt. Neu anlegen erscheint als "Nicht dabei? …" unter den Suchtreffern
- [x] **Favoriten mit "+ Neues Gericht"** (z.B. Linsensuppe = Linsen, Tomaten, Karotten), Portion M
- [x] **Mahlzeit statt Uhrzeit:** Frühstück / Mittag- / Abendessen / Snack (ENTSCHEIDUNGEN 2)
- [x] **Symptome nach Tageszeit** (morgens / mittags / nachmittags / abends / ganzer Tag / nach dem Essen), Uhrzeiten in ENTSCHEIDUNGEN 2
- [x] **Intensität pro Symptom:** gibt es schon (1–5 beim Antippen). Nur langes Drücken speichert ohne Abfrage mit 3
- [x] **Kranke Tage markieren:** Faktor "Krank" im Tages-Abschluss, der Tag fällt aus der Analyse (Migration DB 3 → 4)
- [x] **Krankheitszeitraum** von–bis oder "noch krank", auch weiter zurück; alle Tage darin zählen nicht (Migration DB 5 → 6)
- [x] **Einführung beim ersten Start:** 5 Seiten (Willkommen, So funktioniert es, Geduld, Allergien, Tipp Gerichte). "Gerichte jetzt anlegen" öffnet den Tab Favoriten. Im Profil über "Einführung ansehen" jederzeit wieder (ohne Allergie-Seite)
- [x] **Roh / gekocht** (Pflicht) bei Zutaten mit Birkenpollen-Kreuzreaktion: Karotte, Sellerie, Apfel, Birne, Kirsche, Pfirsich, Pflaume, Haselnuss. Gekocht = Birken-Trigger zählt nicht. Bisherige Einträge gelten als roh (Migration DB 4 → 5)
- [x] **Gerichte:** fehlende Zutat direkt anlegen; gleicher Name eines Gerichts wird ersetzt (mit Hinweis). Gerichte lassen sich bearbeiten (Zutaten ergänzen/entfernen, Name, roh/gekocht); die Id bleibt, eingetragene Mahlzeiten behalten ihre Herkunft
- [x] **Keine Doppelten:** gleicher Name / Synonym sperrt das Anlegen ("Diese Zutat nehmen"), ähnlicher Name (Tippfehler, Teil eines Namens) fragt einmal nach
- [x] **Doppelte zusammenführen:** Beim App-Start wird eine eigene Zutat mit dem Namen einer mitgelieferten Zutat (in der App-Sprache) zusammengeführt (eigenes Dinkelvollkornbrot, 18.11.2026 nach Rückfrage)

## 🔍 Vor Release prüfen
- [ ] **Zeitfenster und Trigger-Zuordnungen** fachlich verifizieren (Literatur / Fachperson)
- [ ] **Schwellenwerte kalibrieren** (Episoden 3h, Gruppierung 80 %, Lift-Grenzen) mit Testdaten und Self-Test
- [ ] **MDR-Einordnung** fachlich prüfen lassen: Formulierungen wie "Tagebuch / Muster erkennen" statt "Diagnose", Hinweis "kein Ersatz für ärztliche Diagnose"
- [ ] **DSGVO:** Gesundheitsdaten sind besondere Kategorie (Art. 9). Local-First hilft, trotzdem Datenschutzerklärung und Einwilligung prüfen
- [ ] **Lizenz**, falls externe Lebensmitteldaten genutzt werden
- [ ] **ID-Bereich für eigene Zutaten:** Seed-Zutaten haben feste IDs (Kategorie × 100 + n). Eigene Zutaten bekommen per AUTOINCREMENT die nächste freie ID und könnten mit später nachgelieferten Seed-Zutaten kollidieren. Vor dem ersten Release lösen (z.B. eigene Zutaten ab 100000).

## 🧪 Analyse-Engine: Annahmen bestätigen (Stand 16.11.2026)
In der Engine umgesetzt, aber vom TRD nicht eindeutig vorgegeben. Alle Werte stehen in `AnalysisConfig`.
- [ ] **Zutaten ohne Trigger** (z.B. Reis, eigene Zutaten ohne Tags) haben kein Zeitfenster. Sie werden nicht analysiert und kommen nicht auf die Sicher-Liste. Alternative: Standardfenster (z.B. 0–24h, alle Symptomgruppen).
- [ ] **Gruppierung (TRD 2.7)**, umgesetzt mit diesen Annahmen:
  - **80 % in beide Richtungen:** Beide Kandidaten müssen ≥ 80 % ihrer Expositionen miteinander teilen. Milch immer zum Brot, aber Brot oft allein → unterscheidbar.
  - **Exposition** = Mahlzeit an einem gültigen Tag (Lifestyle: gültiger Tag vor heute). Die Reaktionsfenster spielen hier keine Rolle, sonst wären Gluten (48h) und Fruktane (12h) am Tagebuch-Ende scheinbar unterscheidbar.
  - **Zutat und Eigenschaft werden gemeinsam gruppiert** ("Milch + Laktose + Milcheiweiß"), solange es keine zweite Quelle gibt. Verträglicher Hartkäse löst Milcheiweiß aus der Gruppe.
  - **Nur Gruppen mit mindestens einer Spur** werden gemeldet. Der Partner darf unauffällig sein.
  - **Ketten** bilden eine Gruppe (A~B und B~C, auch wenn A und C nur 70 % teilen).
  - **Lebensmittel und Lifestyle** werden nicht miteinander verglichen (Mahlzeiten vs. Tage). Schlechter Schlaf + hoher Stress an denselben Tagen bilden eine Gruppe.
  - Das Ranking bleibt unverändert, die Gruppen stehen zusätzlich in `AnalysisResult.groups`. Die Darstellung entscheidet das UI.
- [ ] **Bekannte Allergie:** Nicht nur die Eigenschaft, sondern auch alle Zutaten mit dieser Eigenschaft fehlen im Ranking (Milch bei bekannter Laktose-Unverträglichkeit). Hinweis pro Mahlzeit über `knownAllergyExposures`.
- [ ] **Evidenzstufen** nutzen das portionsgewichtete n₁ (3 × S = 1,5 → noch "zu wenig Daten"). Die Sicher-Liste zählt ungewichtet.
- [ ] **"14 Tage stabil"** = der Verdacht bestand auch bei einer Analyse mit Stichtag vor 14 Tagen. Ob er zwischendurch weg war, wird nicht geprüft.
- [ ] **Episoden:** zusammengefasst wird relativ zum **Episodenstart** (keine endlosen Ketten bei Einträgen alle 2h).
- [ ] **Eigenschaft in einer Mahlzeit:** Enthalten mehrere Zutaten dieselbe Eigenschaft, zählt die größte Portion (keine Summe). Mahlzeiten ohne Kandidat zählen mit Faktor 1.
- [ ] **Treffer-Gewicht:** höchste Intensität aller passenden Episoden im Fenster.
- [ ] **Zeit:** Die Engine rechnet mit UTC-Millis + Offset; `kotlinx-datetime` ist noch nicht eingebunden (erst nötig, wenn das Repository Daten umrechnet).

## 🚀 Launch: Rechtliches & Organisation (Go-live Deutschland)
- [ ] **Rechtsform klären:** Eigene Apps verkaufen gilt meist als Gewerbe (nicht freiberuflich). Start als Einzelunternehmen ohne GmbH möglich
- [ ] **Kleinunternehmerregelung** prüfen (Gründungsjahr max. 25.000 € Umsatz)
- [ ] **Haftung:** Einzelunternehmer haftet mit Privatvermögen → zum Start IT-/Betriebshaftpflicht abschließen, bei Wachstum Wechsel in UG
- [ ] **Steuerberater-Gespräch** vor allen anderen Schritten
- [ ] **Gewerbeanmeldung**
- [ ] **ELSTER:** Fragebogen zur steuerlichen Erfassung
- [ ] **Arbeitgeber informieren** (falls nötig, Nebentätigkeit)
- [ ] **Geschäftskonto** eröffnen
- [ ] **Developer-Accounts** (Google Play, Apple): Händlerangaben und Adresse werden öffentlich angezeigt
- [ ] **Impressum + Datenschutzerklärung** (in App und Store-Eintrag)

## ✍️ Noch zu liefern
- [x] **Pool mit 30–50 Motivations-Sprüchen** (40 Sprüche EN/DE, 17.11.2026; FR/AR fehlen noch)
- [ ] **Abgeschnittene Texte ergänzen:** PRD Phase 2 ("Review-Mining: Automatisi…"), UX Kap. 6 (Erfolgsanimation)
- [ ] **PRD:** Zielgruppe/Persona, Erfolgskennzahlen (z.B. % Nutzer, die 7 Tage durchhalten), Nicht-Ziele des MVP
- [ ] **Store-Text** an Zweckbestimmung und Datenschutz-Entscheidung angleichen
- [ ] **i18n (EN/DE/FR/AR):** TRD in Phase 1 anpassen (stabile `key`-Spalten in den Stammtabellen, `locale` bei `ingredient_synonyms`, Anzeigenamen aus den Ressourcen)
- [ ] **Übersetzungen** aller App-Texte, Symptome, Trigger und Zutaten. Stand 16.11.: Anzeigenamen der Seed-Daten in EN und DE (`seed_strings.xml`), FR und AR fehlen. Notfall-Hinweis und Disclaimer von Muttersprachlern prüfen lassen.
- [ ] **Zutatenliste regional erweitern:** Die Startliste ist deutsch geprägt (z.B. Sauerkraut). Für FR/AR-Nutzer typische Zutaten der französischen und nordafrikanischen Küche ergänzen.
- [ ] **Store-Auftritt und ASO-Keywords** pro Sprache
