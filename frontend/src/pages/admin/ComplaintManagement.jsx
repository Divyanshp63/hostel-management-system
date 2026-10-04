import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const ComplaintManagement = () => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filters
  const [category, setCategory] = useState('');
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // Status Update Modal
  const [selectedComplaint, setSelectedComplaint] = useState(null);
  const [nextStatus, setNextStatus] = useState('IN_PROGRESS');
  const [resolutionNotes, setResolutionNotes] = useState('');
  const [updateLoading, setUpdateLoading] = useState(false);

  const fetchComplaints = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/complaints', {
        params: {
          search,
          category: category || undefined,
          status: status || undefined,
          page,
          size: 10,
        },
      });

      if (res.data?.data) {
        setComplaints(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load complaints.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComplaints();
  }, [page, category, status]);

  const handleOpenStatusModal = (complaint) => {
    setSelectedComplaint(complaint);
    setNextStatus(complaint.status === 'PENDING' ? 'IN_PROGRESS' : 'RESOLVED');
    setResolutionNotes(complaint.resolutionNotes || '');
  };

  const handleStatusSubmit = async (e) => {
    e.preventDefault();
    if (!selectedComplaint) return;
    try {
      setUpdateLoading(true);
      setError('');
      await api.put(`/api/complaints/${selectedComplaint.id}/status`, {
        status: nextStatus,
        resolutionNotes,
      });
      setSuccess(`Complaint #${selectedComplaint.id} updated to ${nextStatus}!`);
      setSelectedComplaint(null);
      fetchComplaints();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update complaint status.');
    } finally {
      setUpdateLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Complaints & Maintenance Tickets</h2>
          <p className="page-subtitle">Assign work orders, track repairs, and ensure student welfare resolution</p>
        </div>
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
                placeholder="Search ticket title, student, room..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && fetchComplaints()}
              />
            </div>
            <div className="col-md-3">
              <select className="form-select" value={category} onChange={(e) => { setCategory(e.target.value); setPage(0); }}>
                <option value="">All Categories</option>
                <option value="PLUMBING">Plumbing</option>
                <option value="ELECTRICAL">Electrical</option>
                <option value="CLEANLINESS">Cleanliness</option>
                <option value="FOOD_MESS">Food & Mess</option>
                <option value="INTERNET_WIFI">Internet & Wi-Fi</option>
                <option value="FURNITURE">Furniture</option>
                <option value="NOISE_DISTURBANCE">Noise Disturbance</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="col-md-3">
              <select className="form-select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                <option value="">All Ticket Statuses</option>
                <option value="PENDING">Pending</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="RESOLVED">Resolved</option>
              </select>
            </div>
            <div className="col-md-1">
              <button className="btn btn-primary w-100" onClick={() => { setPage(0); fetchComplaints(); }}>
                <i className="bi bi-funnel-fill"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Complaints Table */}
      <div className="custom-card">
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Fetching complaint tickets..." />
          ) : complaints.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Ticket</th>
                    <th>Category</th>
                    <th>Student & Room</th>
                    <th>Description</th>
                    <th>Date Logged</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {complaints.map((c) => (
                    <tr key={c.id}>
                      <td className="fw-bold">
                        #{c.id} - <span className="text-dark">{c.title}</span>
                      </td>
                      <td>
                        <span className="badge bg-secondary-subtle text-secondary border">
                          {c.category?.replace('_', ' ')}
                        </span>
                      </td>
                      <td>
                        <div className="fw-medium text-dark">{c.studentName}</div>
                        <small className="text-muted">Room: {c.roomNumber || 'N/A'}</small>
                      </td>
                      <td className="small text-secondary" style={{ maxWidth: '280px' }}>
                        {c.description}
                      </td>
                      <td className="small">{new Date(c.createdAt).toLocaleDateString()}</td>
                      <td>
                        <span className={`badge-status badge-status-${c.status.toLowerCase().replace('_', '-')}`}>
                          {c.status}
                        </span>
                      </td>
                      <td>
                        {c.status !== 'RESOLVED' ? (
                          <button
                            className="btn btn-sm btn-outline-primary"
                            onClick={() => handleOpenStatusModal(c)}
                          >
                            Update Status
                          </button>
                        ) : (
                          <span className="text-success small fw-semibold">
                            <i className="bi bi-check2-all me-1"></i> Completed
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No complaints registered.</p>
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

      {/* Update Complaint Modal */}
      {selectedComplaint && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleStatusSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Update Complaint #{selectedComplaint.id}</h5>
                  <button type="button" className="btn-close" onClick={() => setSelectedComplaint(null)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Next Lifecycle Status *</label>
                    <select
                      className="form-select"
                      value={nextStatus}
                      onChange={(e) => setNextStatus(e.target.value)}
                    >
                      {selectedComplaint.status === 'PENDING' && (
                        <option value="IN_PROGRESS">IN_PROGRESS (Technician Assigned)</option>
                      )}
                      <option value="RESOLVED">RESOLVED (Issue Fixed)</option>
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Resolution Notes</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder="e.g. Electrician replaced faulty MCB switch..."
                      value={resolutionNotes}
                      onChange={(e) => setResolutionNotes(e.target.value)}
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setSelectedComplaint(null)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={updateLoading}>
                    {updateLoading ? 'Saving...' : 'Update Status'}
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

export default ComplaintManagement;
