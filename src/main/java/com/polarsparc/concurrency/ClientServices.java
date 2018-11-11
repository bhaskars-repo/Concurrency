package com.polarsparc.concurrency;

import java.util.HashMap;
import java.util.Map;

public final class ClientServices {
	private static final Map<String, String> client2AccountTbl = new HashMap<>();
	
	// ----- Private Constructor -----
	
	private ClientServices() {
	}
	
	// ----- Public Method(s) -----
	
	public static final void setUp() {
		client2AccountTbl.put("B-1001", "A-10001");
		client2AccountTbl.put("B-1002", "A-10002");
		client2AccountTbl.put("B-1003", "A-10003");
		client2AccountTbl.put("B-1004", "A-10004");
		client2AccountTbl.put("B-1005", "A-10005");
		client2AccountTbl.put("S-2001", "A-10006");
		client2AccountTbl.put("S-2002", "A-10007");
		client2AccountTbl.put("S-2003", "A-10008");
		client2AccountTbl.put("S-2004", "A-10009");
		client2AccountTbl.put("S-2005", "A-10010");
	}
	
	public static final String lookupAccountByClientId(String id) {
		Utility.delay(150);
		
		if (id != null && id.isBlank() == false) {
			String acctId = client2AccountTbl.get(id);
			
//			String msg = String.format("Given client id - %s, Mapped account id - %s", id, acctId);
//			Utility.log("ClientServices", "lookupAccountByClientId", msg);
			
			return acctId;
		}
		
		return null;
	}
}
