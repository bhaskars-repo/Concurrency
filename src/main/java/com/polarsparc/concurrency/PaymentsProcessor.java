package com.polarsparc.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class PaymentsProcessor {
	private static final int COUNT = 10;
	private static final int POOLSZ = 4;
	private static final List<Instruction> INSTRUCTIONS = new ArrayList<>(10);
	
	// ----- Static Block -----
	
	static {
		for (int i = 1; i <= COUNT; i++) {
			INSTRUCTIONS.add(Utility.generateInstruction());
		}
	}
	
	// ----- Main -----
	
	/*
	 * Steps for a payment on an instruction from a buyer to a seller:
	 * 
	 * 1. Lookup the account of the buyer
	 * 2. Lookup the account of the seller
	 * 3. Settle the payment - credit the seller and debit the buyer
	 */
	
	public static void main(String[] args) {
		Utility.log("PaymentsProcessor", "main", "---> Ready to start");
		
		approachOne();
		approachTwo();
		approachThree();
		approachFour();
		approachFive();
		
		Utility.log("PaymentsProcessor", "main", "---> Done");
	}
	
	// ----- Method(s) -----
	
	// Sequential processing
	public static void approachOne() {
		ClientServices.setUp();
		AccountServices.setUp();
		
		Utility.log("PaymentsProcessor", "approachOne", "=====> Ready to start");
		
		long start = System.nanoTime();
		
		for (Instruction inst : INSTRUCTIONS) {
			String bacct = ClientServices.lookupAccountByClientId(inst.getBuyerId());
			String sacct = ClientServices.lookupAccountByClientId(inst.getSellerId());
			try {
				AccountServices.settle(bacct, sacct, inst.getAmount());
			} catch (Exception ex) {
				Utility.log("PaymentsProcessor", "approachOne", ex.getMessage());
			}
		}
		
		long end = System.nanoTime();
		
		String msg = String.format("=====> Average time - %d ms", (TimeUnit.NANOSECONDS.toMillis(end-start)/COUNT));
		Utility.log("PaymentsProcessor", "approachOne", msg);
	}
	
	// Using java threads
	public static void approachTwo() {
		ClientServices.setUp();
		AccountServices.setUp();
		
		Utility.log("PaymentsProcessor", "approachTwo", "=====> Ready to start");
		
		long start = System.nanoTime();
		
		for (Instruction inst : INSTRUCTIONS) {
			Settlement smt = new Settlement();
			smt.setAmount(inst.getAmount());
			
			Thread thr1 = new Thread(() -> {
				smt.setBuyerAccount(ClientServices.lookupAccountByClientId(inst.getBuyerId()));
			});
			thr1.setName("thread-1");
			thr1.start();
			
			Thread thr2 = new Thread(() -> {
				smt.setSellerAccount(ClientServices.lookupAccountByClientId(inst.getSellerId()));
			});
			thr2.setName("thread-2");
			thr2.start();
			
			try {
				thr1.join();
				thr2.join();
			} catch (Exception ex) {
				Utility.log("PaymentsProcessor", "approachTwo", ex.getMessage());
			}
			
			Thread thr3 = new Thread(() -> {
				try {
					AccountServices.settle(smt.getBuyerAccount(), smt.getSellerAccount(), smt.getAmount());
				} catch (Exception ex) {
					Utility.log("PaymentsProcessor", "approachTwo", ex.getMessage());
				}
			});
			thr3.setName("thread-3");
			thr3.start();
			
			try {
				thr3.join();
			} catch (Exception ex) {
				Utility.log("PaymentsProcessor", "approachTwo", ex.getMessage());
			}
		}
		
		long end = System.nanoTime();
		
		String msg = String.format("=====> Average time - %d ms", (TimeUnit.NANOSECONDS.toMillis(end-start)/COUNT));
		Utility.log("PaymentsProcessor", "approachTwo", msg);
	}
	
	// Using java futures
	public static void approachThree() {
		ClientServices.setUp();
		AccountServices.setUp();
		
		ExecutorService executor = Executors.newFixedThreadPool(POOLSZ);
		
		Utility.log("PaymentsProcessor", "approachThree", "=====> Ready to start");
		
		long start = System.nanoTime();
		
		for (Instruction inst : INSTRUCTIONS) {
			Settlement smt = new Settlement();
			smt.setAmount(inst.getAmount());
			
			Future<String> fb = executor.submit(() -> {
				return ClientServices.lookupAccountByClientId(inst.getBuyerId());
			});
			
			Future<String> fs = executor.submit(() -> {
				return ClientServices.lookupAccountByClientId(inst.getSellerId());
			});
			
			try {
				smt.setBuyerAccount(fb.get());
				smt.setSellerAccount(fs.get());
			} catch (Exception ex) {
				Utility.log("PaymentsProcessor", "approachThree", ex.getMessage());
			}
			
			Future<?> fdc = executor.submit(() -> {
				try {
					AccountServices.settle(smt.getBuyerAccount(), smt.getSellerAccount(), smt.getAmount());
				} catch (Exception ex) {
					Utility.log("PaymentsProcessor", "approachThree", ex.getMessage());
				}
			});
			
			try {
				fdc.get();
			} catch (Exception ex) {
				Utility.log("PaymentsProcessor", "approachThree", ex.getMessage());
			}
		}
		
		long end = System.nanoTime();
		
		executor.shutdown();
		
		String msg = String.format("=====> Average time - %d ms", (TimeUnit.NANOSECONDS.toMillis(end-start)/COUNT));
		Utility.log("PaymentsProcessor", "approachThree", msg);
	}
	
	
	// Using java futures
	public static void approachFour() {
		ClientServices.setUp();
		AccountServices.setUp();
		
		ExecutorService executor = Executors.newFixedThreadPool(POOLSZ);
		
		List<Future<?>> futures = new ArrayList<>();
		
		Utility.log("PaymentsProcessor", "approachFour", "=====> Ready to start");
		
		long start = System.nanoTime();
		
		for (Instruction inst : INSTRUCTIONS) {
			Future<?> fb = executor.submit(() -> {
				Settlement smt = new Settlement();
				smt.setAmount(inst.getAmount());
				
				smt.setBuyerAccount(ClientServices.lookupAccountByClientId(inst.getBuyerId()));
				smt.setSellerAccount(ClientServices.lookupAccountByClientId(inst.getSellerId()));
				
				try {
					AccountServices.settle(smt.getBuyerAccount(), smt.getSellerAccount(), smt.getAmount());
				} catch (Exception ex) {
					Utility.log("PaymentsProcessor", "approachFour", ex.getMessage());
				}
			});
			
			futures.add(fb);
		}
		
		for (Future<?> fut : futures) {
			try {
				fut.get();
			} catch (Exception ex) {
				Utility.log("PaymentsProcessor", "approachFour", ex.getMessage());
			}
		}
		
		long end = System.nanoTime();
		
		executor.shutdown();
		
		String msg = String.format("=====> Average time - %d ms", (TimeUnit.NANOSECONDS.toMillis(end-start)/COUNT));
		Utility.log("PaymentsProcessor", "approachFour", msg);
	}
	
	// Using java completable futures
	public static void approachFive() {
		ClientServices.setUp();
		AccountServices.setUp();
		
		ExecutorService executor = Executors.newFixedThreadPool(POOLSZ);
		
		List<CompletableFuture<?>> cfList = new ArrayList<>();
		
		Utility.log("PaymentsProcessor", "approachFive", "=====> Ready to start");
		
		long start = System.nanoTime();
		
		for (Instruction inst : INSTRUCTIONS) {
			CompletableFuture<String> bcf = CompletableFuture.supplyAsync(() -> 
				ClientServices.lookupAccountByClientId(inst.getBuyerId()), executor);
			
			CompletableFuture<String> scf = CompletableFuture.supplyAsync(() -> 
				ClientServices.lookupAccountByClientId(inst.getSellerId()), executor);
			
			CompletableFuture<Settlement> bscf = bcf.thenCombineAsync(scf, (ba, sa) -> {
				Settlement smt = new Settlement();
				smt.setBuyerAccount(ba);
				smt.setSellerAccount(sa);
				smt.setAmount(inst.getAmount());
				return smt;
			});
			
			CompletableFuture<Void> fcf = bscf.thenAcceptAsync(smt -> {
				try {
					AccountServices.settle(smt.getBuyerAccount(), smt.getSellerAccount(), smt.getAmount());
				} catch (Exception ex) {
					Utility.log("PaymentsProcessor", "approachFive", ex.getMessage());
				}
			});
			
			cfList.add(fcf);
		}
		
		for (CompletableFuture<?> cf : cfList) {
			try {
				cf.get();
			} catch (Exception ex) {
				Utility.log("PaymentsProcessor", "approachFive", ex.getMessage());
			}
		}
		
		long end = System.nanoTime();
		
		executor.shutdown();
		
		String msg = String.format("=====> Average time - %d ms", (TimeUnit.NANOSECONDS.toMillis(end-start)/COUNT));
		Utility.log("PaymentsProcessor", "approachFive", msg);
	}
}
