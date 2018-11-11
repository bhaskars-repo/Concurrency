package com.polarsparc.concurrency;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Settlement {
	private String buyerAccount = null;
	private String sellerAccount = null;
	private double amount = 0.0;
}
