# Plugin Documentation

<!-- Use this page to document your plugin. Below is a suggested structure. -->

## Overview

Deze plugin legt een hiërarchische relatie tussen twee documenten binnen een proces vast. Wanneer
het document waarvoor het proces draait aan een ander (bovenliggend) document moet worden
gekoppeld, zorgt de plugin ervoor dat beide kanten van deze relatie correct worden geregistreerd:
het huidige document krijgt een PARENT-relatie naar het opgegeven document, en dat opgegeven
document krijgt op zijn beurt een reciproque CHILD-relatie terug naar het huidige document. Zo
blijft de ouder-kindstructuur tussen documenten in beide richtingen consistent en opvraagbaar.

Daarnaast kan de plugin gegevens van een parent-document doorzetten naar al zijn
kind-documenten: je configureert welke onderdelen van het parent-document naar welk pad in de
kind-documenten worden gekopieerd.

## Dependencies

### Backend

```kotlin
dependencies {
    implementation("com.ritense.valtimoplugins:parent-child-plugin:1.0.0")
}
```

### Frontend

```json
{
  "dependencies": {
    "@valtimo-plugins/parent-child-plugin": "1.0.0"
  }
}
```

In your `app.module.ts`:

```typescript
import {
    ParentChildPluginModule, parentChildPluginSpecification,
} from '@valtimo-plugins/parent-child-plugin';

@NgModule({
    imports: [
        ParentChildPluginModule,
    ],
    providers: [
        {
            provide: PLUGIN_TOKEN,
            useValue: [
                parentChildPluginSpecification,
            ]
        }
    ]
})
```

## Configuration

Deze plugin heeft geen configuratie-eigenschappen.

## Actions

### connect parent

Koppelt het document waarvoor het proces draait aan een parent-document: het huidige document
krijgt een PARENT-relatie, en het parent-document krijgt een CHILD-relatie terug.

| Parameter        | Type   | Required | Description                                |
|------------------|--------|----------|---------------------------------------------|
| parentDocumentId | string | Yes      | Het ID van het document dat als parent moet worden gekoppeld |

### sync to children

Kopieert de geconfigureerde onderdelen van het document waarvoor het proces draait (de parent)
naar alle documenten waarmee het een CHILD-relatie heeft.

| Parameter | Type          | Required | Description                                                        |
|-----------|---------------|----------|--------------------------------------------------------------------|
| mappings  | NodeMapping[] | Yes      | Minimaal één mapping van een pad in de parent naar een pad in het kind |

Een `NodeMapping` bestaat uit:

| Veld   | Type   | Required | Description                                                        |
|--------|--------|----------|--------------------------------------------------------------------|
| source | string | Yes      | JSON pointer naar het onderdeel in het parent-document, bijv. `/destination` |
| target | string | Yes      | JSON pointer waar het onderdeel in het kind-document wordt geschreven, bijv. `/holidayDestination` |

## Usage

Om de plugin te gebruiken, voeg je in je BPMN-proces een service task toe en koppel je hieraan de
"connect parent"-actie van de Parent Child Plugin. Deze actie wordt uitgevoerd bij de start van de
service task (`SERVICE_TASK_START`). Je configureert één parameter:

- **parentDocumentId** — het ID van het document dat als bovenliggend (parent) document moet
  worden gekoppeld.

Het document waarvoor het proces zelf draait (bepaald via de process business key) wordt
automatisch als kind-document gebruikt; je hoeft dit dus niet apart op te geven. Zodra de service
task wordt uitgevoerd, roept de plugin de onderliggende `ParentChildService` aan, die
transactioneel beide documentrelaties (PARENT en CHILD) bijwerkt — mislukt een van beide, dan
wordt de hele koppeling teruggedraaid.

### Gegevens doorzetten naar kind-documenten

Voeg in het proces van het parent-document een service task toe en koppel hieraan de
"sync to children"-actie. In het configuratiescherm voeg je per onderdeel een regel toe met het pad
in het parent-document en het pad in het kind-document. Beide paden zijn JSON pointers en moeten
met `/` beginnen. Hele objecten en arrays worden in hun geheel gekopieerd; ontbrekende
tussenliggende objecten in het kind-document worden aangemaakt.

Voorbeeld van de process link:

```json
{
    "activityId": "sync_participants",
    "activityType": "bpmn:ServiceTask:start",
    "processLinkType": "plugin",
    "pluginConfigurationId": "<id van de plugin-configuratie>",
    "pluginActionDefinitionKey": "sync-to-children",
    "actionProperties": {
        "mappings": [
            { "source": "/destination", "target": "/holidayDestination" },
            { "source": "/startDate", "target": "/startDate" },
            { "source": "/endDate", "target": "/endDate" }
        ]
    }
}
```

Gedrag:

- **Schema** — het schema van het kind-document moet de gekopieerde onderdelen accepteren. Het
  kind-document wordt bij het opslaan tegen zijn eigen schema gevalideerd.
- **Fail fast** — bestaat een `source`-pad niet in het parent-document, dan faalt de actie
  voordat er een kind-document wordt aangepast.
- **Transactie per kind** — elk kind-document wordt in een eigen transactie bijgewerkt. Faalt
  één kind, dan worden de overige kinderen toch bijgewerkt; daarna faalt de actie met een
  melding van de mislukte kind-documenten, zodat er een incident in het proces ontstaat. De actie
  opnieuw uitvoeren is veilig: dezelfde waarden worden opnieuw geschreven.
- **Alleen directe kinderen** — kleinkinderen worden niet bijgewerkt.
- **Geen terugdraaien** — omdat elk kind in een eigen transactie wordt bijgewerkt, blijven de
  wijzigingen in de kind-documenten staan, ook als de processtap van de parent later wordt
  teruggedraaid.
