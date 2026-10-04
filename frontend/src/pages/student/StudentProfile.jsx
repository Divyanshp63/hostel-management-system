import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentProfile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formData, setFormData] = useState({
    course: '',
    branch: '',
    yearOfStudy: 1,
    address: '',
    emergencyContact: '',
    guardianName: '',
    guardianPhone: '',
  });

  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/students/profile/me');
      if (res.data?.data) {
        const p = res.data.data;
        setProfile(p);
        setFormData({
          course: p.course || '',
          branch: p.branch || '',
          yearOfStudy: p.yearOfStudy || 1,
          address: p.address || '',
          emergencyContact: p.emergencyContact || '',
          guardianName: p.guardianName || '',
          guardianPhone: p.guardianPhone || '',
        });
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve profile details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setUpdating(true);
      setError('');
      await api.put('/api/students/profile/me', formData);
      setSuccess('Profile updated successfully!');
      fetchProfile();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading your student profile..." />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">My Resident Profile</h2>
          <p className="page-subtitle">Manage emergency contact details, permanent address, and guardian information</p>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      <div className="row g-4">
        {/* Left: Summary Card */}
        <div className="col-lg-4">
          <div className="custom-card text-center p-4">
            <div
              className="rounded-circle d-inline-flex align-items-center justify-content-center bg-primary text-white fs-1 fw-bold mb-3"
              style={{ width: '84px', height: '84px' }}
            >
              {profile?.user?.name ? profile.user.name.substring(0, 2).toUpperCase() : 'ST'}
            </div>
            <h4 className="fw-bold text-dark mb-1">{profile?.user?.name}</h4>
            <p className="badge bg-primary fs-6 mb-3">{profile?.admissionNumber}</p>
            <div className="text-secondary small mb-3">
              <div><i className="bi bi-envelope me-1"></i> {profile?.user?.email}</div>
              <div><i className="bi bi-telephone me-1"></i> {profile?.user?.phone}</div>
            </div>
            <hr />
            <div className="text-start small">
              <div className="mb-2"><strong>Gender:</strong> {profile?.gender}</div>
              <div className="mb-2"><strong>Room:</strong> {profile?.allocatedRoomNumber ? `Room ${profile.allocatedRoomNumber}` : 'Not Allocated'}</div>
              <div><strong>Registration Date:</strong> {new Date(profile?.createdAt).toLocaleDateString()}</div>
            </div>
          </div>
        </div>

        {/* Right: Editable Form */}
        <div className="col-lg-8">
          <div className="custom-card">
            <div className="custom-card-header">
              <h5 className="custom-card-title">Update Contact & Guardian Info</h5>
            </div>
            <div className="custom-card-body p-4">
              <form onSubmit={handleSubmit}>
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Academic Course</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.course}
                      onChange={(e) => setFormData({ ...formData, course: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Branch</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.branch}
                      onChange={(e) => setFormData({ ...formData, branch: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Year of Study</label>
                    <input
                      type="number"
                      min="1"
                      max="5"
                      className="form-control"
                      value={formData.yearOfStudy}
                      onChange={(e) => setFormData({ ...formData, yearOfStudy: parseInt(e.target.value) })}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Emergency Reachable Contact *</label>
                    <input
                      type="tel"
                      className="form-control"
                      value={formData.emergencyContact}
                      onChange={(e) => setFormData({ ...formData, emergencyContact: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Guardian / Parent Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.guardianName}
                      onChange={(e) => setFormData({ ...formData, guardianName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label small fw-semibold">Guardian Phone Number *</label>
                    <input
                      type="tel"
                      className="form-control"
                      value={formData.guardianPhone}
                      onChange={(e) => setFormData({ ...formData, guardianPhone: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-12">
                    <label className="form-label small fw-semibold">Permanent Home Address *</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      value={formData.address}
                      onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                      required
                    ></textarea>
                  </div>
                </div>

                <div className="mt-4 text-end">
                  <button type="submit" className="btn btn-primary px-4 fw-semibold shadow-sm" disabled={updating}>
                    {updating ? 'Saving Changes...' : 'Save Profile Changes'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentProfile;
