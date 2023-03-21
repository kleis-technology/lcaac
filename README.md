# lca-plugin

![Build](https://github.com/AlbanSeurat/lca-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

<!-- Plugin description -->
Adds support for a new domain-specific language targeting <i>Life Cycle Analysis</i> experts.
The following features are available :
<ul>
    <li>EF 3.1 flows and characterization factors from the <a href="https://eplca.jrc.ec.europa.eu/LCDN/developerEF.xhtml">European Platform on Life Cycle Assessment</a></li>
    <li>Description of processes, intermediary and elementary flows, with parameters.</li>
    <li>Embedded inventory computation engine.</li>
    <li>Code navigation.</li>
</ul>
<!-- Plugin description end -->

## Installation
## Get plugin archive
- Build manually from source:

Build the plugin: `gradlew clean buildPlugin`

The installation file is located in `build/distributions`. 

- Download:

The [latest or almost latest release](https://github.com/kleis-technology/lca-plugin/releases/latest)  
  
## Install  it manually 
using : 
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---


## LCA File type example


```lca
package carrot

process carrot_production {
  params {
    qElec = 3.0 kJ
  }

  products {
    1 kg carrot
  }

  inputs {
    qElec electricity
    3 l water
  }

  emissions {
    (57 percent*kg/kJ) * Qelec co2
  }
}

process electricity_production {
  products {
    1 kJ electricity
  }

  emissions {
    1.0 kg co2
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
    50 T carotte
    30 T fane_de_carottes         
  }
  inputs{
    60 h usage_de_tracteur
    1 T semi_de_carottes
    50 m3 eau_irrigee
  }
  resources {
    50 m3 eau 
  }
  land_use {
    10 ha terrain
  }
}
```