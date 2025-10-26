package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.InvoiceDAO;
import com.example.quanlytoanha.dao.TransactionDAO;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.Transaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceService {
    private static InvoiceService instance;
    private final InvoiceDAO invoiceDAO;
    private final TransactionDAO transactionDAO;

    private InvoiceService() {
        invoiceDAO = InvoiceDAO.getInstance();
        transactionDAO = TransactionDAO.getInstance();
    }

    public static InvoiceService getInstance() {
        if (instance == null) {
            instance = new InvoiceService();
        }
        return instance;
    }

    public List<Invoice> getUnpaidInvoices(int residentId) {
        return invoiceDAO.getUnpaidInvoices(residentId);
    }

    public double getTotalDueAmount(int residentId) {
        return getUnpaidInvoices(residentId).stream()
                .mapToDouble(invoice -> invoice.getTotalAmount().doubleValue())
                .sum();
    }

    public List<Transaction> getTransactions(int residentId, LocalDate fromDate, LocalDate toDate) {
        return transactionDAO.getTransactions(residentId, fromDate, toDate);
    }

    public Transaction processPayment(int residentId, Invoice invoice, double amount) {
        // Create and save the transaction
        Transaction transaction = transactionDAO.createTransaction(residentId, invoice.getInvoiceId(), amount);

        if (transaction != null) {
            // Update the invoice status to PAID
            invoiceDAO.updateInvoiceStatus(invoice.getInvoiceId(), "PAID");
        }

        return transaction;
    }

    public String getLastPaymentInfo(int residentId) {
        return transactionDAO.getLastPaymentInfo(residentId);
    }

    public int getUnpaidInvoicesCount(int residentId) {
        return getUnpaidInvoices(residentId).size();
    }
}
