import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const LeaveManagement = () => {
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filters
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchLeaves = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/leaves', {
        params: {
          search,
          status: status || undefined,
          page,
          size: 10,
        },
      });

      if (res.data?.data) {
        setLeaves(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load leave applications.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLeaves();
  }, [page, status]);

  const handleApprove = async (id) => {
    try {
      await api.post('/api/leaves/approve', { leaveId: id, remarks: 'Leave granted by Warden' });
      setSuccess('Leave request approved.');
      fetchLeaves();
    } catch (err) {
      setError(err.response?.data?.message || 'Approval failed.');
    }
  };

  const handleReject = async (id) => {
    const reason = window.prompt('Enter reason for rejecting leave request:');
    if (!reason) return;
    try {
      await api.post('/api/leaves/reject', { leaveId: id, remarks: reason });
      setSuccess('Leave request rejected.');
      fetchLeaves();
    } catch (err) {
      setError(err.response?.data?.message || 'Rejection failed.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Leave & Absence Applications</h2>
          <p className="page-subtitle">Review student home pass requests, track out-of-campus periods, and monitor approvals</p>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {/* Filter Tabs */}
      <div className="custom-card mb-4">
        <div className="custom-card-body p-3 d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div className="btn-group">
            <button className={`btn btn-sm ${status === '' ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => { setStatus(''); setPage(0); }}>
              All Leaves
            </button>
            <button className={`btn btn-sm ${status === 'PENDING' ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => { setStatus('PENDING'); setPage(0); }}>
              Pending
            </button>
            <button className={`btn btn-sm ${status === 'APPROVED' ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => { setStatus('APPROVED'); setPage(0); }}>
              Approved
            </button>
            <button className={`btn btn-sm ${status === 'REJECTED' ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => { setStatus('REJECTED'); setPage(0); }}>
              Rejected
            </button>
          </div>

          <div className="d-flex gap-2" style={{ maxWidth: '350px', width: '100%' }}>
            <input
              type="text"
              className="form-control form-control-sm"
              placeholder="Search student or reason..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchLeaves()}
            />
            <button className="btn btn-sm btn-primary" onClick={() => { setPage(0); fetchLeaves(); }}>
              <i className="bi bi-search"></i>
            </button>
          </div>
        </div>
      </div>

      {/* Leaves Table */}
      <div className="custom-card">
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Fetching leave requests..." />
          ) : leaves.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Student Name</th>
                    <th>Leave Type</th>
                    <th>Duration</th>
                    <th>Days</th>
                    <th>Reason</th>
                    <th>Emergency Contact</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {leaves.map((l) => (
                    <tr key={l.id}>
                      <td>
                        <div className="fw-semibold text-dark">{l.studentName}</div>
                        <small className="text-muted">Adm: {l.admissionNumber}</small>
                      </td>
                      <td>
                        <span className="badge bg-light text-dark border">
                          {l.leaveType?.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="small">
                        <div>{l.fromDate} to {l.toDate}</div>
                      </td>
                      <td className="fw-bold">{l.totalDays} days</td>
                      <td className="small text-secondary" style={{ maxWidth: '240px' }}>
                        {l.reason}
                      </td>
                      <td className="small"><i className="bi bi-telephone me-1"></i>{l.contactNumber}</td>
                      <td>
                        <span className={`badge-status badge-status-${l.status.toLowerCase()}`}>
                          {l.status}
                        </span>
                      </td>
                      <td>
                        {l.status === 'PENDING' && (
                          <div className="btn-group">
                            <button className="btn btn-sm btn-success" onClick={() => handleApprove(l.id)}>
                              Approve
                            </button>
                            <button className="btn btn-sm btn-danger" onClick={() => handleReject(l.id)}>
                              Reject
                            </button>
                          </div>
                        )}
                        {l.status !== 'PENDING' && (
                          <span className="small text-muted">{l.adminRemarks || '-'}</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No leave requests found.</p>
          )}

          {totalPages > 1 && (
            <div className="d-flex justify-content-between align-items-center p-3 border-top">
              <span className="text-muted small">Page {page + 1} of {totalPages}</span>
              <div className="btn-group">
                <button className="btn btn-sm btn-outline-secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                  Previous
                </button>
                <button className="btn btn-sm btn-outline-secondary" disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default LeaveManagement;
