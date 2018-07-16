package org.tfgdomain.jade;

import jade.content.Predicate;


public class UnlockRequest implements Predicate {
	private Account account;
	
	
	
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
}
