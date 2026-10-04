import React from 'react';
import { Link } from 'react-router-dom';

const NotFound = () => {
  return (
    <div className="min-vh-100 d-flex flex-column align-items-center justify-content-center bg-light text-center px-3">
      <div className="text-primary mb-3">
        <i className="bi bi-compass-fill" style={{ fontSize: '4.5rem' }}></i>
      </div>
      <h1 className="fw-bold text-dark mb-2">404 - Page Not Found</h1>
      <p className="text-secondary lead mb-4" style={{ maxWidth: '480px' }}>
        The page you are looking for does not exist or has been moved.
      </p>
      <Link to="/" className="btn btn-primary px-4 py-2 fw-medium shadow-sm">
        <i className="bi bi-house-door me-2"></i>Go to Home
      </Link>
    </div>
  );
};

export default NotFound;
