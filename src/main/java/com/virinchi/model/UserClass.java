package com.virinchi.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserClass {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//GeneratedValue = auto_increment
	private int id; //primary key, auto_increment
	private String firstName;
	private String role;
	
	private boolean subscribed; // true = active subscription
    private String planType;    // BASIC, PRO, PREMIUM
    private LocalDate subscriptionStart;
    private LocalDate subscriptionEnd;

    // 🔹 Last Payment Info (latest transaction)
    private double lastPaymentAmount;
    private String lastPaymentStatus; // SUCCESS, FAILED, PENDING
    private String lastTransactionId;

	public boolean isSubscribed() {
		return subscribed;
	}
	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}
	public String getPlanType() {
		return planType;
	}
	public void setPlanType(String planType) {
		this.planType = planType;
	}
	public LocalDate getSubscriptionStart() {
		return subscriptionStart;
	}
	public void setSubscriptionStart(LocalDate subscriptionStart) {
		this.subscriptionStart = subscriptionStart;
	}
	public LocalDate getSubscriptionEnd() {
		return subscriptionEnd;
	}
	public void setSubscriptionEnd(LocalDate subscriptionEnd) {
		this.subscriptionEnd = subscriptionEnd;
	}
	public double getLastPaymentAmount() {
		return lastPaymentAmount;
	}
	public void setLastPaymentAmount(double lastPaymentAmount) {
		this.lastPaymentAmount = lastPaymentAmount;
	}
	public String getLastPaymentStatus() {
		return lastPaymentStatus;
	}
	public void setLastPaymentStatus(String lastPaymentStatus) {
		this.lastPaymentStatus = lastPaymentStatus;
	}
	public String getLastTransactionId() {
		return lastTransactionId;
	}
	public void setLastTransactionId(String lastTransactionId) {
		this.lastTransactionId = lastTransactionId;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	private String lastName;
	private String email;
	private String password;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setLastPaymentMethod(String paymentMethod) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}



