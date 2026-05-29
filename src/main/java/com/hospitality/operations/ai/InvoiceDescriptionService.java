package com.hospitality.operations.ai;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Stub service for generating AI-powered invoice descriptions.
 * Will be replaced with Groq AI integration in a later module.
 */
@Service
public class InvoiceDescriptionService {

    /**
     * Generates a text description for a bill based on the line items.
     * Currently returns a placeholder; Groq AI integration will replace this.
     */
    public String generateDescription(String reference, List<String> itemDescriptions) {
        if (itemDescriptions == null || itemDescriptions.isEmpty()) {
            return "Invoice " + reference;
        }
        return "Invoice " + reference + " — " + String.join(", ", itemDescriptions);
    }
}
