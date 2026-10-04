import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Unauthorized = () => {
  const { user } = useAuth();
  const homePath = user?.role === 'STUDENT' ? '/student/dashboard' : '/admin/dashboard';

  return (
    <div className="min-vh-100 d-flex flex-column align-items-center justify-content-center bg-light text-center px-3">
      <div className="text-danger mb-3">
        <i className="bi bi-shield-lock-fill" style={{ fontSize: '4.5rem' }}></i>
      </div>
      <h1 className="fw-bold text-dark mb-2">403 - Access Denied</h1>
      <p className="text-secondary lead mb-4" style={{ maxWidth: '480px' }}>
        You do not have administrative clearance to access this protected area.
      </p>
      <Link to={homePath} className="btn btn-primary px-4 py-2 fw-medium shadow-sm">
        <i className="bi bi-arrow-left me-2"></i>Return to Safe Dashboard
      </Link>
    </div>
  );
};

export default Unauthorized;
