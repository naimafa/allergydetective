# UX/UI Design Document: Allergie-Detektiv

**Version:** 1.2 (Fokus: User-Friendly & Low-Friction)
**Leitprinzip:** "5-Sekunden-Regel" – Jede Eingabe muss blitzschnell und ohne Hürden funktionieren.
**Design-Stil:** Clean, modern, beruhigend (Pastell-Töne), weg vom medizinischen "Krankenhaus-Look".

---

## 1. Design-Sprache & Atmosphäre

* **Farbpalette:**
    * **Primär:** Soft Petrol (`#3A7D8E`) – Vermittelt Ruhe und Vertrauen.
    * **Sekundär:** Salbeigrün (`#84A59D`) – Für "Sichere" Lebensmittel und Erfolgserlebnisse.
    * **Akzent:** Sanftes Koralle (`#F28482`) – Für Symptome (auffällig, aber nicht aggressiv).
* **Typografie:** Moderne Sans-Serif (z.B. *Inter* oder *Montserrat*). Große Klickflächen für mobile Nutzung.
* **Icons:** Abgerundete, freundliche Line-Icons (nicht zu technisch).

---

## 2. Dashboard (Home Screen)

Das Dashboard ist der zentrale Ankerpunkt. Es soll den Nutzer motivieren, statt ihn mit Daten zu erschlagen.

* **Header-Bereich – Daily Quote:** Ein zufälliger Satz aus einem lokalen Pool (ca. 50 Sätze), der rotiert (z.B. *"Jeder Eintrag ist ein Puzzleteil zu deinem Wohlbefinden."*).
* **Der "Detektiv-Fortschritt":** Ein kreisförmiger Balken zeigt den Status der 7-Tage-Analyse an (z.B. *"Tag 4 von 7 – Wir sammeln Spuren"*).
* **Quick-Action-Buttons:** Drei große, zentral platzierte Kacheln:
    1. 🍎 **Essen loggen** (Öffnet Favoriten/Suche)
    2. ⚡ **Symptom melden** (Sofort-Eingabe mit Zeit-Slider)
    3. 🌙 **Lifestyle-Check** (Schlaf & Stress)

---

## 3. User Flows (Interaktions-Designs)

### 3.1 Voice-In & Favoriten (Keyword-Matching)
Da keine Cloud-KI genutzt wird, basiert die Spracheingabe auf **lokalem Wort-Matching**:
1. User drückt Mikrofon-Icon und sagt z.B. *"Spinat Pizza"*.
2. System-Spracherkennung (Offline) wandelt Ton in Text um.
3. Die App sucht in der lokalen Favoriten-Datenbank nach Übereinstimmungen.
4. **Ergebnis:** Falls gefunden, wird die Pizza mit allen Einzelzutaten (Teig, Käse, Spinat) sofort geloggt. Falls nicht, erfolgt die Frage: *"Neu anlegen?"*.

### 3.2 Symptom-Eingabe (Präzision ohne Stress)
* **Default-Zeit:** Die App schlägt immer "Jetzt" vor.
* **Zeit-Slider:** Ein intuitiver horizontaler Balken ermöglicht das Verschieben der Zeit (z.B. *"Vor 30 Minuten"*), falls das Symptom verzögert eingetragen wird.
* **Intensität:** Ein vertikaler Schieberegler von 1 bis 10 mit haptischem Feedback (Vibration).
* Symptom-Grid und Detail-Overlay: siehe [CONTENT_Symptome.md](CONTENT_Symptome.md)

### 3.3 Lifestyle-Slider (Stress & Schlaf)
* **Stress-Barometer:** Ein Slider, der sich farblich von Grün (entspannt) nach Orange (gestresst) verändert.
* **Schlaf-Rating:** Auswahl der Stunden und Qualität (1–5 Sterne) über einfache Icons.

---

## 4. Intelligentes Benachrichtigungs-System

Um den Nutzer nicht zu überlasten ("Notification Fatigue"), reduzieren wir die Push-Nachrichten auf das Minimum:

* **Der Abend-Reminder (ca. 20:00 Uhr):** Eine einzige, freundliche Nachricht: *"Detektiv-Stunde: Hast du heute alles notiert oder fehlt noch ein Puzzleteil? 🕵️‍♂️"*
* **Kein Follow-Up:** Es gibt keine automatischen Abfragen nach dem Essen, um den User nicht zu gängeln.
* **Belohnungs-System:** Bei 5 Tagen konsequentem Logging erscheint eine kleine Animation: *"Meister-Ermittler Status erreicht!"*.

---

## 5. Die Analyse-Präsentation (Aha-Moment)

Nach 7 Tagen wird das Ergebnis visualisiert:

* **Ranking:** "Top-Verdächtige" werden mit Wahrscheinlichkeitsbalken angezeigt.
* **Transparenz:** Kurze Hinweise erklären das Ergebnis (z.B. *"Trat 3x nach Weizen auf, trotz gutem Schlaf."*).
* **Datenschutz-Icon:** Ein permanentes Icon zeigt an: *"Diese Analyse wurde zu 100% lokal auf deinem Handy erstellt."*

---

## 6. UI-Spezifikationen für Entwickler

* **Navigation:** Tab-Bar unten (Home, Historie, Favoriten, Profil).
* **Feedback:** Jede erfolgreiche Speicherung wird mit einer kurzen Animatio… *(Text im Original abgeschnitten – bitte ergänzen)*
