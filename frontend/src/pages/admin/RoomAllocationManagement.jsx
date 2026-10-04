import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const RoomAllocationManagement = () => {
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filters
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // Modal actions
  const [actionLoading, setActionLoading] = useState(false);
  const [selectedAllocation, setSelectedAllocation] = useState(null);
  const [actionType, setActionType] = useState(null); // 'APPROVE' | 'REJECT' | 'VACATE'
  const [adminRemarks, setAdminRemarks] = useState('');

  const fetchAllocations = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/allocations', {
        params: {
          search,
          status: status || undefined,
          page,
          size: 10,
        },
      });

      if (res.data?.data) {
        setAllocations(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve allocations.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllocations();
  }, [page, status]);

  const handleOpenActionModal = (alloc, type) => {
    setSelectedAllocation(alloc);
    setActionType(type);
    setAdminRemarks(type === 'APPROVE' ? 'Approved by Warden' : '');
  };

  const handleExecuteAction = async () => {
    if (!selectedAllocation) return;
    try {
      setActionLoading(true);
      setError('');

      if (actionType === 'APPROVE') {
        await api.post('/api/allocations/approve', {
          allocationId: selectedAllocation.id,
          remarks: adminRemarks,
        });
        setSuccess(`Allocation for Room ${selectedAllocation.roomNumber} approved successfully!`);
      } else if (actionType === 'REJECT') {
        if (!adminRemarks.trim()) {
          setError('Please provide a reason for rejecting this allocation request.');
          setActionLoading(false);
          return;
        }
        await api.post('/api/allocations/reject', {
          allocationId: selectedAllocation.id,
          rejectionReason: adminRemarks,
        });
        setSuccess(`Allocation request rejected.`);
      } else if (actionType === 'VACATE') {
        await api.post('/api/allocations/vacate', null, {
          params: { allocationId: selectedAllocation.id },
        });
        setSuccess(`Room ${selectedAllocation.roomNumber} marked vacated. Bed count updated.`);
      }

      setSelectedAllocation(null);
      setActionType(null);
      fetchAllocations();
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed.');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Room Allocations Workflow</h2>
          <p className="page-subtitle">Review resident applications, assign bed space, and manage room departures</p>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {/* Filter Tabs */}
      <div className="custom-card mb-4">
        <div className="custom-card-body p-3 d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div className="btn-group">
            <button
              className={`btn btn-sm ${status === '' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => { setStatus(''); setPage(0); }}
            >
              All Records
            </button>
            <button
              className={`btn btn-sm ${status === 'PENDING' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => { setStatus('PENDING'); setPage(0); }}
            >
              Pending Approval
            </button>
            <button
              className={`btn btn-sm ${status === 'APPROVED' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => { setStatus('APPROVED'); setPage(0); }}
            >
              Active Residents
            </button>
            <button
              className={`btn btn-sm ${status === 'VACATED' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => { setStatus('VACATED'); setPage(0); }}
            >
              Vacated
            </button>
          </div>

          <div className="d-flex gap-2" style={{ maxWidth: '350px', width: '100%' }}>
            <input
              type="text"
              className="form-control form-control-sm"
              placeholder="Search student or room..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchAllocations()}
            />
            <button className="btn btn-sm btn-primary" onClick={() => { setPage(0); fetchAllocations(); }}>
              <i className="bi bi-search"></i>
            </button>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="custom-card">
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Loading room allocations..." />
          ) : allocations.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Student Details</th>
                    <th>Requested Room</th>
                    <th>Request Date</th>
                    <th>Start & End Date</th>
                    <th>Status</th>
                    <th>Remarks</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {allocations.map((alloc) => (
                    <tr key={alloc.id}>
                      <td>
                        <div className="fw-semibold text-dark">{alloc.studentName}</div>
                        <small className="text-muted">Adm: {alloc.admissionNumber}</small>
                      </td>
                      <td>
                        <span className="badge bg-primary fs-6">Room {alloc.roomNumber}</span>
                        <div className="small text-muted">{alloc.roomType}</div>
                      </td>
                      <td className="small">{new Date(alloc.createdAt).toLocaleDateString()}</td>
                      <td className="small">
                        <div>From: {alloc.startDate || 'Immediate'}</div>
                        {alloc.endDate && <div className="text-muted">To: {alloc.endDate}</div>}
                      </td>
                      <td>
                        <span className={`badge-status badge-status-${alloc.status.toLowerCase()}`}>
                          {alloc.status}
                        </span>
                      </td>
                      <td className="small text-secondary" style={{ maxWidth: '200px' }}>
                        {alloc.remarks || '-'}
                      </td>
                      <td>
                        {alloc.status === 'PENDING' && (
                          <div className="btn-group">
                            <button
                              className="btn btn-sm btn-success"
                              onClick={() => handleOpenActionModal(alloc, 'APPROVE')}
                            >
                              <i className="bi bi-check-lg me-1"></i> Approve
                            </button>
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => handleOpenActionModal(alloc, 'REJECT')}
                            >
                              <i className="bi bi-x-lg me-1"></i> Reject
                            </button>
                          </div>
                        )}
                        {alloc.status === 'APPROVED' && (
                          <button
                            className="btn btn-sm btn-outline-warning text-dark"
                            onClick={() => handleOpenActionModal(alloc, 'VACATE')}
                          >
                            <i className="bi bi-box-arrow-left me-1"></i> Vacate Room
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No room allocation records found.</p>
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

      {/* Action Confirmation Modal */}
      {selectedAllocation && actionType && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <div className="modal-header">
                <h5 className="modal-title fw-bold">
                  {actionType === 'APPROVE' && 'Approve Room Allocation'}
                  {actionType === 'REJECT' && 'Reject Allocation Request'}
                  {actionType === 'VACATE' && 'Mark Room as Vacated'}
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => { setSelectedAllocation(null); setActionType(null); }}
                ></button>
              </div>
              <div className="modal-body p-4">
                <div className="p-3 bg-light rounded-3 mb-3 small">
                  <div><strong>Student:</strong> {selectedAllocation.studentName} ({selectedAllocation.admissionNumber})</div>
                  <div><strong>Room:</strong> Room {selectedAllocation.roomNumber} ({selectedAllocation.roomType})</div>
                </div>

                {actionType !== 'VACATE' ? (
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">
                      {actionType === 'REJECT' ? 'Rejection Reason *' : 'Warden Approval Remarks'}
                    </label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder={actionType === 'REJECT' ? 'e.g. Bed capacity exhausted or invalid course status' : 'Optional notes...'}
                      value={adminRemarks}
                      onChange={(e) => setAdminRemarks(e.target.value)}
                    ></textarea>
                  </div>
                ) : (
                  <p className="text-secondary small mb-0">
                    Are you sure you want to mark this resident as vacated? The occupied bed count of Room {selectedAllocation.roomNumber} will automatically be decremented by 1.
                  </p>
                )}
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => { setSelectedAllocation(null); setActionType(null); }}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className={`btn ${
                    actionType === 'APPROVE' ? 'btn-success' : actionType === 'REJECT' ? 'btn-danger' : 'btn-warning'
                  }`}
                  onClick={handleExecuteAction}
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Processing...' : `Confirm ${actionType}`}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RoomAllocationManagement;
