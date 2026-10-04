import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import StatCard from '../../components/common/StatCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentDashboardPlaceholder = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchStudentStats = async () => {
      try {
        const res = await api.get('/api/dashboard/student');
        if (res.data?.data) {
          setData(res.data.data);
        }
      } catch (err) {
        setError('Failed to load personalized student metrics from backend.');
      } finally {
        setLoading(false);
      }
    };

    fetchStudentStats();
  }, []);

  if (loading) return <LoadingSpinner message="Loading Student Portal..." />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Welcome, {data?.studentName || 'Resident'}!</h2>
          <p className="page-subtitle">Admission: {data?.admissionNumber || 'N/A'}</p>
        </div>
        <span className="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill fw-semibold">
          <i className="bi bi-door-closed me-1"></i> Room: {data?.roomNumber || 'Not Allocated'}
        </span>
      </div>

      {error && <AlertMessage type="danger" message={error} />}

      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Allocated Room"
            value={data?.roomNumber || 'None'}
            icon="door-open-fill"
            color="primary"
            subtitle={`Type: ${data?.roomType || 'N/A'}`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Pending Dues"
            value={`₹${data?.totalPendingDues?.toLocaleString() ?? '0'}`}
            icon="wallet2"
            color={data?.totalPendingDues > 0 ? 'danger' : 'success'}
            subtitle={`Status: ${data?.latestFeeStatus || 'N/A'}`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Active Complaints"
            value={data?.myPendingComplaints ?? 0}
            icon="chat-left-dots-fill"
            color="warning"
            subtitle={`Latest: ${data?.latestComplaintStatus || 'None'}`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Pending Leaves"
            value={data?.myPendingLeaves ?? 0}
            icon="airplane-fill"
            color="info"
            subtitle={`${data?.myPendingVisitors ?? 0} Visitor Requests`}
          />
        </div>
      </div>

      {/* Today's Food Preview */}
      <div className="custom-card">
        <div className="custom-card-header">
          <h5 className="custom-card-title">
            <i className="bi bi-cup-hot-fill text-warning me-2"></i> Today's Mess Menu
          </h5>
        </div>
        <div className="custom-card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <div className="p-3 bg-light rounded-3">
                <span className="badge bg-secondary mb-2">Breakfast</span>
                <p className="mb-0 small fw-semibold text-dark">{data?.todayBreakfast || 'Not Scheduled'}</p>
              </div>
            </div>
            <div className="col-md-3">
              <div className="p-3 bg-light rounded-3">
                <span className="badge bg-primary mb-2">Lunch</span>
                <p className="mb-0 small fw-semibold text-dark">{data?.todayLunch || 'Not Scheduled'}</p>
              </div>
            </div>
            <div className="col-md-3">
              <div className="p-3 bg-light rounded-3">
                <span className="badge bg-warning text-dark mb-2">Snacks</span>
                <p className="mb-0 small fw-semibold text-dark">{data?.todaySnacks || 'Not Scheduled'}</p>
              </div>
            </div>
            <div className="col-md-3">
              <div className="p-3 bg-light rounded-3">
                <span className="badge bg-dark mb-2">Dinner</span>
                <p className="mb-0 small fw-semibold text-dark">{data?.todayDinner || 'Not Scheduled'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentDashboardPlaceholder;
