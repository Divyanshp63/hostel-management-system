import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const RoomManagement = () => {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filters
  const [search, setSearch] = useState('');
  const [roomType, setRoomType] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [formData, setFormData] = useState({
    roomNumber: '',
    blockName: 'Block A',
    floor: 1,
    capacity: 2,
    roomType: 'DOUBLE',
    rentPerMonth: 6500,
    amenities: 'Attached Bathroom, Wi-Fi, Study Table, Wardrobe',
  });

  const fetchRooms = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/rooms', {
        params: {
          search,
          roomType: roomType || undefined,
          status: status || undefined,
          page,
          size: 10,
        },
      });

      if (res.data?.data) {
        setRooms(res.data.data.content || []);
        setTotalPages(res.data.data.totalPages || 1);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load rooms list.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
  }, [page, roomType, status]);

  const handleOpenCreateModal = () => {
    setIsEditing(false);
    setEditingId(null);
    setFormData({
      roomNumber: '',
      blockName: 'Block A',
      floor: 1,
      capacity: 2,
      roomType: 'DOUBLE',
      rentPerMonth: 6500,
      amenities: 'Attached Bathroom, Wi-Fi, Study Table, Wardrobe',
    });
    setShowModal(true);
  };

  const handleOpenEditModal = (room) => {
    setIsEditing(true);
    setEditingId(room.id);
    setFormData({
      roomNumber: room.roomNumber,
      blockName: room.blockName,
      floor: room.floor,
      capacity: room.capacity,
      roomType: room.roomType,
      rentPerMonth: room.rentPerMonth,
      amenities: room.amenities,
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setFormLoading(true);
      setError('');
      if (isEditing) {
        await api.put(`/api/rooms/${editingId}`, formData);
        setSuccess(`Room ${formData.roomNumber} updated successfully!`);
      } else {
        await api.post('/api/rooms', formData);
        setSuccess(`Room ${formData.roomNumber} registered successfully!`);
      }
      setShowModal(false);
      fetchRooms();
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async (id, roomNumber) => {
    if (!window.confirm(`Are you sure you want to delete Room ${roomNumber}? This cannot be undone.`)) {
      return;
    }
    try {
      await api.delete(`/api/rooms/${id}`);
      setSuccess(`Room ${roomNumber} deleted successfully.`);
      fetchRooms();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete room.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Room & Bed Management</h2>
          <p className="page-subtitle">Configure hostel blocks, floor plans, room capacities, and monthly rent</p>
        </div>
        <button className="btn btn-primary d-flex align-items-center gap-2 shadow-sm" onClick={handleOpenCreateModal}>
          <i className="bi bi-plus-lg"></i> Add New Room
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {/* Filters Bar */}
      <div className="custom-card mb-4">
        <div className="custom-card-body p-3">
          <div className="row g-2">
            <div className="col-md-5">
              <input
                type="text"
                className="form-control"
                placeholder="Search room number..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && fetchRooms()}
              />
            </div>
            <div className="col-md-3">
              <select className="form-select" value={roomType} onChange={(e) => { setRoomType(e.target.value); setPage(0); }}>
                <option value="">All Room Types</option>
                <option value="SINGLE">Single Sharing</option>
                <option value="DOUBLE">Double Sharing</option>
                <option value="TRIPLE">Triple Sharing</option>
                <option value="FOUR_SHARING">Four Sharing</option>
              </select>
            </div>
            <div className="col-md-3">
              <select className="form-select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                <option value="">All Occupancy Status</option>
                <option value="AVAILABLE">Available</option>
                <option value="FULL">Full</option>
                <option value="UNDER_MAINTENANCE">Maintenance</option>
              </select>
            </div>
            <div className="col-md-1">
              <button className="btn btn-primary w-100" onClick={() => { setPage(0); fetchRooms(); }}>
                <i className="bi bi-funnel-fill"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Rooms Table */}
      <div className="custom-card">
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Loading hostel rooms..." />
          ) : rooms.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Room No</th>
                    <th>Block & Floor</th>
                    <th>Type</th>
                    <th>Bed Occupancy</th>
                    <th>Monthly Rent</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rooms.map((room) => (
                    <tr key={room.id}>
                      <td className="fw-bold fs-6 text-primary">{room.roomNumber}</td>
                      <td>
                        <span className="fw-medium">{room.blockName}</span> &bull; Floor {room.floor}
                      </td>
                      <td>{room.roomType.replace('_', ' ')}</td>
                      <td style={{ minWidth: '150px' }}>
                        <div className="small fw-semibold mb-1">
                          {room.occupied} / {room.capacity} Beds ({room.availableBeds} Free)
                        </div>
                        <div className="progress" style={{ height: '6px' }}>
                          <div
                            className={`progress-bar ${room.occupied >= room.capacity ? 'bg-danger' : 'bg-success'}`}
                            style={{ width: `${(room.occupied / room.capacity) * 100}%` }}
                          ></div>
                        </div>
                      </td>
                      <td className="fw-bold text-dark">₹{room.rentPerMonth?.toLocaleString()}</td>
                      <td>
                        <span className={`badge-status badge-status-${room.status.toLowerCase()}`}>
                          {room.status}
                        </span>
                      </td>
                      <td>
                        <div className="btn-group">
                          <button
                            className="btn btn-sm btn-outline-secondary"
                            onClick={() => handleOpenEditModal(room)}
                            title="Edit Room"
                          >
                            <i className="bi bi-pencil"></i>
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => handleDelete(room.id, room.roomNumber)}
                            disabled={room.occupied > 0}
                            title={room.occupied > 0 ? 'Cannot delete occupied room' : 'Delete Room'}
                          >
                            <i className="bi bi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted py-5 mb-0">No rooms found matching filters.</p>
          )}

          {totalPages > 1 && (
            <div className="d-flex justify-content-between align-items-center p-3 border-top">
              <span className="text-muted small">Page {page + 1} of {totalPages}</span>
              <div className="btn-group">
                <button
                  className="btn btn-sm btn-outline-secondary"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Previous
                </button>
                <button
                  className="btn btn-sm btn-outline-secondary"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Add / Edit Room Modal */}
      {showModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">
                    {isEditing ? `Edit Room ${formData.roomNumber}` : 'Add New Hostel Room'}
                  </h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Room Number *</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. A-101"
                        value={formData.roomNumber}
                        onChange={(e) => setFormData({ ...formData, roomNumber: e.target.value })}
                        required
                        disabled={isEditing}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Block Name *</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.blockName}
                        onChange={(e) => setFormData({ ...formData, blockName: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Floor *</label>
                      <input
                        type="number"
                        min="0"
                        max="20"
                        className="form-control"
                        value={formData.floor}
                        onChange={(e) => setFormData({ ...formData, floor: parseInt(e.target.value) })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Bed Capacity *</label>
                      <input
                        type="number"
                        min="1"
                        max="8"
                        className="form-control"
                        value={formData.capacity}
                        onChange={(e) => setFormData({ ...formData, capacity: parseInt(e.target.value) })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Room Type *</label>
                      <select
                        className="form-select"
                        value={formData.roomType}
                        onChange={(e) => setFormData({ ...formData, roomType: e.target.value })}
                      >
                        <option value="SINGLE">Single Sharing</option>
                        <option value="DOUBLE">Double Sharing</option>
                        <option value="TRIPLE">Triple Sharing</option>
                        <option value="FOUR_SHARING">Four Sharing</option>
                      </select>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Monthly Rent (₹) *</label>
                      <input
                        type="number"
                        min="0"
                        className="form-control"
                        value={formData.rentPerMonth}
                        onChange={(e) => setFormData({ ...formData, rentPerMonth: parseFloat(e.target.value) })}
                        required
                      />
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Room Amenities</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. Wi-Fi, AC, Attached Bath, Balcony"
                        value={formData.amenities}
                        onChange={(e) => setFormData({ ...formData, amenities: e.target.value })}
                      />
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={formLoading}>
                    {formLoading ? 'Saving...' : isEditing ? 'Update Room' : 'Create Room'}
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

export default RoomManagement;
