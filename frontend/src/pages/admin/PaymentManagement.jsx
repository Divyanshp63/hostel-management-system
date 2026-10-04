import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const PaymentManagement = () => {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filters
  const [search, setSearch] = useState('');
  const [method, setMethod] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // Selected payment for receipt preview modal
  const [selectedReceipt, setSelectedReceipt] = useState(null);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/payments', {
        params: {
          search,
          method: method || undefined,
          status: status || undefined,
          page,
          size: 10,
        },
      });

      if (res.data?.data) {
        setPayments(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load payments ledger.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [page, method, status]);

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Payments & Financial Ledger</h2>
          <p className="page-subtitle">Track transaction audit logs, payment gateways, and verified receipts</p>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      {/* Filter Bar */}
      <div className="custom-card mb-4">
        <div className="custom-card-body p-3">
          <div className="row g-2">
            <div className="col-md-5">
              <input
                type="text"
                className="form-control"
                placeholder="Search transaction ID, student name, admission no..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && fetchPayments()}
              />
            </div>
            <div className="col-md-3">
              <select className="form-select" value={method} onChange={(e) => { setMethod(e.target.value); setPage(0); }}>
                <option value="">All Payment Modes</option>
                <option value="UPI">UPI</option>
                <option value="CREDIT_CARD">Credit Card</option>
                <option value="DEBIT_CARD">Debit Card</option>
                <option value="NET_BANKING">Net Banking</option>
                <option value="CASH">Cash Deposit</option>
              </select>
            </div>
            <div className="col-md-3">
              <select className="form-select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                <option value="">All Transaction Statuses</option>
                <option value="SUCCESS">Success</option>
                <option value="PENDING">Pending</option>
                <option value="FAILED">Failed</option>
              </select>
            </div>
            <div className="col-md-1">
              <button className="btn btn-primary w-100" onClick={() => { setPage(0); fetchPayments(); }}>
                <i className="bi bi-funnel-fill"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Payments Table */}
      <div className="custom-card">
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Loading payments ledger..." />
          ) : payments.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Txn ID</th>
                    <th>Student Name</th>
                    <th>Billing Month</th>
                    <th>Payment Method</th>
                    <th>Amount Paid</th>
                    <th>Transaction Time</th>
                    <th>Status</th>
                    <th>Receipt</th>
                  </tr>
                </thead>
                <tbody>
                  {payments.map((p) => (
                    <tr key={p.id}>
                      <td className="fw-bold font-monospace text-primary">{p.transactionId}</td>
                      <td>
                        <div className="fw-semibold text-dark">{p.studentName}</div>
                        <small className="text-muted">Adm: {p.admissionNumber}</small>
                      </td>
                      <td className="fw-medium">{p.feeMonth}</td>
                      <td>
                        <span className="badge bg-light text-dark border">
                          <i className="bi bi-credit-card me-1"></i> {p.paymentMethod}
                        </span>
                      </td>
                      <td className="fw-bold fs-6 text-success">₹{p.amount?.toLocaleString()}</td>
                      <td className="small text-secondary">
                        {new Date(p.paymentDate).toLocaleString()}
                      </td>
                      <td>
                        <span className={`badge-status badge-status-${p.paymentStatus.toLowerCase()}`}>
                          {p.paymentStatus}
                        </span>
                      </td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-primary"
                          onClick={() => setSelectedReceipt(p)}
                        >
                          <i className="bi bi-receipt"></i>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No payment transactions recorded.</p>
          )}

          {totalPages > 1 && (
            <div className="d-flex justify-content-between align-items-center p-3 border-top">
              <span className="text-muted small">Page {page + 1} of {totalPages}</span>
              <div className="btn-group">
                <button
                  className="btn btn-sm btn-outline-secondary"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Previous
                </button>
                <button
                  className="btn btn-sm btn-outline-secondary"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Receipt Details Modal */}
      {selectedReceipt && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <div className="modal-header border-bottom-0 pb-0">
                <button type="button" className="btn-close" onClick={() => setSelectedReceipt(null)}></button>
              </div>
              <div className="modal-body p-4 text-center">
                <div className="d-inline-flex p-3 bg-success-subtle text-success rounded-circle mb-3">
                  <i className="bi bi-check-circle-fill fs-1"></i>
                </div>
                <h4 className="fw-bold text-dark mb-1">Hostel Fee Receipt</h4>
                <p className="text-muted small mb-4">Official Payment Confirmation</p>

                <div className="bg-light p-3 rounded-3 text-start small mb-4">
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">Transaction ID:</span>
                    <strong className="font-monospace">{selectedReceipt.transactionId}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">Student Name:</span>
                    <strong>{selectedReceipt.studentName}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">Admission No:</span>
                    <strong>{selectedReceipt.admissionNumber}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">Month:</span>
                    <strong>{selectedReceipt.feeMonth}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">Mode of Payment:</span>
                    <strong>{selectedReceipt.paymentMethod}</strong>
                  </div>
                  <div className="d-flex justify-content-between border-top pt-2 mt-2">
                    <span className="fw-bold">Amount Paid:</span>
                    <strong className="text-success fs-6">₹{selectedReceipt.amount?.toLocaleString()}</strong>
                  </div>
                </div>

                <div className="d-grid gap-2">
                  <button
                    className="btn btn-primary"
                    onClick={async () => {
                      try {
                        const res = await api.get(`/api/reports/fee-receipt/${selectedReceipt.id}`, { responseType: 'blob' });
                        const file = new Blob([res.data], { type: 'application/pdf' });
                        const fileURL = URL.createObjectURL(file);
                        window.open(fileURL, '_blank');
                      } catch (err) {
                        alert('Failed to download PDF receipt.');
                      }
                    }}
                  >
                    <i className="bi bi-file-earmark-pdf-fill me-1"></i> Download PDF Receipt
                  </button>
                  <button className="btn btn-outline-secondary" onClick={() => setSelectedReceipt(null)}>
                    Close
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PaymentManagement;
