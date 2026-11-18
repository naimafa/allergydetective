# Self-Test (Phase 4)

**Ziel:** Die App 7 Tage lang selbst nutzen. Danach wissen wir, ob das Loggen schnell genug ist, ob der Bericht verständlich ist und ob die Schwellenwerte der Analyse passen ([ROADMAP](ROADMAP_Entwicklung.md), Phase 4).

**Stand der App:** Branch `develop` (Phase 1–3 plus Export). Debug-Build, Daten bleiben auf dem Handy.

---

## 1. Installation auf dem Handy
1. Handy per USB verbinden, USB-Debugging ist an (wie beim ersten Test).
2. Im Projektordner auf `develop`:
   ```bash
   ./gradlew :androidApp:installDebug
   ```
   Alternativ in Android Studio die Run-Konfiguration `androidApp` auf dem Handy starten.
3. App öffnen → Onboarding durchgehen, bekannte Allergien angeben (oder keine).
4. **Profil → Abend-Erinnerung einschalten** und die Uhrzeit wählen. Benachrichtigungen erlauben.

> Die App hat Auto-Backup ausgeschaltet. Bei Deinstallation sind die Einträge weg. Vor einer Neuinstallation also erst exportieren (siehe 4.).

## 2. Täglicher Ablauf
| Wann | Was | Ziel |
| :--- | :--- | :--- |
| Nach jeder Mahlzeit (oder gesammelt) | **Essen loggen**: Favorit, Zuletzt, Suche oder 🎤 | < 15 Sekunden pro Mahlzeit |
| Bei Beschwerden | **Symptom melden**, Startzeit ggf. über "Andere Zeit …" | Lange drücken = Schnell-Log |
| Abends (Erinnerung) | **Tages-Abschluss** mit "Keine Beschwerden" oder "Alles eingetragen" | Nur bestätigte Tage zählen |

Vergessen? Mahlzeiten und Symptome lassen sich bis vorgestern nachtragen, den Tages-Abschluss ebenso.

## 3. Notizen während des Tests
Kurz notieren (Handy-Notiz reicht), am besten mit Datum:
- **Was nervt?** Zu viele Tipps, fehlende Zutat, falsche Suche, unklare Texte.
- **Wie lange dauert das Loggen?** Grobe Schätzung pro Mahlzeit.
- **Fehlende Zutaten / Synonyme:** z.B. "Semmelknödel nicht gefunden".
- **Spracheingabe:** Funktioniert die Erkennung? Was wurde falsch zugeordnet? (Im Emulator nicht testbar.)
- **Erinnerung:** Kam sie pünktlich? Kam sie, obwohl der Tag schon bestätigt war?
- **Abstürze** mit Uhrzeit und was du gerade gemacht hast.

## 4. Nach 7 Tagen: Auswertung
1. **Deine Spuren** öffnen: Ist der Bericht verständlich? Stimmen die Begründungen mit deinem Gefühl überein?
2. **Profil → Als JSON exportieren** und die Datei z.B. per Mail an dich selbst oder in Drive ablegen.
   Der Export enthält Einträge mit stabilen Schlüsseln (ohne Notizen) und für jeden Kandidaten Lift, n₁/k₁/n₀/k₀ und die Stufe.
3. Die JSON-Datei ins Projekt legen (z.B. `docs/selftest/`, **nicht** committen, siehe `.gitignore`) und mir zusammen mit deinen Notizen geben.

## 5. Was danach kalibriert wird
Alle Werte stehen in `AnalysisConfig`:

| Schwellenwert | Startwert | Frage an die echten Daten |
| :--- | :--- | :--- |
| Episoden zusammenfassen | 3 h | Werden zusammengehörige Beschwerden getrennt gezählt? |
| Gruppierung | 80 % | Tauchen sinnvolle oder unsinnige Gruppen auf? |
| Spur / Verdacht / Starker Verdacht | Lift 1,5 / 2 / 2,5 | Zu viele Fehlalarme oder zu streng? |
| Mindest-Expositionen | n₁ ≥ 3 | Reicht eine Woche für erste Spuren? |
| Sicher-Liste | n₁ ≥ 3, k₁ = 0 | Landen typische Alltagszutaten dort? |
| Zutaten ohne Trigger | nicht analysiert | Fehlen wichtige Lebensmittel (z.B. Reis) im Bericht? |

## 6. Bekannte Grenzen während des Tests
- Nur Android; iOS ist ohne Mac nicht gebaut.
- Einträge in der Historie lassen sich löschen, nicht bearbeiten.
- Notrufnummer: 112 (EU), 911 (US/CA), 000 (AU); andere Länder noch nicht geprüft.
- Die erste aussagekräftige Auswertung kommt nach 7 **bestätigten** Tagen.
