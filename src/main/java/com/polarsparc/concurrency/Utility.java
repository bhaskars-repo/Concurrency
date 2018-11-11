package com.polarsparc.concurrency;

import java.util.Random;

public final class Utility {
	private static final Random RANDOM = new Random();
	
	// ----- Static Block -----
	
	static {
		RANDOM.setSeed(1000);
	}
	
	// ----- Private Constructor -----
	
	private Utility() {
	}
	
	// ----- Public Method(s) -----
	
	public static final void log(String cls, String meth, String msg) {
		System.out.printf("%d [%s] <%s:%s> %s\n", System.currentTimeMillis(), Thread.currentThread().getName(),
			cls, meth, msg);
	}
	
	public static final void delay(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception ex) {
			// Ignore
		}
	}
	
	public static Instruction generateInstruction() {
		String bid = "B-100" + (1 + RANDOM.nextInt(5));
		String sid = "S-200" + (1 + RANDOM.nextInt(5));
		double amt = 5.00;
		
		return new Instruction(bid, sid, amt);
	}
}
