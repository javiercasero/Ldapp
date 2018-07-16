package org.tfgdomain.jade;

import jade.content.AgentAction;


public class Unlock implements AgentAction {
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
