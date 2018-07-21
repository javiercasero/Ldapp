package org.tfgdomain.jade;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase LdapOntology.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;


class LdapOntology extends Ontology {

	  /**
	    A symbolic constant, containing the name of this ontology.
	   */
	  private static final String ONTOLOGY_NAME = "ldap-ontology";

	  // VOCABULARY
	  // Concepts
	  private static final String ACCOUNT = "ACCOUNT";
	  private static final String ACCOUNT_DOMAIN = "domain";
	  private static final String ACCOUNT_USER = "user";
	  	  
	  // Actions
	  private static final String UNLOCK = "UNLOCK";
	  private static final String UNLOCK_ACCOUNT = "account";
	  private static final String UNLOCK_RESULTCODE = "resultcode";
	  
	  // Predicates
	  private static final String UNLOCKREQUEST = "UNLOCKREQUEST";
	  private static final String UNLOCKREQUEST_ACCOUNT = "account";
	  
	  private static final Ontology instance = new LdapOntology();
		
	  /**
	     This method grants access to the unique instance of the
	     ontology.
	     @return An <code>Ontology</code> object, containing the concepts
	     of the ontology.
	  */
	   public static Ontology getInstance() {
			return instance;
	   }
		
	  /**
	   * Constructor
	   */
	  private LdapOntology() {
	    //__CLDC_UNSUPPORTED__BEGIN
	  	super(ONTOLOGY_NAME, BasicOntology.getInstance());


	    try {
			add(new ConceptSchema(ACCOUNT), Account.class);
			add(new PredicateSchema(UNLOCKREQUEST), UnlockRequest.class);
			add(new AgentActionSchema(UNLOCK), Unlock.class);
			
	    	ConceptSchema cs = (ConceptSchema)getSchema(ACCOUNT);
			cs.add(ACCOUNT_DOMAIN, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			cs.add(ACCOUNT_USER, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
	    	
	    	PredicateSchema ps = (PredicateSchema)getSchema(UNLOCKREQUEST);
	    	ps.add(UNLOCKREQUEST_ACCOUNT, getSchema(ACCOUNT));
	    	
			AgentActionSchema as = (AgentActionSchema)getSchema(UNLOCK);
			as.add(UNLOCK_ACCOUNT, (ConceptSchema)getSchema(ACCOUNT), ObjectSchema.MANDATORY);
			as.add(UNLOCK_RESULTCODE, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	    }
	    catch(OntologyException oe) {
	      oe.printStackTrace();
	    }
	  } 
	}
