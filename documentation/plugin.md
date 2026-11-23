# Plugin Documentation

<!-- Use this page to document your plugin. Below is a suggested structure. -->

## Overview

Deze plugin legt een hiërarchische relatie tussen twee documenten binnen een proces vast. Wanneer
het document waarvoor het proces draait aan een ander (bovenliggend) document moet worden
gekoppeld, zorgt de plugin ervoor dat beide kanten van deze relatie correct worden geregistreerd:
het huidige document krijgt een PARENT-relatie naar het opgegeven document, en dat opgegeven
document krijgt op zijn beurt een reciproque CHILD-relatie terug naar het huidige document. Zo
blijft de ouder-kindstructuur tussen documenten in beide richtingen consistent en opvraagbaar.

Daarnaast kunnen gekoppelde documenten gegevens met elkaar delen: met **sync to children** (sinds
1.1.0) kopieert een bovenliggend document gegevens naar al zijn onderliggende documenten, en met
**sync to parent** (sinds 1.2.0) schrijft een onderliggend document gegevens terug naar zijn
bovenliggende document, waarbij lijsten optioneel worden samengevoegd.

## Dependencies

### Backend

```kotlin
dependencies {
    implementation("com.ritense.valtimoplugins:parent-child-plugin:1.2.0")
}
```

### Frontend

```json
{
  "dependencies": {
    "@valtimo-plugins/parent-child-plugin": "1.2.0"
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

*Sinds 1.1.0.* Kopieert de geconfigureerde onderdelen van het document waarvoor het proces draait
(de parent) naar al zijn onderliggende documenten (CHILD-relaties). Wordt uitgevoerd bij de start van
een service task (`SERVICE_TASK_START`).

| Parameter | Type          | Required | Description                                    |
|-----------|---------------|----------|------------------------------------------------|
| mappings  | NodeMapping[] | Yes      | Eén of meer mappings van bovenliggend naar onderliggend document |

Elke mapping heeft de volgende velden:

| Veld   | Type   | Required | Description |
|--------|--------|----------|-------------|
| source | string | Yes      | JSON pointer in het bovenliggende document, bijvoorbeeld `/destination` |
| target | string | Yes      | JSON pointer in het onderliggende document, bijvoorbeeld `/holidayDestination`; ontbrekende tussenliggende objecten worden aangemaakt |

Het doel wordt altijd overschreven; de `merge`-optie wordt door deze actie niet gebruikt.

Gedrag:

- Bestaat een `source`-pad niet in het bovenliggende document, dan faalt de actie voordat een
  onderliggend document wordt aangepast.
- Elk onderliggend document wordt in een eigen transactie bijgewerkt. Mislukt de update van een kind,
  dan worden de overige kinderen toch bijgewerkt; daarna faalt de actie met de ID's van de mislukte
  kinderen.
- Een document zonder onderliggende documenten levert geen fout op; er wordt dan niets gekopieerd.

Voorbeeld van een process link waarin de vakantiegegevens naar alle deelnemers worden gekopieerd:

```json
{
  "activityId": "sync_participants",
  "activityType": "bpmn:ServiceTask:start",
  "processLinkType": "plugin",
  "pluginConfigurationId": "47f61ed4-d2b9-4a6a-854e-44e2151cb32a",
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

### sync to parent

*Sinds 1.2.0.* Kopieert de geconfigureerde onderdelen van het document waarvoor het proces draait
(het kind) naar het document waarmee het een PARENT-relatie heeft. Wordt uitgevoerd bij de start van
een service task (`SERVICE_TASK_START`).

| Parameter | Type          | Required | Description                                    |
|-----------|---------------|----------|------------------------------------------------|
| mappings  | NodeMapping[] | Yes      | Eén of meer mappings van kind naar bovenliggend document |

Elke mapping heeft de volgende velden:

| Veld   | Type    | Required | Description |
|--------|---------|----------|-------------|
| source | string  | Yes      | JSON pointer in het onderliggende document, bijvoorbeeld `/activities` |
| target | string  | Yes      | JSON pointer in het bovenliggende document, bijvoorbeeld `/selectedActivities`; ontbrekende tussenliggende objecten worden aangemaakt |
| merge  | boolean | No       | Standaard `false`. Bij `true` (checkbox **Lijst samenvoegen**) en wanneer bron en bestaand doel allebei een lijst zijn, worden de items van de bron zonder dubbelen achteraan de bestaande lijst toegevoegd. In alle andere gevallen wordt het doel overschreven. |

Gedrag:

- Het onderliggende document moet precies één PARENT-relatie hebben; bij geen of meerdere parents
  faalt de actie zonder iets te wijzigen.
- Bestaat een `source`-pad niet in het onderliggende document, dan faalt de actie voordat het
  bovenliggende document wordt aangepast.
- Het bovenliggende document wordt bijgewerkt binnen de transactie van het proces; overige gegevens
  in het bovenliggende document blijven ongewijzigd.

Voorbeeld van een process link waarin de gekozen activiteiten van een deelnemer worden samengevoegd
in de vakantie (bovenliggend document):

```json
{
  "activityId": "Activity_submit_activities",
  "activityType": "bpmn:ServiceTask:start",
  "processLinkType": "plugin",
  "pluginConfigurationId": "47f61ed4-d2b9-4a6a-854e-44e2151cb32a",
  "pluginActionDefinitionKey": "sync-to-parent",
  "actionProperties": {
    "mappings": [
      { "source": "/activities", "target": "/selectedActivities", "merge": true }
    ]
  }
}
```

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
