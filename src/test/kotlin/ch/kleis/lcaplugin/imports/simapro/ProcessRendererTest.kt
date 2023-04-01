package ch.kleis.lcaplugin.imports.simapro

import org.openlca.simapro.csv.Numeric
import org.openlca.simapro.csv.UncertaintyRecord
import org.openlca.simapro.csv.enums.ProcessCategory
import org.openlca.simapro.csv.enums.ProcessType
import org.openlca.simapro.csv.enums.Status
import org.openlca.simapro.csv.process.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


class ProcessRendererTest {
    val defaultZoneId = ZoneId.systemDefault()

    val sample = ProcessBlock().name("Acai berry, at farm/BR")
        .category(ProcessCategory.MATERIAL) //Category type
        .identifier("LAUSMILA000241671000001")
        .processType(ProcessType.UNIT_PROCESS)
        .status(Status.TO_BE_REVIEWED)
        .infrastructure(false)
        .date(Date.from(LocalDate.of(2018, 9, 20).atStartOfDay(defaultZoneId).toInstant()))
        .record("data entry by: Guillaume Bourgault bourgault@ecoinvent.org Uberlandstrasse 129, CH-8600 Dubendorf, Switzerland is active author: False\u007F")
        .generator(
            """Quantis, WFLDB team
Telephone: 0041 21 353 59 10; E-mail: wfldb@quantis-intl.com; Company: Quantis; Country: CH
"""
        )
        .collectionMethod(
            """Sampling procedure: Principles of the sampling procedure is described in the WFLDB
Methodological Guidelines for the Life Cycle Inventory of Agricultural Products, version 3.5 (Nemecek et al. 2019), available at: http://www.quantis-intl.com/wfldb.
Detailed information about the sources used for data sampling can be taken from WFLDB Documentation, available at: http://www.quantis-intl.com/wfldb
"""
        )
        .dataTreatment("Data traitement")
        .verification(
            """Proof reading validation: Passed internally.
Validator: Agroscope, WFLDB Team
E-mail: lca@agroscope.admin.ch; Company: Agroscope; Country: CH
"""
        )
        .comment(
            """"Reference flow: The functional unit is the production of 1 kg of Acai berry at the farm, with standard water content (89.0%). The reference flow is the average yield per hectare and year: 7.5 t/ha, obtained under irrigated conditions (total  amount of water 1862 m3/ha).

Allocation: No allocation

System boundaries: Cradle-to-gate. The inventory includes the processes of tree seedling production and planting, fertilisation (mineral and manure), irrigation and harvest.  Equipment infrastructure is also considered, such as for sprinkler irrigation. Direct field emissions from crop production activities (e.g. fertiliser) and land use change (LUC) are considered. Irrigation is based on blue water from Pfister for tropical fruits in Brazil. LUC for ""Fruit, tropical fresh nes"" is considered. 100% surface water powered with diesel is considered. No pesticides are applied for acai cultivation according to Bichara 2011. Fertilizers and manure application are assumed to be 100% manual (very low mechanization level). The harvest is assumed to be manual harvesting, the harvest baskets are not included. The harvested fruits are quickly transported to a transformation place where the pulp is extracted and then sold or frozen or further processed. These post-harvest steps are excluded. Number of years from planting to clearing (lifetime): 13 years. 

Geography: Brazil

Technology: Conventional production.

Time: 2000-2018

Data quality rating (DQR) = 1.8, Very good quality
"""
        )
        .systemDescription(SystemDescriptionRow().name("name").comment("Desc"))
        .allocationRules("allocationRules")

    init {
        sample.literatures().add(
            LiteratureRow().name("Methodological Guidelines for the Life Cycle Inventory of Agricultural Products.")
                .comment("comment")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide, in air")
                .subCompartment("in air")
                .unit("kg")
                .amount(Numeric.of(1770.6352377005392))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,2,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Energy, gross calorific value, in biomass")
                .subCompartment("")
                .unit("MJ")
                .amount(Numeric.of(20295.524449877732))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,2,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Water, well, BR;")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(501.95555914578216))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Water, river, BR")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Occupation, permanent crop, irrigated")
                .subCompartment("land")
                .unit("m2a")
                .amount(Numeric.of(10000.0))
                .uncertainty(UncertaintyRecord.logNormal(1.1130))
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Water, river, BR")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Occupation, permanent crop, irrigated")
                .subCompartment("land")
                .unit("m2a")
                .amount(Numeric.of(10000.0))
                .uncertainty(UncertaintyRecord.logNormal(1.1130))
                .comment("(2,1,1,1,1,na)\u007F")
        )
