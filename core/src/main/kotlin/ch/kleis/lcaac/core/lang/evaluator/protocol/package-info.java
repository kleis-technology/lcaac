package ch.kleis.lcaac.core.lang.evaluator.protocol;

/*
    This package represents the construction of the network of processes and substance characterizations
    as the interaction between a Learner and an Oracle.

    Learner.

    The Learner starts with an initial demand of products or substances, under the form of specs
    (EProductSpec, ESubstanceSpec). Her goal is to discover how these products/substances can be
    produced/characterized. She asks the Oracle for that.

    Oracle.

    The Oracle knows all the process templates available, as well as the substance characterizations.
    Given a request for a product or substance, given as a spec, possibly with some parameters,
    she is able to find the appropriate process template or substance characterization and evaluate it.
    She responds to the request with the evaluated process/substance characterization.

    Interaction.

    When the Learner receives responses from the Oracle, she learns new processes/substance characterizations.
    She learns also that her previous request for a product/substance match this process or
    that substance characterization. Assuming that the Oracle gives deterministic answer, the Learner
    will not need to ask the Oracle for that product or substance, as she already knows the answer.

    However, the received processes may require unknown products (in their inputs). The Learner keeps
    asking the Oracle about these unknown products until she knows all the processes and substance characterizations
    required to satisfy her initial demand of products/substances.

    Trace.

    The trace is a recording of the responses the Learner has received during her interaction
    with the Oracle.
 */
