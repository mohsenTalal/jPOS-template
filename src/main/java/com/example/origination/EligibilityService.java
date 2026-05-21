package com.example.origination;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class EligibilityService {

    public EligibilityResult validate(CardOriginationRequest req) {
        EligibilityResult result = new EligibilityResult();
        List<String> failures = new ArrayList<>();

        // KYC status check (connect to your CRM/KYC system)
        if (!checkKycStatus(req.getCustomerId())) {
            failures.add("KYC_NOT_VERIFIED");
        }

        // Account status check (connect to Core Banking)
        if (!checkAccountStatus(req.getAccountNumber())) {
            failures.add("ACCOUNT_NOT_ACTIVE");
        }

        // Customer restrictions check
        if (checkCustomerRestrictions(req.getCustomerId())) {
            failures.add("CUSTOMER_RESTRICTED");
        }

        // Sanctions / AML check
        if (checkSanctions(req.getCustomerId())) {
            failures.add("SANCTIONS_HIT");
        }

        // Existing card count check (max 3 cards per customer)
        int existingCards = getExistingCardCount(req.getCustomerId());
        if (existingCards >= 3) {
            failures.add("MAX_CARD_LIMIT_REACHED");
        }

        // Credit/debit eligibility
        if ("CREDIT".equals(req.getCardType()) &&
                !checkCreditEligibility(req.getCustomerId())) {
            failures.add("CREDIT_NOT_ELIGIBLE");
        }

        result.setEligible(failures.isEmpty());
        result.setFailureReasons(failures);
        return result;
    }

    // ── Stub methods — connect to real systems in production ──────────────

    private boolean checkKycStatus(String customerId) {
        // TODO: Connect to nera SMART / CRM
        return true; // Assume verified for sandbox
    }

    private boolean checkAccountStatus(String accountNumber) {
        // TODO: Connect to Core Banking
        return true; // Assume active for sandbox
    }

    private boolean checkCustomerRestrictions(String customerId) {
        // TODO: Connect to nera CRM
        return false; // No restrictions for sandbox
    }

    private boolean checkSanctions(String customerId) {
        // TODO: Connect to nera SMART AML
        return false; // No sanctions for sandbox
    }

    private int getExistingCardCount(String customerId) {
        // TODO: Query CMS card store
        return 0;
    }

    private boolean checkCreditEligibility(String customerId) {
        // TODO: Connect to credit/loan system
        return true;
    }
}