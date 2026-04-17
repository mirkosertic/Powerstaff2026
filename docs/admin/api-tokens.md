# LLM API-Token konfigurieren

> **Hinweis:** Nur Administratoren können API-Tokens einsehen und verwalten.

Der LLM API-Token wird von der **KI-Profilsuche** (Chat-Modus) benötigt, um Anfragen
an das Sprachmodell zu stellen. Ohne gültigen Token ist der Chat-Modus nicht nutzbar.

---

## Token setzen

1. Klicken Sie in der Navigation auf **Administration** → Tab **Benutzer**
2. Klicken Sie auf das Stift-Symbol **✎** neben dem gewünschten Benutzer
3. Tragen Sie den **LLM API-Token** ein
4. Klicken Sie auf **Speichern**

Der Token wird verschlüsselt gespeichert und ist in der Tabelle nur als **✔ Gesetzt** sichtbar –
der tatsächliche Wert wird nie angezeigt.

---

## Token löschen

Öffnen Sie den Bearbeiten-Dialog und leeren Sie das Token-Feld.

---

## Hinweise

- Der Token gilt pro Benutzer – jeder Sachbearbeiter kann einen eigenen Token haben
- Wenn kein Token gesetzt ist, erscheint im Chat-Modus eine Fehlermeldung
- Der Token-Wert ist im DOM nicht sichtbar (kein Auslesen durch Dritte möglich)
