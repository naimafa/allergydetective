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

## 🧩 Phase 2: bewusst noch nicht gebaut (Stand 17.11.2026)
- [ ] **Favoriten** (Mahlzeit als Vorlage speichern, Snapshot in `meal_ingredients`) und **"Neue Mahlzeit" mit Namen**
- [ ] **Eigene Zutaten** anlegen ("ähnlich wie …", Trigger übernehmen)
- [ ] **Spracheingabe** (SpeechRecognizer offline, Matching wie die Suche)
- [ ] **Tab-Bar** (Home, Historie, Favoriten, Profil) und **Historie** zum Bearbeiten/Löschen alter Einträge
- [ ] **Abend-Reminder** 20:00 (WorkManager / Notification) und **Onboarding** mit bekannten Allergien
- [ ] **Zeit frei wählen:** Mahlzeiten und Symptome haben nur Chips (jetzt, vor 30 min … 5 h). Ein echter Zeit-Slider fehlt noch.
- [ ] **Notrufnummer** ist fest 112 (EU). Für andere Länder (z.B. Marokko 15/150) je Region festlegen.
- [ ] **Zahlenformat:** Schlafstunden zeigen "6.5" statt "6,5"; Formatierung über die Locale folgt mit dem Design.
- [ ] **Android Auto-Backup** ist noch an (`allowBackup="true"`), TRD 3.5 verlangt aus. Vor dem ersten Test mit echten Daten umstellen.
- [ ] **Begründung im Ergebnis** zeigt nur "X von Y Mal". Der Vergleich "sonst 1 von 9" (UX 5) braucht ungewichtete n₀/k₀ aus der Engine.
- [ ] **GitHub-PRs:** Die Phase-2-Branches wurden lokal nach `develop` gemergt, weil auf dem Rechner kein `gh` installiert ist. Für echte PRs: GitHub CLI installieren und einmal `gh auth login` ausführen.

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
- [ ] **Pool mit 30–50 Motivations-Sprüchen**
- [ ] **Abgeschnittene Texte ergänzen:** PRD Phase 2 ("Review-Mining: Automatisi…"), UX Kap. 6 (Erfolgsanimation)
- [ ] **PRD:** Zielgruppe/Persona, Erfolgskennzahlen (z.B. % Nutzer, die 7 Tage durchhalten), Nicht-Ziele des MVP
- [ ] **Store-Text** an Zweckbestimmung und Datenschutz-Entscheidung angleichen
- [ ] **i18n (EN/DE/FR/AR):** TRD in Phase 1 anpassen (stabile `key`-Spalten in den Stammtabellen, `locale` bei `ingredient_synonyms`, Anzeigenamen aus den Ressourcen)
- [ ] **Übersetzungen** aller App-Texte, Symptome, Trigger und Zutaten. Stand 16.11.: Anzeigenamen der Seed-Daten in EN und DE (`seed_strings.xml`), FR und AR fehlen. Notfall-Hinweis und Disclaimer von Muttersprachlern prüfen lassen.
- [ ] **Zutatenliste regional erweitern:** Die Startliste ist deutsch geprägt (z.B. Sauerkraut). Für FR/AR-Nutzer typische Zutaten der französischen und nordafrikanischen Küche ergänzen.
- [ ] **Store-Auftritt und ASO-Keywords** pro Sprache
