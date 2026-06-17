package com.cog.propNest.module.rentDepositManagement.service;

import com.cog.propNest.module.rentDepositManagement.dto.CreatePaymentRequest;
import com.cog.propNest.module.rentDepositManagement.dto.PaymentResponse;
import com.cog.propNest.module.rentDepositManagement.dto.UpdatePaymentRequest;
import com.cog.propNest.module.rentDepositManagement.entity.PaymentMethod;
import com.cog.propNest.module.rentDepositManagement.entity.PaymentStatus;
import com.cog.propNest.module.rentDepositManagement.entity.RentPayment;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.InvoiceNotFoundException;
import com.cog.propNest.module.rentDepositManagement.exception.PaymentAlreadyReceivedException;
import com.cog.propNest.module.rentDepositManagement.exception.PaymentNotFoundException;
import com.cog.propNest.module.rentDepositManagement.repository.RentInvoiceRepository;
import com.cog.propNest.module.rentDepositManagement.repository.RentPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for recording and updating rent payments.
 */
@Service
@Transactional
public class RentPaymentService {

    private final RentPaymentRepository paymentRepository;
    private final RentInvoiceRepository invoiceRepository;

    public RentPaymentService(RentPaymentRepository paymentRepository,
                              RentInvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        require(request.getInvoiceId(), "invoiceId");
        require(request.getPaidAmount(), "paidAmount");
        require(request.getPaymentDate(), "paymentDate");
        require(request.getMethod(), "method");
        require(request.getStatus(), "status");

        if (!invoiceRepository.existsById(request.getInvoiceId())) {
            throw new InvoiceNotFoundException(request.getInvoiceId());
        }

        RentPayment payment = new RentPayment();
        payment.setInvoiceId(request.getInvoiceId());
        payment.setPaidAmount(request.getPaidAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setMethod(parseMethod(request.getMethod()));
        payment.setStatus(parseStatus(request.getStatus()));
        payment.setRemarks(request.getRemarks());

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        return PaymentResponse.from(findPayment(paymentId));
    }

    public PaymentResponse updatePayment(Long paymentId, UpdatePaymentRequest request) {
        RentPayment payment = findPayment(paymentId);
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        // A payment that has already been received is immutable.
        if (payment.getStatus() == PaymentStatus.Received) {
            throw new PaymentAlreadyReceivedException();
        }
        if (request.getStatus() != null) {
            payment.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getMethod() != null) {
            payment.setMethod(parseMethod(request.getMethod()));
        }
        if (request.getRemarks() != null) {
            payment.setRemarks(request.getRemarks());
        }
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    private RentPayment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private PaymentMethod parseMethod(String method) {
        try {
            return PaymentMethod.valueOf(method);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid payment method: " + method);
        }
    }

    private PaymentStatus parseStatus(String status) {
        try {
            return PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid payment status: " + status);
        }
    }

    private void require(Object value, String field) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            throw new BadRequestException(field + " is required");
        }
    }
}
