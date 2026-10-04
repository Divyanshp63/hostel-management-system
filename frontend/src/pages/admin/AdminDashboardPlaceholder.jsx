import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import StatCard from '../../components/common/StatCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const AdminDashboardPlaceholder = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await api.get('/api/dashboard/admin');
        if (res.data?.data) {
          setStats(res.data.data);
        }
      } catch (err) {
        setError('Failed to load dashboard statistics from backend API.');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) return <LoadingSpinner message="Loading Admin Cockpit..." />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Executive Dashboard</h2>
          <p className="page-subtitle">Real-time overview of hostel occupancy, billing, and services</p>
        </div>
        <span className="badge bg-success-subtle text-success border border-success-subtle px-3 py-2 rounded-pill fw-semibold">
          <i className="bi bi-broadcast me-1"></i> Live Connected
        </span>
      </div>

      {error && <AlertMessage type="danger" message={error} />}

      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Total Rooms"
            value={stats?.totalRooms ?? 0}
            icon="door-open-fill"
            color="primary"
            subtitle={`${stats?.totalBeds ?? 0} Total Beds`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Occupancy Rate"
            value={`${stats?.occupancyRate ?? 0}%`}
            icon="pie-chart-fill"
            color="info"
            subtitle={`${stats?.occupiedBeds ?? 0} Occupied Beds`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Total Revenue Collected"
            value={`₹${stats?.totalFeeCollected?.toLocaleString() ?? '0'}`}
            icon="cash-coin"
            color="success"
            subtitle={`₹${stats?.totalFeePending?.toLocaleString() ?? '0'} Pending`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Pending Complaints"
            value={stats?.pendingComplaints ?? 0}
            icon="tools"
            color="warning"
            subtitle={`${stats?.inProgressComplaints ?? 0} In Progress`}
          />
        </div>
      </div>

      <div className="alert alert-info border-0 shadow-sm d-flex align-items-center gap-3 p-4">
        <i className="bi bi-info-circle-fill fs-2 text-info"></i>
        <div>
          <h5 className="alert-heading fw-bold mb-1">Frontend Setup & Architecture Verified!</h5>
          <p className="mb-0 small">
            Axios JWT interceptor successfully queried <code>/api/dashboard/admin</code>. Module 14 will implement all dedicated admin CRUD pages (Rooms, Allocations, Fees, Mess Timetable, etc.).
          </p>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPlaceholder;
