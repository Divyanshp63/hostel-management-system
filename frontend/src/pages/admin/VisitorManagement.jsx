import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const VisitorManagement = () => {
  const [visitors, setVisitors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filters
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchVisitors = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/visitors', {
        params: {
          search,
          status: status || undefined,
          page,
          size: 10,
        },
      });

      if (res.data?.data) {
        setVisitors(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch visitor logs.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVisitors();
  }, [page, status]);

  const handleApprove = async (id) => {
    try {
      await api.post('/api/visitors/approve', { requestId: id, remarks: 'Approved entry' });
      setSuccess('Visitor request approved.');
      fetchVisitors();
    } catch (err) {
      setError(err.response?.data?.message || 'Approval failed.');
    }
  };

  const handleReject = async (id) => {
    const reason = window.prompt('Enter reason for rejecting visitor request:');
    if (!reason) return;
    try {
      await api.post('/api/visitors/reject', { requestId: id, rejectionReason: reason });
      setSuccess('Visitor request rejected.');
      fetchVisitors();
    } catch (err) {
      setError(err.response?.data?.message || 'Rejection failed.');
    }
  };

  const handleComplete = async (id) => {
    try {
      await api.post('/api/visitors/complete', null, { params: { requestId: id } });
      setSuccess('Visitor checked out successfully.');
      fetchVisitors();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to complete visit.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Visitor Passes & Gate Control</h2>
          <p className="page-subtitle">Verify guest identity, approve entry requests, and record visitor check-outs</p>
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
              All Passes
            </button>
            <button
              className={`btn btn-sm ${status === 'PENDING' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => { setStatus('PENDING'); setPage(0); }}
            >
              Pending
            </button>
            <button
              className={`btn btn-sm ${status === 'APPROVED' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => { setStatus('APPROVED'); setPage(0); }}
            >
              Approved / Inside
            </button>
            <button
              className={`btn btn-sm ${status === 'COMPLETED' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => { setStatus('COMPLETED'); setPage(0); }}
            >
              Checked Out
            </button>
          </div>

          <div className="d-flex gap-2" style={{ maxWidth: '350px', width: '100%' }}>
            <input
              type="text"
              className="form-control form-control-sm"
              placeholder="Search visitor, student, phone..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchVisitors()}
            />
            <button className="btn btn-sm btn-primary" onClick={() => { setPage(0); fetchVisitors(); }}>
              <i className="bi bi-search"></i>
            </button>
          </div>
        </div>
      </div>

      {/* Visitors Table */}
      <div className="custom-card">
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Loading visitor logs..." />
          ) : visitors.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Visitor Name</th>
                    <th>Resident Student</th>
                    <th>Relation</th>
                    <th>Visit Date</th>
                    <th>Timings / Checkout</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {visitors.map((v) => (
                    <tr key={v.id}>
                      <td>
                        <div className="fw-semibold text-dark">{v.visitorName}</div>
                        <small className="text-muted"><i className="bi bi-telephone me-1"></i>{v.visitorPhone}</small>
                      </td>
                      <td>
                        <div className="fw-medium">{v.studentName}</div>
                        <small className="text-muted">Room: {v.roomNumber || 'N/A'}</small>
                      </td>
                      <td>{v.relationToStudent}</td>
                      <td>{v.visitDate}</td>
                      <td className="small text-secondary">
                        <div>In: {v.checkInTime ? new Date(v.checkInTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Not Checked In'}</div>
                        {v.checkOutTime && <div>Out: {new Date(v.checkOutTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>}
                      </td>
                      <td>
                        <span className={`badge-status badge-status-${v.status.toLowerCase()}`}>
                          {v.status}
                        </span>
                      </td>
                      <td>
                        {v.status === 'PENDING' && (
                          <div className="btn-group">
                            <button className="btn btn-sm btn-success" onClick={() => handleApprove(v.id)}>
                              Approve
                            </button>
                            <button className="btn btn-sm btn-danger" onClick={() => handleReject(v.id)}>
                              Reject
                            </button>
                          </div>
                        )}
                        {v.status === 'APPROVED' && (
                          <button className="btn btn-sm btn-outline-dark" onClick={() => handleComplete(v.id)}>
                            Mark Checked Out
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No visitor records logged.</p>
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

export default VisitorManagement;
