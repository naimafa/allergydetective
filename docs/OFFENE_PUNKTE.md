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

## ⏳ Später entscheiden
- [ ] **Verschlüsselung** (SQLCipher), verschlüsselter Export/Import, App-Sperre: beim Thema Datenschutz besprechen. Bis dahin das Wort "verschlüsselt" **nicht** im Store-Text verwenden.
- [ ] **App-Name** ("Allergie" vs. breiter, z.B. Bauch-/Spurensucher)
- [ ] **Package / App-ID** (aktuell `com.nfar.allergydetective`): spätestens vor dem ersten Upload in die Play Console entscheiden. Die `applicationId` lässt sich unabhängig von den Kotlin-Packages ändern.
- [ ] **Premium-Idee "Detektiv-Experimente"** (Weglassen und Wiedereinführen begleiten)

## 🔍 Vor Release prüfen
- [ ] **Zeitfenster und Trigger-Zuordnungen** fachlich verifizieren (Literatur / Fachperson)
- [ ] **Schwellenwerte kalibrieren** (Episoden 3h, Gruppierung 80 %, Lift-Grenzen) mit Testdaten und Self-Test
- [ ] **MDR-Einordnung** fachlich prüfen lassen: Formulierungen wie "Tagebuch / Muster erkennen" statt "Diagnose", Hinweis "kein Ersatz für ärztliche Diagnose"
- [ ] **DSGVO:** Gesundheitsdaten sind besondere Kategorie (Art. 9). Local-First hilft, trotzdem Datenschutzerklärung und Einwilligung prüfen
- [ ] **Lizenz**, falls externe Lebensmitteldaten genutzt werden

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
- [ ] **Übersetzungen** aller App-Texte, Symptome, Trigger und Zutaten. Notfall-Hinweis und Disclaimer von Muttersprachlern prüfen lassen.
- [ ] **Zutatenliste regional erweitern:** Die Startliste ist deutsch geprägt (z.B. Sauerkraut). Für FR/AR-Nutzer typische Zutaten der französischen und nordafrikanischen Küche ergänzen.
- [ ] **Store-Auftritt und ASO-Keywords** pro Sprache
