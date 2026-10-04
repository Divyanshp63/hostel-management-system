import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const StudentManagement = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Selected student for details view modal
  const [selectedStudent, setSelectedStudent] = useState(null);

  // Form State for creating student
  const [showAddModal, setShowAddModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: 'Password@123',
    phone: '',
    admissionNumber: '',
    course: 'B.Tech Computer Science',
    branch: 'CSE',
    yearOfStudy: 3,
    dateOfBirth: '2004-05-15',
    gender: 'MALE',
    address: '123 University Campus Hostel Rd',
    emergencyContact: '9876543210',
    guardianName: 'Parent / Guardian',
    guardianPhone: '9876543211',
  });

  const fetchStudents = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/students', {
        params: {
          search: searchTerm,
          page: page,
          size: 10,
        },
      });

      if (res.data?.data) {
        const pageData = res.data.data;
        setStudents(pageData.content || []);
        setTotalPages(pageData.totalPages || 1);
        setTotalElements(pageData.totalElements || 0);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to retrieve students list.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, [page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchStudents();
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      setCreateLoading(true);
      setError('');
      await api.post('/api/students', formData);
      setSuccess('Student record created and user account registered successfully!');
      setShowAddModal(false);
      setPage(0);
      fetchStudents();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to register student record.');
    } finally {
      setCreateLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Student Directory</h2>
          <p className="page-subtitle">Manage registered residents, student profiles, and academic records</p>
        </div>
        <button
          className="btn btn-primary d-flex align-items-center gap-2 shadow-sm"
          onClick={() => setShowAddModal(true)}
        >
          <i className="bi bi-person-plus-fill"></i> Add New Student
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {/* Search and Filters Bar */}
      <div className="custom-card mb-4">
        <div className="custom-card-body p-3">
          <form onSubmit={handleSearchSubmit} className="d-flex gap-2">
            <div className="input-group">
              <span className="input-group-text bg-white border-end-0">
                <i className="bi bi-search text-muted"></i>
              </span>
              <input
                type="text"
                className="form-control border-start-0"
                placeholder="Search by student name, admission number, email, or course..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary px-4">
              Search
            </button>
            {searchTerm && (
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => {
                  setSearchTerm('');
                  setPage(0);
                  setTimeout(fetchStudents, 50);
                }}
              >
                Clear
              </button>
            )}
          </form>
        </div>
      </div>

      {/* Student List Table */}
      <div className="custom-card">
        <div className="custom-card-header">
          <h5 className="custom-card-title">
            Registered Students <span className="badge bg-secondary ms-2">{totalElements} total</span>
          </h5>
        </div>
        <div className="custom-card-body p-0">
          {loading ? (
            <LoadingSpinner message="Fetching student records..." />
          ) : students.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Admission No</th>
                    <th>Full Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Course & Year</th>
                    <th>Allocated Room</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {students.map((student) => (
                    <tr key={student.id}>
                      <td className="fw-semibold text-primary">{student.admissionNumber}</td>
                      <td>
                        <div className="fw-medium text-dark">{student.user?.name || student.name || 'N/A'}</div>
                        <small className="text-muted">{student.gender}</small>
                      </td>
                      <td>{student.user?.email || student.email || 'N/A'}</td>
                      <td>{student.user?.phone || student.phone || 'N/A'}</td>
                      <td>
                        <div>{student.course}</div>
                        <small className="text-muted">Year {student.yearOfStudy}</small>
                      </td>
                      <td>
                        {student.allocatedRoomNumber ? (
                          <span className="badge bg-success">Room {student.allocatedRoomNumber}</span>
                        ) : (
                          <span className="badge bg-secondary">Unallocated</span>
                        )}
                      </td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-primary"
                          onClick={() => setSelectedStudent(student)}
                        >
                          <i className="bi bi-eye me-1"></i> View Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-5 text-muted">
              <i className="bi bi-people fs-1 d-block mb-2 text-secondary"></i>
              No students found matching your criteria.
            </div>
          )}

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="d-flex justify-content-between align-items-center p-3 border-top">
              <span className="text-muted small">
                Showing Page {page + 1} of {totalPages}
              </span>
              <div className="btn-group">
                <button
                  className="btn btn-sm btn-outline-secondary"
                  disabled={page === 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
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

      {/* Student Details Modal */}
      {selectedStudent && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow">
              <div className="modal-header">
                <h5 className="modal-title fw-bold">
                  <i className="bi bi-person-badge text-primary me-2"></i> Student Profile - {selectedStudent.admissionNumber}
                </h5>
                <button type="button" className="btn-close" onClick={() => setSelectedStudent(null)}></button>
              </div>
              <div className="modal-body p-4">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="text-muted small fw-semibold">Full Name</label>
                    <div className="fw-bold">{selectedStudent.user?.name}</div>
                  </div>
                  <div className="col-md-6">
                    <label className="text-muted small fw-semibold">Email</label>
                    <div>{selectedStudent.user?.email}</div>
                  </div>
                  <div className="col-md-6">
                    <label className="text-muted small fw-semibold">Contact Phone</label>
                    <div>{selectedStudent.user?.phone}</div>
                  </div>
                  <div className="col-md-6">
                    <label className="text-muted small fw-semibold">Course & Branch</label>
                    <div>{selectedStudent.course} ({selectedStudent.branch})</div>
                  </div>
                  <div className="col-md-6">
                    <label className="text-muted small fw-semibold">Allocated Room</label>
                    <div>
                      {selectedStudent.allocatedRoomNumber ? (
                        <span className="badge bg-success">Room {selectedStudent.allocatedRoomNumber}</span>
                      ) : (
                        <span className="badge bg-secondary">Not Allocated</span>
                      )}
                    </div>
                  </div>
                  <div className="col-md-6">
                    <label className="text-muted small fw-semibold">Emergency Contact</label>
                    <div>{selectedStudent.emergencyContact}</div>
                  </div>
                  <div className="col-md-6">
                    <label className="text-muted small fw-semibold">Guardian Information</label>
                    <div>{selectedStudent.guardianName} ({selectedStudent.guardianPhone})</div>
                  </div>
                  <div className="col-12">
                    <label className="text-muted small fw-semibold">Permanent Address</label>
                    <div className="text-secondary">{selectedStudent.address}</div>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedStudent(null)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Add New Student Modal */}
      {showAddModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleCreateSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Register New Student</h5>
                  <button type="button" className="btn-close" onClick={() => setShowAddModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Full Name *</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Admission Number *</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. HOSTEL-2026-0042"
                        value={formData.admissionNumber}
                        onChange={(e) => setFormData({ ...formData, admissionNumber: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Email Address *</label>
                      <input
                        type="email"
                        className="form-control"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Phone Number *</label>
                      <input
                        type="tel"
                        className="form-control"
                        value={formData.phone}
                        onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Course *</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.course}
                        onChange={(e) => setFormData({ ...formData, course: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Branch *</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.branch}
                        onChange={(e) => setFormData({ ...formData, branch: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label small fw-semibold">Year of Study *</label>
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
                    <div className="col-md-4">
                      <label className="form-label small fw-semibold">Gender *</label>
                      <select
                        className="form-select"
                        value={formData.gender}
                        onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
                      >
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other</option>
                      </select>
                    </div>
                    <div className="col-md-4">
                      <label className="form-label small fw-semibold">Date of Birth</label>
                      <input
                        type="date"
                        className="form-control"
                        value={formData.dateOfBirth}
                        onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Guardian Name</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.guardianName}
                        onChange={(e) => setFormData({ ...formData, guardianName: e.target.value })}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Guardian Phone</label>
                      <input
                        type="tel"
                        className="form-control"
                        value={formData.guardianPhone}
                        onChange={(e) => setFormData({ ...formData, guardianPhone: e.target.value })}
                      />
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Permanent Address</label>
                      <textarea
                        className="form-control"
                        rows="2"
                        value={formData.address}
                        onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                      ></textarea>
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowAddModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={createLoading}>
                    {createLoading ? 'Saving...' : 'Register Student'}
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

export default StudentManagement;
