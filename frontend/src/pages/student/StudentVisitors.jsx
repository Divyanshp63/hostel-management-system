import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentVisitors = () => {
  const [visitors, setVisitors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Apply Modal
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    visitorName: '',
    visitorPhone: '',
    relationToStudent: 'Parent / Father',
    visitDate: new Date().toISOString().substring(0, 10),
    purpose: '',
  });

  const fetchMyVisitors = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/visitors/my');
      if (res.data?.data) {
        setVisitors(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve visitor pass history.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyVisitors();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setError('');
      await api.post('/api/visitors/request', formData);
      setSuccess('Visitor gate entry pass requested! Warden will verify on arrival.');
      setShowModal(false);
      setFormData({
        visitorName: '',
        visitorPhone: '',
        relationToStudent: 'Parent / Father',
        visitDate: new Date().toISOString().substring(0, 10),
        purpose: '',
      });
      fetchMyVisitors();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to request visitor pass.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Visitor Passes & Guest Requests</h2>
          <p className="page-subtitle">Pre-register family members, relatives, or guests visiting your hostel residence</p>
        </div>
        <button className="btn btn-primary d-flex align-items-center gap-2 shadow-sm" onClick={() => setShowModal(true)}>
          <i className="bi bi-person-plus-fill"></i> Request Visitor Entry
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      <div className="custom-card">
        <div className="custom-card-header">
          <h5 className="custom-card-title">My Registered Visitor Passes</h5>
        </div>
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Loading visitor logs..." />
          ) : visitors.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Visitor Name</th>
                    <th>Phone</th>
                    <th>Relationship</th>
                    <th>Expected Visit Date</th>
                    <th>Purpose of Visit</th>
                    <th>Status</th>
                    <th>Check In / Out</th>
                  </tr>
                </thead>
                <tbody>
                  {visitors.map((v) => (
                    <tr key={v.id}>
                      <td className="fw-semibold text-dark">{v.visitorName}</td>
                      <td>{v.visitorPhone}</td>
                      <td>{v.relationToStudent}</td>
                      <td className="fw-medium">{v.visitDate}</td>
                      <td className="small text-secondary" style={{ maxWidth: '240px' }}>{v.purpose}</td>
                      <td>
                        <span className={`badge-status badge-status-${v.status.toLowerCase()}`}>
                          {v.status}
                        </span>
                      </td>
                      <td className="small text-secondary">
                        {v.checkInTime ? `In: ${new Date(v.checkInTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}` : 'Pending Entry'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No visitor requests registered yet.</p>
          )}
        </div>
      </div>

      {/* Visitor Request Modal */}
      {showModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Request Guest Gate Pass</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Visitor Full Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. Ramesh Sharma"
                      value={formData.visitorName}
                      onChange={(e) => setFormData({ ...formData, visitorName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="row g-2 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Visitor Phone *</label>
                      <input
                        type="tel"
                        className="form-control"
                        placeholder="e.g. 9876543210"
                        value={formData.visitorPhone}
                        onChange={(e) => setFormData({ ...formData, visitorPhone: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Relation *</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. Father, Mother, Sibling"
                        value={formData.relationToStudent}
                        onChange={(e) => setFormData({ ...formData, relationToStudent: e.target.value })}
                        required
                      />
                    </div>
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Expected Visit Date *</label>
                    <input
                      type="date"
                      className="form-control"
                      value={formData.visitDate}
                      onChange={(e) => setFormData({ ...formData, visitDate: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Purpose of Visit *</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder="e.g. Dropping off semester study material and luggage..."
                      value={formData.purpose}
                      onChange={(e) => setFormData({ ...formData, purpose: e.target.value })}
                      required
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Requesting...' : 'Request Pass'}
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

export default StudentVisitors;
