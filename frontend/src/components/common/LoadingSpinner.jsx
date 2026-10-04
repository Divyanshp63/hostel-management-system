import React from 'react';

const LoadingSpinner = ({ message = 'Loading...', fullScreen = false }) => {
  if (fullScreen) {
    return (
      <div
        className="d-flex flex-column align-items-center justify-content-center"
        style={{ minHeight: '100vh', backgroundColor: 'var(--bg-main)' }}
      >
        <div className="spinner-border text-primary" role="status" style={{ width: '3rem', height: '3rem' }}>
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-3 text-secondary fw-medium">{message}</p>
      </div>
    );
  }

  return (
    <div className="d-flex flex-column align-items-center justify-content-center p-5">
      <div className="spinner-border text-primary" role="status">
        <span className="visually-hidden">Loading...</span>
      </div>
      <p className="mt-2 text-secondary small">{message}</p>
    </div>
  );
};

export default LoadingSpinner;
