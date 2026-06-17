package com.cog.propNest.module.rentDepositManagement.seeder;

import com.cog.propNest.module.rentDepositManagement.entity.DepositStatus;
import com.cog.propNest.module.rentDepositManagement.entity.InvoiceStatus;
import com.cog.propNest.module.rentDepositManagement.entity.PaymentMethod;
import com.cog.propNest.module.rentDepositManagement.entity.PaymentStatus;
import com.cog.propNest.module.rentDepositManagement.entity.RentInvoice;
import com.cog.propNest.module.rentDepositManagement.entity.RentPayment;
import com.cog.propNest.module.rentDepositManagement.entity.SecurityDepositLedger;
import com.cog.propNest.module.rentDepositManagement.repository.RentInvoiceRepository;
import com.cog.propNest.module.rentDepositManagement.repository.RentPaymentRepository;
import com.cog.propNest.module.rentDepositManagement.repository.SecurityDepositLedgerRepository;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Seeds the Rent &amp; Deposit Management tables with {@value #SEED_COUNT} randomly
 * generated records per table using JavaFaker. Seeding is skipped if the tables
 * already contain data, so it is safe to run repeatedly.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Number of rows generated per table. */
    private static final int SEED_COUNT = 1000;

    private final RentInvoiceRepository invoiceRepository;
    private final RentPaymentRepository paymentRepository;
    private final SecurityDepositLedgerRepository depositRepository;
    private final Faker faker = new Faker();

    public DataSeeder(RentInvoiceRepository invoiceRepository,
                      RentPaymentRepository paymentRepository,
                      SecurityDepositLedgerRepository depositRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.depositRepository = depositRepository;
    }

    @Override
    public void run(String... args) {
        if (invoiceRepository.count() > 0
                || paymentRepository.count() > 0
                || depositRepository.count() > 0) {
            log.info("Rent & Deposit data already present — skipping seed.");
            return;
        }

        List<RentInvoice> invoices = seedInvoices();
        seedPayments(invoices);
        seedDeposits();
        log.info("Seeded {} invoices, {} payments and {} deposits.",
                SEED_COUNT, SEED_COUNT, SEED_COUNT);
    }

    private List<RentInvoice> seedInvoices() {
        List<RentInvoice> invoices = new ArrayList<>(SEED_COUNT);
        for (int i = 0; i < SEED_COUNT; i++) {
            RentInvoice invoice = new RentInvoice();
            invoice.setLeaseId((long) faker.number().numberBetween(1, 500));
            invoice.setTenantId((long) faker.number().numberBetween(1, 500));
            invoice.setBillingMonth(randomBillingMonth());

            BigDecimal rentAmount = BigDecimal.valueOf(faker.number().numberBetween(5000, 60000));
            BigDecimal otherCharges = BigDecimal.valueOf(faker.number().numberBetween(0, 3000));
            invoice.setRentAmount(rentAmount);
            invoice.setOtherCharges(otherCharges);
            invoice.setTotalAmount(rentAmount.add(otherCharges));

            invoice.setDueDate(randomDate());
            invoice.setStatus(randomEnum(InvoiceStatus.values()));
            invoice.setPaymentMethod(randomEnum(PaymentMethod.values()).name());
            invoices.add(invoice);
        }
        return invoiceRepository.saveAll(invoices);
    }

    private void seedPayments(List<RentInvoice> invoices) {
        List<RentPayment> payments = new ArrayList<>(SEED_COUNT);
        for (int i = 0; i < SEED_COUNT; i++) {
            RentPayment payment = new RentPayment();
            // Reference an actual persisted invoice so the data stays consistent.
            RentInvoice invoice = invoices.get(faker.number().numberBetween(0, invoices.size()));
            payment.setInvoiceId(invoice.getInvoiceId());
            payment.setPaidAmount(BigDecimal.valueOf(faker.number().numberBetween(1000, 60000)));
            payment.setPaymentDate(randomDate());
            payment.setMethod(randomEnum(PaymentMethod.values()));
            payment.setStatus(randomEnum(PaymentStatus.values()));
            payment.setRemarks(faker.lorem().sentence());
            payments.add(payment);
        }
        paymentRepository.saveAll(payments);
    }

    private void seedDeposits() {
        List<SecurityDepositLedger> deposits = new ArrayList<>(SEED_COUNT);
        for (int i = 0; i < SEED_COUNT; i++) {
            SecurityDepositLedger deposit = new SecurityDepositLedger();
            deposit.setLeaseId((long) faker.number().numberBetween(1, 500));
            deposit.setTenantId((long) faker.number().numberBetween(1, 500));
            deposit.setDepositAmount(BigDecimal.valueOf(faker.number().numberBetween(10000, 120000)));
            deposit.setReceivedDate(randomDate());

            DepositStatus status = randomEnum(DepositStatus.values());
            deposit.setStatus(status);
            if (status == DepositStatus.FullyRefunded) {
                deposit.setRefundAmount(deposit.getDepositAmount());
                deposit.setRefundDate(randomDate());
            } else if (status == DepositStatus.PartiallyRefunded) {
                deposit.setRefundAmount(deposit.getDepositAmount()
                        .subtract(BigDecimal.valueOf(faker.number().numberBetween(1000, 5000))));
                deposit.setRefundDate(randomDate());
            } else {
                deposit.setRefundAmount(BigDecimal.ZERO);
            }
            deposit.setRemarks(faker.lorem().sentence());
            deposits.add(deposit);
        }
        depositRepository.saveAll(deposits);
    }

    private String randomBillingMonth() {
        int year = faker.number().numberBetween(2023, 2026);
        int month = faker.number().numberBetween(1, 13);
        return String.format("%d-%02d", year, month);
    }

    private LocalDate randomDate() {
        return faker.date().past(1095, TimeUnit.DAYS)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private <T> T randomEnum(T[] values) {
        return values[faker.number().numberBetween(0, values.length)];
    }
}
