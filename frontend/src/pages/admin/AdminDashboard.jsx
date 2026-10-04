import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axiosConfig';
import StatCard from '../../components/common/StatCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/dashboard/admin');
      if (res.data?.data) {
        setStats(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load executive dashboard statistics.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading) return <LoadingSpinner message="Calculating real-time hostel analytics..." />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Executive Control Center</h2>
          <p className="page-subtitle">Real-time overview of hostel occupancy, billing, and services</p>
        </div>
        <div className="d-flex gap-2">
          <button onClick={fetchDashboardData} className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1">
            <i className="bi bi-arrow-clockwise"></i> Refresh Metrics
          </button>
          <span className="badge bg-success-subtle text-success border border-success-subtle px-3 py-2 rounded-pill fw-semibold">
            <i className="bi bi-circle-fill me-1" style={{ fontSize: '0.5rem' }}></i> Live Active
          </span>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      {/* Row 1: Bed Capacity & Occupancy KPI */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Total Rooms"
            value={stats?.totalRooms ?? 0}
            icon="door-open-fill"
            color="primary"
            subtitle={`${stats?.totalBeds ?? 0} Total Bed Capacity`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Occupancy Rate"
            value={`${stats?.occupancyRate ?? 0}%`}
            icon="pie-chart-fill"
            color="info"
            subtitle={`${stats?.occupiedBeds ?? 0} Occupied / ${stats?.availableBeds ?? 0} Vacant`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Registered Students"
            value={stats?.totalStudents ?? 0}
            icon="people-fill"
            color="success"
            subtitle={`${stats?.activeAllocations ?? 0} Active Residents`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Pending Allocations"
            value={stats?.pendingAllocations ?? 0}
            icon="hourglass-split"
            color="warning"
            subtitle="Awaiting Room Assignment"
          />
        </div>
      </div>

      {/* Row 2: Financial Overview KPI */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Total Fees Billed"
            value={`₹${stats?.totalFeeBilled?.toLocaleString() ?? '0'}`}
            icon="receipt"
            color="primary"
            subtitle="Cumulative Invoiced Amount"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Total Collected"
            value={`₹${stats?.totalFeeCollected?.toLocaleString() ?? '0'}`}
            icon="cash-coin"
            color="success"
            subtitle="Verified Online Collections"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Pending Dues"
            value={`₹${stats?.totalFeePending?.toLocaleString() ?? '0'}`}
            icon="wallet2"
            color="danger"
            subtitle={`${stats?.overdueFeeCount ?? 0} Overdue Invoices`}
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Pending Complaints"
            value={stats?.pendingComplaints ?? 0}
            icon="tools"
            color="warning"
            subtitle={`${stats?.inProgressComplaints ?? 0} In Progress, ${stats?.resolvedComplaints ?? 0} Fixed`}
          />
        </div>
      </div>

      {/* Row 3: Room Type Occupancy & Complaints Breakdown */}
      <div className="row g-4 mb-4">
        <div className="col-lg-7">
          <div className="custom-card h-100">
            <div className="custom-card-header">
              <h5 className="custom-card-title">
                <i className="bi bi-grid-fill text-primary me-2"></i> Room Type Occupancy Distribution
              </h5>
              <Link to="/admin/rooms" className="btn btn-sm btn-link text-decoration-none">
                Manage Rooms <i className="bi bi-arrow-right"></i>
              </Link>
            </div>
            <div className="custom-card-body">
              {stats?.roomTypeOccupancy && stats.roomTypeOccupancy.length > 0 ? (
                <div className="table-responsive">
                  <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                      <tr>
                        <th>Room Type</th>
                        <th>Rooms</th>
                        <th>Total Beds</th>
                        <th>Occupied</th>
                        <th>Occupancy</th>
                      </tr>
                    </thead>
                    <tbody>
                      {stats.roomTypeOccupancy.map((type, idx) => (
                        <tr key={idx}>
                          <td className="fw-semibold">{type.roomType.replace('_', ' ')}</td>
                          <td>{type.roomCount}</td>
                          <td>{type.totalBeds}</td>
                          <td>{type.occupiedBeds}</td>
                          <td style={{ minWidth: '140px' }}>
                            <div className="d-flex align-items-center gap-2">
                              <div className="progress flex-grow-1" style={{ height: '8px' }}>
                                <div
                                  className={`progress-bar ${
                                    type.occupancyRate >= 90
                                      ? 'bg-danger'
                                      : type.occupancyRate >= 60
                                      ? 'bg-primary'
                                      : 'bg-success'
                                  }`}
                                  style={{ width: `${Math.min(100, type.occupancyRate)}%` }}
                                ></div>
                              </div>
                              <span className="small fw-semibold">{type.occupancyRate}%</span>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="text-muted text-center py-4 mb-0">No rooms configured yet.</p>
              )}
            </div>
          </div>
        </div>

        {/* Complaints Breakdown */}
        <div className="col-lg-5">
          <div className="custom-card h-100">
            <div className="custom-card-header">
              <h5 className="custom-card-title">
                <i className="bi bi-pie-chart-fill text-warning me-2"></i> Complaints by Category
              </h5>
              <Link to="/admin/complaints" className="btn btn-sm btn-link text-decoration-none">
                View All <i className="bi bi-arrow-right"></i>
              </Link>
            </div>
            <div className="custom-card-body">
              {stats?.complaintsByCategory && Object.keys(stats.complaintsByCategory).length > 0 ? (
                <div className="d-flex flex-column gap-2">
                  {Object.entries(stats.complaintsByCategory).map(([cat, count]) => (
                    <div key={cat} className="d-flex justify-content-between align-items-center p-2 rounded-2 bg-light">
                      <span className="small fw-medium text-dark">{cat.replace('_', ' ')}</span>
                      <span className={`badge ${count > 0 ? 'bg-primary' : 'bg-secondary'}`}>
                        {count} tickets
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted text-center py-4 mb-0">No complaints logged.</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Row 4: Recent Pending Allocations & Recent Complaints Feed */}
      <div className="row g-4">
        <div className="col-lg-6">
          <div className="custom-card">
            <div className="custom-card-header">
              <h5 className="custom-card-title">
                <i className="bi bi-key-fill text-info me-2"></i> Pending Allocation Requests
              </h5>
              <Link to="/admin/allocations" className="btn btn-sm btn-outline-primary">
                Review Requests
              </Link>
            </div>
            <div className="custom-card-body p-0">
              {stats?.pendingAllocationRequests && stats.pendingAllocationRequests.length > 0 ? (
                <div className="list-group list-group-flush">
                  {stats.pendingAllocationRequests.map((req) => (
                    <div key={req.id} className="list-group-item d-flex justify-content-between align-items-center py-3 px-4">
                      <div>
                        <div className="fw-semibold text-dark">{req.studentName}</div>
                        <small className="text-muted">Adm: {req.admissionNumber}</small>
                      </div>
                      <div className="text-end">
                        <span className="badge bg-primary me-2">Room {req.roomNumber}</span>
                        <span className="badge bg-warning text-dark">PENDING</span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted text-center py-4 mb-0">No pending room requests.</p>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-6">
          <div className="custom-card">
            <div className="custom-card-header">
              <h5 className="custom-card-title">
                <i className="bi bi-chat-left-dots-fill text-danger me-2"></i> Recent Complaints Feed
              </h5>
              <Link to="/admin/complaints" className="btn btn-sm btn-outline-danger">
                Resolve Tickets
              </Link>
            </div>
            <div className="custom-card-body p-0">
              {stats?.recentComplaints && stats.recentComplaints.length > 0 ? (
                <div className="list-group list-group-flush">
                  {stats.recentComplaints.map((c) => (
                    <div key={c.id} className="list-group-item d-flex justify-content-between align-items-center py-3 px-4">
                      <div>
                        <div className="fw-semibold text-dark">{c.title}</div>
                        <small className="text-muted">
                          {c.studentName} &bull; Room: {c.roomNumber} &bull; {c.category}
                        </small>
                      </div>
                      <span className={`badge-status badge-status-${c.status.toLowerCase()}`}>
                        {c.status}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted text-center py-4 mb-0">No active complaints logged.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
