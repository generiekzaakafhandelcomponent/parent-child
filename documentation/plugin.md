# Plugin Documentation

<!-- Use this page to document your plugin. Below is a suggested structure. -->

## Overview

This plugin links a document to a parent document. Its `connect-parent` action assigns a PARENT
document relation to the document the process is running for, and the reciprocal CHILD document
relation to the specified parent document.

## Dependencies

### Backend

```kotlin
dependencies {
    implementation("com.ritense.valtimoplugins:parent-child-plugin:0.0.1")
}
```

### Frontend

```json
{
  "dependencies": {
    "@valtimo-plugins/parent-child-plugin": "0.0.1"
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

This plugin has no configuration properties.

## Actions

### connect parent

Links the document the process is running for to a parent document: a PARENT relation is assigned
to the current document, and a CHILD relation is assigned to the parent document.

| Parameter       | Type   | Required | Description                          |
|-----------------|--------|----------|---------------------------------------|
| parentDocumentId | string | Yes     | The ID of the document to link as parent |

## Usage

Add a service task to your process and configure it to use the Parent Child Plugin's "connect
parent" action, providing the `parentDocumentId` of the document to link. The action runs on
service task start and links the document identified by the process business key to the given
parent document.
