import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentFees = () => {
  const [fees, setFees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Payment Modal State
  const [selectedFee, setSelectedFee] = useState(null);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [submittingPayment, setSubmittingPayment] = useState(false);
  const [receipt, setReceipt] = useState(null);

  const fetchMyFees = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/fees/my');
      if (res.data?.data) {
        setFees(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve fee records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyFees();
  }, []);

  const handleOpenPayModal = (fee) => {
    setSelectedFee(fee);
    setPaymentAmount(fee.remainingAmount);
    setPaymentMethod('UPI');
    setReceipt(null);
  };

  const handleProcessPayment = async (e) => {
    e.preventDefault();
    if (!selectedFee) return;

    const amountNum = parseFloat(paymentAmount);
    if (isNaN(amountNum) || amountNum <= 0) {
      setError('Please enter a valid payment amount.');
      return;
    }

    if (amountNum > selectedFee.remainingAmount) {
      setError(`Payment cannot exceed outstanding balance of ₹${selectedFee.remainingAmount}`);
      return;
    }

    try {
      setSubmittingPayment(true);
      setError('');
      const res = await api.post('/api/payments', {
        feeId: selectedFee.id,
        amount: amountNum,
        paymentMethod: paymentMethod,
      });

      setSuccess('Payment processed successfully!');
      setReceipt(res.data?.data);
      fetchMyFees();
    } catch (err) {
      setError(err.response?.data?.message || 'Payment processing failed.');
    } finally {
      setSubmittingPayment(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">My Fees & Payment Portal</h2>
          <p className="page-subtitle">View monthly hostel rent, mess dues, and pay securely online</p>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      <div className="custom-card">
        <div className="custom-card-header">
          <h5 className="custom-card-title">Fee Invoices & Dues</h5>
        </div>
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Retrieving your fee statements..." />
          ) : fees.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Billing Month</th>
                    <th>Rent</th>
                    <th>Mess Fee</th>
                    <th>Maintenance</th>
                    <th>Total Invoiced</th>
                    <th>Paid</th>
                    <th>Balance Due</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {fees.map((fee) => (
                    <tr key={fee.id}>
                      <td className="fw-bold">{fee.month}</td>
                      <td>₹{fee.roomRent}</td>
                      <td>₹{fee.messFee}</td>
                      <td>₹{fee.maintenanceFee}</td>
                      <td className="fw-bold text-dark">₹{fee.totalAmount?.toLocaleString()}</td>
                      <td className="fw-bold text-success">₹{fee.paidAmount?.toLocaleString()}</td>
                      <td className={`fw-bold ${fee.remainingAmount > 0 ? 'text-danger' : 'text-muted'}`}>
                        ₹{fee.remainingAmount?.toLocaleString()}
                      </td>
                      <td>
                        <span className={`badge-status badge-status-${fee.status.toLowerCase().replace('_', '-')}`}>
                          {fee.status}
                        </span>
                      </td>
                      <td>
                        {fee.remainingAmount > 0 ? (
                          <button
                            className="btn btn-sm btn-primary px-3 shadow-sm"
                            onClick={() => handleOpenPayModal(fee)}
                          >
                            <i className="bi bi-credit-card me-1"></i> Pay Now
                          </button>
                        ) : (
                          <span className="badge bg-success-subtle text-success border border-success-subtle p-2">
                            <i className="bi bi-check2-circle me-1"></i> Paid in Full
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No fee invoices issued for your account yet.</p>
          )}
        </div>
      </div>

      {/* Online Payment Modal */}
      {selectedFee && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              {!receipt ? (
                <form onSubmit={handleProcessPayment}>
                  <div className="modal-header">
                    <h5 className="modal-title fw-bold">Pay Hostel Fee - {selectedFee.month}</h5>
                    <button type="button" className="btn-close" onClick={() => setSelectedFee(null)}></button>
                  </div>
                  <div className="modal-body p-4">
                    <div className="p-3 bg-light rounded-3 mb-3 small">
                      <div className="d-flex justify-content-between mb-1">
                        <span>Total Invoiced:</span>
                        <strong>₹{selectedFee.totalAmount?.toLocaleString()}</strong>
                      </div>
                      <div className="d-flex justify-content-between mb-1">
                        <span>Already Paid:</span>
                        <strong className="text-success">₹{selectedFee.paidAmount?.toLocaleString()}</strong>
                      </div>
                      <div className="d-flex justify-content-between border-top pt-1">
                        <span className="fw-bold">Balance Outstanding:</span>
                        <strong className="text-danger">₹{selectedFee.remainingAmount?.toLocaleString()}</strong>
                      </div>
                    </div>

                    <div className="mb-3">
                      <label className="form-label small fw-semibold">Payment Amount (₹) *</label>
                      <input
                        type="number"
                        min="1"
                        max={selectedFee.remainingAmount}
                        className="form-control form-control-lg fw-bold text-primary"
                        value={paymentAmount}
                        onChange={(e) => setPaymentAmount(e.target.value)}
                        required
                      />
                      <small className="text-muted">You can pay in full or enter partial installment.</small>
                    </div>

                    <div className="mb-3">
                      <label className="form-label small fw-semibold">Payment Gateway / Method *</label>
                      <select
                        className="form-select"
                        value={paymentMethod}
                        onChange={(e) => setPaymentMethod(e.target.value)}
                      >
                        <option value="UPI">UPI (GooglePay / PhonePe / Paytm)</option>
                        <option value="DEBIT_CARD">Debit Card (Visa / MasterCard / RuPay)</option>
                        <option value="CREDIT_CARD">Credit Card</option>
                        <option value="NET_BANKING">Net Banking (All Major Banks)</option>
                      </select>
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button type="button" className="btn btn-secondary" onClick={() => setSelectedFee(null)}>
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={submittingPayment}>
                      {submittingPayment ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                          Processing Payment...
                        </>
                      ) : (
                        `Pay ₹${parseFloat(paymentAmount || 0).toLocaleString()}`
                      )}
                    </button>
                  </div>
                </form>
              ) : (
                /* Payment Success Receipt Screen */
                <div className="p-4 text-center">
                  <div className="d-inline-flex p-3 bg-success-subtle text-success rounded-circle mb-3">
                    <i className="bi bi-check-circle-fill fs-1"></i>
                  </div>
                  <h4 className="fw-bold text-dark mb-1">Payment Successful!</h4>
                  <p className="text-muted small mb-4">Official Receipt Issued by HostelOps</p>

                  <div className="bg-light p-3 rounded-3 text-start small mb-4">
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">Txn ID:</span>
                      <strong className="font-monospace text-primary">{receipt.transactionId}</strong>
                    </div>
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">Month:</span>
                      <strong>{receipt.feeMonth}</strong>
                    </div>
                    <div className="d-flex justify-content-between mb-2">
                      <span className="text-muted">Amount Paid:</span>
                      <strong className="text-success fs-6">₹{receipt.amount?.toLocaleString()}</strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span className="text-muted">Date & Time:</span>
                      <strong>{new Date(receipt.paymentDate).toLocaleString()}</strong>
                    </div>
                  </div>

                  <div className="d-grid gap-2">
                    <button
                      className="btn btn-primary"
                      onClick={async () => {
                        try {
                          const res = await api.get(`/api/reports/fee-receipt/${receipt.id}`, { responseType: 'blob' });
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
                    <button className="btn btn-outline-secondary" onClick={() => { setSelectedFee(null); setReceipt(null); }}>
                      Done
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentFees;
