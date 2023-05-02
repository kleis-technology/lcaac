package ch.kleis.lcaplugin.language.psi.stub.substance

fun substanceKey(fqn: String, type: String, compartment: String, subCompartment: String?): String {
    val typeField = """type="$type""""
    val compartmentField = """compartment="$compartment""""
    val subCompartmentField = subCompartment?.let { """sub_compartment="$it"""" }
    val arguments = listOfNotNull(typeField, compartmentField, subCompartmentField).joinToString()
    return """$fqn($arguments)"""
}
