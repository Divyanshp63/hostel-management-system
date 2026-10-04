import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const NoticeManagement = () => {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Create Modal
  const [showModal, setShowModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    priority: 'HIGH',
    targetAudience: 'ALL',
    active: true,
  });

  const fetchNotices = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/notices');
      if (res.data?.data) {
        setNotices(res.data.data.content || res.data.data || []);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load notices.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotices();
  }, []);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      setCreateLoading(true);
      setError('');
      await api.post('/api/notices', formData);
      setSuccess('Notice broadcast published successfully!');
      setShowModal(false);
      setFormData({
        title: '',
        content: '',
        priority: 'HIGH',
        targetAudience: 'ALL',
        active: true,
      });
      fetchNotices();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create notice.');
    } finally {
      setCreateLoading(false);
    }
  };

  const handleDelete = async (id, title) => {
    if (!window.confirm(`Delete notice: "${title}"?`)) return;
    try {
      await api.delete(`/api/notices/${id}`);
      setSuccess('Notice deleted.');
      fetchNotices();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete notice.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Notice Board & Broadcasts</h2>
          <p className="page-subtitle">Publish official announcements, maintenance schedules, and holiday bulletins</p>
        </div>
        <button
          className="btn btn-primary d-flex align-items-center gap-2 shadow-sm"
          onClick={() => setShowModal(true)}
        >
          <i className="bi bi-megaphone-fill"></i> Post Announcement
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {loading ? (
        <LoadingSpinner message="Loading notices..." />
      ) : notices.length > 0 ? (
        <div className="row g-4">
          {notices.map((n) => (
            <div key={n.id} className="col-12 col-md-6 col-xl-4">
              <div className="custom-card h-100 d-flex flex-column justify-content-between">
                <div>
                  <div className="custom-card-header d-flex justify-content-between align-items-center">
                    <span className={`badge ${
                      n.priority === 'HIGH' ? 'bg-danger' : n.priority === 'MEDIUM' ? 'bg-warning text-dark' : 'bg-secondary'
                    }`}>
                      {n.priority} PRIORITY
                    </span>
                    <small className="text-muted">{new Date(n.createdAt).toLocaleDateString()}</small>
                  </div>
                  <div className="custom-card-body">
                    <h5 className="fw-bold text-dark mb-2">{n.title}</h5>
                    <p className="text-secondary small mb-3">{n.content}</p>
                    <span className="badge bg-light text-dark border">
                      Audience: {n.targetAudience}
                    </span>
                  </div>
                </div>
                <div className="p-3 border-top bg-light d-flex justify-content-between align-items-center">
                  <span className={`small fw-semibold ${n.active ? 'text-success' : 'text-danger'}`}>
                    <i className={`bi bi-circle-fill me-1`} style={{ fontSize: '0.5rem' }}></i>
                    {n.active ? 'Active' : 'Inactive'}
                  </span>
                  <button
                    className="btn btn-sm btn-outline-danger"
                    onClick={() => handleDelete(n.id, n.title)}
                  >
                    <i className="bi bi-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-5 text-muted">
          <i className="bi bi-megaphone fs-1 d-block mb-2 text-secondary"></i>
          No notices currently posted.
        </div>
      )}

      {/* Post Notice Modal */}
      {showModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleCreateSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Post Official Notice</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Notice Title *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. Wi-Fi Server Maintenance on Saturday"
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                      required
                    />
                  </div>
                  <div className="row g-2 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Priority Level *</label>
                      <select
                        className="form-select"
                        value={formData.priority}
                        onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                      >
                        <option value="HIGH">High (Urgent)</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="LOW">Low</option>
                      </select>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Target Audience *</label>
                      <select
                        className="form-select"
                        value={formData.targetAudience}
                        onChange={(e) => setFormData({ ...formData, targetAudience: e.target.value })}
                      >
                        <option value="ALL">All Residents & Staff</option>
                        <option value="STUDENTS_ONLY">Students Only</option>
                        <option value="STAFF_ONLY">Staff Only</option>
                      </select>
                    </div>
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Notice Announcement Body *</label>
                    <textarea
                      className="form-control"
                      rows="4"
                      placeholder="Detailed content of the announcement..."
                      value={formData.content}
                      onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                      required
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={createLoading}>
                    {createLoading ? 'Publishing...' : 'Broadcast Announcement'}
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

export default NoticeManagement;
