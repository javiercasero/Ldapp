package org.tfgdomain.jade;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase Unlock.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import jade.content.AgentAction;


class Unlock implements AgentAction {
	private Account account;
	private int resultCode;
	
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
	public int getResultCode(){
		return resultCode;
	}
	
	public void setResultCode(int resultCode){
		this.resultCode = resultCode;
	}
}
