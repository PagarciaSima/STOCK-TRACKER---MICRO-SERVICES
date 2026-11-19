package com.trading.notification.service.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

	@KafkaListener(topics = "order.executed")
    public void consumeOrderExecuted(
            @Payload Map<String, Object> payload){

        try{
        	String userId = (String) payload.get("userId");
        	String type = (String) payload.get("type");
        	String symbol = (String) payload.get("symbol");
        	Object quantity = payload.get("quantity");
        	Object price = payload.get("price");
        	Object totalAmount = payload.get("totalAmount");
        	
        	String notificationTitle = type.equals("BUY")
	                ? "ORDER EXECUTED - BUY"
	                : "ORDER EXECUTED - SELL";
			String notificationMessage = String.format("%s %s shares of %s at %s. Total: %s", type, quantity, symbol,
					price, totalAmount);
        	sendNotification(userId, notificationTitle, notificationMessage);
        }
        catch (Exception e){
        	log.error("Error: {}", e.getMessage());
        }
    }
	
	@KafkaListener(topics = "order.failed")
	public void consumeOrderFailed(
	        @Payload Map<String, Object> payload){

	    try{
	    	String userId = (String) payload.get("userId");
	    	String symbol = (String) payload.get("symbol");
	    	String reason = (String) payload.get("reason");
	    	String notificationMessage = String.format("Your order for %s failed. Reason: %s",
	           symbol, reason
	        );
	    	sendNotification(userId, "Order Failed", notificationMessage);
	    	        
	    }
	    catch (Exception e){
	    	log.error("Error: {}", e.getMessage());
	    }
	}
	
	@KafkaListener(topics = "user.registered")
	public void consumeUserRegistered(
	        @Payload Map<String, Object> payload){

	    try{
	    	String userId = (String) payload.get("userId");
	    	String firstName = (String) payload.get("firstName");
	    	Object walletBalance = payload.get("walletBalance");
	    	String notificationTitle = "Welcome to Stock Trading Platform!";
	    	String notificationMessage = String.format(
	                "Welcome %s!  %s added to your wallet.",
	                firstName, walletBalance
	        );
	    	sendNotification(userId, notificationTitle, notificationMessage);
	    	        
	    }
	    catch (Exception e){
	    	log.error("Error: {}", e.getMessage());
	    }
	}

    private void sendNotification(String userId, String title, String message){
    	log.info("--------------------------------------------------");
    	log.info("NOTIFICATION SENT");
    	log.info("TO USER: {}", userId);
    	log.info("TITLE: {}", title);
    	log.info("MESSAGE: {}", message);
    	log.info("--------------------------------------------------");
    }

}