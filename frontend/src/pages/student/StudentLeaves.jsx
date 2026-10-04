import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentLeaves = () => {
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Apply Leave Modal
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    leaveType: 'HOME_VISIT',
    fromDate: new Date().toISOString().substring(0, 10),
    toDate: new Date(new Date().setDate(new Date().getDate() + 2)).toISOString().substring(0, 10),
    reason: '',
    contactNumber: '',
  });

  const fetchMyLeaves = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/leaves/my');
      if (res.data?.data) {
        setLeaves(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve leave history.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyLeaves();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setError('');
      await api.post('/api/leaves/apply', formData);
      setSuccess('Leave application submitted successfully! Waiting for warden clearance.');
      setShowModal(false);
      setFormData({
        leaveType: 'HOME_VISIT',
        fromDate: new Date().toISOString().substring(0, 10),
        toDate: new Date(new Date().setDate(new Date().getDate() + 2)).toISOString().substring(0, 10),
        reason: '',
        contactNumber: '',
      });
      fetchMyLeaves();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to apply for leave.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Leave Passes & Gate Permission</h2>
          <p className="page-subtitle">Apply for weekend home visits, emergency absences, and track warden permissions</p>
        </div>
        <button className="btn btn-primary d-flex align-items-center gap-2 shadow-sm" onClick={() => setShowModal(true)}>
          <i className="bi bi-airplane-fill"></i> Apply For Leave
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      <div className="custom-card">
        <div className="custom-card-header">
          <h5 className="custom-card-title">My Leave History</h5>
        </div>
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Loading leave applications..." />
          ) : leaves.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Type</th>
                    <th>From Date</th>
                    <th>To Date</th>
                    <th>Duration</th>
                    <th>Reason</th>
                    <th>Contact Phone</th>
                    <th>Status</th>
                    <th>Warden Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {leaves.map((l) => (
                    <tr key={l.id}>
                      <td>
                        <span className="badge bg-light text-dark border">
                          {l.leaveType?.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="fw-medium">{l.fromDate}</td>
                      <td className="fw-medium">{l.toDate}</td>
                      <td className="fw-bold">{l.totalDays} Days</td>
                      <td className="small text-secondary" style={{ maxWidth: '250px' }}>{l.reason}</td>
                      <td className="small"><i className="bi bi-telephone me-1"></i>{l.contactNumber}</td>
                      <td>
                        <span className={`badge-status badge-status-${l.status.toLowerCase()}`}>
                          {l.status}
                        </span>
                      </td>
                      <td className="small text-secondary">{l.adminRemarks || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No leave requests found.</p>
          )}
        </div>
      </div>

      {/* Apply Leave Modal */}
      {showModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Apply for Out-of-Campus Leave</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Leave Type *</label>
                    <select
                      className="form-select"
                      value={formData.leaveType}
                      onChange={(e) => setFormData({ ...formData, leaveType: e.target.value })}
                    >
                      <option value="HOME_VISIT">Home Visit</option>
                      <option value="MEDICAL">Medical Treatment / Sick Leave</option>
                      <option value="ACADEMIC_INTERNSHIP">Academic / Internship Duty</option>
                      <option value="PERSONAL_FAMILY">Personal / Family Function</option>
                      <option value="EMERGENCY">Emergency Absence</option>
                    </select>
                  </div>
                  <div className="row g-2 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">From Date *</label>
                      <input
                        type="date"
                        className="form-control"
                        value={formData.fromDate}
                        onChange={(e) => setFormData({ ...formData, fromDate: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">To Date *</label>
                      <input
                        type="date"
                        className="form-control"
                        value={formData.toDate}
                        onChange={(e) => setFormData({ ...formData, toDate: e.target.value })}
                        required
                      />
                    </div>
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Emergency Reachable Phone *</label>
                    <input
                      type="tel"
                      className="form-control"
                      placeholder="e.g. 9876543210"
                      value={formData.contactNumber}
                      onChange={(e) => setFormData({ ...formData, contactNumber: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Reason for Absence *</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder="State destination city and purpose of leave..."
                      value={formData.reason}
                      onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                      required
                    ></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Submitting...' : 'Submit Application'}
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

export default StudentLeaves;
