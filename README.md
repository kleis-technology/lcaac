# lca-plugin

![Build](https://github.com/AlbanSeurat/lca-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml) and [sources package](/src/main/kotlin).
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [x] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [x] Set the Plugin ID in the above README badges.
- [x] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
Adds support for a new domain-specific language targeting <i>Life Cycle Analysis</i> experts. The following features are available for free.
<ul>
    <li>EF 3.1 flows and characterization factors from the <a href="https://eplca.jrc.ec.europa.eu/LCDN/developerEF.xhtml">European Platform on Life Cycle Assessment</a></li>
    <li>Description of processes, intermediary and elementary flows, with parameters.</li>
    <li>Embedded inventory computation engine.</li>
    <li>Code navigation.</li>
</ul>
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "lca-plugin"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/kleis-technology/lca-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---


## LCA File type example


```lca
package carrot

import ef31.*

process carrot_production {
    parameters {
        - R1: 2.0
        - qElec : 3.0
    }

    products {
        - carrot 1 kg
    }

    inputs {
        - electricity ${qElec} kJ
        - water ${3.0} l
    }

    emissions {
        - "HFC-32, air, lower stratosphere and upper troposphere" ${2 * sin(R1)^2 + qElec^2} kg
        -  "warfarin, air, non-urban high stack" 3 kg
    }
}

process electricity_production {
    products {
        - electricity 1 kJ
    }

    emissions {
        - "(+)-bornan-2-one, air, urban air close to ground" 1.0 kg
    }
}

process water_production {
    products {
        - water 1 l
    }

    emissions {
        - "HFC-23, soil, agricultural" 1 kg
        - "HFC-32, air, lower stratosphere and upper troposphere" 1 kg
    }
}
```

