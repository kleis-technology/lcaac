# lca-plugin

![Build](https://github.com/AlbanSeurat/lca-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

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
  - <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Manage plugin repositories</kbd> > <kbd> Add `https://plugins.jetbrains.com/plugins/alpha/list` </kbd>
  - <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "life cycle analysis as code"</kbd> >
    <kbd>Install Plugin</kbd>
  
- Manually:

Build the plugin: `gradlew clean buildPlugin`

The installation file is located in `build/distributions`. 

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

## Vocabulary definition

> **process**: contains a list of exchanges

> **exchange**: contains a list of (quantity, input | product | resource | land-use | emission)

> **biosphere exchange**: exchange with a resource, a land-use, or an emission

> **technosphere exchange**: exchange with an input or a product
 
> **substance**: has a name, a compartiment, a sub-compartiment, and a list of emission-factors.
> A substance is used by a resource, an emission or a land-use 

> **emission-factor**: contains a value and an indicator name 

### Example
Un fermier exploite sa ferme pour produire des carottes. Pour produire 50T de carottes, il a besoin de
- 60 h d'usage de tracteur
- 10 ha de terrain
- 1 T semi de carottes (qui elle même a besoin de carottes)
- 50 m^3 d'eau irrigée
- 50 m^3 d'eau de pluie

Il obtient également 30T de fânes de carottes
```lca
package carotte_exemple

process production_de_carottes {
  products {
   - carotte 50 T
   - fane_de_carottes 30 T         
  }
  inputs{
    - usage_de_tracteur 60 h
    - semi_de_carottes 1 T
    - eau_irrigee 50 m3
  }
    resources {
     - eau 50 m3
    }
  land_use {
    - terrain 10 ha
  }
}
```