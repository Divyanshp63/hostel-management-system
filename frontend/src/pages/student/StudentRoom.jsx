import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentRoom = () => {
  const [allocation, setAllocation] = useState(null);
  const [availableRooms, setAvailableRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Request Room Modal State
  const [selectedRoomId, setSelectedRoomId] = useState('');
  const [requestRemarks, setRequestRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchRoomData = async () => {
    try {
      setLoading(true);
      setError('');
      // Check active allocation
      const allocRes = await api.get('/api/allocations/my');
      if (allocRes.data?.data && allocRes.data.data.length > 0) {
        // Find approved or pending allocation
        const active = allocRes.data.data.find((a) => a.status === 'APPROVED' || a.status === 'PENDING');
        setAllocation(active || null);
      } else {
        setAllocation(null);
      }

      // Fetch available rooms
      const roomsRes = await api.get('/api/rooms/available');
      if (roomsRes.data?.data) {
        setAvailableRooms(roomsRes.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load room details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRoomData();
  }, []);

  const handleRequestRoom = async (e) => {
    e.preventDefault();
    if (!selectedRoomId) {
      setError('Please select an available room.');
      return;
    }
    try {
      setSubmitting(true);
      setError('');
      await api.post('/api/allocations/request', {
        roomId: parseInt(selectedRoomId),
        remarks: requestRemarks,
      });
      setSuccess('Room allocation request submitted! Awaiting warden approval.');
      fetchRoomData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit room allocation request.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner message="Checking room allocation status..." />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">My Room & Hostel Residence</h2>
          <p className="page-subtitle">View your assigned room details, amenities, or apply for room transfer</p>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {allocation ? (
        <div className="row g-4">
          <div className="col-lg-8">
            <div className="custom-card">
              <div className="custom-card-header d-flex justify-content-between align-items-center">
                <h5 className="custom-card-title">
                  <i className="bi bi-door-open-fill text-primary me-2"></i> Room {allocation.roomNumber}
                </h5>
                <span className={`badge-status badge-status-${allocation.status.toLowerCase()}`}>
                  {allocation.status}
                </span>
              </div>
              <div className="custom-card-body">
                {allocation.status === 'PENDING' ? (
                  <div className="alert alert-warning">
                    <i className="bi bi-hourglass-split me-2"></i>
                    Your application for <strong>Room {allocation.roomNumber}</strong> has been submitted on{' '}
                    {new Date(allocation.createdAt).toLocaleDateString()} and is waiting for warden approval.
                  </div>
                ) : (
                  <div className="row g-4">
                    <div className="col-md-6">
                      <div className="p-3 bg-light rounded-3">
                        <label className="text-muted small fw-semibold">Sharing Type</label>
                        <h6 className="fw-bold text-dark mb-0">{allocation.roomType}</h6>
                      </div>
                    </div>
                    <div className="col-md-6">
                      <div className="p-3 bg-light rounded-3">
                        <label className="text-muted small fw-semibold">Allocation Start Date</label>
                        <h6 className="fw-bold text-dark mb-0">{allocation.startDate || 'Current Semester'}</h6>
                      </div>
                    </div>
                    <div className="col-12">
                      <div className="p-3 border rounded-3 bg-white">
                        <h6 className="fw-bold text-dark mb-2">Room Amenities & Features</h6>
                        <div className="d-flex flex-wrap gap-2">
                          <span className="badge bg-light text-dark border p-2"><i className="bi bi-wifi me-1"></i> High-Speed Wi-Fi</span>
                          <span className="badge bg-light text-dark border p-2"><i className="bi bi-droplet me-1"></i> 24x7 Water Supply</span>
                          <span className="badge bg-light text-dark border p-2"><i className="bi bi-shield-check me-1"></i> Security Guard Patrol</span>
                          <span className="badge bg-light text-dark border p-2"><i className="bi bi-book me-1"></i> Dedicated Study Desk</span>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="col-lg-4">
            <div className="custom-card">
              <div className="custom-card-header">
                <h5 className="custom-card-title">Hostel Guidelines</h5>
              </div>
              <div className="custom-card-body small text-secondary">
                <ul className="mb-0 ps-3">
                  <li className="mb-2">Gate curfew time is strictly <strong>10:00 PM</strong>.</li>
                  <li className="mb-2">Keep your room and corridor space neat and hygienic.</li>
                  <li className="mb-2">Electric heaters or induction stoves are strictly prohibited in rooms.</li>
                  <li>Any damages will be deducted from security deposit.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="row g-4">
          <div className="col-lg-7">
            <div className="custom-card">
              <div className="custom-card-header">
                <h5 className="custom-card-title">Apply for Hostel Bed Allocation</h5>
              </div>
              <div className="custom-card-body p-4">
                <p className="text-secondary small mb-4">
                  You do not have an active room allocation. Please select your preferred room from the available list below and submit your application.
                </p>

                <form onSubmit={handleRequestRoom}>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Select Available Room *</label>
                    <select
                      className="form-select"
                      value={selectedRoomId}
                      onChange={(e) => setSelectedRoomId(e.target.value)}
                      required
                    >
                      <option value="">-- Choose Room --</option>
                      {availableRooms.map((r) => (
                        <option key={r.id} value={r.id}>
                          Room {r.roomNumber} ({r.blockName}, Floor {r.floor}) - {r.roomType} &bull; ₹{r.rentPerMonth}/mo ({r.availableBeds} beds available)
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="mb-4">
                    <label className="form-label small fw-semibold">Request Notes (Optional)</label>
                    <textarea
                      className="form-control"
                      rows="3"
                      placeholder="e.g. Prefer quiet room for exam preparation..."
                      value={requestRemarks}
                      onChange={(e) => setRequestRemarks(e.target.value)}
                    ></textarea>
                  </div>

                  <button type="submit" className="btn btn-primary w-100 py-2 fw-semibold" disabled={submitting}>
                    {submitting ? 'Submitting Application...' : 'Submit Room Application'}
                  </button>
                </form>
              </div>
            </div>
          </div>

          <div className="col-lg-5">
            <div className="custom-card">
              <div className="custom-card-header">
                <h5 className="custom-card-title">Available Room Vacancies ({availableRooms.length})</h5>
              </div>
              <div className="custom-card-body p-0">
                <div className="list-group list-group-flush">
                  {availableRooms.slice(0, 5).map((r) => (
                    <div key={r.id} className="list-group-item d-flex justify-content-between align-items-center py-3 px-4">
                      <div>
                        <div className="fw-bold text-dark">Room {r.roomNumber}</div>
                        <small className="text-muted">{r.roomType} &bull; {r.blockName}</small>
                      </div>
                      <div className="text-end">
                        <div className="fw-bold text-primary">₹{r.rentPerMonth}/mo</div>
                        <small className="badge bg-success-subtle text-success border border-success-subtle">
                          {r.availableBeds} beds free
                        </small>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentRoom;
