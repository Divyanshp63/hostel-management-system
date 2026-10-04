import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentComplaints = () => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // New Complaint Modal
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    category: 'PLUMBING',
    description: '',
  });

  const fetchMyComplaints = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/complaints/my');
      if (res.data?.data) {
        setComplaints(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve your complaints.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyComplaints();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setError('');
      await api.post('/api/complaints', formData);
      setSuccess('Complaint ticket lodged successfully! Hostel maintenance team will inspect.');
      setShowModal(false);
      setFormData({ title: '', category: 'PLUMBING', description: '' });
      fetchMyComplaints();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit complaint.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Maintenance & Complaints</h2>
          <p className="page-subtitle">Report room issues, electrical faults, Wi-Fi errors, and track resolution status</p>
        </div>
        <button className="btn btn-primary d-flex align-items-center gap-2 shadow-sm" onClick={() => setShowModal(true)}>
          <i className="bi bi-plus-lg"></i> Lodge New Ticket
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      <div className="custom-card">
        <div className="custom-card-header">
          <h5 className="custom-card-title">My Registered Complaints</h5>
        </div>
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Loading your tickets..." />
          ) : complaints.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Ticket ID</th>
                    <th>Issue Summary</th>
                    <th>Category</th>
                    <th>Room</th>
                    <th>Date Logged</th>
                    <th>Status</th>
                    <th>Warden / Staff Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {complaints.map((c) => (
                    <tr key={c.id}>
                      <td className="fw-bold">#{c.id}</td>
                      <td>
                        <div className="fw-semibold text-dark">{c.title}</div>
                        <small className="text-secondary d-block" style={{ maxWidth: '280px' }}>
                          {c.description}
                        </small>
                      </td>
                      <td>
                        <span className="badge bg-light text-dark border">
                          {c.category?.replace('_', ' ')}
                        </span>
                      </td>
                      <td>Room {c.roomNumber || 'N/A'}</td>
                      <td className="small">{new Date(c.createdAt).toLocaleDateString()}</td>
                      <td>
                        <span className={`badge-status badge-status-${c.status.toLowerCase().replace('_', '-')}`}>
                          {c.status}
                        </span>
                      </td>
                      <td className="small text-secondary">
                        {c.resolutionNotes || (c.status === 'PENDING' ? 'Awaiting assignment' : 'Under inspection')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">You have not logged any complaints yet.</p>
          )}
        </div>
      </div>

      {/* Lodge Ticket Modal */}
      {showModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Lodge Maintenance Ticket</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Complaint Title *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. Geyser not heating in bathroom"
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Issue Category *</label>
                    <select
                      className="form-select"
                      value={formData.category}
                      onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    >
                      <option value="PLUMBING">Plumbing (Tap, Leakage, Washroom)</option>
                      <option value="ELECTRICAL">Electrical (Fan, Light, Switch, Geyser)</option>
                      <option value="CLEANLINESS">Cleanliness & Housekeeping</option>
                      <option value="FOOD_MESS">Food & Mess Catering</option>
                      <option value="INTERNET_WIFI">Internet / Wi-Fi Network</option>
                      <option value="FURNITURE">Bed, Desk or Cupboard Repair</option>
                      <option value="NOISE_DISTURBANCE">Noise Disturbance</option>
                      <option value="OTHER">General / Other</option>
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Detailed Description *</label>
                    <textarea
                      className="form-control"
                      rows="4"
                      placeholder="Please describe the exact issue and location in your room..."
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      required
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Submitting...' : 'Submit Ticket'}
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

export default StudentComplaints;
