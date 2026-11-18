# Coding-Richtlinien: Allergie-Detektiv

**Grundregel:** Best Practices und Design Patterns anwenden, **sobald sie sinnvoll sind**. Kein Pattern um seiner selbst willen (KISS, YAGNI). Der Code soll lesbar, testbar und plattformunabhängig bleiben (siehe [ARCHITEKTUR.md](ARCHITEKTUR.md)).

---

## 1. Architektur-Patterns

| Pattern | Wo | Warum |
| :--- | :--- | :--- |
| **Schichten (Clean Architecture light)** | `ui` → `domain` ← `data` | Die Analyse-Engine kennt weder UI noch Datenbank |
| **MVVM + Unidirectional Data Flow** | Screens | ViewModel hält einen unveränderlichen `UiState` (StateFlow), die UI schickt Events |
| **Repository** | `data` | Einziger Zugang zu Room. Das ViewModel kennt nur Interfaces. |
| **Use Case** | `domain` | z.B. `LogMealUseCase`, `RunAnalysisUseCase`. Nur einführen, wenn Logik über reines Weiterreichen hinausgeht. |
| **Dependency Injection** | überall | **Koin** (KMP-tauglich, leichtgewichtig) |
| **Plattform-Adapter** | `platform` | Interface in `commonMain`, Umsetzung in `androidMain` / `iosMain` (`expect/actual` nur für einfache Fälle) |
| **Strategy** | Analyse-Engine | Austauschbare Regeln (z.B. Evidenzstufen, Episoden-Bildung), wenn sie kalibriert werden |

## 2. Analyse-Engine
* **Reine Funktionen:** Eingabe = Daten, Ausgabe = Ergebnis. Keine Seiteneffekte, kein Datenbankzugriff in der Engine.
* **Test-first:** Unit-Tests mit synthetischen Daten **vor** der Implementierung (TRD Kapitel 4).
* **Schwellenwerte zentral** in einem `AnalysisConfig`-Objekt, nicht im Code verstreut.

## 3. Kotlin-Konventionen
* **Unveränderlich:** `data class` mit `val`. Zustände und Ergebnisse als `sealed interface` (z.B. `EvidenceLevel`, `UiState`).
* **Fehlerbehandlung:** `Result` bzw. eigene `sealed`-Typen statt Exceptions über Schichtgrenzen.
* **Zeit:** `kotlinx-datetime`, intern immer UTC. Zeitzone nur für die Anzeige.
* **Coroutines:** `Dispatchers` per Injection, nie hart codiert (testbar).
* **Sprache:** Die Implementierungssprache ist **Englisch**: Klassen, Funktionen, Variablen, Kommentare, Tests, Commit-Messages sowie Datenbank- und JSON-Schlüssel (z.B. `histamine` statt `Histamin`). Die Doku in `docs/` darf Deutsch sein. Sichtbare App-Texte und Anzeigenamen stehen in den String-Ressourcen, nie fest im Code.

## 4. Projekt-Hygiene
* **Abhängigkeiten** zentral im Version Catalog (`gradle/libs.versions.toml`).
* **Room:** Schema-Export ab Version 1 aktivieren, jede Änderung als Migration.
* **Seed-Daten** (Zutaten, Trigger, Symptome) als JSON in den `commonMain`-Ressourcen, nicht im Code.
* **Texte** in Compose-Resources (`strings.xml`), keine festen Strings im UI. Damit sind weitere Sprachen später einfach.
* **Kleine Dateien:** eine Verantwortung pro Datei, Richtwert ≤ 300 Zeilen. Gut für Menschen **und** für die KI, die dann weniger Kontext braucht.
* **Statische Analyse:** ktlint + detekt, sobald das Grundgerüst steht.

## 5. Git
* **Kleine Commits** nach Conventional Commits: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`
* `main` bleibt lauffähig. Größere Features im eigenen Branch.
* **`develop` ist der Integrations-Branch** (seit 17.11.2026): Jeder Schritt bekommt einen Branch `feature/<name>` von `develop` und wird nach `develop` gemergt. Nach `main` geht es erst nach Freigabe.
* Nach jedem Meilenstein [PROGRESS.md](PROGRESS.md) aktualisieren.

## 6. Internationalisierung (i18n)
* **Sprachen:** Englisch (Fallback), Deutsch, Französisch, Arabisch. Die Sprache folgt der Systemsprache und ist später in den Einstellungen wählbar.
* **Ressourcen:** Alle sichtbaren Texte stehen in Compose-Resources (`values/`, `values-de/`, `values-fr/`, `values-ar/`). Keine festen Strings und keine zusammengeklebten Satzteile, denn Satzbau und Plural unterscheiden sich je Sprache. Platzhalter und Plural-Ressourcen nutzen.
* **Datenbank:** Nur stabile englische Schlüssel (`key`, z.B. `histamine`, `wheat_bread`), keine Anzeigenamen. Die Anzeigenamen kommen aus den Ressourcen. Synonyme für die Spracheingabe werden pro Sprache (`locale`) gepflegt.
* **Arabisch / RTL:** Das Layout spiegelt sich. `start`/`end` statt `left`/`right`, Richtungs-Icons spiegeln, Zahlen und Datum über die Locale formatieren statt selbst bauen. Arabisch früh testen, nicht erst am Ende.
* **Spracheingabe:** Die Erkennungssprache entspricht der App-Sprache.
* **Texte mit Rechtsbezug** (Zweckbestimmung, Notfall-Hinweis, Disclaimer) in allen Sprachen gleichwertig halten und von Muttersprachlern prüfen lassen.
* **Platz einplanen:** Deutsche und französische Texte sind länger. Keine festen Breiten in Layouts.