//        ;land;m2a;10000.0;Lognormal;1.1130;0;0;(2,1,1,1,1,na)

        sample.resources().add(
            ElementaryExchangeRow()
                .name("Transformation, from permanent crop, irrigated")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Water, river, BR")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,1,1,1,1,na)\n")
        )
        1.113
        //                    Resources
//
//    Transformation, to permanent crop, irrigated;land;m2;500.0;Lognormal;1.2077;0;0;(2,1,1,1,1,na)
//
        sample.products().add(
            ProductOutputRow().name("Acai berry, at farm (WFLDB 3.7)/BR U")
                .unit("kg")
                .amount(Numeric.of("9750*10/13"))
                .allocation(Numeric.of("100"))
                .wasteType("not defined")
                .category("_WFLDB 3.7 (Phase 2)\\Plant products\\Perennials\\Acai berry")
                .comment(
                    """The yield when productive is 9750 kg/ha-y (average between min (4500) and max (15000) from Oliveira 2018 (data for intensively managed acai crops is 12-15 tons fruit/ha-y) and Oliveira 2000 (4500 to 9000 kg/ha depending on the intensification of the cultivation)). Over the tree lifetime, no production is considered during year 1 to 3 and then production from year 4 to 13 based on Oliveira 2018.
The final yield corresponds to the average yield over the entire lifetime of the tree, i.e., with 10 productive years and 3 unproductive years."""
                )
        )
        sample.platformId()


//
    }

//
//    Time period
//    Unspecified
//
//    Geography
//    Unspecified

//    Technology
//    Unspecified
//
//    Representativeness
//    Unspecified

//    Multiple output allocation
//    Unspecified

//    Substitution allocation
//    Unspecified

//    Cut off rules
//    Unspecified

//    Capital goods
//    Unspecified
//
//    Boundary with nature
//    Unspecified


//    External documents
//            https://v38.ecoquery.ecoinvent.org/Details/PDF/8F333401-E072-43B6-AFFB-9D58D5D60AB4/290C1F85-4CC4-4FA1-B0C8-2CB7F4276DCE;


//Data sources:
//Bichara 2011: Bichara C M G, Rogez H. A�ai (Euterpe oleracea Martius). In Yahia E M, Postharvest biology and technology of tropical and subtropical fruits, Woodhead Publishing, 2011.
//Oliveira 2000: Oliveira M. do S.P, Carvalho J.E.U de, Nascimento W.M.O do. A�a� (Euterpe  oleracea Mart.). Jaboticabal: Funep, 2000.52p. (FUNEP. Frutas Nativas,7) (Downloaded on https://www.agencia.cnptia.embrapa.br/Repositorio/Oliveira+et+al.%252C+2000_000gbtehk8902wx5ok07shnq9dunz6i0.pdf, October 2018)
//Oliveira 2018: Oliveira M do S P, Schwartz G. A�a�Euterpe oleracea. In Rodrigues S, Oliveira Silva E de , Brito E S de. Exotic Fruits, Academic Press, 2018.


//    Avoided products


