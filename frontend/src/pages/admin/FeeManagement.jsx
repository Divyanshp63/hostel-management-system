import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const FeeManagement = () => {
  const [fees, setFees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filters
  const [status, setStatus] = useState('');
  const [month, setMonth] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // Bulk Generator Modal
  const [showBulkModal, setShowBulkModal] = useState(false);
  const [bulkLoading, setBulkLoading] = useState(false);
  const [bulkData, setBulkData] = useState({
    month: new Date().toISOString().substring(0, 7), // e.g. '2026-10'
    dueDate: new Date(new Date().setDate(new Date().getDate() + 15)).toISOString().substring(0, 10),
    messFee: 2500,
    electricityFee: 500,
    maintenanceFee: 500,
  });

  const fetchFees = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/fees', {
        params: {
          search,
          status: status || undefined,
          month: month || undefined,
          page,
          size: 10,
        },
      });

      if (res.data?.data) {
        setFees(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch fee invoices.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFees();
  }, [page, status, month]);

  const handleBulkSubmit = async (e) => {
    e.preventDefault();
    try {
      setBulkLoading(true);
      setError('');
      const res = await api.post('/api/fees/bulk', bulkData);
      setSuccess(`Invoices generated successfully for ${res.data?.data?.length || 0} active residents!`);
      setShowBulkModal(false);
      setPage(0);
      fetchFees();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate bulk fees.');
    } finally {
      setBulkLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Fee Billing & Dues</h2>
          <p className="page-subtitle">Generate monthly hostel rent, monitor payments, and manage overdue balances</p>
        </div>
        <button
          className="btn btn-primary d-flex align-items-center gap-2 shadow-sm"
          onClick={() => setShowBulkModal(true)}
        >
          <i className="bi bi-magic"></i> Generate Monthly Invoices
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {/* Filters Bar */}
      <div className="custom-card mb-4">
        <div className="custom-card-body p-3">
          <div className="row g-2">
            <div className="col-md-5">
              <input
                type="text"
                className="form-control"
                placeholder="Search student name or admission no..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && fetchFees()}
              />
            </div>
            <div className="col-md-3">
              <input
                type="month"
                className="form-control"
                value={month}
                onChange={(e) => { setMonth(e.target.value); setPage(0); }}
              />
            </div>
            <div className="col-md-3">
              <select className="form-select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                <option value="">All Fee Statuses</option>
                <option value="PAID">Paid</option>
                <option value="PARTIALLY_PAID">Partially Paid</option>
                <option value="PENDING">Pending</option>
                <option value="OVERDUE">Overdue</option>
              </select>
            </div>
            <div className="col-md-1">
              <button className="btn btn-primary w-100" onClick={() => { setPage(0); fetchFees(); }}>
                <i className="bi bi-funnel-fill"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Fees Table */}
      <div className="custom-card">
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Retrieving fee records..." />
          ) : fees.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Student</th>
                    <th>Billing Month</th>
                    <th>Fee Breakdown</th>
                    <th>Total Invoiced</th>
                    <th>Paid</th>
                    <th>Balance Due</th>
                    <th>Due Date</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {fees.map((fee) => (
                    <tr key={fee.id}>
                      <td>
                        <div className="fw-semibold text-dark">{fee.studentName}</div>
                        <small className="text-muted">Adm: {fee.admissionNumber}</small>
                      </td>
                      <td className="fw-medium text-dark">{fee.month}</td>
                      <td className="small text-secondary">
                        Rent: ₹{fee.roomRent} &bull; Mess: ₹{fee.messFee} &bull; Maint: ₹{fee.maintenanceFee}
                      </td>
                      <td className="fw-bold text-dark">₹{fee.totalAmount?.toLocaleString()}</td>
                      <td className="fw-bold text-success">₹{fee.paidAmount?.toLocaleString()}</td>
                      <td className={`fw-bold ${fee.remainingAmount > 0 ? 'text-danger' : 'text-muted'}`}>
                        ₹{fee.remainingAmount?.toLocaleString()}
                      </td>
                      <td className="small text-secondary">{fee.dueDate}</td>
                      <td>
                        <span className={`badge-status badge-status-${fee.status.toLowerCase().replace('_', '-')}`}>
                          {fee.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No fee records found matching filters.</p>
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

      {/* Bulk Fee Generation Modal */}
      {showBulkModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleBulkSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">
                    <i className="bi bi-magic text-primary me-2"></i> Bulk Generate Monthly Fees
                  </h5>
                  <button type="button" className="btn-close" onClick={() => setShowBulkModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="alert alert-info small mb-3">
                    This will automatically create invoices for all students with <strong>APPROVED</strong> room allocations, pulling individual room rents dynamically.
                  </div>
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Billing Month *</label>
                      <input
                        type="month"
                        className="form-control"
                        value={bulkData.month}
                        onChange={(e) => setBulkData({ ...bulkData, month: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Payment Due Date *</label>
                      <input
                        type="date"
                        className="form-control"
                        value={bulkData.dueDate}
                        onChange={(e) => setBulkData({ ...bulkData, dueDate: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label small fw-semibold">Mess Fee (₹) *</label>
                      <input
                        type="number"
                        min="0"
                        className="form-control"
                        value={bulkData.messFee}
                        onChange={(e) => setBulkData({ ...bulkData, messFee: parseFloat(e.target.value) })}
                        required
                      />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label small fw-semibold">Electricity (₹) *</label>
                      <input
                        type="number"
                        min="0"
                        className="form-control"
                        value={bulkData.electricityFee}
                        onChange={(e) => setBulkData({ ...bulkData, electricityFee: parseFloat(e.target.value) })}
                        required
                      />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label small fw-semibold">Maintenance (₹) *</label>
                      <input
                        type="number"
                        min="0"
                        className="form-control"
                        value={bulkData.maintenanceFee}
                        onChange={(e) => setBulkData({ ...bulkData, maintenanceFee: parseFloat(e.target.value) })}
                        required
                      />
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowBulkModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={bulkLoading}>
                    {bulkLoading ? 'Generating Invoices...' : 'Generate All Invoices'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FeeManagement;
