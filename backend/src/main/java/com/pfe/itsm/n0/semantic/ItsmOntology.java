package com.pfe.itsm.n0.semantic;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public final class ItsmOntology {

    public static final String NS = "https://pfe.local/itsm/ontology#";
    public static final String RESOURCE_NS = "https://pfe.local/itsm/resource/";

    public static final Resource ARTICLE = resource("KnowledgeArticle");
    public static final Resource SECTION = resource("KnowledgeSection");
    public static final Resource SYMPTOM = resource("Symptom");
    public static final Resource CAUSE = resource("Cause");
    public static final Resource SOLUTION = resource("Solution");
    public static final Resource VERIFICATION = resource("Verification");
    public static final Resource ESCALATION_RULE = resource("EscalationRule");
    public static final Resource CATEGORY = resource("TicketCategory");
    public static final Resource SUPPORT_LEVEL = resource("SupportLevel");

    public static final Property hasSection = property("hasSection");
    public static final Property hasSymptom = property("hasSymptom");
    public static final Property hasCause = property("hasCause");
    public static final Property hasSolution = property("hasSolution");
    public static final Property hasVerification = property("hasVerification");
    public static final Property hasEscalationRule = property("hasEscalationRule");
    public static final Property belongsToCategory = property("belongsToCategory");
    public static final Property escalatesTo = property("escalatesTo");
    public static final Property documentedIn = property("documentedIn");
    public static final Property sourceText = property("sourceText");

    private ItsmOntology() {
    }

    private static Resource resource(String localName) {
        return ResourceFactory.createResource(NS + localName);
    }

    private static Property property(String localName) {
        return ResourceFactory.createProperty(NS, localName);
    }
}