//    Materials/fuels
//    Fruit tree seedling, for planting {GLO}| market for fruit tree seedling, for planting | Cut-off, U;p;1400/13;Undefined;0;0;0;(2,1,1,1,1,na). There are 1400 plants/ha in average according to Oliveira 2000. The plants are grouped by 3 or 4, every 5 m x 5 m. Palm lifetime is 13 years according to Oliveira 2018.
//
//    Irrigating, surface, diesel powered (WFLDB 3.7)/GLO U;m3;2672.74832203915;Lognormal;1.0714;0;0;(2,1,1,1,1,na). 100% diesel powered surface irrigation is considered for a�ai cultivation.
//
//    Inorganic nitrogen fertiliser, as N {RoW}| nutrient supply from ammonium sulfate | Cut-off, U;kg;((2*300*0.1+3*300*0.1*12)/3+(2*100+11*200)*0.21/3+(2*33.3+11*150)*0.21/3)/1000*400/13*0.431;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year.
//Ammonium sulphate contains 21% N
//
//    NPK (15-15-15), as N, at plant (WFLDB 3.7)/RoW U;kg;((2*300*0.1+3*300*0.1*12)/3+(2*100+11*200)*0.21/3+(2*33.3+11*150)*0.21/3)/1000*400/13*0.569;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year.
//Proxy for 10-28-20
//
//    Triple superphosphate, as P2O5, at plant (WFLDB 3.7)/RoW U;kg;(200*0.48+(2*300*0.28+3*300*0.28*12)/3+(2*100+11*200)*0.48/3+(2*33.3+11*220)*0.48/3)/1000*400/13*0.452;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year.
//Triple superphosphate 48% P2O5 (ecoinvent data)
//
//    NPK (15-15-15), as P2O5, at plant (WFLDB 3.7)/RoW U;kg;(200*0.48+(2*300*0.28+3*300*0.28*12)/3+(2*100+11*200)*0.48/3+(2*33.3+11*220)*0.48/3)/1000*400/13*0.548;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year.
//Proxy for 10-28-20
//
//    Potassium chloride, as K2O, at plant (WFLDB 3.7)/RoW U;kg;((2*300*0.2+3*300*0.2*12)/3+(2*100+11*200)*0.6/3+(2*33.3+11*250)*0.6/3)/1000*400/13*0.579;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year.
//Potassium chloride 60% K2O (ecoinvent data)
//
//    NPK (15-15-15), as K2O, at plant (WFLDB 3.7)/RoW U;kg;((2*300*0.2+3*300*0.2*12)/3+(2*100+11*200)*0.6/3+(2*33.3+11*250)*0.6/3)/1000*400/13*0.421;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year.
//Proxy for 10-28-20
//
//    Manure, liquid, cattle {GLO}| market for | Cut-off, U;kg;657.7692307692308;Lognormal;1.0714;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year. 86.3% is bovine manure and 13.7% is poultry manure.
//Manure is assumed to be 50% liquid manure and 50% solid manure without any info on this in the reference and it is considered the manure is 400 kg/m3 (https://www.aqua-calc.com/page/density-table/substance/manure)
//
//    Poultry manure, fresh {GLO}| market for | Cut-off, U;kg;103.17948717948718;Lognormal;1.0714;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year. 86.3% is bovine manure and 13.7% is poultry manure.
//Manure is assumed to be 50% liquid manure and 50% solid manure without any info on this in the reference and it is considered the manure is 400 kg/m3 (https://www.aqua-calc.com/page/density-table/substance/manure)
//
//    Manure, solid, cattle {GLO}| market for | Cut-off, U;kg;(12.5/2+2.5/2+11*2.5/3+(12.5/2+2.5/2)*13/3)/1000*400/13*0.5*400*0.864;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year. 86.3% is bovine manure and 13.7% is poultry manure.
//Manure is assumed to be 50% liquid manure and 50% solid manure without any info on this in the reference and it is considered the manure is 400 kg/m3 (https://www.aqua-calc.com/page/density-table/substance/manure)
//
//    Poultry manure, dried {GLO}| market for | Cut-off, U;kg;(12.5/2+2.5/2+11*2.5/3+(12.5/2+2.5/2)*13/3)/1000*400/13*0.5*400*0.136;Undefined;0;0;0;(2,1,1,1,1,na). Based on Oliveira 2000: 400 plants/ha (5 m between each plant). At the planting, 12.5 L manure is applied and 200 g triple superphosphate per tree. There are then are 3 fertilization plans: a) 2 x 300 g NPK 10-28-20 year 1 then 3 x 300 g of the same fertilizer per year. b) 100 ammonium sulfate, 100 g triple superphosphate and 100 g KCl per plant for year 1 and 2, then twice this amount plus 2.5 L famryard manure per plant and year. c) 10-15 L bovine manure or 2-3 L poultry manure and 33.3 g ammonium nitrate, 33.3 g triple superphosphate and 33.3 g KCl per plant for year 1 and 2, then the same amount of manure and 150 g ammonium sulphate, 220 g triple superphosphate and 250 g KCl per plant and year. 86.3% is bovine manure and 13.7% is poultry manure.
//Manure is assumed to be 50% liquid manure and 50% solid manure without any info on this in the reference and it is considered the manure is 400 kg/m3 (https://www.aqua-calc.com/page/density-table/substance/manure)
//
//    Crop, default, heavy metals uptake (WFLDB 3.7)/GLO U;kg;824.9999999999999 * Heavy_metal_uptake;Undefined;0;0;0;(2,2,1,1,1,na)
//
//    Land use change, perennial crop, annualized on 20 years (WFLDB 3.7)/BR U;ha;0.1486*LUC_crop_specific+0.3654*(1-LUC_crop_specific);Undefined;0;0;0;(2,1,1,1,1,na)
//
//    Packaging, for fertilisers or pesticides {GLO}| packaging production for solid fertiliser or pesticide, per kilogram of packed product | Cut-off, U;kg;271.5900307692308;Lognormal;1.0744;0;0;(2,2,1,1,1,na)
//
//
//    Electricity/heat
//
//    Emissions to air
//    Ammonia;low. pop.;kg;3.1842487991655006;Lognormal;1.2090;0;0;(2,2,1,1,1,na) - Calculated value - EMEP/EEA (2016)
//
//    Nitrogen oxides;low. pop.;kg;1.104;Lognormal;1.4057;0;0;(2,2,1,1,1,na) - Calculated value - EMEP/EEA (2016)
//
//    Dinitrogen monoxide;low. pop.;kg;0.8815614366518127;Lognormal;1.4057;0;0;(2,2,1,1,1,na) - Calculated value - IPCC (2006)
//
//    Water, BR;low. pop.;ton;1202.7367449176174;Lognormal;1.2090;0;0;(2,2,1,1,1,na) - Calculated value - Emissions calculated from water balance
//
//
//    Emissions to water
//    Nitrate;groundwater;kg;161.0527173719368;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - SQCB-NO3
//
//    Phosphorus;river;kg;0.6576583677461771;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Prasuhn (2006)
//
//    Phosphate;river;kg;0.8968227313106426;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Prasuhn (2006)
//
//    Phosphate;groundwater;kg;0.1845977549055192;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Prasuhn (2006)
//
//    Cadmium;river;kg;8.586272731579713E-5;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Cadmium;groundwater;kg;4.368919634587126E-5;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Chromium;river;kg;0.007876404026411404;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Chromium;groundwater;kg;0.019321366132470336;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Copper;river;kg;0.01072244472284125;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Copper;groundwater;kg;0.003076436800499609;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Lead;river;kg;0.001090317347299651;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Lead;groundwater;kg;8.208098759368602E-5;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Mercury;river;kg;1.4612976228965614E-5;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Mercury;groundwater;kg;7.707775433430391E-7;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Nickel;river;kg;0.004588811210023889;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Zinc;river;kg;0.010650139845251311;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Zinc;groundwater;kg;0.015663506094134527;Lognormal;1.8042;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Water, BR;river;m3;294.0023154243065;Lognormal;1.0744;0;0;(2,2,1,1,1,na) - Calculated value - Emissions calculated from water balance
//
//    Water, BR;groundwater;m3;1176.009261697226;Lognormal;1.0744;0;0;(2,2,1,1,1,na) - Calculated value - Emissions calculated from water balance
//
//
//    Emissions to soil
//    Cadmium;agricultural;kg;0.004104844160803827;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Chromium;agricultural;kg;0.007015179369464961;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Copper;agricultural;kg;-0.001747533635763676;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Lead;agricultural;kg;-0.000766970419242704;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Mercury;agricultural;kg;2.779265268885652E-5;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Nickel;agricultural;kg;-0.000252462330056446;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//    Zinc;agricultural;kg;0.001159607007109607;Lognormal;1.5051;0;0;(2,2,1,1,1,na) - Calculated value - Freiermuth (2006)
//
//
//    Final waste flows
//
//    Non material emissions
//
//    Social issues
//
//    Economic issues
//
//    Waste to treatment
//
//    Input parameters
//
//    Calculated parameters
//
//    End

}