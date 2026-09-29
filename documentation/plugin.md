# Plugin Documentation

<!-- Use this page to document your plugin. Below is a suggested structure. -->

## Overview

Deze plugin legt een hiërarchische relatie tussen twee documenten binnen een proces vast. Wanneer
het document waarvoor het proces draait aan een ander (bovenliggend) document moet worden
gekoppeld, zorgt de plugin ervoor dat beide kanten van deze relatie correct worden geregistreerd:
het huidige document krijgt een PARENT-relatie naar het opgegeven document, en dat opgegeven
document krijgt op zijn beurt een reciproque CHILD-relatie terug naar het huidige document. Zo
blijft de ouder-kindstructuur tussen documenten in beide richtingen consistent en opvraagbaar.

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
