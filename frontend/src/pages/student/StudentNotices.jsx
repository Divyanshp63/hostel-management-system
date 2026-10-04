import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentNotices = () => {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedPriority, setSelectedPriority] = useState('ALL');

  useEffect(() => {
    const fetchNotices = async () => {
      try {
        setLoading(true);
        setError('');
        const res = await api.get('/api/notices/active');
        if (res.data?.data) {
          setNotices(res.data.data);
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load hostel notices.');
      } finally {
        setLoading(false);
      }
    };

    fetchNotices();
  }, []);

  const filteredNotices = notices.filter((n) => {
    if (selectedPriority === 'ALL') return true;
    return n.priority === selectedPriority;
  });

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Hostel Notice Board</h2>
          <p className="page-subtitle">Official announcements, emergency alerts, maintenance schedules, and holiday bulletins</p>
        </div>
        <div className="btn-group">
          <button
            className={`btn btn-sm ${selectedPriority === 'ALL' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setSelectedPriority('ALL')}
          >
            All Bulletins
          </button>
          <button
            className={`btn btn-sm ${selectedPriority === 'HIGH' ? 'btn-danger' : 'btn-outline-secondary'}`}
            onClick={() => setSelectedPriority('HIGH')}
          >
            Urgent / High
          </button>
          <button
            className={`btn btn-sm ${selectedPriority === 'MEDIUM' ? 'btn-warning text-dark' : 'btn-outline-secondary'}`}
            onClick={() => setSelectedPriority('MEDIUM')}
          >
            Medium
          </button>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      {loading ? (
        <LoadingSpinner message="Loading hostel notices..." />
      ) : filteredNotices.length > 0 ? (
        <div className="row g-4">
          {filteredNotices.map((n) => (
            <div key={n.id} className="col-12 col-md-6">
              <div className="custom-card h-100 border-start border-4 border-primary">
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
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-5 text-muted">
          <i className="bi bi-bell-slash fs-1 d-block mb-2 text-secondary"></i>
          No notices found for this filter.
        </div>
      )}
    </div>
  );
};

export default StudentNotices;
