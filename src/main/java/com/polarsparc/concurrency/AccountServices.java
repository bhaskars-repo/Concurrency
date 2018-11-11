package com.polarsparc.concurrency;

import java.util.HashMap;
import java.util.Map;

public final class AccountServices {
	private static final Map<String, Account> clientAccounts = new HashMap<>();
	
	// ----- Private Constructor -----
			
	private AccountServices() {
	}
	
	// ----- Public Method(s) -----
	
	public static final void setUp() {
		clientAccounts.put("A-10001", new Account("B-1001", "A-10001", 10000.00));
		clientAccounts.put("A-10002", new Account("B-1002", "A-10002", 12500.00));
		clientAccounts.put("A-10003", new Account("B-1003", "A-10003", 5000.00));
		clientAccounts.put("A-10004", new Account("B-1004", "A-10004", 15000.00));
		clientAccounts.put("A-10005", new Account("B-1005", "A-10005", 7500.00));
		clientAccounts.put("A-10006", new Account("S-2001", "A-10006", 10000.00));
		clientAccounts.put("A-10007", new Account("S-2002", "A-10007", 5000.00));
		clientAccounts.put("A-10008", new Account("S-2003", "A-10008", 15000.00));
		clientAccounts.put("A-10009", new Account("S-2004", "A-10009", 7500.00));
		clientAccounts.put("A-10010", new Account("S-2005", "A-10010", 10000.00));
	}
	
	public static final void settle(String bid, String sid, double amt) throws Exception {
		Utility.delay(350);
		
		if (bid != null && bid.isBlank() == false && sid != null && sid.isBlank() == false) {
			synchronized(clientAccounts) {
				Account bacct = clientAccounts.get(bid);
				Account sacct = clientAccounts.get(sid);
				if (bacct != null && sacct != null) {
					if (bacct.debit(amt)) {
						sacct.credit(amt);
					} else {
						throw new Exception(String.format("Exception: Buyer account id - %s, Specified amount - %.2f,"
							+ "Balance - %.2f",	bid, amt, bacct.getBalance()));
					}
				}
				
				String msg = String.format("Buyer account - %s, Seller account - %s, Amount - %.2f, "
					+ "Buyer balance - %.2f, Seller balance - %.2f", bid, sid, amt, bacct.getBalance(), sacct.getBalance());
				Utility.log("AccountServices", "settle", msg);
			}
		}
	}
}
