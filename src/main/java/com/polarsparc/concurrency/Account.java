package com.polarsparc.concurrency;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Account {
	private String clientId = null;
	private String accountId = null;
	private double balance = 0.0;
	
	public boolean hasBalance(double amt) {
		return balance > amt;
	}
	
	public boolean debit(double amt) {
		if (hasBalance(amt)) {
			balance -= amt;
			
			return true;
		}
		
		return false;
	}
	
	public void credit(double amt) {
		balance += amt;
	}
}
