/**
 * Presentation of the import module concepts
 * <ul>
 * <li> {@link ch.kleis.lcaplugin.imports.model} contains all the pivot models (ImportedXxx): objects having all the
 * data to produce
 * LCA files (included things like comments)</li>
 * <li>{@link ch.kleis.lcaplugin.imports.shared} contains serializer in charge to convert the pivots model into LCA
 * language</li>
 * <li>class XxxRender are in charge of the output file structure. For example in LCIA import we prefer to have one file
 * per process containing the process and the virtual substance.<br>
 * In Simapro import we prefer to have all the substances in a dedicated folder.</li>
 * <li>{@link ch.kleis.lcaplugin.imports.Importer} is the abstract class with the import logic. It's subclassed for each
 * kind of import</li>
 * <li>{@link ch.kleis.lcaplugin.imports.FileWriterWithSize} is in charge of the strategy : one per file and to close
 * all the resources</li>
 * </ul>
 * <p>
 */

package ch.kleis.lcaplugin.imports;


