package org.tfgdomain.jade;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase Account.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import jade.content.Concept;

public class Account implements Concept{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5411660095110729012L;
	private String  _domain;		// Dominio de la cuenta
	private String _user;			// Usuario de la cuenta
	
	// 
	public void setDomain(String domain) {
		_domain=domain;
	}
	public String getDomain() {
		return _domain;
	}
	public void setUser(String user) {
		_user=user;
	}
	public String getUser() {
		return _user;
	}
	
}
