package com.polarsparc.concurrency;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Instruction {
	private String buyerId = null;
	private String sellerId = null;
	private double amount = 0.0;
}
