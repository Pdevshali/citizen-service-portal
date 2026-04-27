package com.pdev.ekyc_service.service;

import com.pdev.ekyc_service.dto.*;
import com.pdev.ekyc_service.kafka.events.KycInitiationEvent;

public interface EkycService {

    GenerateOtpResponse generateOtp(GenerateOtpRequest request);

    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

    KycStatusResponse getKycStatus(String citizenId);

    void handleKycInitiation(KycInitiationEvent event);
}
