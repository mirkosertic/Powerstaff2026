# Spezifikation SSO-Login (zukünftige Version)

Dieses Dokument beschreibt die geplante SSO-Integration für eine spätere Version von Powerstaff 2026.
Release 1.0 verwendet formularbasiertes Login mit lokaler Benutzertabelle (vgl. SWARCHITEKTUR.md – Abschnitt 7).

> **Status:** Geplant – nicht Teil von Release 1.0.

## Zielarchitektur

Powerstaff soll langfristig über **OAuth2/OIDC** authentifiziert werden:

- **Produktion:** Microsoft Entra ID (Azure Active Directory)
- **Lokale Entwicklung:** Keycloak in Docker (OIDC-kompatibel, emuliert Entra ID-Verhalten)

Die Authentifizierung erfolgt vollständig über den konfigurierten Identity Provider. Es gibt keine eigene Passwortverwaltung mehr.

## Spring Security Konfiguration (Zielzustand)

Die OIDC-Konfiguration erfolgt ausschließlich über `application.yml`. Ein Profil-Switch (`spring.profiles.active=local` vs. `prod`) steuert den genutzten Provider.

```yaml
# Produktionskonfiguration (application-prod.yml)
spring:
  security:
    oauth2:
      client:
        registration:
          entra:
            client-id: ${AZURE_CLIENT_ID}
            client-secret: ${AZURE_CLIENT_SECRET}
            scope: openid, profile, email
        provider:
          entra:
            issuer-uri: https://login.microsoftonline.com/${AZURE_TENANT_ID}/v2.0

# Lokale Entwicklung (application-local.yml)
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: powerstaff-local
            client-secret: local-secret
            scope: openid, profile, email
        provider:
          keycloak:
            issuer-uri: http://localhost:9090/realms/powerstaff
```

## Benutzeridentität im Zielzustand

Die Benutzeridentität wird über den OIDC `sub`-Claim ermittelt. Dieser Wert ist provider-unabhängig eindeutig.

In Release 1.0 ist die Benutzeridentität `ps_user.username` (ein frei wählbarer String). Bei der Migration muss sichergestellt werden, dass die `user_id`-Werte in `remembered_project`, `profile_search_chat.creation_user` und allen Audit-Feldern (`created_by`, `last_modified_by`) auf die neuen OIDC `sub`-Werte migriert werden.

## Umgebungsvariablen (Produktion, Zielzustand)

| Variable              | Beschreibung           |
|-----------------------|------------------------|
| `AZURE_CLIENT_ID`     | Entra ID App-Client-ID |
| `AZURE_TENANT_ID`     | Entra ID Tenant-ID     |
| `AZURE_CLIENT_SECRET` | Entra ID Client-Secret |

## Microsoft Entra ID Integration – Schritt-für-Schritt

**Schritt 1: App-Registrierung in Azure Portal**

1. [Azure Portal](https://portal.azure.com) öffnen → **Microsoft Entra ID** → **App-Registrierungen** → **Neue Registrierung**
2. Name: `Powerstaff 2026`
3. Unterstützte Kontotypen: **Nur Konten in diesem Organisationsverzeichnis** (Single Tenant)
4. Umleitungs-URI: `Web` → `https://{produktions-domain}/login/oauth2/code/entra`
5. Registrieren

**Schritt 2: Anmeldeinformationen erstellen**

1. In der App-Registrierung → **Zertifikate und Geheimnisse** → **Neuer geheimer Clientschlüssel**
2. Beschreibung: `powerstaff-prod`, Ablauf: nach Richtlinie des Unternehmens
3. Den generierten Wert sofort kopieren – er wird nur einmalig angezeigt

**Schritt 3: Benötigte Werte ermitteln**

In der App-Registrierung unter **Übersicht**:
- `AZURE_CLIENT_ID` = Anwendungs-ID (Client)
- `AZURE_TENANT_ID` = Verzeichnis-ID (Mandant)
- `AZURE_CLIENT_SECRET` = der in Schritt 2 erstellte Wert

**Schritt 4: Token-Konfiguration (optional, empfohlen)**

1. → **Tokenkonfiguration** → **Optionalen Anspruch hinzufügen** → Token-Typ: **ID**
2. Ansprüche hinzufügen: `email`, `preferred_username`

Dies stellt sicher, dass Name und E-Mail-Adresse im OIDC-Token verfügbar sind.

**Schritt 5: Benutzerzugriff steuern**

Standardmäßig kann sich jeder Benutzer des Tenants anmelden. Für eine Einschränkung auf bestimmte Benutzer oder Gruppen:

1. In der App-Registrierung → **Unternehmensanwendungen** → die App auswählen
2. → **Eigenschaften** → **Zuweisung erforderlich** auf `Ja` setzen
3. → **Benutzer und Gruppen** → die berechtigten Benutzer/Gruppen zuweisen

## Migrationspfad (Release 1.0 → SSO)

1. **Spring Security Konfiguration** austauschen: Form-Login-Konfiguration durch OAuth2/OIDC-Konfiguration ersetzen (`PsUserDetailsService` entfällt, `SecurityConfig` wird angepasst)
2. **`AuditingConfig`** anpassen: `UserDetails::getUsername` → `OidcUser::getSubject`
3. **`@AuthenticationPrincipal`-Injection** anpassen: `UserDetails` → `OidcUser` in allen Controllern
4. **Datenmigration** der `user_id`-Werte: Bestehende `username`-Werte in `remembered_project` und Audit-Felder auf OIDC `sub`-Claims mappen (Mapping-Tabelle erforderlich)
5. **`ps_user`-Tabelle** kann nach erfolgreicher Migration entfernt werden (Flyway-Migration)
