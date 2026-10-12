package com.banksphere.service;

import com.banksphere.dto.*;
import com.banksphere.exception.*;
import com.banksphere.model.*;
import com.banksphere.repository.BankAccountRepository;
import com.banksphere.repository.BankTransactionRepository;
import com.banksphere.repository.TransferRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankingService {
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final DtoMapper mapper;
    private final AuditService auditService;

    @Transactional
    public TransactionDto deposit(MoneyOperationRequest request, String email) {
        BankAccount account = lockedOwnedActiveAccount(request.accountId(), email);
        BigDecimal amount = money(request.amount());
        account.setBalance(account.getBalance().add(amount));
        BankTransaction transaction = saveTransaction(account, TransactionType.DEPOSIT, amount,
                descriptionOrDefault(request.description(), "Demo deposit"), null);
        auditService.record(email, "DEPOSIT", "BankAccount", account.getId().toString(),
                "Simulated deposit " + amount);
        return mapper.toTransactionDto(transaction);
    }

    @Transactional
    public TransactionDto withdraw(MoneyOperationRequest request, String email) {
        BankAccount account = lockedOwnedActiveAccount(request.accountId(), email);
        BigDecimal amount = money(request.amount());
        requireSufficientFunds(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
        BankTransaction transaction = saveTransaction(account, TransactionType.WITHDRAWAL, amount,
                descriptionOrDefault(request.description(), "Demo withdrawal"), null);
        auditService.record(email, "WITHDRAWAL", "BankAccount", account.getId().toString(),
                "Simulated withdrawal " + amount);
        return mapper.toTransactionDto(transaction);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, String email) {
        if (transferRepository.existsByIdempotencyKey(request.idempotencyKey())) {
            throw new DuplicateTransferException("This transfer request was already submitted");
        }
        BankAccount fromProbe = accountRepository.findByIdAndUser_Email(request.fromAccountId(), email)
                .orElseThrow(() -> new ForbiddenOperationException("Source account is not owned by the current user"));
        BankAccount toProbe = accountRepository.findByAccountNumber(request.recipientAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient account not found"));
        if (fromProbe.getId().equals(toProbe.getId())) {
            throw new BadRequestException("Cannot transfer to the same account");
        }

        List<Long> lockOrder = new ArrayList<>(List.of(fromProbe.getId(), toProbe.getId()));
        lockOrder.sort(Comparator.naturalOrder());
        BankAccount first = accountRepository.lockById(lockOrder.get(0))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        BankAccount second = accountRepository.lockById(lockOrder.get(1))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        BankAccount from = first.getId().equals(fromProbe.getId()) ? first : second;
        BankAccount to = first.getId().equals(toProbe.getId()) ? first : second;

        assertOwned(from, email);
        assertActive(from);
        assertActive(to);
        BigDecimal amount = money(request.amount());
        requireSufficientFunds(from, amount);

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transfer transfer = transferRepository.save(Transfer.builder()
                .transferReference("TRF-" + UUID.randomUUID())
                .fromAccount(from)
                .toAccount(to)
                .amount(amount)
                .description(descriptionOrDefault(request.description(), "Demo transfer"))
                .status(TransferStatus.COMPLETED)
                .idempotencyKey(request.idempotencyKey())
                .build());
        saveTransaction(from, TransactionType.TRANSFER_OUT, amount, transfer.getDescription(), transfer);
        saveTransaction(to, TransactionType.TRANSFER_IN, amount, transfer.getDescription(), transfer);
        auditService.record(email, "TRANSFER", "Transfer", transfer.getTransferReference(),
                "Transferred demo funds to " + to.getAccountNumber());
        return new TransferResponse(transfer.getTransferReference(), from.getAccountNumber(), to.getAccountNumber(),
                amount, transfer.getStatus().name(), transfer.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> customerTransactions(String email, String query, TransactionType type,
                                                     LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return transactionRepository.findAll(transactionSpec(email, query, type, fromDate, toDate), pageable)
                .map(mapper::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> allTransactions(String query, TransactionType type, LocalDate fromDate,
                                                LocalDate toDate, Pageable pageable) {
        return transactionRepository.findAll(transactionSpec(null, query, type, fromDate, toDate), pageable)
                .map(mapper::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public byte[] statementCsv(String email, String query, TransactionType type, LocalDate fromDate, LocalDate toDate) {
        List<BankTransaction> transactions = transactionRepository.findAll(
                transactionSpec(email, query, type, fromDate, toDate), Sort.by(Sort.Direction.DESC, "occurredAt"));
        StringBuilder csv = new StringBuilder("Reference,Date,Account,Type,Amount,Balance After,Description\n");
        for (BankTransaction t : transactions) {
            csv.append(escape(t.getTransactionReference())).append(',')
                    .append(t.getOccurredAt()).append(',')
                    .append(escape(t.getAccount().getAccountNumber())).append(',')
                    .append(t.getType()).append(',')
                    .append(t.getAmount()).append(',')
                    .append(t.getBalanceAfter()).append(',')
                    .append(escape(t.getDescription())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] statementPdf(String email, String query, TransactionType type, LocalDate fromDate, LocalDate toDate) {
        List<BankTransaction> transactions = transactionRepository.findAll(
                transactionSpec(email, query, type, fromDate, toDate), Sort.by(Sort.Direction.DESC, "occurredAt"));
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("BankSphere Account Statement"));
            document.add(new Paragraph("Educational simulation only - not a real bank statement."));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("Date");
            table.addCell("Account");
            table.addCell("Type");
            table.addCell("Amount");
            table.addCell("Balance");
            for (BankTransaction transaction : transactions) {
                table.addCell(transaction.getOccurredAt().toString());
                table.addCell(transaction.getAccount().getAccountNumber());
                table.addCell(transaction.getType().name());
                table.addCell(transaction.getAmount().toPlainString());
                table.addCell(transaction.getBalanceAfter().toPlainString());
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new BadRequestException("Could not generate PDF statement");
        }
    }

    private BankTransaction saveTransaction(BankAccount account, TransactionType type, BigDecimal amount,
                                            String description, Transfer transfer) {
        return transactionRepository.save(BankTransaction.builder()
                .transactionReference("TXN-" + UUID.randomUUID())
                .account(account)
                .type(type)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .description(description)
                .transfer(transfer)
                .occurredAt(Instant.now())
                .build());
    }

    private BankAccount lockedOwnedActiveAccount(Long accountId, String email) {
        BankAccount account = accountRepository.lockById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        assertOwned(account, email);
        assertActive(account);
        return account;
    }

    private void assertOwned(BankAccount account, String email) {
        if (!account.getUser().getEmail().equals(email)) {
            throw new ForbiddenOperationException("Account is not owned by the current user");
        }
    }

    private void assertActive(BankAccount account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account " + account.getAccountNumber() + " is not active");
        }
    }

    private void requireSufficientFunds(BankAccount account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient demo funds");
        }
    }

    private BigDecimal money(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String descriptionOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private Specification<BankTransaction> transactionSpec(String email, String query, TransactionType type,
                                                           LocalDate fromDate, LocalDate toDate) {
        return (root, criteriaQuery, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            Join<BankTransaction, BankAccount> account = root.join("account");
            if (email != null) {
                predicates.add(cb.equal(account.get("user").get("email"), email));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"),
                        fromDate.atStartOfDay().toInstant(ZoneOffset.UTC)));
            }
            if (toDate != null) {
                predicates.add(cb.lessThan(root.get("occurredAt"),
                        toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
            }
            if (query != null && !query.isBlank()) {
                String like = "%" + query.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("transactionReference")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(account.get("accountNumber")), like)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")
                ? "\"" + escaped + "\""
                : escaped;
    }
}
