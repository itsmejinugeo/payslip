package com.costco.eeterm.successfactors.pojo;

import java.util.List;
/**
 * This POJO is for Pay Advice Overview
 */
public class PayAdviceOverview extends Employee{
	
	private List<PayAdvice> payadviceList;
	
	public List<PayAdvice> getPayadviceList() {
		return payadviceList;
	}
	public void setPayadviceList(List<PayAdvice> payadviceList) {
		this.payadviceList = payadviceList;
	}
	
}
