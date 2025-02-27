package br.com.lucasabreu.events.exception;

public class SubscriptionConflictException extends RuntimeException {
	public SubscriptionConflictException(String msg) {
		super(msg);
	}
}
