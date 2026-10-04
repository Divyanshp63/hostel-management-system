import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';
import StatCard from '../../components/common/StatCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentDashboard = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchStudentDashboard = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/dashboard/student');
      if (res.data?.data) {
        setData(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve your student dashboard.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudentDashboard();
  }, []);

  if (loading) return <LoadingSpinner message="Loading your student profile and hostel dues..." />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Welcome back, {data?.studentName || 'Resident'}!</h2>
          <p className="page-subtitle">Admission Number: <strong>{data?.admissionNumber || 'N/A'}</strong></p>
        </div>
        <div className="d-flex align-items-center gap-2">
          {data?.allocationStatus === 'APPROVED' ? (
            <span className="badge bg-success-subtle text-success border border-success-subtle px-3 py-2 rounded-pill fw-semibold">
              <i className="bi bi-door-closed me-1"></i> Room {data?.roomNumber} ({data?.roomType})
            </span>
          ) : data?.allocationStatus === 'PENDING' ? (
            <span className="badge bg-warning-subtle text-warning border border-warning-subtle px-3 py-2 rounded-pill fw-semibold">
              <i className="bi bi-hourglass-split me-1"></i> Allocation Request Pending
            </span>
          ) : (
            <Link to="/student/room" className="btn btn-sm btn-primary">
              <i className="bi bi-key-fill me-1"></i> Request Room
            </Link>
          )}
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      {/* Dues Warning Banner if balance exists */}
      {data?.totalPendingDues > 0 && (
        <div className="alert alert-warning border-0 shadow-sm d-flex justify-content-between align-items-center p-3 mb-4">
          <div className="d-flex align-items-center gap-3">
            <i className="bi bi-exclamation-circle-fill fs-3 text-warning"></i>
            <div>
              <strong className="d-block">Outstanding Hostel Dues: ₹{data.totalPendingDues?.toLocaleString()}</strong>
              <small className="text-secondary">Please clear your current month balance before the payment deadline.</small>
            </div>
          </div>
          <Link to="/student/fees" className="btn btn-warning text-dark fw-semibold btn-sm px-3">
            Pay Fees Online <i className="bi bi-arrow-right ms-1"></i>
          </Link>
        </div>
      )}

      {/* Top Metric Cards */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Allocated Room"
            value={data?.roomNumber || 'None'}
            icon="door-open-fill"
            color="primary"
            subtitle={`Status: ${data?.allocationStatus || 'NONE'}`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Current Dues"
            value={`₹${data?.totalPendingDues?.toLocaleString() ?? '0'}`}
            icon="wallet2"
            color={data?.totalPendingDues > 0 ? 'danger' : 'success'}
            subtitle={data?.lastPaymentAmount > 0 ? `Last Paid: ₹${data.lastPaymentAmount}` : 'No payment history'}
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

      {/* Today's Mess Menu Card */}
      <div className="custom-card mb-4">
        <div className="custom-card-header d-flex justify-content-between align-items-center">
          <h5 className="custom-card-title">
            <i className="bi bi-cup-hot-fill text-warning me-2"></i> Today's Dining Schedule
          </h5>
          <Link to="/student/mess-menu" className="btn btn-sm btn-link text-decoration-none">
            Weekly Timetable <i className="bi bi-arrow-right"></i>
          </Link>
        </div>
        <div className="custom-card-body">
          <div className="row g-3">
            <div className="col-12 col-sm-6 col-md-3">
              <div className="p-3 bg-light rounded-3 h-100 border">
                <span className="badge bg-warning text-dark mb-2">Breakfast</span>
                <p className="small mb-0 fw-semibold text-dark">{data?.todayBreakfast || 'Not Scheduled'}</p>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-md-3">
              <div className="p-3 bg-light rounded-3 h-100 border">
                <span className="badge bg-primary mb-2">Lunch</span>
                <p className="small mb-0 fw-semibold text-dark">{data?.todayLunch || 'Not Scheduled'}</p>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-md-3">
              <div className="p-3 bg-light rounded-3 h-100 border">
                <span className="badge bg-info text-dark mb-2">Snacks</span>
                <p className="small mb-0 fw-semibold text-dark">{data?.todaySnacks || 'Not Scheduled'}</p>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-md-3">
              <div className="p-3 bg-light rounded-3 h-100 border">
                <span className="badge bg-dark mb-2">Dinner</span>
                <p className="small mb-0 fw-semibold text-dark">{data?.todayDinner || 'Not Scheduled'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Latest Announcements */}
      <div className="custom-card">
        <div className="custom-card-header d-flex justify-content-between align-items-center">
          <h5 className="custom-card-title">
            <i className="bi bi-bell-fill text-danger me-2"></i> Hostel Notice Board
          </h5>
          <Link to="/student/notices" className="btn btn-sm btn-link text-decoration-none">
            All Notices <i className="bi bi-arrow-right"></i>
          </Link>
        </div>
        <div className="custom-card-body p-0">
          {data?.latestNotices && data.latestNotices.length > 0 ? (
            <div className="list-group list-group-flush">
              {data.latestNotices.map((n) => (
                <div key={n.id} className="list-group-item py-3 px-4">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <span className={`badge ${
                      n.priority === 'HIGH' ? 'bg-danger' : n.priority === 'MEDIUM' ? 'bg-warning text-dark' : 'bg-secondary'
                    }`}>
                      {n.priority}
                    </span>
                    <small className="text-muted">{new Date(n.createdAt).toLocaleDateString()}</small>
                  </div>
                  <h6 className="fw-bold text-dark mb-1">{n.title}</h6>
                  <p className="small text-secondary mb-0">{n.content}</p>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-center text-muted py-4 mb-0">No active announcements right now.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default StudentDashboard;
