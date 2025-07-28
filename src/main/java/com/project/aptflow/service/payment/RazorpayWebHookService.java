package com.project.aptflow.service.payment;

public interface RazorpayWebHookService {
    boolean verifySignature(String payload, String signature);

    void processEvent(String payload);
}
